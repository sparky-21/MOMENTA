package com.momenta.service;

import com.momenta.dao.UserDAO;
import com.momenta.dao.impl.UserDAOImpl;
import com.momenta.model.User;
import com.momenta.utility.PasswordUtil;

public class UserService {
    private final UserDAO userDAO = new UserDAOImpl();

    public User register(String name, String username, String password, String persona) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name cannot be empty.");
        if (username == null || username.isBlank()) throw new IllegalArgumentException("Username cannot be empty.");
        if (username.trim().length() < 3) throw new IllegalArgumentException("Username must contain at least 3 characters.");
        if (password == null || password.length() < 6) throw new IllegalArgumentException("Password must contain at least 6 characters.");
        if (persona == null || persona.isBlank()) throw new IllegalArgumentException("Please select a profession.");

        username = username.trim();
        if (userDAO.usernameExists(username)) throw new IllegalArgumentException("Username already exists.");

        User user = new User(name.trim(), username, PasswordUtil.hash(password), persona.trim());
        return userDAO.save(user);
    }

    public User login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Username and password are required.");
        }
        User user = userDAO.findByUsername(username.trim());
        if (user == null || !PasswordUtil.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password.");
        }
        return user;
    }
}
