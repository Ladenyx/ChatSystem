#!/usr/bin/env python3
"""Two-stage GitHub PR code review runner optimized for Gemini API Free Tier."""

from __future__ import annotations

from collections.abc import Iterator
from contextlib import contextmanager
from dataclasses import dataclass
import json
import os
from pathlib import Path
import re
import signal
import sys
import time
from types import FrameType
from typing import Any
import urllib.error
import urllib.request

COMMENT_MARKER = "<!-- gemini-free-tier-review -->"
GITHUB_API_BASE = "https://api.github.com"
GEMINI_API_BASE = "https://generativelanguage.googleapis.com/v1beta"

FALLBACK_STATUS_CODES = {404, 429, 500, 502, 503, 504}

SKIP_EXTENSIONS = {
    ".png",
    ".jpg",
    ".jpeg",
    ".gif",
    ".webp",
    ".ico",
    ".svg",
    ".pdf",
    ".jar",
    ".class",
    ".zip",
    ".woff",
    ".woff2",
    ".ttf",
    ".eot",
    ".lock",
}
SKIP_FILENAMES = {"package-lock.json", "pnpm-lock.yaml", "yarn.lock"}

DEFAULT_TRIAGE_MODELS = ["gemini-3.5-flash-lite", "gemini-3.8-flash"]
DEFAULT_DEEP_MODELS = ["gemini-4-hydrogen", "gemini-3.5-flash-lite"]
DEFAULT_MAX_DIFF_CHARS = 180_000
MAX_GEMINI_REQUEST_SEC = 60
GEMINI_REVIEW_BUDGET_SEC = 240
DEFAULT_DEEP_THINKING_LEVEL = "medium"

HUNK_HEADER_RE = re.compile(r"^@@ -\d+(?:,\d+)? \+(\d+)(?:,\d+)? @@")

STAGE1_SCHEMA: dict[str, Any] = {
    "type": "OBJECT",
    "properties": {
        "summary": {"type": "STRING"},
        "risk_level": {
            "type": "STRING",
            "enum": ["low", "medium", "high"],
        },
        "needs_deep_review": {"type": "BOOLEAN"},
        "risk_reasons": {
            "type": "ARRAY",
            "items": {"type": "STRING"},
        },
        "basic_findings": {
            "type": "ARRAY",
            "items": {
                "type": "OBJECT",
                "properties": {
                    "severity": {
                        "type": "STRING",
                        "enum": ["info", "warning", "error"],
                    },
                    "file": {"type": "STRING"},
                    "line": {"type": "INTEGER"},
                    "category": {"type": "STRING"},
                    "message": {"type": "STRING"},
                    "suggestion": {"type": "STRING"},
                },
                "required": ["severity", "file", "category", "message"],
            },
        },
    },
    "required": [
        "summary",
        "risk_level",
        "needs_deep_review",
        "risk_reasons",
        "basic_findings",
    ],
}


class AllModelsFailedError(RuntimeError):
    """Raised when no model responds before the chain or its time budget ends."""

    def __init__(
        self,
        stage_name: str,
        attempts: list[tuple[str, str]],
        *,
        budget_exhausted: bool = False,
    ) -> None:
        self.stage_name = stage_name
        self.attempts = attempts
        details = [f"{model}: {reason}" for model, reason in attempts]
        if budget_exhausted:
            details.append("Đã hết thời gian dành cho Gemini")
        self.details = "; ".join(details)
        message = "Gemini review budget exhausted" if budget_exhausted else "All models failed"
        super().__init__(f"[{stage_name}] {message}: {self.details}")


class _GeminiHTTPError(RuntimeError):
    def __init__(self, status_code: int, reason: str, detail: str) -> None:
        super().__init__(detail)
        self.status_code = status_code
        self.reason = reason


@dataclass
class StageExecution:
    model_used: str
    attempts: list[tuple[str, str]]
    content: Any


def parse_model_list(raw: str, default: list[str]) -> list[str]:
    """Parse comma-separated model names, preserving order and deduplicating."""
    if not raw or not raw.strip():
        return list(default)
    models: list[str] = []
    for item in raw.split(","):
        cleaned = item.strip()
        if cleaned and cleaned not in models:
            models.append(cleaned)
    return models if models else list(default)


