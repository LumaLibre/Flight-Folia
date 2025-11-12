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

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * Represents the context in which a command is executed.
 * Provides structured access to command execution data.
 *
 * @author Kiran Hart
 */
@Getter
public final class CommandContext {

    private final CommandSender sender;
    private final String[] args;
    private final String label;
    private final String fullCommand;
    private final boolean isPlayer;
    private final boolean isConsole;
    private final Player player;

    public CommandContext(@NonNull CommandSender sender, @NonNull String[] args, @NonNull String label) {
        this.sender = sender;
        this.args = args;
        this.label = label;
        this.isPlayer = sender instanceof Player;
        this.isConsole = !isPlayer;
        this.player = isPlayer ? (Player) sender : null;
        this.fullCommand = label + (args.length > 0 ? " " + String.join(" ", args) : "");
    }

    /**
     * Get argument at index, or null if out of bounds
     */
    public String getArg(int index) {
        return index >= 0 && index < args.length ? args[index] : null;
    }

    /**
     * Get argument at index with default value
     */
    public String getArg(int index, String defaultValue) {
        String arg = getArg(index);
        return arg != null ? arg : defaultValue;
    }

    /**
     * Check if argument exists at index
     */
    public boolean hasArg(int index) {
        return index >= 0 && index < args.length;
    }

    /**
     * Get number of arguments
     */
    public int getArgCount() {
        return args.length;
    }

    /**
     * Get all arguments as a list
     */
    public List<String> getArgs() {
        return Arrays.asList(args);
    }

    /**
     * Get arguments from start index to end
     */
    public String[] getArgs(int start) {
        if (start >= args.length) {
            return new String[0];
        }
        return Arrays.copyOfRange(args, start, args.length);
    }

    /**
     * Get arguments from start index to end index
     */
    public String[] getArgs(int start, int end) {
        if (start >= args.length || end <= start) {
            return new String[0];
        }
        int actualEnd = Math.min(end, args.length);
        return Arrays.copyOfRange(args, start, actualEnd);
    }

    /**
     * Join arguments from start index with space
     */
    public String joinArgs(int start) {
        return String.join(" ", getArgs(start));
    }

    /**
     * Join arguments from start to end with space
     */
    public String joinArgs(int start, int end) {
        return String.join(" ", getArgs(start, end));
    }
}

