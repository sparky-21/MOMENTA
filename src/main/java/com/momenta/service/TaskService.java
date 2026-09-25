package com.momenta.service;

import com.momenta.dao.TaskDAO;
import com.momenta.dao.impl.TaskDAOImpl;
import com.momenta.engine.PriorityEngine;
import com.momenta.model.Task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
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

    public Task createTask(Task task) {
        recalculatePriority(task);
        return taskDAO.save(task);
    }

    public void updateTask(Task task) {
        recalculatePriority(task);
        taskDAO.update(task);
    }

    public void deleteTask(int id) {
        taskDAO.delete(id);
    }

    /** Marks a task complete, stamps completedAt, and persists it. */
    public void completeTask(Task task) {
        task.setStatus("COMPLETED");
        task.setProgress(100);
        task.setCompletedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        taskDAO.update(task);
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
     */
    public Task getMomentaNowRecommendation(int userId) {
        List<Task> incomplete = getIncompleteTasks(userId);
        incomplete.forEach(this::recalculatePriority);
        return incomplete.stream()
                .max(Comparator.comparingInt(Task::getPriorityScore))
                .orElse(null);
    }
}
