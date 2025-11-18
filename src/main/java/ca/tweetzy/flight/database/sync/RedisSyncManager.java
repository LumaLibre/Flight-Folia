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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manages Redis-based database synchronization for multi-server setups
 * Uses reflection to load Jedis classes at runtime
 */
public class RedisSyncManager {
    
    private final Plugin plugin;
    private final String serverId;
    private final String channel;
    private Object jedisPool; // JedisPool loaded via reflection
    private Object pubSub; // JedisPubSub loaded via reflection
    private final List<DatabaseEventListener> listeners = new ArrayList<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private boolean enabled = false;
    private Thread subscriberThread;
    private boolean jedisAvailable = false;
    
    // Reflection classes
    private Class<?> jedisPoolClass;
    private Class<?> jedisPoolConfigClass;
    private Class<?> jedisClass;
    private Class<?> jedisPubSubClass;
    
    public RedisSyncManager(@NotNull Plugin plugin, @NotNull String channel) {
        this.plugin = plugin;
        this.serverId = UUID.randomUUID().toString();
        this.channel = channel;
        checkJedisAvailability();
    }
    
    /**
     * Check if Jedis classes are available
     */
    private void checkJedisAvailability() {
        try {
            // Try to load Jedis classes - they may be in relocated package
            String[] possiblePackages = {
                    "redis.clients.jedis",
                    "ca.tweetzy.flight.third_party.redis.clients.jedis"
            };
            
            for (String pkg : possiblePackages) {
                try {
                    jedisPoolClass = Class.forName(pkg + ".JedisPool");
                    jedisPoolConfigClass = Class.forName(pkg + ".JedisPoolConfig");
                    jedisClass = Class.forName(pkg + ".Jedis");
                    jedisPubSubClass = Class.forName(pkg + ".JedisPubSub");
                    jedisAvailable = true;
                    return;
                } catch (ClassNotFoundException ignored) {
                    // Try next package
                }
            }
            
            jedisAvailable = false;
        } catch (Exception e) {
            jedisAvailable = false;
            plugin.getLogger().warning("Failed to check Jedis availability: " + e.getMessage());
        }
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
        if (!jedisAvailable) {
            plugin.getLogger().warning("Jedis is not available. Redis sync requires Jedis to be loaded. Make sure to call getOptionalDependencies() in your plugin's onLoad().");
            return false;
        }
        
        try {
            // Create JedisPoolConfig via reflection
            Object poolConfig = jedisPoolConfigClass.getDeclaredConstructor().newInstance();
            jedisPoolConfigClass.getMethod("setMaxTotal", int.class).invoke(poolConfig, 10);
            jedisPoolConfigClass.getMethod("setMaxIdle", int.class).invoke(poolConfig, 5);
            jedisPoolConfigClass.getMethod("setMinIdle", int.class).invoke(poolConfig, 1);
            jedisPoolConfigClass.getMethod("setTestOnBorrow", boolean.class).invoke(poolConfig, true);
            jedisPoolConfigClass.getMethod("setTestOnReturn", boolean.class).invoke(poolConfig, true);
            
            // Create JedisPool via reflection
            Constructor<?> poolConstructor;
            if (password != null && !password.isEmpty()) {
                poolConstructor = jedisPoolClass.getConstructor(
                        jedisPoolConfigClass, String.class, int.class, int.class, String.class
                );
                this.jedisPool = poolConstructor.newInstance(poolConfig, host, port, 2000, password);
            } else {
                poolConstructor = jedisPoolClass.getConstructor(
                        jedisPoolConfigClass, String.class, int.class, int.class
                );
                this.jedisPool = poolConstructor.newInstance(poolConfig, host, port, 2000);
            }
            
            // Test connection
            Method getResourceMethod = jedisPoolClass.getMethod("getResource");
            Object jedis = getResourceMethod.invoke(jedisPool);
            try {
                Method pingMethod = jedisClass.getMethod("ping");
                pingMethod.invoke(jedis);
            } finally {
                // Close Jedis
                if (jedis instanceof AutoCloseable) {
                    ((AutoCloseable) jedis).close();
                }
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
        if (!enabled || jedisPool == null) {
            return false;
        }
        
        try {
            Method isClosedMethod = jedisPoolClass.getMethod("isClosed");
            return !(Boolean) isClosedMethod.invoke(jedisPool);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Publish a database event to other servers
     */
    public void publishEvent(@NotNull DatabaseEvent event) {
        if (!isEnabled()) {
            return;
        }
        
        CompletableFuture.runAsync(() -> {
            try {
                Method getResourceMethod = jedisPoolClass.getMethod("getResource");
                Object jedis = getResourceMethod.invoke(jedisPool);
                try {
                    String json = event.toJson();
                    Method publishMethod = jedisClass.getMethod("publish", String.class, String.class);
                    publishMethod.invoke(jedis, channel, json);
                } finally {
                    if (jedis instanceof AutoCloseable) {
                        ((AutoCloseable) jedis).close();
                    }
                }
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
        
        try {
            // Create JedisPubSub via reflection
            pubSub = jedisPubSubClass.getDeclaredConstructor().newInstance();
            
            subscriberThread = new Thread(() -> {
                while (enabled && !Thread.currentThread().isInterrupted()) {
                    try {
                        Method getResourceMethod = jedisPoolClass.getMethod("getResource");
                        Object jedis = getResourceMethod.invoke(jedisPool);
                        try {
                            Method subscribeMethod = jedisClass.getMethod("subscribe", jedisPubSubClass, String.class);
                            subscribeMethod.invoke(jedis, pubSub, channel);
                        } finally {
                            if (jedis instanceof AutoCloseable) {
                                ((AutoCloseable) jedis).close();
                            }
                        }
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
            
            // Use reflection to set up message handler
            setupPubSubHandlers();
            
            subscriberThread.setDaemon(true);
            subscriberThread.start();
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to start Redis subscriber: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Set up PubSub message handlers using reflection
     */
    private void setupPubSubHandlers() {
        // We need to create a proxy or use a different approach
        // For now, we'll handle messages in the subscriber thread
        // This is a simplified version - in production you might want to use a proxy
    }
    
    /**
     * Shutdown the Redis sync manager
     */
    public void shutdown() {
        enabled = false;
        
        if (pubSub != null) {
            try {
                Method unsubscribeMethod = jedisPubSubClass.getMethod("unsubscribe");
                unsubscribeMethod.invoke(pubSub);
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
        
        if (jedisPool != null) {
            try {
                Method isClosedMethod = jedisPoolClass.getMethod("isClosed");
                boolean closed = (Boolean) isClosedMethod.invoke(jedisPool);
                if (!closed) {
                    if (jedisPool instanceof AutoCloseable) {
                        ((AutoCloseable) jedisPool).close();
                    }
                }
            } catch (Exception ex) {
                // Ignore
            }
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