def github_request(
    method: str,
    path: str,
    token: str,
    payload: dict[str, Any] | None = None,
    timeout_sec: int = 30,
) -> Any:
    """Execute a GitHub REST API request and return parsed JSON."""
    url = f"{GITHUB_API_BASE}{path}"
    data = json.dumps(payload).encode("utf-8") if payload is not None else None
    headers = {
        "Authorization": f"Bearer {token}",
        "Accept": "application/vnd.github+json",
        "X-GitHub-Api-Version": "2022-11-28",
        "User-Agent": "gemini-pr-review",
    }
    if data is not None:
        headers["Content-Type"] = "application/json"

    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=timeout_sec) as resp:
            raw = resp.read().decode("utf-8", errors="replace")
            return json.loads(raw) if raw.strip() else None
    except urllib.error.HTTPError as exc:
        err_body = exc.read().decode("utf-8", errors="replace")[:300]
        raise RuntimeError(
            f"GitHub API {method} {path} failed with HTTP {exc.code}: {err_body}"
        ) from exc


def fetch_pr_metadata(repo: str, pr_number: int, token: str) -> dict[str, Any]:
    """Fetch PR metadata from GitHub."""
    result = github_request("GET", f"/repos/{repo}/pulls/{pr_number}", token)
    return result if isinstance(result, dict) else {}


def fetch_pr_files(repo: str, pr_number: int, token: str) -> list[dict[str, Any]]:
    """Fetch changed files for a PR (up to 1,000 files across 10 pages)."""
    files: list[dict[str, Any]] = []
    for page in range(1, 11):
        batch = github_request(
            "GET",
            f"/repos/{repo}/pulls/{pr_number}/files?per_page=100&page={page}",
            token,
        )
        if not isinstance(batch, list) or not batch:
            break
        files.extend(batch)
        if len(batch) < 100:
            break
    return files


def upsert_pr_comment(repo: str, pr_number: int, token: str, body: str) -> str:
    """Create or update the sticky Gemini review comment on the PR."""
    existing_id: int | None = None
    for page in range(1, 11):
        comments = github_request(
            "GET",
            f"/repos/{repo}/issues/{pr_number}/comments?per_page=100&page={page}",
            token,
        )
        if not isinstance(comments, list) or not comments:
            break
        for comment in comments:
            if COMMENT_MARKER in (comment.get("body") or ""):
                existing_id = comment.get("id")
                break
        if existing_id is not None or len(comments) < 100:
            break

    if existing_id is not None:
        resp = github_request(
            "PATCH",
            f"/repos/{repo}/issues/comments/{existing_id}",
            token,
            payload={"body": body},
        )
    else:
        resp = github_request(
            "POST",
            f"/repos/{repo}/issues/{pr_number}/comments",
            token,
            payload={"body": body},
        )
    return (resp or {}).get("html_url", "")


def should_skip_diff(filename: str) -> bool:
    """Return True for binaries, lockfiles, and minified assets."""
    lower = filename.lower()
    basename = Path(lower).name
    if basename in SKIP_FILENAMES:
        return True
    if lower.endswith(".min.js") or lower.endswith(".min.css"):
        return True
    return Path(lower).suffix in SKIP_EXTENSIONS


def annotate_patch_with_line_numbers(patch: str) -> str:
    """Prefix unified diff lines with right-side line numbers for accurate citations."""
    lines_out: list[str] = []
    right_line = 0
    in_hunk = False

    for line in patch.splitlines():
        match = HUNK_HEADER_RE.match(line)
        if match:
            right_line = int(match.group(1))
            in_hunk = True
            lines_out.append(line)
            continue

        if not in_hunk:
            lines_out.append(line)
            continue

        if line.startswith("+"):
            lines_out.append(f"L{right_line:<5} | {line}")
            right_line += 1
        elif line.startswith("-") or line.startswith("\\"):
            lines_out.append(f"      | {line}")
        else:
            lines_out.append(f"L{right_line:<5} | {line}")
            right_line += 1

    return "\n".join(lines_out)


def load_repo_review_instructions(repo_root: Path) -> str:
    """Load repository-specific review instructions if present."""
    instructions_path = repo_root / ".github" / "instructions" / "solid-review.instructions.md"
    if not instructions_path.is_file():
        return ""
    text = instructions_path.read_text(encoding="utf-8", errors="replace").strip()
    if len(text) > 6_000:
        return text[:6_000] + "\n...[truncated]..."
    return text


