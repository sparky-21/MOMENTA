package com.momenta.model;

import java.time.LocalDateTime;

/** A persisted focus session belonging to a user and optionally a task. */
public class FocusSession {
    private int id;
    private int userId;
    private Integer taskId;
    private int durationMinutes;
    private String startedAt;
    private String endedAt;

    public FocusSession() {}

    public FocusSession(int userId, Integer taskId, int durationMinutes,
                        String startedAt, String endedAt) {
        this.userId = userId;
        this.taskId = taskId;
        this.durationMinutes = durationMinutes;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public Integer getTaskId() { return taskId; }
    public void setTaskId(Integer taskId) { this.taskId = taskId; }
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    public String getStartedAt() { return startedAt; }
    public void setStartedAt(String startedAt) { this.startedAt = startedAt; }
    public String getEndedAt() { return endedAt; }
    public void setEndedAt(String endedAt) { this.endedAt = endedAt; }

    public static FocusSession start(int userId, Integer taskId, int durationMinutes) {
        return new FocusSession(userId, taskId, durationMinutes,
                LocalDateTime.now().toString(), null);
    }
}
