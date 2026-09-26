package com.momenta.controller;

import com.momenta.service.AnalyticsService;
import com.momenta.threading.TaskExecutor;
import com.momenta.utility.AlertUtil;
import com.momenta.utility.CurrentUser;
import com.momenta.utility.SceneManager;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Phase 15 — Analytics screen.
 *
 * The controller only coordinates UI. AnalyticsService performs the
 * calculations, and the calculation runs through the shared ExecutorService.
 */
public class AnalyticsController {

    @FXML private Label pulseLabel;
    @FXML private Label productivityLabel;
    @FXML private Label taskCompletionLabel;
    @FXML private Label goalCompletionLabel;
    @FXML private Label habitConsistencyLabel;
    @FXML private Label focusScoreLabel;
    @FXML private Label financeLabel;

    @FXML private ProgressBar pulseProgress;
    @FXML private ProgressBar productivityProgress;
    @FXML private ProgressBar habitProgress;

    @FXML private BarChart<String, Number> focusChart;
    @FXML private LineChart<String, Number> spendingChart;
    @FXML private PieChart spendingCategoryChart;

    private final AnalyticsService analyticsService = new AnalyticsService();

    @FXML
    public void initialize() {
        loadAnalytics();
    }

    private void loadAnalytics() {
        final int userId = CurrentUser.getId();

        Task<AnalyticsService.AnalyticsSnapshot> analyticsTask = new Task<>() {
            @Override
            protected AnalyticsService.AnalyticsSnapshot call() {
                return analyticsService.calculate(userId);
            }
        };

        analyticsTask.setOnSucceeded(e -> showAnalytics(analyticsTask.getValue()));
        analyticsTask.setOnFailed(e -> AlertUtil.showError(
                "Analytics",
                "Could not calculate analytics.",
                analyticsTask.getException()
        ));

        TaskExecutor.getInstance().submit(analyticsTask);
    }

    private void showAnalytics(AnalyticsService.AnalyticsSnapshot data) {
        pulseLabel.setText(format(data.pulse()));
        productivityLabel.setText(format(data.productivityScore()));
        taskCompletionLabel.setText(
                data.completedTasks() + " / " + data.totalTasks()
                        + " (" + format(data.taskCompletionRate()) + "%)"
        );
        goalCompletionLabel.setText(
                data.completedGoals() + " / " + data.totalGoals()
                        + " (" + format(data.goalCompletionRate()) + "%)"
        );
        habitConsistencyLabel.setText(format(data.habitConsistency()) + "%");
        focusScoreLabel.setText(format(data.focusScore()) + " / 100");

        financeLabel.setText(
                String.format(
                        "Income: %.2f    Expenses: %.2f    Balance: %.2f",
                        data.totalIncome(),
                        data.totalExpenses(),
                        data.totalIncome() - data.totalExpenses()
                )
        );

        pulseProgress.setProgress(data.pulse() / 100.0);
        productivityProgress.setProgress(data.productivityScore() / 100.0);
        habitProgress.setProgress(data.habitConsistency() / 100.0);

        fillFocusChart(data.focusMinutesByDay());
        fillSpendingChart(data.spendingByDay());
        fillCategoryChart(data.spendingByCategory());
    }

    private void fillFocusChart(Map<LocalDate, Integer> values) {
        focusChart.getData().clear();
        BarChart.Series<String, Number> series = new BarChart.Series<>();
        series.setName("Focus Minutes");

        DateTimeFormatter format = DateTimeFormatter.ofPattern("MM/dd");
        values.forEach((date, minutes) ->
                series.getData().add(
                        new BarChart.Data<>(format.format(date), minutes)
                )
        );

        focusChart.getData().add(series);
    }

    private void fillSpendingChart(Map<LocalDate, Double> values) {
        spendingChart.getData().clear();
        LineChart.Series<String, Number> series = new LineChart.Series<>();
        series.setName("Daily Spending");

        DateTimeFormatter format = DateTimeFormatter.ofPattern("MM/dd");
        values.forEach((date, amount) ->
                series.getData().add(
                        new LineChart.Data<>(format.format(date), amount)
                )
        );

        spendingChart.getData().add(series);
    }

    private void fillCategoryChart(Map<String, Double> values) {
        spendingCategoryChart.getData().clear();
        values.forEach((category, amount) ->
                spendingCategoryChart.getData().add(
                        new PieChart.Data(category, amount)
                )
        );
    }

    private String format(double value) {
        return String.format("%.1f", value);
    }

    @FXML
    private void onRefresh() {
        loadAnalytics();
    }

    @FXML
    private void onBackToDashboard() {
        SceneManager.getInstance().invalidate("Dashboard");
        SceneManager.getInstance().switchTo("Dashboard");
    }
}
