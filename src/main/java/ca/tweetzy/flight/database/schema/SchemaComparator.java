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
import org.jetbrains.annotations.NotNull;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Compares entity schema with database schema to detect differences
 */
public class SchemaComparator {
    
    private final DatabaseConnector connector;
    private final TypeMapper typeMapper;
    
    public SchemaComparator(@NotNull DatabaseConnector connector) {
        this.connector = connector;
        this.typeMapper = new TypeMapper(connector);
    }
    
    /**
     * Get the current database schema for a table
     */
    @NotNull
    public TableDefinition getDatabaseSchema(@NotNull String tableName, @NotNull String tablePrefix) throws SQLException {
        TableDefinition[] tableDefRef = new TableDefinition[1];
        tableDefRef[0] = new TableDefinition(tableName);
        String fullTableName = tablePrefix + tableName;
        
        connector.connect(connection -> {
            DatabaseMetaData metaData = connection.getMetaData();
            TableDefinition tableDef = tableDefRef[0];
            
            // Get columns
            try (ResultSet columns = metaData.getColumns(null, null, fullTableName, null)) {
                while (columns.next()) {
                    String columnName = columns.getString("COLUMN_NAME");
                    String sqlType = columns.getString("TYPE_NAME");
                    int dataType = columns.getInt("DATA_TYPE");
                    int columnSize = columns.getInt("COLUMN_SIZE");
                    int nullable = columns.getInt("NULLABLE");
                    String defaultValue = columns.getString("COLUMN_DEF");
                    
                    // Build full SQL type
                    String fullSqlType = sqlType;
                    if (columnSize > 0 && (dataType == java.sql.Types.VARCHAR || dataType == java.sql.Types.CHAR)) {
                        fullSqlType = sqlType + "(" + columnSize + ")";
                    }
                    
                    boolean isNullable = nullable == DatabaseMetaData.columnNullable;
                    
                    ColumnDefinition columnDef = new ColumnDefinition(
                        columnName,
                        fullSqlType,
                        isNullable,
                        false, // unique - would need separate query
                        false, // primary key - handled separately
                        defaultValue,
                        null, // deprecatedSince - not stored in DB
                        null  // removalVersion - not stored in DB
                    );
                    
                    tableDef.addColumn(columnDef);
                }
            }
            
            // Get primary keys
            try (ResultSet primaryKeys = metaData.getPrimaryKeys(null, null, fullTableName)) {
                while (primaryKeys.next()) {
                    String columnName = primaryKeys.getString("COLUMN_NAME");
                    ColumnDefinition column = tableDef.getColumn(columnName);
                    if (column != null) {
                        // Create new column definition with primary key flag
                        ColumnDefinition pkColumn = new ColumnDefinition(
                            column.getName(),
                            column.getSqlType(),
                            column.isNullable(),
                            column.isUnique(),
                            true, // primary key
                            column.getDefaultValue(),
                            column.getDeprecatedSince(),
                            column.getRemovalVersion()
                        );
                        tableDef.addColumn(pkColumn);
                    }
                }
            }
        });
        
        return tableDefRef[0];
    }
    
    /**
     * Compare entity schema with database schema and return differences
     */
    @NotNull
    public SchemaDiff compare(@NotNull TableDefinition entitySchema, 
                             @NotNull TableDefinition databaseSchema,
                             @NotNull String pluginVersion) {
        SchemaDiff diff = new SchemaDiff();
        
        Map<String, ColumnDefinition> entityColumns = entitySchema.getColumns();
        Map<String, ColumnDefinition> dbColumns = databaseSchema.getColumns();
        
        // Find new columns (in entity but not in DB)
        for (ColumnDefinition entityCol : entityColumns.values()) {
            if (!dbColumns.containsKey(entityCol.getName())) {
                diff.addNewColumn(entityCol);
            }
        }
        
        // Find removed columns (in DB but not in entity)
        for (ColumnDefinition dbCol : dbColumns.values()) {
            if (!entityColumns.containsKey(dbCol.getName())) {
                // Check if it should be removed based on version
                if (dbCol.getRemovalVersion() != null && 
                    compareVersions(pluginVersion, dbCol.getRemovalVersion()) >= 0) {
                    diff.addRemovedColumn(dbCol.getName());
                } else {
                    diff.addOrphanedColumn(dbCol.getName());
                }
            }
        }
        
        // Find changed columns (same name, different type)
        for (ColumnDefinition entityCol : entityColumns.values()) {
            ColumnDefinition dbCol = dbColumns.get(entityCol.getName());
            if (dbCol != null) {
                if (!entityCol.getSqlType().equals(dbCol.getSqlType())) {
                    // Check if conversion is safe
                    boolean safe = typeMapper.isSafeConversion(dbCol.getSqlType(), entityCol.getSqlType());
                    diff.addChangedColumn(entityCol, dbCol, safe);
                }
            }
        }
        
        return diff;
    }
    
    /**
     * Compare two version strings
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
    
    private int parseVersionPart(@NotNull String part) {
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
     * Represents schema differences
     */
    public static class SchemaDiff {
        private final List<ColumnDefinition> newColumns = new ArrayList<>();
        private final List<String> removedColumns = new ArrayList<>();
        private final List<String> orphanedColumns = new ArrayList<>();
        private final List<ColumnChange> changedColumns = new ArrayList<>();
        
        public void addNewColumn(@NotNull ColumnDefinition column) {
            newColumns.add(column);
        }
        
        public void addRemovedColumn(@NotNull String columnName) {
            removedColumns.add(columnName);
        }
        
        public void addOrphanedColumn(@NotNull String columnName) {
            orphanedColumns.add(columnName);
        }
        
        public void addChangedColumn(@NotNull ColumnDefinition newColumn, 
                                     @NotNull ColumnDefinition oldColumn,
                                     boolean safeConversion) {
            changedColumns.add(new ColumnChange(newColumn, oldColumn, safeConversion));
        }
        
        @NotNull
        public List<ColumnDefinition> getNewColumns() {
            return newColumns;
        }
        
        @NotNull
        public List<String> getRemovedColumns() {
            return removedColumns;
        }
        
        @NotNull
        public List<String> getOrphanedColumns() {
            return orphanedColumns;
        }
        
        @NotNull
        public List<ColumnChange> getChangedColumns() {
            return changedColumns;
        }
        
        public boolean hasChanges() {
            return !newColumns.isEmpty() || 
                   !removedColumns.isEmpty() || 
                   !orphanedColumns.isEmpty() || 
                   !changedColumns.isEmpty();
        }
        
        public static class ColumnChange {
            private final ColumnDefinition newColumn;
            private final ColumnDefinition oldColumn;
            private final boolean safeConversion;
            
            public ColumnChange(@NotNull ColumnDefinition newColumn, 
                              @NotNull ColumnDefinition oldColumn,
                              boolean safeConversion) {
                this.newColumn = newColumn;
                this.oldColumn = oldColumn;
                this.safeConversion = safeConversion;
            }
            
            @NotNull
            public ColumnDefinition getNewColumn() {
                return newColumn;
            }
            
            @NotNull
            public ColumnDefinition getOldColumn() {
                return oldColumn;
            }
            
            public boolean isSafeConversion() {
                return safeConversion;
            }
        }
    }
}

