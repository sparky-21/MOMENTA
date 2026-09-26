package com.momenta.utility;

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton (Section 32) responsible for switching between the FXML views
 * listed in Section 31 (Dashboard, Tasks, Goals, ...). Controllers never
 * touch the Stage directly — they call SceneManager.getInstance().switchTo(name)
 * so navigation logic stays in one place.
 *
 * Loaded Parents are cached so revisiting a screen doesn't reparse its FXML
 * every time, and a short FadeTransition (Section 26) is used for view
 * changes so navigation feels intentional rather than an instant flash.
 */
public final class SceneManager {

    private static final SceneManager INSTANCE = new SceneManager();

    private Stage stage;
    private Scene scene;
    private final Map<String, Parent> viewCache = new HashMap<>();

    private SceneManager() {
    }

    public static SceneManager getInstance() {
        return INSTANCE;
    }

    public void init(Stage stage) {
        this.stage = stage;
    }

    /**
     * @param viewName matches a file under /view, e.g. "Dashboard" -> Dashboard.fxml
     */
    public void switchTo(String viewName) {
        try {
            Parent root = viewCache.computeIfAbsent(viewName, this::load);

            if (scene == null) {
                scene = new Scene(root, 1200, 750);
                stage.setScene(scene);
            } else {
                scene.setRoot(root);
            }

            // Phase 18: keep every view bounded by the live window size.
            // maxWidth/maxHeight bindings avoid the old dashboard min-width
            // feedback problem while still demonstrating JavaFX property binding.
            bindRootToScene(root);

            // Phase 19: apply the complete MOMENTA palette without CSS.
            MomentaTheme.apply(root, viewName);

            FadeTransition fade = new FadeTransition(Duration.millis(220), root);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();

        } catch (RuntimeException e) {
            AlertUtil.showError("Navigation error", "Could not open " + viewName + ".", e);
        }
    }

    /** Drops a cached view so the next switchTo(viewName) reloads it fresh from data. */
    public void invalidate(String viewName) {
        viewCache.remove(viewName);
    }

    private void bindRootToScene(Parent root) {
        if (!(root instanceof javafx.scene.layout.Region region) || scene == null) {
            return;
        }

        region.maxWidthProperty().unbind();
        region.maxHeightProperty().unbind();
        region.maxWidthProperty().bind(scene.widthProperty());
        region.maxHeightProperty().bind(scene.heightProperty());
    }

    private Parent load(String viewName) {
        try {
            String path = "/com/momenta/view/" + viewName + ".fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            return loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load view: " + viewName, e);
        }
    }
}
