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
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Registry for GUI config action handlers.
 * Thread-safe for concurrent access.
 */
public final class GuiConfigActionRegistry {

    private static final Map<String, GuiConfigActionHandler> handlers = new ConcurrentHashMap<>();
    private static final Pattern ACTION_PATTERN = Pattern.compile("([a-z_]+)(?:\\(([^)]*)\\))?");
    private static final Pattern PARAM_PATTERN = Pattern.compile("([a-z_]+)=([^,]+)");

    static {
        // Register built-in actions
        registerAction("close", new CloseAction());
        registerAction("back", new BackAction());
        registerAction("next_page", new NextPageAction());
        registerAction("prev_page", new PrevPageAction());
        registerAction("previous_page", new PrevPageAction()); // Alias
        registerAction("open_gui", new OpenGuiAction());
        registerAction("command", new CommandAction());
        registerAction("play_sound", new PlaySoundAction());
        registerAction("message", new MessageAction());
        registerAction("set_var", new SetVarAction());
    }

    /**
     * Register a custom action handler.
     * 
     * @param actionId The action ID (e.g., "open_gui", "custom:my_handler")
     * @param handler The handler implementation
     */
    public static void registerAction(@NonNull String actionId, @NonNull GuiConfigActionHandler handler) {
        handlers.put(actionId.toLowerCase(), handler);
    }

    /**
     * Unregister an action handler.
     */
    public static void unregisterAction(@NonNull String actionId) {
        handlers.remove(actionId.toLowerCase());
    }

    /**
     * Execute an action string.
     * Supports formats like:
     * - "close"
     * - "open_gui(menu_name)"
     * - "open_gui(menu_name, param1=value1, param2=value2)"
     * - "chain:action1;action2;action3"
     * - "if:${condition}:action1:action2"
     * 
     * @param event The click event
     * @param context The GUI config context
     * @param actionString The action string to execute
     * @return true if the action was handled, false otherwise
     */
    public static boolean executeAction(@NonNull GuiClickEvent event, @NonNull GuiConfigContext context, @NonNull String actionString) {
        if (actionString == null || actionString.trim().isEmpty()) {
            return false;
        }

        String trimmed = actionString.trim();

        // Handle chained actions
        if (trimmed.startsWith("chain:")) {
            String[] actions = trimmed.substring(6).split(";");
            boolean allHandled = true;
            for (String action : actions) {
                if (!executeAction(event, context, action.trim())) {
                    allHandled = false;
                }
            }
            return allHandled;
        }

        // Handle conditional actions
        if (trimmed.startsWith("if:")) {
            String[] parts = trimmed.substring(3).split(":", 3);
            if (parts.length >= 2) {
                String condition = parts[0];
                String trueAction = parts.length > 1 ? parts[1] : "";
                String falseAction = parts.length > 2 ? parts[2] : "";

                boolean conditionResult = GuiConfigExpressionEngine.evaluateBoolean(condition, context);
                String actionToExecute = conditionResult ? trueAction : falseAction;
                
                if (!actionToExecute.isEmpty()) {
                    return executeAction(event, context, actionToExecute);
                }
            }
            return false;
        }

        // Handle custom actions
        if (trimmed.startsWith("custom:")) {
            String customId = trimmed.substring(7);
            GuiConfigActionHandler handler = handlers.get("custom:" + customId);
            if (handler != null) {
                Map<String, String> params = new HashMap<>();
                return handler.execute(event, context, trimmed, params);
            }
            return false;
        }

        // Parse standard action format: action_name(param1=value1, param2=value2)
        Matcher matcher = ACTION_PATTERN.matcher(trimmed);
        if (matcher.matches()) {
            String actionId = matcher.group(1);
            String paramString = matcher.group(2);

            Map<String, String> parameters = parseParameters(paramString);

            GuiConfigActionHandler handler = handlers.get(actionId);
            if (handler != null) {
                return handler.execute(event, context, trimmed, parameters);
            }
        }

        return false;
    }

    @NonNull
    private static Map<String, String> parseParameters(@Nullable String paramString) {
        Map<String, String> params = new HashMap<>();
        
        if (paramString == null || paramString.trim().isEmpty()) {
            return params;
        }

        Matcher matcher = PARAM_PATTERN.matcher(paramString);
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2).trim();
            // Remove quotes if present
            if ((value.startsWith("\"") && value.endsWith("\"")) || 
                (value.startsWith("'") && value.endsWith("'"))) {
                value = value.substring(1, value.length() - 1);
            }
            params.put(key, value);
        }

        return params;
    }
}

