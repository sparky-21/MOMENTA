package com.momenta.engine;

import com.momenta.model.Task;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Phase 10: deterministic, explainable task-priority calculation.
 *
 * Score before workload adjustment:
 *   deadline 0-40 + importance 0-25 + incompletion 0-20
 *   + effort 0-15 + goal importance 0-10.
 * The final score is clamped to 0-100 after subtracting workload adjustment.
 */
public final class PriorityEngine {
    private PriorityEngine() {}

    public static PriorityResult score(Task task) {
        return score(task, 0, 0);
    }

    public static PriorityResult score(Task task, int goalImportance, int workloadCount) {
        List<String> reasons = new ArrayList<>();
        int deadline = deadlineScore(task, reasons);
        int importance = importanceScore(task, reasons);
        int incomplete = incompletionScore(task, reasons);
        int effort = effortScore(task, reasons);
        int goal = goalImportanceScore(goalImportance, reasons);
        int workload = workloadAdjustment(workloadCount, reasons);

        int total = deadline + importance + incomplete + effort + goal - workload;
        total = Math.max(0, Math.min(100, total));
        return new PriorityResult(total, reasons);
    }

    private static int deadlineScore(Task task, List<String> reasons) {
        String deadline = task.getDeadline();
        if (deadline == null || deadline.isBlank()) return 5;
        try {
            long days = ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(deadline));
            if (days < 0) { reasons.add("Deadline has already passed"); return 40; }
            if (days == 0) { reasons.add("Deadline is today"); return 40; }
            if (days == 1) { reasons.add("Deadline is tomorrow"); return 35; }
            if (days <= 3) { reasons.add("Deadline is within " + days + " days"); return 25; }
            if (days <= 7) { reasons.add("Deadline is within a week"); return 15; }
            return 5;
        } catch (Exception ignored) {
            return 5;
        }
    }

    private static int importanceScore(Task task, List<String> reasons) {
        int score = Math.max(1, Math.min(5, task.getImportance())) * 5;
        if (task.getImportance() >= 4) reasons.add("Marked as high importance");
        return score;
    }

    private static int incompletionScore(Task task, List<String> reasons) {
        int progress = Math.max(0, Math.min(100, task.getProgress()));
        int score = (int) Math.round((100 - progress) * 0.20);
        if (progress <= 25) reasons.add("Only " + progress + "% complete");
        return score;
    }

    private static int effortScore(Task task, List<String> reasons) {
        int minutes = Math.max(0, task.getEstimatedMinutes());
        if (minutes >= 180) {
            reasons.add("Significant work remaining (" + (minutes / 60) + "h+ estimated)");
            return 15;
        }
        if (minutes >= 60) return 10;
        return 5;
    }

    private static int goalImportanceScore(int goalImportance, List<String> reasons) {
        if (goalImportance <= 0) return 0;
        int score = Math.min(5, goalImportance) * 2;
        if (goalImportance >= 4) reasons.add("Connected to an important goal");
        return score;
    }

    private static int workloadAdjustment(int workloadCount, List<String> reasons) {
        int adjustment = Math.min(10, Math.max(0, workloadCount - 5));
        if (adjustment > 0) reasons.add("Current workload is high (" + workloadCount + " incomplete tasks)");
        return adjustment;
    }

    public static final class PriorityResult {
        private final int score;
        private final List<String> reasons;

        public PriorityResult(int score, List<String> reasons) {
            this.score = score;
            this.reasons = List.copyOf(reasons);
        }

        public int getScore() { return score; }
        public List<String> getReasons() { return reasons; }
    }
}
