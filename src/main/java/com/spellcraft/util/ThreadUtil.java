package com.spellcraft.util;

import com.cjcrafter.foliascheduler.TaskImplementation;
import com.cjcrafter.foliascheduler.folia.FoliaTask;
import com.spellcraft.SpellCraftPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.logging.Level;

import static com.cjcrafter.foliascheduler.util.ServerVersions.isFolia;
import static com.spellcraft.SpellCraftPlugin.plugin;
import static com.spellcraft.SpellCraftPlugin.scheduler;

/**
 * Utility class for scheduling tasks safely across Bukkit and Folia servers.
 * <p>
 * Supports tasks tied to entities, regions, global execution, or asynchronous execution.
 * Automatically handles threading, scheduling, and shutdown scenarios.
 */
public class ThreadUtil {

    /** Flag indicating the scheduler is shutting down. */
    private static final AtomicBoolean SHUTTING_DOWN = new AtomicBoolean(false);

    /** Counter for generating unique task IDs. */
    private static final AtomicLong TASK_ID_COUNTER = new AtomicLong();

    /** Map of task IDs to weak references of running tasks. */
    private static final Map<Long, WeakReference<ThreadTask>> TASKS = new ConcurrentHashMap<>();

    /**
     * Represents a registered task with unique ID, optional label, and underlying handle
     * (BukkitTask, FoliaTask, or TaskImplementation).
     */
    public static final class ThreadTask {
        private final long id;
        private final String label;
        private final Object handle;

        private ThreadTask(long id, String label, Object handle) {
            this.id = id;
            this.label = label;
            this.handle = handle;
        }

        /** @return the unique ID of this task */
        public long getId() {
            return id;
        }

        /** @return the optional label assigned to this task */
        public String getLabel() {
            return label;
        }

        /** @return the underlying task handle (BukkitTask, FoliaTask, or TaskImplementation) */
        public Object getHandle() {
            return handle;
        }

        /**
         * Cancels the task if it is still active.
         *
         * @return true if the task was successfully cancelled, false otherwise
         */
        public boolean cancel() {
            boolean cancelled = false;
            if (handle instanceof FoliaTask<?> foliaTask) {
                foliaTask.cancel();
                cancelled = true;
            } else if (handle instanceof org.bukkit.scheduler.BukkitTask bukkitTask) {
                bukkitTask.cancel();
                cancelled = true;
            } else if (handle instanceof TaskImplementation<?> taskImpl) {
                taskImpl.cancel();
                cancelled = true;
            }
            if (cancelled) cleanup();
            return cancelled;
        }

        /**
         * Checks whether the task has been cancelled.
         *
         * @return true if cancelled or handle is null, false otherwise
         */
        public boolean isCancelled() {
            if (handle == null) return true;
            if (handle instanceof FoliaTask<?> foliaTask) return foliaTask.isCancelled();
            if (handle instanceof TaskImplementation<?> taskImpl) return taskImpl.isCancelled();
            if (handle instanceof org.bukkit.scheduler.BukkitTask bukkitTask) return bukkitTask.isCancelled();
            return true;
        }

        /** Removes this task from the global task registry. */
        private void cleanup() {
            TASKS.remove(id);
        }
    }

    /** Private constructor to prevent instantiation. */
    public ThreadUtil() {}

    /**
     * Registers a task in the global task map.
     *
     * @param handle the underlying task object
     * @param label optional label for identification
     * @return a {@link ThreadTask} representing the registered task
     */
    private static ThreadTask register(Object handle, String label) {
        long id = TASK_ID_COUNTER.incrementAndGet();
        ThreadTask task = new ThreadTask(id, label, handle);
        TASKS.put(id, new WeakReference<>(task));
        return task;
    }

    /**
     * Retrieves a task by its unique ID.
     *
     * @param id the task ID
     * @return the {@link ThreadTask} or null if not found
     */
    public static ThreadTask getTask(long id) {
        WeakReference<ThreadTask> ref = TASKS.get(id);
        ThreadTask task = ref == null ? null : ref.get();
        if (task == null) TASKS.remove(id);
        return task;
    }

