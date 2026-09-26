package com.momenta.application;

import com.momenta.database.DatabaseInitializer;
import com.momenta.service.NotificationScheduler;
import com.momenta.threading.TaskExecutor;
import com.momenta.utility.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

/** Application bootstrap. */
public class Main extends Application {
    private final NotificationScheduler notificationScheduler = new NotificationScheduler();

    @Override
    public void start(Stage primaryStage) {
        DatabaseInitializer.initialize();

        SceneManager.getInstance().init(primaryStage);
        SceneManager.getInstance().switchTo("Login");

        primaryStage.setTitle("MOMENTA — Personal Operating System");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(620);
        primaryStage.show();

        notificationScheduler.start();
    }

    @Override
    public void stop() {
        notificationScheduler.stop();
        TaskExecutor.getInstance().shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}