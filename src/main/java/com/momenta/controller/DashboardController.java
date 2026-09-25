package com.momenta.controller;

import com.momenta.engine.MomentaCore;
import com.momenta.model.Task;
import com.momenta.network.InsightService;
import com.momenta.network.Quote;
import com.momenta.service.GoalService;
import com.momenta.service.ProjectService;
import com.momenta.service.TaskService;
import com.momenta.threading.TaskExecutor;
import com.momenta.utility.SceneManager;
import com.momenta.utility.CurrentUser;

import java.util.List;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.util.Duration;


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
    @FXML private Label insightLabel;
    @FXML private Label insightAuthorLabel;

    private final TaskService taskService = new TaskService();
    private final MomentaCore momentaCore = new MomentaCore();
    private final GoalService goalService = new GoalService();
    private final ProjectService projectService = new ProjectService();
    private final InsightService insightService = new InsightService();

    @FXML
    public void initialize() {
        greetingLabel.setText(greetingForNow());
        loadDashboardData();
        loadDailyInsight();
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
                int incompleteCount =
                        taskService.getIncompleteTasks(CurrentUser.getId()).size();
                MomentaCore.Recommendation recommendation =
                        momentaCore.recommend(CurrentUser.getId());
                long activeGoals = goalService.getAllGoals(CurrentUser.getId()).stream()
                        .filter(g -> "ACTIVE".equals(g.getStatus())).count();
                long activeProjects = projectService.getAllProjects(CurrentUser.getId()).stream()
                        .filter(p -> "ACTIVE".equals(p.getStatus())).count();
                return new DashboardData(incompleteCount, recommendation, (int) activeGoals, (int) activeProjects);
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

        MomentaCore.Recommendation recommendation = data.recommendation();
        if (recommendation == null) {
            nowTitleLabel.setText("Nothing pending — add a task to get started.");
            nowPriorityLabel.setText("");
            nowDeadlineLabel.setText("");
            nowReasonsLabel.setText("");
            nowProgressBar.setProgress(0);
            pulseLabel.setText("—");
            return;
        }

        Task rec = recommendation.task();
        nowTitleLabel.setText(rec.getTitle());
        nowPriorityLabel.setText("Priority: " + recommendation.score() + " / 100");
        nowDeadlineLabel.setText(rec.getDeadline() == null || rec.getDeadline().isBlank()
                ? "No deadline set" : "Deadline: " + rec.getDeadline());

        List<String> reasons = recommendation.reasons().isEmpty()
                ? List.of("Standard priority")
                : recommendation.reasons();
        nowReasonsLabel.setText(String.join("\n• ", reasons));
        nowProgressBar.setProgress(rec.getProgress() / 100.0);

        // MOMENTA Pulse (simplified for Phase 1-4: productivity component only,
        // full multi-factor Pulse arrives with the Analytics phase, Section 17).
        pulseLabel.setText(String.valueOf(recommendation.score()));

        fadeIn(nowTitleLabel);
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
        loadDailyInsight();
    }

    @FXML
    private void onRefreshInsight() {
        loadDailyInsight();
    }

    private void loadDailyInsight() {
        javafx.concurrent.Task<Quote> networkTask = new javafx.concurrent.Task<>() {
            @Override
            protected Quote call() throws Exception {
                return insightService.getDailyInsight();
            }
        };

        networkTask.setOnSucceeded(e -> {
            Quote quote = networkTask.getValue();
            insightLabel.setText("\"" + quote.getQuote() + "\"");
            insightAuthorLabel.setText("— " + quote.getAuthor());
        });

        networkTask.setOnFailed(e -> {
            insightLabel.setText("Daily insight is unavailable right now.");
            insightAuthorLabel.setText("Please check your internet connection.");
        });

        TaskExecutor.getInstance().workPool().submit(networkTask);
    }

    private String greetingForNow() {
        int hour = java.time.LocalTime.now().getHour();
        if (hour < 12) return "Good morning";
        if (hour < 17) return "Good afternoon";
        return "Good evening";
    }
    @FXML
    private void onOpenHabits() {
        SceneManager.getInstance().invalidate("Habits");
        SceneManager.getInstance().switchTo("Habits");
    }

    @FXML
    private void onOpenFinance() {
        SceneManager.getInstance().invalidate("Finance");
        SceneManager.getInstance().switchTo("Finance");
    }

    @FXML
    private void onOpenFocus() {
        SceneManager.getInstance().invalidate("Focus");
        SceneManager.getInstance().switchTo("Focus");
    }


    /** Simple carrier record for the background load result. */
    private record DashboardData(
            int incompleteCount,
            MomentaCore.Recommendation recommendation,
            int activeGoals,
            int activeProjects) {
    }
}