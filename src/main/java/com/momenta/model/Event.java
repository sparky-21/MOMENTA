package com.momenta.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/** Event model used by the Phase 6 Calendar. */
public class Event {

    private final IntegerProperty id = new SimpleIntegerProperty(this, "id");
    private final IntegerProperty userId = new SimpleIntegerProperty(this, "userId", 1);
    private final StringProperty title = new SimpleStringProperty(this, "title", "");
    private final StringProperty description = new SimpleStringProperty(this, "description", "");
    private final StringProperty eventDate = new SimpleStringProperty(this, "eventDate", "");
    private final StringProperty startTime = new SimpleStringProperty(this, "startTime", "");
    private final StringProperty endTime = new SimpleStringProperty(this, "endTime", "");
    private final StringProperty createdAt = new SimpleStringProperty(this, "createdAt", "");

    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    public int getUserId() { return userId.get(); }
    public void setUserId(int value) { userId.set(value); }
    public IntegerProperty userIdProperty() { return userId; }

    public String getTitle() { return title.get(); }
    public void setTitle(String value) { title.set(value); }
    public StringProperty titleProperty() { return title; }

    public String getDescription() { return description.get(); }
    public void setDescription(String value) { description.set(value); }
    public StringProperty descriptionProperty() { return description; }

    public String getEventDate() { return eventDate.get(); }
    public void setEventDate(String value) { eventDate.set(value); }
    public StringProperty eventDateProperty() { return eventDate; }

    public String getStartTime() { return startTime.get(); }
    public void setStartTime(String value) { startTime.set(value); }
    public StringProperty startTimeProperty() { return startTime; }

    public String getEndTime() { return endTime.get(); }
    public void setEndTime(String value) { endTime.set(value); }
    public StringProperty endTimeProperty() { return endTime; }

    public String getCreatedAt() { return createdAt.get(); }
    public void setCreatedAt(String value) { createdAt.set(value); }
    public StringProperty createdAtProperty() { return createdAt; }
}