    /** @return a list of weak references to all currently registered tasks */
    public static List<WeakReference<ThreadTask>> getAllRunningTasks() {
        return TASKS.values().stream().toList();
    }

    /**
     * Runs a task safely on the main thread for a specific entity.
     *
     * @param entity the target entity
     * @param runnable the task to execute
     */
    public static void ensureEntity(@NotNull Entity entity, @NotNull Runnable runnable) {
        if (entity instanceof Player player && !player.isOnline()) return;
        if (isFolia()) {
            if (scheduler.isOwnedByCurrentRegion(entity) || SHUTTING_DOWN.get()) {
                runCatch(runnable, "Error in ensureEntity task on shutdown");
                return;
            }
            scheduler.entity(entity).execute(runnable, null, 1L);
        } else {
            if (Bukkit.isPrimaryThread()) {
                runCatch(runnable, "Error in ensureEntity task");
                return;
            }
            Bukkit.getScheduler().runTask(plugin, runnable);
        }
    }

    /**
     * Schedules a delayed task for an entity.
     *
     * @param entity the target entity
     * @param runnable the task to run
     * @param delay ticks to delay
     * @return the scheduled {@link ThreadTask}
     */
    public static ThreadTask ensureEntityLater(@NotNull Entity entity, @NotNull Runnable runnable, long delay) {
        return ensureEntityLater(entity, runnable, delay, null);
    }

    /**
     * Schedules a delayed task for an entity with optional label.
     *
     * @param entity the target entity
     * @param runnable the task to run
     * @param delay ticks to delay
     * @param label optional label for the task
     * @return the scheduled {@link ThreadTask} or null if entity offline
     */
    public static ThreadTask ensureEntityLater(@NotNull Entity entity, @NotNull Runnable runnable, long delay, String label) {
        if (entity instanceof Player player && !player.isOnline()) return null;
        delay = Math.max(1, delay);
        if (isFolia()) return register(scheduler.entity(entity).runDelayed(runnable, delay), label);
        return register(Bukkit.getScheduler().runTaskLater(plugin, runnable, delay), label);
    }

    /**
     * Schedules a repeating task for an entity.
     *
     * @param entity the target entity
     * @param runnable the task to run
     * @param delay initial delay
     * @param repeat repeat interval
     * @return the scheduled {@link ThreadTask}
     */
    public static ThreadTask ensureEntityTimer(@NotNull Entity entity, @NotNull Runnable runnable, long delay, long repeat) {
        return ensureEntityTimer(entity, runnable, delay, repeat, null);
    }

    /**
     * Schedules a repeating task for an entity with optional label.
     *
     * @param entity the target entity
     * @param runnable the task to run
     * @param delay initial delay
     * @param repeat repeat interval
     * @param label optional label for the task
     * @return the scheduled {@link ThreadTask} or null if entity offline
     */
    public static ThreadTask ensureEntityTimer(@NotNull Entity entity, @NotNull Runnable runnable, long delay, long repeat, String label) {
        if (entity instanceof Player player && !player.isOnline()) return null;
        delay = Math.max(1, delay);
        repeat = Math.max(1, repeat);
        if (isFolia()) {
            return register(
                    scheduler.entity(entity).runAtFixedRate(task -> {
                        if (!runCatch(runnable, "Error in ensureEntityTimer task")) task.cancel();
                    }, null, delay, repeat),
                    label
            );
        }
        return register(Bukkit.getScheduler().runTaskTimer(plugin, runnable, delay, repeat), label);
    }


    /**
     * Runs a task safely on the main thread for a specific location.
     *
     * @param location the target location
     * @param runnable the task to execute
     */
    public static void ensureLocation(@NotNull Location location, @NotNull Runnable runnable) {
        if (isFolia()) {
            if (scheduler.isOwnedByCurrentRegion(location) || SHUTTING_DOWN.get()) {
                runCatch(runnable, "Error in ensureLocation task on shutdown");
                return;
            }
            scheduler.region(location).execute(runnable);
        } else {
            if (Bukkit.isPrimaryThread()) {
                runCatch(runnable, "Error in ensureLocation task");
                return;
            }
            Bukkit.getScheduler().runTask(plugin, runnable);
        }
    }

