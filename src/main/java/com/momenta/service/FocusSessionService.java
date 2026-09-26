package com.momenta.service;

import com.momenta.dao.FocusSessionDAO;
import com.momenta.dao.impl.FocusSessionDAOImpl;
import com.momenta.model.FocusSession;
import com.momenta.model.Task;

import java.time.LocalDateTime;
import java.util.List;

/** Business layer for Focus Mode. */
public class FocusSessionService {
    private final FocusSessionDAO dao = new FocusSessionDAOImpl();
    private final TaskService taskService = new TaskService();
    private final NotificationService notificationService = new NotificationService();

    public FocusSession start(int userId, Task task, int durationMinutes) {
        if (durationMinutes <= 0) throw new IllegalArgumentException("Duration must be greater than zero.");
        return dao.save(FocusSession.start(userId, task == null ? null : task.getId(), durationMinutes));
    }

    public void finish(FocusSession session, Task task) {
        dao.finish(session.getId(), LocalDateTime.now().toString());
        notificationService.create(session.getUserId(), "FOCUS",
                task == null ? "Focus session completed."
                        : "Focus session completed for \"" + task.getTitle() + "\".");
        if (task != null && !"COMPLETED".equals(task.getStatus())) {
            int newProgress = Math.min(100, task.getProgress() + progressForSession(task, session.getDurationMinutes()));
            task.setProgress(newProgress);
            if (newProgress >= 100) task.setStatus("COMPLETED");
            taskService.updateTask(task);
        }
    }

    private int progressForSession(Task task, int minutes) {
        int estimate = Math.max(1, task.getEstimatedMinutes());
        return Math.max(1, (int) Math.round(minutes * 100.0 / estimate));
    }

    public List<FocusSession> getHistory(int userId) {
        return dao.findAll(userId);
    }
}
