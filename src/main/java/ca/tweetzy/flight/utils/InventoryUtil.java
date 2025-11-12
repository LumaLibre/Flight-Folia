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
import lombok.experimental.UtilityClass;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Inventory serialization utilities.
 * Supports full inventory, armor, and ender chest serialization.
 * 
 * @author Kiran Hart
 */
@UtilityClass
public class InventoryUtil {
    
    /**
     * Serialize a player's full inventory (including armor)
     * 
     * @param player The player
     * @return Serialized string (Base64)
     */
    @NonNull
    public String serializeFullInventory(@NonNull Player player) {
        return serializeFullInventory(player.getInventory());
    }
    
    /**
     * Serialize a player inventory (including armor)
     * 
     * @param inventory The player inventory
     * @return Serialized string (Base64)
     */
    @NonNull
    public String serializeFullInventory(@NonNull PlayerInventory inventory) {
        Map<String, Object> data = new HashMap<>();
        
        // Serialize main inventory
        ItemStack[] contents = inventory.getContents();
        data.put("contents", serializeItemArray(contents));
        
        // Serialize armor
        ItemStack[] armor = inventory.getArmorContents();
        data.put("armor", serializeItemArray(armor));
        
        // Serialize off hand (1.9+)
        ItemStack offHand = inventory.getItemInOffHand();
        if (offHand != null) {
            data.put("offhand", SerializeUtil.encodeItem(offHand));
        }
        
        // Convert to YAML string then Base64
        FileConfiguration config = new YamlConfiguration();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            config.set(entry.getKey(), entry.getValue());
        }
        
        try {
            String yaml = config.saveToString();
            return Base64.getEncoder().encodeToString(yaml.getBytes());
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Deserialize a full inventory (including armor)
     * 
     * @param player The player
     * @param serialized The serialized string (Base64)
     */
    public void deserializeFullInventory(@NonNull Player player, @NonNull String serialized) {
        deserializeFullInventory(player.getInventory(), serialized);
    }
    
    /**
     * Deserialize a full inventory (including armor)
     * 
     * @param inventory The player inventory
     * @param serialized The serialized string (Base64)
     */
    public void deserializeFullInventory(@NonNull PlayerInventory inventory, @NonNull String serialized) {
        try {
            String yaml = new String(Base64.getDecoder().decode(serialized));
            FileConfiguration config = new YamlConfiguration();
            config.loadFromString(yaml);
            
            // Deserialize main inventory
            if (config.contains("contents")) {
                ItemStack[] contents = deserializeItemArray(config.getList("contents"));
                inventory.setContents(contents);
            }
            
            // Deserialize armor
            if (config.contains("armor")) {
                ItemStack[] armor = deserializeItemArray(config.getList("armor"));
                inventory.setArmorContents(armor);
            }
            
            // Deserialize off hand (1.9+)
            if (config.contains("offhand")) {
                Object offHandData = config.get("offhand");
                ItemStack offHand = offHandData instanceof String 
                    ? SerializeUtil.decodeItem((String) offHandData)
                    : null;
                if (offHand != null) {
                    inventory.setItemInOffHand(offHand);
                }
            }
        } catch (Exception e) {
            // Silently fail
        }
    }
    
    /**
     * Serialize a regular inventory
     * 
     * @param inventory The inventory
     * @return Serialized string (Base64)
     */
    @NonNull
    public String serialize(@NonNull Inventory inventory) {
        Map<String, Object> data = new HashMap<>();
        data.put("contents", serializeItemArray(inventory.getContents()));
        
        FileConfiguration config = new YamlConfiguration();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            config.set(entry.getKey(), entry.getValue());
        }
        
        try {
            String yaml = config.saveToString();
            return Base64.getEncoder().encodeToString(yaml.getBytes());
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Deserialize a regular inventory
     * 
     * @param inventory The inventory
     * @param serialized The serialized string (Base64)
     */
    public void deserialize(@NonNull Inventory inventory, @NonNull String serialized) {
        try {
            String yaml = new String(Base64.getDecoder().decode(serialized));
            FileConfiguration config = new YamlConfiguration();
            config.loadFromString(yaml);
            
            if (config.contains("contents")) {
                ItemStack[] contents = deserializeItemArray(config.getList("contents"));
                inventory.setContents(contents);
            }
        } catch (Exception e) {
            // Silently fail
        }
    }
    
    /**
     * Serialize ender chest
     * 
     * @param player The player
     * @return Serialized string (Base64)
     */
    @NonNull
    public String serializeEnderChest(@NonNull Player player) {
        return serialize(player.getEnderChest());
    }
    
    /**
     * Deserialize ender chest
     * 
     * @param player The player
     * @param serialized The serialized string (Base64)
     */
    public void deserializeEnderChest(@NonNull Player player, @NonNull String serialized) {
        deserialize(player.getEnderChest(), serialized);
    }
    
    /**
     * Save inventory to file
     * 
     * @param player The player
     * @param file The file to save to
     */
    public void saveToFile(@NonNull Player player, @NonNull File file) {
        try {
            FileConfiguration config = new YamlConfiguration();
            config.set("inventory", serializeFullInventory(player));
            config.set("enderchest", serializeEnderChest(player));
            config.save(file);
        } catch (IOException e) {
            // Silently fail
        }
    }
    
    /**
     * Load inventory from file
     * 
     * @param player The player
     * @param file The file to load from
     */
    public void loadFromFile(@NonNull Player player, @NonNull File file) {
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            if (config.contains("inventory")) {
                deserializeFullInventory(player, config.getString("inventory"));
            }
            if (config.contains("enderchest")) {
                deserializeEnderChest(player, config.getString("enderchest"));
            }
        } catch (Exception e) {
            // Silently fail
        }
    }
    
    /**
     * Serialize an array of items
     */
    @NonNull
    private Object serializeItemArray(@Nullable ItemStack[] items) {
        if (items == null) {
            return new Object[0];
        }
        
        Object[] serialized = new Object[items.length];
        for (int i = 0; i < items.length; i++) {
            serialized[i] = items[i] != null ? SerializeUtil.encodeItem(items[i]) : null;
        }
        return serialized;
    }
    
    /**
     * Deserialize an array of items
     */
    @NonNull
    private ItemStack[] deserializeItemArray(@Nullable Object data) {
        if (data == null) {
            return new ItemStack[0];
        }
        
        if (!(data instanceof java.util.List)) {
            return new ItemStack[0];
        }
        
        java.util.List<?> list = (java.util.List<?>) data;
        ItemStack[] items = new ItemStack[list.size()];
        
        for (int i = 0; i < list.size(); i++) {
            Object itemData = list.get(i);
            items[i] = itemData != null && itemData instanceof String 
                ? SerializeUtil.decodeItem((String) itemData) 
                : null;
        }
        
        return items;
    }
}