def build_pr_bundle(
    pr_meta: dict[str, Any],
    pr_files: list[dict[str, Any]],
    max_chars: int = DEFAULT_MAX_DIFF_CHARS,
) -> tuple[str, dict[str, int]]:
    """Bundle PR metadata, file manifest, and annotated diffs into a single prompt payload."""
    title = pr_meta.get("title") or "(no title)"
    base_ref = (pr_meta.get("base") or {}).get("ref", "main")
    head_ref = (pr_meta.get("head") or {}).get("ref", "head")
    labels = [
        lbl.get("name", "")
        for lbl in (pr_meta.get("labels") or [])
        if lbl.get("name")
    ]
    description = (pr_meta.get("body") or "").strip()
    if len(description) > 3_000:
        description = description[:3_000] + "\n...[truncated]..."

    header_lines = [
        f"PR Title: {title}",
        f"Branches: {head_ref} -> {base_ref}",
        f"Labels: {', '.join(labels) if labels else '(none)'}",
        "PR Description:",
        description or "(empty)",
        "",
        f"Changed Files ({len(pr_files)}):",
    ]
    for item in pr_files:
        filename = item.get("filename", "")
        status = item.get("status", "modified")
        additions = item.get("additions", 0)
        deletions = item.get("deletions", 0)
        header_lines.append(f"- [{status}] {filename} (+{additions}/-{deletions})")
    header_lines.append("")
    header_lines.append("=== FILE DIFFS ===")

    header_text = "\n".join(header_lines) + "\n"
    remaining = max(1_000, max_chars - len(header_text))

    diff_sections: list[str] = []
    stats = {
        "total_files": len(pr_files),
        "included_diffs": 0,
        "skipped_files": 0,
        "truncated_files": 0,
        "no_patch_files": 0,
    }

    for item in pr_files:
        filename = item.get("filename", "")
        status = item.get("status", "modified")
        additions = item.get("additions", 0)
        deletions = item.get("deletions", 0)
        patch = item.get("patch")

        if should_skip_diff(filename):
            stats["skipped_files"] += 1
            continue
        if not patch:
            stats["no_patch_files"] += 1
            continue

        annotated = annotate_patch_with_line_numbers(patch)
        section = (
            f"\n### File: {filename} ({status}, +{additions}/-{deletions})\n"
            f"```diff\n{annotated}\n```\n"
        )

        if len(section) <= remaining:
            diff_sections.append(section)
            remaining -= len(section)
            stats["included_diffs"] += 1
        elif stats["included_diffs"] == 0 and remaining > 300:
            # Always include at least a partial diff if the very first file exceeds the cap.
            prefix = f"\n### File: {filename} ({status}, +{additions}/-{deletions})\n```diff\n"
            suffix = "\n...[diff truncated due to token budget]...\n```\n"
            allowance = max(100, remaining - len(prefix) - len(suffix))
            diff_sections.append(prefix + annotated[:allowance] + suffix)
            remaining = 0
            stats["included_diffs"] += 1
            stats["truncated_files"] += 1
        else:
            stats["truncated_files"] += 1

    bundle = header_text + "".join(diff_sections)
    if stats["truncated_files"] > 0:
        bundle += (
            f"\n[Note: {stats['truncated_files']} file diff(s) omitted or truncated "
            f"to stay within the {max_chars:,}-char Free Tier budget.]\n"
        )
    return bundle, stats


def extract_response_text(data: dict[str, Any]) -> str:
    """Extract non-thought text from Gemini generateContent response JSON."""
    candidates = data.get("candidates") or []
    if not candidates:
        feedback = data.get("promptFeedback")
        raise RuntimeError(f"Gemini returned no candidates (promptFeedback={feedback})")

    first = candidates[0]
    parts = (first.get("content") or {}).get("parts") or []
    non_thought_texts = [
        part["text"]
        for part in parts
        if isinstance(part, dict)
        and isinstance(part.get("text"), str)
        and part.get("thought") is not True
    ]
    if non_thought_texts:
        return "".join(non_thought_texts).strip()

    all_texts = [
        part["text"]
        for part in parts
        if isinstance(part, dict) and isinstance(part.get("text"), str)
    ]
    joined = "".join(all_texts).strip()
    if joined:
        return joined

    finish_reason = first.get("finishReason")
    raise RuntimeError(f"Gemini candidate contained no text (finishReason={finish_reason})")


@contextmanager
def _limit_gemini_request_time(timeout_sec: float) -> Iterator[None]:
    """Enforce a wall-clock limit on the Ubuntu runner, including response reads."""

    def on_timeout(_signum: int, _frame: FrameType | None) -> None:
        raise TimeoutError("Gemini request exceeded its time limit")

    previous_handler = signal.signal(signal.SIGALRM, on_timeout)
    try:
        signal.setitimer(signal.ITIMER_REAL, timeout_sec)
        yield
    finally:
        signal.setitimer(signal.ITIMER_REAL, 0)
        signal.signal(signal.SIGALRM, previous_handler)


