package com.momenta.controller;

import com.momenta.model.FocusSession;
import com.momenta.model.Task;
import com.momenta.service.FocusSessionService;
import com.momenta.service.TaskService;
import com.momenta.threading.TaskExecutor;
import com.momenta.utility.AlertUtil;
import com.momenta.utility.AnimationUtil;
import com.momenta.utility.CurrentUser;
import com.momenta.utility.SceneManager;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Phase 9 — Focus Mode controller. UI work stays on the JavaFX thread. */
public class FocusController {

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
    // Guards against a double finish (e.g. the 1-second Timeline tick
    // reaching zero at the same moment the user clicks Stop) sending two
    // concurrent "finish" background tasks for the same session.
    private boolean finishing;

    // Task titles for the history table used to be looked up with a fresh
    // taskService.getAllTasks(...) database call inside the TableColumn's
    // cellValueFactory — which JavaFX invokes on the FX Application Thread
    // every time a row is rendered (initial load, scroll, resize...). That
    // meant a full DB query per cell-render on the UI thread, violating
    // Rule #14 far more often than a one-off screen load would. Titles are
    // now loaded once in the background alongside the history itself and
    // cached here; the cellValueFactory only ever reads this in-memory map.
    private final Map<Integer, String> taskTitleCache = new HashMap<>();

    @FXML
    public void initialize() {
        durationSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 180, 25));
        historyTaskColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getTaskId() == null
                        ? "General Focus"
                        : taskTitleCache.getOrDefault(cell.getValue().getTaskId(), "Task #" + cell.getValue().getTaskId())));
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
        final int uid = CurrentUser.getId();
        javafx.concurrent.Task<List<Task>> loadTask = new javafx.concurrent.Task<>() {
            @Override
            protected List<Task> call() {
                return taskService.getIncompleteTasks(uid);
            }
        };
        loadTask.setOnSucceeded(e -> taskCombo.getItems().setAll(loadTask.getValue()));
        loadTask.setOnFailed(e -> AlertUtil.showError("Focus Mode",
                "Could not load tasks.", loadTask.getException()));
        TaskExecutor.getInstance().workPool().submit(loadTask);
    }

    private void loadHistory() {
        final int uid = CurrentUser.getId();
        javafx.concurrent.Task<HistoryData> loadTask = new javafx.concurrent.Task<>() {
            @Override
            protected HistoryData call() {
                List<FocusSession> sessions = focusService.getHistory(uid);
                Map<Integer, String> titles = new HashMap<>();
                for (Task t : taskService.getAllTasks(uid)) {
                    titles.put(t.getId(), t.getTitle());
                }
                return new HistoryData(sessions, titles);
            }
        };
        loadTask.setOnSucceeded(e -> {
            HistoryData data = loadTask.getValue();
            taskTitleCache.clear();
            taskTitleCache.putAll(data.titles());
            historyTable.getItems().setAll(data.sessions());
        });
        loadTask.setOnFailed(e -> AlertUtil.showError("Focus Mode",
                "Could not load session history.", loadTask.getException()));
        TaskExecutor.getInstance().workPool().submit(loadTask);
    }

    @FXML
    private void onStart() {
        if (running) return;
        final Task selectedTask = taskCombo.getValue();
        final int minutes = durationSpinner.getValue();

        // focusService.start() is an INSERT — moved off the FX thread like
        // every other write in this pass. The controls are disabled
        // immediately so a second click can't fire a second session while
        // this one is still being saved.
        durationSpinner.setDisable(true);
        taskCombo.setDisable(true);
        statusLabel.setText("Starting…");

        javafx.concurrent.Task<FocusSession> startTask = new javafx.concurrent.Task<>() {
            @Override
            protected FocusSession call() {
                return focusService.start(CurrentUser.getId(), selectedTask, minutes);
            }
        };

        startTask.setOnSucceeded(e -> {
            activeTask = selectedTask;
            activeSession = startTask.getValue();
            remainingSeconds = minutes * 60;
            running = true;
            finishing = false;
            statusLabel.setText("Focus session running");

            timeline = new Timeline(new KeyFrame(Duration.seconds(1), ev -> tick()));
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();
        });

        startTask.setOnFailed(e -> {
            AlertUtil.showError("Focus Mode", "Could not start the focus session.", startTask.getException());
            durationSpinner.setDisable(false);
            taskCombo.setDisable(false);
            statusLabel.setText("");
        });

        TaskExecutor.getInstance().workPool().submit(startTask);
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
        if (finishing || activeSession == null) return;
        finishing = true;

        if (timeline != null) timeline.stop();
        running = false;

        final FocusSession sessionToFinish = activeSession;
        final Task taskToFinish = activeTask;

        javafx.concurrent.Task<Void> finishTask = new javafx.concurrent.Task<>() {
            @Override
            protected Void call() {
                focusService.finish(sessionToFinish, taskToFinish);
                return null;
            }
        };

        finishTask.setOnSucceeded(e -> {
            statusLabel.setText("Focus session completed");
            AnimationUtil.success(statusLabel);
            loadTasks();
            loadHistory();
            resetSessionState();
        });

        finishTask.setOnFailed(e -> {
            AlertUtil.showError("Focus Mode", "Could not save the completed session.", finishTask.getException());
            resetSessionState();
        });

        TaskExecutor.getInstance().workPool().submit(finishTask);
    }

    private void resetSessionState() {
        activeSession = null;
        activeTask = null;
        running = false;
        finishing = false;
        taskCombo.setDisable(false);
        durationSpinner.setDisable(false);
        showTime(durationSpinner.getValue() * 60);
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

    private record HistoryData(List<FocusSession> sessions, Map<Integer, String> titles) {}
}
