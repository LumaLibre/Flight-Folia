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

import ca.tweetzy.flight.database.Callback;
import ca.tweetzy.flight.database.DatabaseConnector;
import ca.tweetzy.flight.database.query.QueryBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Base implementation of Repository interface
 * 
 * @param <T> The entity type
 * @param <ID> The ID type
 */
public class BaseRepository<T, ID> implements Repository<T, ID> {
    
    protected final DatabaseConnector connector;
    protected final String tablePrefix;
    protected final String tableName;
    protected final EntityMapper<T> mapper;
    protected final QueryBuilder queryBuilder;
    
    public BaseRepository(@NotNull DatabaseConnector connector, 
                         @NotNull String tablePrefix, 
                         @NotNull String tableName,
                         @NotNull EntityMapper<T> mapper) {
        this.connector = connector;
        this.tablePrefix = tablePrefix;
        this.tableName = tableName;
        this.mapper = mapper;
        this.queryBuilder = new QueryBuilder(connector, tablePrefix);
    }
    
    @Override
    public void save(@NotNull T entity, @Nullable Callback<T> callback) {
        Map<String, Object> values = mapper.toMap(entity);
        Object id = mapper.getId(entity);
        
        // Check if entity exists
        @SuppressWarnings("unchecked")
        ID typedId = (ID) id;
        existsById(typedId, (ex, exists) -> {
            if (ex != null) {
                if (callback != null) {
                    callback.accept(ex, null);
                }
                return;
            }
            
            if (exists != null && exists) {
                // Update existing entity
                ca.tweetzy.flight.database.query.UpdateQuery updateQuery = queryBuilder.update(tableName)
                    .setAll(values)
                    .where(mapper.getIdColumn(), id);
                updateQuery.execute((updateEx, affectedRows) -> {
                    if (callback != null) {
                        if (updateEx != null) {
                            callback.accept(updateEx, null);
                        } else {
                            callback.accept(null, entity);
                        }
                    }
                });
            } else {
                // Insert new entity
                ca.tweetzy.flight.database.query.InsertQuery insertQuery = queryBuilder.insert(tableName)
                    .setAll(values);
                insertQuery.execute((insertEx, affectedRows) -> {
                    if (callback != null) {
                        if (insertEx != null) {
                            callback.accept(insertEx, null);
                        } else {
                            callback.accept(null, entity);
                        }
                    }
                });
            }
        });
    }
    
