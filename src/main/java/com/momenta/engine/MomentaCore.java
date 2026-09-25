package com.momenta.engine;

import com.momenta.model.Task;
import com.momenta.service.GoalService;
import com.momenta.service.RecommendationService;
import com.momenta.service.TaskService;

import java.util.List;

/**
 * Phase 10 + Phase 11 central business coordinator.
 *
 * The controller talks to MomentaCore instead of directly deciding how
 * recommendations are calculated. MomentaCore returns plain data and never
 * touches JavaFX controls.
 */
public class MomentaCore {

    private final RecommendationService recommendationService;

    public MomentaCore() {
        this(
                new RecommendationService(
                        new TaskService(),
                        new GoalService(),
                        new RecommendationEngine()
                )
        );
    }

    public MomentaCore(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    public Recommendation recommend(int userId) {
        RecommendationService.RecommendationResult result =
                recommendationService.recommend(userId);

        if (result == null) {
            return null;
        }

        return new Recommendation(
                result.task(),
                result.score(),
                result.reasons()
        );
    }

    public List<Task> rankIncompleteTasks(int userId) {
        return recommendationService.rank(userId);
    }

    public record Recommendation(
            Task task,
            int score,
            List<String> reasons
    ) {
    }
}
