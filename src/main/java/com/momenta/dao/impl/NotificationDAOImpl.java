package com.momenta.dao.impl;

import com.momenta.dao.NotificationDAO;
import com.momenta.database.DatabaseConnection;
import com.momenta.model.Notification;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** JDBC implementation for notifications. */
public class NotificationDAOImpl implements NotificationDAO {

    @Override
    public Notification save(Notification notification) {
        String sql = "INSERT INTO notifications (user_id, type, message, is_read, created_at) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, notification.getUserId());
            ps.setString(2, notification.getType());
            ps.setString(3, notification.getMessage());
            ps.setInt(4, notification.isRead() ? 1 : 0);
            ps.setString(5, notification.getCreatedAt() == null || notification.getCreatedAt().isBlank()
                    ? LocalDateTime.now().toString() : notification.getCreatedAt());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) notification.setId(keys.getInt(1));
            }
            return notification;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save notification: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Notification> findAll(int userId) {
        return query("SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC, id DESC", userId);
    }

    @Override
    public List<Notification> findUnread(int userId) {
        return query("SELECT * FROM notifications WHERE user_id = ? AND is_read = 0 ORDER BY created_at DESC, id DESC", userId);
    }

    @Override
    public int countUnread(int userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = 0";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count unread notifications: " + e.getMessage(), e);
        }
    }

    @Override
    public void markAsRead(int notificationId) {
        updateRead("UPDATE notifications SET is_read = 1 WHERE id = ?", notificationId);
    }

    @Override
    public void markAllAsRead(int userId) {
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement("UPDATE notifications SET is_read = 1 WHERE user_id = ?")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to mark notifications as read: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsRecent(int userId, String type, String message) {
        String sql = """
                SELECT COUNT(*) FROM notifications
                WHERE user_id = ? AND type = ? AND message = ?
                  AND created_at >= datetime('now', '-1 day')
                """;
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, type);
            ps.setString(3, message);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check recent notification: " + e.getMessage(), e);
        }
    }

    private void updateRead(String sql, int id) {
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update notification: " + e.getMessage(), e);
        }
    }

    private List<Notification> query(String sql, int userId) {
        List<Notification> result = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query notifications: " + e.getMessage(), e);
        }
        return result;
    }

    private Notification mapRow(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setId(rs.getInt("id"));
        n.setUserId(rs.getInt("user_id"));
        n.setType(rs.getString("type"));
        n.setMessage(rs.getString("message"));
        n.setRead(rs.getInt("is_read") == 1);
        n.setCreatedAt(rs.getString("created_at"));
        return n;
    }
}
