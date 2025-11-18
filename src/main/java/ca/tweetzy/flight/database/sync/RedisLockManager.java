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
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages distributed locks using Redis
 * Uses Redis SET NX EX pattern for atomic lock acquisition
 */
public class RedisLockManager {
    
    private final Plugin plugin;
    private final RedisSyncManager redisSyncManager;
    private final ConcurrentHashMap<String, LockInfo> activeLocks = new ConcurrentHashMap<>();
    private static final String LOCK_PREFIX = "lock:";
    private static final long DEFAULT_LOCK_TIMEOUT = 30; // seconds
    
    private static class LockInfo {
        final String lockValue;
        final long expirationTime;
        final Thread lockThread;
        
        LockInfo(String lockValue, long expirationTime, Thread lockThread) {
            this.lockValue = lockValue;
            this.expirationTime = expirationTime;
            this.lockThread = lockThread;
        }
    }
    
    public RedisLockManager(@NotNull Plugin plugin, @NotNull RedisSyncManager redisSyncManager) {
        this.plugin = plugin;
        this.redisSyncManager = redisSyncManager;
    }
    
    /**
     * Acquire a distributed lock
     * 
     * @param key The lock key
     * @param timeoutSeconds Lock timeout in seconds (auto-releases after this time)
     * @return true if lock was acquired, false otherwise
     */
    public boolean acquireLock(@NotNull String key, long timeoutSeconds) {
        if (!redisSyncManager.isEnabled()) {
            return false;
        }
        
        String lockKey = LOCK_PREFIX + key;
        String lockValue = UUID.randomUUID().toString();
        long expirationTime = System.currentTimeMillis() + (timeoutSeconds * 1000);
        
        JedisPool jedisPool = redisSyncManager.getJedisPool();
        if (jedisPool == null) {
            return false;
        }
        
        try (Jedis jedis = jedisPool.getResource()) {
            // Use Lua script for atomic lock acquisition with SET NX EX
            String luaScript = 
                "if redis.call('set', KEYS[1], ARGV[1], 'NX', 'EX', ARGV[2]) then " +
                "  return 'OK' " +
                "else " +
                "  return nil " +
                "end";
            
            List<String> keys = Arrays.asList(lockKey);
            List<String> args = Arrays.asList(lockValue, String.valueOf(timeoutSeconds));
            
            String result = (String) jedis.eval(luaScript, keys, args);
            
            if ("OK".equals(result)) {
                // Lock acquired successfully
                activeLocks.put(lockKey, new LockInfo(lockValue, expirationTime, Thread.currentThread()));
                return true;
            }
            
            return false;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to acquire lock " + key + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Acquire a lock with default timeout
     */
    public boolean acquireLock(@NotNull String key) {
        return acquireLock(key, DEFAULT_LOCK_TIMEOUT);
    }
    
    /**
     * Release a distributed lock
     * 
     * @param key The lock key
     * @return true if lock was released, false otherwise
     */
    public boolean releaseLock(@NotNull String key) {
        if (!redisSyncManager.isEnabled()) {
            return false;
        }
        
        String lockKey = LOCK_PREFIX + key;
        LockInfo lockInfo = activeLocks.get(lockKey);
        
        if (lockInfo == null) {
            return false; // Lock not held by this server
        }
        
        // Only release if held by current thread
        if (lockInfo.lockThread != Thread.currentThread()) {
            return false;
        }
        
        JedisPool jedisPool = redisSyncManager.getJedisPool();
        if (jedisPool == null) {
            return false;
        }
        
        try (Jedis jedis = jedisPool.getResource()) {
            // Use Lua script for atomic release (only release if value matches)
            String luaScript = 
                "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                "  return redis.call('del', KEYS[1]) " +
                "else " +
                "  return 0 " +
                "end";
            
            List<String> keys = Arrays.asList(lockKey);
            List<String> args = Arrays.asList(lockInfo.lockValue);
            
            Long result = (Long) jedis.eval(luaScript, keys, args);
            
            if (result != null && result > 0) {
                activeLocks.remove(lockKey);
                return true;
            }
            
            return false;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to release lock " + key + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if a lock is currently held
     * 
     * @param key The lock key
     * @return true if lock is held, false otherwise
     */
    public boolean isLocked(@NotNull String key) {
        if (!redisSyncManager.isEnabled()) {
            return false;
        }
        
        String lockKey = LOCK_PREFIX + key;
        
        JedisPool jedisPool = redisSyncManager.getJedisPool();
        if (jedisPool == null) {
            return false;
        }
        
        try (Jedis jedis = jedisPool.getResource()) {
            boolean exists = jedis.exists(lockKey);
            return exists;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to check lock " + key + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Renew a lock's expiration time
     * 
     * @param key The lock key
     * @param additionalSeconds Additional seconds to add to expiration
     * @return true if lock was renewed, false otherwise
     */
    public boolean renewLock(@NotNull String key, long additionalSeconds) {
        if (!redisSyncManager.isEnabled()) {
            return false;
        }
        
        String lockKey = LOCK_PREFIX + key;
        LockInfo lockInfo = activeLocks.get(lockKey);
        
        if (lockInfo == null) {
            return false; // Lock not held by this server
        }
        
        // Only renew if held by current thread
        if (lockInfo.lockThread != Thread.currentThread()) {
            return false;
        }
        
        JedisPool jedisPool = redisSyncManager.getJedisPool();
        if (jedisPool == null) {
            return false;
        }
        
        try (Jedis jedis = jedisPool.getResource()) {
            // Use Lua script to atomically renew lock
            String luaScript = 
                "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                "  return redis.call('expire', KEYS[1], ARGV[2]) " +
                "else " +
                "  return 0 " +
                "end";
            
            List<String> keys = Arrays.asList(lockKey);
            List<String> args = Arrays.asList(lockInfo.lockValue, String.valueOf(additionalSeconds));
            
            Long result = (Long) jedis.eval(luaScript, keys, args);
            
            if (result != null && result > 0) {
                // Update expiration time
                long newExpirationTime = System.currentTimeMillis() + (additionalSeconds * 1000);
                activeLocks.put(lockKey, new LockInfo(lockInfo.lockValue, newExpirationTime, lockInfo.lockThread));
                return true;
            }
            
            return false;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to renew lock " + key + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Clean up expired locks from local tracking
     */
    public void cleanupExpiredLocks() {
        long now = System.currentTimeMillis();
        activeLocks.entrySet().removeIf(entry -> entry.getValue().expirationTime < now);
    }
}
