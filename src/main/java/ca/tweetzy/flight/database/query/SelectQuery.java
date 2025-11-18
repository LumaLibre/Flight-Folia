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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * SELECT query builder
 */
public class SelectQuery extends Query {
    
    private final List<String> columns = new ArrayList<>();
    private String orderBy;
    private boolean orderAscending = true;
    private Integer limit;
    private Integer offset;
    
    public SelectQuery(@NotNull DatabaseConnector connector, @NotNull String tablePrefix, @NotNull String table) {
        super(connector, tablePrefix, table);
    }
    
    /**
     * Add a WHERE clause to the query
     */
    public SelectQuery where(@NotNull String column, @NotNull String operator, @Nullable Object value) {
        super.where(column, operator, value);
        return this;
    }
    
    /**
     * Add a WHERE clause with = operator
     */
    public SelectQuery where(@NotNull String column, @Nullable Object value) {
        super.where(column, value);
        return this;
    }
    
    /**
     * Specify columns to select (defaults to * if not called)
     */
    public SelectQuery columns(@NotNull String... columns) {
        this.columns.clear();
        for (String column : columns) {
            this.columns.add(column);
        }
        return this;
    }
    
    /**
     * Add a column to select
     */
    public SelectQuery column(@NotNull String column) {
        this.columns.add(column);
        return this;
    }
    
    /**
     * Order by a column
     */
    public SelectQuery orderBy(@NotNull String column, boolean ascending) {
        this.orderBy = column;
        this.orderAscending = ascending;
        return this;
    }
    
    /**
     * Order by a column (ascending)
     */
    public SelectQuery orderBy(@NotNull String column) {
        return orderBy(column, true);
    }
    
    /**
     * Limit the number of results
     */
    public SelectQuery limit(int limit) {
        this.limit = limit;
        return this;
    }
    
    /**
     * Set offset for pagination
     */
    public SelectQuery offset(int offset) {
        this.offset = offset;
        return this;
    }
    
    @Override
    protected String buildSQL() {
        StringBuilder sql = new StringBuilder("SELECT ");
        
        if (columns.isEmpty()) {
            sql.append("*");
        } else {
            sql.append(String.join(", ", columns));
        }
        
        sql.append(" FROM ").append(getFullTableName());
        sql.append(buildWhereClause());
        
        if (orderBy != null) {
            sql.append(" ORDER BY ").append(orderBy).append(orderAscending ? " ASC" : " DESC");
        }
        
        if (limit != null) {
            sql.append(" LIMIT ").append(limit);
        }
        
        if (offset != null) {
            sql.append(" OFFSET ").append(offset);
        }
        
        return sql.toString();
    }
    
    @Override
    protected void setParameters(PreparedStatement statement) throws SQLException {
        setWhereParameters(statement, 1);
    }
    
    @Override
    protected void executeStatement(PreparedStatement statement) throws SQLException {
        // For SELECT, we don't execute here - use fetch methods instead
        throw new UnsupportedOperationException("Use fetch() or fetchFirst() methods instead");
    }
    
    /**
     * Fetch all results
     */
    public <T> void fetch(@NotNull Function<ResultSet, T> mapper, @Nullable Callback<List<T>> callback) {
        connector.connect(connection -> {
            String sql = buildSQL();
            List<T> results = new ArrayList<>();
            
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                setParameters(statement);
                
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        T result = mapper.apply(rs);
                        if (result != null) {
                            results.add(result);
                        }
                    }
                }
                
                if (callback != null) {
                    callback.accept(null, results);
                }
            } catch (SQLException ex) {
                if (callback != null) {
                    callback.accept(ex, null);
                } else {
                    throw new RuntimeException("Failed to execute SELECT query: " + sql, ex);
                }
            }
        });
    }
    
    /**
     * Fetch the first result
     */
    public <T> void fetchFirst(@NotNull Function<ResultSet, T> mapper, @Nullable Callback<T> callback) {
        limit(1);
        fetch(rs -> {
            try {
                if (rs.next()) {
                    return mapper.apply(rs);
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            return null;
        }, (ex, results) -> {
            if (callback != null) {
                if (ex != null) {
                    callback.accept(ex, null);
                } else if (results != null && !results.isEmpty()) {
                    callback.accept(null, results.get(0));
                } else {
                    callback.accept(null, null);
                }
            }
        });
    }
    
    /**
     * Fetch all results as ResultSet (for custom processing)
     */
    public void fetchResultSet(@NotNull Consumer<ResultSet> consumer) {
        connector.connect(connection -> {
            String sql = buildSQL();
            
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                setParameters(statement);
                
                try (ResultSet rs = statement.executeQuery()) {
                    consumer.accept(rs);
                }
            } catch (SQLException ex) {
                throw new RuntimeException("Failed to execute SELECT query: " + sql, ex);
            }
        });
    }
}

