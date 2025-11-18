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
import ca.tweetzy.flight.gui.config.GuiConfigExpressionEngine;
import ca.tweetzy.flight.gui.events.GuiClickEvent;
import lombok.NonNull;

import java.util.Map;

/**
 * Built-in action handler for opening another GUI.
 * Usage: open_gui(gui_name, param1=value1, param2=value2)
 */
public final class OpenGuiAction implements GuiConfigActionHandler {

    @Override
    public boolean execute(@NonNull GuiClickEvent event, @NonNull GuiConfigContext context, @NonNull String actionString, @NonNull Map<String, String> parameters) {
        // This is a placeholder - actual implementation would require
        // a GUI factory/registry system to create GUIs by name
        // For now, we'll just return false to indicate it's not fully implemented
        // Plugins can register custom handlers for their specific GUI types
        
        String guiName = parameters.getOrDefault("gui", parameters.getOrDefault("name", ""));
        if (guiName.isEmpty()) {
            // Try to extract from action string: open_gui(gui_name)
            int start = actionString.indexOf('(');
            int end = actionString.indexOf(')');
            if (start > 0 && end > start) {
                String args = actionString.substring(start + 1, end);
                String[] parts = args.split(",");
                if (parts.length > 0) {
                    guiName = parts[0].trim();
                }
            }
        }
        
        if (guiName.isEmpty()) {
            return false;
        }
        
        // Resolve GUI name with variables
        guiName = GuiConfigExpressionEngine.resolveVariables(guiName, context);
        
        // Note: Actual GUI opening would require a GUI factory/registry
        // This is a placeholder for the action structure
        // Plugins should register custom handlers for their GUI types
        return false;
    }
}

