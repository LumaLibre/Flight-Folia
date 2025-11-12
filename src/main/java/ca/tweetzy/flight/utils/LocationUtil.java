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
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Location utilities for serialization, region checking, and distance calculations.
 * 
 * @author Kiran Hart
 */
@UtilityClass
public class LocationUtil {
    
    /**
     * Serialize a location to a string
     * Format: world:x:y:z:yaw:pitch
     * 
     * @param location The location to serialize
     * @return Serialized string
     */
    @NonNull
    public String serialize(@NonNull Location location) {
        return String.format("%s:%f:%f:%f:%f:%f",
            location.getWorld() != null ? location.getWorld().getName() : "world",
            location.getX(),
            location.getY(),
            location.getZ(),
            location.getYaw(),
            location.getPitch()
        );
    }
    
    /**
     * Deserialize a location from a string
     * 
     * @param serialized The serialized string
     * @return Location or null if invalid
     */
    @Nullable
    public Location deserialize(@NonNull String serialized) {
        try {
            String[] parts = serialized.split(":");
            if (parts.length < 4) {
                return null;
            }
            
            String worldName = parts[0];
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                return null;
            }
            
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = parts.length > 4 ? Float.parseFloat(parts[4]) : 0.0f;
            float pitch = parts.length > 5 ? Float.parseFloat(parts[5]) : 0.0f;
            
            return new Location(world, x, y, z, yaw, pitch);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Serialize a location to a map
     * 
     * @param location The location to serialize
     * @return Map containing location data
     */
    @NonNull
    public Map<String, Object> serializeToMap(@NonNull Location location) {
        Map<String, Object> map = new HashMap<>();
        map.put("world", location.getWorld() != null ? location.getWorld().getName() : "world");
        map.put("x", location.getX());
        map.put("y", location.getY());
        map.put("z", location.getZ());
        map.put("yaw", location.getYaw());
        map.put("pitch", location.getPitch());
        return map;
    }
    
    /**
     * Deserialize a location from a map
     * 
     * @param map The map containing location data
     * @return Location or null if invalid
     */
    @Nullable
    public Location deserializeFromMap(@NonNull Map<String, Object> map) {
        try {
            String worldName = (String) map.get("world");
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                return null;
            }
            
            double x = ((Number) map.get("x")).doubleValue();
            double y = ((Number) map.get("y")).doubleValue();
            double z = ((Number) map.get("z")).doubleValue();
            float yaw = map.containsKey("yaw") ? ((Number) map.get("yaw")).floatValue() : 0.0f;
            float pitch = map.containsKey("pitch") ? ((Number) map.get("pitch")).floatValue() : 0.0f;
            
            return new Location(world, x, y, z, yaw, pitch);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Check if a location is within a cuboid region
     * 
     * @param location The location to check
     * @param min The minimum corner of the region
     * @param max The maximum corner of the region
     * @return true if location is in region
     */
    public boolean isInRegion(@NonNull Location location, @NonNull Location min, @NonNull Location max) {
        if (location.getWorld() == null || min.getWorld() == null || max.getWorld() == null) {
            return false;
        }
        
        if (!location.getWorld().equals(min.getWorld()) || !location.getWorld().equals(max.getWorld())) {
            return false;
        }
        
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        
        double minX = Math.min(min.getX(), max.getX());
        double maxX = Math.max(min.getX(), max.getX());
        double minY = Math.min(min.getY(), max.getY());
        double maxY = Math.max(min.getY(), max.getY());
        double minZ = Math.min(min.getZ(), max.getZ());
        double maxZ = Math.max(min.getZ(), max.getZ());
        
        return x >= minX && x <= maxX &&
               y >= minY && y <= maxY &&
               z >= minZ && z <= maxZ;
    }
    
    /**
     * Check if a location is within a sphere region
     * 
     * @param location The location to check
     * @param center The center of the sphere
     * @param radius The radius of the sphere
     * @return true if location is in sphere
     */
    public boolean isInSphere(@NonNull Location location, @NonNull Location center, double radius) {
        if (location.getWorld() == null || center.getWorld() == null) {
            return false;
        }
        
        if (!location.getWorld().equals(center.getWorld())) {
            return false;
        }
        
        return distance(location, center) <= radius;
    }
    
    /**
     * Calculate 3D distance between two locations
     * 
     * @param loc1 First location
     * @param loc2 Second location
     * @return Distance in blocks
     */
    public double distance(@NonNull Location loc1, @NonNull Location loc2) {
        if (loc1.getWorld() == null || loc2.getWorld() == null) {
            return Double.MAX_VALUE;
        }
        
        if (!loc1.getWorld().equals(loc2.getWorld())) {
            return Double.MAX_VALUE;
        }
        
        double dx = loc1.getX() - loc2.getX();
        double dy = loc1.getY() - loc2.getY();
        double dz = loc1.getZ() - loc2.getZ();
        
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
    
    /**
     * Calculate 2D distance between two locations (ignores Y)
     * 
     * @param loc1 First location
     * @param loc2 Second location
     * @return Distance in blocks
     */
    public double distance2D(@NonNull Location loc1, @NonNull Location loc2) {
        if (loc1.getWorld() == null || loc2.getWorld() == null) {
            return Double.MAX_VALUE;
        }
        
        if (!loc1.getWorld().equals(loc2.getWorld())) {
            return Double.MAX_VALUE;
        }
        
        double dx = loc1.getX() - loc2.getX();
        double dz = loc1.getZ() - loc2.getZ();
        
        return Math.sqrt(dx * dx + dz * dz);
    }
    
    /**
     * Get the center point between two locations
     * 
     * @param loc1 First location
     * @param loc2 Second location
     * @return Center location
     */
    @Nullable
    public Location getCenter(@NonNull Location loc1, @NonNull Location loc2) {
        if (loc1.getWorld() == null || loc2.getWorld() == null) {
            return null;
        }
        
        if (!loc1.getWorld().equals(loc2.getWorld())) {
            return null;
        }
        
        return new Location(
            loc1.getWorld(),
            (loc1.getX() + loc2.getX()) / 2.0,
            (loc1.getY() + loc2.getY()) / 2.0,
            (loc1.getZ() + loc2.getZ()) / 2.0,
            (loc1.getYaw() + loc2.getYaw()) / 2.0f,
            (loc1.getPitch() + loc2.getPitch()) / 2.0f
        );
    }
    
    /**
     * Check if a chunk is loaded
     * 
     * @param location The location in the chunk
     * @return true if chunk is loaded
     */
    public boolean isChunkLoaded(@NonNull Location location) {
        if (location.getWorld() == null) {
            return false;
        }
        return location.getWorld().isChunkLoaded(
            location.getBlockX() >> 4,
            location.getBlockZ() >> 4
        );
    }
    
    /**
     * Load a chunk
     * 
     * @param location The location in the chunk
     * @return true if chunk was loaded
     */
    public boolean loadChunk(@NonNull Location location) {
        if (location.getWorld() == null) {
            return false;
        }
        location.getWorld().loadChunk(
            location.getBlockX() >> 4,
            location.getBlockZ() >> 4
        );
        return true;
    }
    
    /**
     * Unload a chunk
     * 
     * @param location The location in the chunk
     * @return true if chunk was unloaded
     */
    public boolean unloadChunk(@NonNull Location location) {
        if (location.getWorld() == null) {
            return false;
        }
        location.getWorld().unloadChunk(
            location.getBlockX() >> 4,
            location.getBlockZ() >> 4
        );
        return true;
    }
}

