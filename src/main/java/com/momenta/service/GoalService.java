package com.momenta.service;

import com.momenta.dao.GoalDAO;
import com.momenta.dao.TaskDAO;
import com.momenta.dao.impl.GoalDAOImpl;
import com.momenta.dao.impl.TaskDAOImpl;
import com.momenta.model.Goal;
import com.momenta.model.Task;

import java.util.List;

/**
 * GoalService — Section 7.
 *
 * The one piece of real business logic here is recalculateProgress(): a
 * goal's progress is NOT something the user types in directly (there's no
 * progress field in the Goal dialog) — it is derived from how many of the
 * tasks attached to that goal are completed. This mirrors how
 * PriorityEngine keeps scoring out of the DAO layer: GoalDAO only persists
 * whatever progress value it's given, it never computes one.
 */
public class GoalService {

    private final GoalDAO goalDAO = new GoalDAOImpl();
    private final TaskDAO taskDAO = new TaskDAOImpl();

    public Goal createGoal(Goal goal) {
        Goal saved = goalDAO.save(goal);
        recalculateProgress(saved);
        return saved;
    }

    public void updateGoal(Goal goal) {
        goalDAO.update(goal);
    }

    public void deleteGoal(int id) {
        goalDAO.delete(id);
    }

    public List<Goal> getAllGoals(int userId) {
        return goalDAO.findAll(userId);
    }

    /** Top-level ("Life") goals — parentGoalId is null. */
    public List<Goal> getTopLevelGoals(int userId) {
        return goalDAO.findTopLevel(userId);
    }

    public List<Goal> getChildGoals(int parentGoalId) {
        return goalDAO.findChildren(parentGoalId);
    }

    /**
     * Recomputes a goal's progress as the percentage of its directly
     * associated tasks that are COMPLETED, then persists it.
     *
     * Deliberately simple for Phase 5: it looks only at tasks whose
     * goal_id equals this goal (not tasks belonging to sub-goals or to
     * projects under this goal). A weighted roll-up across the full
     * hierarchy is a reasonable Phase 10+ (MomentaCore/Analytics)
     * enhancement, but keeping this version straightforward keeps it
     * easy to explain and verify at the viva.
     */
    public void recalculateProgress(Goal goal) {
        List<Task> allTasks = taskDAO.findAll(goal.getUserId());
        List<Task> linkedTasks = allTasks.stream()
                .filter(t -> goal.getId() == (t.getGoalId() == null ? -1 : t.getGoalId()))
                .toList();

        if (linkedTasks.isEmpty()) {
            return; // no linked tasks yet — leave progress as whatever it was (likely 0)
        }

        long completed = linkedTasks.stream().filter(Task::isCompleted).count();
        int percentage = (int) Math.round((completed * 100.0) / linkedTasks.size());

        goal.setProgress(percentage);
        goalDAO.update(goal);
    }

    /** Recalculates progress for every goal a user has — used when the Goals view loads. */
    public void recalculateAllProgress(int userId) {
        for (Goal goal : goalDAO.findAll(userId)) {
            recalculateProgress(goal);
        }
    }
}
