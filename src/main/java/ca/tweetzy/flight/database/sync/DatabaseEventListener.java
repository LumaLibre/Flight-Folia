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

package ca.tweetzy.flight.database.sync;

import org.jetbrains.annotations.NotNull;

/**
 * Interface for listening to database change events from other servers
 */
public interface DatabaseEventListener {
    
    /**
     * Called when a database event is received from another server
     * 
     * @param event The database event
     */
    void onDatabaseEvent(@NotNull DatabaseEvent event);
    
    /**
     * Get the table name this listener is interested in (null for all tables)
     * 
     * @return The table name, or null to listen to all tables
     */
    default String getTableName() {
        return null;
    }
    
    /**
     * Get the table prefix this listener is interested in (null for all prefixes)
     * 
     * @return The table prefix, or null to listen to all prefixes
     */
    default String getTablePrefix() {
        return null;
    }
}

