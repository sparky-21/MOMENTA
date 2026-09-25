package com.momenta.dao.impl;

import com.momenta.dao.HabitDAO;
import com.momenta.database.DatabaseConnection;
import com.momenta.model.Habit;
import com.momenta.model.HabitLog;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HabitDAOImpl implements HabitDAO {
    @Override public Habit save(Habit h) {
        String sql="INSERT INTO habits(user_id,name,current_streak,longest_streak) VALUES(?,?,?,?)";
        try(PreparedStatement ps=DatabaseConnection.getConnection().prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){
            ps.setInt(1,h.getUserId()); ps.setString(2,h.getName()); ps.setInt(3,h.getCurrentStreak()); ps.setInt(4,h.getLongestStreak());
            ps.executeUpdate();
            try(ResultSet rs=ps.getGeneratedKeys()){if(rs.next())h.setId(rs.getInt(1));}
            return h;
        }catch(SQLException e){throw new RuntimeException("Failed to save habit",e);}
    }
    @Override public void update(Habit h){
        String sql="UPDATE habits SET name=?, current_streak=?, longest_streak=? WHERE id=? AND user_id=?";
        try(PreparedStatement ps=DatabaseConnection.getConnection().prepareStatement(sql)){
            ps.setString(1,h.getName()); ps.setInt(2,h.getCurrentStreak()); ps.setInt(3,h.getLongestStreak()); ps.setInt(4,h.getId()); ps.setInt(5,h.getUserId()); ps.executeUpdate();
        }catch(SQLException e){throw new RuntimeException("Failed to update habit",e);}
    }
    @Override public void delete(int id){
        try(PreparedStatement ps=DatabaseConnection.getConnection().prepareStatement("DELETE FROM habits WHERE id=?")){ps.setInt(1,id);ps.executeUpdate();}
        catch(SQLException e){throw new RuntimeException("Failed to delete habit",e);}
    }
    @Override public Habit findById(int id){
        try(PreparedStatement ps=DatabaseConnection.getConnection().prepareStatement("SELECT * FROM habits WHERE id=?")){
            ps.setInt(1,id); try(ResultSet rs=ps.executeQuery()){return rs.next()?mapHabit(rs):null;}
        }catch(SQLException e){throw new RuntimeException("Failed to find habit",e);}
    }
    @Override public List<Habit> findAll(int userId){
        List<Habit> out=new ArrayList<>();
        try(PreparedStatement ps=DatabaseConnection.getConnection().prepareStatement("SELECT * FROM habits WHERE user_id=? ORDER BY name")){
            ps.setInt(1,userId); try(ResultSet rs=ps.executeQuery()){while(rs.next())out.add(mapHabit(rs));} return out;
        }catch(SQLException e){throw new RuntimeException("Failed to load habits",e);}
    }
    @Override public void upsertLog(HabitLog log){
        String sql="INSERT INTO habit_logs(habit_id,log_date,completed) VALUES(?,?,?) ON CONFLICT(habit_id,log_date) DO UPDATE SET completed=excluded.completed";
        try(PreparedStatement ps=DatabaseConnection.getConnection().prepareStatement(sql)){
            ps.setInt(1,log.getHabitId()); ps.setString(2,log.getLogDate()); ps.setInt(3,log.isCompleted()?1:0); ps.executeUpdate();
        }catch(SQLException e){throw new RuntimeException("Failed to save habit log",e);}
    }
    @Override public HabitLog findLog(int habitId,String date){
        try(PreparedStatement ps=DatabaseConnection.getConnection().prepareStatement("SELECT * FROM habit_logs WHERE habit_id=? AND log_date=?")){
            ps.setInt(1,habitId);ps.setString(2,date);try(ResultSet rs=ps.executeQuery()){return rs.next()?mapLog(rs):null;}
        }catch(SQLException e){throw new RuntimeException("Failed to find habit log",e);}
    }
    @Override public List<HabitLog> findLogs(int habitId){
        List<HabitLog> out=new ArrayList<>();
        try(PreparedStatement ps=DatabaseConnection.getConnection().prepareStatement("SELECT * FROM habit_logs WHERE habit_id=? ORDER BY log_date DESC")){
            ps.setInt(1,habitId);try(ResultSet rs=ps.executeQuery()){while(rs.next())out.add(mapLog(rs));}return out;
        }catch(SQLException e){throw new RuntimeException("Failed to load habit logs",e);}
    }
    private Habit mapHabit(ResultSet rs)throws SQLException{
        Habit h=new Habit();h.setId(rs.getInt("id"));h.setUserId(rs.getInt("user_id"));h.setName(rs.getString("name"));
        h.setCurrentStreak(rs.getInt("current_streak"));h.setLongestStreak(rs.getInt("longest_streak"));h.setCreatedAt(rs.getString("created_at"));return h;
    }
    private HabitLog mapLog(ResultSet rs)throws SQLException{
        HabitLog l=new HabitLog();l.setId(rs.getInt("id"));l.setHabitId(rs.getInt("habit_id"));l.setLogDate(rs.getString("log_date"));l.setCompleted(rs.getInt("completed")!=0);return l;
    }
}