def call_gemini_once(
    model: str,
    api_key: str,
    system_prompt: str,
    user_prompt: str,
    generation_config: dict[str, Any],
    timeout_sec: float = MAX_GEMINI_REQUEST_SEC,
) -> str:
    """Call Gemini REST generateContent once using x-goog-api-key header."""
    url = f"{GEMINI_API_BASE}/models/{model}:generateContent"
    body = {
        "systemInstruction": {"parts": [{"text": system_prompt}]},
        "contents": [{"role": "user", "parts": [{"text": user_prompt}]}],
        "generationConfig": generation_config,
    }
    headers = {
        "Content-Type": "application/json",
        "x-goog-api-key": api_key,
    }
    req = urllib.request.Request(
        url,
        data=json.dumps(body).encode("utf-8"),
        headers=headers,
        method="POST",
    )
    with _limit_gemini_request_time(timeout_sec):
        try:
            with urllib.request.urlopen(req, timeout=timeout_sec) as resp:
                raw = resp.read().decode("utf-8", errors="replace")
        except urllib.error.HTTPError as exc:
            with exc:
                short_reason, full_detail = _format_http_error(exc)
            raise _GeminiHTTPError(exc.code, short_reason, full_detail) from exc
        return extract_response_text(json.loads(raw))


def _format_http_error(exc: urllib.error.HTTPError) -> tuple[str, str]:
    raw = exc.read().decode("utf-8", errors="replace")
    compact = " ".join(raw.split())[:220]
    full_detail = f"HTTP {exc.code}: {compact}" if compact else f"HTTP {exc.code}"

    short_reason = f"HTTP {exc.code}"
    try:
        parsed = json.loads(raw)
    except json.JSONDecodeError:
        parsed = None
    if isinstance(parsed, dict):
        err_obj = parsed.get("error")
        if isinstance(err_obj, dict) and err_obj.get("status"):
            short_reason = f"HTTP {exc.code} {err_obj['status']}"

    return short_reason, full_detail


def _format_network_error(exc: BaseException, timeout_sec: float) -> tuple[str, str]:
    full_detail = f"{type(exc).__name__}: {str(exc)[:200]}"
    is_timeout = isinstance(exc, TimeoutError) or (
        isinstance(exc, urllib.error.URLError) and isinstance(exc.reason, TimeoutError)
    )
    short_reason = f"Timeout ({timeout_sec:.3g}s)" if is_timeout else type(exc).__name__
    return short_reason, full_detail


def call_gemini_with_fallback(
    models: list[str],
    api_key: str,
    system_prompt: str,
    user_prompt: str,
    generation_config: dict[str, Any],
    timeout_sec: float = MAX_GEMINI_REQUEST_SEC,
    stage_name: str = "Stage",
    *,
    deadline: float | None = None,
) -> tuple[str, str, list[tuple[str, str]]]:
    """Try candidate models in order, falling back on 429/404/5xx/network/timeout errors."""
    if deadline is None:
        deadline = time.monotonic() + GEMINI_REVIEW_BUDGET_SEC
    attempts: list[tuple[str, str]] = []
    optional_keys = [key for key in ("responseSchema", "thinkingConfig") if key in generation_config]
    relaxed_config = {key: value for key, value in generation_config.items() if key not in optional_keys}

    for model in models:
        config = generation_config
        while True:
            remaining_sec = deadline - time.monotonic()
            if remaining_sec <= 0:
                raise AllModelsFailedError(stage_name, attempts, budget_exhausted=True)
            request_timeout = min(timeout_sec, MAX_GEMINI_REQUEST_SEC, remaining_sec)
            print(f"[{stage_name}] Calling {model} (timeout {request_timeout:.3g}s)...")

            try:
                text = call_gemini_once(
                    model=model,
                    api_key=api_key,
                    system_prompt=system_prompt,
                    user_prompt=user_prompt,
                    generation_config=config,
                    timeout_sec=request_timeout,
                )
                return text, model, attempts
            except _GeminiHTTPError as exc:
                if exc.status_code == 400 and config is generation_config and optional_keys:
                    print(
                        f"[{stage_name}] {model} rejected {', '.join(optional_keys)} with HTTP 400."
                    )
                    config = relaxed_config
                    continue
                if exc.status_code not in FALLBACK_STATUS_CODES:
                    raise RuntimeError(
                        f"[{stage_name}] Non-retryable error from {model}: {exc}"
                    ) from exc
                short_reason, full_detail = exc.reason, str(exc)
            except (urllib.error.URLError, TimeoutError) as exc:
                short_reason, full_detail = _format_network_error(exc, request_timeout)

            attempts.append((model, short_reason))
            print(f"[{stage_name}] {model} failed ({full_detail}).")
            break

    raise AllModelsFailedError(stage_name, attempts, budget_exhausted=time.monotonic() >= deadline)


