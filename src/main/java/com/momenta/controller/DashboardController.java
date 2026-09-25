package com.momenta.controller;

import com.momenta.model.Task;
import com.momenta.service.GoalService;
import com.momenta.service.ProjectService;
import com.momenta.service.TaskService;
import com.momenta.threading.TaskExecutor;
import com.momenta.utility.SceneManager;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.util.Duration;

import java.util.List;

/**
 * Controller for Dashboard.fxml (Section 5 + Section 16 MOMENTA NOW).
 *
 * Threading pattern demonstrated here (Section 21 diagram):
 *
 *   JavaFX Application Thread
 *           |  (user opens Dashboard)
 *           v
 *   javafx.concurrent.Task<List<Task>>   <-- built below
 *           |
 *           v
 *   TaskExecutor.workPool().submit(task)  <-- runs on a background thread
 *           |  (task.run() calls TaskService -> TaskDAO -> JDBC -> SQLite)
 *           v
 *   task.setOnSucceeded(...)  <-- automatically re-enters the JavaFX
 *           |                     Application Thread
 *           v
 *   UI updated (labels, MOMENTA NOW card) safely
 *
 * The controller NEVER touches a JavaFX Label/ProgressBar from inside the
 * Task's call() method — only inside setOnSucceeded — which is what keeps
 * this safe (Software Engineering Rule #4).
 */
public class DashboardController {

    @FXML private Label greetingLabel;
    @FXML private Label taskCountLabel;
    @FXML private Label goalCountLabel;
    @FXML private Label projectCountLabel;
    @FXML private Label pulseLabel;

    @FXML private Label nowTitleLabel;
    @FXML private Label nowPriorityLabel;
    @FXML private Label nowDeadlineLabel;
    @FXML private Label nowReasonsLabel;
    @FXML private ProgressBar nowProgressBar;

    private final TaskService taskService = new TaskService();
    private final GoalService goalService = new GoalService();
    private final ProjectService projectService = new ProjectService();
    private static final int CURRENT_USER_ID = 1; // single-user for now (Phase 1-4)

    @FXML
    public void initialize() {
        greetingLabel.setText(greetingForNow());
        loadDashboardData();
    }

    /**
     * Builds a background javafx.concurrent.Task that fetches the incomplete
     * task list and computes the MOMENTA NOW recommendation off the UI
     * thread, then applies the result on the JavaFX Application Thread.
     */
    private void loadDashboardData() {
        javafx.concurrent.Task<DashboardData> loadTask = new javafx.concurrent.Task<>() {
            @Override
            protected DashboardData call() {
                List<Task> incomplete = taskService.getIncompleteTasks(CURRENT_USER_ID);
                Task recommended = taskService.getMomentaNowRecommendation(CURRENT_USER_ID);
                long activeGoals = goalService.getAllGoals(CURRENT_USER_ID).stream()
                        .filter(g -> "ACTIVE".equals(g.getStatus())).count();
                long activeProjects = projectService.getAllProjects(CURRENT_USER_ID).stream()
                        .filter(p -> "ACTIVE".equals(p.getStatus())).count();
                return new DashboardData(incomplete.size(), recommended, (int) activeGoals, (int) activeProjects);
            }
        };

        loadTask.setOnSucceeded(e -> applyDashboardData(loadTask.getValue()));
        loadTask.setOnFailed(e ->
                nowTitleLabel.setText("Could not load dashboard data."));

        TaskExecutor.getInstance().workPool().submit(loadTask);
    }

    private void applyDashboardData(DashboardData data) {
        taskCountLabel.setText(String.valueOf(data.incompleteCount()));
        goalCountLabel.setText(String.valueOf(data.activeGoals()));
        projectCountLabel.setText(String.valueOf(data.activeProjects()));

        Task rec = data.recommendation();
        if (rec == null) {
            nowTitleLabel.setText("Nothing pending — add a task to get started.");
            nowPriorityLabel.setText("");
            nowDeadlineLabel.setText("");
            nowReasonsLabel.setText("");
            nowProgressBar.setProgress(0);
            pulseLabel.setText("—");
            return;
        }

        nowTitleLabel.setText(rec.getTitle());
        nowPriorityLabel.setText("Priority: " + rec.getPriorityScore() + " / 100");
        nowDeadlineLabel.setText(rec.getDeadline() == null || rec.getDeadline().isBlank()
                ? "No deadline set" : "Deadline: " + rec.getDeadline());
        nowReasonsLabel.setText(String.join("\n• ", buildReasonLines(rec)));
        nowProgressBar.setProgress(rec.getProgress() / 100.0);

        // MOMENTA Pulse (simplified for Phase 1-4: productivity component only,
        // full multi-factor Pulse arrives with the Analytics phase, Section 17).
        pulseLabel.setText(String.valueOf(rec.getPriorityScore()));

        fadeIn(nowTitleLabel);
    }

    private List<String> buildReasonLines(Task rec) {
        var result = com.momenta.engine.PriorityEngine.score(rec);
        return result.getReasons().isEmpty()
                ? List.of("Standard priority")
                : result.getReasons();
    }

    private void fadeIn(javafx.scene.Node node) {
        FadeTransition fade = new FadeTransition(Duration.millis(300), node);
        fade.setFromValue(0.3);
        fade.setToValue(1);
        fade.play();
    }

    @FXML
    private void onOpenTasks() {
        SceneManager.getInstance().invalidate("Tasks");
        SceneManager.getInstance().switchTo("Tasks");
    }

    @FXML
    private void onOpenGoals() {
        SceneManager.getInstance().invalidate("Goals");
        SceneManager.getInstance().switchTo("Goals");
    }

    @FXML
    private void onOpenProjects() {
        SceneManager.getInstance().invalidate("Projects");
        SceneManager.getInstance().switchTo("Projects");
    }

    @FXML
    private void onOpenCalendar() {
        SceneManager.getInstance().invalidate("Calendar");
        SceneManager.getInstance().switchTo("Calendar");
    }

    @FXML
    private void onRefresh() {
        loadDashboardData();
    }

    private String greetingForNow() {
        int hour = java.time.LocalTime.now().getHour();
        if (hour < 12) return "Good morning";
        if (hour < 17) return "Good afternoon";
        return "Good evening";
    }

    /** Simple carrier record for the background load result. */
    private record DashboardData(int incompleteCount, Task recommendation,
                                  int activeGoals, int activeProjects) {
    }
}
