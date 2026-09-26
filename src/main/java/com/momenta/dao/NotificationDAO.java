package com.momenta.dao;

import com.momenta.model.Notification;
import java.util.List;

public interface NotificationDAO {
    Notification save(Notification notification);
    List<Notification> findAll(int userId);
    List<Notification> findUnread(int userId);
    int countUnread(int userId);
    void markAsRead(int notificationId);
    void markAllAsRead(int userId);
    boolean existsRecent(int userId, String type, String message);
}
