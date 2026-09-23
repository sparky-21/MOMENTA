package com.momenta.dao.impl;

import com.momenta.dao.GoalDAO;
import com.momenta.database.DatabaseManager;
import com.momenta.model.Goal;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GoalDAOImpl implements GoalDAO {

    private Connection conn() {
        return DatabaseManager.getInstance().getConnection();
    }

    @Override
    public Goal save(Goal goal) {
        String sql = """
                INSERT INTO goals
                (title, description, tier, parent_goal_id, deadline, progress, status)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps =
                     conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            bind(ps, goal);

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    goal.setId(keys.getInt(1));
                }
            }

            return goal;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save goal", e);
        }
    }

    @Override
    public boolean update(Goal goal) {
        String sql = """
                UPDATE goals
                SET title = ?,
                    description = ?,
                    tier = ?,
                    parent_goal_id = ?,
                    deadline = ?,
                    progress = ?,
                    status = ?
                WHERE id = ?
                """;

        try (PreparedStatement ps = conn().prepareStatement(sql)) {

            bind(ps, goal);
            ps.setInt(8, goal.getId());

            int affectedRows = ps.executeUpdate();

            return affectedRows > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to update goal " + goal.getId(), e
            );
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM goals WHERE id = ?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {

            ps.setInt(1, id);

            int affectedRows = ps.executeUpdate();

            return affectedRows > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to delete goal " + id, e
            );
        }
    }

    @Override
    public Optional<Goal> findById(int id) {
        String sql = "SELECT * FROM goals WHERE id = ?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to find goal " + id, e
            );
        }

        return Optional.empty();
    }

    @Override
    public List<Goal> findAll() {
        return query(
                "SELECT * FROM goals " +
                        "ORDER BY deadline IS NULL, deadline ASC",
                ps -> {}
        );
    }

    @Override
    public List<Goal> findActive() {
        return query(
                "SELECT * FROM goals " +
                        "WHERE status = 'ACTIVE' " +
                        "ORDER BY deadline IS NULL, deadline ASC",
                ps -> {}
        );
    }

    private interface Binder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    private List<Goal> query(String sql, Binder binder) {
        List<Goal> list = new ArrayList<>();

        try (PreparedStatement ps = conn().prepareStatement(sql)) {

            binder.bind(ps);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    list.add(map(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Goal query failed", e);
        }

        return list;
    }

    private void bind(PreparedStatement ps, Goal g) throws SQLException {

        ps.setString(1, g.getTitle());
        ps.setString(2, g.getDescription());
        ps.setString(3, g.getTier());

        if (g.getParentGoalId() != null) {
            ps.setInt(4, g.getParentGoalId());
        } else {
            ps.setNull(4, Types.INTEGER);
        }

        if (g.getDeadline() != null) {
            ps.setString(5, g.getDeadline().toString());
        } else {
            ps.setNull(5, Types.VARCHAR);
        }

        ps.setDouble(6, g.getProgress());
        ps.setString(7, g.getStatus());
    }

    private Goal map(ResultSet rs) throws SQLException {

        Goal g = new Goal();

        g.setId(rs.getInt("id"));
        g.setTitle(rs.getString("title"));
        g.setDescription(rs.getString("description"));
        g.setTier(rs.getString("tier"));

        int parent = rs.getInt("parent_goal_id");
        g.setParentGoalId(
                rs.wasNull() ? null : parent
        );

        String deadline = rs.getString("deadline");

        g.setDeadline(
                deadline != null
                        ? LocalDate.parse(deadline)
                        : null
        );

        g.setProgress(rs.getDouble("progress"));
        g.setStatus(rs.getString("status"));

        return g;
    }
}