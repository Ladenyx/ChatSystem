# Repository Guidelines

## Project Structure & Module Organization

This Java chat project is in design; application code and build configuration do not exist yet.

- `README.md`: project overview, proposed architecture, and submission layout.
- `ROADMAP.md`: feature IDs, UI/logic status, owners, and deliverables.
- `docs/requirements/`: requirements and use cases; `use-cases/UC01-signup.md` documents registration.
- `.github/DISCUSSION_TEMPLATE/`: design-decision and Q&A forms.
- `Final Project.pdf`: course specification.

The planned layout uses `src/admin/`, `src/user/`, `src/server/`, and `src/common/`. Database scripts belong in `script/`; release artifacts in `release/`; reports in `report/`; commit screenshots in `git/`; documentation assets in `docs/images/`. These directories are not implemented yet. No test directory exists.

### Course Milestones & Scope Decisions (`Final Project.pdf`)

- **Version 1 (20% — UI & Database)**: `src/` must contain actual runnable Java desktop GUI code for both Admin and User screens (layouts/views ready to launch and screenshot for the report; Figma/static mockups alone do not satisfy `src/`). Event-driven business logic, live DB queries, and socket messaging are not required yet, though basic navigation between screens is recommended. Also requires the DB creation script in `script/`.
- **Version 2 (80% — Full System)**: Complete working Admin and User subsystems in `src/`, runnable `.jar` files plus an editable DB config file in `release/`, DB script in `script/`, commit screenshots in `git/`, full report, and self-assessment form.
- **Admin Subsystem is a Desktop GUI (`Admin App`)**: Both User and Admin applications are graphical desktop UIs (never CLI/terminal consoles), providing interactive tables, filters, and visual charts for Admin analytics (`A-12`–`A-16`). Avoid calling the Admin app "Console" to prevent CLI confusion.
- **LLM Assistant (`U-27`)**: Implements **both** capabilities via **two separate UI buttons** on the chat compose bar — one button to refine/edit the current draft and one button to recommend/suggest replies from conversation context.
- **Course-Project Engineering Threshold (No Over-Engineering)**: Build strictly to the standard required for a university course project — clean, modular, runnable, and directly fulfilling `Final Project.pdf` and `ROADMAP.md`. Do not add production-scale infrastructure, speculative abstractions, excessive edge-case scaffolding, or defensive boilerplate beyond what the course demo, grading rubric, and core tests actually need.

## Build, Test, and Development Commands

Run from the repository root:

- `git status --short`: inspect local changes before editing.
- `git diff --check`: detect whitespace errors in tracked changes.
- `git diff -- README.md ROADMAP.md docs/`: review tracked documentation; inspect new files separately.
- `rg 'U-01|UC-01' ROADMAP.md docs/`: locate related requirements.

No application or build/test command exists. Do not present `mvn` or `gradle` examples as working commands. Document build, test, and launch instructions when tooling is selected. The README targets JDK 17+.

## Coding Style & Naming Conventions

For new Java code, use four-space indentation, `UpperCamelCase` classes, `lowerCamelCase` methods/fields, `UPPER_SNAKE_CASE` constants, and lowercase packages. Keep UI, server logic, and shared protocol models separate. No formatter or linter is configured.

Keep Markdown headings descriptive and tables consistent. Preserve roadmap IDs such as `U-01` and `A-01`; reference them in use cases and changes.

## Testing Guidelines

No test framework or coverage threshold is configured. For documentation, verify requirement links, feature IDs, and acceptance criteria. When Java testing is introduced, document the framework and command; name test classes `<ClassName>Test`. Cover validation failures, authorization, offline delivery, and concurrent registration where relevant. Report checks actually performed.

## Commit & Pull Request Guidelines

History mainly uses `docs:` with one `doc:` entry. Follow the README's Conventional Commits guidance, for example `docs(U-01): clarify signup rules`. Use standard branch prefixes such as `feature/`, `fix/`, or `docs/` for all work, including agent work; do not use `codex/` branches.

PRs should explain changes, reference feature IDs or issues, record verification, and include screenshots for UI changes. Update roadmap status/ownership when applicable and obtain one teammate review. Each member must contribute their own commits.

## Security & Configuration

Never commit database passwords, SMTP credentials, or LLM keys. Provide placeholder configuration examples and verify ignore rules before adding local configuration. Preserve unrelated local changes.
