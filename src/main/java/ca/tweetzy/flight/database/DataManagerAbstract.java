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

package ca.tweetzy.flight.database;

import ca.tweetzy.flight.database.query.QueryBuilder;
import ca.tweetzy.flight.database.repository.BaseRepository;
import ca.tweetzy.flight.database.repository.EntityMapper;
import ca.tweetzy.flight.database.repository.Repository;
import ca.tweetzy.flight.database.sync.DatabaseEvent;
import ca.tweetzy.flight.database.sync.DatabaseEventListener;
import ca.tweetzy.flight.database.sync.RedisSyncManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class DataManagerAbstract {
    protected final DatabaseConnector databaseConnector;
    protected final Plugin plugin;

    protected final ExecutorService asyncPool = Executors.newSingleThreadExecutor();
    
    private QueryBuilder queryBuilder;
    private RedisSyncManager redisSyncManager;
    private final Map<Class<?>, Repository<?, ?>> repositories = new ConcurrentHashMap<>();

    @Deprecated
    private static final Map<String, LinkedList<Runnable>> queues = new HashMap<>();

    public DataManagerAbstract(DatabaseConnector databaseConnector, Plugin plugin) {
        this.databaseConnector = databaseConnector;
        this.plugin = plugin;
    }

    /**
     * @return the prefix to be used by all table names
     */
    public String getTablePrefix() {
        return this.plugin.getDescription().getName().toLowerCase() + '_';
    }

    /**
     * Deprecated because it is often times not accurate to its use case. (+race-conditions)
     */
    @Deprecated
    protected int lastInsertedId(Connection connection) {
        return lastInsertedId(connection, null);
    }

    /**
     * Deprecated because it is often times not accurate to its use case. (+race-conditions)
     */
    @Deprecated
    protected int lastInsertedId(Connection connection, String table) {
        String select = "SELECT * FROM " + this.getTablePrefix() + table + " ORDER BY id DESC LIMIT 1";
        String query;
        if (this.databaseConnector instanceof SQLiteConnector) {
            query = table == null ? "SELECT last_insert_rowid()" : select;
        } else {
            query = table == null ? "SELECT LAST_INSERT_ID()" : select;
        }

        int id = -1;
        try (Statement statement = connection.createStatement()) {
            ResultSet result = statement.executeQuery(query);
            result.next();
            id = result.getInt(1);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return id;
    }

    /**
     * Queue a task to be run asynchronously. <br>
     *
     * @param runnable task to run
     */
    @Deprecated
    public void async(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, runnable);
    }

    /**
     * Queue a task to be run synchronously.
     *
     * @param runnable task to run on the next server tick
     */
    public void sync(Runnable runnable) {
        Bukkit.getScheduler().runTask(this.plugin, runnable);
    }

    public void runAsync(Runnable runnable) {
        runAsync(runnable, null);
    }

    // FIXME: The problem with a single threaded async queue is that the database implementations and this queue
    //        are **not** thread-safe in any way. The connection is not pooled or anything...
    //        So the actual problem is that plugins just queue way too much tasks on bulk which it just shouldn't need to do...
    public void runAsync(Runnable task, Consumer<Throwable> callback) {
        this.asyncPool.execute(() -> {
            try {
                task.run();

                if (callback != null) {
                    callback.accept(null);
                }
            } catch (Throwable th) {
                if (callback != null) {
                    callback.accept(th);
                    return;
                }

                th.printStackTrace();
            }
        });
    }

    public void shutdownTaskQueue() {
        this.asyncPool.shutdown();
    }

    public List<Runnable> forceShutdownTaskQueue() {
        return this.asyncPool.shutdownNow();
    }

    public boolean isTaskQueueTerminated() {
        return this.asyncPool.isTerminated();
    }

    public long getTaskQueueSize() {
        if (this.asyncPool instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor) this.asyncPool).getTaskCount();
        }

        return -1;
    }

    /**
     * @see ExecutorService#awaitTermination(long, TimeUnit)
     */
    public boolean waitForShutdown(long timeout, TimeUnit unit) throws InterruptedException {
        return this.asyncPool.awaitTermination(timeout, unit);
    }

    /**
     * Queue tasks to be run asynchronously.
     *
     * @param runnable task to put into queue.
     * @param queueKey the queue key to add the runnable to.
     */
    @Deprecated
    public void queueAsync(Runnable runnable, String queueKey) {
        if (queueKey == null) {
            return;
        }

        List<Runnable> queue = queues.computeIfAbsent(queueKey, t -> new LinkedList<>());
        queue.add(runnable);

        if (queue.size() == 1) {
            runQueue(queueKey);
        }
    }

    @Deprecated
    private void runQueue(String queueKey) {
        doQueue(queueKey, (s) -> {
            if (!queues.get(queueKey).isEmpty()) {
                runQueue(queueKey);
            }
        });
    }

    @Deprecated
    private void doQueue(String queueKey, Consumer<Boolean> callback) {
        Runnable runnable = queues.get(queueKey).getFirst();

        async(() -> {
            runnable.run();

            sync(() -> {
                queues.get(queueKey).remove(runnable);
                callback.accept(true);
            });
        });
    }
    
    /**
     * Get the QueryBuilder instance for this data manager
     * 
     * @return The QueryBuilder instance
     */
    @NotNull
    public QueryBuilder getQueryBuilder() {
        if (queryBuilder == null) {
            queryBuilder = new QueryBuilder(databaseConnector, getTablePrefix());
        }
        return queryBuilder;
    }
    
    /**
     * Get or create a repository for the given entity type
     * 
     * @param entityClass The entity class
     * @param tableName The table name (without prefix)
     * @param mapper The entity mapper
     * @return The repository instance
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public <T, ID> Repository<T, ID> getRepository(@NotNull Class<T> entityClass,
                                                     @NotNull String tableName,
                                                     @NotNull EntityMapper<T> mapper) {
        return (Repository<T, ID>) repositories.computeIfAbsent(entityClass, k -> 
            new BaseRepository<>(databaseConnector, getTablePrefix(), tableName, mapper)
        );
    }
    
    /**
     * Initialize Redis sync manager (optional)
     * 
     * @param host Redis host
     * @param port Redis port
     * @param password Redis password (null if no password)
     * @param channel Redis pub/sub channel name
     * @return true if initialization was successful
     */
    public boolean initializeRedisSync(@NotNull String host, int port, @Nullable String password, @NotNull String channel) {
        if (redisSyncManager != null) {
            plugin.getLogger().warning("Redis sync manager already initialized");
            return redisSyncManager.isEnabled();
        }
        
        redisSyncManager = new RedisSyncManager(plugin, channel);
        boolean success = redisSyncManager.initialize(host, port, password);
        
        if (success) {
            plugin.getLogger().info("Redis sync manager initialized for multi-server support");
        } else {
            plugin.getLogger().warning("Redis sync manager initialization failed - multi-server sync disabled");
        }
        
        return success;
    }
    
    /**
     * Get the Redis sync manager (may be null if not initialized)
     * 
     * @return The Redis sync manager, or null if not initialized
     */
    @Nullable
    public RedisSyncManager getRedisSyncManager() {
        return redisSyncManager;
    }
    
    /**
     * Register a database event listener
     * 
     * @param listener The listener to register
     */
    public void registerDatabaseEventListener(@NotNull DatabaseEventListener listener) {
        if (redisSyncManager != null && redisSyncManager.isEnabled()) {
            redisSyncManager.registerListener(listener);
        }
    }
    
    /**
     * Unregister a database event listener
     * 
     * @param listener The listener to unregister
     */
    public void unregisterDatabaseEventListener(@NotNull DatabaseEventListener listener) {
        if (redisSyncManager != null) {
            redisSyncManager.unregisterListener(listener);
        }
    }
    
    /**
     * Publish a database event to other servers (if Redis sync is enabled)
     * 
     * @param eventType The event type (INSERT, UPDATE, DELETE)
     * @param tableName The table name (without prefix)
     * @param data The data map (column name -> value)
     */
    public void publishDatabaseEvent(@NotNull DatabaseEvent.EventType eventType,
                                      @NotNull String tableName,
                                      @NotNull Map<String, Object> data) {
        if (redisSyncManager != null && redisSyncManager.isEnabled()) {
            DatabaseEvent event = new DatabaseEvent(
                redisSyncManager.getServerId(),
                eventType,
                tableName,
                getTablePrefix(),
                data
            );
            redisSyncManager.publishEvent(event);
        }
    }
    
    /**
     * Shutdown the data manager and cleanup resources
     */
    public void shutdown() {
        shutdownTaskQueue();
        
        if (redisSyncManager != null) {
            redisSyncManager.shutdown();
        }
        
        repositories.clear();
    }
}
