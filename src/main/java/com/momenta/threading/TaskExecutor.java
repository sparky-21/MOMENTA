package com.momenta.threading;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Section 21/23 — MOMENTA's controlled thread pool.
 *
 * Two pools, each with a bounded, explicit size (never Executors.newCachedThreadPool,
 * which is effectively unbounded):
 *
 *   - workPool: short-lived one-off background jobs (DB reads/writes,
 *     analytics recalculation, priority recalculation). Sized at 4 threads,
 *     matching the "Thread 1..4" conceptual diagram in Section 23.
 *
 *   - scheduler: a single-thread ScheduledExecutorService dedicated to the
 *     DeadlineScheduler (Section 22), which polls the database on a fixed
 *     interval. It is kept separate from workPool so a long analytics job
 *     never delays a deadline check.
 *
 * Any code that needs to run off the JavaFX Application Thread should call
 * TaskExecutor.getInstance().submit(...) or .schedule(...) rather than
 * creating its own Thread/ExecutorService — this keeps every background
 * thread in MOMENTA accounted for and shuttable from one place.
 */
public final class TaskExecutor {

    private static final TaskExecutor INSTANCE = new TaskExecutor();

    private final ExecutorService workPool = Executors.newFixedThreadPool(4);
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private TaskExecutor() {
    }

    public static TaskExecutor getInstance() {
        return INSTANCE;
    }

    public ExecutorService workPool() {
        return workPool;
    }

    public ScheduledExecutorService scheduler() {
        return scheduler;
    }

    /** Section 15 (Software Engineering Rules #15): shut down cleanly on exit. */
    public void shutdown() {
        workPool.shutdown();
        scheduler.shutdown();
        try {
            if (!workPool.awaitTermination(3, TimeUnit.SECONDS)) {
                workPool.shutdownNow();
            }
            if (!scheduler.awaitTermination(3, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            workPool.shutdownNow();
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
