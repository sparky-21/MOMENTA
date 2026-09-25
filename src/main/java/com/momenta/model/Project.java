package com.momenta.model;

import javafx.beans.property.*;

/**
 * Project model (Section 8).
 *
 * A project optionally belongs to a Goal (goalId nullable — the spec's
 * hierarchy is Life Goal -> Year Goal -> Monthly Goal -> Project -> Tasks,
 * but a project can also stand alone). Its progress, like Goal's, is not
 * hand-entered — ProjectService recalculates it from the completion state
 * of the tasks attached to it ("Project progress should be calculated
 * dynamically").
 */
public class Project {

    private final IntegerProperty id = new SimpleIntegerProperty(this, "id");
    private final IntegerProperty userId = new SimpleIntegerProperty(this, "userId", 1);
    private final ObjectProperty<Integer> goalId = new SimpleObjectProperty<>(this, "goalId");

    private final StringProperty title = new SimpleStringProperty(this, "title", "");
    private final StringProperty description = new SimpleStringProperty(this, "description", "");
    private final StringProperty deadline = new SimpleStringProperty(this, "deadline", "");
    private final StringProperty status = new SimpleStringProperty(this, "status", "ACTIVE");
    private final IntegerProperty progress = new SimpleIntegerProperty(this, "progress", 0);
    private final StringProperty createdAt = new SimpleStringProperty(this, "createdAt", "");

    public Project() {
    }

    // ----- id -----
    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    // ----- userId -----
    public int getUserId() { return userId.get(); }
    public void setUserId(int value) { userId.set(value); }
    public IntegerProperty userIdProperty() { return userId; }

    // ----- goalId (nullable) -----
    public Integer getGoalId() { return goalId.get(); }
    public void setGoalId(Integer value) { goalId.set(value); }
    public ObjectProperty<Integer> goalIdProperty() { return goalId; }

    // ----- title -----
    public String getTitle() { return title.get(); }
    public void setTitle(String value) { title.set(value); }
    public StringProperty titleProperty() { return title; }

    // ----- description -----
    public String getDescription() { return description.get(); }
    public void setDescription(String value) { description.set(value); }
    public StringProperty descriptionProperty() { return description; }

    // ----- deadline -----
    public String getDeadline() { return deadline.get(); }
    public void setDeadline(String value) { deadline.set(value); }
    public StringProperty deadlineProperty() { return deadline; }

    // ----- status -----
    public String getStatus() { return status.get(); }
    public void setStatus(String value) { status.set(value); }
    public StringProperty statusProperty() { return status; }

    // ----- progress (0-100, auto-calculated by ProjectService) -----
    public int getProgress() { return progress.get(); }
    public void setProgress(int value) { progress.set(Math.max(0, Math.min(100, value))); }
    public IntegerProperty progressProperty() { return progress; }

    // ----- createdAt -----
    public String getCreatedAt() { return createdAt.get(); }
    public void setCreatedAt(String value) { createdAt.set(value); }
    public StringProperty createdAtProperty() { return createdAt; }

    @Override
    public String toString() {
        return title.get();
    }
}