    /**
     * Schedules a delayed task for a location.
     *
     * @param location the target location
     * @param runnable the task to run
     * @param delay ticks to delay
     * @return the scheduled {@link ThreadTask}
     */
    public static ThreadTask ensureLocationLater(@NotNull Location location, @NotNull Runnable runnable, long delay) {
        return ensureLocationLater(location, runnable, delay, null);
    }

    /**
     * Schedules a delayed task for a location with optional label.
     *
     * @param location the target location
     * @param runnable the task to run
     * @param delay ticks to delay
     * @param label optional label
     * @return the scheduled {@link ThreadTask}
     */
    public static ThreadTask ensureLocationLater(@NotNull Location location, @NotNull Runnable runnable, long delay, String label) {
        delay = Math.max(1, delay);
        if (isFolia()) return register(
                scheduler.region(location).runDelayed(task -> {
                    if (!runCatch(runnable, "Error in ensureLocationLater")) task.cancel();
                }, delay),
                label
        );
        return register(Bukkit.getScheduler().runTaskLater(plugin, runnable, delay), label);
    }

    /**
     * Schedules a repeating task for a location.
     *
     * @param location the target location
     * @param runnable the task to run
     * @param delay initial delay
     * @param repeat repeat interval
     * @return the scheduled {@link ThreadTask}
     */
    public static ThreadTask ensureLocationTimer(@NotNull Location location, @NotNull Runnable runnable, long delay, long repeat) {
        return ensureLocationTimer(location, runnable, delay, repeat, null);
    }

    /**
     * Schedules a repeating task for a location with optional label.
     *
     * @param location the target location
     * @param runnable the task to run
     * @param delay initial delay
     * @param repeat repeat interval
     * @param label optional label
     * @return the scheduled {@link ThreadTask}
     */
    public static ThreadTask ensureLocationTimer(@NotNull Location location, @NotNull Runnable runnable, long delay, long repeat, String label) {
        delay = Math.max(1, delay);
        repeat = Math.max(1, repeat);
        if (isFolia()) return register(
                scheduler.region(location).runAtFixedRate(task -> {
                    if (!runCatch(runnable, "Error in ensureLocationTimer task")) task.cancel();
                }, delay, repeat),
                label
        );
        return register(Bukkit.getScheduler().runTaskTimer(plugin, runnable, delay, repeat), label);
    }


    /**
     * Runs a task asynchronously.
     *
     * @param runnable the task to run
     */
    public static void runAsync(@NotNull Runnable runnable) {
        if (isFolia()) {
            if (SHUTTING_DOWN.get()) {
                runCatch(runnable, "Error in runAsync task on shutdown");
                return;
            }
            scheduler.async().runNow(task -> {
                if (!runCatch(runnable, "Error in runAsync task")) task.cancel();
            });
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
        }
    }

    /**
     * Runs a task asynchronously after a delay.
     *
     * @param runnable the task to run
     * @param delay ticks to delay
     * @return the scheduled {@link ThreadTask}
     */
    public static ThreadTask runAsyncLater(@NotNull Runnable runnable, long delay) {
        return runAsyncLater(runnable, delay, null);
    }

    /**
     * Runs a task asynchronously after a delay with optional label.
     *
     * @param runnable the task to run
     * @param delay ticks to delay
     * @param label optional label
     * @return the scheduled {@link ThreadTask}
     */
    public static ThreadTask runAsyncLater(@NotNull Runnable runnable, long delay, String label) {
        delay = Math.max(1, delay);
        if (isFolia()) return register(
                scheduler.async().runDelayed(task -> {
                    if (!runCatch(runnable, "Error in runAsyncLater task")) task.cancel();
                }, delay * 50L, TimeUnit.MILLISECONDS),
                label
        );
        return register(Bukkit.getScheduler().runTaskLater(plugin, runnable, delay), label);
    }

