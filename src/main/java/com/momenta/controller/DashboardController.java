package com.momenta.controller;

import com.momenta.engine.MomentaCore;
import com.momenta.model.FocusSession;
import com.momenta.model.GamificationStats;
import com.momenta.model.Notification;
import com.momenta.model.Task;
import com.momenta.network.InsightService;
import com.momenta.network.Quote;
import com.momenta.service.FocusSessionService;
import com.momenta.service.GamificationService;
import com.momenta.service.GoalService;
import com.momenta.service.NotificationService;
import com.momenta.service.ProjectService;
import com.momenta.service.TaskService;
import com.momenta.threading.TaskExecutor;
import com.momenta.utility.AlertUtil;
import com.momenta.utility.AnimationUtil;
import com.momenta.utility.CurrentUser;
import com.momenta.utility.MomentaTheme;
import com.momenta.utility.SceneManager;
import com.momenta.utility.SessionStore;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class DashboardController {
    @FXML private BorderPane root;
    @FXML private VBox sidebar;
    @FXML private Label greetingLabel;
    @FXML private Label selectedDateLabel;
    @FXML private Label overviewDateLabel;
    @FXML private Label dateProgressTitleLabel;
    @FXML private HBox dateNavigator;

    @FXML private StackPane progressRing;
    @FXML private Circle progressTrack;
    @FXML private Arc progressArc;
    @FXML private Label todayProgressPercentLabel;
    @FXML private Label todayProgressMetaLabel;
    @FXML private Label todayProgressHintLabel;

    @FXML private Label taskCountLabel;
    @FXML private Label goalCountLabel;
    @FXML private Label projectCountLabel;
    @FXML private Label focusMinutesLabel;
    @FXML private ProgressBar taskProgressBar;

    @FXML private Label nowTitleLabel;
    @FXML private Label nowPriorityLabel;
    @FXML private Label nowPriorityBadge;
    @FXML private Label nowDeadlineLabel;
    @FXML private Label nowCategoryLabel;
    @FXML private Label nowReasonsLabel;
    @FXML private ProgressBar nowProgressBar;
    @FXML private Label nowProgressPercentLabel;

    @FXML private Label notificationCountLabel;
    @FXML private ListView<String> notificationList;
    @FXML private VBox taskCard;
    @FXML private VBox goalCard;
    @FXML private VBox projectCard;
    @FXML private VBox focusCard;
    @FXML private VBox nowCard;
    @FXML private VBox todayProgressCard;
    @FXML private VBox todayTasksCard;
    @FXML private VBox upcomingCard;
    @FXML private VBox todayTaskList;
    @FXML private VBox upcomingList;
    @FXML private Label todayTaskSummaryLabel;
    @FXML private Label insightLabel;
    @FXML private Label insightAuthorLabel;
    @FXML private VBox gamificationCard;
    @FXML private Label levelLabel;
    @FXML private Label xpLabel;
    @FXML private ProgressBar xpProgressBar;
    @FXML private Label streakLabel;
    @FXML private Label achievementLabel;

    private final TaskService taskService = new TaskService();
    private final GoalService goalService = new GoalService();
    private final ProjectService projectService = new ProjectService();
    private final FocusSessionService focusSessionService = new FocusSessionService();
    private final NotificationService notificationService = new NotificationService();
    private final InsightService insightService = new InsightService();
    private final GamificationService gamificationService = new GamificationService();

    private Timeline greetingClock;
    private LocalDate selectedDate = LocalDate.now();

    private static final DateTimeFormatter FULL_DATE =
            DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter SHORT_DATE =
            DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH);

    @FXML
    public void initialize() {
        greetingLabel.setText(greetingForNow());
        notificationList.setPrefHeight(105);
        notificationList.setMinHeight(90);
        notificationList.setPlaceholder(new Label("✦  No notifications yet"));

        setupProgressRing();
        buildDateNavigator();
        updateDateLabels();

        greetingClock = new Timeline(new KeyFrame(Duration.seconds(60), e ->
                greetingLabel.setText(greetingForNow())));
        greetingClock.setCycleCount(Timeline.INDEFINITE);
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                greetingLabel.setText(greetingForNow());
                greetingClock.play();
            } else {
                greetingClock.stop();
            }
        });

        javafx.application.Platform.runLater(() -> AnimationUtil.playSequentialEntry(List.of(
                todayProgressCard, nowCard, taskCard, goalCard, projectCard, focusCard,
                todayTasksCard, upcomingCard, gamificationCard, insightLabel
        )));

        loadDashboardData();
        loadNotifications();
        loadDailyInsight();
        loadGamification();
    }

    private void setupProgressRing() {
        final double size = 198;
        final double radius = 82;

        progressRing.setMinSize(size, size);
        progressRing.setPrefSize(size, size);
        progressRing.setMaxSize(size, size);
        progressRing.setAlignment(Pos.CENTER);

        // Group-এর ভেতরে centerX/Y 0 রাখলে দুটি শেপ একে অপরের সাথে হুবহু কোঅর্ডিনেট শেয়ার করে
        progressTrack.setCenterX(0);
        progressTrack.setCenterY(0);
        progressTrack.setRadius(radius);
        progressTrack.setFill(Color.TRANSPARENT);
        progressTrack.setStroke(MomentaTheme.SURFACE_3);
        progressTrack.setStrokeWidth(15);
        progressTrack.setStrokeType(javafx.scene.shape.StrokeType.CENTERED);

        progressArc.setCenterX(0);
        progressArc.setCenterY(0);
        progressArc.setRadiusX(radius);
        progressArc.setRadiusY(radius);
        progressArc.setFill(Color.TRANSPARENT);
        progressArc.setStroke(MomentaTheme.ROYAL_PURPLE);
        progressArc.setStrokeWidth(15);
        progressArc.setStrokeType(javafx.scene.shape.StrokeType.CENTERED);
        progressArc.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        progressArc.setType(ArcType.OPEN);
        // Start at 12 o'clock (90 degrees). Negative length moves clockwise.
        progressArc.setStartAngle(90);
        progressArc.setLength(0);
    }

    private void buildDateNavigator() {
        dateNavigator.getChildren().clear();

        for (int offset = -3; offset <= 3; offset++) {
            LocalDate date = selectedDate.plusDays(offset);
            VBox card = createDateCard(date);
            dateNavigator.getChildren().add(card);
        }
    }

    private VBox createDateCard(LocalDate date) {
        VBox card = new VBox(2);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(92);
        card.setMinWidth(76);
        card.setPrefHeight(72);
        card.setPadding(new Insets(9, 7, 8, 7));
        card.setCursor(Cursor.HAND);

        Label day = new Label(date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase());
        day.setFont(Font.font("Segoe UI Semibold", 10));
        Label number = new Label(String.valueOf(date.getDayOfMonth()));
        number.setFont(Font.font("Segoe UI Semibold", 21));
        Label marker = new Label(date.equals(LocalDate.now()) ? "TODAY" : "•");
        marker.setFont(Font.font("Segoe UI Semibold", 8));

        card.getChildren().addAll(day, number, marker);
        card.setUserData(date);
        applyDateCardStyle(card, date.equals(selectedDate));

        card.setOnMouseClicked(e -> selectDate(date));
        card.setOnMouseEntered(e -> {
            if (!date.equals(selectedDate)) {
                card.setTranslateY(-2);
                card.setBackground(new Background(new BackgroundFill(
                        MomentaTheme.SURFACE_3, new CornerRadii(14), Insets.EMPTY)));
            }
        });
        card.setOnMouseExited(e -> {
            if (!date.equals(selectedDate)) {
                card.setTranslateY(0);
                applyDateCardStyle(card, false);
            }
        });
        return card;
    }

    private void applyDateCardStyle(VBox card, boolean selected) {
        Color accent = MomentaTheme.ROYAL_PURPLE;
        card.setBackground(new Background(new BackgroundFill(
                selected ? accent.deriveColor(0, 0.78, 0.52, 1) : MomentaTheme.SURFACE,
                new CornerRadii(14), Insets.EMPTY)));
        card.setBorder(new Border(new BorderStroke(
                selected ? accent.deriveColor(0, 0.55, 1, 0.95) : MomentaTheme.BORDER_SOFT,
                BorderStrokeStyle.SOLID, new CornerRadii(14), new BorderWidths(selected ? 1.2 : 1))));
        card.setEffect(selected ? new javafx.scene.effect.DropShadow(14, accent) : null);

        if (card.getChildren().size() >= 3) {
            ((Label) card.getChildren().get(0)).setTextFill(selected ? MomentaTheme.TEXT : MomentaTheme.MUTED);
            ((Label) card.getChildren().get(1)).setTextFill(MomentaTheme.TEXT);
            ((Label) card.getChildren().get(2)).setTextFill(selected ? MomentaTheme.NEON_LILAC : MomentaTheme.MUTED);
        }
    }

    private void selectDate(LocalDate date) {
        selectedDate = date;
        for (Node node : dateNavigator.getChildren()) {
            if (node instanceof VBox card && card.getUserData() instanceof LocalDate d) {
                applyDateCardStyle(card, d.equals(selectedDate));
                card.setTranslateY(0);
            }
        }
        updateDateLabels();
        loadSelectedDateData();
    }

    @FXML
    private void onPreviousDate() {
        selectedDate = selectedDate.minusDays(1);
        buildDateNavigator();
        updateDateLabels();
        loadSelectedDateData();
    }

    @FXML
    private void onNextDate() {
        selectedDate = selectedDate.plusDays(1);
        buildDateNavigator();
        updateDateLabels();
        loadSelectedDateData();
    }

    @FXML
    private void onToday() {
        selectedDate = LocalDate.now();
        buildDateNavigator();
        updateDateLabels();
        loadSelectedDateData();
    }

    private void updateDateLabels() {
        String full = selectedDate.format(FULL_DATE);
        selectedDateLabel.setText(full);
        overviewDateLabel.setText(selectedDate.equals(LocalDate.now()) ? "TODAY" : selectedDate.format(SHORT_DATE));
        dateProgressTitleLabel.setText(selectedDate.equals(LocalDate.now())
                ? "TODAY'S PROGRESS"
                : selectedDate.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase() + "'S PROGRESS");
    }

    private void loadDashboardData() {
        final int uid = CurrentUser.getId();
        javafx.concurrent.Task<DashboardData> loadTask = new javafx.concurrent.Task<>() {
            @Override
            protected DashboardData call() {
                List<Task> incomplete = taskService.getIncompleteTasks(uid);
                MomentaCore.Recommendation recommendation = taskService.getMomentaNowRecommendation(uid);
                long activeGoals = goalService.getAllGoals(uid).stream()
                        .filter(g -> "ACTIVE".equals(g.getStatus())).count();
                long activeProjects = projectService.getAllProjects(uid).stream()
                        .filter(p -> "ACTIVE".equals(p.getStatus())).count();
                List<FocusSession> sessions = focusSessionService.getHistory(uid);
                return new DashboardData(incomplete, recommendation, (int) activeGoals, (int) activeProjects, sessions);
            }
        };
        loadTask.setOnSucceeded(e -> {
            DashboardData data = loadTask.getValue();
            applyDashboardData(data);
            loadSelectedDateData();
        });
        loadTask.setOnFailed(e -> nowTitleLabel.setText("Could not load dashboard data."));
        TaskExecutor.getInstance().workPool().submit(loadTask);
    }

    private void loadSelectedDateData() {
        final int uid = CurrentUser.getId();
        final LocalDate requestedDate = selectedDate;
        javafx.concurrent.Task<DateData> loadTask = new javafx.concurrent.Task<>() {
            @Override
            protected DateData call() {
                List<Task> tasks = taskService.getTasksForDashboardDate(uid, requestedDate.toString());
                List<FocusSession> sessions = focusSessionService.getHistory(uid);
                return new DateData(tasks, sessions);
            }
        };
        loadTask.setOnSucceeded(e -> {
            if (requestedDate.equals(selectedDate)) {
                applySelectedDateData(loadTask.getValue().tasks(), loadTask.getValue().sessions());
            }
        });
        loadTask.setOnFailed(e -> {
            if (requestedDate.equals(selectedDate)) showDateData(List.of(), List.of());
        });
        TaskExecutor.getInstance().workPool().submit(loadTask);
    }

    private void applyDashboardData(DashboardData data) {
        List<Task> incomplete = data.incompleteTasks();
        taskCountLabel.setText(String.valueOf(incomplete.size()));
        goalCountLabel.setText(String.valueOf(data.activeGoals()));
        projectCountLabel.setText(String.valueOf(data.activeProjects()));
        loadUpcoming(incomplete);

        MomentaCore.Recommendation recommendation = data.recommendation();
        if (recommendation == null) {
            nowTitleLabel.setText("Nothing pending — add a task to get started.");
            nowPriorityLabel.setText("");
            nowPriorityBadge.setText("CLEAR");
            nowDeadlineLabel.setText("");
            nowCategoryLabel.setText("");
            nowReasonsLabel.setText("");
            nowProgressBar.setProgress(0);
            nowProgressPercentLabel.setText("");
            return;
        }

        Task rec = recommendation.task();
        nowTitleLabel.setText(rec.getTitle());
        nowPriorityLabel.setText("Priority score: " + recommendation.score() + " / 100");
        nowPriorityBadge.setText(recommendation.score() >= 80 ? "HIGH" : recommendation.score() >= 55 ? "NEXT" : "NORMAL");
        nowDeadlineLabel.setText(rec.getDeadline() == null || rec.getDeadline().isBlank()
                ? "No deadline" : "Due " + rec.getDeadline());
        nowCategoryLabel.setText("• " + (rec.getCategory() == null ? "Other" : rec.getCategory()));
        List<String> reasons = recommendation.reasons();
        nowReasonsLabel.setText("• " + String.join("\n• ", reasons.isEmpty() ? List.of("Standard priority") : reasons));
        AnimationUtil.animateProgress(nowProgressBar, rec.getProgress() / 100.0);
        nowProgressPercentLabel.setText(rec.getProgress() + "%");
        AnimationUtil.fadeIn(nowTitleLabel, 180);
    }

    private void applySelectedDateData(List<Task> tasks, List<FocusSession> sessions) {
        int total = tasks.size();
        int completed = (int) tasks.stream().filter(Task::isCompleted).count();
        taskCountLabel.setText(String.valueOf(total));
        double progress = total == 0 ? 0 : completed / (double) total;
        int percent = (int) Math.round(progress * 100);

        todayProgressPercentLabel.setText(percent + "%");
        todayProgressMetaLabel.setText(completed + " / " + total + " tasks");
        todayProgressHintLabel.setText(total == 0
                ? "No tasks planned for this date. Add something meaningful to get started."
                : completed == total
                ? "Everything planned for this date is complete."
                : (total - completed) + " task" + ((total - completed) == 1 ? "" : "s") + " still moving.");

        // ঘড়ির কাঁটার দিকে মসৃণভাবে চলার জন্য নেগেটিভ লেন্থ পাঠানো হয়েছে
        AnimationUtil.animateArc(progressArc, -progress * 360);
        taskProgressBar.setProgress(progress);
        todayTaskSummaryLabel.setText(completed + " done • " + (total - completed) + " open");
        taskCard.setOnMouseClicked(e -> onOpenTasks());

        long focusMinutes = sessions.stream()
                .filter(s -> isSameDate(s.getStartedAt(), selectedDate))
                .mapToLong(FocusSession::getDurationMinutes)
                .sum();
        focusMinutesLabel.setText(focusMinutes + " min");
        showDateData(tasks, sessions);
    }

    private void showDateData(List<Task> tasks, List<FocusSession> sessions) {
        todayTaskList.getChildren().clear();
        List<Task> sorted = tasks.stream()
                .sorted(Comparator.comparing(Task::isCompleted)
                        .thenComparing(Task::getPriorityScore, Comparator.reverseOrder()))
                .limit(7)
                .toList();

        if (sorted.isEmpty()) {
            Label empty = new Label("No tasks planned for this date.");
            empty.setTextFill(MomentaTheme.MUTED);
            empty.setPadding(new Insets(12, 4, 12, 4));
            todayTaskList.getChildren().add(empty);
        } else {
            for (Task task : sorted) todayTaskList.getChildren().add(createTaskRow(task));
        }
    }

    private VBox createTaskRow(Task task) {
        VBox row = new VBox(5);
        row.setPadding(new Insets(10, 12, 10, 12));
        row.setCursor(Cursor.HAND);
        row.setBackground(new Background(new BackgroundFill(
                MomentaTheme.SURFACE, new CornerRadii(12), Insets.EMPTY)));
        row.setBorder(new Border(new BorderStroke(
                task.isCompleted() ? MomentaTheme.SOFT_GREEN.deriveColor(0, 0.5, 1, 0.35) : MomentaTheme.BORDER_SOFT,
                BorderStrokeStyle.SOLID, new CornerRadii(12), new BorderWidths(1))));

        HBox header = new HBox(9);
        header.setAlignment(Pos.CENTER_LEFT);
        Label status = new Label(task.isCompleted() ? "✓" : task.getProgress() > 0 ? "◐" : "○");
        status.setFont(Font.font("Segoe UI Semibold", 14));
        status.setTextFill(task.isCompleted() ? MomentaTheme.SOFT_GREEN : MomentaTheme.ROYAL_PURPLE);

        Label title = new Label(task.getTitle());
        title.setFont(Font.font("Segoe UI Semibold", 13));
        title.setTextFill(MomentaTheme.TEXT);
        title.setWrapText(true);

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        Label meta = new Label(task.getPriorityScore() > 0 ? "P" + task.getPriorityScore() : task.getCategory());
        meta.setTextFill(MomentaTheme.MUTED);
        meta.setFont(Font.font("Segoe UI", 10));
        header.getChildren().addAll(status, title, spacer, meta);

        StackPane progressTrack = new StackPane();
        progressTrack.setMinHeight(6);
        progressTrack.setPrefHeight(6);
        progressTrack.setMaxWidth(Double.MAX_VALUE);
        progressTrack.setBackground(new Background(new BackgroundFill(MomentaTheme.SURFACE_3, new CornerRadii(8), Insets.EMPTY)));

        Region progressFill = new Region();
        progressFill.setMinHeight(6);
        progressFill.setPrefHeight(6);
        progressFill.setMaxHeight(6);
        progressFill.setMaxWidth(Double.MAX_VALUE);
        progressFill.setBackground(new Background(new BackgroundFill(MomentaTheme.ROYAL_PURPLE, new CornerRadii(8), Insets.EMPTY)));
        double fraction = Math.max(0, Math.min(1, task.getProgress() / 100.0));
        progressFill.prefWidthProperty().bind(progressTrack.widthProperty().multiply(fraction));
        StackPane.setAlignment(progressFill, Pos.CENTER_LEFT);
        progressTrack.getChildren().add(progressFill);

        row.getChildren().addAll(header, progressTrack);

        row.setOnMouseEntered(e -> {
            row.setTranslateY(-1);
            row.setBackground(new Background(new BackgroundFill(MomentaTheme.SURFACE_3, new CornerRadii(12), Insets.EMPTY)));
        });
        row.setOnMouseExited(e -> {
            row.setTranslateY(0);
            row.setBackground(new Background(new BackgroundFill(MomentaTheme.SURFACE, new CornerRadii(12), Insets.EMPTY)));
        });
        row.setOnMouseClicked(e -> onOpenTasks());
        return row;
    }

    private void loadUpcoming(List<Task> incomplete) {
        upcomingList.getChildren().clear();
        LocalDate today = LocalDate.now();
        List<Task> upcoming = incomplete.stream()
                .filter(t -> t.getDeadline() != null && !t.getDeadline().isBlank())
                .filter(t -> {
                    try { return !LocalDate.parse(t.getDeadline()).isBefore(today); }
                    catch (Exception ignored) { return false; }
                })
                .sorted(Comparator.comparing(t -> safeDate(t.getDeadline())))
                .limit(5)
                .toList();

        if (upcoming.isEmpty()) {
            Label empty = new Label("No upcoming deadlines — your runway is clear.");
            empty.setTextFill(MomentaTheme.MUTED);
            empty.setWrapText(true);
            upcomingList.getChildren().add(empty);
            return;
        }

        for (Task task : upcoming) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(9, 10, 9, 10));
            row.setBackground(new Background(new BackgroundFill(MomentaTheme.SURFACE, new CornerRadii(11), Insets.EMPTY)));
            row.setBorder(new Border(new BorderStroke(MomentaTheme.BORDER_SOFT, BorderStrokeStyle.SOLID,
                    new CornerRadii(11), new BorderWidths(1))));

            VBox info = new VBox(2);
            Label title = new Label(task.getTitle());
            title.setTextFill(MomentaTheme.TEXT);
            title.setFont(Font.font("Segoe UI Semibold", 12.5));
            title.setWrapText(true);
            Label category = new Label(task.getCategory() + "  •  " + task.getProgress() + "%");
            category.setTextFill(MomentaTheme.MUTED);
            category.setFont(Font.font("Segoe UI", 10));
            info.getChildren().addAll(title, category);
            HBox.setHgrow(info, javafx.scene.layout.Priority.ALWAYS);

            Label due = new Label(formatDue(task.getDeadline()));
            due.setTextFill(MomentaTheme.PEACH);
            due.setFont(Font.font("Segoe UI Semibold", 10));
            row.getChildren().addAll(info, due);
            row.setCursor(Cursor.HAND);
            row.setOnMouseClicked(e -> onOpenTasks());
            upcomingList.getChildren().add(row);
        }
    }

    private void loadGamification() {
        final int uid = CurrentUser.getId();
        javafx.concurrent.Task<GamificationStats> task = new javafx.concurrent.Task<>() {
            @Override protected GamificationStats call() { return gamificationService.calculate(uid); }
        };
        task.setOnSucceeded(e -> applyGamification(task.getValue()));
        task.setOnFailed(e -> {
            levelLabel.setText("LEVEL —");
            xpLabel.setText("Gamification unavailable");
            achievementLabel.setText("");
        });
        TaskExecutor.getInstance().workPool().submit(task);
    }

    private void applyGamification(GamificationStats stats) {
        levelLabel.setText("LEVEL " + String.format("%02d", stats.getLevel()));
        xpLabel.setText(stats.getXpIntoLevel() + " / " + stats.getXpForNextLevel()
                + " XP to next level  •  " + stats.getTotalXp() + " total");
        AnimationUtil.animateProgress(xpProgressBar, stats.getProgress());
        streakLabel.setText("🔥 " + stats.getCurrentStreak() + " day streak");
        long unlocked = stats.getUnlockedAchievementCount();
        achievementLabel.setText(unlocked + " / " + stats.getAchievements().size() + " achievements");
        if (unlocked > 0) AnimationUtil.pulse(gamificationCard);
    }

    private void loadNotifications() {
        final int uid = CurrentUser.getId();
        javafx.concurrent.Task<NotificationData> task = new javafx.concurrent.Task<>() {
            @Override protected NotificationData call() {
                List<Notification> unread = notificationService.getUnread(uid);
                int count = notificationService.getUnreadCount(uid);
                List<String> messages = unread.stream().limit(6)
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
        javafx.concurrent.Task<Quote> networkTask = new javafx.concurrent.Task<>() {
            @Override protected Quote call() throws Exception { return insightService.getDailyInsight(); }
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
    @FXML private void onOpenAnalytics() { SceneManager.getInstance().invalidate("Analytics"); SceneManager.getInstance().switchTo("Analytics"); }
    @FXML private void onOpenSettings() { SceneManager.getInstance().invalidate("Settings"); SceneManager.getInstance().switchTo("Settings"); }

    @FXML
    private void onLogout() {
        if (!AlertUtil.confirm("Logout", "Are you sure you want to log out of MOMENTA?")) return;
        CurrentUser.logout();
        SessionStore.forget();
        SceneManager.getInstance().invalidateAll();
        SceneManager.getInstance().switchTo("Login");
    }

    @FXML
    private void onRefresh() {
        greetingLabel.setText(greetingForNow());
        buildDateNavigator();
        updateDateLabels();
        loadDashboardData();
        loadSelectedDateData();
        loadNotifications();
        loadDailyInsight();
        loadGamification();
    }

    @FXML private void onRefreshInsight() { loadDailyInsight(); }

    @FXML
    private void onOpenNotifications() {
        final int uid = CurrentUser.getId();
        javafx.concurrent.Task<List<Notification>> task = new javafx.concurrent.Task<>() {
            @Override protected List<Notification> call() { return notificationService.getUnread(uid); }
        };
        task.setOnSucceeded(e -> {
            List<Notification> unread = task.getValue();
            String message = unread.isEmpty()
                    ? "You have no unread notifications."
                    : unread.stream().map(Notification::getMessage).collect(Collectors.joining("\n\n"));
            AlertUtil.showInfo("Notifications", message);
        });
        task.setOnFailed(e -> AlertUtil.showError("Notifications", "Could not load notifications.", task.getException()));
        TaskExecutor.getInstance().workPool().submit(task);
    }

    @FXML
    private void onMarkAllNotificationsRead() {
        final int uid = CurrentUser.getId();
        javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<>() {
            @Override protected Void call() { notificationService.markAllAsRead(uid); return null; }
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

    private boolean isSameDate(String value, LocalDate date) {
        if (value == null || value.isBlank()) return false;
        try { return LocalDateTime.parse(value).toLocalDate().equals(date); }
        catch (Exception ignored) {
            try { return LocalDate.parse(value.substring(0, 10)).equals(date); }
            catch (Exception ignoredAgain) { return false; }
        }
    }

    private LocalDate safeDate(String value) {
        try { return LocalDate.parse(value); }
        catch (Exception ignored) { return LocalDate.MAX; }
    }

    private String formatDue(String value) {
        LocalDate date = safeDate(value);
        if (date.equals(LocalDate.MAX)) return value;
        if (date.equals(LocalDate.now())) return "Today";
        if (date.equals(LocalDate.now().plusDays(1))) return "Tomorrow";
        return date.format(SHORT_DATE);
    }

    private record DashboardData(List<Task> incompleteTasks, MomentaCore.Recommendation recommendation,
                                 int activeGoals, int activeProjects, List<FocusSession> sessions) {}

    private record DateData(List<Task> tasks, List<FocusSession> sessions) {}
    private record NotificationData(int unreadCount, List<String> messages) {}
}