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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * INSERT query builder
 */
public class InsertQuery extends Query {
    
    private final Map<String, Object> values = new LinkedHashMap<>();
    private boolean returnGeneratedKeys = false;
    
    public InsertQuery(@NotNull DatabaseConnector connector, @NotNull String tablePrefix, @NotNull String table) {
        super(connector, tablePrefix, table);
    }
    
    /**
     * Add a WHERE clause to the query (rarely used for INSERT)
     */
    public InsertQuery where(@NotNull String column, @NotNull String operator, @Nullable Object value) {
        super.where(column, operator, value);
        return this;
    }
    
    /**
     * Add a WHERE clause with = operator
     */
    public InsertQuery where(@NotNull String column, @Nullable Object value) {
        super.where(column, value);
        return this;
    }
    
    /**
     * Set a column value
     */
    public InsertQuery set(@NotNull String column, @Nullable Object value) {
        this.values.put(column, value);
        return this;
    }
    
    /**
     * Set multiple column values
     */
    public InsertQuery setAll(@NotNull Map<String, Object> values) {
        this.values.putAll(values);
        return this;
    }
    
    /**
     * Enable returning generated keys (for auto-increment IDs)
     */
    public InsertQuery returnGeneratedKeys() {
        this.returnGeneratedKeys = true;
        return this;
    }
    
    @Override
    protected String buildSQL() {
        if (values.isEmpty()) {
            throw new IllegalStateException("No values specified for INSERT query");
        }
        
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(getFullTableName());
        sql.append(" (");
        sql.append(String.join(", ", values.keySet()));
        sql.append(") VALUES (");
        
        List<String> placeholders = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            placeholders.add("?");
        }
        sql.append(String.join(", ", placeholders));
        sql.append(")");
        
        return sql.toString();
    }
    
    @Override
    protected void setParameters(PreparedStatement statement) throws SQLException {
        int index = 1;
        for (Object value : values.values()) {
            setParameter(statement, index, value);
            index++;
        }
    }
    
    @Override
    protected void executeStatement(PreparedStatement statement) throws SQLException {
        statement.executeUpdate();
    }
    
    /**
     * Execute the INSERT and return the number of affected rows
     */
    public void execute(@Nullable Callback<Integer> callback) {
        connector.connect(connection -> {
            String sql = buildSQL();
            
            try (PreparedStatement statement = returnGeneratedKeys 
                    ? connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)
                    : connection.prepareStatement(sql)) {
                
                setParameters(statement);
                int affectedRows = statement.executeUpdate();
                
                if (callback != null) {
                    callback.accept(null, affectedRows);
                }
            } catch (SQLException ex) {
                if (callback != null) {
                    callback.accept(ex, null);
                } else {
                    throw new RuntimeException("Failed to execute INSERT query: " + sql, ex);
                }
            }
        });
    }
    
    /**
     * Execute the INSERT and return the generated key (if returnGeneratedKeys was called)
     */
    public void executeWithGeneratedKey(@NotNull Callback<Long> callback) {
        returnGeneratedKeys();
        connector.connect(connection -> {
            String sql = buildSQL();
            
            try (PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                setParameters(statement);
                statement.executeUpdate();
                
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        callback.accept(null, generatedKeys.getLong(1));
                    } else {
                        callback.accept(null, null);
                    }
                }
            } catch (SQLException ex) {
                callback.accept(ex, null);
            }
        });
    }
    
    private void setParameter(PreparedStatement statement, int index, Object value) throws SQLException {
        if (value == null) {
            statement.setNull(index, java.sql.Types.NULL);
        } else if (value instanceof String) {
            statement.setString(index, (String) value);
        } else if (value instanceof Integer) {
            statement.setInt(index, (Integer) value);
        } else if (value instanceof Long) {
            statement.setLong(index, (Long) value);
        } else if (value instanceof Double) {
            statement.setDouble(index, (Double) value);
        } else if (value instanceof Float) {
            statement.setFloat(index, (Float) value);
        } else if (value instanceof Boolean) {
            statement.setBoolean(index, (Boolean) value);
        } else if (value instanceof byte[]) {
            statement.setBytes(index, (byte[]) value);
        } else if (value instanceof java.util.UUID) {
            // Convert UUID to string to avoid serialization issues with MySQL
            statement.setString(index, value.toString());
        } else {
            statement.setObject(index, value);
        }
    }
}

