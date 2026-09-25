package com.momenta.dao;

import com.momenta.model.FocusSession;
import java.util.List;

public interface FocusSessionDAO {
    FocusSession save(FocusSession session);
    void finish(int id, String endedAt);
    List<FocusSession> findAll(int userId);
    List<FocusSession> findByTask(int taskId);
}
