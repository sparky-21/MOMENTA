package com.momenta.dao;

import com.momenta.model.Goal;
import java.sql.SQLException;
import java.util.List;

public interface GoalDAO extends CrudDAO<Goal> {
    List<Goal> findActive() throws SQLException;
}
