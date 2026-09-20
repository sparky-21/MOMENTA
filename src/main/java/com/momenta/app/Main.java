package com.momenta.app;

import com.momenta.database.DatabaseManager;
import com.momenta.threading.AppExecutor;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * MOMENTA — Personal Operating System
 * "One place to run your life."
 *
 * This is the JavaFX entry point. Its only jobs are:
 *   1. Initialize the database (create tables if they don't exist)
 *   2. Load the root FXML view (dashboard.fxml)
 *   3. Show the primary Stage
 *
 * No business logic, no SQL, no layout code lives here — that's the whole
 * point of MVC: Main just wires the app together and gets out of the way.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        // 1. Make sure the SQLite database file + schema exist before any
        //    screen tries to read from it.
        DatabaseManager.getInstance().initializeSchema();

        // 2. Load the dashboard UI. The FXML file is what you'll open and
        //    edit visually in Scene Builder.
        FXMLLoader loader = new FXMLLoader(
                Main.class.getResource("/com/momenta/view/dashboard.fxml"));
        Parent root = loader.load();

        // 3. Show it.
        Scene scene = new Scene(root, 1280, 800);
        primaryStage.setTitle("MOMENTA — One place to run your life.");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(700);
        primaryStage.show();

        // Make sure background threads don't keep the JVM alive after the
        // window is closed.
        primaryStage.setOnCloseRequest(e -> {
            AppExecutor.shutdown();
            DatabaseManager.getInstance().closeConnection();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
