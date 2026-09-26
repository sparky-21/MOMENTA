package com.momenta.controller;

import javafx.concurrent.Task;
import com.momenta.model.Notification;
import com.momenta.network.InsightService;
import com.momenta.network.Quote;
import com.momenta.service.GoalService;
import com.momenta.service.NotificationService;
import com.momenta.service.ProjectService;
import com.momenta.service.TaskService;
import com.momenta.threading.TaskExecutor;
import com.momenta.utility.AlertUtil;
import com.momenta.utility.CurrentUser;
import com.momenta.utility.SceneManager;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/** Dashboard controller refined in Phase 15; notification integration added in Phase 14. */
public class DashboardController {
    @FXML private BorderPane root;
    @FXML private VBox sidebar;
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
    @FXML private Label nowProgressPercentLabel;
    @FXML private Label notificationCountLabel;
    @FXML private ListView<String> notificationList;
    @FXML private VBox pulseCard;
    @FXML private VBox taskCard;
    @FXML private VBox goalCard;
    @FXML private VBox projectCard;
    @FXML private Label insightLabel;
    @FXML private Label insightAuthorLabel;

    private final TaskService taskService = new TaskService();
    private final GoalService goalService = new GoalService();
    private final ProjectService projectService = new ProjectService();
    private final NotificationService notificationService = new NotificationService();
    private final InsightService insightService = new InsightService();

    @FXML
    public void initialize() {
        greetingLabel.setText(greetingForNow());
        applyVisualTheme();
        loadDashboardData();
        loadNotifications();
        loadDailyInsight();

        // Explicit JavaFX property binding demonstrates responsive sizing without CSS.
        Platform.runLater(() -> {
            if (root.getScene() != null) {
                root.minWidthProperty().bind(root.getScene().widthProperty().multiply(0.65));
            }
        });
    }

    private void applyVisualTheme() {
        Color seaGreen = Color.web("#287F78");
        Color mint = Color.web("#B8F2E6");
        Color sky = Color.web("#F4FAFC");
        Color skyBlue = Color.web("#5DADE2");
        Color lavender = Color.web("#9B8AFB");
        Color softGreen = Color.web("#6BCB9A");
        Color charcoal = Color.web("#263238");

        root.setBackground(new Background(new BackgroundFill(sky, CornerRadii.EMPTY, Insets.EMPTY)));
        sidebar.setBackground(new Background(new BackgroundFill(seaGreen, CornerRadii.EMPTY, Insets.EMPTY)));
        sidebar.setPadding(new Insets(20, 14, 20, 14));

        styleCard(pulseCard, lavender, charcoal);
        styleCard(taskCard, skyBlue, charcoal);
        styleCard(goalCard, softGreen, charcoal);
        styleCard(projectCard, mint, charcoal);

        notificationList.setPrefHeight(120);
        notificationList.setPlaceholder(new Label("No notifications yet."));
    }

