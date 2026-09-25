package com.momenta.dao.impl;

import com.momenta.dao.GoalDAO;
import com.momenta.database.DatabaseConnection;
import com.momenta.model.Goal;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GoalDAOImpl implements GoalDAO {

    @Override
    public Goal save(Goal goal) {
        String sql = """
            INSERT INTO goals (user_id, parent_goal_id, title, description, deadline,
                                progress, status, importance)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, goal.getUserId());
            setNullableInt(ps, 2, goal.getParentGoalId());
            ps.setString(3, goal.getTitle());
            ps.setString(4, goal.getDescription());
            ps.setString(5, goal.getDeadline());
            ps.setInt(6, goal.getProgress());
            ps.setString(7, goal.getStatus());
            ps.setInt(8, goal.getImportance());

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) goal.setId(keys.getInt(1));
            }
            return goal;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save goal: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Goal goal) {
        String sql = """
            UPDATE goals SET parent_goal_id = ?, title = ?, description = ?, deadline = ?,
                              progress = ?, status = ?, importance = ?
            WHERE id = ?
        """;
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            setNullableInt(ps, 1, goal.getParentGoalId());
            ps.setString(2, goal.getTitle());
            ps.setString(3, goal.getDescription());
            ps.setString(4, goal.getDeadline());
            ps.setInt(5, goal.getProgress());
            ps.setString(6, goal.getStatus());
            ps.setInt(7, goal.getImportance());
            ps.setInt(8, goal.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update goal " + goal.getId() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM goals WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete goal " + id + ": " + e.getMessage(), e);
        }
    }

    @Override
    public Goal findById(int id) {
        String sql = "SELECT * FROM goals WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find goal " + id + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<Goal> findAll(int userId) {
        return queryList("SELECT * FROM goals WHERE user_id = ? ORDER BY created_at DESC", userId);
    }

    @Override
    public List<Goal> findTopLevel(int userId) {
        return queryList("SELECT * FROM goals WHERE user_id = ? AND parent_goal_id IS NULL ORDER BY created_at DESC", userId);
    }

    @Override
    public List<Goal> findChildren(int parentGoalId) {
        String sql = "SELECT * FROM goals WHERE parent_goal_id = ? ORDER BY created_at DESC";
        List<Goal> goals = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, parentGoalId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) goals.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find children of goal " + parentGoalId + ": " + e.getMessage(), e);
        }
        return goals;
    }

    private List<Goal> queryList(String sql, int userId) {
        List<Goal> goals = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) goals.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query goals: " + e.getMessage(), e);
        }
        return goals;
    }

    private Goal mapRow(ResultSet rs) throws SQLException {
        Goal g = new Goal();
        g.setId(rs.getInt("id"));
        g.setUserId(rs.getInt("user_id"));
        g.setParentGoalId((Integer) rs.getObject("parent_goal_id"));
        g.setTitle(rs.getString("title"));
        g.setDescription(rs.getString("description"));
        g.setDeadline(rs.getString("deadline"));
        g.setProgress(rs.getInt("progress"));
        g.setStatus(rs.getString("status"));
        g.setImportance(rs.getInt("importance"));
        g.setCreatedAt(rs.getString("created_at"));
        return g;
    }

    private void setNullableInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) ps.setNull(index, Types.INTEGER);
        else ps.setInt(index, value);
    }
}
