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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

/**
 * Represents a database change event for multi-server synchronization
 * Uses Spigot's built-in Gson library
 */
public class DatabaseEvent {
    
    public enum EventType {
        INSERT,
        UPDATE,
        DELETE
    }
    
    private final String eventId;
    private final String serverId;
    private final EventType eventType;
    private final String tableName;
    private final String tablePrefix;
    private final Map<String, Object> data;
    private final long timestamp;
    
    private static final Gson GSON = new GsonBuilder().create();
    
    public DatabaseEvent(@NotNull String serverId,
                        @NotNull EventType eventType,
                        @NotNull String tableName,
                        @NotNull String tablePrefix,
                        @NotNull Map<String, Object> data) {
        this.eventId = UUID.randomUUID().toString();
        this.serverId = serverId;
        this.eventType = eventType;
        this.tableName = tableName;
        this.tablePrefix = tablePrefix;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getEventId() {
        return eventId;
    }
    
    public String getServerId() {
        return serverId;
    }
    
    public EventType getEventType() {
        return eventType;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public String getTablePrefix() {
        return tablePrefix;
    }
    
    public Map<String, Object> getData() {
        return data;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Serialize this event to JSON
     */
    @NotNull
    public String toJson() {
        return GSON.toJson(this);
    }
    
    /**
     * Deserialize an event from JSON
     */
    @NotNull
    public static DatabaseEvent fromJson(@NotNull String json) {
        return GSON.fromJson(json, DatabaseEvent.class);
    }
}

