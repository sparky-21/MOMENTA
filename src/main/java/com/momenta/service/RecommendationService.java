package com.momenta.service;

import com.momenta.engine.PriorityEngine;
import com.momenta.engine.RecommendationEngine;
import com.momenta.model.Goal;
import com.momenta.model.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Phase 11 recommendation service.
 *
 * Loads the user's incomplete tasks/goals, calculates contextual priority
 * using workload and goal importance, and delegates task selection/ranking
 * to RecommendationEngine.
 */
public class RecommendationService {

    private final TaskService taskService;
    private final GoalService goalService;
    private final RecommendationEngine recommendationEngine;

    public RecommendationService() {
        this(
                new TaskService(),
                new GoalService(),
                new RecommendationEngine()
        );
    }

    public RecommendationService(
            TaskService taskService,
            GoalService goalService,
            RecommendationEngine recommendationEngine
    ) {
        this.taskService = taskService;
        this.goalService = goalService;
        this.recommendationEngine = recommendationEngine;
    }

    public RecommendationResult recommend(int userId) {
        List<Task> tasks = taskService.getIncompleteTasks(userId);

        if (tasks.isEmpty()) {
            return null;
        }

        Map<Integer, Goal> goals = loadGoals(userId);
        calculateContextualPriority(tasks, goals);

        Task selected = recommendationEngine.recommend(tasks);

        if (selected == null) {
            return null;
        }

        Goal selectedGoal = selected.getGoalId() == null
                ? null
                : goals.get(selected.getGoalId());

        int goalImportance = selectedGoal == null
                ? 0
                : selectedGoal.getImportance();

        PriorityEngine.PriorityResult result =
                PriorityEngine.score(selected, goalImportance, tasks.size());

        return new RecommendationResult(
                selected,
                result.getScore(),
                result.getReasons()
        );
    }

    public List<Task> rank(int userId) {
        List<Task> tasks = taskService.getIncompleteTasks(userId);

        if (tasks.isEmpty()) {
            return List.of();
        }

        Map<Integer, Goal> goals = loadGoals(userId);
        calculateContextualPriority(tasks, goals);

        return recommendationEngine.rank(tasks);
    }

    private void calculateContextualPriority(
            List<Task> tasks,
            Map<Integer, Goal> goals
    ) {
        int workload = tasks.size();

        for (Task task : tasks) {
            Goal goal = task.getGoalId() == null
                    ? null
                    : goals.get(task.getGoalId());

            int goalImportance = goal == null
                    ? 0
                    : goal.getImportance();

            PriorityEngine.PriorityResult result =
                    PriorityEngine.score(
                            task,
                            goalImportance,
                            workload
                    );

            task.setPriorityScore(result.getScore());
        }
    }

    private Map<Integer, Goal> loadGoals(int userId) {
        Map<Integer, Goal> goalMap = new HashMap<>();

        for (Goal goal : goalService.getAllGoals(userId)) {
            goalMap.put(goal.getId(), goal);
        }

        return goalMap;
    }

    public record RecommendationResult(
            Task task,
            int score,
            List<String> reasons
    ) {
    }
}
