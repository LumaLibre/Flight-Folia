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

import ca.tweetzy.flight.collection.expiringmap.ExpiringMap;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Player data caching system to reduce database queries.
 * 
 * @author Kiran Hart
 */
public class PlayerDataCache<T> {
    
    private final Plugin plugin;
    private final ExpiringMap<String, T> cache;
    private final long expirationMinutes;
    
    public PlayerDataCache(@NonNull Plugin plugin) {
        this(plugin, 30); // Default 30 minutes
    }
    
    public PlayerDataCache(@NonNull Plugin plugin, long expirationMinutes) {
        this.plugin = plugin;
        this.expirationMinutes = expirationMinutes;
        this.cache = ExpiringMap.builder()
            .expiration(expirationMinutes, TimeUnit.MINUTES)
            .build();
    }
    
    /**
     * Get cached data or load it if not cached
     * 
     * @param player The player
     * @param key The cache key
     * @param loader The data loader function
     * @return The cached or loaded data
     */
    @NonNull
    public T get(@NonNull Player player, @NonNull String key, @NonNull Supplier<T> loader) {
        return get(player.getUniqueId(), key, loader);
    }
    
    /**
     * Get cached data or load it if not cached
     * 
     * @param uuid The player UUID
     * @param key The cache key
     * @param loader The data loader function
     * @return The cached or loaded data
     */
    @NonNull
    public T get(@NonNull UUID uuid, @NonNull String key, @NonNull Supplier<T> loader) {
        String cacheKey = uuid.toString() + ":" + key;
        T data = cache.get(cacheKey);
        
        if (data == null) {
            data = loader.get();
            if (data != null) {
                cache.put(cacheKey, data, expirationMinutes, TimeUnit.MINUTES);
            }
        }
        
        return data;
    }
    
    /**
     * Put data into cache
     * 
     * @param player The player
     * @param key The cache key
     * @param data The data to cache
     */
    public void put(@NonNull Player player, @NonNull String key, @NonNull T data) {
        put(player.getUniqueId(), key, data);
    }
    
    /**
     * Put data into cache
     * 
     * @param uuid The player UUID
     * @param key The cache key
     * @param data The data to cache
     */
    public void put(@NonNull UUID uuid, @NonNull String key, @NonNull T data) {
        String cacheKey = uuid.toString() + ":" + key;
        cache.put(cacheKey, data, expirationMinutes, TimeUnit.MINUTES);
    }
    
    /**
     * Invalidate cached data for a player
     * 
     * @param player The player
     * @param key The cache key
     */
    public void invalidate(@NonNull Player player, @NonNull String key) {
        invalidate(player.getUniqueId(), key);
    }
    
    /**
     * Invalidate cached data for a player
     * 
     * @param uuid The player UUID
     * @param key The cache key
     */
    public void invalidate(@NonNull UUID uuid, @NonNull String key) {
        String cacheKey = uuid.toString() + ":" + key;
        cache.remove(cacheKey);
    }
    
    /**
     * Invalidate all cached data for a player
     * 
     * @param player The player
     */
    public void invalidateAll(@NonNull Player player) {
        invalidateAll(player.getUniqueId());
    }
    
    /**
     * Invalidate all cached data for a player
     * 
     * @param uuid The player UUID
     */
    public void invalidateAll(@NonNull UUID uuid) {
        String prefix = uuid.toString() + ":";
        cache.keySet().removeIf(key -> key != null && key.startsWith(prefix));
    }
    
    /**
     * Clear all cached data
     */
    public void clear() {
        cache.clear();
    }
    
    /**
     * Check if data is cached
     * 
     * @param player The player
     * @param key The cache key
     * @return true if cached
     */
    public boolean isCached(@NonNull Player player, @NonNull String key) {
        return isCached(player.getUniqueId(), key);
    }
    
    /**
     * Check if data is cached
     * 
     * @param uuid The player UUID
     * @param key The cache key
     * @return true if cached
     */
    public boolean isCached(@NonNull UUID uuid, @NonNull String key) {
        String cacheKey = uuid.toString() + ":" + key;
        return cache.containsKey(cacheKey);
    }
}

