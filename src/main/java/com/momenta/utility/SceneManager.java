package com.momenta.utility;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

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

            // Stay fully invisible until every step below (attach, bind,
            // theme, layout) has actually run. AnimationUtil.fadeIn() also
            // starts its FadeTransition at opacity 0, but a Transition only
            // takes effect on its first animation pulse — it does not
            // guarantee opacity is 0 the instant play() is called. Setting
            // it here ourselves, synchronously, closes that gap: whatever
            // JavaFX renders between scene.setRoot(root) below and the
            // fadeIn() call at the end, it will render nothing for this
            // node — see the Scene's own fill (set in the branch below) for
            // why that no longer means "white".
            root.setOpacity(0);

            if (scene == null) {
                scene = new Scene(root, 1200, 750);
                // JavaFX Scenes are white-filled by default. If anything
                // ever renders while root is transparent or not yet themed
                // (see setOpacity(0) above), this is what shows through —
                // MOMENTA's own background, not a flash of plain white.
                scene.setFill(MomentaTheme.NIGHT);
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
            root.applyCss();
            root.layout();

            // Phase 21: install Ctrl+K once on the application's Scene.
            CommandPalette.install(scene);

            // Phase 20: centralized page-entry animation. Everything above
            // has already happened, so this fade reveals the finished,
            // fully-themed view — never a part-way state.
            AnimationUtil.fadeIn(root, 220);

        } catch (RuntimeException e) {
            AlertUtil.showError("Navigation error", "Could not open " + viewName + ".", e);
        }
    }

    /** Drops a cached view so the next switchTo(viewName) reloads it fresh from data. */
    public void invalidate(String viewName) {
        viewCache.remove(viewName);
    }
    /**
     * Drops every cached view. Used after a Settings change (Phase 23) so theme,
     * accent, font scale and compact-mode updates are picked up by every screen
     * the next time it is opened, not just the one currently on screen.
     */
    public void invalidateAll() {
        viewCache.clear();
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

    /**
     * Loads an FXML view, themes it, and forces it through a full CSS +
     * layout pass immediately — instead of leaving any of that for
     * switchTo() to do after the view is already attached to the Scene.
     *
     * This used to call only applyCss()+layout() here and leave
     * MomentaTheme.apply() for switchTo() to call afterward. That left a
     * window where a Parent could sit in viewCache still wearing JavaFX's
     * default look — white text fields, plain gray buttons — because
     * nothing had painted MOMENTA's colors onto it yet. Whether that
     * default look was actually visible depended on incidental
     * scene-attachment/pulse timing, which is exactly why it showed up as
     * a flash on the very first visit to a view and never again
     * afterwards (once themed, the same cached Parent instance stays
     * themed).
     *
     * Doing MomentaTheme.apply() right here instead means every Parent
     * that ever enters viewCache — whether loaded eagerly by preload() or
     * lazily by switchTo() — is already fully themed before its first
     * CSS/layout pass and before it is ever attached to a Scene. There is
     * no unstyled state left for a cached view to be caught in.
     */
    private Parent load(String viewName) {
        try {
            String path = "/com/momenta/view/" + viewName + ".fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();
            MomentaTheme.apply(root, viewName);
            root.applyCss();
            root.layout();
            return root;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load view: " + viewName, e);
        }
    }

    /**
     * Loads a view into the cache without displaying it, so the first real
     * switchTo(viewName) call for it is instant and already warmed up
     * (FXML parsed, CSS/layout done) rather than paying that cost on the
     * user's first click. Must run on the JavaFX Application Thread, same
     * as any FXML/Node work, so this always goes through Platform.runLater
     * even when called from the FX thread itself.
     *
     * Only views whose controllers do not require CurrentUser to already be
     * logged in should be preloaded this way (e.g. Login, Register) —
     * Dashboard and the other workspace screens read CurrentUser.getId()
     * during initialize() and would throw if warmed before login.
     */
    public void preload(String... viewNames) {
        javafx.application.Platform.runLater(() -> {
            for (String viewName : viewNames) {
                viewCache.computeIfAbsent(viewName, this::load);
            }
        });
    }
}
