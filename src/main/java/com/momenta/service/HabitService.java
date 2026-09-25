package com.momenta.service;

import com.momenta.dao.HabitDAO;
import com.momenta.dao.impl.HabitDAOImpl;
import com.momenta.model.Habit;
import com.momenta.model.HabitLog;
import java.time.LocalDate;
import java.util.List;

public class HabitService {
    private final HabitDAO dao=new HabitDAOImpl();

    public Habit createHabit(Habit h){
        validate(h); h.setCurrentStreak(0); h.setLongestStreak(0); return dao.save(h);
    }
    public void updateHabit(Habit h){validate(h);dao.update(h);}
    public void deleteHabit(int id){dao.delete(id);}
    public List<Habit> getAllHabits(int userId){return dao.findAll(userId);}
    public List<HabitLog> getLogs(int habitId){return dao.findLogs(habitId);}

    public void setCompleted(Habit habit, LocalDate date, boolean completed){
        HabitLog log=new HabitLog(); log.setHabitId(habit.getId()); log.setLogDate(date.toString()); log.setCompleted(completed);
        dao.upsertLog(log); recalculateStreak(habit); dao.update(habit);
    }

    public boolean isCompleted(Habit habit, LocalDate date){
        HabitLog log=dao.findLog(habit.getId(),date.toString()); return log!=null && log.isCompleted();
    }

    public double weeklyConsistency(Habit habit, LocalDate end){
        int completed=0;
        for(int i=0;i<7;i++) if(isCompleted(habit,end.minusDays(i))) completed++;
        return completed/7.0;
    }

    private void recalculateStreak(Habit habit){
        int current=0; LocalDate day=LocalDate.now();
        while(isCompleted(habit,day)){current++;day=day.minusDays(1);}
        int longest=0, run=0;
        for(HabitLog log: dao.findLogs(habit.getId())){
            if(!log.isCompleted()) {run=0;continue;}
            run++;
            if(run>longest) longest=run;
        }
        // findLogs is newest-first; calculate longest using dates instead
        List<HabitLog> logs=dao.findLogs(habit.getId()).stream()
                .filter(HabitLog::isCompleted).sorted((a,b)->a.getLogDate().compareTo(b.getLogDate())).toList();
        longest=0; run=0; LocalDate prev=null;
        for(HabitLog log:logs){
            LocalDate d=LocalDate.parse(log.getLogDate());
            if(prev!=null && d.equals(prev.plusDays(1))) run++; else run=1;
            longest=Math.max(longest,run); prev=d;
        }
        habit.setCurrentStreak(current); habit.setLongestStreak(longest);
    }

    private void validate(Habit h){if(h.getName()==null||h.getName().isBlank())throw new IllegalArgumentException("Habit name cannot be empty.");}
}
