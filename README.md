# MOMENTA — Personal Operating System (Phase 1–4)

This is the foundation build: project skeleton, MVC + DAO wiring, the full
SQLite schema for every module in the spec, and one complete vertical
slice — **Task Management**, wired all the way from FXML through to SQL and
back, with the **MOMENTA NOW** priority recommendation running on top of it.

## What's included (Phases 1–4)

| Layer | Files | Spec section |
|---|---|---|
| Entry point | `application/Main.java` | §31 |
| Database | `database/DatabaseConnection.java`, `database/DatabaseInitializer.java` | §18, §19 |
| Model | `model/Task.java` (JavaFX Properties) | §6 |
| DAO | `dao/TaskDAO.java`, `dao/impl/TaskDAOImpl.java` | §20 |
| Service | `service/TaskService.java` | §14 |
| Engine | `engine/PriorityEngine.java` — deterministic, explainable scoring | §15 |
| Threading | `threading/TaskExecutor.java` — bounded pool, clean shutdown | §21, §23 |
| Controllers | `controller/DashboardController.java`, `controller/TaskController.java` | §5, §6, §16 |
| Utility | `utility/SceneManager.java`, `utility/AlertUtil.java` | §24, §34 |
| Views | `view/Dashboard.fxml`, `view/Tasks.fxml` — **no CSS anywhere** | §2 |

The `momenta.db` SQLite file will be created automatically on first run,
with all 12 tables from §18 (users, tasks, goals, projects, events, habits,
habit_logs, expenses, income, focus_sessions, notifications, settings) —
even though only `tasks` is wired up to the UI yet, so later phases
(Goals, Projects, Habits, Finance...) don't need schema changes.

## How to build and run

You need **JDK 17+** and **Maven** installed.

```bash
cd MOMENTA
mvn clean javafx:run
```

Maven will download JavaFX 21 and the `sqlite-jdbc` driver automatically
the first time (both are declared in `pom.xml`).

If you're using **IntelliJ IDEA**: open the folder as a Maven project,
let it import, then run `Main.java` directly, or use the Maven `javafx:run`
goal from the Maven side panel.

## Demonstrating the "one action propagates" principle (§36)

1. Launch → Dashboard loads (background thread fetches incomplete tasks).
2. Click **Open Tasks** → **Add Task** → fill in title, deadline, importance.
3. Save → `TaskService.createTask()` runs the `PriorityEngine`, then
   `TaskDAOImpl` inserts a row into SQLite.
4. Go back to **Dashboard** → it reloads (cache invalidated) and **MOMENTA
   NOW** now recommends whichever task scored highest, with reasons shown.

That's the full chain: `UI → Controller → Service → Engine → DAO →
Database → UI`, with the JavaFX Application Thread never blocked (every
DB call above runs inside a `javafx.concurrent.Task` on
`TaskExecutor.workPool()`).

## No CSS, as required

No `application.css` file exists anywhere in this project, and no FXML
node uses an inline `style="-fx-..."` attribute or `setStyle(...)` call in
Java. Layout and visual structure come entirely from `BorderPane` /
`VBox` / `HBox` / `TitledPane` / `Separator` and `Font`, per §24 and §30.

## Roadmap — what's next

Following the phases in the spec (§37), the next builds are:

- **Phase 5**: Goals + Projects (hierarchical goals, project progress)
- **Phase 6**: Calendar + Events
- **Phase 7–8**: Habits, Finance
- **Phase 9**: Focus Mode (Timeline-based countdown, session logging)
- **Phase 10–11**: `MomentaCore` + full Recommendation Engine
- **Phase 12–14**: Deadline scheduler (`ScheduledExecutorService`),
  Analytics (charts), Notifications
- **Phase 15–18**: UI refinement, animations, Command Palette (Ctrl+K),
  integration testing

Ask for the next phase whenever you're ready — each one builds directly
on this skeleton without touching what already works, per §37's own rule
("preserve previously implemented functionality").
