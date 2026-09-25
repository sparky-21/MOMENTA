package com.momenta.dao;

import com.momenta.model.Project;
import java.util.List;

public interface ProjectDAO {
    Project save(Project project);
    void update(Project project);
    void delete(int id);
    Project findById(int id);
    List<Project> findAll(int userId);
    List<Project> findByGoal(int goalId);
}
