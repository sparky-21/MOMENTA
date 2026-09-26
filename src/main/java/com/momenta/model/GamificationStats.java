package com.momenta.model;

import java.util.List;

/**
 * Phase 22 — Snapshot of the current user's gamification state.
 *
 * This is derived from existing MOMENTA data, so no new database table is
 * required and existing user data remains untouched.
 */
public class GamificationStats {
    private final int totalXp;
    private final int level;
    private final int xpIntoLevel;
    private final int xpForNextLevel;
    private final int currentStreak;
    private final int completedTasks;
    private final int completedFocusSessions;
    private final int completedGoals;
    private final int completedHabitDays;
    private final List<Achievement> achievements;

    public GamificationStats(
            int totalXp,
            int level,
            int xpIntoLevel,
            int xpForNextLevel,
            int currentStreak,
            int completedTasks,
            int completedFocusSessions,
            int completedGoals,
            int completedHabitDays,
            List<Achievement> achievements) {
        this.totalXp = totalXp;
        this.level = level;
        this.xpIntoLevel = xpIntoLevel;
        this.xpForNextLevel = xpForNextLevel;
        this.currentStreak = currentStreak;
        this.completedTasks = completedTasks;
        this.completedFocusSessions = completedFocusSessions;
        this.completedGoals = completedGoals;
        this.completedHabitDays = completedHabitDays;
        this.achievements = List.copyOf(achievements);
    }

    public int getTotalXp() { return totalXp; }
    public int getLevel() { return level; }
    public int getXpIntoLevel() { return xpIntoLevel; }
    public int getXpForNextLevel() { return xpForNextLevel; }
    public int getCurrentStreak() { return currentStreak; }
    public int getCompletedTasks() { return completedTasks; }
    public int getCompletedFocusSessions() { return completedFocusSessions; }
    public int getCompletedGoals() { return completedGoals; }
    public int getCompletedHabitDays() { return completedHabitDays; }
    public List<Achievement> getAchievements() { return achievements; }

    public double getProgress() {
        return xpForNextLevel == 0 ? 0 : (double) xpIntoLevel / xpForNextLevel;
    }

    public long getUnlockedAchievementCount() {
        return achievements.stream().filter(Achievement::isUnlocked).count();
    }
}
