package com.momenta.controller;

import com.momenta.model.User;
import com.momenta.service.UserService;
import com.momenta.threading.TaskExecutor;
import com.momenta.utility.CurrentUser;
import com.momenta.utility.SceneManager;
import com.momenta.utility.SessionStore;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {
    @FXML private TextField nameField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> professionBox;
    @FXML private TextField otherProfessionField;
    @FXML private Label messageLabel;

    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        professionBox.getItems().addAll(
                "Student", "Software Developer", "Teacher", "Doctor", "Designer",
                "Business Owner", "Researcher", "Freelancer", "Entrepreneur",
                "Job Seeker", "Professional", "Other"
        );
        professionBox.setOnAction(e -> {
            boolean other = "Other".equals(professionBox.getValue());
            otherProfessionField.setVisible(other);
            otherProfessionField.setManaged(other);
        });
    }

    @FXML
    private void onRegister() {
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            messageLabel.setText("Passwords do not match.");
            return;
        }

        String profession = professionBox.getValue();
        if ("Other".equals(profession)) profession = otherProfessionField.getText();

        // Rule #14: registration (unique-username check + insert +
        // password hashing) used to run straight on the FX Application
        // Thread. Moved onto TaskExecutor's pool for the same reason as
        // LoginController.onLogin().
        final String name = nameField.getText();
        final String username = usernameField.getText();
        final String password = passwordField.getText();
        final String finalProfession = profession;

        messageLabel.setText("");

        javafx.concurrent.Task<User> registerTask = new javafx.concurrent.Task<>() {
            @Override
            protected User call() {
                return userService.register(name, username, password, finalProfession);
            }
        };

        registerTask.setOnSucceeded(e -> {
            User newUser = registerTask.getValue();
            CurrentUser.login(newUser);
            // A brand-new account signs itself in immediately, so remember
            // it too — otherwise the person would hit the login screen on
            // their very next launch right after just registering.
            SessionStore.remember(newUser.getId());
            SceneManager.getInstance().invalidate("Dashboard");
            SceneManager.getInstance().switchTo("Dashboard");
        });

        registerTask.setOnFailed(e -> {
            Throwable ex = registerTask.getException();
            if (ex instanceof IllegalArgumentException) {
                messageLabel.setText(ex.getMessage());
            } else {
                messageLabel.setText("Unable to create account.");
                if (ex != null) ex.printStackTrace();
            }
        });

        TaskExecutor.getInstance().workPool().submit(registerTask);
    }

    @FXML
    private void onBack() {
        SceneManager.getInstance().switchTo("Login");
    }
}
