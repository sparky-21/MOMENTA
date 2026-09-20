package com.momenta.threading;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * MOMENTA's single, shared thread pool.
 *
 * WHY THIS EXISTS (viva talking point):
 * Every database query, analytics calculation, or priority-engine run takes
 * real time. If we ran that work directly inside a button's onAction
 * handler, it would execute on the JavaFX Application Thread — the ONE
 * thread responsible for painting the UI and responding to clicks. The
 * window would freeze until the query finished.
 *
 * Instead, every service in MOMENTA submits work to this pool via
 * javafx.concurrent.Task, and only touches JavaFX controls again inside
 * Task.succeeded()/setOnSucceeded(), which JavaFX guarantees runs back on
 * the Application Thread. That hand-off is the whole trick:
 *
 *   JavaFX Thread --submit--> AppExecutor (background thread)
 *                                   |
 *                             SQL / computation
 *                                   |
 *   JavaFX Thread <--Platform.runLater / setOnSucceeded--
 *
 * We use a FIXED pool (not one-thread-per-task) so a burst of activity
 * (e.g. dashboard loading five widgets at once) can't spawn unbounded
 * threads and exhaust memory. We use a SEPARATE scheduled pool for
 * recurring work (the deadline monitor) so a slow one-off task can never
 * delay a scheduled tick.
 */
public final class AppExecutor {

    // General-purpose pool: DB reads/writes, analytics, recommendation runs.
    private static final ExecutorService POOL = Executors.newFixedThreadPool(4);

    // Dedicated pool for recurring background jobs (deadline scanner, etc.)
    private static final ScheduledExecutorService SCHEDULER =
            Executors.newScheduledThreadPool(1);

    private AppExecutor() {
        // static utility class — never instantiated
    }

    public static ExecutorService pool() {
        return POOL;
    }

    public static ScheduledExecutorService scheduler() {
        return SCHEDULER;
    }

    /** Call once, from Main, when the application window closes. */
    public static void shutdown() {
        POOL.shutdown();
        SCHEDULER.shutdown();
        try {
            if (!POOL.awaitTermination(3, TimeUnit.SECONDS)) {
                POOL.shutdownNow();
            }
            if (!SCHEDULER.awaitTermination(3, TimeUnit.SECONDS)) {
                SCHEDULER.shutdownNow();
            }
        } catch (InterruptedException e) {
            POOL.shutdownNow();
            SCHEDULER.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
