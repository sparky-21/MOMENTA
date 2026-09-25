package com.momenta.model;

import javafx.beans.property.*;

/**
 * Goal model (Section 7).
 *
 * Supports the hierarchy Life Goal -> Year Goal -> Monthly Goal -> Project -> Tasks
 * via the self-referencing parentGoalId: a goal with parentGoalId == null is a
 * top-level (Life) goal; a goal whose parentGoalId points at another goal is
 * one level down. The "level" itself isn't stored as an enum — it's implied
 * by how deep the parent chain goes, which keeps the schema simple and lets
 * a user nest goals as shallow or as deep as they actually want.
 *
 * progress is NOT free-typed by the user in the common case — GoalService
 * recalculates it from the completion of associated tasks (Section 7:
 * "goal progress should automatically reflect associated task completion").
 * It stays a plain settable property here so the service layer can write it.
 */
public class Goal {

    private final IntegerProperty id = new SimpleIntegerProperty(this, "id");
    private final IntegerProperty userId = new SimpleIntegerProperty(this, "userId", 1);
    private final ObjectProperty<Integer> parentGoalId = new SimpleObjectProperty<>(this, "parentGoalId");

    private final StringProperty title = new SimpleStringProperty(this, "title", "");
    private final StringProperty description = new SimpleStringProperty(this, "description", "");
    private final StringProperty deadline = new SimpleStringProperty(this, "deadline", "");
    private final IntegerProperty progress = new SimpleIntegerProperty(this, "progress", 0);
    private final StringProperty status = new SimpleStringProperty(this, "status", "ACTIVE");
    private final IntegerProperty importance = new SimpleIntegerProperty(this, "importance", 3); // 1-5
    private final StringProperty createdAt = new SimpleStringProperty(this, "createdAt", "");

    public Goal() {
    }

    // ----- id -----
    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    // ----- userId -----
    public int getUserId() { return userId.get(); }
    public void setUserId(int value) { userId.set(value); }
    public IntegerProperty userIdProperty() { return userId; }

    // ----- parentGoalId (nullable — null means top-level "Life Goal") -----
    public Integer getParentGoalId() { return parentGoalId.get(); }
    public void setParentGoalId(Integer value) { parentGoalId.set(value); }
    public ObjectProperty<Integer> parentGoalIdProperty() { return parentGoalId; }

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

    // ----- progress (0-100, auto-calculated by GoalService) -----
    public int getProgress() { return progress.get(); }
    public void setProgress(int value) { progress.set(Math.max(0, Math.min(100, value))); }
    public IntegerProperty progressProperty() { return progress; }

    // ----- status -----
    public String getStatus() { return status.get(); }
    public void setStatus(String value) { status.set(value); }
    public StringProperty statusProperty() { return status; }

    // ----- importance (1-5) -----
    public int getImportance() { return importance.get(); }
    public void setImportance(int value) { importance.set(value); }
    public IntegerProperty importanceProperty() { return importance; }

    // ----- createdAt -----
    public String getCreatedAt() { return createdAt.get(); }
    public void setCreatedAt(String value) { createdAt.set(value); }
    public StringProperty createdAtProperty() { return createdAt; }

    @Override
    public String toString() {
        // Used by ComboBox cells elsewhere (Task/Project dialogs) so a Goal
        // reads naturally when shown in a dropdown instead of Object@hash.
        return title.get();
    }
}
