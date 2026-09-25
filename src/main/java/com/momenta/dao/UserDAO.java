package com.momenta.dao;

import com.momenta.model.User;

public interface UserDAO {
    User save(User user);
    User findByUsername(String username);
    boolean usernameExists(String username);
}
