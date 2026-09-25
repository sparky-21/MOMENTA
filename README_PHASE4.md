# MOMENTA Phase 4 — Authentication + Task Workspace

This package is built on top of the MOMENTA Phase 3 project structure.

## What was added

1. Login screen shown first at application launch.
2. Create-account dialog with profile type.
3. SHA-256 password hashing before local SQLite storage.
4. SessionManager for the authenticated user.
5. User-scoped tasks through `tasks.user_id`.
6. Automatic SQLite migration for an existing Phase 3 database that does not yet have `tasks.user_id`.
7. Task add / edit / complete / delete / refresh.
8. Task fields: title, description, category, importance, deadline, estimated duration, status.
9. Background database operations using JavaFX `Task` + the existing `AppExecutor`.
10. Premium dark FXML layout without a stylesheet. Visual properties are defined in FXML/Scene Builder-friendly JavaFX properties.

## Important merge rule

Do NOT delete the `.git` folder. This project archive keeps the existing Git repository.

Replace the project files with this Phase 4 version, but keep your own `momenta.db` if you already have real local data.

Then run:

```bash
mvn clean javafx:run
```

If Maven is configured through IntelliJ, reload the Maven project and run the existing Launcher configuration.

## First run

Because the login is now the first screen, either:

- click **Create a new account**, create a user, then sign in; or
- use an account already stored in the database with a SHA-256 password created by this Phase 4 registration flow.

## Scene Builder

Open these files in Scene Builder:

- `src/main/resources/com/momenta/view/login.fxml`
- `src/main/resources/com/momenta/view/register.fxml`
- `src/main/resources/com/momenta/view/dashboard.fxml`

There is no external CSS file and there are no CSS stylesheet references.

## Architecture

```text
FXML
  ↓
Controller
  ↓
Service
  ↓
DAO
  ↓
JDBC
  ↓
SQLite
```

For background operations:

```text
JavaFX Application Thread
        ↓
JavaFX Task
        ↓
AppExecutor
        ↓
DAO / SQLite
        ↓
Task succeeded
        ↓
JavaFX Application Thread
        ↓
Observable UI
```

## Phase 4 Git commit suggestion

```bash
git status
git add .
git commit -m "Phase 4 - Authentication and task management"
git push origin main
```

Do not force-push.
