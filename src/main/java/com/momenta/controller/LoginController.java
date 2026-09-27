package com.momenta.controller;

import com.momenta.model.User;
import com.momenta.service.UserService;
import com.momenta.service.SettingsService;
import com.momenta.threading.TaskExecutor;
import com.momenta.utility.CurrentUser;
import com.momenta.utility.SceneManager;
import com.momenta.utility.SessionStore;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberMeCheck;
    @FXML private Label messageLabel;

    private final UserService userService = new UserService();

    @FXML
    private void onLogin() {
        // Rule #14 (Section 33): userService.login() hits SQLite (password
        // hash lookup) — previously called straight on the JavaFX
        // Application Thread, briefly freezing the Login screen on every
        // attempt. Moved onto TaskExecutor's pool like every other DB call
        // in the app.
        final String username = usernameField.getText();
        final String password = passwordField.getText();

        messageLabel.setText("");

        javafx.concurrent.Task<User> loginTask = new javafx.concurrent.Task<>() {
            @Override
            protected User call() {
                return userService.login(username, password);
            }
        };

        loginTask.setOnSucceeded(e -> {
            User loggedInUser = loginTask.getValue();
            CurrentUser.login(loggedInUser);

            // "Entry bypass" system (login-fatigue fix): remember this user
            // so the next launch skips Login/Register entirely, unless the
            // person unchecks "Keep me signed in".
            if (rememberMeCheck != null && rememberMeCheck.isSelected()) {
                SessionStore.remember(loggedInUser.getId());
            } else {
                SessionStore.forget();
            }

            String landingView = SettingsService.getLandingView();
            SceneManager.getInstance().invalidate(landingView);
            SceneManager.getInstance().switchTo(landingView);
        });

        loginTask.setOnFailed(e -> {
            Throwable ex = loginTask.getException();
            if (ex instanceof IllegalArgumentException) {
                messageLabel.setText(ex.getMessage());
            } else {
                messageLabel.setText("Unable to login right now.");
                if (ex != null) ex.printStackTrace();
            }
        });

        TaskExecutor.getInstance().workPool().submit(loginTask);
    }

    @FXML
    private void onHome() {
        SceneManager.getInstance().switchTo("Home");
    }

    @FXML
    private void onRegister() {
        SceneManager.getInstance().switchTo("Register");
    }
}
