package com.momenta.engine;

import com.momenta.model.Goal;
import com.momenta.model.Task;
import com.momenta.service.GoalService;
import com.momenta.service.TaskService;

import java.util.Comparator;
import java.util.List;

/**
 * Phase 10 central business engine. It coordinates data and engines but never
 * touches JavaFX controls. Controllers call this class; it returns plain data.
 */
public class MomentaCore {
    private final TaskService taskService;
    private final GoalService goalService;

    public MomentaCore() {
        this(new TaskService(), new GoalService());
    }

    public MomentaCore(TaskService taskService, GoalService goalService) {
        this.taskService = taskService;
        this.goalService = goalService;
    }

    public Recommendation recommend(int userId) {
        List<Task> tasks = taskService.getIncompleteTasks(userId);
        if (tasks.isEmpty()) return null;

        int workload = tasks.size();
        tasks.forEach(task -> {
            Goal goal = findGoal(task, userId);
            int goalImportance = goal == null ? 0 : goal.getImportance();
            task.setPriorityScore(PriorityEngine.score(task, goalImportance, workload).getScore());
        });

        Task selected = tasks.stream()
                .max(Comparator.comparingInt(Task::getPriorityScore))
                .orElse(null);
        if (selected == null) return null;

        Goal goal = findGoal(selected, userId);
        int goalImportance = goal == null ? 0 : goal.getImportance();
        PriorityEngine.PriorityResult result =
                PriorityEngine.score(selected, goalImportance, workload);
        return new Recommendation(selected, result.getScore(), result.getReasons());
    }

    public List<Task> rankIncompleteTasks(int userId) {
        List<Task> tasks = taskService.getIncompleteTasks(userId);
        int workload = tasks.size();
        tasks.forEach(task -> {
            Goal goal = findGoal(task, userId);
            task.setPriorityScore(PriorityEngine.score(task,
                    goal == null ? 0 : goal.getImportance(), workload).getScore());
        });
        tasks.sort(Comparator.comparingInt(Task::getPriorityScore).reversed());
        return tasks;
    }

    private Goal findGoal(Task task, int userId) {
        if (task.getGoalId() == null) return null;
        return goalService.getAllGoals(userId).stream()
                .filter(g -> g.getId() == task.getGoalId())
                .findFirst().orElse(null);
    }

    public record Recommendation(Task task, int score, List<String> reasons) {}
}
