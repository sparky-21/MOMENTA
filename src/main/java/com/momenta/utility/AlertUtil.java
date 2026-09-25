package com.momenta.utility;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * Section 34 — Error Handling.
 *
 * No raw exception or stack trace should ever reach the user. Every failure
 * path in MOMENTA routes through one of these methods so dialogs stay
 * consistent, and the technical detail (exception message) is logged to
 * stderr rather than shown in the dialog body.
 */
public final class AlertUtil {

    private AlertUtil() {
    }

    public static void showError(String title, String userMessage, Throwable technical) {
        if (technical != null) {
            System.err.println("[" + title + "] " + technical.getMessage());
        }
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(userMessage);
        alert.showAndWait();
    }

    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
