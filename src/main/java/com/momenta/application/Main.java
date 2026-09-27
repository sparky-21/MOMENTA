package com.momenta.application;

import com.momenta.database.DatabaseInitializer;
import com.momenta.model.User;
import com.momenta.service.NotificationScheduler;
import com.momenta.service.SettingsService;
import com.momenta.service.UserService;
import com.momenta.threading.TaskExecutor;
import com.momenta.utility.CurrentUser;
import com.momenta.utility.SceneManager;
import com.momenta.utility.SessionStore;
import javafx.application.Application;
import javafx.stage.Stage;

/** Application bootstrap. */
public class Main extends Application {
    private final NotificationScheduler notificationScheduler = new NotificationScheduler();
    private final UserService userService = new UserService();

    @Override
    public void start(Stage primaryStage) {
        DatabaseInitializer.initialize();

        SceneManager.getInstance().init(primaryStage);

        primaryStage.setTitle("MOMENTA — Personal Operating System");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(620);

        notificationScheduler.start();

        // SessionStore is a local java.util.prefs read (no I/O worth
        // worrying about), so it's safe to check synchronously right here,
        // before the window is ever shown or a scene picked.
        int rememberedUserId = SessionStore.getRememberedUserId();

        if (rememberedUserId < 0) {
            SceneManager.getInstance().switchTo("Home");

            // Warm up Login/Register in the background so the first click on
            // "Create an account" / "Log in" doesn't pay the first-load CSS
            // and layout cost that switchTo("Home") just paid for Home itself.
            SceneManager.getInstance().preload("Login", "Register");

            primaryStage.show();
            return;
        }

        // A "Keep me signed in" session exists. The account lookup itself
        // still has to run off the JavaFX Application Thread (it's a SQLite
        // SELECT), but resolving it *before* primaryStage.show() means the
        // person's very first frame is the real destination — either their
        // landing view or, on failure, Home — instead of Home appearing for
        // a moment and then being swapped out from under them once the
        // lookup finishes. Home is preloaded in the background so it's
        // instant if the lookup fails or the account no longer exists.
        SceneManager.getInstance().preload("Home", "Login", "Register");
        resolveRememberedSession(primaryStage, rememberedUserId);
    }

    /**
     * "Entry bypass" system: if a user previously checked "Keep me signed
     * in" (SessionStore), skip Login/Register on this launch and land
     * straight on their configured landing view instead — resolved before
     * the window is shown, so no intermediate screen is ever visible.
     */
    private void resolveRememberedSession(Stage primaryStage, int rememberedUserId) {
        javafx.concurrent.Task<User> lookupTask = new javafx.concurrent.Task<>() {
            @Override
            protected User call() {
                return userService.getById(rememberedUserId);
            }
        };

        lookupTask.setOnSucceeded(e -> {
            User user = lookupTask.getValue();
            if (user == null) {
                // Account no longer exists — clear the stale remembered id
                // and fall back to Home, same as a normal first launch.
                SessionStore.forget();
                SceneManager.getInstance().switchTo("Home");
            } else {
                CurrentUser.login(user);
                String landingView = SettingsService.getLandingView();
                SceneManager.getInstance().switchTo(landingView);
            }
            primaryStage.show();
        });

        lookupTask.setOnFailed(e -> {
            // Auto-login is a convenience, not a requirement — if it fails
            // the person just lands on Home and logs in manually.
            Throwable ex = lookupTask.getException();
            if (ex != null) ex.printStackTrace();
            SceneManager.getInstance().switchTo("Home");
            primaryStage.show();
        });

        TaskExecutor.getInstance().workPool().submit(lookupTask);
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