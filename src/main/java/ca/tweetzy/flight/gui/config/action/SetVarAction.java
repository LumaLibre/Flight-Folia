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
 * Built-in action handler for setting context variables.
 * Usage: set_var(filter_type, new_value)
 */
public final class SetVarAction implements GuiConfigActionHandler {

    @Override
    public boolean execute(@NonNull GuiClickEvent event, @NonNull GuiConfigContext context, @NonNull String actionString, @NonNull Map<String, String> parameters) {
        String varName = parameters.getOrDefault("var", parameters.getOrDefault("name", ""));
        String varValue = parameters.getOrDefault("value", "");
        
        if (varName.isEmpty()) {
            // Try to extract from action string: set_var(filter_type, new_value)
            int start = actionString.indexOf('(');
            int end = actionString.indexOf(')');
            if (start > 0 && end > start) {
                String args = actionString.substring(start + 1, end);
                String[] parts = args.split(",", 2);
                if (parts.length > 0) {
                    varName = parts[0].trim();
                }
                if (parts.length > 1) {
                    varValue = parts[1].trim();
                    // Remove quotes if present
                    if ((varValue.startsWith("\"") && varValue.endsWith("\"")) || 
                        (varValue.startsWith("'") && varValue.endsWith("'"))) {
                        varValue = varValue.substring(1, varValue.length() - 1);
                    }
                }
            }
        }
        
        if (varName.isEmpty()) {
            return false;
        }
        
        // Resolve value with variables
        if (!varValue.isEmpty()) {
            varValue = GuiConfigExpressionEngine.resolveVariables(varValue, context);
        }
        
        // Set variable
        context.setVariable(varName, varValue);
        
        return true;
    }
}

