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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * Repository interface for CRUD operations on entities
 * 
 * @param <T> The entity type
 * @param <ID> The ID type (typically String for UUID or Long for auto-increment)
 */
public interface Repository<T, ID> {
    
    /**
     * Save an entity (INSERT or UPDATE)
     * 
     * @param entity The entity to save
     * @param callback Optional callback for the saved entity
     */
    void save(@NotNull T entity, @Nullable Callback<T> callback);
    
    /**
     * Save multiple entities in a batch
     * 
     * @param entities The entities to save
     * @param callback Optional callback for the number of affected rows
     */
    void saveAll(@NotNull Collection<T> entities, @Nullable Callback<Integer> callback);
    
    /**
     * Find an entity by ID
     * 
     * @param id The ID to search for
     * @param callback Callback for the found entity (null if not found)
     */
    void findById(@NotNull ID id, @NotNull Callback<T> callback);
    
    /**
     * Find all entities
     * 
     * @param callback Callback for the list of entities
     */
    void findAll(@NotNull Callback<List<T>> callback);
    
    /**
     * Delete an entity by ID
     * 
     * @param id The ID to delete
     * @param callback Optional callback for whether the deletion was successful
     */
    void deleteById(@NotNull ID id, @Nullable Callback<Boolean> callback);
    
    /**
     * Delete an entity
     * 
     * @param entity The entity to delete
     * @param callback Optional callback for whether the deletion was successful
     */
    void delete(@NotNull T entity, @Nullable Callback<Boolean> callback);
    
    /**
     * Delete multiple entities
     * 
     * @param entities The entities to delete
     * @param callback Optional callback for the number of deleted entities
     */
    void deleteAll(@NotNull Collection<T> entities, @Nullable Callback<Integer> callback);
    
    /**
     * Check if an entity exists by ID
     * 
     * @param id The ID to check
     * @param callback Callback for whether the entity exists
     */
    void existsById(@NotNull ID id, @NotNull Callback<Boolean> callback);
    
    /**
     * Count all entities
     * 
     * @param callback Callback for the count
     */
    void count(@NotNull Callback<Long> callback);
}

