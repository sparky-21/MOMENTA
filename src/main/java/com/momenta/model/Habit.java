package com.momenta.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Habit {
    private final IntegerProperty id = new SimpleIntegerProperty(this, "id");
    private final IntegerProperty userId = new SimpleIntegerProperty(this, "userId", 1);
    private final StringProperty name = new SimpleStringProperty(this, "name", "");
    private final IntegerProperty currentStreak = new SimpleIntegerProperty(this, "currentStreak", 0);
    private final IntegerProperty longestStreak = new SimpleIntegerProperty(this, "longestStreak", 0);
    private final StringProperty createdAt = new SimpleStringProperty(this, "createdAt", "");

    public int getId(){return id.get();} public void setId(int v){id.set(v);} public IntegerProperty idProperty(){return id;}
    public int getUserId(){return userId.get();} public void setUserId(int v){userId.set(v);} public IntegerProperty userIdProperty(){return userId;}
    public String getName(){return name.get();} public void setName(String v){name.set(v);} public StringProperty nameProperty(){return name;}
    public int getCurrentStreak(){return currentStreak.get();} public void setCurrentStreak(int v){currentStreak.set(v);} public IntegerProperty currentStreakProperty(){return currentStreak;}
    public int getLongestStreak(){return longestStreak.get();} public void setLongestStreak(int v){longestStreak.set(v);} public IntegerProperty longestStreakProperty(){return longestStreak;}
    public String getCreatedAt(){return createdAt.get();} public void setCreatedAt(String v){createdAt.set(v);} public StringProperty createdAtProperty(){return createdAt;}

    @Override public String toString(){return getName();}
}
