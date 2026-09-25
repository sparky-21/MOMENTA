package com.momenta.application;

import com.momenta.database.DatabaseInitializer;
import com.momenta.threading.TaskExecutor;
import com.momenta.utility.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Application entry point.
 *
 * Responsibilities of Main (and ONLY these):
 *   1. Bootstrap the SQLite database (create tables if they don't exist).
 *   2. Hand control to SceneManager, which owns the primary Stage and
 *      knows how to switch between FXML views.
 *   3. Guarantee that background threads (ExecutorService /
 *      ScheduledExecutorService) are shut down cleanly on exit.
 *
 * Main must NOT contain business logic, SQL, or JavaFX control code.
 * That separation is what keeps the MVC chain (UI -> Controller ->
 * Service -> Engine -> DAO -> Database) honest.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Phase 2: create momenta.db + all tables if this is a first run.
        DatabaseInitializer.initialize();

        // SceneManager owns the Stage from here on; it loads Dashboard.fxml
        // as the first view. Views are added via SceneManager.switchTo(...).
        SceneManager.getInstance().init(primaryStage);
        SceneManager.getInstance().switchTo("Dashboard");

        primaryStage.setTitle("MOMENTA — Personal Operating System");
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(700);
        primaryStage.show();
    }

    @Override
    public void stop() {
        // Section 21/23: executors must be shut down when the app closes.
        TaskExecutor.getInstance().shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
