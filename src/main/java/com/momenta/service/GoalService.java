package com.momenta.service;

import com.momenta.dao.GoalDAO;
import com.momenta.dao.impl.GoalDAOImpl;
import com.momenta.model.Goal;
import com.momenta.threading.AppExecutor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class GoalService {

    private final GoalDAO dao = new GoalDAOImpl();

    public void loadActiveAsync(Consumer<ObservableList<Goal>> onLoaded) {

        Task<List<Goal>> job = new Task<>() {
            @Override
            protected List<Goal> call() throws SQLException {
                return dao.findActive();
            }
        };

        job.setOnSucceeded(e ->
                onLoaded.accept(
                        FXCollections.observableArrayList(job.getValue())
                )
        );

        job.setOnFailed(e ->
                job.getException().printStackTrace()
        );

        AppExecutor.pool().execute(job);
    }

    public void loadAllAsync(Consumer<ObservableList<Goal>> onLoaded) {

        Task<List<Goal>> job = new Task<>() {
            @Override
            protected List<Goal> call() throws SQLException {
                return dao.findAll();
            }
        };

        job.setOnSucceeded(e ->
                onLoaded.accept(
                        FXCollections.observableArrayList(job.getValue())
                )
        );

        job.setOnFailed(e ->
                job.getException().printStackTrace()
        );

        AppExecutor.pool().execute(job);
    }

    public void createAsync(Goal goal, Consumer<Goal> onSaved) {

        Task<Goal> job = new Task<>() {
            @Override
            protected Goal call() throws SQLException {
                return dao.save(goal);
            }
        };

        job.setOnSucceeded(e ->
                onSaved.accept(job.getValue())
        );

        job.setOnFailed(e ->
                job.getException().printStackTrace()
        );

        AppExecutor.pool().execute(job);
    }

    public void updateAsync(Goal goal, Consumer<Boolean> onUpdated) {

        Task<Boolean> job = new Task<>() {
            @Override
            protected Boolean call() throws SQLException {
                return dao.update(goal);
            }
        };

        job.setOnSucceeded(e ->
                onUpdated.accept(job.getValue())
        );

        job.setOnFailed(e ->
                job.getException().printStackTrace()
        );

        AppExecutor.pool().execute(job);
    }

    public void deleteAsync(int id, Consumer<Boolean> onDeleted) {

        Task<Boolean> job = new Task<>() {
            @Override
            protected Boolean call() throws SQLException {
                return dao.delete(id);
            }
        };

        job.setOnSucceeded(e ->
                onDeleted.accept(job.getValue())
        );

        job.setOnFailed(e ->
                job.getException().printStackTrace()
        );

        AppExecutor.pool().execute(job);
    }

    public void findByIdAsync(
            int id,
            Consumer<Optional<Goal>> onFound
    ) {

        Task<Optional<Goal>> job = new Task<>() {
            @Override
            protected Optional<Goal> call() throws SQLException {
                return dao.findById(id);
            }
        };

        job.setOnSucceeded(e ->
                onFound.accept(job.getValue())
        );

        job.setOnFailed(e ->
                job.getException().printStackTrace()
        );

        AppExecutor.pool().execute(job);
    }
}