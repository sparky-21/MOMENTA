package com.momenta.dao;

import com.momenta.model.Habit;
import com.momenta.model.HabitLog;
import java.util.List;

public interface HabitDAO {
    Habit save(Habit habit);
    void update(Habit habit);
    void delete(int id);
    Habit findById(int id);
    List<Habit> findAll(int userId);
    void upsertLog(HabitLog log);
    HabitLog findLog(int habitId, String date);
    List<HabitLog> findLogs(int habitId);
}
