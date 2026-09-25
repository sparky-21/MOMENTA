package com.momenta.model;

public class HabitLog {
    private int id;
    private int habitId;
    private String logDate;
    private boolean completed;

    public int getId(){return id;} public void setId(int v){id=v;}
    public int getHabitId(){return habitId;} public void setHabitId(int v){habitId=v;}
    public String getLogDate(){return logDate;} public void setLogDate(String v){logDate=v;}
    public boolean isCompleted(){return completed;} public void setCompleted(boolean v){completed=v;}
}
