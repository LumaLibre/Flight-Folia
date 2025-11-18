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

package ca.tweetzy.flight.database.sync;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manages Redis-based database synchronization for multi-server setups
 */
public class RedisSyncManager {
    
    private final Plugin plugin;
    private final String serverId;
    private final String channel;
    private JedisPool jedisPool;
    private JedisPubSub pubSub;
    private final List<DatabaseEventListener> listeners = new ArrayList<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private boolean enabled = false;
    private Thread subscriberThread;
    
    public RedisSyncManager(@NotNull Plugin plugin, @NotNull String channel) {
        this.plugin = plugin;
        this.serverId = UUID.randomUUID().toString();
        this.channel = channel;
    }
    
    /**
     * Initialize Redis connection
     * 
     * @param host Redis host
     * @param port Redis port
     * @param password Redis password (null if no password)
     * @return true if initialization was successful
     */
    public boolean initialize(@NotNull String host, int port, @Nullable String password) {
        try {
            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(10);
            poolConfig.setMaxIdle(5);
            poolConfig.setMinIdle(1);
            poolConfig.setTestOnBorrow(true);
            poolConfig.setTestOnReturn(true);
            
            if (password != null && !password.isEmpty()) {
                this.jedisPool = new JedisPool(poolConfig, host, port, 2000, password);
            } else {
                this.jedisPool = new JedisPool(poolConfig, host, port, 2000);
            }
            
            // Test connection
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.ping();
            }
            
            this.enabled = true;
            startSubscriber();
            
            plugin.getLogger().info("Redis sync manager initialized successfully");
            return true;
        } catch (Exception ex) {
            plugin.getLogger().warning("Failed to initialize Redis sync manager: " + ex.getMessage());
            ex.printStackTrace();
            this.enabled = false;
            return false;
        }
    }
    
    /**
     * Check if Redis sync is enabled and connected
     */
    public boolean isEnabled() {
        return enabled && jedisPool != null && !jedisPool.isClosed();
    }
    
    /**
     * Publish a database event to other servers
     */
    public void publishEvent(@NotNull DatabaseEvent event) {
        if (!isEnabled()) {
            return;
        }
        
        CompletableFuture.runAsync(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                String json = event.toJson();
                jedis.publish(channel, json);
            } catch (Exception ex) {
                plugin.getLogger().warning("Failed to publish database event: " + ex.getMessage());
                ex.printStackTrace();
            }
        }, executorService);
    }
    
    /**
     * Register a listener for database events
     */
    public void registerListener(@NotNull DatabaseEventListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }
    
    /**
     * Unregister a listener
     */
    public void unregisterListener(@NotNull DatabaseEventListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
    
    /**
     * Start the Redis subscriber thread
     */
    private void startSubscriber() {
        if (subscriberThread != null && subscriberThread.isAlive()) {
            return;
        }
        
        pubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                try {
                    DatabaseEvent event = DatabaseEvent.fromJson(message);
                    
                    // Don't process events from this server
                    if (event.getServerId().equals(serverId)) {
                        return;
                    }
                    
                    // Notify listeners
                    synchronized (listeners) {
                        for (DatabaseEventListener listener : listeners) {
                            try {
                                // Check if listener is interested in this event
                                String listenerTable = listener.getTableName();
                                String listenerPrefix = listener.getTablePrefix();
                                
                                if (listenerTable != null && !listenerTable.equals(event.getTableName())) {
                                    continue;
                                }
                                
                                if (listenerPrefix != null && !listenerPrefix.equals(event.getTablePrefix())) {
                                    continue;
                                }
                                
                                listener.onDatabaseEvent(event);
                            } catch (Exception ex) {
                                plugin.getLogger().warning("Error in database event listener: " + ex.getMessage());
                                ex.printStackTrace();
                            }
                        }
                    }
                } catch (Exception ex) {
                    plugin.getLogger().warning("Failed to process database event: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
            
            @Override
            public void onSubscribe(String channel, int subscribedChannels) {
                plugin.getLogger().info("Subscribed to Redis channel: " + channel);
            }
            
            @Override
            public void onUnsubscribe(String channel, int subscribedChannels) {
                plugin.getLogger().info("Unsubscribed from Redis channel: " + channel);
            }
        };
        
        subscriberThread = new Thread(() -> {
            while (enabled && !Thread.currentThread().isInterrupted()) {
                try (Jedis jedis = jedisPool.getResource()) {
                    jedis.subscribe(pubSub, channel);
                } catch (Exception ex) {
                    if (enabled) {
                        plugin.getLogger().warning("Redis subscriber error: " + ex.getMessage());
                        try {
                            Thread.sleep(5000); // Wait before reconnecting
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
        }, "Redis-Subscriber-" + plugin.getName());
        
        subscriberThread.setDaemon(true);
        subscriberThread.start();
    }
    
    /**
     * Shutdown the Redis sync manager
     */
    public void shutdown() {
        enabled = false;
        
        if (pubSub != null) {
            try {
                pubSub.unsubscribe();
            } catch (Exception ex) {
                // Ignore
            }
        }
        
        if (subscriberThread != null && subscriberThread.isAlive()) {
            subscriberThread.interrupt();
            try {
                subscriberThread.join(5000);
            } catch (InterruptedException ex) {
                // Ignore
            }
        }
        
        if (jedisPool != null && !jedisPool.isClosed()) {
            jedisPool.close();
        }
        
        executorService.shutdown();
        
        plugin.getLogger().info("Redis sync manager shut down");
    }
    
    /**
     * Get the server ID
     */
    @NotNull
    public String getServerId() {
        return serverId;
    }
}

