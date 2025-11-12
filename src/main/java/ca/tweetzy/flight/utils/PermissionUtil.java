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

import lombok.NonNull;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Permission utilities for managing player permissions.
 * Requires a permission plugin (like LuckPerms, PermissionsEx, etc.)
 * 
 * @author Kiran Hart
 */
public class PermissionUtil {
    
    private final Plugin plugin;
    
    public PermissionUtil(@NonNull Plugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Check if a player has a permission
     * 
     * @param player The player
     * @param permission The permission node
     * @return true if player has permission
     */
    public boolean has(@NonNull Player player, @NonNull String permission) {
        return player.hasPermission(permission);
    }
    
    /**
     * Check if an offline player has a permission
     * 
     * @param player The offline player
     * @param permission The permission node
     * @return true if player has permission
     */
    public boolean has(@NonNull OfflinePlayer player, @NonNull String permission) {
        if (player.isOnline()) {
            return has(player.getPlayer(), permission);
        }
        // For offline players, we'd need a permission plugin API
        // This is a basic implementation
        return false;
    }
    
    /**
     * Add a permission to a player (requires permission plugin)
     * Note: This is a placeholder. Actual implementation depends on permission plugin.
     * 
     * @param player The player
     * @param permission The permission node
     */
    public void add(@NonNull Player player, @NonNull String permission) {
        // This would require integration with a permission plugin API
        // For now, this is a placeholder
        plugin.getLogger().warning("PermissionUtil.add() requires a permission plugin integration");
    }
    
    /**
     * Remove a permission from a player (requires permission plugin)
     * Note: This is a placeholder. Actual implementation depends on permission plugin.
     * 
     * @param player The player
     * @param permission The permission node
     */
    public void remove(@NonNull Player player, @NonNull String permission) {
        // This would require integration with a permission plugin API
        // For now, this is a placeholder
        plugin.getLogger().warning("PermissionUtil.remove() requires a permission plugin integration");
    }
    
    /**
     * Check if a player has a permission with a default value
     * 
     * @param player The player
     * @param permission The permission node
     * @param defaultValue Default value if permission is not set
     * @return true if player has permission or default value
     */
    public boolean has(@NonNull Player player, @NonNull String permission, boolean defaultValue) {
        return player.hasPermission(permission) || (defaultValue && !player.isPermissionSet(permission));
    }
}

