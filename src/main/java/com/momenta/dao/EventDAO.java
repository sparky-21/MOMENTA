package com.momenta.dao;

import com.momenta.model.Event;

import java.util.List;

public interface EventDAO {
    Event save(Event event);
    void update(Event event);
    void delete(int id);
    Event findById(int id);
    List<Event> findAll(int userId);
    List<Event> findByDate(int userId, String isoDate);
}
