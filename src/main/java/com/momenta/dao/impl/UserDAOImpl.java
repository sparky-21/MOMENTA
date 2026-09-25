package com.momenta.dao.impl;

import com.momenta.dao.UserDAO;
import com.momenta.database.DatabaseConnection;
import com.momenta.model.User;

import java.sql.*;

public class UserDAOImpl implements UserDAO {
    @Override
    public User save(User user) {
        String sql = "INSERT INTO users(name, username, password_hash, persona) VALUES(?,?,?,?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getPersona());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) user.setId(rs.getInt(1));
            }
            return user;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save user", e);
        }
    }

    @Override
    public User findByUsername(String username) {
        String sql = "SELECT id,name,username,password_hash,persona FROM users WHERE username=?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setName(rs.getString("name"));
                u.setUsername(rs.getString("username"));
                u.setPasswordHash(rs.getString("password_hash"));
                u.setPersona(rs.getString("persona"));
                return u;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user", e);
        }
    }

    @Override
    public boolean usernameExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username=? LIMIT 1";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check username", e);
        }
    }
}
