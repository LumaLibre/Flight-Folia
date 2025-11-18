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

package ca.tweetzy.flight.database.schema;

import ca.tweetzy.flight.database.annotations.Nested;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

/**
 * Handles serialization/deserialization of nested objects
 */
public class NestedObjectHandler {
    
    private static final Gson GSON = new GsonBuilder().create();
    
    /**
     * Serialize a nested object to JSON
     */
    @NotNull
    public String serializeToJson(@NotNull Object object) {
        return GSON.toJson(object);
    }
    
    /**
     * Deserialize a nested object from JSON
     */
    @Nullable
    public <T> T deserializeFromJson(@NotNull String json, @NotNull Class<T> clazz) {
        try {
            return GSON.fromJson(json, clazz);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Check if a field is marked as nested
     */
    public boolean isNested(@NotNull Field field) {
        return field.isAnnotationPresent(Nested.class);
    }
    
    /**
     * Get the nested type for a field
     */
    @NotNull
    public Nested.NestedType getNestedType(@NotNull Field field) {
        Nested annotation = field.getAnnotation(Nested.class);
        if (annotation != null) {
            return annotation.serializeAs();
        }
        return Nested.NestedType.JSON; // Default
    }
    
    /**
     * Check if a type should be treated as nested (complex object, not primitive/wrapper)
     */
    public boolean shouldBeNested(@NotNull Class<?> type) {
        // Primitives and wrappers
        if (type.isPrimitive() || 
            type == String.class ||
            type == Integer.class ||
            type == Long.class ||
            type == Double.class ||
            type == Float.class ||
            type == Boolean.class ||
            type == Byte.class ||
            type == Short.class ||
            type == Character.class) {
            return false;
        }
        
        // UUID
        if (type == java.util.UUID.class) {
            return false;
        }
        
        // Enums
        if (type.isEnum()) {
            return false;
        }
        
        // Dates
        if (java.util.Date.class.isAssignableFrom(type)) {
            return false;
        }
        
        // Everything else is considered nested
        return true;
    }
}

