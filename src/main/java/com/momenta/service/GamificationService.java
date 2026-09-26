package com.momenta.service;

import com.momenta.dao.FocusSessionDAO;
import com.momenta.dao.impl.FocusSessionDAOImpl;
import com.momenta.model.Achievement;
import com.momenta.model.FocusSession;
import com.momenta.model.GamificationStats;
import com.momenta.model.Goal;
import com.momenta.model.Habit;
import com.momenta.model.HabitLog;
import com.momenta.model.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Phase 22 — Lightweight productivity gamification.
 *
 * XP is derived from existing persisted MOMENTA data:
 *   completed task       = +50 XP
 *   completed focus      = +25 XP
 *   completed goal       = +100 XP
 *   completed habit day  = +10 XP
 *   current streak day   = +20 XP
 *
 * Level formula:
 *   200 XP is required for each level.
 *   level = floor(totalXP / 200) + 1
 *
 * There is deliberately no random XP and no new database table. The same
 * database state always produces the same gamification result.
 */
public class GamificationService {

    public static final int TASK_XP = 50;
    public static final int FOCUS_XP = 25;
    public static final int GOAL_XP = 100;
    public static final int HABIT_DAY_XP = 10;
    public static final int STREAK_DAY_XP = 20;
    public static final int XP_PER_LEVEL = 200;

    private final TaskService taskService = new TaskService();
    private final GoalService goalService = new GoalService();
    private final HabitService habitService = new HabitService();
    private final FocusSessionDAO focusSessionDAO = new FocusSessionDAOImpl();

    public GamificationStats calculate(int userId) {
        List<Task> tasks = taskService.getAllTasks(userId);
        List<Goal> goals = goalService.getAllGoals(userId);
        List<Habit> habits = habitService.getAllHabits(userId);
        List<FocusSession> sessions = focusSessionDAO.findAll(userId);

        int completedTasks = (int) tasks.stream()
                .filter(Task::isCompleted)
                .count();

        int completedGoals = (int) goals.stream()
                .filter(g -> "COMPLETED".equalsIgnoreCase(g.getStatus()) || g.getProgress() >= 100)
                .count();

        int completedFocusSessions = (int) sessions.stream()
                .filter(s -> s.getEndedAt() != null && !s.getEndedAt().isBlank())
                .count();

        int completedHabitDays = 0;
        int currentStreak = 0;

        for (Habit habit : habits) {
            currentStreak = Math.max(currentStreak, habit.getCurrentStreak());

            for (HabitLog log : habitService.getLogs(habit.getId())) {
                if (log.isCompleted()) {
                    completedHabitDays++;
                }
            }
        }

        int xp = 0;
        xp += completedTasks * TASK_XP;
        xp += completedFocusSessions * FOCUS_XP;
        xp += completedGoals * GOAL_XP;
        xp += completedHabitDays * HABIT_DAY_XP;
        xp += currentStreak * STREAK_DAY_XP;

        int level = (xp / XP_PER_LEVEL) + 1;
        int xpIntoLevel = xp % XP_PER_LEVEL;

        List<Achievement> achievements = buildAchievements(
                completedTasks,
                completedFocusSessions,
                completedGoals,
                completedHabitDays,
                currentStreak);

        return new GamificationStats(
                xp,
                level,
                xpIntoLevel,
                XP_PER_LEVEL,
                currentStreak,
                completedTasks,
                completedFocusSessions,
                completedGoals,
                completedHabitDays,
                achievements);
    }

    private List<Achievement> buildAchievements(
            int completedTasks,
            int completedFocusSessions,
            int completedGoals,
            int completedHabitDays,
            int currentStreak) {

        List<Achievement> result = new ArrayList<>();

        result.add(new Achievement(
                "First Step",
                "Complete your first task.",
                completedTasks >= 1));

        result.add(new Achievement(
                "Task Finisher",
                "Complete 10 tasks.",
                completedTasks >= 10));

        result.add(new Achievement(
                "Focus Starter",
                "Complete your first focus session.",
                completedFocusSessions >= 1));

        result.add(new Achievement(
                "Deep Focus",
                "Complete 10 focus sessions.",
                completedFocusSessions >= 10));

        result.add(new Achievement(
                "Goal Getter",
                "Complete your first goal.",
                completedGoals >= 1));

        result.add(new Achievement(
                "Habit Builder",
                "Complete 7 habit days.",
                completedHabitDays >= 7));

        result.add(new Achievement(
                "Seven Day Streak",
                "Maintain a 7-day habit streak.",
                currentStreak >= 7));

        return result;
    }
}
