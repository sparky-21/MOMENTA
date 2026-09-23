package com.momenta.service;

import com.momenta.dao.ProjectDAO;
import com.momenta.dao.impl.ProjectDAOImpl;
import com.momenta.model.Project;

import java.sql.SQLException;
import java.util.List;

public class ProjectService {

    private final ProjectDAO projectDAO = new ProjectDAOImpl();

    public Project create(Project project) throws SQLException {
        validate(project);
        return projectDAO.save(project);
    }

    public boolean update(Project project) throws SQLException {
        validate(project);
        return projectDAO.update(project);
    }

    public boolean delete(int id) throws SQLException {
        return projectDAO.delete(id);
    }

    public List<Project> getAll() throws SQLException {
        return projectDAO.findAll();
    }

    public List<Project> getActive() throws SQLException {
        return projectDAO.findActive();
    }

    private void validate(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("Project is required.");
        }
        if (project.getTitle() == null || project.getTitle().isBlank()) {
            throw new IllegalArgumentException("Project title is required.");
        }
    }
}