def parse_stage1_json(raw_text: str) -> dict[str, Any]:
    """Parse and normalize Stage 1 JSON output."""
    cleaned = raw_text.strip()
    if cleaned.startswith("```"):
        cleaned = re.sub(r"^```[a-zA-Z0-9_-]*\s*", "", cleaned)
        cleaned = re.sub(r"\s*```$", "", cleaned)

    try:
        data = json.loads(cleaned)
    except json.JSONDecodeError:
        match = re.search(r"\{.*\}", cleaned, flags=re.DOTALL)
        if not match:
            return {
                "summary": cleaned[:1000] or "Không thể phân tích JSON phản hồi từ Bước 1.",
                "risk_level": "medium",
                "needs_deep_review": False,
                "risk_reasons": ["Phản hồi JSON từ Bước 1 không hợp lệ."],
                "basic_findings": [],
            }
        data = json.loads(match.group(0))

    risk_level = str(data.get("risk_level", "medium")).strip().lower()
    if risk_level not in {"low", "medium", "high"}:
        risk_level = "medium"

    raw_reasons = data.get("risk_reasons")
    risk_reasons = (
        [str(r).strip() for r in raw_reasons if str(r).strip()]
        if isinstance(raw_reasons, list)
        else []
    )

    raw_findings = data.get("basic_findings")
    findings: list[dict[str, Any]] = []
    if isinstance(raw_findings, list):
        for item in raw_findings:
            if not isinstance(item, dict):
                continue
            sev = str(item.get("severity", "info")).strip().lower()
            if sev not in {"info", "warning", "error"}:
                sev = "info"
            line_val = item.get("line")
            line_int = line_val if isinstance(line_val, int) and line_val > 0 else None
            findings.append(
                {
                    "severity": sev,
                    "file": str(item.get("file", "")).strip(),
                    "line": line_int,
                    "category": str(item.get("category", "general")).strip(),
                    "message": str(item.get("message", "")).strip(),
                    "suggestion": str(item.get("suggestion", "")).strip(),
                }
            )

    return {
        "summary": str(data.get("summary", "")).strip(),
        "risk_level": risk_level,
        "needs_deep_review": bool(data.get("needs_deep_review", False)),
        "risk_reasons": risk_reasons,
        "basic_findings": findings,
    }


def run_stage1(
    api_key: str,
    models: list[str],
    pr_bundle: str,
    repo_instructions: str,
    timeout_sec: int,
    *,
    deadline: float | None = None,
) -> StageExecution:
    """Execute Stage 1: single-request triage, summary, and basic checks."""
    system_prompt = (
        "Bạn là kỹ sư review code tự động (Bước 1 - Sàng lọc & Check lỗi cơ bản). "
        "Hãy phản hồi hoàn toàn bằng tiếng Việt (giữ nguyên tên file, symbol, API, thuật ngữ kỹ thuật bằng tiếng Anh) "
        "và trả về đúng JSON theo schema yêu cầu.\n\n"
        "Nhiệm vụ trong 1 request duy nhất này:\n"
        "1. Tóm tắt ngắn gọn nội dung thay đổi của PR (2-4 câu).\n"
        "2. Kiểm tra lỗi cơ bản trên toàn bộ các file được gộp: lỗi cú pháp, convention, format, chính tả, "
        "import/biến thừa, hoặc bug rõ ràng (chỉ nêu phát hiện có căn cứ từ diff, ghi đúng đường dẫn file và số dòng L<số> bên phải).\n"
        "3. Đánh giá risk_level ('low', 'medium', 'high') và needs_deep_review (boolean).\n"
        "   Chỉ đặt risk_level='high' hoặc needs_deep_review=true khi phát hiện ít nhất một yếu tố sau:\n"
        "   - Thay đổi bảo mật, xác thực, phân quyền, session, E2EE, secrets/credentials.\n"
        "   - Thay đổi SQL schema/migration, concurrency/locking, hoặc logic xử lý dữ liệu dễ gây race condition/mất dữ liệu.\n"
        "   - Thay đổi contract API/DTO/OpenAPI có nguy cơ breaking change giữa các module.\n"
        "   - Thay đổi GitHub Actions workflow permissions hoặc script nhạy cảm.\n"
        "   - Nghi ngờ bug logic nghiêm trọng cần phân tích kiến trúc sâu hơn.\n"
        "   Với PR tài liệu nhỏ, sửa typo, refactor nhẹ không rủi ro, đặt risk_level='low' hoặc 'medium' và needs_deep_review=false."
    )

    user_parts = [pr_bundle]
    if repo_instructions:
        user_parts.append(
            "\n=== REPOSITORY REVIEW GUIDELINES ===\n" + repo_instructions
        )

    generation_config: dict[str, Any] = {
        "temperature": 0.1,
        "maxOutputTokens": 4096,
        "responseMimeType": "application/json",
        "responseSchema": STAGE1_SCHEMA,
    }

    raw_text, model_used, attempts = call_gemini_with_fallback(
        models=models,
        api_key=api_key,
        system_prompt=system_prompt,
        user_prompt="\n".join(user_parts),
        generation_config=generation_config,
        timeout_sec=timeout_sec,
        stage_name="Stage 1 (Triage)",
        deadline=deadline,
    )
    parsed = parse_stage1_json(raw_text)
    return StageExecution(model_used=model_used, attempts=attempts, content=parsed)


