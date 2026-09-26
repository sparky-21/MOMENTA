package com.momenta.utility;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.control.ProgressBar;
import javafx.util.Duration;

import java.util.List;

/**
 * Phase 20 — Central JavaFX animation helper.
 *
 * Animations are intentionally short and subtle so MOMENTA feels responsive
 * without becoming distracting. No CSS is used.
 */
public final class AnimationUtil {

    private AnimationUtil() {
    }

    public static void fadeIn(Node node) {
        fadeIn(node, 260);
    }

    public static void fadeIn(Node node, double millis) {
        if (node == null) return;
        FadeTransition fade = new FadeTransition(Duration.millis(millis), node);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }

    public static void slideIn(Node node, double fromY) {
        if (node == null) return;
        node.setTranslateY(fromY);

        TranslateTransition slide = new TranslateTransition(Duration.millis(280), node);
        slide.setFromY(fromY);
        slide.setToY(0);
        slide.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition fade = new FadeTransition(Duration.millis(220), node);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);

        ParallelTransition animation = new ParallelTransition(slide, fade);
        animation.play();
    }

    public static void popIn(Node node) {
        if (node == null) return;
        node.setOpacity(0);
        node.setScaleX(0.94);
        node.setScaleY(0.94);

        FadeTransition fade = new FadeTransition(Duration.millis(220), node);
        fade.setFromValue(0);
        fade.setToValue(1);

        ScaleTransition scale = new ScaleTransition(Duration.millis(260), node);
        scale.setFromX(0.94);
        scale.setFromY(0.94);
        scale.setToX(1);
        scale.setToY(1);
        scale.setInterpolator(Interpolator.EASE_OUT);

        new ParallelTransition(fade, scale).play();
    }

    public static void pulse(Node node) {
        if (node == null) return;
        ScaleTransition scale = new ScaleTransition(Duration.millis(180), node);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.05);
        scale.setToY(1.05);
        scale.setAutoReverse(true);
        scale.setCycleCount(2);
        scale.play();
    }

    public static void success(Node node) {
        if (node == null) return;

        ScaleTransition scale = new ScaleTransition(Duration.millis(160), node);
        scale.setFromX(1);
        scale.setFromY(1);
        scale.setToX(1.08);
        scale.setToY(1.08);
        scale.setAutoReverse(true);
        scale.setCycleCount(2);

        RotateTransition rotate = new RotateTransition(Duration.millis(160), node);
        rotate.setFromAngle(-2);
        rotate.setToAngle(2);
        rotate.setAutoReverse(true);
        rotate.setCycleCount(2);

        new ParallelTransition(scale, rotate).play();
    }

    public static void animateProgress(ProgressBar bar, double target) {
        if (bar == null) return;
        double safeTarget = Math.max(0, Math.min(1, target));

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(bar.progressProperty(), bar.getProgress())),
                new KeyFrame(Duration.millis(450),
                        new KeyValue(bar.progressProperty(), safeTarget, Interpolator.EASE_BOTH))
        );
        timeline.play();
    }

    public static void playSequentialEntry(List<? extends Node> nodes) {
        if (nodes == null || nodes.isEmpty()) return;

        SequentialTransition sequence = new SequentialTransition();
        for (Node node : nodes) {
            if (node == null) continue;

            node.setOpacity(0);
            node.setTranslateY(12);

            FadeTransition fade = new FadeTransition(Duration.millis(180), node);
            fade.setFromValue(0);
            fade.setToValue(1);

            TranslateTransition slide = new TranslateTransition(Duration.millis(220), node);
            slide.setFromY(12);
            slide.setToY(0);
            slide.setInterpolator(Interpolator.EASE_OUT);

            sequence.getChildren().add(new ParallelTransition(fade, slide));
        }
        sequence.setDelay(Duration.millis(60));
        sequence.play();
    }
}
