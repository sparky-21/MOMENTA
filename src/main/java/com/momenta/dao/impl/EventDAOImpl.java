package com.momenta.dao.impl;

import com.momenta.dao.EventDAO;
import com.momenta.database.DatabaseConnection;
import com.momenta.model.Event;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class EventDAOImpl implements EventDAO {

    @Override
    public Event save(Event event) {
        String sql = """
                INSERT INTO events
                (user_id, title, description, event_date, start_time, end_time)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, event.getUserId());
            ps.setString(2, event.getTitle());
            ps.setString(3, event.getDescription());
            ps.setString(4, event.getEventDate());
            ps.setString(5, event.getStartTime());
            ps.setString(6, event.getEndTime());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) event.setId(keys.getInt(1));
            }
            return event;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save event", e);
        }
    }

    @Override
    public void update(Event event) {
        String sql = """
                UPDATE events
                SET title = ?, description = ?, event_date = ?, start_time = ?, end_time = ?
                WHERE id = ? AND user_id = ?
                """;

        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, event.getTitle());
            ps.setString(2, event.getDescription());
            ps.setString(3, event.getEventDate());
            ps.setString(4, event.getStartTime());
            ps.setString(5, event.getEndTime());
            ps.setInt(6, event.getId());
            ps.setInt(7, event.getUserId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update event", e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM events WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete event", e);
        }
    }

    @Override
    public Event findById(int id) {
        String sql = "SELECT * FROM events WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find event", e);
        }
    }

    @Override
    public List<Event> findAll(int userId) {
        String sql = """
                SELECT * FROM events
                WHERE user_id = ?
                ORDER BY event_date ASC, start_time ASC
                """;
        List<Event> events = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) events.add(mapRow(rs));
            }
            return events;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load events", e);
        }
    }

    @Override
    public List<Event> findByDate(int userId, String isoDate) {
        String sql = """
                SELECT * FROM events
                WHERE user_id = ? AND event_date = ?
                ORDER BY start_time ASC
                """;
        List<Event> events = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, isoDate);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) events.add(mapRow(rs));
            }
            return events;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load events for date", e);
        }
    }

    private Event mapRow(ResultSet rs) throws SQLException {
        Event event = new Event();
        event.setId(rs.getInt("id"));
        event.setUserId(rs.getInt("user_id"));
        event.setTitle(rs.getString("title"));
        event.setDescription(rs.getString("description"));
        event.setEventDate(rs.getString("event_date"));
        event.setStartTime(rs.getString("start_time"));
        event.setEndTime(rs.getString("end_time"));
        event.setCreatedAt(rs.getString("created_at"));
        return event;
    }
}
