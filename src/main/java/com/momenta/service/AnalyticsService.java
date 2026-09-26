package com.momenta.service;

import com.momenta.model.Expense;
import com.momenta.model.FocusSession;
import com.momenta.model.Goal;
import com.momenta.model.Habit;
import com.momenta.model.HabitLog;
import com.momenta.model.Task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Phase 15 — deterministic analytics calculations for one user.
 *
 * This class contains no JavaFX code. It loads existing module data through
 * their services and converts it into a single immutable AnalyticsSnapshot.
 */
public class AnalyticsService {

    private final TaskService taskService = new TaskService();
    private final GoalService goalService = new GoalService();
    private final HabitService habitService = new HabitService();
    private final FinanceService financeService = new FinanceService();
    private final FocusSessionService focusSessionService = new FocusSessionService();

    public AnalyticsSnapshot calculate(int userId) {
        List<Task> tasks = taskService.getAllTasks(userId);
        List<Goal> goals = goalService.getAllGoals(userId);
        List<Habit> habits = habitService.getAllHabits(userId);
        List<Expense> expenses = financeService.getExpenses(userId);
        List<FocusSession> sessions = focusSessionService.getHistory(userId);

        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(6);

        Map<LocalDate, Integer> focusMinutes = new LinkedHashMap<>();
        Map<LocalDate, Double> spending = new LinkedHashMap<>();

        for (int i = 0; i < 7; i++) {
            LocalDate date = start.plusDays(i);
            focusMinutes.put(date, 0);
            spending.put(date, 0.0);
        }

        for (FocusSession session : sessions) {
            LocalDate date = parseDateTime(session.getStartedAt());
            if (date != null && !date.isBefore(start) && !date.isAfter(today)) {
                focusMinutes.computeIfPresent(
                        date,
                        (key, value) -> value + session.getDurationMinutes()
                );
            }
        }

        for (Expense expense : expenses) {
            LocalDate date = parseDate(expense.getExpenseDate());
            if (date != null && !date.isBefore(start) && !date.isAfter(today)) {
                spending.computeIfPresent(
                        date,
                        (key, value) -> value + expense.getAmount()
                );
            }
        }

        long completedTasks = tasks.stream()
                .filter(Task::isCompleted)
                .count();

        long completedGoals = goals.stream()
                .filter(g -> "COMPLETED".equalsIgnoreCase(g.getStatus()))
                .count();

        double taskCompletion = percentage(completedTasks, tasks.size());
        double goalCompletion = percentage(completedGoals, goals.size());
        double habitConsistency = calculateHabitConsistency(habits, start, today);
        double focusScore = Math.min(100.0,
                focusMinutes.values().stream().mapToInt(Integer::intValue).sum()
                        / 420.0 * 100.0);

        double productivityScore =
                (taskCompletion * 0.50) +
                (goalCompletion * 0.20) +
                (habitConsistency * 0.20) +
                (focusScore * 0.10);

        double pulse = (productivityScore * 0.60)
                + (habitConsistency * 0.20)
                + (focusScore * 0.20);

        Map<String, Double> categorySpending =
                expenses.stream().collect(Collectors.groupingBy(
                        Expense::getCategory,
                        LinkedHashMap::new,
                        Collectors.summingDouble(Expense::getAmount)
                ));

        return new AnalyticsSnapshot(
                completedTasks,
                tasks.size(),
                completedGoals,
                goals.size(),
                taskCompletion,
                goalCompletion,
                habitConsistency,
                focusMinutes,
                spending,
                categorySpending,
                financeService.totalIncome(userId),
                financeService.totalExpenses(userId),
                clamp(pulse),
                clamp(productivityScore),
                clamp(focusScore)
        );
    }

    private double calculateHabitConsistency(
            List<Habit> habits,
            LocalDate start,
            LocalDate end
    ) {
        if (habits.isEmpty()) return 0;

        int totalPossible = 0;
        int completed = 0;

        for (Habit habit : habits) {
            List<HabitLog> logs = habitService.getLogs(habit.getId());
            Set<LocalDate> completedDates = logs.stream()
                    .filter(HabitLog::isCompleted)
                    .map(HabitLog::getLogDate)
                    .map(this::parseDate)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                totalPossible++;
                if (completedDates.contains(date)) {
                    completed++;
                }
            }
        }

        return percentage(completed, totalPossible);
    }

    private double percentage(long value, long total) {
        return total == 0 ? 0 : (value * 100.0) / total;
    }

    private double clamp(double value) {
        return Math.max(0, Math.min(100, value));
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDate.parse(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private LocalDate parseDateTime(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDateTime.parse(value).toLocalDate();
        } catch (Exception ignored) {
            try {
                return LocalDate.parse(value.substring(0, 10));
            } catch (Exception ignoredAgain) {
                return null;
            }
        }
    }

    public record AnalyticsSnapshot(
            long completedTasks,
            int totalTasks,
            long completedGoals,
            int totalGoals,
            double taskCompletionRate,
            double goalCompletionRate,
            double habitConsistency,
            Map<LocalDate, Integer> focusMinutesByDay,
            Map<LocalDate, Double> spendingByDay,
            Map<String, Double> spendingByCategory,
            double totalIncome,
            double totalExpenses,
            double pulse,
            double productivityScore,
            double focusScore
    ) {}
}
