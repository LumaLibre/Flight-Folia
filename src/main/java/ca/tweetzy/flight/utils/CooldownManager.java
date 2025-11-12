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
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Cooldown management system for players and actions.
 * Supports per-player cooldowns, global cooldowns, and persistent cooldowns.
 * 
 * @author Kiran Hart
 */
public class CooldownManager {
    
    private final JavaPlugin plugin;
    private final Map<UUID, Map<String, Long>> playerCooldowns = new ConcurrentHashMap<>();
    private final Map<String, Long> globalCooldowns = new ConcurrentHashMap<>();
    private final ExpiringMap<String, Long> persistentCooldowns;
    private String bypassPermission = null;
    
    public CooldownManager(@NonNull JavaPlugin plugin) {
        this.plugin = plugin;
        this.persistentCooldowns = ExpiringMap.builder()
            .expiration(1, TimeUnit.HOURS)
            .build();
        
        // Cleanup task to remove expired cooldowns
        new BukkitRunnable() {
            @Override
            public void run() {
                cleanupExpiredCooldowns();
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 20L * 60L); // Every minute
    }
    
    /**
     * Set a cooldown for a player and action
     * 
     * @param player The player
     * @param action The action identifier
     * @param seconds Cooldown duration in seconds
     */
    public void setCooldown(@NonNull Player player, @NonNull String action, long seconds) {
        setCooldown(player.getUniqueId(), action, seconds);
    }
    
    /**
     * Set a cooldown for a player UUID and action
     * 
     * @param uuid The player UUID
     * @param action The action identifier
     * @param seconds Cooldown duration in seconds
     */
    public void setCooldown(@NonNull UUID uuid, @NonNull String action, long seconds) {
        long expiryTime = System.currentTimeMillis() + (seconds * 1000L);
        playerCooldowns.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).put(action, expiryTime);
    }
    
    /**
     * Set a global cooldown (all players share)
     * 
     * @param action The action identifier
     * @param seconds Cooldown duration in seconds
     */
    public void setGlobalCooldown(@NonNull String action, long seconds) {
        long expiryTime = System.currentTimeMillis() + (seconds * 1000L);
        globalCooldowns.put(action, expiryTime);
    }
    
    /**
     * Set a persistent cooldown (survives server restarts, stored in memory)
     * 
     * @param key Unique key for the cooldown
     * @param seconds Cooldown duration in seconds
     */
    public void setPersistentCooldown(@NonNull String key, long seconds) {
        long expiryTime = System.currentTimeMillis() + (seconds * 1000L);
        persistentCooldowns.put(key, expiryTime, seconds, TimeUnit.SECONDS);
    }
    
    /**
     * Check if a player is on cooldown for an action
     * 
     * @param player The player
     * @param action The action identifier
     * @return true if on cooldown
     */
    public boolean isOnCooldown(@NonNull Player player, @NonNull String action) {
        return isOnCooldown(player.getUniqueId(), action);
    }
    
