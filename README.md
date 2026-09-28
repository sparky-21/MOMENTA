# MOMENTA

**MOMENTA** is a JavaFX desktop productivity suite backed by SQLite. It brings tasks, goals, projects, a calendar, habits, finance tracking, focus mode and analytics into one app, with user login, background multithreading and a live web API.

> Built as an Object-Oriented Programming / JavaFX assignment project.

---

## Tech Stack

| Area | Technology |
|---|---|
| Language | Java 17 |
| UI | JavaFX (FXML + controllers) |
| Database | SQLite via `sqlite-jdbc` |
| JSON | Jackson (`jackson-databind`) |
| Networking | Java `HttpClient` |
| Build | Maven (`javafx-maven-plugin`) |
| Version control | Git + GitHub |

---

## Features

| Module | Description |
|---|---|
| **Login / Register** | Account creation with username and password (`PasswordField`), hashed passwords, unique usernames |
| **Home / Dashboard** | Daily overview, progress bars and a daily quote fetched from the internet |
| **Tasks** | Full CRUD with importance (1-5), deadline, estimated time, progress and category; a priority engine computes a priority score |
| **Goals** | Goals with progress and importance, including sub-goals through a parent goal |
| **Projects** | Projects linked to goals and containing tasks |
| **Calendar** | Google Calendar-style monthly grid; events and task deadlines shown on their dates; double-click a date to add an event |
| **Habits** | Daily habit logging with current and longest streaks |
| **Finance** | Income and expense tracking with categories and charts |
| **Focus Mode** | Timed focus sessions linked to tasks, with session history |
| **Analytics** | Pie, bar and line charts of productivity data |
| **Gamification** | XP and achievements derived from completed tasks, focus sessions, goals and habits |
| **Recommendation Engine** | Suggests what to work on next |
| **Notifications** | A background scheduler checks deadlines periodically |
| **Settings** | Default focus duration and currency |
| **UI polish** | Animations, command palette and a custom theme |

---

## Assignment Requirements Coverage

| # | Requirement | How MOMENTA implements it | Where to look |
|---|---|---|---|
| 1 | **Version control** | Regular commits organised by development phase; `main` plus feature branches for phase 5 (goals/projects), phase 6 (calendar) and phases 9-10 (focus mode) | GitHub commit history |
| 2 | **Advanced OOP** | Interfaces (every `*DAO` with a matching `*DAOImpl`), an abstract class (`AbstractScreenController`), inheritance, generics, singletons (`TaskExecutor`, `SceneManager`), encapsulated models | `dao/`, `controller/AbstractScreenController.java` |
| 3 | **JavaFX UI design** | `BorderPane`, `StackPane`, `VBox`, `HBox`, `GridPane`, `FlowPane`, `ScrollPane`, `AnchorPane`; controls such as `PasswordField`, `TextField`, `ComboBox`, `DatePicker`, `TableView`, `Slider`, `Spinner`, `ProgressBar`, `CheckBox`; Pie, Bar and Line charts | `resources/com/momenta/view/*.fxml` |
| 4 | **Layout responsiveness** | `percentWidth` / `percentHeight` with `hgrow` / `vgrow` in the calendar grid; property bindings to scene width and height | `Calendar.fxml`, `SceneManager.java` |
| 5 | **Concurrency** | Fixed thread pool (4 workers) and a scheduled executor; JavaFX `Task` with `Platform.runLater` so the UI never blocks | `threading/TaskExecutor.java` |
| 6 | **SQLite database** | 12 tables, foreign keys enabled, `ON DELETE CASCADE` / `SET NULL` relationships, `CHECK` constraints, unique index | `database/DatabaseInitializer.java` |
| 7 | **CRUD** | Create, read, update and delete using prepared statements, demonstrated on Tasks | `TaskDAOImpl.java`, `TaskController.java` |
| 8 | **HTTP + JSON** | GET request to the DummyJSON quotes API, parsed into a `Quote` object with Jackson | `network/NetworkService.java`, `network/JsonParser.java` |

---

## Architecture

```
FXML View -> Controller -> Service -> DAO (interface) -> DAOImpl -> SQLite
                  |
                  v
     TaskExecutor (background pool) -> Platform.runLater -> UI
```

Each layer has a single responsibility: controllers handle the UI, services hold the logic, and DAOs handle the SQL.

### Project structure

```
src/main/java/com/momenta/
├── application/   Main, Launcher
├── controller/    Screen controllers (extend AbstractScreenController)
├── service/       Business logic
├── dao/           DAO interfaces
│   └── impl/      SQLite implementations
├── model/         Entity classes
├── database/      Connection + schema initialisation
├── engine/        Priority, recommendation and core engines
├── network/       HTTP client, JSON parser, Quote model
├── threading/     TaskExecutor (thread pools)
└── utility/       Theme, animations, scene manager, alerts, password utils
```

---

## Database Design

```
users ──< goals ──< projects ──< tasks ──< focus_sessions
  │          └── goals (parent_goal_id, self-reference)
  ├──< events
  ├──< habits ──< habit_logs
  ├──< expenses
  ├──< income
  ├──< notifications
  └──── settings (1:1)
```

- Every table references `users` with `ON DELETE CASCADE`.
- `goals`, `projects` and `tasks` are linked with `ON DELETE SET NULL`.
- `habit_logs` has a unique constraint on `(habit_id, log_date)`.
- `CHECK` constraints keep values valid (e.g. progress 0-100, importance 1-5).

---

## Concurrency Model

- `TaskExecutor` is a singleton holding a **fixed thread pool of 4 workers** and a **single-thread scheduled executor**.
- Database and network calls run inside JavaFX `Task` objects on the pool.
- Results return to the JavaFX Application Thread through `onSucceeded` or `Platform.runLater`.
- `AbstractScreenController.runInBackground(...)` gives every screen the same safe pattern.

---

## Networking

The dashboard fetches a daily quote from `https://dummyjson.com/quotes/random`:

1. `NetworkService` sends an HTTP GET with `HttpClient`, timeouts and a status-code check.
2. `JsonParser` maps the JSON response to a `Quote` (id, quote, author) using Jackson.
3. `InsightService` combines both, and the call runs in a background `Task`.

An internet connection is needed for the quote to load.

---

## Getting Started

**Prerequisites:** JDK 17+ and Maven.

```bash
git clone https://github.com/sparky-21/MOMENTA.git
cd MOMENTA/momenta
mvn clean javafx:run
```

The SQLite database (`momenta.db`) and all tables are created automatically on first launch. Register a new account on the login screen to begin.

---

## Development History

Built phase by phase:

| Phase | Focus |
|---|---|
| 1-4 | Project skeleton, full SQLite schema, task module |
| 5 | Goals and projects |
| 6 | Calendar and events |
| 7-8 | Habit and finance trackers |
| 9-10 | Focus mode and MOMENTA core |
| 11 | Recommendation engine |
| 12-13 | User login and passwords |
| 14-15 | Analytics, multithreading and executor service |
| 18-19 | Responsive layout and visual polish |
| 20-24 | Settings, animations, command palette, gamification |

---

## Future Improvements

- Salted password hashing (BCrypt or PBKDF2) in place of plain SHA-256
- Cloud sync and export (CSV / PDF)
- Unit tests for services and DAOs
- Additional web APIs for richer insights

---

## Author

**sparky-21** - [github.com/sparky-21](https://github.com/sparky-21)
