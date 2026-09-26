package com.momenta.controller;

import com.momenta.service.SettingsService;
import com.momenta.utility.AlertUtil;
import com.momenta.utility.CurrentUser;
import com.momenta.utility.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

/** Phase 23 - Settings and Personalization screen. */
public class SettingsController {
    @FXML private Label accountLabel;
    @FXML private ComboBox<String> themeBox;
    @FXML private ComboBox<String> accentBox;
    @FXML private Slider fontScaleSlider;
    @FXML private Label fontScaleLabel;
    @FXML private CheckBox animationsCheck;
    @FXML private CheckBox compactCheck;
    @FXML private ComboBox<String> landingViewBox;

    @FXML
    public void initialize() {
        accountLabel.setText(CurrentUser.isLoggedIn()
                ? "Personal settings for " + CurrentUser.getName()
                : "Personal settings");

        themeBox.getItems().setAll("Light", "Dark");
        accentBox.getItems().setAll("Sea Green", "Sky Blue", "Lavender", "Soft Green", "Peach", "Pink", "Periwinkle");
        landingViewBox.getItems().setAll("Dashboard", "Tasks", "Goals", "Projects", "Calendar", "Habits", "Finance", "Focus", "Analytics");

        themeBox.setValue(SettingsService.getTheme());
        accentBox.setValue(SettingsService.getAccent());
        landingViewBox.setValue(SettingsService.getLandingView());
        fontScaleSlider.setMin(85);
        fontScaleSlider.setMax(125);
        fontScaleSlider.setMajorTickUnit(10);
        fontScaleSlider.setMinorTickCount(1);
        fontScaleSlider.setBlockIncrement(5);
        fontScaleSlider.setValue(SettingsService.getFontScale() * 100.0);
        animationsCheck.setSelected(SettingsService.isAnimationsEnabled());
        compactCheck.setSelected(SettingsService.isCompactMode());
        updateFontScaleLabel();
        fontScaleSlider.valueProperty().addListener((obs, oldValue, newValue) -> updateFontScaleLabel());
    }

    private void updateFontScaleLabel() {
        fontScaleLabel.setText(String.format("%.0f%%", fontScaleSlider.getValue()));
    }

    @FXML
    private void onApply() {
        SettingsService.setTheme(themeBox.getValue());
        SettingsService.setAccent(accentBox.getValue());
        SettingsService.setFontScale(fontScaleSlider.getValue() / 100.0);
        SettingsService.setAnimationsEnabled(animationsCheck.isSelected());
        SettingsService.setCompactMode(compactCheck.isSelected());
        SettingsService.setLandingView(landingViewBox.getValue());

        SceneManager.getInstance().invalidateAll();
        SceneManager.getInstance().switchTo("Settings");
        AlertUtil.showInfo("Settings saved", "Your MOMENTA personalization has been saved.");
    }

    @FXML
    private void onReset() {
        SettingsService.resetDefaults();
        initialize();
        SceneManager.getInstance().invalidateAll();
        SceneManager.getInstance().switchTo("Settings");
    }

    @FXML
    private void onBack() {
        SceneManager.getInstance().invalidate("Dashboard");
        SceneManager.getInstance().switchTo("Dashboard");
    }
}
