# Copilot instructions

## Project at a glance

WhatsUp (ChatSystem) is a Java desktop chat project: JavaFX user and admin clients, shared UI and protocol modules, a server module, and a PostgreSQL schema/test suite. The repository is mid-sized (about 160 Java files) and primarily Java, FXML, CSS, SQL, and Markdown. Important distinction: the clients are still offline previews backed by deterministic mock data, and the server is a Version 2 scaffold; do not assume documented future features are implemented. See `README.md` and `ROADMAP.md` for current scope.

## Layout and where to work

- `pom.xml`: Maven reactor and centrally managed versions. `mvnw` / `mvnw.cmd`: Maven 3.9.11 wrapper.
- `src/common`: shared Java DTOs, domain types, and REST/WebSocket protocol models; keep it independent of JavaFX and server frameworks.
- `src/desktop-ui`: shared JavaFX navigation, themes, overlays, and mock data.
- `src/user`, `src/admin`: client controllers and tests under `src/{main,test}/java`; FXML, CSS, images, and fonts under `src/main/resources/com/chatsystem/{user,admin}/`. Keep changes within the corresponding feature package and resource directory; update registered routes in `UserViews` / `AdminViews` when adding a view.
- `src/server`: intended backend module; currently only a scaffold.
- `script/schema.sql`, `script/sql/`, `script/tests/`: PostgreSQL 18 schema and SQL checks. `00-core-users.sql` is the single shared `users` definition; the other DDL files are divided by domain.
- `docs/requirements/`, `docs/decisions/`, `docs/architecture/`, `docs/databases/`: requirements, domain decisions, UI/API contracts, and ERDs. Treat these as design context; check roadmap status before implementing a planned feature.
- Root guidance: `README.md`, `ROADMAP.md`, `compose.yaml`, `.env.example`, `script/README.md`. There is no root `CONTRIBUTING.md`, Java linter configuration, or lint script. Contribution/commit guidance is at the end of `README.md`.

## Build and validate

Always use **JDK 25** (the README recommends Eclipse Temurin) and the Maven Wrapper; the parent POM rejects Java outside `[25,26)` and Maven outside `[3.9,4.0)`. From the repository root:

```sh
./mvnw --version
./mvnw validate
./mvnw clean verify
```

`clean verify` is the required Java build and test check. For a focused module plus its dependencies, use e.g. `./mvnw -pl src/user -am verify`. There is no separate lint command. The optional JavaFX suite needs a desktop session or Xvfb on Linux:

```sh
./mvnw -Pui-tests verify
# Linux without a display:
xvfb-run -a ./mvnw -Pui-tests verify
```

Observed validation notes: JDK 27 fails immediately in Maven Enforcer; select JDK 25 via `JAVA_HOME` rather than bypassing the check. In this environment a fresh Ubuntu JDK 25 install also lacked the CA trust used by Maven Central; Maven then failed with `PKIX path building failed`. Restore JDK/system CA trust instead of disabling TLS. The temporary, environment-specific workaround that passed here was:

```sh
JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64 PATH=/usr/lib/jvm/java-25-openjdk-amd64/bin:$PATH MAVEN_OPTS='-Djavax.net.ssl.trustStore=/usr/local/sdkman/candidates/java/27.0.0.0+35-tem/lib/security/cacerts -Djavax.net.ssl.trustStorePassword=changeit' ./mvnw clean verify
```

Use it only if the JDK 25 truststore is missing the required CA and that JDK 27 truststore exists; these paths are not portable. The optional UI suite under Xvfb still had two popup-window lookup errors (`aiAssistantSuggestsRepliesAndRewritesDraftInBothThemes` and `groupMemberAdminActionsAndRemovalFlowInBothThemes` in `UserApplicationTest`), including at 1366x768; no workaround was verified. Recheck on the supported Temurin JDK/desktop before attributing these to a code change.

### PostgreSQL changes

Docker Engine **and Docker Compose v2** are required. For local setup, copy `.env.example` to `.env`, replace the placeholder password, then run in order:

```sh
docker compose config --quiet
docker compose up -d --wait db
docker compose ps
python3 script/test-db.py --flow all
```

Run a focused SQL flow while iterating: `python3 script/test-db.py --flow auth-friends`, `--flow messaging-e2ee-spam`, or `--flow logs-analytics`. The runner creates and cleans up an isolated temporary database; do not reset the development database or run `docker compose down -v`. `python3 script/test-native.py --flow all` is an alternative on Linux/macOS with PostgreSQL 18 installed. See `script/README.md` for native/local-server setup and safety details.

`.github/workflows/database.yml` is the only GitHub Actions workflow. It runs PostgreSQL 18 bootstrap checks, then `all` and each domain flow on fresh Docker Compose (Ubuntu) and native PostgreSQL (Windows); it is triggered by changes to `script/**`, `compose.yaml`, or itself, and can be dispatched manually. Reproduce the four flows above after SQL or Compose changes. SQL validation passed here: all 381 integrated checks and each independent flow, including concurrency probes.

## Running and behavior conventions

To run a desktop client, first install reactor artifacts (also repeat after changing `common` or `desktop-ui`), then launch the chosen app from separate terminals:

```sh
./mvnw install
./mvnw -pl src/user javafx:run
./mvnw -pl src/admin javafx:run
```

Do not pass `-am` to `javafx:run`. These apps currently use mock data and do not authenticate or persist to the server/database. GUI launch was not available in the headless validation session.

For behavior and wire-format changes, consult `docs/architecture/contracts/` and keep `src/common` DTOs, OpenAPI/JSON schemas, and fixtures consistent. For UI work, follow `docs/architecture/desktop-ui.md`: route ownership, FXML, controller, CSS, and tests are organized by feature; JavaFX navigation/theme calls belong on the FX thread. For SQL changes, update the owning DDL and test pair, relevant ERD/docs, and run the domain flow plus `all` before completion. Never put database, SMTP, or API credentials in client resources or commit local `.env`.

Trust these instructions and the linked repository guides; search the codebase only when the guidance is incomplete or contradicted by current files.
