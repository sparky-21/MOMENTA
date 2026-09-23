package com.momenta.dao.impl;

import com.momenta.dao.ProjectDAO;
import com.momenta.database.DatabaseManager;
import com.momenta.model.Project;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProjectDAOImpl implements ProjectDAO {

    private Connection conn() {
        return DatabaseManager.getInstance().getConnection();
    }

    @Override
    public Project save(Project project) {
        String sql = """
                INSERT INTO projects(title, description, goal_id, deadline, progress, status)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, project);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) project.setId(keys.getInt(1));
            }
            return project;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save project", e);
        }
    }

    @Override
    public boolean update(Project project) {
        String sql = """
                UPDATE projects
                SET title = ?, description = ?, goal_id = ?, deadline = ?, progress = ?, status = ?
                WHERE id = ?
                """;

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            bind(ps, project);
            ps.setInt(7, project.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update project " + project.getId(), e);
        }
    }

    @Override
    public boolean delete(int id) {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM projects WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete project " + id, e);
        }
    }

    @Override
    public Optional<Project> findById(int id) {
        return findOne("SELECT * FROM projects WHERE id = ?", ps -> ps.setInt(1, id));
    }

    @Override
    public List<Project> findAll() {
        return query("SELECT * FROM projects ORDER BY deadline IS NULL, deadline ASC");
    }

    @Override
    public List<Project> findActive() {
        return query("SELECT * FROM projects WHERE status = 'ACTIVE' ORDER BY deadline IS NULL, deadline ASC");
    }

    private List<Project> query(String sql) {
        List<Project> projects = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) projects.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Project query failed", e);
        }
        return projects;
    }

    private interface Binder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    private Optional<Project> findOne(String sql, Binder binder) {
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Project query failed", e);
        }
        return Optional.empty();
    }

    private void bind(PreparedStatement ps, Project project) throws SQLException {
        ps.setString(1, project.getTitle());
        ps.setString(2, project.getDescription());
        if (project.getGoalId() == null) ps.setNull(3, Types.INTEGER);
        else ps.setInt(3, project.getGoalId());
        if (project.getDeadline() == null) ps.setNull(4, Types.VARCHAR);
        else ps.setString(4, project.getDeadline().toString());
        ps.setDouble(5, project.getProgress());
        ps.setString(6, project.getStatus());
    }

    private Project map(ResultSet rs) throws SQLException {
        Project project = new Project();
        project.setId(rs.getInt("id"));
        project.setTitle(rs.getString("title"));
        project.setDescription(rs.getString("description"));

        int goalId = rs.getInt("goal_id");
        project.setGoalId(rs.wasNull() ? null : goalId);

        String deadline = rs.getString("deadline");
        project.setDeadline(deadline == null ? null : LocalDate.parse(deadline));
        project.setProgress(rs.getDouble("progress"));
        project.setStatus(rs.getString("status"));
        return project;
    }
}
