package com.momenta.dao.impl;

import com.momenta.dao.ProjectDAO;
import com.momenta.database.DatabaseConnection;
import com.momenta.model.Project;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProjectDAOImpl implements ProjectDAO {

    @Override
    public Project save(Project project) {
        String sql = """
            INSERT INTO projects (user_id, goal_id, title, description, deadline, status)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, project.getUserId());
            setNullableInt(ps, 2, project.getGoalId());
            ps.setString(3, project.getTitle());
            ps.setString(4, project.getDescription());
            ps.setString(5, project.getDeadline());
            ps.setString(6, project.getStatus());

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) project.setId(keys.getInt(1));
            }
            return project;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save project: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Project project) {
        String sql = """
            UPDATE projects SET goal_id = ?, title = ?, description = ?, deadline = ?, status = ?
            WHERE id = ?
        """;
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            setNullableInt(ps, 1, project.getGoalId());
            ps.setString(2, project.getTitle());
            ps.setString(3, project.getDescription());
            ps.setString(4, project.getDeadline());
            ps.setString(5, project.getStatus());
            ps.setInt(6, project.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update project " + project.getId() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM projects WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete project " + id + ": " + e.getMessage(), e);
        }
    }

    @Override
    public Project findById(int id) {
        String sql = "SELECT * FROM projects WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find project " + id + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<Project> findAll(int userId) {
        String sql = "SELECT * FROM projects WHERE user_id = ? ORDER BY created_at DESC";
        List<Project> projects = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) projects.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query projects: " + e.getMessage(), e);
        }
        return projects;
    }

    @Override
    public List<Project> findByGoal(int goalId) {
        String sql = "SELECT * FROM projects WHERE goal_id = ? ORDER BY created_at DESC";
        List<Project> projects = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, goalId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) projects.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find projects for goal " + goalId + ": " + e.getMessage(), e);
        }
        return projects;
    }

    private Project mapRow(ResultSet rs) throws SQLException {
        Project p = new Project();
        p.setId(rs.getInt("id"));
        p.setUserId(rs.getInt("user_id"));
        p.setGoalId((Integer) rs.getObject("goal_id"));
        p.setTitle(rs.getString("title"));
        p.setDescription(rs.getString("description"));
        p.setDeadline(rs.getString("deadline"));
        p.setStatus(rs.getString("status"));
        p.setProgress(0); // computed by ProjectService, not stored redundantly here
        p.setCreatedAt(rs.getString("created_at"));
        return p;
    }

    private void setNullableInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) ps.setNull(index, Types.INTEGER);
        else ps.setInt(index, value);
    }
}
