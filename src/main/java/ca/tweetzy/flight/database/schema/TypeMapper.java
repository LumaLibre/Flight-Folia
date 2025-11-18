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

import ca.tweetzy.flight.database.DatabaseConnector;
import ca.tweetzy.flight.database.MySQLConnector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Maps Java types to SQL types
 */
public class TypeMapper {
    
    private final DatabaseConnector connector;
    
    public TypeMapper(@NotNull DatabaseConnector connector) {
        this.connector = connector;
    }
    
    /**
     * Get SQL type for a Java type
     */
    @NotNull
    public String getSqlType(@NotNull Class<?> javaType, @Nullable String explicitType, int length) {
        // Use explicit type if provided
        if (explicitType != null && !explicitType.isEmpty()) {
            return explicitType;
        }
        
        // Handle primitives
        if (javaType == boolean.class || javaType == Boolean.class) {
            return isMySQL() ? "TINYINT(1)" : "INTEGER";
        }
        
        if (javaType == byte.class || javaType == Byte.class) {
            return "TINYINT";
        }
        
        if (javaType == short.class || javaType == Short.class) {
            return "SMALLINT";
        }
        
        if (javaType == int.class || javaType == Integer.class) {
            return "INT";
        }
        
        if (javaType == long.class || javaType == Long.class) {
            return "BIGINT";
        }
        
        if (javaType == float.class || javaType == Float.class) {
            return "FLOAT";
        }
        
        if (javaType == double.class || javaType == Double.class) {
            return "DOUBLE";
        }
        
        // Handle common object types
        if (javaType == String.class) {
            if (length > 0) {
                return "VARCHAR(" + length + ")";
            }
            return isMySQL() ? "TEXT" : "TEXT";
        }
        
        if (javaType == UUID.class) {
            return "VARCHAR(36)"; // UUID string representation
        }
        
        if (javaType.isEnum()) {
            return "VARCHAR(255)"; // Enum stored as VARCHAR for portability
        }
        
        // Handle dates
        if (java.util.Date.class.isAssignableFrom(javaType)) {
            return "BIGINT"; // Store as timestamp
        }
        
        // Default to TEXT for unknown types (will be serialized as JSON)
        return "TEXT";
    }
    
    /**
     * Check if a type conversion is safe (expanding, not losing data)
     */
    public boolean isSafeConversion(@NotNull String fromType, @NotNull String toType) {
        // Normalize types for comparison
        String from = normalizeType(fromType);
        String to = normalizeType(toType);
        
        // Same type
        if (from.equals(to)) {
            return true;
        }
        
        // Expanding conversions (safe)
        if (from.equals("TINYINT") && (to.equals("INT") || to.equals("BIGINT"))) {
            return true;
        }
        
        if (from.equals("INT") && to.equals("BIGINT")) {
            return true;
        }
        
        if (from.equals("FLOAT") && to.equals("DOUBLE")) {
            return true;
        }
        
        // VARCHAR expansions
        if (from.startsWith("VARCHAR") && to.startsWith("VARCHAR")) {
            int fromLen = extractLength(from);
            int toLen = extractLength(to);
            if (toLen > fromLen) {
                return true;
            }
        }
        
        if (from.startsWith("VARCHAR") && to.equals("TEXT")) {
            return true;
        }
        
        // VARCHAR to TEXT is safe
        if (from.startsWith("VARCHAR") && to.equals("TEXT")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Normalize SQL type for comparison (remove length specifications)
     */
    @NotNull
    private String normalizeType(@NotNull String type) {
        if (type.contains("(")) {
            return type.substring(0, type.indexOf("("));
        }
        return type.toUpperCase();
    }
    
    /**
     * Extract length from VARCHAR(n) or similar
     */
    private int extractLength(@NotNull String type) {
        if (type.contains("(") && type.contains(")")) {
            try {
                String lengthStr = type.substring(type.indexOf("(") + 1, type.indexOf(")"));
                return Integer.parseInt(lengthStr);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
    
    /**
     * Check if using MySQL
     */
    private boolean isMySQL() {
        return connector instanceof MySQLConnector;
    }
    
    
    /**
     * Get default value for a type
     */
    @Nullable
    public String getDefaultValue(@NotNull Class<?> javaType, boolean nullable) {
        if (nullable) {
            return null;
        }
        
        if (javaType == boolean.class || javaType == Boolean.class) {
            return "0";
        }
        
        if (javaType.isPrimitive()) {
            return "0";
        }
        
        if (javaType == String.class) {
            return "''";
        }
        
        return null;
    }
}