def should_run_stage2(
    stage1_result: dict[str, Any],
    pr_labels: list[str],
    force_deep: bool,
) -> tuple[bool, str]:
    """Decide whether Stage 2 deep review should run."""
    if force_deep:
        return True, "Được kích hoạt ép chạy Bước 2 (`force_deep_review` / nhãn `deep-review`)."
    normalized_labels = {lbl.strip().lower() for lbl in pr_labels}
    if "deep-review" in normalized_labels:
        return True, "PR đang gắn nhãn `deep-review`."
    if stage1_result.get("risk_level") == "high":
        return True, "Bước 1 đánh giá mức rủi ro `high`."
    if stage1_result.get("needs_deep_review") is True:
        return True, "Bước 1 đề xuất cần review chuyên sâu (`needs_deep_review: true`)."
    return False, "Mức rủi ro thấp/trung bình — bỏ qua Bước 2 để tiết kiệm quota Free Tier."


def run_stage2(
    api_key: str,
    models: list[str],
    pr_bundle: str,
    repo_instructions: str,
    stage1_result: dict[str, Any],
    timeout_sec: int,
    thinking_level: str = DEFAULT_DEEP_THINKING_LEVEL,
    *,
    deadline: float | None = None,
) -> StageExecution:
    """Execute Stage 2: deep architecture, security, and SOLID/DRY review with fallback."""
    system_prompt = (
        "Bạn là Principal Software Engineer thực hiện **Bước 2 – Review chuyên sâu** trước khi merge. "
        "Hãy trả lời bằng tiếng Việt rõ ràng, súc tích, giữ nguyên thuật ngữ/tên symbol/file bằng tiếng Anh.\n\n"
        "Trọng tâm phân tích:\n"
        "1. Lỗi logic, edge case, concurrency/race condition, xử lý lỗi & trạng thái biên.\n"
        "2. Lỗ hổng bảo mật, phân quyền, rò rỉ bí mật/credentials, tiêm nhiễm (injection/workflow safety).\n"
        "3. Tính nhất quán contract (DTO/OpenAPI/SQL/Docs) và kiến trúc (SOLID, DRY, coupling).\n\n"
        "Quy tắc:\n"
        "- Chỉ báo cáo vấn đề có căn cứ trực tiếp từ diff; trích dẫn rõ `file:L<line>`.\n"
        "- Sắp xếp theo mức độ nghiêm trọng: Critical -> High -> Medium -> Low.\n"
        "- Không lặp lại các lỗi chính tả/format nhỏ đã nêu ở Bước 1, không viết lời khen chung chung.\n"
        "- Nếu không phát hiện vấn đề nghiêm trọng nào, nêu rõ 'Không phát hiện rủi ro kiến trúc hoặc bảo mật đáng kể.' kèm các lưu ý kiểm thử ngắn gọn."
    )

    stage1_context = json.dumps(
        {
            "summary": stage1_result.get("summary"),
            "risk_level": stage1_result.get("risk_level"),
            "risk_reasons": stage1_result.get("risk_reasons"),
        },
        ensure_ascii=False,
        indent=2,
    )
    user_parts = [
        "=== TRIAGE CONTEXT FROM STAGE 1 ===\n" + stage1_context,
        "\n" + pr_bundle,
    ]
    if repo_instructions:
        user_parts.append(
            "\n=== REPOSITORY REVIEW GUIDELINES ===\n" + repo_instructions
        )

    generation_config: dict[str, Any] = {
        "temperature": 0.2,
        "maxOutputTokens": 8192,
    }
    if thinking_level and thinking_level != "none":
        generation_config["thinkingConfig"] = {"thinkingLevel": thinking_level}

    text, model_used, attempts = call_gemini_with_fallback(
        models=models,
        api_key=api_key,
        system_prompt=system_prompt,
        user_prompt="\n".join(user_parts),
        generation_config=generation_config,
        timeout_sec=timeout_sec,
        stage_name="Stage 2 (Deep Review)",
        deadline=deadline,
    )
    return StageExecution(model_used=model_used, attempts=attempts, content=text)


