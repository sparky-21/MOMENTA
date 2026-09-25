package com.momenta.dao.impl;

import com.momenta.dao.FocusSessionDAO;
import com.momenta.database.DatabaseConnection;
import com.momenta.model.FocusSession;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** JDBC persistence for Focus Mode. */
public class FocusSessionDAOImpl implements FocusSessionDAO {
    @Override
    public FocusSession save(FocusSession session) {
        String sql = "INSERT INTO focus_sessions " +
                "(user_id, task_id, duration_minutes, started_at, ended_at) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, session.getUserId());
            if (session.getTaskId() == null) ps.setNull(2, Types.INTEGER);
            else ps.setInt(2, session.getTaskId());
            ps.setInt(3, session.getDurationMinutes());
            ps.setString(4, session.getStartedAt());
            ps.setString(5, session.getEndedAt());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) session.setId(keys.getInt(1));
            }
            return session;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save focus session: " + e.getMessage(), e);
        }
    }

    @Override
    public void finish(int id, String endedAt) {
        String sql = "UPDATE focus_sessions SET ended_at = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, endedAt);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to finish focus session: " + e.getMessage(), e);
        }
    }

    @Override
    public List<FocusSession> findAll(int userId) {
        return query("SELECT * FROM focus_sessions WHERE user_id = ? ORDER BY started_at DESC", userId);
    }

    @Override
    public List<FocusSession> findByTask(int taskId) {
        return query("SELECT * FROM focus_sessions WHERE task_id = ? ORDER BY started_at DESC", taskId);
    }

    private List<FocusSession> query(String sql, int id) {
        List<FocusSession> result = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    FocusSession s = new FocusSession();
                    s.setId(rs.getInt("id"));
                    s.setUserId(rs.getInt("user_id"));
                    s.setTaskId((Integer) rs.getObject("task_id"));
                    s.setDurationMinutes(rs.getInt("duration_minutes"));
                    s.setStartedAt(rs.getString("started_at"));
                    s.setEndedAt(rs.getString("ended_at"));
                    result.add(s);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query focus sessions: " + e.getMessage(), e);
        }
        return result;
    }
}
