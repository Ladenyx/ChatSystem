# Repository Guidelines

## Project Structure & Module Organization

This Java chat system is a multi-module Maven project (`pom.xml`, `./mvnw` / `mvnw.cmd`) targeting **JDK 25** (enforced by `maven-enforcer-plugin` `[25,26)` and Maven `[3.9,4.0)`).

- `src/common/`: shared DTOs, API contracts, and WebSocket protocol models (`chatsystem-common`).
- `src/server/`: Spring Boot backend REST/WebSocket server (`chatsystem-server`).
- `src/desktop-ui/`: shared JavaFX 25 / AtlantaFX theme, navigation, async state, skeletons, and HTTP/WS client helpers (`chatsystem-desktop-ui`).
- `src/user/`: User desktop client (`chatsystem-user`).
- `src/admin/`: Admin desktop GUI (`chatsystem-admin`).
- `script/`: database schema and seed scripts (`compose.yaml` and `.env.example` configure local services).
- `README.md`, `ROADMAP.md`, `docs/`: project overview, feature IDs/ownership, requirements, and architecture notes (including `docs/architecture/desktop-ui.md` and `docs/images/`).
- `release/`, `report/`, `git/`: submission packages, course report, and commit screenshots.
- `.github/DISCUSSION_TEMPLATE/`: design-decision and Q&A forms.

### Course Milestones & Scope Decisions (`Final Project.pdf`)

- **Version 1 (20% — UI & Database)**: `src/` must contain actual runnable Java desktop GUI code for both Admin and User screens (layouts/views ready to launch and screenshot for the report; Figma/static mockups alone do not satisfy `src/`). Event-driven business logic, live DB queries, and socket messaging are not required yet, though basic navigation between screens is recommended. Also requires the DB creation script in `script/`.
- **Version 2 (80% — Full System)**: Complete working Admin and User subsystems in `src/`, runnable `.jar` files plus an editable DB config file in `release/`, DB script in `script/`, commit screenshots in `git/`, full report, and self-assessment form.
- **Admin Subsystem is a Desktop GUI (`Admin App`)**: Both User and Admin applications are graphical desktop UIs (never CLI/terminal consoles), providing interactive tables, filters, and visual charts for Admin analytics (`A-12`–`A-16`). Avoid calling the Admin app "Console" to prevent CLI confusion.
- **LLM Assistant (`U-27`)**: Implements **both** capabilities via **two separate UI buttons** on the chat compose bar — one button to refine/edit the current draft and one button to recommend/suggest replies from conversation context.
- **Course-Project Engineering Threshold (No Over-Engineering)**: Build strictly to the standard required for a university course project — clean, modular, runnable, and directly fulfilling `Final Project.pdf` and `ROADMAP.md`. Do not add production-scale infrastructure, speculative abstractions, excessive edge-case scaffolding, or defensive boilerplate beyond what the course demo, grading rubric, and core tests actually need.

## Build, Test, and Development Commands

Run from the repository root (with JDK 25 in `JAVA_HOME`):

- `./mvnw clean verify`: compile and run headless unit tests across all modules.
- `./mvnw -Pui-tests verify`: run full verification including JavaFX UI tests (requires a display server or `xvfb-run`).
- `./mvnw -pl src/<module> -am test`: run tests for a specific module and its local dependencies.
- `git status --short`: inspect local changes before editing.
- `git diff --check`: detect whitespace errors in tracked changes.
- `rg 'U-01|UC-01' ROADMAP.md docs/`: locate related requirements.

## Coding Style & Naming Conventions

For new Java code, use four-space indentation, `UpperCamelCase` classes, `lowerCamelCase` methods/fields, `UPPER_SNAKE_CASE` constants, and lowercase packages. Keep UI, server logic, and shared protocol models separate. No formatter or linter is configured.

Keep Markdown headings descriptive and tables consistent. Preserve roadmap IDs such as `U-01` and `A-01`; reference them in use cases and changes.

## Testing Guidelines

Tests use **JUnit 5** (`junit-jupiter`) via Maven Surefire under `src/*/src/test/java/`. No coverage threshold is enforced. Name test classes `<ClassName>Test`. JavaFX UI tests are gated by `-Dchatsystem.uiTests=true` (enabled by `-Pui-tests`). Cover validation failures, authorization, offline delivery, async state transitions, and concurrent flows where relevant. For documentation, verify requirement links, feature IDs, and acceptance criteria. Report checks actually performed, including skipped UI tests when `-Pui-tests` is not run.

## Commit & Pull Request Guidelines

Follow Conventional Commits, for example `docs(U-01): clarify signup rules` or `feat(desktop-ui): add skeleton states`. Use standard branch prefixes such as `feature/`, `fix/`, or `docs/` for all work, including agent work; do not use `codex/` branches.

PRs should explain changes, reference feature IDs or issues, record verification, and include screenshots for UI changes. Update roadmap status/ownership when applicable and obtain one teammate review. Each member must contribute their own commits.

## Security & Configuration

Never commit database passwords, SMTP credentials, or LLM keys. Provide placeholder configuration examples and verify ignore rules before adding local configuration. Preserve unrelated local changes.
