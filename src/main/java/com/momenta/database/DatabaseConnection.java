package com.momenta.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Singleton wrapper around the single SQLite JDBC connection MOMENTA uses.
 *
 * WHY a singleton here (Section 32 — Design Patterns):
 * SQLite is file-based and only tolerates one writer at a time. Rather than
 * opening/closing a connection per DAO call (expensive, and a source of
 * "database is locked" bugs), MOMENTA keeps one long-lived Connection and
 * lets every DAO borrow it. Foreign key enforcement is turned on here too,
 * since SQLite disables it by default.
 *
 * WHY an absolute path (not "jdbc:sqlite:momenta.db"):
 * A bare relative filename resolves against the process's *current working
 * directory*, which differs depending on how the app is launched — an IDE
 * run config, "mvn javafx:run" from the module folder vs the parent folder,
 * or a packaged jar — can each have a different working directory. That
 * silently opens/creates a *different* momenta.db each time, which looks
 * exactly like "nothing is being saved". Pinning the path to a fixed
 * location under the user's home folder makes every launch method open the
 * same physical file.
 */
public final class DatabaseConnection {

    private static final String DB_DIR =
            System.getProperty("user.home") + File.separator + ".momenta";
    private static final String DB_FILE = DB_DIR + File.separator + "momenta.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE;
    private static Connection connection;

    private DatabaseConnection() {
        // no instances
    }

    public static synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                File dir = new File(DB_DIR);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                connection = DriverManager.getConnection(DB_URL);
                // SQLite ignores FOREIGN KEY constraints unless this pragma
                // is set on every connection — easy to forget, so we do it
                // once, centrally, here.
                try (Statement pragma = connection.createStatement()) {
                    pragma.execute("PRAGMA foreign_keys = ON;");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to " + DB_FILE, e);
        }
        return connection;
    }

    public static synchronized void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
}