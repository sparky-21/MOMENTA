package com.momenta.dao;

import com.momenta.model.Project;
import java.sql.SQLException;
import java.util.List;

public interface ProjectDAO extends CrudDAO<Project> {
    List<Project> findActive() throws SQLException;
}
