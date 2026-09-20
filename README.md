# MOMENTA — Phase 1 + 2 Foundation

**One place to run your life.**

This is the first working slice of MOMENTA: project setup, full database
schema, and a complete Tasks module (create / list / complete), built the
way the whole app will be built — MVC + DAO + background threading, FXML
laid out for Scene Builder, **no external CSS file**.

## What's actually working right now

- Launching the app opens the MOMENTA dashboard shell (sidebar + Tasks view).
- Adding a task writes it to a local SQLite file (`momenta.db`, created next
  to wherever you run the app from) via `PreparedStatement` — never raw
  string-built SQL.
- The task list loads from the database on a **background thread**
  (`AppExecutor` + `javafx.concurrent.Task`), so the UI never freezes even
  while the query runs — you'd only notice this on a slow disk or a much
  bigger dataset, but the pattern is in place and is what every later
  module (Goals, Habits, Finance, Focus, Analytics) will reuse.
- Marking a task "Done" updates SQLite and refreshes the table.
- The full schema for every future module (goals, projects, events, habits,
  habit_logs, income, expenses, focus_sessions, notifications, settings) is
  already created on first launch, with foreign keys wired between them —
  so later phases just add DAOs/Services/Controllers against tables that
  already exist, instead of us doing schema migrations mid-project.

## How to open and run it

1. **Scene Builder**: open `src/main/resources/com/momenta/view/dashboard.fxml`
   directly in Scene Builder — it's a plain FXML file with a
   `fx:controller` pointing at `DashboardController`. Any control you drag
   in becomes visible in the controller once you give it a matching
   `fx:id` and add the field in Java.
2. **Running**: this is a standard Maven project.
   - IntelliJ IDEA: open the folder, let Maven import, run `Main.java`
     (or `mvn javafx:run` in the terminal).
   - Command line: `mvn clean javafx:run` (requires Java 17+ and Maven
     installed locally).
   - The SQLite driver and JavaFX itself are pulled automatically from
     Maven Central via `pom.xml` — no manual JavaFX SDK download needed.

## Why no CSS file

Every visual property (`-fx-background-color`, `-fx-font-weight`,
`-fx-background-radius`, ...) is set as an inline `style="..."` attribute
directly on each FXML element. This is exactly what Scene Builder writes
for you automatically when you set colors/fonts from its **Inspector →
Properties → Style** field — so you can keep editing this file visually in
Scene Builder without ever creating a `.css` resource.

## Folder structure

```
momenta/
├── pom.xml
└── src/main/
    ├── java/com/momenta/
    │   ├── app/Main.java              — entry point, wires DB + FXML + Stage
    │   ├── model/Task.java            — JavaFX-property-based model
    │   ├── database/DatabaseManager.java  — singleton connection + full schema
    │   ├── dao/TaskDAO.java           — persistence contract
    │   ├── dao/impl/TaskDAOImpl.java  — JDBC/PreparedStatement implementation
    │   ├── service/TaskService.java   — background-threaded business layer
    │   ├── controller/DashboardController.java — thin FXML controller
    │   └── threading/AppExecutor.java — shared thread pool + scheduler
    └── resources/com/momenta/view/
        └── dashboard.fxml             — open this in Scene Builder
```

## Roadmap — what comes next, phase by phase

This mirrors the phase plan you gave me, so nothing gets dumped in one
unreviewable block:

| Phase | Adds |
|---|---|
| 3 | Task editing/deletion UI, categories filter, task detail panel |
| 4 | Goals (hierarchical: Life → Year → Month → Project) + auto-progress from tasks |
| 5 | Projects grouping tasks, dynamic progress calculation |
| 6 | Calendar view (DatePicker-driven agenda of tasks/events/habits/focus) |
| 7 | Habits + streak tracking |
| 8 | Finance (income/expenses/budgets + charts) |
| 9 | Focus Mode (countdown timer, session history, XP) |
| 10 | NexusCore-equivalent (`MomentaCore`) + `PriorityEngine` + "What should I do now?" |
| 11 | `ScheduledExecutorService` deadline monitor + Notification Center |
| 12 | Analytics dashboard (BarChart/PieChart/LineChart) |
| 13 | Command palette (Ctrl+K), XP/gamification, transitions & polish |

Say the word and we'll do Phase 3 (Goals + Projects) next, following the
same MVC/DAO/threading pattern established here.
