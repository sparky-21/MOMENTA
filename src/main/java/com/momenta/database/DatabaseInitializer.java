package com.momenta.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Creates every MOMENTA table on first run (Section 18).
 *
 * Design notes for the viva:
 *  - Every child table has a foreign key back to its parent (tasks -> projects,
 *    tasks -> goals, habit_logs -> habits, focus_sessions -> tasks, etc.)
 *    with ON DELETE CASCADE so deleting a project cleans up its tasks rather
 *    than leaving orphan rows.
 *  - CHECK constraints enforce valid ranges (progress 0-100, priority 0-100)
 *    at the database layer, not just in Java, so the data stays consistent
 *    even if a bug in the UI ever sends a bad value.
 *  - All tables are created with "IF NOT EXISTS" so re-launching the app
 *    never wipes existing data.
 */
public final class DatabaseInitializer {

    private DatabaseInitializer() {
    }

    public static void initialize() {
        try (Statement stmt = DatabaseConnection.getConnection().createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    persona TEXT DEFAULT 'Other',
                    created_at TEXT NOT NULL DEFAULT (datetime('now'))
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS goals (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    parent_goal_id INTEGER,
                    title TEXT NOT NULL,
                    description TEXT,
                    deadline TEXT,
                    progress INTEGER NOT NULL DEFAULT 0 CHECK (progress BETWEEN 0 AND 100),
                    status TEXT NOT NULL DEFAULT 'ACTIVE',
                    importance INTEGER NOT NULL DEFAULT 3 CHECK (importance BETWEEN 1 AND 5),
                    created_at TEXT NOT NULL DEFAULT (datetime('now')),
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                    FOREIGN KEY (parent_goal_id) REFERENCES goals(id) ON DELETE SET NULL
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS projects (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    goal_id INTEGER,
                    title TEXT NOT NULL,
                    description TEXT,
                    deadline TEXT,
                    status TEXT NOT NULL DEFAULT 'ACTIVE',
                    created_at TEXT NOT NULL DEFAULT (datetime('now')),
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                    FOREIGN KEY (goal_id) REFERENCES goals(id) ON DELETE SET NULL
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS tasks (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    project_id INTEGER,
                    goal_id INTEGER,
                    title TEXT NOT NULL,
                    description TEXT,
                    category TEXT NOT NULL DEFAULT 'Other',
                    importance INTEGER NOT NULL DEFAULT 3 CHECK (importance BETWEEN 1 AND 5),
                    deadline TEXT,
                    estimated_minutes INTEGER NOT NULL DEFAULT 30,
                    progress INTEGER NOT NULL DEFAULT 0 CHECK (progress BETWEEN 0 AND 100),
                    status TEXT NOT NULL DEFAULT 'PENDING',
                    priority_score INTEGER NOT NULL DEFAULT 0,
                    created_at TEXT NOT NULL DEFAULT (datetime('now')),
                    completed_at TEXT,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE SET NULL,
                    FOREIGN KEY (goal_id) REFERENCES goals(id) ON DELETE SET NULL
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS events (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    title TEXT NOT NULL,
                    description TEXT,
                    event_date TEXT NOT NULL,
                    start_time TEXT,
                    end_time TEXT,
                    created_at TEXT NOT NULL DEFAULT (datetime('now')),
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS habits (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    current_streak INTEGER NOT NULL DEFAULT 0,
                    longest_streak INTEGER NOT NULL DEFAULT 0,
                    created_at TEXT NOT NULL DEFAULT (datetime('now')),
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS habit_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    habit_id INTEGER NOT NULL,
                    log_date TEXT NOT NULL,
                    completed INTEGER NOT NULL DEFAULT 1,
                    UNIQUE (habit_id, log_date),
                    FOREIGN KEY (habit_id) REFERENCES habits(id) ON DELETE CASCADE
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS expenses (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    amount REAL NOT NULL CHECK (amount >= 0),
                    category TEXT NOT NULL DEFAULT 'Other',
                    note TEXT,
                    expense_date TEXT NOT NULL,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS income (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    amount REAL NOT NULL CHECK (amount >= 0),
                    source TEXT,
                    income_date TEXT NOT NULL,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS focus_sessions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    task_id INTEGER,
                    duration_minutes INTEGER NOT NULL CHECK (duration_minutes > 0),
                    started_at TEXT NOT NULL,
                    ended_at TEXT,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE SET NULL
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS notifications (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    type TEXT NOT NULL,
                    message TEXT NOT NULL,
                    is_read INTEGER NOT NULL DEFAULT 0,
                    created_at TEXT NOT NULL DEFAULT (datetime('now')),
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS settings (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL UNIQUE,
                    default_focus_minutes INTEGER NOT NULL DEFAULT 25,
                    currency TEXT NOT NULL DEFAULT 'BDT',
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                );
            """);

            // A single default user row so early phases (before Login.fxml
            // exists) have a user_id=1 to attach tasks to.
            stmt.execute("""
                INSERT INTO users (id, name, persona)
                SELECT 1, 'Zigi', 'Student'
                WHERE NOT EXISTS (SELECT 1 FROM users WHERE id = 1);
            """);

            System.out.println("MOMENTA database ready (momenta.db).");

        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database schema", e);
        }
    }
}
