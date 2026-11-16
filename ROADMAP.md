# Roadmap

Progress tracker for the Java Real-Time Chat System. The project overview and setup instructions are in [README.md](README.md).

**Last updated:** 2026-11-15

## Contents

- [How to use this file](#how-to-use-this-file)
- [Milestones](#milestones)
- [Version 1 deliverables](#version-1-deliverables)
- [Version 2 deliverables](#version-2-deliverables)
- [Feature tracker](#feature-tracker)
- [Beyond the requirements](#beyond-the-requirements)
- [Pre-submission checks](#pre-submission-checks)

---

## How to use this file

**Status legend**

| Icon | Meaning |
|:----:|---------|
| ⬜ | Planned, not started |
| 🚧 | In progress |
| ✅ | Done |
| 🔴 | Blocked (note the reason in the pull request or issue) |

**Definition of done**

- **UI (Version 1):** the screen exists in the code, matches the design and was reviewed by a teammate. Event handling is not required yet.
- **Logic (Version 2):** the feature works end to end against the real database, is merged into `main` and can be shown in the demo.

**Rules**

- Update the status in the same pull request that completes the work.
- Keep feature IDs stable. Use them in branch names and commit messages, for example `feat(U-12): push messages to online recipients`.

<p align="right">(<a href="#roadmap">back to top</a>)</p>

---

## Milestones

| Milestone | Weight | Scope | Status |
|-----------|:------:|-------|:------:|
| **Version 1** | 20% | UI screens of both applications, database creation script, report | 🚧 |
| **Version 2** | 80% | Complete working system, release package, final report, self-assessment | ⬜ |

<p align="right">(<a href="#roadmap">back to top</a>)</p>

---

## Version 1 deliverables

Interface and database. The archive contains `src/`, `script/`, `git/` and the report.

| Deliverable | Status |
|-------------|:------:|
| `src/`: screens of the User Client (UI only) | 🚧 |
| `src/`: screens of the Admin App (Desktop GUI, UI only) | 🚧 |
| `script/`: database creation script | ⬜ |
| `git/`: commit screenshots of every member | ⬜ |
| Report: team information | ⬜ |
| Report: task list of each member | ⬜ |
| Report: contribution table (total 100) | ⬜ |
| Report: database design | ⬜ |
| Report: list of designed screens, with screenshots | ⬜ |

<p align="right">(<a href="#roadmap">back to top</a>)</p>

---

## Version 2 deliverables

The complete system. The archive contains `src/`, `release/`, `script/`, `git/` and the report.

| Deliverable | Status |
|-------------|:------:|
| `src/`: runnable source of both applications | ⬜ |
| `release/`: at least two JAR files (admin and user) | ⬜ |
| `release/`: one configuration file holding the database connection settings | ⬜ |
| `script/`: database creation script (final) | ⬜ |
| `git/`: commit screenshots of every member | ⬜ |
| Report: team information, task list, contribution table (total 100) | ⬜ |
| Report: features and techniques the team considers outstanding or beyond the requirements | ⬜ |
| Report: system design | ⬜ |
| Report: database design | ⬜ |
| Report: user guide with screenshots | ⬜ |
| Report: YouTube demo link | ⬜ |
| Report: GitHub link | ⬜ |
| Self-assessment report (course template) | ⬜ |

<p align="right">(<a href="#roadmap">back to top</a>)</p>

---

## Feature tracker

**UI** is due in Version 1 and **Logic** in Version 2. Features are listed in the order of the course specification.

### Foundation

| ID | Task | Status |
|----|------|:------:|
| X-01 | Database design and creation script | 🚧 |
| X-02 | Chat server: connections, message routing, presence | ⬜ |
| X-03 | Configuration file loading (database, server, email, LLM) | ⬜ |
| X-04 | Build and packaging of the runnable JARs | 🚧 |
| X-05 | Release test: JARs start on a clean machine after editing only the configuration file | ⬜ |

X-01: PostgreSQL Compose and the [SQL collaboration scaffold](docs/databases/workflow.md) are ready. Shared users/conversation anchors and isolated per-domain runners support parallel development; business DDL and domain acceptance tests remain pending.

X-04: Maven scaffold/Wrapper (task 1.1) and desktop launchers/navigation/theme (task 1.2) are ready. The shared `desktop-ui` library supports both applications. See [README.md](README.md#chạy-user-client-và-admin-app) for launch commands and [UI architecture](docs/architecture/desktop-ui.md) for integration and verification. Business screens currently contain placeholders; runnable fat-JAR packaging remains pending. No individual UI feature is marked complete yet.

Task 1.3 is ready: shared immutable UI fixtures and a preview page with tables, message/evidence lists, and twelve-month charts in both applications. See [mock data APIs and verification](docs/architecture/ui-mock-data.md) and [screenshots in Git history](https://github.com/Ladenyx/ChatSystem/blob/47e3662b15611c6715c62e79295a83ef4a29d03d/docs/images/ui-mock-data/README.md). This supplies data for stages 3–4; individual business screens retain their current status.

### User Client

| ID | Feature | UI | Logic |
|----|---------|:--:|:-----:|
| **Authentication & profile** | | | |
| U-01 | Register an account | ⬜ | ⬜ |
| U-02 | Log in | ⬜ | ⬜ |
| U-03 | Update profile information | ⬜ | ⬜ |
| U-04 | Change password | ⬜ | ⬜ |
| U-05 | Reset password: random password sent by email | ⬜ | ⬜ |
| **Relationship management** | | | |
| U-06 | Friend list with name filter and online/offline status | ⬜ | ⬜ |
| U-07 | Search users by name or username and send a friend request (blockers hidden) | ⬜ | ⬜ |
| U-08 | Friend requests: list with name filter, accept or decline | ⬜ | ⬜ |
| U-09 | Unfriend | ⬜ | ⬜ |
| U-10 | Block a user (unfriends; no new requests allowed) | ⬜ | ⬜ |
| U-11 | Online friends list with name filter, chat and create group | ⬜ | ⬜ |
| **Messaging** | | | |
| U-12 | Real-time chat with an online friend | ⬜ | ⬜ |
| U-13 | Offline delivery: messages stored for a friend who is away | ⬜ | ⬜ |
| U-14 | Chat history | ⬜ | ⬜ |
| U-15 | Delete the whole chat history | ⬜ | ⬜ |
| U-16 | Delete one or several messages | ⬜ | ⬜ |
| U-17 | Search text in one conversation and jump to the match | ⬜ | ⬜ |
| U-18 | Search text across all conversations and jump to the match | ⬜ | ⬜ |
| **Group chat** | | | |
| U-19 | Create a group (at least one other username) | ⬜ | ⬜ |
| U-20 | Rename a group | ⬜ | ⬜ |
| U-21 | Add members | ⬜ | ⬜ |
| U-22 | Grant group-admin rights | ⬜ | ⬜ |
| U-23 | Remove members (group admins only) | ⬜ | ⬜ |
| U-24 | Group chat | ⬜ | ⬜ |
| **Security, moderation and AI** | | | |
| U-25 | Encrypted group chat | ⬜ | ⬜ |
| U-26 | Report a user for spam | ⬜ | ⬜ |
| U-27 | LLM assistant: refine a draft and suggest a reply | ⬜ | ⬜ |

### Admin App (Desktop GUI)

| ID | Feature | UI | Logic |
|----|---------|:--:|:-----:|
| **User oversight** | | | |
| A-01 | User list: filter by name, username, status; sort by name, creation date | ⬜ | ⬜ |
| A-02 | Create, update and delete users | ⬜ | ⬜ |
| A-03 | Lock and unlock accounts | ⬜ | ⬜ |
| A-04 | Reset a user's password | ⬜ | ⬜ |
| A-05 | View a user's login history | ⬜ | ⬜ |
| A-06 | View a user's friend list | ⬜ | ⬜ |
| **Access logs and groups** | | | |
| A-07 | Login log in chronological order (time, username, full name) | ⬜ | ⬜ |
| A-08 | Group list: sort by name, creation time; filter by name | ⬜ | ⬜ |
| A-09 | Members and admins of a group | ⬜ | ⬜ |
| **Spam resolution** | | | |
| A-10 | Spam reports: sort and filter by time, username | ⬜ | ⬜ |
| A-11 | Lock a user's account from a spam report | ⬜ | ⬜ |
| **Analytics and data visualization** | | | |
| A-12 | New registrations in a date range: sort by name, creation time; filter by name | ⬜ | ⬜ |
| A-13 | Chart: new registrations per month for a selected year | ⬜ | ⬜ |
| A-14 | Friend counts (direct and friends-of-friends): sort, name filter, direct-count filter (`=`, `<`, `>`) | ⬜ | ⬜ |
| A-15 | Active users in a date range (app opens, people chatted with, groups chatted in): sort, name filter, activity filter (`=`, `<`, `>`) | ⬜ | ⬜ |
| A-16 | Chart: active users per month for a selected year | ⬜ | ⬜ |

<p align="right">(<a href="#roadmap">back to top</a>)</p>

---

## Beyond the requirements

Extra features or techniques the team wants to highlight in the report's self-evaluation.

| ID | Idea | Status |
|----|------|:------:|
| E-01 | TBD | ⬜ |

<p align="right">(<a href="#roadmap">back to top</a>)</p>

---

## Pre-submission checks

Review before each submission. The course rules give a zero for a member without commits, a wrong archive structure, missing report content and a missing self-assessment report.

| Check | Version 1 | Version 2 |
|-------|:---------:|:---------:|
| Every member has commits in the repository, with screenshots in `git/` | ⬜ | ⬜ |
| The archive structure matches the specification exactly | ⬜ | ⬜ |
| The report contains every required section | ⬜ | ⬜ |
| The self-assessment report is completed on the course template | n/a | ⬜ |
| The database script runs from scratch on an empty database | ⬜ | ⬜ |
| The JARs start after editing only the configuration file | n/a | ⬜ |
| Contribution percentages add up to 100 | ⬜ | ⬜ |
| The demo video is public and its link is in the report | n/a | ⬜ |
| No passwords, API keys or tokens are committed | ⬜ | ⬜ |

<p align="right">(<a href="#roadmap">back to top</a>)</p>
