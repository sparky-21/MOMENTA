package com.momenta.controller;

import com.momenta.model.User;
import com.momenta.service.UserService;
import com.momenta.utility.CurrentUser;
import com.momenta.utility.SceneManager;
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

        try {
            User user = userService.register(
                    nameField.getText(), usernameField.getText(),
                    passwordField.getText(), profession
            );
            CurrentUser.login(user);
            SceneManager.getInstance().invalidate("Dashboard");
            SceneManager.getInstance().switchTo("Dashboard");
        } catch (IllegalArgumentException e) {
            messageLabel.setText(e.getMessage());
        } catch (RuntimeException e) {
            messageLabel.setText("Unable to create account.");
            e.printStackTrace();
        }
    }

    @FXML
    private void onBack() {
        SceneManager.getInstance().switchTo("Login");
    }
}
