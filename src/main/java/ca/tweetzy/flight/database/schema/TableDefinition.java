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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a database table definition
 */
public class TableDefinition {
    
    private final String tableName;
    private final Map<String, ColumnDefinition> columns = new HashMap<>();
    private final List<String> primaryKeys = new ArrayList<>();
    
    public TableDefinition(@NotNull String tableName) {
        this.tableName = tableName;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public void addColumn(@NotNull ColumnDefinition column) {
        columns.put(column.getName(), column);
        if (column.isPrimaryKey()) {
            primaryKeys.add(column.getName());
        }
    }
    
    @Nullable
    public ColumnDefinition getColumn(@NotNull String name) {
        return columns.get(name);
    }
    
    @NotNull
    public Map<String, ColumnDefinition> getColumns() {
        return new HashMap<>(columns);
    }
    
    @NotNull
    public List<String> getPrimaryKeys() {
        return new ArrayList<>(primaryKeys);
    }
    
    public boolean hasColumn(@NotNull String name) {
        return columns.containsKey(name);
    }
    
    @Override
    public String toString() {
        return "TableDefinition{" +
                "tableName='" + tableName + '\'' +
                ", columns=" + columns.size() +
                ", primaryKeys=" + primaryKeys +
                '}';
    }
}

