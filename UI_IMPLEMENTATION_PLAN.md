# UI Implementation Plan – Version 1

Final project **Chat System** (Java Application Programming). This document covers only the **user interface** of Version 1 (20% of the grade): source code for the screens of both applications, with no event handling yet. Visual rules (colors, font, shapes, layout) are in [DESIGN_SYSTEM.md](DESIGN_SYSTEM.md).

> Decisions are recorded in section 11. Schedule: **Friday 16 Oct to Sunday 18 Oct 2026**.

## 1. Goal and scope

**Goal:** build all screens of the User Client and the Admin Console in JavaFX with mock data, then screenshot every screen for the report.

**In scope**
- All screens and dialogs required by the specification (section 6).
- All controls required by the specification: filter boxes, sort combo boxes, date range pickers, operator combo box (`=` / `<` / `>`), year picker, charts.
- Mock data so that screens look realistic in screenshots.

**Out of scope (Version 2)**
- Database connection, server, sockets, email sending, LLM calls, encryption.
- Real event handling. Buttons and controls only need to be present; simple navigation between screens is allowed if it is cheap.

## 2. Technology

| Item | Choice |
|---|---|
| JDK | JDK 21 (LTS); the README requires 17 or later |
| UI | JavaFX 21.0.x (LTS, runs on JDK 17 or later) |
| Build | Maven |
| Theme and components | AtlantaFX (PrimerLight) |
| Icons | Ikonli (FontAwesome / Material) or self-sourced PNG/SVG |
| Layout | **Hybrid:** hand-written FXML for static structure, Java for dynamic parts (see below); Scene Builder only as an optional preview |
| Charts | JavaFX built-in `BarChart` / `LineChart` |

**How to split FXML and Java**
- **FXML + CSS:** the skeleton of each screen (BorderPane, filter bar, TableView, buttons, forms). It is short, readable and separate from logic.
- **Java:** data-driven parts such as TableView columns, message bubbles (`MessageBubble`), charts, and shared components (section 5).
- Each screen has one `.fxml` file and one controller. Shared components are written in Java so screens can reuse them.

Main `pom.xml` dependencies (check for the latest versions when configuring):
- `org.openjfx:javafx-controls`, `org.openjfx:javafx-fxml`
- `io.github.mkpaz:atlantafx-base`
- `org.kordamp.ikonli:ikonli-javafx` + the icon pack in use
- Plugin: `javafx-maven-plugin` (run with `mvn javafx:run`)

## 3. Project structure

Matches the submission requirements (`src/`, `script/`, `git/`) and the README. Each application is a Maven module inside `src/`.

```text
.
├── pom.xml                         # parent POM, declares the modules
├── src/
│   ├── common/                     # models, service interfaces, shared UI components
│   │   └── src/main/java/common/
│   │       ├── model/              # User, Message, ChatGroup, FriendRequest, LoginRecord, SpamReport...
│   │       ├── service/            # interfaces: UserService, ChatService, AdminService...
│   │       └── ui/                 # FilterTable, ConfirmDialog, DateRangePicker, OperatorFilter...
│   ├── user/                       # User Client
│   │   └── src/main/
│   │       ├── java/user/ui/       # controllers + mock services
│   │       └── resources/user/     # fxml/, css/, images/
│   ├── admin/                      # Admin Console
│   │   └── src/main/
│   │       ├── java/admin/ui/
│   │       └── resources/admin/    # fxml/, css/, images/
│   └── server/                     # (Version 2, teammates)
├── script/                         # database creation script (teammates)
├── git/                            # commit screenshots of every member
└── report/
```

Naming convention: FXML `UserListView.fxml` pairs with controller `UserListController.java`. One pair of files per screen avoids merge conflicts.

## 4. Design references

Full visual rules, tokens and the starter `theme.css` are in [DESIGN_SYSTEM.md](DESIGN_SYSTEM.md). Summary:

| Used for | Source |
|---|---|
| Components (buttons, tables, combo boxes, date pickers) | AtlantaFX (PrimerLight) + AtlantaFX Sampler |
| Design system and chat layout (User Client) | Dribbble shot "Web Chat UI" |
| Admin layout | Library Management System |

At a glance: light theme with a dark navigation rail, accent `#7678ed`, warning `#ff7a55`, Poppins font, 8px spacing grid, UI labels in English. Window sizes are fixed during screenshots (User 1100×700, Admin 1280×760, login / register 420×560).

References are for layout and organization only. The team writes its own code.

## 5. Shared components (build first)

Located in `src/common/.../ui/` and reused by most screens:

1. **FilterTable:** the "filter bar on top + `TableView` + action button bar at the bottom" template.
2. **DateRangePicker:** two `DatePicker`s (from date, to date).
3. **OperatorFilter:** combo box `=` / `<` / `>` plus a number field (used by A-14 and A-15).
4. **YearPicker:** combo box for choosing a year (used by both charts).
5. **ConfirmDialog / FormDialog:** confirmation and form dialogs.
6. **StatusBadge:** online / offline and active / locked labels.
7. **MessageBubble:** sent and received message bubbles for the chat window.
8. **NavSidebar:** the left navigation rail shared by the User Client and the Admin Console.

