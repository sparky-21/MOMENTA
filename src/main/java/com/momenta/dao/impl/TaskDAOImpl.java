package com.momenta.dao.impl;

import com.momenta.dao.TaskDAO;
import com.momenta.database.DatabaseManager;
import com.momenta.model.Task;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SQLite implementation of TaskDAO.
 *
 * Every method:
 *   1. Opens a PreparedStatement (never a raw Statement with concatenated
 *      user input — that's how SQL injection happens).
 *   2. Binds parameters with setX(index, value).
 *   3. Uses try-with-resources so the Statement/ResultSet always closes,
 *      even if an exception is thrown.
 *
 * Note this class runs on whatever thread calls it. It is NOT itself
 * thread-aware — TaskService is the layer responsible for pushing these
 * calls onto a background thread so the JavaFX Application Thread never
 * blocks on a query. Keeping that separation is why DAO code stays simple.
 */
public class TaskDAOImpl implements TaskDAO {

    private Connection conn() {
        return DatabaseManager.getInstance().getConnection();
    }

    @Override
    public Task save(Task task) {
        String sql = """
            INSERT INTO tasks (title, description, category, importance,
                                deadline, estimated_minutes, progress, status,
                                project_id, goal_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindTaskFields(ps, task);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    task.setId(keys.getInt(1));
                }
            }
            return task;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save task", e);
        }
    }

    @Override
    public void update(Task task) {
        String sql = """
            UPDATE tasks SET title = ?, description = ?, category = ?,
                              importance = ?, deadline = ?, estimated_minutes = ?,
                              progress = ?, status = ?, project_id = ?, goal_id = ?,
                              completed_at = CASE WHEN ? = 'DONE' THEN CURRENT_TIMESTAMP ELSE completed_at END
            WHERE id = ?
            """;
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            bindTaskFields(ps, task);
            ps.setString(11, task.getStatus());
            ps.setInt(12, task.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update task " + task.getId(), e);
        }
    }

    @Override
    public void delete(int taskId) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, taskId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete task " + taskId, e);
        }
    }

    @Override
    public Optional<Task> findById(int taskId) {
        String sql = "SELECT * FROM tasks WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, taskId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find task " + taskId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Task> findAll() {
        String sql = "SELECT * FROM tasks ORDER BY deadline IS NULL, deadline ASC";
        return runListQuery(sql, ps -> {});
    }

    @Override
    public List<Task> findIncomplete() {
        String sql = "SELECT * FROM tasks WHERE status != 'DONE' ORDER BY deadline IS NULL, deadline ASC";
        return runListQuery(sql, ps -> {});
    }

    @Override
    public List<Task> findByDate(LocalDate date) {
        String sql = "SELECT * FROM tasks WHERE date(deadline) = date(?)";
        return runListQuery(sql, ps -> ps.setString(1, date.toString()));
    }

    // --- helpers -----------------------------------------------------

    private interface Binder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    private List<Task> runListQuery(String sql, Binder binder) {
        List<Task> results = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Task query failed: " + sql, e);
        }
        return results;
    }

    private void bindTaskFields(PreparedStatement ps, Task task) throws SQLException {
        ps.setString(1, task.getTitle());
        ps.setString(2, task.getDescription());
        ps.setString(3, task.getCategory());
        ps.setInt(4, task.getImportance());
        ps.setString(5, task.getDeadline() != null ? task.getDeadline().toString() : null);
        ps.setInt(6, task.getEstimatedMinutes());
        ps.setDouble(7, task.getProgress());
        ps.setString(8, task.getStatus());
        if (task.getProjectId() != null) ps.setInt(9, task.getProjectId()); else ps.setNull(9, Types.INTEGER);
        if (task.getGoalId() != null) ps.setInt(10, task.getGoalId()); else ps.setNull(10, Types.INTEGER);
    }

    private Task mapRow(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setId(rs.getInt("id"));
        task.setTitle(rs.getString("title"));
        task.setDescription(rs.getString("description"));
        task.setCategory(rs.getString("category"));
        task.setImportance(rs.getInt("importance"));
        String deadlineStr = rs.getString("deadline");
        task.setDeadline(deadlineStr != null ? LocalDateTime.parse(deadlineStr) : null);
        task.setEstimatedMinutes(rs.getInt("estimated_minutes"));
        task.setProgress(rs.getDouble("progress"));
        task.setStatus(rs.getString("status"));
        int projectId = rs.getInt("project_id");
        task.setProjectId(rs.wasNull() ? null : projectId);
        int goalId = rs.getInt("goal_id");
        task.setGoalId(rs.wasNull() ? null : goalId);
        return task;
    }
}
