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

import ca.tweetzy.flight.database.Callback;
import ca.tweetzy.flight.database.DatabaseConnector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * DELETE query builder
 */
public class DeleteQuery extends Query {
    
    public DeleteQuery(@NotNull DatabaseConnector connector, @NotNull String tablePrefix, @NotNull String table) {
        super(connector, tablePrefix, table);
    }
    
    /**
     * Add a WHERE clause to the query
     */
    public DeleteQuery where(@NotNull String column, @NotNull String operator, @Nullable Object value) {
        super.where(column, operator, value);
        return this;
    }
    
    /**
     * Add a WHERE clause with = operator
     */
    public DeleteQuery where(@NotNull String column, @Nullable Object value) {
        super.where(column, value);
        return this;
    }
    
    @Override
    protected String buildSQL() {
        StringBuilder sql = new StringBuilder("DELETE FROM ");
        sql.append(getFullTableName());
        sql.append(buildWhereClause());
        
        return sql.toString();
    }
    
    @Override
    protected void setParameters(PreparedStatement statement) throws SQLException {
        setWhereParameters(statement, 1);
    }
    
    @Override
    protected void executeStatement(PreparedStatement statement) throws SQLException {
        statement.executeUpdate();
    }
    
    /**
     * Execute the DELETE and return the number of affected rows
     */
    public void execute(@Nullable Callback<Integer> callback) {
        connector.connect(connection -> {
            String sql = buildSQL();
            
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                setParameters(statement);
                int affectedRows = statement.executeUpdate();
                
                if (callback != null) {
                    callback.accept(null, affectedRows);
                }
            } catch (SQLException ex) {
                if (callback != null) {
                    callback.accept(ex, null);
                } else {
                    throw new RuntimeException("Failed to execute DELETE query: " + sql, ex);
                }
            }
        });
    }
}