## 6. Screen list

IDs match `ROADMAP.md`. Screenshot file name: `<ID>-<screen-name>.png`.

UI labels are in **English**. **Dialogs count as separate screens** and are listed in the report's screen list (the specification asks for a list of designed screens with screenshots, so listing them fully is the safest choice).

### 6.1 User Client

| # | Screen | Roadmap ID | Main elements |
|---|---|---|---|
| 1 | Login | U-02 | username, password, login button, links to register / forgot password |
| 2 | Register | U-01 | username, full name, address, date of birth, gender, email, password |
| 3 | Forgot password | U-05 | email / username field, "send new password" button |
| 4 | Main window | – | rail: Chats, Friends, Online, Requests, Find people, Groups; bottom: Profile, Log out |
| 5 | Friend list | U-06, U-09, U-10 | name filter, online / offline status, chat, unfriend and block buttons |
| 6 | Friend requests | U-08 | name filter, accept / decline buttons |
| 7 | Online friends | U-11 | name filter, chat and create-group buttons |
| 8 | Find people | U-07 | search by name / username, add friend, chat, create group |
| 9 | Chat window | U-12 to U-16, U-26 | conversation list, message bubbles, input + send, delete one or several messages, delete whole history, report spam, search in conversation |
| 10 | Search all chat history | U-18 | search box, result list, jump to message |
| 11 | AI assistant (panel) | U-27 | right-hand panel of the chat window (toggle button), Refine draft / Suggest reply options, result area, "Use" button |
| 12 | Account info | U-03 | profile update form |
| 13 | Change password | U-04 | old password, new password, confirmation |
| 14 | Create group (dialog) | U-19 | group name, pick at least one member |
| 15 | Manage group (dialog / panel) | U-20 to U-25 | rename, add / remove members, grant admin, encryption toggle |

Notes: U-17 (search inside one conversation) lives inside screen #9. U-24 (group chat) reuses the chat window of screen #9.

### 6.2 Admin Console

| # | Screen | Roadmap ID | Main elements |
|---|---|---|---|
| 1 | Admin login | – | username, password |
| 2 | Main window | – | sidebar linking to the screens below |
| 3 | User management | A-01 to A-04 | filter by name / username / status, sort by name / creation date, add / edit / delete / lock / unlock / reset password buttons |
| 4 | Add / edit user (dialog) | A-02 | username, full name, address, date of birth, gender, email |
| 5 | User login history (dialog) | A-05 | table of login times |
| 6 | User friend list (dialog) | A-06 | friends table |
| 7 | System login history | A-07 | table: time, username, full name (chronological) |
| 8 | Chat group list | A-08 | sort by name / creation time, filter by name |
| 9 | Group members / admins (dialog) | A-09 | two tabs or two tables: members, admins |
| 10 | Spam reports | A-10, A-11 | sort by time / username, filter by time and username, lock-account button |
| 11 | New registrations | A-12 | date range, sort by name / creation time, name filter |
| 12 | New registrations chart | A-13 | year picker, x axis = month, y axis = number of new registrations |
| 13 | Users and friend counts | A-14 | direct-friends column + friends-of-friends column, sort, name filter, `=` / `<` / `>` filter |
| 14 | Active users | A-15 | date range, columns: app opens, people chatted with, groups chatted in, `=` / `<` / `>` filter |
| 15 | Active users chart | A-16 | year picker, x axis = month, y axis = number of users who opened the app |

## 7. Three-day schedule (one person on UI)

Starts Friday 16 Oct and ends Sunday 18 Oct 2026. Keep Sunday evening free for screenshots, packaging and updating the ROADMAP.

### Day 1 (Friday 16 Oct): foundation and simple screens
- [x] Create the parent POM and three modules (`common`, `user`, `admin`).
- [x] Verify the Maven JavaFX launch configuration: built all modules and opened both User and Admin with temporary Maven 3.10.0 and the JDK 27 copy under `src/user/src/main/java/user/ui/oracleJdk-27`. Test suites were skipped. `-Dmaven.compiler.fork=true` was needed because JDK 27's in-process compiler hit `AccessDeniedException` on a JavaFX JAR. JavaFX starts with native-access and `Unsafe` deprecation warnings under JDK 27.
- [x] Add AtlantaFX dependencies and create `theme.css` from [DESIGN_SYSTEM.md](DESIGN_SYSTEM.md).
- [x] Visually verify Poppins with Vietnamese names containing diacritics (confirmed by the user).
- [x] Build the shared components of section 5: FilterTable, NavSidebar, DateRangePicker, OperatorFilter, AvatarLabel, StatusBadge and ConfirmDialog.
- [x] Create the seven core mock models in `common/model` and sample data for users, conversations and groups in `common/mock/MockData.java`.
- [x] User: #1 Login, #2 Register, #3 Forgot password and #4 Main window UI entry points.
- [x] Admin: #1 Admin login, #2 Main window UI with sidebar navigation and overview table.
- [ ] Commit and push at the end of the day.

