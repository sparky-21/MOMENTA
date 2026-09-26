package com.momenta.service;

import com.momenta.dao.NotificationDAO;
import com.momenta.dao.impl.NotificationDAOImpl;
import com.momenta.model.Notification;
import com.momenta.model.Task;
import com.momenta.dao.TaskDAO;
import com.momenta.dao.impl.TaskDAOImpl;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/** Business layer for Phase 14 notifications and deadline monitoring. */
public class NotificationService {
    private final NotificationDAO dao = new NotificationDAOImpl();
    private final TaskDAO taskDAO = new TaskDAOImpl();

    public Notification create(int userId, String type, String message) {
        return dao.save(new Notification(userId, type, message));
    }

    public List<Notification> getAll(int userId) { return dao.findAll(userId); }
    public List<Notification> getUnread(int userId) { return dao.findUnread(userId); }
    public int getUnreadCount(int userId) { return dao.countUnread(userId); }
    public void markAsRead(int notificationId) { dao.markAsRead(notificationId); }
    public void markAllAsRead(int userId) { dao.markAllAsRead(userId); }

    /** Creates at most one matching deadline warning per 24 hours. */
    public void checkUpcomingDeadlines(int userId) {
        LocalDate today = LocalDate.now();
        for (Task task : taskDAO.findIncomplete(userId)) {
            String deadline = task.getDeadline();
            if (deadline == null || deadline.isBlank()) continue;

            try {
                LocalDate date = LocalDate.parse(deadline);
                long days = ChronoUnit.DAYS.between(today, date);
                if (days < 0 || days > 1) continue;

                String message = days == 0
                        ? "Deadline today: \"" + task.getTitle() + "\"."
                        : "Deadline tomorrow: \"" + task.getTitle() + "\".";

                if (!dao.existsRecent(userId, "DEADLINE", message)) {
                    create(userId, "DEADLINE", message);
                }
            } catch (Exception ignored) {
                // Invalid legacy deadline values are ignored by the scheduler.
            }
        }
    }
}
