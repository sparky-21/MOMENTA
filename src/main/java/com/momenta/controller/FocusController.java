package com.momenta.controller;

import com.momenta.model.FocusSession;
import com.momenta.model.Task;
import com.momenta.service.FocusSessionService;
import com.momenta.service.TaskService;
import com.momenta.utility.AlertUtil;
import com.momenta.utility.SceneManager;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;

import java.util.List;

/** Phase 9 — Focus Mode controller. UI work stays on the JavaFX thread. */
public class FocusController {
    private static final int USER_ID = 1;

    @FXML private ComboBox<Task> taskCombo;
    @FXML private Spinner<Integer> durationSpinner;
    @FXML private Label timerLabel;
    @FXML private Label statusLabel;
    @FXML private TableView<FocusSession> historyTable;
    @FXML private TableColumn<FocusSession, String> historyTaskColumn;
    @FXML private TableColumn<FocusSession, Number> historyDurationColumn;
    @FXML private TableColumn<FocusSession, String> historyStartedColumn;

    private final TaskService taskService = new TaskService();
    private final FocusSessionService focusService = new FocusSessionService();
    private Timeline timeline;
    private FocusSession activeSession;
    private Task activeTask;
    private int remainingSeconds;
    private boolean running;

    @FXML
    public void initialize() {
        durationSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 180, 25));
        historyTaskColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getTaskId() == null ? "General Focus" : findTaskTitle(cell.getValue().getTaskId())));
        historyDurationColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getDurationMinutes()));
        historyStartedColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getStartedAt()));

        // Task combo shows the task title, not Task@<hash> — Task has no
        // toString() override (unlike Goal/Event), so a cell factory is used
        // here instead of touching the shared model class.
        javafx.util.Callback<javafx.scene.control.ListView<Task>, javafx.scene.control.ListCell<Task>> taskCellFactory =
                list -> new javafx.scene.control.ListCell<>() {
                    @Override
                    protected void updateItem(Task task, boolean empty) {
                        super.updateItem(task, empty);
                        setText(empty || task == null ? null : task.getTitle());
                    }
                };
        taskCombo.setCellFactory(taskCellFactory);
        taskCombo.setButtonCell(taskCellFactory.call(null));

        // Section 21 / Rule #14: never block the JavaFX Application Thread
        // with a database read — both loads run on TaskExecutor's pool and
        // only touch the UI in setOnSucceeded.
        loadTasks();
        loadHistory();
        showTime(durationSpinner.getValue() * 60);
    }

    private void loadTasks() {
        javafx.concurrent.Task<List<Task>> loadTask = new javafx.concurrent.Task<>() {
            @Override
            protected List<Task> call() {
                return taskService.getIncompleteTasks(USER_ID);
            }
        };
        loadTask.setOnSucceeded(e -> taskCombo.getItems().setAll(loadTask.getValue()));
        loadTask.setOnFailed(e -> AlertUtil.showError("Focus Mode",
                "Could not load tasks.", loadTask.getException()));
        com.momenta.threading.TaskExecutor.getInstance().workPool().submit(loadTask);
    }

    private void loadHistory() {
        javafx.concurrent.Task<List<FocusSession>> loadTask = new javafx.concurrent.Task<>() {
            @Override
            protected List<FocusSession> call() {
                return focusService.getHistory(USER_ID);
            }
        };
        loadTask.setOnSucceeded(e -> historyTable.getItems().setAll(loadTask.getValue()));
        loadTask.setOnFailed(e -> AlertUtil.showError("Focus Mode",
                "Could not load session history.", loadTask.getException()));
        com.momenta.threading.TaskExecutor.getInstance().workPool().submit(loadTask);
    }

    private String findTaskTitle(int taskId) {
        return taskService.getAllTasks(USER_ID).stream()
                .filter(t -> t.getId() == taskId)
                .map(Task::getTitle)
                .findFirst().orElse("Task #" + taskId);
    }

    @FXML
    private void onStart() {
        if (running) return;
        activeTask = taskCombo.getValue();
        int minutes = durationSpinner.getValue();
        try {
            activeSession = focusService.start(USER_ID, activeTask, minutes);
            remainingSeconds = minutes * 60;
            running = true;
            statusLabel.setText("Focus session running");
            durationSpinner.setDisable(true);
            taskCombo.setDisable(true);

            timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> tick()));
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();
        } catch (RuntimeException ex) {
            AlertUtil.showError("Focus Mode", "Could not start the focus session.", ex);
        }
    }

    private void tick() {
        remainingSeconds--;
        showTime(remainingSeconds);
        if (remainingSeconds <= 0) finishSession();
    }

    @FXML
    private void onPause() {
        if (timeline != null && running) {
            timeline.pause();
            running = false;
            statusLabel.setText("Paused");
        }
    }

    @FXML
    private void onResume() {
        if (timeline != null && !running && activeSession != null && remainingSeconds > 0) {
            timeline.play();
            running = true;
            statusLabel.setText("Focus session running");
        }
    }

    @FXML
    private void onStop() {
        if (activeSession == null) return;
        finishSession();
    }

    private void finishSession() {
        if (timeline != null) timeline.stop();
        try {
            focusService.finish(activeSession, activeTask);
            statusLabel.setText("Focus session completed");
            loadTasks();
            loadHistory();
        } catch (RuntimeException ex) {
            AlertUtil.showError("Focus Mode", "Could not save the completed session.", ex);
        } finally {
            activeSession = null;
            activeTask = null;
            running = false;
            taskCombo.setDisable(false);
            durationSpinner.setDisable(false);
            showTime(durationSpinner.getValue() * 60);
        }
    }

    private void showTime(int seconds) {
        int min = Math.max(0, seconds) / 60;
        int sec = Math.max(0, seconds) % 60;
        timerLabel.setText(String.format("%02d:%02d", min, sec));
    }

    @FXML
    private void onBackToDashboard() {
        if (timeline != null) timeline.stop();
        SceneManager.getInstance().invalidate("Dashboard");
        SceneManager.getInstance().switchTo("Dashboard");
    }
}