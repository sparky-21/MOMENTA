package com.momenta.engine;

import com.momenta.model.Task;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * PriorityEngine — Section 15.
 *
 * This is a plain, deterministic, rule-based scoring function. It is NOT
 * machine learning and must never be described as "AI" in the UI or the
 * report (Section 1 / Section 15 both say this explicitly).
 *
 * Priority Score (0-100) =
 *      Deadline Score      (0-40)  — how soon the deadline is
 *    + Importance Score    (0-25)  — importance field (1-5) scaled
 *    + Incompletion Score  (0-20)  — 100 - progress, scaled
 *    + Effort Score        (0-15)  — larger estimated effort nudges a task
 *                                    up, since it needs an earlier start
 *
 * Each component is capped so no single factor can dominate, and the
 * PriorityResult carries the reasons in plain English/Bangla-ready strings
 * so the UI (MOMENTA NOW / Section 16) can explain the recommendation
 * instead of just showing a number.
 */
public final class PriorityEngine {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    private PriorityEngine() {
    }

    public static PriorityResult score(Task task) {
        List<String> reasons = new ArrayList<>();

        int deadlineScore = deadlineScore(task, reasons);
        int importanceScore = importanceScore(task, reasons);
        int incompletionScore = incompletionScore(task, reasons);
        int effortScore = effortScore(task, reasons);

        int total = deadlineScore + importanceScore + incompletionScore + effortScore;
        total = Math.max(0, Math.min(100, total));

        return new PriorityResult(total, reasons);
    }

    private static int deadlineScore(Task task, List<String> reasons) {
        String deadline = task.getDeadline();
        if (deadline == null || deadline.isBlank()) {
            return 5; // no deadline set — low urgency, but not zero
        }
        try {
            long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(deadline, ISO));
            if (daysLeft < 0) {
                reasons.add("Deadline has already passed");
                return 40;
            } else if (daysLeft == 0) {
                reasons.add("Deadline is today");
                return 40;
            } else if (daysLeft == 1) {
                reasons.add("Deadline is tomorrow");
                return 35;
            } else if (daysLeft <= 3) {
                reasons.add("Deadline is within " + daysLeft + " days");
                return 25;
            } else if (daysLeft <= 7) {
                reasons.add("Deadline is within a week");
                return 15;
            } else {
                return 5;
            }
        } catch (Exception e) {
            return 5;
        }
    }

    private static int importanceScore(Task task, List<String> reasons) {
        int importance = task.getImportance(); // 1-5
        int score = importance * 5; // max 25
        if (importance >= 4) {
            reasons.add("Marked as high importance");
        }
        return score;
    }

    private static int incompletionScore(Task task, List<String> reasons) {
        int remaining = 100 - task.getProgress();
        int score = (int) Math.round(remaining * 0.20); // max 20
        if (task.getProgress() <= 25) {
            reasons.add("Only " + task.getProgress() + "% complete");
        }
        return score;
    }

    private static int effortScore(Task task, List<String> reasons) {
        int minutes = task.getEstimatedMinutes();
        int score;
        if (minutes >= 180) {
            score = 15;
            reasons.add("Significant work remaining (" + (minutes / 60) + "h+ estimated)");
        } else if (minutes >= 60) {
            score = 10;
        } else {
            score = 5;
        }
        return score;
    }

    /** Immutable result: the score plus the human-readable reasons behind it. */
    public static final class PriorityResult {
        private final int score;
        private final List<String> reasons;

        PriorityResult(int score, List<String> reasons) {
            this.score = score;
            this.reasons = reasons;
        }

        public int getScore() { return score; }
        public List<String> getReasons() { return reasons; }
    }
}
