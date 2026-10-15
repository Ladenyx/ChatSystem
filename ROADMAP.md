# Roadmap

Progress tracker for the Java Real-Time Chat System. The project overview and setup instructions are in [README.md](README.md).

**Last updated:** TBD

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
- Write the owner's GitHub handle in the **Owner** column.
- Keep feature IDs stable. Use them in branch names and commit messages, for example `feat(U-12): push messages to online recipients`.

<p align="right">(<a href="#roadmap">back to top</a>)</p>

---

## Milestones

| Milestone | Weight | Scope | Status |
|-----------|:------:|-------|:------:|
| **Version 1** | 20% | UI screens of both applications, database creation script, report | ⬜ |
| **Version 2** | 80% | Complete working system, release package, final report, self-assessment | ⬜ |

<p align="right">(<a href="#roadmap">back to top</a>)</p>

---

## Version 1 deliverables

Interface and database. The archive contains `src/`, `script/`, `git/` and the report.

| Deliverable | Status | Owner |
|-------------|:------:|-------|
| `src/`: screens of the User Client (UI only) | ⬜ | TBD |
| `src/`: screens of the Admin Console (UI only) | ⬜ | TBD |
| `script/`: database creation script | ⬜ | TBD |
| `git/`: commit screenshots of every member | ⬜ | TBD |
| Report: team information | ⬜ | TBD |
| Report: task list of each member | ⬜ | TBD |
| Report: contribution table (total 100) | ⬜ | TBD |
| Report: database design | ⬜ | TBD |
| Report: list of designed screens, with screenshots | ⬜ | TBD |

<p align="right">(<a href="#roadmap">back to top</a>)</p>

---

## Version 2 deliverables

The complete system. The archive contains `src/`, `release/`, `script/`, `git/` and the report.

| Deliverable | Status | Owner |
|-------------|:------:|-------|
| `src/`: runnable source of both applications | ⬜ | TBD |
| `release/`: at least two JAR files (admin and user) | ⬜ | TBD |
| `release/`: one configuration file holding the database connection settings | ⬜ | TBD |
| `script/`: database creation script (final) | ⬜ | TBD |
| `git/`: commit screenshots of every member | ⬜ | TBD |
| Report: team information, task list, contribution table (total 100) | ⬜ | TBD |
| Report: features and techniques the team considers outstanding or beyond the requirements | ⬜ | TBD |
| Report: system design | ⬜ | TBD |
| Report: database design | ⬜ | TBD |
| Report: user guide with screenshots | ⬜ | TBD |
| Report: YouTube demo link | ⬜ | TBD |
| Report: GitHub link | ⬜ | TBD |
| Self-assessment report (course template) | ⬜ | TBD |

<p align="right">(<a href="#roadmap">back to top</a>)</p>

---

## Feature tracker

**UI** is due in Version 1 and **Logic** in Version 2. Features are listed in the order of the course specification.

### Foundation

| ID | Task | Status | Owner |
|----|------|:------:|-------|
| X-01 | Database design and creation script | ⬜ | TBD |
| X-02 | Chat server: connections, message routing, presence | ⬜ | TBD |
| X-03 | Configuration file loading (database, server, email, LLM) | ⬜ | TBD |
| X-04 | Build and packaging of the runnable JARs | ⬜ | TBD |
| X-05 | Release test: JARs start on a clean machine after editing only the configuration file | ⬜ | TBD |

### User Client

