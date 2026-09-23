package com.momenta.model;

import javafx.beans.property.*;
import java.time.LocalDate;

public class Project {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final ObjectProperty<Integer> goalId = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> deadline = new SimpleObjectProperty<>();
    private final DoubleProperty progress = new SimpleDoubleProperty(0.0);
    private final StringProperty status = new SimpleStringProperty("ACTIVE");

    public Project() {}

    public Project(int id, String title, String description, Integer goalId,
                   LocalDate deadline, double progress, String status) {
        setId(id);
        setTitle(title);
        setDescription(description);
        setGoalId(goalId);
        setDeadline(deadline);
        setProgress(progress);
        setStatus(status);
    }

    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    public String getTitle() { return title.get(); }
    public void setTitle(String value) { title.set(value); }
    public StringProperty titleProperty() { return title; }

    public String getDescription() { return description.get(); }
    public void setDescription(String value) { description.set(value); }
    public StringProperty descriptionProperty() { return description; }

    public Integer getGoalId() { return goalId.get(); }
    public void setGoalId(Integer value) { goalId.set(value); }
    public ObjectProperty<Integer> goalIdProperty() { return goalId; }

    public LocalDate getDeadline() { return deadline.get(); }
    public void setDeadline(LocalDate value) { deadline.set(value); }
    public ObjectProperty<LocalDate> deadlineProperty() { return deadline; }

    public double getProgress() { return progress.get(); }
    public void setProgress(double value) { progress.set(value); }
    public DoubleProperty progressProperty() { return progress; }

    public String getStatus() { return status.get(); }
    public void setStatus(String value) { status.set(value); }
    public StringProperty statusProperty() { return status; }
}
