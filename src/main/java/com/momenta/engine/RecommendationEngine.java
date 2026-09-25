package com.momenta.engine;

import com.momenta.model.Task;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Phase 11:
 * Responsible only for deciding which task should be recommended or ranked.
 *
 * PriorityEngine calculates the score.
 * RecommendationEngine uses the calculated score to select/rank tasks.
 */
public class RecommendationEngine {

    public Task recommend(List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return null;
        }

        return tasks.stream()
                .max(
                        Comparator.comparingInt(Task::getPriorityScore)
                                .thenComparingInt(Task::getImportance)
                                // For equal scores, an earlier deadline wins.
                                .thenComparing(
                                        this::deadlineValue,
                                        Comparator.reverseOrder()
                                )
                                // For equal score/deadline, more estimated work wins.
                                .thenComparingInt(Task::getEstimatedMinutes)
                )
                .orElse(null);
    }

    public List<Task> rank(List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return List.of();
        }

        return tasks.stream()
                .sorted(
                        Comparator.comparingInt(Task::getPriorityScore)
                                .reversed()
                                .thenComparing(
                                        Task::getImportance,
                                        Comparator.reverseOrder()
                                )
                                .thenComparing(this::deadlineValue)
                )
                .toList();
    }

    private LocalDate deadlineValue(Task task) {
        String deadline = task.getDeadline();

        if (deadline == null || deadline.isBlank()) {
            return LocalDate.MAX;
        }

        try {
            return LocalDate.parse(deadline);
        } catch (Exception e) {
            return LocalDate.MAX;
        }
    }
}
