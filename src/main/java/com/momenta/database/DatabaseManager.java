package com.momenta.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Owns the single JDBC Connection to MOMENTA's SQLite database file and
 * creates the full schema on first run.
 *
 * WHY SINGLETON HERE (and only here):
 * SQLite is a file-based database — opening many separate connections from
 * different parts of the app invites locking issues ("database is locked"
 * errors) when two writes overlap. A single shared Connection, reused by
 * every DAO, avoids that. This is one of the few places in MOMENTA where
 * Singleton is actually justified, per the "don't force patterns" rule —
 * it's not used for random unrelated classes.
 *
 * DAOs never write raw SQL string concatenation here or anywhere else —
 * every query in the DAO layer uses PreparedStatement with bound
 * parameters, which is what actually prevents SQL injection.
 */
public final class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:momenta.db";
    private static DatabaseManager instance;

    private Connection connection;

    private DatabaseManager() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            // Enforce FK constraints -- SQLite has them OFF by default.
            try (Statement pragma = connection.createStatement()) {
                pragma.execute("PRAGMA foreign_keys = ON;");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not open MOMENTA database", e);
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates every table MOMENTA needs, if it doesn't already exist.
     * Safe to call on every launch. Tables beyond `tasks` are created now
     * (schema is cheap to define upfront) even though their DAOs/services
     * will be built in later phases (Goals, Habits, Finance, Focus, ...).
     */
    public void initializeSchema() {
        String[] ddl = {
            """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                email TEXT UNIQUE,
                password TEXT,
                profile_type TEXT DEFAULT 'Other',
                xp INTEGER DEFAULT 0,
                level INTEGER DEFAULT 1,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP
            );
            """,
            """
            CREATE TABLE IF NOT EXISTS goals (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                description TEXT,
                tier TEXT NOT NULL,          -- LIFE, YEAR, MONTH, PROJECT
                parent_goal_id INTEGER,
                deadline TEXT,
                progress REAL DEFAULT 0,
                status TEXT DEFAULT 'ACTIVE',
                FOREIGN KEY (parent_goal_id) REFERENCES goals(id) ON DELETE SET NULL
            );
            """,
            """
            CREATE TABLE IF NOT EXISTS projects (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                description TEXT,
                goal_id INTEGER,
                deadline TEXT,
                progress REAL DEFAULT 0,
                status TEXT DEFAULT 'ACTIVE',
                FOREIGN KEY (goal_id) REFERENCES goals(id) ON DELETE SET NULL
            );
            """,
            """
            CREATE TABLE IF NOT EXISTS tasks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                description TEXT,
                category TEXT DEFAULT 'Other',
                importance INTEGER DEFAULT 3,     -- 1 (low) .. 5 (critical)
                deadline TEXT,                    -- ISO-8601 date-time
                estimated_minutes INTEGER DEFAULT 30,
                progress REAL DEFAULT 0,          -- 0.0 .. 1.0
                status TEXT DEFAULT 'PENDING',    -- PENDING, IN_PROGRESS, DONE
                project_id INTEGER,
                goal_id INTEGER,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                completed_at TEXT,
                FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE SET NULL,
                FOREIGN KEY (goal_id) REFERENCES goals(id) ON DELETE SET NULL
            );
            """,
            """
            CREATE TABLE IF NOT EXISTS events (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                start_time TEXT NOT NULL,
                end_time TEXT,
                description TEXT,
                related_task_id INTEGER,
                FOREIGN KEY (related_task_id) REFERENCES tasks(id) ON DELETE SET NULL
            );
            """,
            """
            CREATE TABLE IF NOT EXISTS habits (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                frequency TEXT DEFAULT 'DAILY',   -- DAILY, WEEKLY
                current_streak INTEGER DEFAULT 0,
                longest_streak INTEGER DEFAULT 0,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP
            );
            """,
            """
            CREATE TABLE IF NOT EXISTS habit_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                habit_id INTEGER NOT NULL,
                log_date TEXT NOT NULL,
                completed INTEGER DEFAULT 1,
                FOREIGN KEY (habit_id) REFERENCES habits(id) ON DELETE CASCADE
            );
            """,
            """
            CREATE TABLE IF NOT EXISTS income (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                source TEXT NOT NULL,
                amount REAL NOT NULL,
                received_on TEXT NOT NULL
            );
            """,
            """
            CREATE TABLE IF NOT EXISTS expenses (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                category TEXT NOT NULL,
                amount REAL NOT NULL,
                spent_on TEXT NOT NULL,
                note TEXT
            );
            """,
            """
            CREATE TABLE IF NOT EXISTS focus_sessions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                task_id INTEGER,
                started_at TEXT NOT NULL,
                ended_at TEXT,
                planned_minutes INTEGER,
                actual_minutes INTEGER,
                completed INTEGER DEFAULT 0,
                FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE SET NULL
            );
            """,
            """
            CREATE TABLE IF NOT EXISTS notifications (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                type TEXT NOT NULL,
                message TEXT NOT NULL,
                is_read INTEGER DEFAULT 0,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP
            );
            """,
            """
            CREATE TABLE IF NOT EXISTS settings (
                key TEXT PRIMARY KEY,
                value TEXT
            );
            """
        };

        try (Statement stmt = connection.createStatement()) {
            for (String sql : ddl) {
                stmt.execute(sql);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize MOMENTA schema", e);
        }
    }
}
