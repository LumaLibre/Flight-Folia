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

import lombok.NonNull;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for GUIs that can be configured via config files.
 * This is an optional feature - GUIs can still be configured programmatically.
 */
public interface ConfigurableGui {

    /**
     * Load configuration from a config file.
     * 
     * @param configName The name of the config file (without .yml extension)
     * @return true if config was loaded successfully, false otherwise
     */
    boolean loadFromConfig(@NonNull String configName);

    /**
     * Set a context variable for use in config expressions.
     * 
     * @param key The variable key
     * @param value The variable value
     */
    void setConfigContext(@NonNull String key, @Nullable Object value);

    /**
     * Get the GUI config context.
     * 
     * @return The context, or null if not initialized
     */
    @Nullable
    GuiConfigContext getConfigContext();

    /**
     * Get the player associated with this GUI.
     * 
     * @return The player
     */
    @NonNull
    Player getPlayer();
}

