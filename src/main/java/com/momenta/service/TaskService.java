package com.momenta.service;

import com.momenta.dao.TaskDAO;
import com.momenta.dao.impl.TaskDAOImpl;
import com.momenta.model.Task;
import com.momenta.threading.AppExecutor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


import java.util.List;
import java.util.function.Consumer;

/**
 * Business-logic layer for tasks. This is the ONLY class a Controller is
 * allowed to call for task operations — Controllers never see TaskDAO.
 *
 * THE THREADING PATTERN USED THROUGHOUT MOMENTA (viva talking point):
 * Every method here wraps its DAO call in a javafx.concurrent.Task<T>,
 * submits it to AppExecutor.pool(), and hands the result back through a
 * callback. javafx.concurrent.Task guarantees setOnSucceeded/setOnFailed
 * run on the JavaFX Application Thread automatically — so the Controller's
 * callback can safely touch an ObservableList or a TableView directly,
 * with no manual Platform.runLater() needed at the call site.
 *
 *   Controller.loadTasks()
 *        │  calls
 *        ▼
 *   TaskService.loadAllTasksAsync(onLoaded)
 *        │  wraps DAO.findAll() in a javafx.concurrent.Task
 *        │  submits it to AppExecutor.pool()          [background thread]
 *        ▼
 *   TaskDAOImpl.findAll() -- runs the SQL query        [background thread]
 *        │  result flows back through Task.succeeded()
 *        ▼
 *   onLoaded.accept(list)                              [JavaFX thread again]
 */
public class TaskService {

    private final TaskDAO taskDAO = new TaskDAOImpl();

    /** Loads every task in the background, then hands the list back on the FX thread. */
    public void loadAllTasksAsync(Consumer<ObservableList<Task>> onLoaded) {
        javafx.concurrent.Task<List<Task>> job = new javafx.concurrent.Task<>() {
            @Override
            protected List<Task> call() {
                return taskDAO.findAll();
            }
        };
        job.setOnSucceeded(e -> onLoaded.accept(FXCollections.observableArrayList(job.getValue())));
        job.setOnFailed(e -> job.getException().printStackTrace());
        AppExecutor.pool().execute(job);
    }

    /** Loads only incomplete tasks — used by the Priority Engine and dashboard widgets. */
    public void loadIncompleteTasksAsync(Consumer<List<Task>> onLoaded) {
        javafx.concurrent.Task<List<Task>> job = new javafx.concurrent.Task<>() {
            @Override
            protected List<Task> call() {
                return taskDAO.findIncomplete();
            }
        };
        job.setOnSucceeded(e -> onLoaded.accept(job.getValue()));
        job.setOnFailed(e -> job.getException().printStackTrace());
        AppExecutor.pool().execute(job);
    }

    public void createTaskAsync(Task task, Consumer<Task> onSaved) {
        javafx.concurrent.Task<Task> job = new javafx.concurrent.Task<>() {
            @Override
            protected Task call() {
                return taskDAO.save(task);
            }
        };
        job.setOnSucceeded(e -> onSaved.accept(job.getValue()));
        job.setOnFailed(e -> job.getException().printStackTrace());
        AppExecutor.pool().execute(job);
    }

    public void markCompleteAsync(Task task, Runnable onDone) {
        task.setStatus("DONE");
        task.setProgress(1.0);
        javafx.concurrent.Task<Void> job = new javafx.concurrent.Task<>() {
            @Override
            protected Void call() {
                taskDAO.update(task);
                return null;
            }
        };
        job.setOnSucceeded(e -> onDone.run());
        job.setOnFailed(e -> job.getException().printStackTrace());
        AppExecutor.pool().execute(job);
    }

    public void deleteTaskAsync(int taskId, Runnable onDeleted) {
        javafx.concurrent.Task<Void> job = new javafx.concurrent.Task<>() {
            @Override
            protected Void call() {
                taskDAO.delete(taskId);
                return null;
            }
        };
        job.setOnSucceeded(e -> onDeleted.run());
        job.setOnFailed(e -> job.getException().printStackTrace());
        AppExecutor.pool().execute(job);
    }
}