    /**
     * Runs a repeating asynchronous task.
     *
     * @param runnable the task to run
     * @param delay initial delay
     * @param repeat repeat interval
     * @return the scheduled {@link ThreadTask}
     */
    public static ThreadTask runAsyncTimer(@NotNull Runnable runnable, long delay, long repeat) {
        return runAsyncTimer(runnable, delay, repeat, null);
    }

    /**
     * Runs a repeating asynchronous task with optional label.
     *
     * @param runnable the task to run
     * @param delay initial delay
     * @param repeat repeat interval
     * @param label optional label
     * @return the scheduled {@link ThreadTask}
     */
    public static ThreadTask runAsyncTimer(@NotNull Runnable runnable, long delay, long repeat, String label) {
        delay = Math.max(1, delay);
        if (isFolia()) return register(
                scheduler.async().runAtFixedRate((Consumer<TaskImplementation<Void>>) task -> runnable.run(),
                        delay * 50L, repeat * 50L, TimeUnit.MILLISECONDS),
                label
        );
        return register(Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, delay, repeat), label);
    }


    /**
     * Runs a task globally on the main thread.
     *
     * @param runnable the task to run
     */
    public static void runGlobal(@NotNull Runnable runnable) {
        if (isFolia()) {
            if (SHUTTING_DOWN.get()) {
                runCatch(runnable, "Error in runGlobal task on shutdown");
                return;
            }
            scheduler.global().run(task -> {
                if (!runCatch(runnable, "Error in runGlobal task")) task.cancel();
            });
        } else {
            Bukkit.getScheduler().runTask(plugin, runnable);
        }
    }

    /**
     * Runs a global task after a delay.
     *
     * @param runnable the task to run
     * @param delay ticks to delay
     * @return the scheduled {@link ThreadTask}
     */
    public static ThreadTask runGlobalLater(@NotNull Runnable runnable, long delay) {
        return runGlobalLater(runnable, delay, null);
    }

    /**
     * Runs a global task after a delay with optional label.
     *
     * @param runnable the task to run
     * @param delay ticks to delay
     * @param label optional label
     * @return the scheduled {@link ThreadTask}
     */
    public static ThreadTask runGlobalLater(@NotNull Runnable runnable, long delay, String label) {
        delay = Math.max(1, delay);
        if (isFolia()) return register(
                scheduler.global().runDelayed(task -> {
                    if (!runCatch(runnable, "Error in runGlobalLater task")) task.cancel();
                }, delay),
                label
        );
        return register(Bukkit.getScheduler().runTaskLater(plugin, runnable, delay), label);
    }

    /**
     * Runs a repeating global task.
     *
     * @param runnable the task to run
     * @param delay initial delay
     * @param repeat repeat interval
     * @return the scheduled {@link ThreadTask}
     */
    public static ThreadTask runGlobalTimer(@NotNull Runnable runnable, long delay, long repeat) {
        return runGlobalTimer(runnable, delay, repeat, null);
    }

    /**
     * Runs a repeating global task with optional label.
     *
     * @param runnable the task to run
     * @param delay initial delay
     * @param repeat repeat interval
     * @param label optional label
     * @return the scheduled {@link ThreadTask}
     */
    public static ThreadTask runGlobalTimer(@NotNull Runnable runnable, long delay, long repeat, String label) {
        delay = Math.max(1, delay);
        if (isFolia()) return register(
                scheduler.global().runAtFixedRate(task -> {
                    if (!runCatch(runnable, "Error in runGlobalTimer task")) task.cancel();
                }, delay, repeat),
                label
        );
        return register(Bukkit.getScheduler().runTaskTimer(plugin, runnable, delay, repeat), label);
    }

    /**
     * Runs a runnable and logs any exceptions.
     *
     * @param runnable the task to run
     * @param error error message to log in case of exception
     * @return true if execution succeeded, false if exception occurred
     */
    private static boolean runCatch(Runnable runnable, String error) {
        try {
            runnable.run();
            return true;
        } catch (Exception e) {
            SpellCraftPlugin.log.log(Level.WARNING, error, e);
            return false;
        }
    }

    /**
     * Marks the scheduler as shutting down and clears all registered tasks.
     */
    public static void shutdown() {
        SHUTTING_DOWN.set(true);
        TASKS.clear();
    }
}