| ID | Feature | UI | Logic | Owner |
|----|---------|:--:|:-----:|-------|
| **Authentication & profile** | | | | |
| U-01 | Register an account | ⬜ | ⬜ | TBD |
| U-02 | Log in | ⬜ | ⬜ | TBD |
| U-03 | Update profile information | ⬜ | ⬜ | TBD |
| U-04 | Change password | ⬜ | ⬜ | TBD |
| U-05 | Reset password: random password sent by email | ⬜ | ⬜ | TBD |
| **Relationship management** | | | | |
| U-06 | Friend list with name filter and online/offline status | ⬜ | ⬜ | TBD |
| U-07 | Search users by name or username and send a friend request (blockers hidden) | ⬜ | ⬜ | TBD |
| U-08 | Friend requests: list with name filter, accept or decline | ⬜ | ⬜ | TBD |
| U-09 | Unfriend | ⬜ | ⬜ | TBD |
| U-10 | Block a user (unfriends; no new requests allowed) | ⬜ | ⬜ | TBD |
| U-11 | Online friends list with name filter, chat and create group | ⬜ | ⬜ | TBD |
| **Messaging** | | | | |
| U-12 | Real-time chat with an online friend | ⬜ | ⬜ | TBD |
| U-13 | Offline delivery: messages stored for a friend who is away | ⬜ | ⬜ | TBD |
| U-14 | Chat history | ⬜ | ⬜ | TBD |
| U-15 | Delete the whole chat history | ⬜ | ⬜ | TBD |
| U-16 | Delete one or several messages | ⬜ | ⬜ | TBD |
| U-17 | Search text in one conversation and jump to the match | ⬜ | ⬜ | TBD |
| U-18 | Search text across all conversations and jump to the match | ⬜ | ⬜ | TBD |
| **Group chat** | | | | |
| U-19 | Create a group (at least one other username) | ⬜ | ⬜ | TBD |
| U-20 | Rename a group | ⬜ | ⬜ | TBD |
| U-21 | Add members | ⬜ | ⬜ | TBD |
| U-22 | Grant group-admin rights | ⬜ | ⬜ | TBD |
| U-23 | Remove members (group admins only) | ⬜ | ⬜ | TBD |
| U-24 | Group chat | ⬜ | ⬜ | TBD |
| **Security, moderation and AI** | | | | |
| U-25 | Encrypted group chat | ⬜ | ⬜ | TBD |
| U-26 | Report a user for spam | ⬜ | ⬜ | TBD |
| U-27 | LLM assistant: refine a draft or suggest a reply | ⬜ | ⬜ | TBD |

### Admin Console

| ID | Feature | UI | Logic | Owner |
|----|---------|:--:|:-----:|-------|
| **User oversight** | | | | |
| A-01 | User list: filter by name, username, status; sort by name, creation date | ⬜ | ⬜ | TBD |
| A-02 | Create, update and delete users | ⬜ | ⬜ | TBD |
| A-03 | Lock and unlock accounts | ⬜ | ⬜ | TBD |
| A-04 | Reset a user's password | ⬜ | ⬜ | TBD |
| A-05 | View a user's login history | ⬜ | ⬜ | TBD |
| A-06 | View a user's friend list | ⬜ | ⬜ | TBD |
| **Access logs and groups** | | | | |
| A-07 | Login log in chronological order (time, username, full name) | ⬜ | ⬜ | TBD |
| A-08 | Group list: sort by name, creation time; filter by name | ⬜ | ⬜ | TBD |
| A-09 | Members and admins of a group | ⬜ | ⬜ | TBD |
| **Spam resolution** | | | | |
| A-10 | Spam reports: sort and filter by time, username | ⬜ | ⬜ | TBD |
| A-11 | Lock a user's account from a spam report | ⬜ | ⬜ | TBD |
| **Analytics and data visualization** | | | | |
| A-12 | New registrations in a date range: sort by name, creation time; filter by name | ⬜ | ⬜ | TBD |
| A-13 | Chart: new registrations per month for a selected year | ⬜ | ⬜ | TBD |
| A-14 | Friend counts (direct and friends-of-friends): sort, name filter, direct-count filter (`=`, `<`, `>`) | ⬜ | ⬜ | TBD |
| A-15 | Active users in a date range (app opens, people chatted with, groups chatted in): sort, name filter, activity filter (`=`, `<`, `>`) | ⬜ | ⬜ | TBD |
| A-16 | Chart: active users per month for a selected year | ⬜ | ⬜ | TBD |

<p align="right">(<a href="#roadmap">back to top</a>)</p>

---

## Beyond the requirements

Extra features or techniques the team wants to highlight in the report's self-evaluation.

| ID | Idea | Status | Owner |
|----|------|:------:|-------|
| E-01 | TBD | ⬜ | TBD |

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
