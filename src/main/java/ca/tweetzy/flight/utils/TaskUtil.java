/*
 * Flight
 * Copyright 2022 Kiran Hart
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ca.tweetzy.flight.utils;

import ca.tweetzy.flight.FlightPlugin;
import com.tcoded.folialib.enums.EntityTaskResult;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Task utilities for better async/sync task management.
 * 
 * @author Kiran Hart
 */
public final class TaskUtil {
    
    /**
     * Run a task asynchronously
     * 
     * @param task The task to run
     * @return CompletableFuture
     */
    @NonNull
    public static CompletableFuture<Void> runAsync(@NonNull Runnable task) {
        return CompletableFuture.runAsync(() -> {
            try {
                task.run();
            } catch (Exception e) {
                //plugin.getLogger().severe("Error in async task: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    /**
     * Run a task asynchronously and then run a callback on the main thread
     * 
     * @param asyncTask The async task
     * @param syncCallback The sync callback
     * @return CompletableFuture
     */
    @NonNull
    public static CompletableFuture<Void> runAsyncThenSyncGlobally(@NonNull Runnable asyncTask, @NonNull Runnable syncCallback) {
        return runAsync(asyncTask).thenRun(() -> {
            FlightPlugin.getInstance().getScheduler().runNextTick(t -> syncCallback.run());
        });
    }

    /**
     * Executes the specified asynchronous task, and upon its completion, schedules and runs
     * the given synchronous callback at the specified location.
     *
     * @param asyncTask the task to run asynchronously
     * @param location the location where the synchronous callback should be executed
     * @param syncCallback the task to run synchronously at the specified location after the completion of the asynchronous task
     * @return a CompletableFuture representing the state of the asynchronous and synchronous tasks
     */
    @NonNull
    public static CompletableFuture<Void> runAsyncThenSyncAtLocation(@NonNull Runnable asyncTask, @NonNull Location location, @NonNull Runnable syncCallback) {
        return runAsync(asyncTask).thenRun(() -> {
            FlightPlugin.getInstance().getScheduler().runAtLocation(location, t -> syncCallback.run());
        });
    }

    /**
     * Executes an asynchronous task followed by a synchronous callback at the specified entity.
     *
     * @param asyncTask the asynchronous task to be executed first
     * @param entity the entity at which the synchronous callback will be run
     * @param syncCallback the synchronous callback to execute after the asynchronous task
     * @return a CompletableFuture representing the completion of both the asynchronous and synchronous operations
     */
    @NonNull
    public static CompletableFuture<Void> runAsyncThenSyncAtEntity(@NonNull Runnable asyncTask, @NonNull Entity entity, @NonNull Runnable syncCallback) {
        return runAsync(asyncTask).thenRun(() -> {
            FlightPlugin.getInstance().getScheduler().runAtEntity(entity, t -> syncCallback.run());
        });
    }
    
    /**
     * Run a task asynchronously with result
     * 
     * @param supplier The supplier that returns a result
     * @param <T> The result type
     * @return CompletableFuture with result
     */
    @NonNull
    public static <T> CompletableFuture<T> supplyAsync(@NonNull Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return supplier.get();
            } catch (Exception e) {
                //plugin.getLogger().severe("Error in async supplier: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        });
    }
    
    /**
     * Run a task asynchronously with result and then run callback on main thread
     * 
     * @param supplier The supplier that returns a result
     * @param syncCallback The sync callback that receives the result
     * @param <T> The result type
     * @return CompletableFuture
     */
    @NonNull
    public static <T> CompletableFuture<Void> supplyAsyncThenSyncGlobally(@NonNull Supplier<T> supplier, @NonNull Consumer<T> syncCallback) {
        return supplyAsync(supplier).thenAccept(result -> {
            FlightPlugin.getInstance().getScheduler().runNextTick(t -> syncCallback.accept(result));
        });
    }

    /**
     * Executes a task asynchronously that provides a result, and then performs a synchronous callback
     * at a specified location using the result.
     *
     * @param supplier The supplier that provides the result asynchronously
     * @param location The location where the synchronous callback should execute
     * @param syncCallback The synchronous callback that processes the result
     * @param <T> The type of the result provided by the supplier
     * @return A CompletableFuture that represents the completion of the asynchronous and synchronous tasks
     */
    @NonNull
    public static <T> CompletableFuture<Void> supplyAsyncThenSyncAtLocation(@NonNull Supplier<T> supplier, @NonNull Location location, @NonNull Consumer<T> syncCallback) {
        return supplyAsync(supplier).thenAccept(result -> {
            FlightPlugin.getInstance().getScheduler().runAtLocation(location, t -> syncCallback.accept(result));
        });
    }

    /**
     * Executes a task asynchronously to provide a result, and then performs a synchronous callback
     * at a specific entity using the result.
     *
     * @param supplier The supplier that provides the result asynchronously.
     * @param entity The entity where the synchronous callback should execute.
     * @param syncCallback The synchronous callback that processes the result.
     * @param <T> The type of the result provided by the supplier.
     * @return A CompletableFuture that represents the completion of the asynchronous and synchronous tasks.
     */
    @NonNull
    public static <T> CompletableFuture<Void> supplyAsyncThenSyncAtEntity(@NonNull Supplier<T> supplier, @NonNull Entity entity, @NonNull Consumer<T> syncCallback) {
        return supplyAsync(supplier).thenAccept(result -> {
            FlightPlugin.getInstance().getScheduler().runAtEntity(entity, t -> syncCallback.accept(result));
        });
    }
    
    /**
     * Run a task asynchronously with error handling
     * 
     * @param asyncTask The async task
     * @param syncCallback The sync callback
     * @param errorHandler The error handler
     * @return CompletableFuture
     */
    @NonNull
    public static CompletableFuture<Void> runAsyncWithError(@NonNull Runnable asyncTask,
                                                     @NonNull Runnable syncCallback,
                                                     @NonNull Consumer<Throwable> errorHandler) {
        return CompletableFuture.runAsync(() -> {
            try {
                asyncTask.run();
                FlightPlugin.getInstance().getScheduler().runNextTick(t -> syncCallback.run());
            } catch (Exception e) {
                FlightPlugin.getInstance().getScheduler().runNextTick(t -> errorHandler.accept(e));
            }
        });
    }
    
    /**
     * Run a task on the main thread
     * 
     * @param task The task to run
     * @return CompletableFuture of Void
     */
    @NonNull
    public static CompletableFuture<Void> runGlobally(@NonNull Runnable task) {
        return FlightPlugin.getInstance().getScheduler()
                .runNextTick(t -> task.run());
    }
    
    /**
     * Run a task on the main thread later
     * 
     * @param task The task to run
     * @param delay The delay in ticks
     * @return WrappedTask
     */
    @NonNull
    public static WrappedTask runGloballyLater(@NonNull Runnable task, long delay) {
        return FlightPlugin.getInstance().getScheduler()
                .runLater(task, delay);
    }
    
    /**
     * Run a repeating task on the main thread
     * 
     * @param task The task to run
     * @param delay The initial delay in ticks
     * @param period The period in ticks
     * @return WrappedTask
     */
    @NonNull
    public static WrappedTask runGloballyRepeating(@NonNull Runnable task, long delay, long period) {
        return FlightPlugin.getInstance().getScheduler()
                .runTimer(task, delay, period);
    }

    /**
     * Executes a task at a specific location asynchronously.
     *
     * @param location The location where the task is to be executed
     * @param task The task to run at the specified location
     * @return A CompletableFuture representing the asynchronous operation
     */
    @NonNull
    public static CompletableFuture<Void> runAtLocation(@NonNull Location location, @NonNull Runnable task) {
        return FlightPlugin.getInstance().getScheduler()
                .runAtLocation(location, t -> task.run());
    }

    /**
     * Schedules a task to be executed at a specific location after a specified delay.
     *
     * @param location The location where the task is to be executed
     * @param task The task to run at the specified location
     * @param delay The delay in ticks before the task is executed
     * @return A WrappedTask representing the scheduled task
     */
    @NonNull
    public static WrappedTask runAtLocationLater(@NonNull Location location, @NonNull Runnable task, long delay) {
        return FlightPlugin.getInstance().getScheduler()
                .runAtLocationLater(location, task, delay);
    }

    /**
     * Schedules a repeating task to be executed at a specific location after an initial delay
     * and with a specified period between each execution.
     *
     * @param location The location where the task is to be executed
     * @param task The task to run at the specified location
     * @param delay The initial delay in ticks before the task is first executed
     * @param period The period in ticks between successive executions of the task
     * @return A WrappedTask representing the scheduled repeating task
     */
    @NonNull
    public static WrappedTask runAtLocationRepeating(@NonNull Location location, @NonNull Runnable task, long delay, long period) {
        return FlightPlugin.getInstance().getScheduler()
                .runAtLocationTimer(location, task, delay, period);
    }

    /**
     * Executes a task associated with a specific entity asynchronously.
     *
     * @param entity The entity where the task is to be executed
     * @param task   The task to run at the specified entity
     * @return A CompletableFuture representing the asynchronous operation
     */
    @NonNull
    public static CompletableFuture<EntityTaskResult> runAtEntity(@NonNull Entity entity, @NonNull Runnable task) {
        return FlightPlugin.getInstance().getScheduler()
                .runAtEntity(entity, t -> task.run());
    }

    /**
     * Schedules a task to be executed at a specific entity after a specified delay.
     *
     * @param entity The entity where the task is to be executed.
     * @param task   The task to run at the specified entity.
     * @param delay  The delay in ticks before the task is executed.
     * @return A WrappedTask representing the scheduled task.
     */
    @NonNull
    public static WrappedTask runAtEntityLater(@NonNull Entity entity, @NonNull Runnable task, long delay) {
        return FlightPlugin.getInstance().getScheduler()
                .runAtEntityLater(entity, task, delay);
    }

    /**
     * Schedules a repeating task to be executed at a specific entity with an initial delay
     * and a fixed period between executions.
     *
     * @param entity The entity where the task is to be executed.
     * @param task The task to run at the specified entity.
     * @param delay The initial delay in ticks before the task is first executed.
     * @param period The period in ticks between successive executions of the task.
     * @return A WrappedTask representing the scheduled repeating task.
     */
    @NonNull
    public static WrappedTask runAtEntityRepeating(@NonNull Entity entity, @NonNull Runnable task, long delay, long period) {
        return FlightPlugin.getInstance().getScheduler()
                .runAtEntityTimer(entity, task, delay, period);
    }
    
    /**
     * Run a repeating task asynchronously
     * 
     * @param task The task to run
     * @param delay The initial delay in ticks
     * @param period The period in ticks
     * @return WrappedTask
     */
    @NonNull
    public static WrappedTask runAsyncRepeating(@NonNull Runnable task, long delay, long period) {
        return FlightPlugin.getInstance().getScheduler()
                .runTimerAsync(task, delay, period);
    }
    
    /**
     * Chain async and sync tasks
     * 
     * @param asyncTask The async task
     * @return TaskChain for chaining
     */
    @NonNull
    public static TaskChain chain(@NonNull Runnable asyncTask) {
        return new TaskChain(asyncTask);
    }
    
    /**
     * Task chain builder
     */
    public static class TaskChain {
        private CompletableFuture<Void> future;
        
        private TaskChain(@NonNull Runnable asyncTask) {
            this.future = CompletableFuture.runAsync(asyncTask);
        }
        
        /**
         * Then run on main thread
         */
        @NonNull
        public TaskChain thenGlobally(@NonNull Runnable syncTask) {
            future = future.thenRun(() -> {
                FlightPlugin.getInstance().getScheduler().runNextTick(t -> syncTask.run());
            });
            return this;
        }

        /**
         * Adds a synchronous task to the task chain, which will be executed at the specified location
         * on the main thread after all previous tasks in the chain are complete.
         *
         * @param location the location where the task will be executed
         * @param syncTask the task to be executed at the specified location
         * @return the current task chain instance for chaining additional tasks
         */
        public TaskChain thenAtLocation(@NonNull Location location, @NonNull Runnable syncTask) {
            future = future.thenRun(() -> {
                FlightPlugin.getInstance().getScheduler().runAtLocation(location, t -> syncTask.run());
            });
            return this;
        }

        /**
         * Adds a synchronous task to the task chain, which will be executed in the context of the specified
         * {@link Entity} on the main thread after all previous tasks in the chain are complete.
         *
         * @param entity   the entity in whose context the task will be executed
         * @param syncTask the task to be executed at the specified entity context
         * @return the current task chain instance for chaining additional tasks
         */
        public TaskChain thenAtEntity(@NonNull Entity entity, @NonNull Runnable syncTask) {
            future = future.thenRun(() -> {
                FlightPlugin.getInstance().getScheduler().runAtEntity(entity, t -> syncTask.run());
            });
            return this;
        }
        
        /**
         * Then run async
         */
        @NonNull
        public TaskChain thenAsync(@NonNull Runnable asyncTask) {
            future = future.thenRunAsync(asyncTask);
            return this;
        }
        
        /**
         * Handle errors
         */
        @NonNull
        public TaskChain onError(@NonNull Consumer<Throwable> errorHandler) {
            future = future.exceptionally(throwable -> {
                FlightPlugin.getInstance().getScheduler()
                                .runNextTick(t -> errorHandler.accept(throwable));
                return null;
            });
            return this;
        }
    }
}

