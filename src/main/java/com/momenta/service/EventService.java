package com.momenta.service;

import com.momenta.dao.EventDAO;
import com.momenta.dao.impl.EventDAOImpl;
import com.momenta.model.Event;

import java.util.List;

public class EventService {
    private final EventDAO eventDAO = new EventDAOImpl();

    public Event createEvent(Event event) {
        validate(event);
        return eventDAO.save(event);
    }

    public void updateEvent(Event event) {
        validate(event);
        eventDAO.update(event);
    }

    public void deleteEvent(int id) { eventDAO.delete(id); }

    public Event getEvent(int id) { return eventDAO.findById(id); }

    public List<Event> getAllEvents(int userId) { return eventDAO.findAll(userId); }

    public List<Event> getEventsForDate(int userId, String isoDate) {
        return eventDAO.findByDate(userId, isoDate);
    }

    private void validate(Event event) {
        if (event.getTitle() == null || event.getTitle().isBlank()) {
            throw new IllegalArgumentException("Event title cannot be empty.");
        }
        if (event.getEventDate() == null || event.getEventDate().isBlank()) {
            throw new IllegalArgumentException("Event date is required.");
        }
    }
}