    /**
     * Check if a player UUID is on cooldown for an action
     * 
     * @param uuid The player UUID
     * @param action The action identifier
     * @return true if on cooldown
     */
    public boolean isOnCooldown(@NonNull UUID uuid, @NonNull String action) {
        // Check bypass permission
        Player player = plugin.getServer().getPlayer(uuid);
        if (player != null && bypassPermission != null && player.hasPermission(bypassPermission)) {
            return false;
        }
        
        // Check global cooldown first
        Long globalExpiry = globalCooldowns.get(action);
        if (globalExpiry != null && System.currentTimeMillis() < globalExpiry) {
            return true;
        }
        
        // Check player cooldown
        Map<String, Long> playerCooldownMap = playerCooldowns.get(uuid);
        if (playerCooldownMap != null) {
            Long expiry = playerCooldownMap.get(action);
            if (expiry != null && System.currentTimeMillis() < expiry) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check if a persistent cooldown is active
     * 
     * @param key The cooldown key
     * @return true if on cooldown
     */
    public boolean isPersistentCooldownActive(@NonNull String key) {
        return persistentCooldowns.containsKey(key);
    }
    
    /**
     * Get remaining cooldown time in seconds
     * 
     * @param player The player
     * @param action The action identifier
     * @return Remaining seconds, or 0 if not on cooldown
     */
    public long getRemainingTime(@NonNull Player player, @NonNull String action) {
        return getRemainingTime(player.getUniqueId(), action);
    }
    
    /**
     * Get remaining cooldown time in seconds
     * 
     * @param uuid The player UUID
     * @param action The action identifier
     * @return Remaining seconds, or 0 if not on cooldown
     */
    public long getRemainingTime(@NonNull UUID uuid, @NonNull String action) {
        long currentTime = System.currentTimeMillis();
        
        // Check global cooldown first
        Long globalExpiry = globalCooldowns.get(action);
        if (globalExpiry != null && currentTime < globalExpiry) {
            return (globalExpiry - currentTime) / 1000L;
        }
        
        // Check player cooldown
        Map<String, Long> playerCooldownMap = playerCooldowns.get(uuid);
        if (playerCooldownMap != null) {
            Long expiry = playerCooldownMap.get(action);
            if (expiry != null && currentTime < expiry) {
                return (expiry - currentTime) / 1000L;
            }
        }
        
        return 0;
    }
    
    /**
     * Get remaining persistent cooldown time in seconds
     * 
     * @param key The cooldown key
     * @return Remaining seconds, or 0 if not on cooldown
     */
    public long getPersistentRemainingTime(@NonNull String key) {
        Long expiry = persistentCooldowns.get(key);
        if (expiry != null) {
            long remaining = (expiry - System.currentTimeMillis()) / 1000L;
            return Math.max(0, remaining);
        }
        return 0;
    }
    
    /**
     * Remove a cooldown for a player
     * 
     * @param player The player
     * @param action The action identifier
     */
    public void removeCooldown(@NonNull Player player, @NonNull String action) {
        removeCooldown(player.getUniqueId(), action);
    }
    
    /**
     * Remove a cooldown for a player UUID
     * 
     * @param uuid The player UUID
     * @param action The action identifier
     */
    public void removeCooldown(@NonNull UUID uuid, @NonNull String action) {
        Map<String, Long> playerCooldownMap = playerCooldowns.get(uuid);
        if (playerCooldownMap != null) {
            playerCooldownMap.remove(action);
            if (playerCooldownMap.isEmpty()) {
                playerCooldowns.remove(uuid);
            }
        }
    }
    
    /**
     * Remove a global cooldown
     * 
     * @param action The action identifier
     */
    public void removeGlobalCooldown(@NonNull String action) {
        globalCooldowns.remove(action);
    }
    
    /**
     * Remove a persistent cooldown
     * 
     * @param key The cooldown key
     */
    public void removePersistentCooldown(@NonNull String key) {
        persistentCooldowns.remove(key);
    }
    
    /**
     * Clear all cooldowns for a player
     * 
     * @param player The player
     */
    public void clearPlayerCooldowns(@NonNull Player player) {
        clearPlayerCooldowns(player.getUniqueId());
    }
    
    /**
     * Clear all cooldowns for a player UUID
     * 
     * @param uuid The player UUID
     */
    public void clearPlayerCooldowns(@NonNull UUID uuid) {
        playerCooldowns.remove(uuid);
    }
    
    /**
     * Clear all global cooldowns
     */
    public void clearGlobalCooldowns() {
        globalCooldowns.clear();
    }
    
    /**
     * Clear all persistent cooldowns
     */
    public void clearPersistentCooldowns() {
        persistentCooldowns.clear();
    }
    
    /**
     * Clear all cooldowns
     */
    public void clearAll() {
        playerCooldowns.clear();
        globalCooldowns.clear();
        persistentCooldowns.clear();
    }
    
    /**
     * Set a permission that bypasses all cooldowns
     * 
     * @param permission The permission node
     */
    public void setBypassPermission(@NonNull String permission) {
        this.bypassPermission = permission;
    }
    
    /**
     * Get the bypass permission
     * 
     * @return The permission node, or null if not set
     */
    public String getBypassPermission() {
        return bypassPermission;
    }
    
    /**
     * Cleanup expired cooldowns
     */
    private void cleanupExpiredCooldowns() {
        long currentTime = System.currentTimeMillis();
        
        // Cleanup player cooldowns
        playerCooldowns.entrySet().removeIf(entry -> {
            entry.getValue().entrySet().removeIf(cooldown -> currentTime >= cooldown.getValue());
            return entry.getValue().isEmpty();
        });
        
        // Cleanup global cooldowns
        globalCooldowns.entrySet().removeIf(entry -> currentTime >= entry.getValue());
        
        // Persistent cooldowns are automatically cleaned by ExpiringMap
    }
}

