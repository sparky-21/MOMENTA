package com.momenta.controller;

import com.momenta.model.User;
import com.momenta.service.UserService;
import com.momenta.utility.CurrentUser;
import com.momenta.utility.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    private final UserService userService = new UserService();

    @FXML
    private void onLogin() {
        try {
            User user = userService.login(usernameField.getText(), passwordField.getText());
            CurrentUser.login(user);
            SceneManager.getInstance().invalidate("Dashboard");
            SceneManager.getInstance().switchTo("Dashboard");
        } catch (IllegalArgumentException e) {
            messageLabel.setText(e.getMessage());
        } catch (RuntimeException e) {
            messageLabel.setText("Unable to login right now.");
            e.printStackTrace();
        }
    }

    @FXML
    private void onRegister() {
        SceneManager.getInstance().switchTo("Register");
    }
}
