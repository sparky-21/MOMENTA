# MOMENTA — Personal Operating System (Phase 1–5)

This is the foundation build: project skeleton, MVC + DAO wiring, the full
SQLite schema for every module in the spec, **Task Management** wired all
the way from FXML through to SQL and back with the **MOMENTA NOW** priority
recommendation on top, and now **Goals + Projects** (Phase 5).

## What's new in Phase 5

- **Goals** (`model/Goal.java`, `dao/GoalDAO(+Impl).java`, `service/GoalService.java`,
  `controller/GoalController.java`, `view/Goals.fxml`): hierarchical via
  `parentGoalId` (Life Goal → Year Goal → Monthly Goal...). Progress is
  **not** typed in by hand — `GoalService.recalculateProgress()` derives it
  from the completion % of tasks linked to that goal (§7).
- **Projects** (`model/Project.java`, `dao/ProjectDAO(+Impl).java`,
  `service/ProjectService.java`, `controller/ProjectController.java`,
  `view/Projects.fxml`): optionally linked to a Goal. Progress is computed
  **live** on every load from linked tasks — never stored, so it can't go
  stale (§8 — "calculated dynamically" taken literally: the `projects`
  table has no `progress` column at all).
- **Task ↔ Goal/Project linkage**: the task dialog (`TaskController`) now
  has Project and Goal dropdowns. Creating/completing/deleting a task
  invalidates the Dashboard, Goals, and Projects views so their numbers
  are never stale the next time you open them.
- **Dashboard**: two new cards (Active Goals, Active Projects), and Goals /
  Projects sidebar buttons are now enabled.

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
