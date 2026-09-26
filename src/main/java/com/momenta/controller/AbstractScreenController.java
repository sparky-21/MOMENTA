package com.momenta.controller;

import com.momenta.threading.TaskExecutor;
import com.momenta.utility.AlertUtil;
import javafx.concurrent.Task;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Common base for every MOMENTA screen controller (Tasks, Projects, Goals,
 * Habits, ...).
 *
 * Before this class existed, {@code runInBackground(Callable, Consumer)} was
 * copy-pasted, byte-for-byte, into TaskController, ProjectController and
 * GoalController: build a javafx.concurrent.Task, submit it to the shared
 * pool, route success back onto the JavaFX Application Thread, route any
 * exception to AlertUtil instead of letting it crash the UI. That is exactly
 * the kind of shared behaviour an abstract class exists for — every screen
 * controller needs it, no screen controller should have to rewrite it, and
 * none of them should be instantiated on their own (a "screen controller"
 * that loads nothing and saves nothing isn't a real screen).
 *
 * Template-method pattern: {@link #refresh()} is the one thing this class
 * cannot know how to do (each screen's data is different), so it is left
 * abstract. Subclasses only decide *what* refresh() reloads; *how* work
 * hops from the background thread back to the UI thread is decided once,
 * here, and inherited everywhere.
 */
public abstract class AbstractScreenController {

    /**
     * Reloads this screen's data from the database and repopulates its
     * ObservableList(s). Called once right after FXML injection finishes,
     * and again after CRUD operations that dashboards elsewhere depend on
     * invalidate this screen's cached scene.
     */
    protected abstract void refresh();

    /**
     * Runs {@code work} on the shared background pool and hands the result
     * to {@code onDone} back on the JavaFX Application Thread. Any
     * exception thrown by {@code work} is caught and shown through
     * {@link AlertUtil} rather than reaching the user as a raw stack trace.
     */
    protected final <T> void runInBackground(Callable<T> work, Consumer<T> onDone) {
        Task<T> bgTask = new Task<>() {
            @Override
            protected T call() throws Exception {
                return work.call();
            }
        };
        bgTask.setOnSucceeded(e -> onDone.accept(bgTask.getValue()));
        bgTask.setOnFailed(e -> AlertUtil.showError(
                "Operation failed",
                "Something went wrong while talking to the database. Please try again.",
                bgTask.getException()));
        TaskExecutor.getInstance().workPool().submit(bgTask);
    }
}
