package com.momenta.service;

import com.momenta.threading.TaskExecutor;
import com.momenta.utility.CurrentUser;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/** Periodically checks deadline-based notifications in the background. */
public class NotificationScheduler {
    private final NotificationService notificationService = new NotificationService();
    private ScheduledFuture<?> future;

    public synchronized void start() {
        if (future != null && !future.isCancelled()) return;
        future = TaskExecutor.getInstance().scheduler()
                .scheduleAtFixedRate(this::check, 5, 60, TimeUnit.SECONDS);
    }

    private void check() {
        if (!CurrentUser.isLoggedIn()) return; // scheduler fires every 60s regardless of login state; skip until someone signs in
        try {
            notificationService.checkUpcomingDeadlines(CurrentUser.getId());
        } catch (RuntimeException e) {
            System.err.println("[NotificationScheduler] " + e.getMessage());
        }
    }

    public synchronized void stop() {
        if (future != null) {
            future.cancel(false);
            future = null;
        }
    }
}