package com.momenta.threading;

import javafx.concurrent.Task;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Centralized MOMENTA background executor.
 *
 * Phase 14:
 * - one bounded ExecutorService for background work
 * - one single-thread ScheduledExecutorService for periodic jobs
 * - JavaFX Task results return to the JavaFX Application Thread through
 *   Task's onSucceeded/onFailed handlers
 * - no controller creates raw Thread objects
 */
public final class TaskExecutor {

    private static final TaskExecutor INSTANCE = new TaskExecutor();

    private final ExecutorService workPool;
    private final ScheduledExecutorService scheduler;

    private TaskExecutor() {
        AtomicInteger workId = new AtomicInteger(1);
        AtomicInteger schedulerId = new AtomicInteger(1);

        ThreadFactory workFactory = runnable -> {
            Thread thread = new Thread(runnable, "momenta-worker-" + workId.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        };

        ThreadFactory schedulerFactory = runnable -> {
            Thread thread = new Thread(runnable, "momenta-scheduler-" + schedulerId.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        };

        workPool = Executors.newFixedThreadPool(4, workFactory);
        scheduler = Executors.newSingleThreadScheduledExecutor(schedulerFactory);
    }

    public static TaskExecutor getInstance() {
        return INSTANCE;
    }

    /** Existing API preserved for current controllers/services. */
    public ExecutorService workPool() {
        return workPool;
    }

    /** Existing API preserved for deadline scheduling. */
    public ScheduledExecutorService scheduler() {
        return scheduler;
    }

    /** Submit a JavaFX Task to the shared background pool. */
    public <T> Future<?> submit(Task<T> task) {
        return workPool.submit(task);
    }

    /** Submit a normal Callable without creating a new Thread. */
    public <T> Future<T> submit(Callable<T> work) {
        return workPool.submit(work);
    }

    /** Submit a fire-and-forget background action. */
    public Future<?> submit(Runnable work) {
        return workPool.submit(work);
    }

    /** Schedule a periodic background action. */
    public ScheduledFuture<?> scheduleAtFixedRate(
            Runnable work,
            long initialDelay,
            long period,
            TimeUnit unit
    ) {
        return scheduler.scheduleAtFixedRate(work, initialDelay, period, unit);
    }

    /**
     * Graceful application shutdown. Existing Main.stop() can keep calling
     * this method; no duplicate shutdown logic is required elsewhere.
     */
    public synchronized void shutdown() {
        if (!workPool.isShutdown()) {
            workPool.shutdown();
        }
        if (!scheduler.isShutdown()) {
            scheduler.shutdown();
        }

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
