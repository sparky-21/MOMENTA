package com.momenta.dao.impl;

import com.momenta.dao.UserDAO;
import com.momenta.database.DatabaseManager;
import com.momenta.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAOImpl implements UserDAO {

    private Connection conn() {
        return DatabaseManager.getInstance().getConnection();
    }

    @Override
    public User save(User user) {
        String sql = """
                INSERT INTO users(name, email, password, profile_type)
                VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getProfileType());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getInt(1));
                }
            }

            return user;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save user", e);
        }
    }

    @Override
    public boolean update(User user) {
        String sql = """
                UPDATE users
                SET name = ?, email = ?, password = ?, profile_type = ?
                WHERE id = ?
                """;

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getProfileType());
            ps.setInt(5, user.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user " + user.getId(), e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete user " + id, e);
        }
    }

    @Override
    public Optional<User> findById(int id) {
        return findOne("SELECT * FROM users WHERE id = ?", ps -> ps.setInt(1, id));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return findOne("SELECT * FROM users WHERE email = ?", ps -> ps.setString(1, email));
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY id";

        try (PreparedStatement ps = conn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("User query failed", e);
        }

        return users;
    }

    private interface Binder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    private Optional<User> findOne(String sql, Binder binder) {
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            binder.bind(ps);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("User query failed", e);
        }

        return Optional.empty();
    }

    private User map(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setProfileType(rs.getString("profile_type"));
        return user;
    }
}