    private void styleCard(VBox card, Color accent, Color textColor) {
        if (card == null) return;
        card.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(14), Insets.EMPTY)));
        card.setBorder(new Border(new BorderStroke(
                accent, BorderStrokeStyle.SOLID, new CornerRadii(14), new BorderWidths(2))));
        card.setPadding(new Insets(14));
        card.setSpacing(8);
        for (Node node : card.getChildren()) {
            if (node instanceof Label label) label.setTextFill(textColor);
        }
    }

    private void loadDashboardData() {
        final int uid = CurrentUser.getId();
        Task<DashboardData> loadTask = new Task<>() {
            @Override
            protected DashboardData call() {
                List<com.momenta.model.Task> incomplete = taskService.getIncompleteTasks(uid);
                com.momenta.model.Task recommended = taskService.getMomentaNowRecommendation(uid);
                long activeGoals = goalService.getAllGoals(uid).stream()
                        .filter(g -> "ACTIVE".equals(g.getStatus())).count();
                long activeProjects = projectService.getAllProjects(uid).stream()
                        .filter(p -> "ACTIVE".equals(p.getStatus())).count();
                return new DashboardData(incomplete.size(), recommended, (int) activeGoals, (int) activeProjects);
            }
        };
        loadTask.setOnSucceeded(e -> applyDashboardData(loadTask.getValue()));
        loadTask.setOnFailed(e -> nowTitleLabel.setText("Could not load dashboard data."));
        TaskExecutor.getInstance().workPool().submit(loadTask);
    }

    private void applyDashboardData(DashboardData data) {
        taskCountLabel.setText(String.valueOf(data.incompleteCount()));
        goalCountLabel.setText(String.valueOf(data.activeGoals()));
        projectCountLabel.setText(String.valueOf(data.activeProjects()));

        com.momenta.model.Task rec = data.recommendation();
        if (rec == null) {
            nowTitleLabel.setText("Nothing pending — add a task to get started.");
            nowPriorityLabel.setText("");
            nowDeadlineLabel.setText("");
            nowReasonsLabel.setText("");
            nowProgressBar.setProgress(0);
            nowProgressPercentLabel.setText("");
            pulseLabel.setText("—");
            return;
        }

        nowTitleLabel.setText(rec.getTitle());
        nowPriorityLabel.setText("Priority: " + rec.getPriorityScore() + " / 100");
        nowDeadlineLabel.setText(rec.getDeadline() == null || rec.getDeadline().isBlank()
                ? "No deadline set" : "Deadline: " + rec.getDeadline());
        List<String> reasons = com.momenta.engine.PriorityEngine.score(rec).getReasons();
        nowReasonsLabel.setText("• " + String.join("\n• ", reasons.isEmpty() ? List.of("Standard priority") : reasons));
        nowProgressBar.setProgress(rec.getProgress() / 100.0);
        nowProgressPercentLabel.setText(rec.getProgress() + "%");
        pulseLabel.setText(String.valueOf(rec.getPriorityScore()));
        fadeIn(nowTitleLabel);
    }

    private void loadNotifications() {
        final int uid = CurrentUser.getId();
        Task<NotificationData> task = new Task<>() {
            @Override
            protected NotificationData call() {
                List<Notification> unread = notificationService.getUnread(uid);
                int count = notificationService.getUnreadCount(uid);
                List<String> messages = unread.stream()
                        .limit(6)
                        .map(n -> "[" + n.getType() + "] " + n.getMessage())
                        .collect(Collectors.toList());
                return new NotificationData(count, messages);
            }
        };
        task.setOnSucceeded(e -> {
            NotificationData data = task.getValue();
            notificationCountLabel.setText(String.valueOf(data.unreadCount()));
            notificationList.getItems().setAll(data.messages());
        });
        task.setOnFailed(e -> notificationCountLabel.setText("—"));
        TaskExecutor.getInstance().workPool().submit(task);
    }

    private void loadDailyInsight() {
        Task<Quote> networkTask = new Task<>() {
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

    @FXML private void onOpenTasks() { SceneManager.getInstance().invalidate("Tasks"); SceneManager.getInstance().switchTo("Tasks"); }
    @FXML private void onOpenGoals() { SceneManager.getInstance().invalidate("Goals"); SceneManager.getInstance().switchTo("Goals"); }
    @FXML private void onOpenProjects() { SceneManager.getInstance().invalidate("Projects"); SceneManager.getInstance().switchTo("Projects"); }
    @FXML private void onOpenCalendar() { SceneManager.getInstance().invalidate("Calendar"); SceneManager.getInstance().switchTo("Calendar"); }
    @FXML private void onOpenHabits() { SceneManager.getInstance().invalidate("Habits"); SceneManager.getInstance().switchTo("Habits"); }
    @FXML private void onOpenFinance() { SceneManager.getInstance().invalidate("Finance"); SceneManager.getInstance().switchTo("Finance"); }
    @FXML private void onOpenFocus() { SceneManager.getInstance().invalidate("Focus"); SceneManager.getInstance().switchTo("Focus"); }

    @FXML private void onRefresh() { loadDashboardData(); loadNotifications(); loadDailyInsight(); }

    @FXML
    private void onRefreshInsight() {
        loadDailyInsight();
    }

    @FXML
    private void onOpenNotifications() {
        List<Notification> unread = notificationService.getUnread(CurrentUser.getId());
        String message = unread.isEmpty()
                ? "You have no unread notifications."
                : unread.stream().map(Notification::getMessage).collect(Collectors.joining("\n\n"));
        AlertUtil.showInfo("Notifications", message);
    }

    @FXML
    private void onMarkAllNotificationsRead() {
        final int uid = CurrentUser.getId();
        Task<Void> task = new Task<>() {
            @Override protected Void call() {
                notificationService.markAllAsRead(uid);
                return null;
            }
        };
        task.setOnSucceeded(e -> loadNotifications());
        task.setOnFailed(e -> AlertUtil.showError("Notifications", "Could not update notifications.", task.getException()));
        TaskExecutor.getInstance().workPool().submit(task);
    }

    private String greetingForNow() {
        int hour = LocalTime.now().getHour();
        if (hour < 12) return "Good morning";
        if (hour < 17) return "Good afternoon";
        return "Good evening";
    }

    private void fadeIn(Node node) {
        FadeTransition fade = new FadeTransition(Duration.millis(300), node);
        fade.setFromValue(0.3);
        fade.setToValue(1);
        fade.play();
    }

    private record DashboardData(int incompleteCount, com.momenta.model.Task recommendation, int activeGoals, int activeProjects) {}
    private record NotificationData(int unreadCount, List<String> messages) {}
}