    @Override
    public void saveAll(@NotNull Collection<T> entities, @Nullable Callback<Integer> callback) {
        if (entities.isEmpty()) {
            if (callback != null) {
                callback.accept(null, 0);
            }
            return;
        }
        
        connector.connect(connection -> {
            int totalAffected = 0;
            Exception lastException = null;
            
            try {
                connection.setAutoCommit(false);
                
                for (T entity : entities) {
                    Map<String, Object> values = mapper.toMap(entity);
                    Object id = mapper.getId(entity);
                    
                    // Simple existence check - try update first, if no rows affected, insert
                    String updateSql = "UPDATE " + tablePrefix + tableName + " SET ";
                    List<String> setClauses = new java.util.ArrayList<>();
                    for (String column : values.keySet()) {
                        setClauses.add(column + " = ?");
                    }
                    updateSql += String.join(", ", setClauses);
                    updateSql += " WHERE " + mapper.getIdColumn() + " = ?";
                    
                    try (java.sql.PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                        int paramIndex = 1;
                        for (Object value : values.values()) {
                            setParameter(updateStmt, paramIndex++, value);
                        }
                        setParameter(updateStmt, paramIndex, id);
                        
                        int affected = updateStmt.executeUpdate();
                        
                        if (affected == 0) {
                            // No update occurred, do insert
                            String insertSql = "INSERT INTO " + tablePrefix + tableName + " (";
                            insertSql += String.join(", ", values.keySet()) + ") VALUES (";
                            List<String> placeholders = new java.util.ArrayList<>();
                            for (int i = 0; i < values.size(); i++) {
                                placeholders.add("?");
                            }
                            insertSql += String.join(", ", placeholders) + ")";
                            
                            try (java.sql.PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                                paramIndex = 1;
                                for (Object value : values.values()) {
                                    setParameter(insertStmt, paramIndex++, value);
                                }
                                insertStmt.executeUpdate();
                                totalAffected++;
                            }
                        } else {
                            totalAffected += affected;
                        }
                    }
                }
                
                connection.commit();
                
                if (callback != null) {
                    callback.accept(null, totalAffected);
                }
            } catch (Exception ex) {
                lastException = ex;
                try {
                    connection.rollback();
                } catch (java.sql.SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (java.sql.SQLException ex) {
                    ex.printStackTrace();
                }
            }
            
            if (lastException != null && callback != null) {
                callback.accept(lastException, null);
            }
        });
    }
    
    @Override
    public void findById(@NotNull ID id, @NotNull Callback<T> callback) {
        ca.tweetzy.flight.database.query.SelectQuery selectQuery = queryBuilder.select(tableName)
            .where(mapper.getIdColumn(), id);
        selectQuery.fetchFirst(rs -> {
            try {
                return mapper.map(rs);
            } catch (java.sql.SQLException ex) {
                throw new RuntimeException(ex);
            }
        }, callback);
    }
    
    @Override
    public void findAll(@NotNull Callback<List<T>> callback) {
        queryBuilder.select(tableName)
            .fetch(rs -> {
                try {
                    return mapper.map(rs);
                } catch (java.sql.SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }, callback);
    }
    
    @Override
    public void deleteById(@NotNull ID id, @Nullable Callback<Boolean> callback) {
        ca.tweetzy.flight.database.query.DeleteQuery deleteQuery = queryBuilder.delete(tableName)
            .where(mapper.getIdColumn(), id);
        deleteQuery.execute((ex, affectedRows) -> {
            if (callback != null) {
                callback.accept(ex, affectedRows != null && affectedRows > 0);
            }
        });
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void delete(@NotNull T entity, @Nullable Callback<Boolean> callback) {
        Object id = mapper.getId(entity);
        deleteById((ID) id, callback);
    }
    
    @Override
    public void deleteAll(@NotNull Collection<T> entities, @Nullable Callback<Integer> callback) {
        if (entities.isEmpty()) {
            if (callback != null) {
                callback.accept(null, 0);
            }
            return;
        }
        
        connector.connect(connection -> {
            int totalDeleted = 0;
            Exception lastException = null;
            
            try {
                connection.setAutoCommit(false);
                String sql = "DELETE FROM " + tablePrefix + tableName + " WHERE " + mapper.getIdColumn() + " = ?";
                
                try (java.sql.PreparedStatement statement = connection.prepareStatement(sql)) {
                    for (T entity : entities) {
                        Object id = mapper.getId(entity);
                        setParameter(statement, 1, id);
                        statement.addBatch();
                    }
                    
                    int[] results = statement.executeBatch();
                    for (int result : results) {
                        totalDeleted += result;
                    }
                }
                
                connection.commit();
                
                if (callback != null) {
                    callback.accept(null, totalDeleted);
                }
            } catch (Exception ex) {
                lastException = ex;
                try {
                    connection.rollback();
                } catch (java.sql.SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (java.sql.SQLException ex) {
                    ex.printStackTrace();
                }
            }
            
            if (lastException != null && callback != null) {
                callback.accept(lastException, null);
            }
        });
    }
    
    @Override
    public void existsById(@NotNull ID id, @NotNull Callback<Boolean> callback) {
        ca.tweetzy.flight.database.query.SelectQuery selectQuery = queryBuilder.select(tableName)
            .columns(mapper.getIdColumn())
            .where(mapper.getIdColumn(), id);
        selectQuery.fetchFirst(rs -> {
            try {
                return true; // If we get a result, it exists
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }, (ex, result) -> {
            if (callback != null) {
                callback.accept(ex, result != null);
            }
        });
    }
    
    @Override
    public void count(@NotNull Callback<Long> callback) {
        queryBuilder.select(tableName)
            .columns("COUNT(*) as count")
            .fetchFirst(rs -> {
                try {
                    if (rs.next()) {
                        return rs.getLong("count");
                    }
                    return 0L;
                } catch (java.sql.SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }, callback);
    }
    
    private void setParameter(java.sql.PreparedStatement statement, int index, Object value) throws java.sql.SQLException {
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
        } else {
            statement.setObject(index, value);
        }
    }
}

