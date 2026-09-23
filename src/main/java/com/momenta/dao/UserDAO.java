package com.momenta.dao;

import com.momenta.model.User;
import java.sql.SQLException;
import java.util.Optional;

public interface UserDAO extends CrudDAO<User> {
    Optional<User> findByEmail(String email) throws SQLException;
}
