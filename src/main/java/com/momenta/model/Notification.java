package com.momenta.model;

import javafx.beans.property.*;

/** Notification model for Phase 14. */
public class Notification {
    private final IntegerProperty id = new SimpleIntegerProperty(this, "id");
    private final IntegerProperty userId = new SimpleIntegerProperty(this, "userId");
    private final StringProperty type = new SimpleStringProperty(this, "type", "GENERAL");
    private final StringProperty message = new SimpleStringProperty(this, "message", "");
    private final BooleanProperty read = new SimpleBooleanProperty(this, "read", false);
    private final StringProperty createdAt = new SimpleStringProperty(this, "createdAt", "");

    public Notification() {}

    public Notification(int userId, String type, String message) {
        setUserId(userId);
        setType(type);
        setMessage(message);
    }

    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    public int getUserId() { return userId.get(); }
    public void setUserId(int value) { userId.set(value); }
    public IntegerProperty userIdProperty() { return userId; }

    public String getType() { return type.get(); }
    public void setType(String value) { type.set(value); }
    public StringProperty typeProperty() { return type; }

    public String getMessage() { return message.get(); }
    public void setMessage(String value) { message.set(value); }
    public StringProperty messageProperty() { return message; }

    public boolean isRead() { return read.get(); }
    public void setRead(boolean value) { read.set(value); }
    public BooleanProperty readProperty() { return read; }

    public String getCreatedAt() { return createdAt.get(); }
    public void setCreatedAt(String value) { createdAt.set(value); }
    public StringProperty createdAtProperty() { return createdAt; }
}
