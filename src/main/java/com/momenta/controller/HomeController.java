package com.momenta.controller;

import com.momenta.utility.SceneManager;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

/** Premium public landing page for MOMENTA. Keeps application features unchanged. */
public class HomeController {

    @FXML private HBox featureRow;

    @FXML
    public void initialize() {
        if (featureRow != null) {
            for (Node child : featureRow.getChildren()) {
                child.setOpacity(0);
                child.setTranslateY(16);
            }
        }
    }

    @FXML
    private void onLogin() {
        SceneManager.getInstance().switchTo("Login");
    }

    @FXML
    private void onRegister() {
        SceneManager.getInstance().switchTo("Register");
    }

    /** Called by MomentaTheme after the page is styled. */
    public void playEntrance() {
        if (featureRow == null) return;
        javafx.animation.SequentialTransition sequence = new javafx.animation.SequentialTransition();
        for (Node child : featureRow.getChildren()) {
            FadeTransition fade = new FadeTransition(Duration.millis(260), child);
            fade.setFromValue(0); fade.setToValue(1);
            javafx.animation.TranslateTransition slide = new javafx.animation.TranslateTransition(Duration.millis(320), child);
            slide.setFromY(16); slide.setToY(0);
            sequence.getChildren().add(new ParallelTransition(fade, slide));
        }
        sequence.setDelay(Duration.millis(180));
        sequence.play();
    }

    public void hover(Node node, boolean entering) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(140), node);
        scale.setToX(entering ? 1.025 : 1.0);
        scale.setToY(entering ? 1.025 : 1.0);
        scale.play();
    }
}
