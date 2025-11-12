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

package ca.tweetzy.flight.command;

import java.util.*;

/**
 * Date Created: April 09 2022
 * Time Created: 11:42 p.m.
 *
 * @author Kiran Hart
 */
public final class NestedCommand {

    final Command parent;
    final LinkedHashMap<String, Command> children = new LinkedHashMap<>();
    
    // Optimized: Pre-computed multi-word subcommand keys for faster lookup
    private final Set<String> multiWordKeys = new HashSet<>();
    private final Map<String, String> prefixMap = new HashMap<>(); // For loose command matching

    protected NestedCommand(Command parent) {
        this.parent = parent;
    }

    public NestedCommand addSubCommand(Command command) {
        command.getSubCommands().forEach(cmd -> {
            String lowerKey = cmd.toLowerCase();
            children.put(lowerKey, command);
            
            // Track multi-word commands for optimized lookup
            if (cmd.indexOf(' ') != -1) {
                multiWordKeys.add(lowerKey);
            }
            
            // Build prefix map for loose matching
            String firstWord = cmd.split(" ")[0].toLowerCase();
            if (!prefixMap.containsKey(firstWord) || prefixMap.get(firstWord).length() > lowerKey.length()) {
                prefixMap.put(firstWord, lowerKey);
            }
        });
        return this;
    }

    public NestedCommand addSubCommands(Command... commands) {
        for (Command command : commands) {
            addSubCommand(command);
        }
        return this;
    }

    /**
     * Check if this nested command has multi-word subcommands
     */
    boolean hasMultiWordSubcommands() {
        return !multiWordKeys.isEmpty();
    }

    /**
     * Get all multi-word keys (for optimized matching)
     */
    Set<String> getMultiWordKeys() {
        return Collections.unmodifiableSet(multiWordKeys);
    }

    /**
     * Find closest match for prefix (for loose command matching)
     */
    String findClosestMatch(String prefix) {
        String lowerPrefix = prefix.toLowerCase();
        
        // Exact match check
        if (children.containsKey(lowerPrefix)) {
            return lowerPrefix;
        }
        
        // Prefix match
        String match = null;
        int count = 0;
        for (String key : children.keySet()) {
            if (key.startsWith(lowerPrefix)) {
                match = key;
                if (++count > 1) {
                    // Multiple matches, return null
                    return null;
                }
            }
        }
        
        return match;
    }
}