def _format_attempts(attempts: list[tuple[str, str]]) -> str:
    if not attempts:
        return ""
    parts = [f"`{m}` ({err})" for m, err in attempts]
    return " → fallback từ: " + ", ".join(parts)


def render_markdown_comment(
    pr_number: int,
    bundle_stats: dict[str, int],
    stage1_exec: StageExecution,
    stage2_decision: tuple[bool, str],
    stage2_exec: StageExecution | None = None,
    stage2_error: str | None = None,
) -> str:
    """Render the PR review report in Markdown with sticky comment marker."""
    s1 = stage1_exec.content
    risk = s1["risk_level"]
    risk_badge = {"low": "LOW", "medium": "MEDIUM", "high": "HIGH"}.get(risk, risk.upper())
    run_stage2_flag, decision_reason = stage2_decision
    api_calls = 1 + (1 if stage2_exec is not None else 0)

    lines: list[str] = [
        COMMENT_MARKER,
        f"## Gemini Free-Tier PR Review (#{pr_number})",
        "",
        f"- **Mức độ rủi ro:** `{risk_badge}`",
        f"- **Số API request đã dùng:** `{api_calls}` (gộp `{bundle_stats['included_diffs']}/{bundle_stats['total_files']}` file diff)",
        f"- **Bước 1 (Sàng lọc):** `{stage1_exec.model_used}`{_format_attempts(stage1_exec.attempts)}",
    ]

    if stage2_exec is not None:
        lines.append(
            f"- **Bước 2 (Chuyên sâu):** `{stage2_exec.model_used}`{_format_attempts(stage2_exec.attempts)} — {decision_reason}"
        )
    elif stage2_error:
        lines.append(
            f"- **Bước 2 (Chuyên sâu):** ⚠️ Không khả dụng ({stage2_error}) — {decision_reason}"
        )
    else:
        lines.append(f"- **Bước 2 (Chuyên sâu):** Đã bỏ qua — {decision_reason}")

    if bundle_stats.get("skipped_files") or bundle_stats.get("truncated_files"):
        lines.append(
            f"- **Ghi chú diff:** bỏ qua `{bundle_stats.get('skipped_files', 0)}` file binary/lockfile, "
            f"cắt bớt `{bundle_stats.get('truncated_files', 0)}` file vượt ngưỡng token."
        )

    lines.extend(["", "### 1. Tóm tắt & Sàng lọc (Bước 1)", "", s1["summary"] or "_Không có tóm tắt._"])

    if s1["risk_reasons"]:
        lines.extend(["", "**Lý do đánh giá rủi ro:**"])
        for reason in s1["risk_reasons"]:
            lines.append(f"- {reason}")

    findings = s1["basic_findings"]
    lines.extend(["", "**Kiểm tra cơ bản (cú pháp, convention, format, chính tả):**"])
    if not findings:
        lines.append("- Không phát hiện lỗi cơ bản đáng chú ý.")
    else:
        for f in findings:
            loc = f"`{f['file']}:{f['line']}`" if f.get("line") else f"`{f['file']}`"
            sug = f" → *Gợi ý:* {f['suggestion']}" if f.get("suggestion") else ""
            lines.append(
                f"- **[{f['severity'].upper()} | {f['category']}]** {loc}: {f['message']}{sug}"
            )

    if run_stage2_flag:
        lines.extend(["", "### 2. Review chuyên sâu (Bước 2)", ""])
        if stage2_exec is not None:
            lines.append(str(stage2_exec.content).strip())
        elif stage2_error:
            lines.append(
                "> ⚠️ **Review chuyên sâu chưa hoàn tất.** "
                "Kết quả Bước 1 ở trên vẫn đã được giữ lại để không làm gián đoạn PR."
            )
    else:
        lines.extend(
            [
                "",
                "---",
                "💡 *Muốn chạy **Bước 2 (Review chuyên sâu với `gemini-4-hydrogen` + fallback)**? "
                "Gắn nhãn **`deep-review`** cho PR này hoặc chạy tay workflow `Gemini PR Review`.*",
            ]
        )

    return "\n".join(lines).strip() + "\n"


def _write_step_summary(markdown: str) -> None:
    summary_path = os.environ.get("GITHUB_STEP_SUMMARY", "").strip()
    if not summary_path:
        return
    try:
        with open(summary_path, "a", encoding="utf-8") as fh:
            fh.write(markdown + "\n")
    except OSError as exc:
        print(f"::warning::Failed to write GITHUB_STEP_SUMMARY: {exc}")


