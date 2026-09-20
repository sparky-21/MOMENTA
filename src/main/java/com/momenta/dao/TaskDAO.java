package com.momenta.dao;

import com.momenta.model.Task;

import java.util.List;
import java.util.Optional;

/**
 * DAO contract for Task persistence.
 *
 * The Controller and Service layers only ever talk to this interface, never
 * to a concrete JDBC class directly. That means:
 *   - We could swap SQLite for another SQL database later by writing a new
 *     implementation, with zero changes to Service/Controller code.
 *   - We can write tests against a fake in-memory TaskDAO without touching
 *     a real database.
 */
public interface TaskDAO {
    Task save(Task task);
    void update(Task task);
    void delete(int taskId);
    Optional<Task> findById(int taskId);
    List<Task> findAll();
    List<Task> findIncomplete();
    List<Task> findByDate(java.time.LocalDate date);
}
