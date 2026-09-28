package com.momenta.controller;

import com.momenta.dao.FocusSessionDAO;
import com.momenta.dao.impl.FocusSessionDAOImpl;
import com.momenta.model.FocusSession;
import com.momenta.model.Task;
import com.momenta.service.FocusSessionService;
import com.momenta.service.TaskService;
import com.momenta.threading.TaskExecutor;
import com.momenta.utility.AlertUtil;
import com.momenta.utility.CurrentUser;
import com.momenta.utility.SceneManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class FocusController {

    @FXML private BorderPane root;
    @FXML private Label timerLabel;
    @FXML private Label statusLabel;
    @FXML private Label sessionTypeLabel;
    @FXML private Label historySummaryLabel;
    @FXML private StackPane timerCircle;

    @FXML private ComboBox<Task> taskComboBox;
    @FXML private Spinner<Integer> durationSpinner;

    @FXML private Button startButton;
    @FXML private Button pauseButton;
    @FXML private Button resumeButton;
    @FXML private Button stopButton;

    private final TaskService taskService = new TaskService();
    private final FocusSessionService focusSessionService = new FocusSessionService();
    private final FocusSessionDAO focusSessionDAO = new FocusSessionDAOImpl();

    private Timeline timer;
    private int remainingSeconds = 25 * 60;
    private int selectedDurationMinutes = 25;
    private boolean isRunning = false;
    private LocalDateTime sessionStartTime;

    @FXML
    public void initialize() {
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 120, 25, 5);
        durationSpinner.setValueFactory(valueFactory);

        // Spinner dark style
        durationSpinner.getEditor().setStyle("-fx-background-color: #1c1836; -fx-text-fill: #ffffff; -fx-alignment: CENTER; -fx-font-weight: bold;");

        durationSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!isRunning && newVal != null) {
                selectedDurationMinutes = newVal;
                remainingSeconds = newVal * 60;
                updateTimerDisplay();
            }
        });

        // ComboBox dropdown dark styling
        taskComboBox.setButtonCell(createTaskCell());
        taskComboBox.setCellFactory(listView -> createTaskCell());

        updateControlsState(false, false);
        loadTasks();
        loadHistory();
        updateTimerDisplay();
    }

    private ListCell<Task> createTaskCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Task task, boolean empty) {
                super.updateItem(task, empty);
                if (empty || task == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: #1c1836; -fx-text-fill: #8f88ab;");
                } else {
                    setText(task.getTitle());
                    setTextFill(Color.WHITE);
                    setFont(Font.font("Segoe UI", 12.5));
                    setStyle("-fx-background-color: #1c1836; -fx-text-fill: white; -fx-padding: 6 10;");
                }
            }
        };
    }

    private void loadTasks() {
        final int uid = CurrentUser.getId();
        javafx.concurrent.Task<List<Task>> loadTask = new javafx.concurrent.Task<>() {
            @Override
            protected List<Task> call() {
                return taskService.getIncompleteTasks(uid);
            }
        };
        loadTask.setOnSucceeded(e -> {
            taskComboBox.getItems().setAll(loadTask.getValue());
            if (!taskComboBox.getItems().isEmpty()) {
                taskComboBox.getSelectionModel().selectFirst();
            }
        });
        TaskExecutor.getInstance().workPool().submit(loadTask);
    }

    private void loadHistory() {
        final int uid = CurrentUser.getId();
        javafx.concurrent.Task<List<FocusSession>> loadTask = new javafx.concurrent.Task<>() {
            @Override
            protected List<FocusSession> call() {
                return focusSessionService.getHistory(uid);
            }
        };
        loadTask.setOnSucceeded(e -> {
            List<FocusSession> sessions = loadTask.getValue();
            long totalMinutes = sessions.stream()
                    .filter(s -> s.getStartedAt() != null && s.getStartedAt().startsWith(LocalDate.now().toString()))
                    .mapToLong(FocusSession::getDurationMinutes)
                    .sum();
            historySummaryLabel.setText("You have logged " + totalMinutes + " minutes of deep work across "
                    + sessions.size() + " session(s) today.");
        });
        TaskExecutor.getInstance().workPool().submit(loadTask);
    }

    @FXML
    private void onStart() {
        if (isRunning) return;
        selectedDurationMinutes = durationSpinner.getValue();
        remainingSeconds = selectedDurationMinutes * 60;
        sessionStartTime = LocalDateTime.now();

        startTimer();
        isRunning = true;
        updateControlsState(true, false);
        statusLabel.setText("Focusing on: " + getSelectedTaskTitle());
    }

    @FXML
    private void onPause() {
        if (!isRunning || timer == null) return;
        timer.pause();
        statusLabel.setText("Session Paused");
        updateControlsState(true, true);
    }

    @FXML
    private void onResume() {
        if (timer == null) return;
        timer.play();
        statusLabel.setText("Focusing on: " + getSelectedTaskTitle());
        updateControlsState(true, false);
    }

    @FXML
    private void onStop() {
        if (timer != null) timer.stop();
        isRunning = false;

        saveSessionIfEligible();
        remainingSeconds = durationSpinner.getValue() * 60;
        updateTimerDisplay();
        statusLabel.setText("Ready");
        updateControlsState(false, false);
        loadHistory();
    }

    private void startTimer() {
        if (timer != null) timer.stop();
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            remainingSeconds--;
            updateTimerDisplay();
            if (remainingSeconds <= 0) {
                timer.stop();
                isRunning = false;
                onStop();
                AlertUtil.showInfo("Focus Complete", "Great job! Your focus session has concluded.");
            }
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    private void saveSessionIfEligible() {
        if (sessionStartTime == null) return;
        int elapsedMinutes = Math.max(1, selectedDurationMinutes - (remainingSeconds / 60));
        Task selected = taskComboBox.getValue();
        Integer taskId = selected != null ? selected.getId() : null;

        // FocusSession(userId, taskId, durationMinutes, startedAt, endedAt)
        FocusSession session = new FocusSession(
                CurrentUser.getId(),
                taskId,
                elapsedMinutes,
                sessionStartTime.toString(),
                LocalDateTime.now().toString()
        );

        // FocusSessionDAO এর save(session) মেথড কল করা হয়েছে
        TaskExecutor.getInstance().workPool().submit(() -> focusSessionDAO.save(session));
    }

    private void updateTimerDisplay() {
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }

    private void updateControlsState(boolean running, boolean paused) {
        startButton.setDisable(running);
        pauseButton.setDisable(!running || paused);
        resumeButton.setDisable(!running || !paused);
        stopButton.setDisable(!running);
        durationSpinner.setDisable(running);
        taskComboBox.setDisable(running);
    }

    private String getSelectedTaskTitle() {
        Task t = taskComboBox.getValue();
        return t != null ? t.getTitle() : "Deep Work";
    }

    @FXML
    private void onBackToDashboard() {
        if (isRunning && !AlertUtil.confirm("Exit Focus Mode", "A focus session is currently running. Leave anyway?")) {
            return;
        }
        if (timer != null) timer.stop();
        SceneManager.getInstance().switchTo("Dashboard");
    }
}