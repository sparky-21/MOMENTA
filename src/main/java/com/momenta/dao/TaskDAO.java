package com.momenta.dao;

import com.momenta.model.Task;
import java.util.List;

/**
 * Contract for Task persistence (Section 20 — DAO Architecture).
 *
 * Nothing above this interface (Service, Controller, UI) is allowed to know
 * that SQLite exists. If MOMENTA ever migrated to PostgreSQL, only
 * TaskDAOImpl would change — TaskService and TaskController would not.
 */
public interface TaskDAO {
    Task save(Task task);
    void update(Task task);
    void delete(int id);
    Task findById(int id);
    List<Task> findAll(int userId);
    List<Task> findIncomplete(int userId);
    List<Task> findByDate(int userId, String isoDate);
}