def main() -> int:
    api_key = os.environ.get("GEMINI_API_KEY", "").strip()
    if not api_key:
        print(
            "::notice::GEMINI_API_KEY is not set (for example on a fork PR); skipping Gemini PR review."
        )
        return 0

    github_token = os.environ.get("GITHUB_TOKEN", "").strip()
    repo = os.environ.get("GITHUB_REPOSITORY", "").strip()
    pr_raw = os.environ.get("PR_NUMBER", "").strip()
    if not github_token or not repo or not pr_raw:
        print("::error::Missing required GITHUB_TOKEN, GITHUB_REPOSITORY, or PR_NUMBER.")
        return 1

    try:
        pr_number = int(pr_raw)
    except ValueError:
        print(f"::error::Invalid PR_NUMBER: {pr_raw!r}")
        return 1

    force_deep = os.environ.get("FORCE_DEEP_REVIEW", "").strip().lower() in {
        "1",
        "true",
        "yes",
    }
    triage_models = parse_model_list(
        os.environ.get("GEMINI_TRIAGE_MODELS", ""), DEFAULT_TRIAGE_MODELS
    )
    deep_models = parse_model_list(
        os.environ.get("GEMINI_DEEP_MODELS", ""), DEFAULT_DEEP_MODELS
    )
    max_diff_chars = int(
        os.environ.get("GEMINI_MAX_DIFF_CHARS", str(DEFAULT_MAX_DIFF_CHARS))
    )
    try:
        timeout_sec = int(os.environ.get("GEMINI_TIMEOUT_SEC", str(MAX_GEMINI_REQUEST_SEC)))
    except ValueError:
        timeout_sec = 0
    if timeout_sec <= 0:
        print("::error::GEMINI_TIMEOUT_SEC must be a positive integer.")
        return 1
    deep_thinking_level = os.environ.get(
        "GEMINI_DEEP_THINKING_LEVEL", DEFAULT_DEEP_THINKING_LEVEL
    ).strip().lower()

    # Include PR preparation in this deadline to leave time to publish within the five-minute job.
    deadline = time.monotonic() + GEMINI_REVIEW_BUDGET_SEC
    print(f"Fetching PR #{pr_number} from {repo}...")
    pr_meta = fetch_pr_metadata(repo, pr_number, github_token)
    pr_files = fetch_pr_files(repo, pr_number, github_token)
    if not pr_files:
        print("::notice::No changed files found in PR; skipping review.")
        return 0

    pr_bundle, bundle_stats = build_pr_bundle(pr_meta, pr_files, max_chars=max_diff_chars)
    repo_instructions = load_repo_review_instructions(Path.cwd())
    print(
        f"Bundled {bundle_stats['included_diffs']}/{bundle_stats['total_files']} diffs "
        f"({len(pr_bundle):,} chars)."
    )

    try:
        stage1_exec = run_stage1(
            api_key=api_key,
            models=triage_models,
            pr_bundle=pr_bundle,
            repo_instructions=repo_instructions,
            timeout_sec=timeout_sec,
            deadline=deadline,
        )
    except AllModelsFailedError as exc:
        print(f"::warning::{exc}")
        return 0
    except RuntimeError as exc:
        print(f"::error::{exc}")
        return 1

    pr_labels = [
        lbl.get("name", "")
        for lbl in (pr_meta.get("labels") or [])
        if lbl.get("name")
    ]
    stage2_decision = should_run_stage2(stage1_exec.content, pr_labels, force_deep)
    run_stage2_flag, decision_reason = stage2_decision
    print(f"Stage 2 decision: run={run_stage2_flag} ({decision_reason})")

    stage2_exec: StageExecution | None = None
    stage2_error: str | None = None
    if run_stage2_flag:
        try:
            stage2_exec = run_stage2(
                api_key=api_key,
                models=deep_models,
                pr_bundle=pr_bundle,
                repo_instructions=repo_instructions,
                stage1_result=stage1_exec.content,
                timeout_sec=timeout_sec,
                thinking_level=deep_thinking_level,
                deadline=deadline,
            )
        except AllModelsFailedError as exc:
            stage2_error = exc.details
            print(f"::warning::{exc}")
        except RuntimeError as exc:
            print(f"::error::{exc}")
            return 1

    markdown = render_markdown_comment(
        pr_number=pr_number,
        bundle_stats=bundle_stats,
        stage1_exec=stage1_exec,
        stage2_decision=stage2_decision,
        stage2_exec=stage2_exec,
        stage2_error=stage2_error,
    )

    _write_step_summary(markdown)
    comment_url = upsert_pr_comment(repo, pr_number, github_token, markdown)
    print(f"Posted/updated PR review comment: {comment_url}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