### Day 2 (Saturday 17 Oct): User Client
- [ ] #5 Friend list, #6 Friend requests, #7 Online friends, #8 Find people (same list template).
- [ ] #9 Chat window (the heaviest): conversation list, bubbles, input bar, toolbar (delete, report spam, search, AI assistant).
- [ ] #10 Search all chat history, #11 AI assistant panel.
- [ ] #12 Account info, #13 Change password, #14–#15 group dialogs.
- [ ] Screenshot the User screens.

### Day 3 (Sunday 18 Oct): Admin Console and wrap-up
- [ ] #3–#4 User management + dialog; #5–#6 login history and friend list dialogs.
- [ ] #7 Login history, #8–#9 Chat groups, #10 Spam reports.
- [ ] #11 New registrations, #13 Friend counts, #14 Active users (reuse FilterTable).
- [ ] #12 and #15 the two charts.
- [ ] Afternoon: screenshot the Admin screens, cross-review with a teammate, fix display issues, update `ROADMAP.md`.

**If behind schedule:** cut polish (effects, icons, animation), not screens. The report requires the full screen list.

## 8. Mock data and separating UI from logic

- Shared models in `common/model`: `User`, `Message`, `ChatGroup`, `FriendRequest`, `LoginRecord`, `SpamReport`.
- Service interfaces in `common/service`: `UserService`, `FriendService`, `ChatService`, `GroupService`, `AdminService`.
- In this phase each UI module has **Mock\*Service** classes returning hard-coded data. Controllers call only the interfaces.
- Version 2: replace the mocks with real implementations (server / database); the UI needs almost no changes.
- Sample data should include Vietnamese names with diacritics, long names, a few locked accounts and a few offline users to check how the UI handles them.

## 9. Coordination with teammates

The UI is independent of the rest, so teammates **do not need to wait**:

| Person | Parallel work in Version 1 |
|---|---|
| You (UI) | `src/user`, `src/admin`, screenshots |
| Teammate 1 | Database design + `script/create_database.sql`, "database design" section of the report |
| Teammate 2 | Version 1 report (team information, task list, contribution table), preparing server / DAO for Version 2 |

**Git**
- Branches: `feature/ui-user`, `feature/ui-admin`, `feature/ui-common`; commits follow Conventional Commits, for example `feat(U-06): friend list screen`.
- `common/` is shared: agree on model names first, only add new fields, avoid renames so nobody's code breaks.
- Every member needs real commits (the specification gives 0 points to a member without commits); put commit screenshots in `git/`.
- The contribution table must reflect the actual effort.

## 10. Definition of done and acceptance checks

A screen is done when:
- [ ] It exists in the code and follows the agreed design system.
- [ ] It has every control required by the specification (check against section 6).
- [ ] Names with diacritics in the sample data render correctly and text is not clipped when the window is resized.
- [ ] It has sample data and a screenshot named `<ID>-<name>.png`.
- [ ] A teammate reviewed it and its UI status in `ROADMAP.md` is set to ✅ in the same pull request.

Before submitting Version 1 (check against "Pre-submission checks" in the ROADMAP):
- [ ] The archive structure is correct: `src/`, `script/`, `git/`, report.
- [ ] The report is complete: team information, task list, contribution table (total 100), database design, list of screens with screenshots.
- [ ] Every member has commits.
- [ ] No passwords or API keys are committed.
- [ ] The project builds and runs from scratch on a clean machine (`mvn javafx:run` for each module).

## 11. Decisions

| # | Topic | Decision |
|---|---|---|
| 1 | Schedule | Friday 16 Oct to Sunday 18 Oct 2026 |
| 2 | Layout approach | Hand-written FXML for static structure, Java for dynamic parts (section 2) |
| 3 | UI label language | English |
| 4 | Theme | Light (AtlantaFX PrimerLight) with a dark rail |
| 5 | JDK / JavaFX | JDK 21 + JavaFX 21.0.x; teammates on JDK 17 can still run it |
| 6 | Admin and User | Two separate Maven modules, each building one JAR (matches the Version 2 requirement) |
| 7 | Server | Unclear; tentatively a separate `src/server` module, which does not affect Version 1 UI |
| 8 | Dialogs | Counted as separate screens in the report |
| 9 | AI assistant | Right-hand panel of the chat window |
| 10 | Screenshot format | No requirement; keep window sizes fixed, save as PNG |
| 11 | Design references | Dribbble "Web Chat UI" (design system and chat layout), AtlantaFX (components), Library Management System (admin layout); replaces TomChatApp |

**Still open:** whether Poppins renders Vietnamese correctly (check on day 1), the shades estimated from the template images (verify while building), and where teammates want the server placed (only affects the folder structure).
