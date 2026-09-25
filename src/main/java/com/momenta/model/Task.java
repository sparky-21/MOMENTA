package com.momenta.model;

import javafx.beans.property.*;

/**
 * Task model — built with JavaFX Properties (not plain fields) so that:
 *   1. TableView columns can bind directly to these properties and repaint
 *      automatically when a value changes (Observer pattern, Section 32).
 *   2. ProgressBar / labels elsewhere in the UI can bind to progressProperty()
 *      and stay in sync without manual refresh calls.
 *
 * This class is a pure model: no JavaFX Nodes, no SQL. It knows nothing
 * about how it is displayed or persisted — that is TaskController's and
 * TaskDAO's job respectively (Section 8 of the software engineering rules).
 */
public class Task {

    private final IntegerProperty id = new SimpleIntegerProperty(this, "id");
    private final IntegerProperty userId = new SimpleIntegerProperty(this, "userId", 1);
    private final ObjectProperty<Integer> projectId = new SimpleObjectProperty<>(this, "projectId");
    private final ObjectProperty<Integer> goalId = new SimpleObjectProperty<>(this, "goalId");

    private final StringProperty title = new SimpleStringProperty(this, "title", "");
    private final StringProperty description = new SimpleStringProperty(this, "description", "");
    private final StringProperty category = new SimpleStringProperty(this, "category", "Other");
    private final IntegerProperty importance = new SimpleIntegerProperty(this, "importance", 3); // 1-5
    private final StringProperty deadline = new SimpleStringProperty(this, "deadline", "");        // ISO yyyy-MM-dd
    private final IntegerProperty estimatedMinutes = new SimpleIntegerProperty(this, "estimatedMinutes", 30);
    private final IntegerProperty progress = new SimpleIntegerProperty(this, "progress", 0);        // 0-100
    private final StringProperty status = new SimpleStringProperty(this, "status", "PENDING");
    private final IntegerProperty priorityScore = new SimpleIntegerProperty(this, "priorityScore", 0);
    private final StringProperty createdAt = new SimpleStringProperty(this, "createdAt", "");
    private final StringProperty completedAt = new SimpleStringProperty(this, "completedAt", "");

    public Task() {
    }

    public Task(String title, String category, int importance, String deadline, int estimatedMinutes) {
        setTitle(title);
        setCategory(category);
        setImportance(importance);
        setDeadline(deadline);
        setEstimatedMinutes(estimatedMinutes);
    }

    // ----- id -----
    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    // ----- userId -----
    public int getUserId() { return userId.get(); }
    public void setUserId(int value) { userId.set(value); }
    public IntegerProperty userIdProperty() { return userId; }

    // ----- projectId (nullable) -----
    public Integer getProjectId() { return projectId.get(); }
    public void setProjectId(Integer value) { projectId.set(value); }
    public ObjectProperty<Integer> projectIdProperty() { return projectId; }

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

    // ----- category -----
    public String getCategory() { return category.get(); }
    public void setCategory(String value) { category.set(value); }
    public StringProperty categoryProperty() { return category; }

    // ----- importance (1-5) -----
    public int getImportance() { return importance.get(); }
    public void setImportance(int value) { importance.set(value); }
    public IntegerProperty importanceProperty() { return importance; }

    // ----- deadline -----
    public String getDeadline() { return deadline.get(); }
    public void setDeadline(String value) { deadline.set(value); }
    public StringProperty deadlineProperty() { return deadline; }

    // ----- estimatedMinutes -----
    public int getEstimatedMinutes() { return estimatedMinutes.get(); }
    public void setEstimatedMinutes(int value) { estimatedMinutes.set(value); }
    public IntegerProperty estimatedMinutesProperty() { return estimatedMinutes; }

    // ----- progress (0-100) -----
    public int getProgress() { return progress.get(); }
    public void setProgress(int value) { progress.set(Math.max(0, Math.min(100, value))); }
    public IntegerProperty progressProperty() { return progress; }

    // ----- status -----
    public String getStatus() { return status.get(); }
    public void setStatus(String value) { status.set(value); }
    public StringProperty statusProperty() { return status; }

    // ----- priorityScore (computed by PriorityEngine) -----
    public int getPriorityScore() { return priorityScore.get(); }
    public void setPriorityScore(int value) { priorityScore.set(value); }
    public IntegerProperty priorityScoreProperty() { return priorityScore; }

    // ----- createdAt -----
    public String getCreatedAt() { return createdAt.get(); }
    public void setCreatedAt(String value) { createdAt.set(value); }
    public StringProperty createdAtProperty() { return createdAt; }

    // ----- completedAt -----
    public String getCompletedAt() { return completedAt.get(); }
    public void setCompletedAt(String value) { completedAt.set(value); }
    public StringProperty completedAtProperty() { return completedAt; }

    public boolean isCompleted() {
        return "COMPLETED".equals(getStatus());
    }
}
