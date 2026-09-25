package com.momenta.dao.impl;

import com.momenta.dao.TaskDAO;
import com.momenta.database.DatabaseConnection;
import com.momenta.model.Task;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of TaskDAO (Section 19).
 *
 * Rules followed here:
 *   - Every query is a PreparedStatement; user input is NEVER concatenated
 *     into SQL text (prevents SQL injection).
 *   - Every Connection-derived resource (Statement/ResultSet) is opened in
 *     try-with-resources so it is closed even if an exception is thrown.
 *   - SQLException is wrapped in an unchecked RuntimeException at this
 *     boundary so Service/Controller code isn't forced to catch a
 *     JDBC-specific checked exception — they instead handle domain errors.
 */
public class TaskDAOImpl implements TaskDAO {

    @Override
    public Task save(Task task) {
        String sql = """
            INSERT INTO tasks (user_id, project_id, goal_id, title, description, category,
                                importance, deadline, estimated_minutes, progress, status, priority_score)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, task.getUserId());
            setNullableInt(ps, 2, task.getProjectId());
            setNullableInt(ps, 3, task.getGoalId());
            ps.setString(4, task.getTitle());
            ps.setString(5, task.getDescription());
            ps.setString(6, task.getCategory());
            ps.setInt(7, task.getImportance());
            ps.setString(8, task.getDeadline());
            ps.setInt(9, task.getEstimatedMinutes());
            ps.setInt(10, task.getProgress());
            ps.setString(11, task.getStatus());
            ps.setInt(12, task.getPriorityScore());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    task.setId(keys.getInt(1));
                }
            }
            return task;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save task: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Task task) {
        String sql = """
            UPDATE tasks SET project_id = ?, goal_id = ?, title = ?, description = ?,
                              category = ?, importance = ?, deadline = ?, estimated_minutes = ?,
                              progress = ?, status = ?, priority_score = ?,
                              completed_at = ?
            WHERE id = ?
        """;
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            setNullableInt(ps, 1, task.getProjectId());
            setNullableInt(ps, 2, task.getGoalId());
            ps.setString(3, task.getTitle());
            ps.setString(4, task.getDescription());
            ps.setString(5, task.getCategory());
            ps.setInt(6, task.getImportance());
            ps.setString(7, task.getDeadline());
            ps.setInt(8, task.getEstimatedMinutes());
            ps.setInt(9, task.getProgress());
            ps.setString(10, task.getStatus());
            ps.setInt(11, task.getPriorityScore());
            ps.setString(12, task.getCompletedAt());
            ps.setInt(13, task.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update task " + task.getId() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete task " + id + ": " + e.getMessage(), e);
        }
    }

    @Override
    public Task findById(int id) {
        String sql = "SELECT * FROM tasks WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find task " + id + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<Task> findAll(int userId) {
        String sql = "SELECT * FROM tasks WHERE user_id = ? ORDER BY created_at DESC";
        return queryList(sql, userId);
    }

    @Override
    public List<Task> findIncomplete(int userId) {
        String sql = "SELECT * FROM tasks WHERE user_id = ? AND status != 'COMPLETED' ORDER BY deadline ASC";
        return queryList(sql, userId);
    }

    @Override
    public List<Task> findByDate(int userId, String isoDate) {
        String sql = "SELECT * FROM tasks WHERE user_id = ? AND deadline = ? ORDER BY importance DESC";
        List<Task> tasks = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, isoDate);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) tasks.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find tasks by date: " + e.getMessage(), e);
        }
        return tasks;
    }

    // ---------- helpers ----------

    private List<Task> queryList(String sql, int userId) {
        List<Task> tasks = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) tasks.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query tasks: " + e.getMessage(), e);
        }
        return tasks;
    }

    private Task mapRow(ResultSet rs) throws SQLException {
        Task t = new Task();
        t.setId(rs.getInt("id"));
        t.setUserId(rs.getInt("user_id"));
        t.setProjectId((Integer) rs.getObject("project_id"));
        t.setGoalId((Integer) rs.getObject("goal_id"));
        t.setTitle(rs.getString("title"));
        t.setDescription(rs.getString("description"));
        t.setCategory(rs.getString("category"));
        t.setImportance(rs.getInt("importance"));
        t.setDeadline(rs.getString("deadline"));
        t.setEstimatedMinutes(rs.getInt("estimated_minutes"));
        t.setProgress(rs.getInt("progress"));
        t.setStatus(rs.getString("status"));
        t.setPriorityScore(rs.getInt("priority_score"));
        t.setCreatedAt(rs.getString("created_at"));
        t.setCompletedAt(rs.getString("completed_at"));
        return t;
    }

    private void setNullableInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }
}
