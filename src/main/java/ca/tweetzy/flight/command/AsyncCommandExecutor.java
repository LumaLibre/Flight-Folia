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

package ca.tweetzy.flight.command;

import ca.tweetzy.flight.FlightPlugin;
import ca.tweetzy.flight.utils.Common;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Async command executor with proper callback support
 * Provides true async execution for I/O-heavy commands
 */
public final class AsyncCommandExecutor {

    private final JavaPlugin plugin;

    public AsyncCommandExecutor(@NonNull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Execute a command asynchronously
     * 
     * @param asyncTask Task to run asynchronously (can perform I/O operations)
     * @param syncCallback Callback to run on main thread after async task completes
     * @param errorHandler Error handler (runs on main thread)
     */
    public void executeAsyncThenGlobal(@NonNull Runnable asyncTask,
                            @NonNull Runnable syncCallback,
                            @NonNull Consumer<Throwable> errorHandler) {
        CompletableFuture.runAsync(() -> {
            try {
                asyncTask.run();
                // Schedule callback on global thread
                FlightPlugin.getInstance().getScheduler().runNextTick(t -> syncCallback.run());
            } catch (Exception e) {
                // Schedule error handler on global thread
                FlightPlugin.getInstance().getScheduler().runNextTick(t -> errorHandler.accept(e));
            }
        });
    }

    /**
     * Execute a command asynchronously with result
     * 
     * @param asyncTask Task that returns a result
     * @param syncCallback Callback that receives the result (runs on main thread)
     * @param errorHandler Error handler (runs on main thread)
     * @param <T> Result type
     */
    public <T> void executeAsyncThenGlobal(@NonNull java.util.function.Supplier<T> asyncTask,
                                 @NonNull Consumer<T> syncCallback,
                                 @NonNull Consumer<Throwable> errorHandler) {
        CompletableFuture.supplyAsync(asyncTask)
            .thenAccept(result -> {
                // Schedule callback on global thread
                FlightPlugin.getInstance().getScheduler().runNextTick(t -> syncCallback.accept(result));
            })
            .exceptionally(throwable -> {
                // Schedule error handler on global thread
                FlightPlugin.getInstance().getScheduler().runNextTick(t -> errorHandler.accept(throwable));
                return null;
            });
    }

    /**
     * Execute a command asynchronously with CommandContext
     * 
     * @param context Command context
     * @param asyncTask Async task that uses the context
     * @param syncCallback Sync callback after async task
     * @param errorHandler Error handler
     */
    public void executeAsyncThenGlobal(@NonNull CommandContext context,
                            @NonNull Consumer<CommandContext> asyncTask,
                            @NonNull Consumer<CommandContext> syncCallback,
                            @NonNull Consumer<Throwable> errorHandler) {
        CompletableFuture.runAsync(() -> {
            try {
                asyncTask.accept(context);
                // Schedule callback on global thread
                FlightPlugin.getInstance().getScheduler().runNextTick(t -> syncCallback.accept(context));
            } catch (Exception e) {
                // Schedule error handler on global thread
                FlightPlugin.getInstance().getScheduler().runNextTick(t -> errorHandler.accept(e));
            }
        });
    }

    /**
     * Execute a command asynchronously and send message to sender
     */
    public void executeAsyncWithMessage(@NonNull CommandSender sender,
                                       @NonNull Runnable asyncTask,
                                       @NonNull String successMessage,
                                       @NonNull String errorMessage) {
        executeAsyncThenGlobal(
            asyncTask,
            () -> {
                if (successMessage != null && !successMessage.isEmpty()) {
                    sender.sendMessage(successMessage);
                }
            },
            throwable -> {
                Common.log("&cAsync command error: " + throwable.getMessage());
                if (errorMessage != null && !errorMessage.isEmpty()) {
                    sender.sendMessage(errorMessage);
                }
            }
        );
    }
}

