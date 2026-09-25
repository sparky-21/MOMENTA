package com.momenta.service;

import com.momenta.dao.ProjectDAO;
import com.momenta.dao.TaskDAO;
import com.momenta.dao.impl.ProjectDAOImpl;
import com.momenta.dao.impl.TaskDAOImpl;
import com.momenta.model.Project;
import com.momenta.model.Task;

import java.util.List;

/**
 * ProjectService — Section 8.
 *
 * Unlike Goal (which persists a stored progress column), Project's schema
 * has no progress column at all (see DatabaseInitializer) — "Project
 * progress should be calculated dynamically" is taken literally here: it
 * is computed fresh every time from the linked tasks and never written to
 * the database, so it can never go stale.
 */
public class ProjectService {

    private final ProjectDAO projectDAO = new ProjectDAOImpl();
    private final TaskDAO taskDAO = new TaskDAOImpl();

    public Project createProject(Project project) {
        return projectDAO.save(project);
    }

    public void updateProject(Project project) {
        projectDAO.update(project);
    }

    public void deleteProject(int id) {
        projectDAO.delete(id);
    }

    /** Returns all of a user's projects with progress computed live from their tasks. */
    public List<Project> getAllProjects(int userId) {
        List<Project> projects = projectDAO.findAll(userId);
        List<Task> allTasks = taskDAO.findAll(userId);
        projects.forEach(p -> p.setProgress(computeProgress(p, allTasks)));
        return projects;
    }

    public List<Project> getProjectsForGoal(int goalId) {
        return projectDAO.findByGoal(goalId);
    }

    /** % of the project's linked tasks that are COMPLETED — 0 if it has none yet. */
    private int computeProgress(Project project, List<Task> allTasks) {
        List<Task> linkedTasks = allTasks.stream()
                .filter(t -> project.getId() == (t.getProjectId() == null ? -1 : t.getProjectId()))
                .toList();

        if (linkedTasks.isEmpty()) {
            return 0;
        }
        long completed = linkedTasks.stream().filter(Task::isCompleted).count();
        return (int) Math.round((completed * 100.0) / linkedTasks.size());
    }
}
