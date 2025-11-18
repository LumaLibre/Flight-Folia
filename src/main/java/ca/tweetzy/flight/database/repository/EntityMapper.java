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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * Interface for mapping database rows to entity objects
 * 
 * @param <T> The entity type
 */
public interface EntityMapper<T> {
    
    /**
     * Map a ResultSet row to an entity object
     * 
     * @param resultSet The ResultSet positioned at the row to map
     * @return The mapped entity, or null if the row should be skipped
     * @throws SQLException If an error occurs reading from the ResultSet
     */
    @Nullable
    T map(@NotNull ResultSet resultSet) throws SQLException;
    
    /**
     * Map an entity to a map of column names and values for INSERT/UPDATE operations
     * 
     * @param entity The entity to map
     * @return A map of column names to values
     */
    @NotNull
    Map<String, Object> toMap(@NotNull T entity);
    
    /**
     * Get the primary key value from an entity
     * 
     * @param entity The entity
     * @return The primary key value (typically a String UUID or Long ID)
     */
    @NotNull
    Object getId(@NotNull T entity);
    
    /**
     * Get the name of the primary key column
     * 
     * @return The primary key column name
     */
    @NotNull
    String getIdColumn();
}

