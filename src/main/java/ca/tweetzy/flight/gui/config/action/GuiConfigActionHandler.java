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

package ca.tweetzy.flight.gui.config.action;

import ca.tweetzy.flight.gui.config.GuiConfigContext;
import ca.tweetzy.flight.gui.events.GuiClickEvent;
import lombok.NonNull;

/**
 * Interface for action handlers in GUI configs.
 */
public interface GuiConfigActionHandler {
    
    /**
     * Execute the action.
     * 
     * @param event The click event
     * @param context The GUI config context
     * @param actionString The full action string (e.g., "open_gui(menu, param=value)")
     * @param parameters Parsed parameters from the action string
     * @return true if the action was handled, false otherwise
     */
    boolean execute(@NonNull GuiClickEvent event, @NonNull GuiConfigContext context, @NonNull String actionString, @NonNull java.util.Map<String, String> parameters);
}

