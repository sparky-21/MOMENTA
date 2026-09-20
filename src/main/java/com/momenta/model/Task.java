package com.momenta.model;

import javafx.beans.property.*;

import java.time.LocalDateTime;

/**
 * The Task model.
 *
 * WHY JavaFX PROPERTIES INSTEAD OF PLAIN FIELDS (viva talking point):
 * A plain `private String title;` with a getter has no way to notify the UI
 * when it changes. JavaFX's SimpleStringProperty / SimpleDoubleProperty etc.
 * implement the Observer pattern: a TableView cell, a ProgressBar, or a
 * label can *bind* directly to a property, and when the value changes
 * anywhere in the app, every bound control updates itself automatically —
 * no manual "refresh the UI" calls scattered through the codebase.
 *
 * This class deliberately has ZERO JavaFX UI imports beyond
 * javafx.beans.property (which is just observable data, not visual
 * controls) — it stays testable and reusable outside of a running UI.
 */
public class Task {

    private final IntegerProperty id = new SimpleIntegerProperty(this, "id");
    private final StringProperty title = new SimpleStringProperty(this, "title");
    private final StringProperty description = new SimpleStringProperty(this, "description");
    private final StringProperty category = new SimpleStringProperty(this, "category", "Other");
    private final IntegerProperty importance = new SimpleIntegerProperty(this, "importance", 3);
    private final ObjectProperty<LocalDateTime> deadline = new SimpleObjectProperty<>(this, "deadline");
    private final IntegerProperty estimatedMinutes = new SimpleIntegerProperty(this, "estimatedMinutes", 30);
    private final DoubleProperty progress = new SimpleDoubleProperty(this, "progress", 0.0);
    private final StringProperty status = new SimpleStringProperty(this, "status", "PENDING");
    private final ObjectProperty<Integer> projectId = new SimpleObjectProperty<>(this, "projectId");
    private final ObjectProperty<Integer> goalId = new SimpleObjectProperty<>(this, "goalId");

    public Task() {
    }

    public Task(String title, String category, int importance,
                LocalDateTime deadline, int estimatedMinutes) {
        setTitle(title);
        setCategory(category);
        setImportance(importance);
        setDeadline(deadline);
        setEstimatedMinutes(estimatedMinutes);
    }

    // --- id ---
    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    // --- title ---
    public String getTitle() { return title.get(); }
    public void setTitle(String value) { title.set(value); }
    public StringProperty titleProperty() { return title; }

    // --- description ---
    public String getDescription() { return description.get(); }
    public void setDescription(String value) { description.set(value); }
    public StringProperty descriptionProperty() { return description; }

    // --- category ---
    public String getCategory() { return category.get(); }
    public void setCategory(String value) { category.set(value); }
    public StringProperty categoryProperty() { return category; }

    // --- importance (1..5) ---
    public int getImportance() { return importance.get(); }
    public void setImportance(int value) { importance.set(value); }
    public IntegerProperty importanceProperty() { return importance; }

    // --- deadline ---
    public LocalDateTime getDeadline() { return deadline.get(); }
    public void setDeadline(LocalDateTime value) { deadline.set(value); }
    public ObjectProperty<LocalDateTime> deadlineProperty() { return deadline; }

    // --- estimatedMinutes ---
    public int getEstimatedMinutes() { return estimatedMinutes.get(); }
    public void setEstimatedMinutes(int value) { estimatedMinutes.set(value); }
    public IntegerProperty estimatedMinutesProperty() { return estimatedMinutes; }

    // --- progress (0.0 .. 1.0) ---
    public double getProgress() { return progress.get(); }
    public void setProgress(double value) { progress.set(value); }
    public DoubleProperty progressProperty() { return progress; }

    // --- status ---
    public String getStatus() { return status.get(); }
    public void setStatus(String value) { status.set(value); }
    public StringProperty statusProperty() { return status; }

    // --- projectId (nullable) ---
    public Integer getProjectId() { return projectId.get(); }
    public void setProjectId(Integer value) { projectId.set(value); }
    public ObjectProperty<Integer> projectIdProperty() { return projectId; }

    // --- goalId (nullable) ---
    public Integer getGoalId() { return goalId.get(); }
    public void setGoalId(Integer value) { goalId.set(value); }
    public ObjectProperty<Integer> goalIdProperty() { return goalId; }

    public boolean isDone() {
        return "DONE".equals(getStatus());
    }
}
