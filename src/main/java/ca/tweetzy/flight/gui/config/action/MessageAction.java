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
import ca.tweetzy.flight.utils.Common;
import lombok.NonNull;

import java.util.Map;

/**
 * Built-in action handler for sending messages.
 * Usage: message(&aHello ${player.name})
 */
public final class MessageAction implements GuiConfigActionHandler {

    @Override
    public boolean execute(@NonNull GuiClickEvent event, @NonNull GuiConfigContext context, @NonNull String actionString, @NonNull Map<String, String> parameters) {
        String message = parameters.getOrDefault("message", "");
        
        if (message.isEmpty()) {
            // Try to extract from action string: message(&aHello)
            int start = actionString.indexOf('(');
            int end = actionString.indexOf(')');
            if (start > 0 && end > start) {
                message = actionString.substring(start + 1, end).trim();
                
                // Remove quotes if present
                if ((message.startsWith("\"") && message.endsWith("\"")) || 
                    (message.startsWith("'") && message.endsWith("'"))) {
                    message = message.substring(1, message.length() - 1);
                }
            }
        }
        
        if (message.isEmpty()) {
            return false;
        }
        
        // Resolve message with variables
        message = GuiConfigExpressionEngine.resolveVariables(message, context);
        
        // Colorize and send
        Common.tell(event.player, message);
        
        return true;
    }
}

