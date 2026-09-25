package com.momenta.dao;

import com.momenta.model.Goal;
import java.util.List;

public interface GoalDAO {
    Goal save(Goal goal);
    void update(Goal goal);
    void delete(int id);
    Goal findById(int id);
    List<Goal> findAll(int userId);
    List<Goal> findTopLevel(int userId);      // parentGoalId IS NULL — "Life Goals"
    List<Goal> findChildren(int parentGoalId); // sub-goals of a given goal
}
