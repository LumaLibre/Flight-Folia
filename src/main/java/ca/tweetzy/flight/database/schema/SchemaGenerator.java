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
import ca.tweetzy.flight.database.annotations.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates table definitions from annotated entity classes
 */
public class SchemaGenerator {
    
    private final TypeMapper typeMapper;
    private final NestedObjectHandler nestedHandler;
    
    public SchemaGenerator(@NotNull DatabaseConnector connector) {
        this.typeMapper = new TypeMapper(connector);
        this.nestedHandler = new NestedObjectHandler();
    }
    
    /**
     * Generate a table definition from an entity class
     */
    @NotNull
    public TableDefinition generateTableDefinition(@NotNull Class<?> entityClass, @NotNull String pluginVersion) {
        Table tableAnnotation = entityClass.getAnnotation(Table.class);
        if (tableAnnotation == null) {
            throw new IllegalArgumentException("Entity class " + entityClass.getName() + " must be annotated with @Table");
        }
        
        String tableName = tableAnnotation.value();
        TableDefinition tableDef = new TableDefinition(tableName);
        
        // Process all fields
        for (Field field : entityClass.getDeclaredFields()) {
            // Skip ignored fields
            if (field.isAnnotationPresent(Ignore.class)) {
                continue;
            }
            
            // Check if field should be removed
            RemovalVersion removalVersion = field.getAnnotation(RemovalVersion.class);
            if (removalVersion != null && shouldRemove(pluginVersion, removalVersion.value())) {
                continue; // Skip this field, it's scheduled for removal
            }
            
            ColumnDefinition columnDef = createColumnDefinition(field, pluginVersion);
            if (columnDef != null) {
                tableDef.addColumn(columnDef);
            }
        }
        
        return tableDef;
    }
    
    /**
     * Create a column definition from a field
     */
    @Nullable
    private ColumnDefinition createColumnDefinition(@NotNull Field field, @NotNull String pluginVersion) {
        Column columnAnnotation = field.getAnnotation(Column.class);
        
        // Get column name
        String columnName;
        if (columnAnnotation != null && !columnAnnotation.value().isEmpty()) {
            columnName = columnAnnotation.value();
        } else {
            columnName = toSnakeCase(field.getName());
        }
        
        // Check if it's a primary key
        boolean isPrimaryKey = field.isAnnotationPresent(Id.class);
        
        // Get column properties
        boolean nullable = columnAnnotation == null || columnAnnotation.nullable();
        boolean unique = columnAnnotation != null && columnAnnotation.unique();
        int length = columnAnnotation != null ? columnAnnotation.length() : 0;
        String explicitType = columnAnnotation != null && !columnAnnotation.sqlType().isEmpty() 
            ? columnAnnotation.sqlType() 
            : null;
        
        // Handle nested objects
        Class<?> fieldType = field.getType();
        if (nestedHandler.isNested(field) || nestedHandler.shouldBeNested(fieldType)) {
            // Nested objects are stored as JSON (TEXT)
            explicitType = "TEXT";
        }
        
        // Get SQL type
        String sqlType = typeMapper.getSqlType(fieldType, explicitType, length);
        
        // Get deprecation info
        DeprecatedSince deprecatedSince = field.getAnnotation(DeprecatedSince.class);
        RemovalVersion removalVersion = field.getAnnotation(RemovalVersion.class);
        
        String deprecatedSinceVersion = deprecatedSince != null ? deprecatedSince.value() : null;
        String removalVersionStr = removalVersion != null ? removalVersion.value() : null;
        
        // Get default value
        String defaultValue = typeMapper.getDefaultValue(fieldType, nullable);
        
        return new ColumnDefinition(
            columnName,
            sqlType,
            nullable,
            unique,
            isPrimaryKey,
            defaultValue,
            deprecatedSinceVersion,
            removalVersionStr
        );
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
    
    /**
     * Check if a field should be removed based on version
     */
    private boolean shouldRemove(@NotNull String currentVersion, @NotNull String removalVersion) {
        return compareVersions(currentVersion, removalVersion) >= 0;
    }
    
    /**
     * Compare two version strings (X.Y.Z format)
     */
    private int compareVersions(@NotNull String v1, @NotNull String v2) {
        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");
        
        int maxLength = Math.max(parts1.length, parts2.length);
        
        for (int i = 0; i < maxLength; i++) {
            int num1 = i < parts1.length ? parseVersionPart(parts1[i]) : 0;
            int num2 = i < parts2.length ? parseVersionPart(parts2[i]) : 0;
            
            if (num1 != num2) {
                return Integer.compare(num1, num2);
            }
        }
        
        return 0;
    }
    
    /**
     * Parse a version part (handles numbers and removes non-numeric suffixes)
     */
    private int parseVersionPart(@NotNull String part) {
        // Remove any non-numeric suffix (e.g., "1-SNAPSHOT" -> "1")
        StringBuilder numStr = new StringBuilder();
        for (char c : part.toCharArray()) {
            if (Character.isDigit(c)) {
                numStr.append(c);
            } else {
                break;
            }
        }
        return numStr.length() > 0 ? Integer.parseInt(numStr.toString()) : 0;
    }
    
    /**
     * Generate CREATE TABLE SQL statement
     */
    @NotNull
    public String generateCreateTableSQL(@NotNull TableDefinition tableDef, @NotNull String tablePrefix) {
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        sql.append(tablePrefix).append(tableDef.getTableName()).append(" (");
        
        List<String> columnDefs = new ArrayList<>();
        List<String> primaryKeys = new ArrayList<>();
        
        for (ColumnDefinition column : tableDef.getColumns().values()) {
            StringBuilder colDef = new StringBuilder();
            colDef.append(column.getName()).append(" ").append(column.getSqlType());
            
            if (!column.isNullable()) {
                colDef.append(" NOT NULL");
            }
            
            if (column.isUnique() && !column.isPrimaryKey()) {
                colDef.append(" UNIQUE");
            }
            
            if (column.getDefaultValue() != null) {
                colDef.append(" DEFAULT ").append(column.getDefaultValue());
            }
            
            columnDefs.add(colDef.toString());
            
            if (column.isPrimaryKey()) {
                primaryKeys.add(column.getName());
            }
        }
        
        sql.append(String.join(", ", columnDefs));
        
        if (!primaryKeys.isEmpty()) {
            sql.append(", PRIMARY KEY (").append(String.join(", ", primaryKeys)).append(")");
        }
        
        sql.append(")");
        
        return sql.toString();
    }
    
    /**
     * Generate ALTER TABLE ADD COLUMN SQL statement
     */
    @NotNull
    public String generateAddColumnSQL(@NotNull ColumnDefinition column, @NotNull String tableName, @NotNull String tablePrefix) {
        StringBuilder sql = new StringBuilder("ALTER TABLE ");
        sql.append(tablePrefix).append(tableName);
        sql.append(" ADD COLUMN ");
        sql.append(column.getName()).append(" ").append(column.getSqlType());
        
        if (!column.isNullable()) {
            sql.append(" NOT NULL");
        }
        
        if (column.isUnique()) {
            sql.append(" UNIQUE");
        }
        
        if (column.getDefaultValue() != null) {
            sql.append(" DEFAULT ").append(column.getDefaultValue());
        }
        
        return sql.toString();
    }
    
    /**
     * Generate ALTER TABLE DROP COLUMN SQL statement
     */
    @NotNull
    public String generateDropColumnSQL(@NotNull String columnName, @NotNull String tableName, @NotNull String tablePrefix) {
        return "ALTER TABLE " + tablePrefix + tableName + " DROP COLUMN " + columnName;
    }
}

