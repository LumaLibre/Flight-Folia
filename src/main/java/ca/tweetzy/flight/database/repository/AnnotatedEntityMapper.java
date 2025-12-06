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

package ca.tweetzy.flight.database.repository;

import ca.tweetzy.flight.database.annotations.*;
import ca.tweetzy.flight.database.schema.NestedObjectHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Automatically generates EntityMapper from annotations
 */
public class AnnotatedEntityMapper<T> implements EntityMapper<T> {
    
    private final Class<T> entityClass;
    private final NestedObjectHandler nestedHandler;
    private final Map<String, Field> columnToField = new HashMap<>();
    private final Map<Field, String> fieldToColumn = new HashMap<>();
    private Field idField;
    private String idColumnName;
    
    public AnnotatedEntityMapper(@NotNull Class<T> entityClass) {
        this.entityClass = entityClass;
        this.nestedHandler = new NestedObjectHandler();
        analyzeEntity();
    }
    
    /**
     * Analyze the entity class and build field mappings
     */
    private void analyzeEntity() {
        for (Field field : entityClass.getDeclaredFields()) {
            // Skip ignored fields
            if (field.isAnnotationPresent(Ignore.class)) {
                continue;
            }
            
            // Skip static fields (constants, class-level fields)
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            
            field.setAccessible(true);
            
            // Get column name
            String columnName;
            Column columnAnnotation = field.getAnnotation(Column.class);
            if (columnAnnotation != null && !columnAnnotation.value().isEmpty()) {
                columnName = columnAnnotation.value();
            } else {
                columnName = toSnakeCase(field.getName());
            }
            
            columnToField.put(columnName, field);
            fieldToColumn.put(field, columnName);
            
            // Check if it's the ID field
            if (field.isAnnotationPresent(Id.class)) {
                idField = field;
                idColumnName = columnName;
            }
        }
        
        if (idField == null) {
            throw new IllegalArgumentException("Entity class " + entityClass.getName() + " must have a field annotated with @Id");
        }
    }
    
    @Override
    @Nullable
    public T map(@NotNull ResultSet resultSet) throws SQLException {
        try {
            T entity = entityClass.getDeclaredConstructor().newInstance();
            
            for (Map.Entry<String, Field> entry : columnToField.entrySet()) {
                String columnName = entry.getKey();
                Field field = entry.getValue();
                
                // Check if column exists in result set
                if (!hasColumn(resultSet, columnName)) {
                    continue;
                }
                
                Object value = getValueFromResultSet(resultSet, columnName, field);
                field.set(entity, value);
            }
            
            return entity;
        } catch (Exception e) {
            throw new RuntimeException("Failed to map entity from ResultSet", e);
        }
    }
    
    @Override
    @NotNull
    public Map<String, Object> toMap(@NotNull T entity) {
        Map<String, Object> map = new HashMap<>();
        
        for (Map.Entry<Field, String> entry : fieldToColumn.entrySet()) {
            Field field = entry.getKey();
            String columnName = entry.getValue();
            
            try {
                Object value = field.get(entity);
                
                // Handle nested objects
                if (nestedHandler.isNested(field) || nestedHandler.shouldBeNested(field.getType())) {
                    if (value != null) {
                        value = nestedHandler.serializeToJson(value);
                    }
                } else if (field.getType().isEnum()) {
                    if (value != null) {
                        value = ((Enum<?>) value).name();
                    }
                } else if (value instanceof UUID) {
                    value = value.toString();
                }
                
                map.put(columnName, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to access field: " + field.getName(), e);
            }
        }
        
        return map;
    }
    
    @Override
    @NotNull
    public Object getId(@NotNull T entity) {
        try {
            Object id = idField.get(entity);
            if (id instanceof UUID) {
                return id.toString();
            }
            return id;
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to get ID from entity", e);
        }
    }
    
    @Override
    @NotNull
    public String getIdColumn() {
        return idColumnName;
    }
    
    /**
     * Get value from ResultSet and convert to appropriate type
     */
    @Nullable
    private Object getValueFromResultSet(@NotNull ResultSet rs, @NotNull String columnName, @NotNull Field field) throws SQLException {
        Class<?> fieldType = field.getType();
        Object value = rs.getObject(columnName);
        
        if (value == null || rs.wasNull()) {
            return null;
        }
        
        // Handle nested objects (JSON)
        if (nestedHandler.isNested(field) || nestedHandler.shouldBeNested(fieldType)) {
            if (value instanceof String) {
                return nestedHandler.deserializeFromJson((String) value, fieldType);
            }
            return value;
        }
        
        // Handle enums
        if (fieldType.isEnum()) {
            if (value instanceof String) {
                try {
                    @SuppressWarnings({"unchecked", "rawtypes"})
                    Class<? extends Enum> enumClass = (Class<? extends Enum>) fieldType;
                    return Enum.valueOf(enumClass, (String) value);
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }
        }
        
        // Handle UUID
        if (fieldType == UUID.class) {
            if (value instanceof String) {
                try {
                    return UUID.fromString((String) value);
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }
        }
        
        // Handle primitives
        if (fieldType == boolean.class || fieldType == Boolean.class) {
            if (value instanceof Number) {
                return ((Number) value).intValue() != 0;
            }
            if (value instanceof Boolean) {
                return value;
            }
        }
        
        // Type conversion for numbers
        if (fieldType == int.class || fieldType == Integer.class) {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
        }
        
        if (fieldType == long.class || fieldType == Long.class) {
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
        }
        
        if (fieldType == double.class || fieldType == Double.class) {
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
        }
        
        if (fieldType == float.class || fieldType == Float.class) {
            if (value instanceof Number) {
                return ((Number) value).floatValue();
            }
        }
        
        return value;
    }
    
    /**
     * Check if ResultSet has a column
     */
    private boolean hasColumn(@NotNull ResultSet rs, @NotNull String columnName) {
        try {
            rs.findColumn(columnName);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * Convert camelCase to snake_case
     */
    @NotNull
    private String toSnakeCase(@NotNull String camelCase) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < camelCase.length(); i++) {
            char c = camelCase.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    result.append('_');
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}

