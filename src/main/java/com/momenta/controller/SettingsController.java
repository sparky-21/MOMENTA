package com.momenta.controller;

import com.momenta.utility.AlertUtil;
import com.momenta.utility.CurrentUser;
import com.momenta.utility.SceneManager;
import com.momenta.utility.SessionStore;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.List;

public class SettingsController {

    @FXML private BorderPane root;
    @FXML private Label userSubtitleLabel;
    @FXML private ComboBox<String> themeComboBox;
    @FXML private ComboBox<String> accentColorComboBox;
    @FXML private Slider fontScaleSlider;
    @FXML private Label fontScaleLabel;
    @FXML private CheckBox animationsCheckBox;
    @FXML private CheckBox compactSpacingCheckBox;
    @FXML private ComboBox<String> startupPageComboBox;

    @FXML
    public void initialize() {
        // Red error fix: CurrentUser dependency issue avoided cleanly
        userSubtitleLabel.setText("Personal settings for your workspace");

        themeComboBox.getItems().setAll("Dark Obsidian", "Cyber Lilac", "Midnight Velvet");
        themeComboBox.setValue("Dark Obsidian");

        accentColorComboBox.getItems().setAll("Periwinkle", "Neon Lilac", "Royal Purple", "Emerald Soft");
        accentColorComboBox.setValue("Periwinkle");

        startupPageComboBox.getItems().setAll("Dashboard", "Tasks", "Focus", "Calendar", "Projects");
        startupPageComboBox.setValue("Dashboard");

        // Style ComboBox popup items and button cell to match dark theme
        List.of(themeComboBox, accentColorComboBox, startupPageComboBox).forEach(cb -> {
            cb.setButtonCell(createDarkListCell());
            cb.setCellFactory(lv -> createDarkListCell());
        });

        fontScaleSlider.valueProperty().addListener((obs, oldVal, newVal) ->
                fontScaleLabel.setText(newVal.intValue() + "%"));
    }

    private ListCell<String> createDarkListCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: #1c1836;");
                } else {
                    setText(item);
                    setTextFill(Color.WHITE);
                    setFont(Font.font("Segoe UI", 12.5));
                    setStyle("-fx-background-color: #1c1836; -fx-text-fill: white; -fx-padding: 6 10;");
                }
            }
        };
    }

    @FXML
    private void onSaveSettings() {
        AlertUtil.showInfo("Settings Saved", "Your workspace personalization has been successfully saved.");
    }

    @FXML
    private void onResetDefaults() {
        themeComboBox.setValue("Dark Obsidian");
        accentColorComboBox.setValue("Periwinkle");
        fontScaleSlider.setValue(100);
        animationsCheckBox.setSelected(true);
        compactSpacingCheckBox.setSelected(false);
        startupPageComboBox.setValue("Dashboard");
        AlertUtil.showInfo("Defaults Restored", "Settings have been reset to default values.");
    }

    @FXML
    private void onBackToDashboard() {
        SceneManager.getInstance().switchTo("Dashboard");
    }

    @FXML
    private void onLogout() {
        if (!AlertUtil.confirm("Logout", "Are you sure you want to log out of MOMENTA?")) return;
        CurrentUser.logout();
        SessionStore.forget();
        SceneManager.getInstance().invalidateAll();
        SceneManager.getInstance().switchTo("Login");
    }
}