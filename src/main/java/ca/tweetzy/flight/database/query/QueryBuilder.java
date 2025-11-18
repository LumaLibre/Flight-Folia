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

package ca.tweetzy.flight.database.query;

import ca.tweetzy.flight.database.DatabaseConnector;
import org.jetbrains.annotations.NotNull;

/**
 * Fluent query builder for database operations
 */
public class QueryBuilder {
    
    private final DatabaseConnector connector;
    private final String tablePrefix;
    
    public QueryBuilder(@NotNull DatabaseConnector connector, @NotNull String tablePrefix) {
        this.connector = connector;
        this.tablePrefix = tablePrefix;
    }
    
    /**
     * Create a SELECT query
     * 
     * @param table The table name (without prefix)
     * @return A SelectQuery instance
     */
    public SelectQuery select(@NotNull String table) {
        return new SelectQuery(connector, tablePrefix, table);
    }
    
    /**
     * Create an INSERT query
     * 
     * @param table The table name (without prefix)
     * @return An InsertQuery instance
     */
    public InsertQuery insert(@NotNull String table) {
        return new InsertQuery(connector, tablePrefix, table);
    }
    
    /**
     * Create an UPDATE query
     * 
     * @param table The table name (without prefix)
     * @return An UpdateQuery instance
     */
    public UpdateQuery update(@NotNull String table) {
        return new UpdateQuery(connector, tablePrefix, table);
    }
    
    /**
     * Create a DELETE query
     * 
     * @param table The table name (without prefix)
     * @return A DeleteQuery instance
     */
    public DeleteQuery delete(@NotNull String table) {
        return new DeleteQuery(connector, tablePrefix, table);
    }
}

