package com.momenta.service;

import com.momenta.dao.TaskDAO;
import com.momenta.dao.impl.TaskDAOImpl;
import com.momenta.engine.PriorityEngine;
import com.momenta.engine.MomentaCore;
import com.momenta.model.Goal;
import com.momenta.model.Task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * TaskService — sits between TaskController and TaskDAO (Section 14 MVC chain:
 * UI -> Controller -> Service -> Engine -> DAO -> Database).
 *
 * This is where business rules live that are NOT pure persistence (DAO's job)
 * and NOT UI concerns (Controller's job): recalculating priority scores,
 * marking completion timestamps, deciding what "incomplete" or "today's
 * tasks" means.
 */
public class TaskService {

    private final TaskDAO taskDAO = new TaskDAOImpl();
    private final NotificationService notificationService = new NotificationService();
    private final GoalService goalService = new GoalService();

    public Task createTask(Task task) {
        recalculatePriority(task);
        Task saved = taskDAO.save(task);
        recalculateLinkedGoal(saved);
        return saved;
    }

    public void updateTask(Task task) {
        recalculatePriority(task);
        taskDAO.update(task);
        recalculateLinkedGoal(task);
    }

    public void deleteTask(int id) {
        // Fetched before delete so a goal linked to this task still gets its
        // progress recalculated afterwards (otherwise a deleted task would
        // leave a stale percentage on its goal until the Goals screen is
        // reopened).
        Task existing = taskDAO.findById(id);
        taskDAO.delete(id);
        if (existing != null) {
            recalculateLinkedGoal(existing);
        }
    }

    /** Marks a task complete, stamps completedAt, and persists it. */
    public void completeTask(Task task) {
        task.setStatus("COMPLETED");
        task.setProgress(100);
        task.setCompletedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        taskDAO.update(task);
        recalculateLinkedGoal(task);
        notificationService.create(task.getUserId(), "TASK",
                "Task \"" + task.getTitle() + "\" completed.");
    }

    /**
     * Section 7 / Section 36: a goal's progress must reflect its linked
     * tasks' completion the moment a task changes — not only the next time
     * the Goals screen happens to be opened (GoalController used to be the
     * only place recalculateAllProgress() was called from). Every write
     * path above now routes through here so Dashboard, Focus Mode and the
     * Task screen all keep goal progress current.
     */
    private void recalculateLinkedGoal(Task task) {
        if (task == null || task.getGoalId() == null) return;
        Goal goal = goalService.getGoal(task.getGoalId());
        if (goal != null) {
            goalService.recalculateProgress(goal);
        }
    }

    public List<Task> getAllTasks(int userId) {
        return taskDAO.findAll(userId);
    }

    public List<Task> getIncompleteTasks(int userId) {
        return taskDAO.findIncomplete(userId);
    }

    public List<Task> getTasksForDate(int userId, String isoDate) {
        return taskDAO.findByDate(userId, isoDate);
    }

    /** Recomputes and stores the deterministic priority score (Section 15). */
    public void recalculatePriority(Task task) {
        PriorityEngine.PriorityResult result = PriorityEngine.score(task);
        task.setPriorityScore(result.getScore());
    }

    /**
     * MOMENTA NOW (Section 16): ranks all incomplete tasks by priority score
     * and returns the single best candidate to focus on right now, or null
     * if there is nothing incomplete.
     *
     * Returns the full Recommendation (task + score + reasons) rather than
     * just the Task. Previously only the Task came back here, and the
     * Dashboard recomputed "reasons" itself with PriorityEngine.score(task)
     * using default goalImportance=0/workloadCount=0 — so a task boosted by
     * an important goal or a heavy workload showed a priority number that
     * included those factors but a reasons list that never mentioned them.
     * Returning the same result RecommendationService already computed
     * keeps the displayed score and its explanation consistent (Section 15
     * requires the recommendation to be explainable).
     */
    public MomentaCore.Recommendation getMomentaNowRecommendation(int userId) {
        return new MomentaCore().recommend(userId);
    }
}
