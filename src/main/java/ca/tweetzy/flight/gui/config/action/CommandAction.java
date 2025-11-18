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
import org.bukkit.Bukkit;

import java.util.Map;

/**
 * Built-in action handler for running commands.
 * Usage: command(/ah filter, as_console=false)
 */
public final class CommandAction implements GuiConfigActionHandler {

    @Override
    public boolean execute(@NonNull GuiClickEvent event, @NonNull GuiConfigContext context, @NonNull String actionString, @NonNull Map<String, String> parameters) {
        String command = parameters.getOrDefault("command", "");
        
        if (command.isEmpty()) {
            // Try to extract from action string: command(/ah filter)
            int start = actionString.indexOf('(');
            int end = actionString.indexOf(')');
            if (start > 0 && end > start) {
                String args = actionString.substring(start + 1, end);
                // Find first comma or closing paren
                int commaIndex = args.indexOf(',');
                if (commaIndex > 0) {
                    command = args.substring(0, commaIndex).trim();
                } else {
                    command = args.trim();
                }
                
                // Remove quotes if present
                if ((command.startsWith("\"") && command.endsWith("\"")) || 
                    (command.startsWith("'") && command.endsWith("'"))) {
                    command = command.substring(1, command.length() - 1);
                }
            }
        }
        
        if (command.isEmpty()) {
            return false;
        }
        
        // Resolve command with variables
        command = GuiConfigExpressionEngine.resolveVariables(command, context);
        
        // Remove leading slash if present
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        
        // Check if should run as console
        boolean asConsole = Boolean.parseBoolean(parameters.getOrDefault("as_console", "false"));
        
        // Execute command
        if (asConsole) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        } else {
            event.player.performCommand(command);
        }
        
        return true;
    }
}

