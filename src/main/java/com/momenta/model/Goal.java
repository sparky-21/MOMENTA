package com.momenta.model;

import javafx.beans.property.*;
import java.time.LocalDate;

public class Goal {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final StringProperty tier = new SimpleStringProperty("MONTH");
    private final ObjectProperty<Integer> parentGoalId = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> deadline = new SimpleObjectProperty<>();
    private final DoubleProperty progress = new SimpleDoubleProperty(0.0);
    private final StringProperty status = new SimpleStringProperty("ACTIVE");

    public Goal() {}

    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    public String getTitle() { return title.get(); }
    public void setTitle(String value) { title.set(value); }
    public StringProperty titleProperty() { return title; }

    public String getDescription() { return description.get(); }
    public void setDescription(String value) { description.set(value); }
    public StringProperty descriptionProperty() { return description; }

    public String getTier() { return tier.get(); }
    public void setTier(String value) { tier.set(value); }
    public StringProperty tierProperty() { return tier; }

    public Integer getParentGoalId() { return parentGoalId.get(); }
    public void setParentGoalId(Integer value) { parentGoalId.set(value); }
    public ObjectProperty<Integer> parentGoalIdProperty() { return parentGoalId; }

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
