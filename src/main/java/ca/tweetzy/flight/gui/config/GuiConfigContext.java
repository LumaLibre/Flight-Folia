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

package ca.tweetzy.flight.gui.config;

import ca.tweetzy.flight.gui.Gui;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages context variables for GUI config expressions and placeholders.
 * Thread-safe for concurrent access.
 */
public final class GuiConfigContext {

    @Getter
    private final Player player;
    @Getter
    private final Gui gui;
    private final Map<String, Object> variables = new ConcurrentHashMap<>();

    public GuiConfigContext(@NonNull Player player, @NonNull Gui gui) {
        this.player = player;
        this.gui = gui;
        
        // Add default variables
        setVariable("player", player);
        setVariable("gui", gui);
        // Note: page and pages will be updated via updateGuiVariables()
        updateGuiVariables();
    }

    /**
     * Set a context variable
     */
    public void setVariable(@NonNull String key, @Nullable Object value) {
        if (value == null) {
            variables.remove(key);
        } else {
            variables.put(key, value);
        }
    }

    /**
     * Get a context variable
     */
    @Nullable
    public Object getVariable(@NonNull String key) {
        return variables.get(key);
    }

    /**
     * Get a context variable with a default value
     */
    @Nullable
    public Object getVariable(@NonNull String key, @Nullable Object defaultValue) {
        return variables.getOrDefault(key, defaultValue);
    }

    /**
     * Check if a variable exists
     */
    public boolean hasVariable(@NonNull String key) {
        return variables.containsKey(key);
    }

    /**
     * Get all variables as a map (read-only view)
     */
    @NonNull
    public Map<String, Object> getVariables() {
        return new HashMap<>(variables);
    }

    /**
     * Update GUI-specific variables (page, pages, etc.)
     */
    public void updateGuiVariables() {
        // Access page/pages via reflection since they're protected
        try {
            java.lang.reflect.Field pageField = gui.getClass().getDeclaredField("page");
            java.lang.reflect.Field pagesField = gui.getClass().getDeclaredField("pages");
            pageField.setAccessible(true);
            pagesField.setAccessible(true);
            setVariable("page", pageField.getInt(gui));
            setVariable("pages", pagesField.getInt(gui));
        } catch (Exception e) {
            // If all else fails, use default values
            setVariable("page", 1);
            setVariable("pages", 1);
        }
    }

    /**
     * Clear all variables except defaults
     */
    public void clear() {
        variables.clear();
        setVariable("player", player);
        setVariable("gui", gui);
        updateGuiVariables();
    }
}

