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
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Argument parser for commands with type-safe argument extraction
 *
 * @author Kiran Hart
 */
@Getter
public final class ArgumentParser {

    private final CommandContext context;
    private final List<String> errors = new ArrayList<>();

    public ArgumentParser(@NonNull CommandContext context) {
        this.context = context;
    }

    /**
     * Get string argument at index
     */
    public String getString(int index) {
        return context.getArg(index);
    }

    /**
     * Get string argument at index with default
     */
    public String getString(int index, String defaultValue) {
        return context.getArg(index, defaultValue);
    }

    /**
     * Get required string argument at index
     */
    public String getRequiredString(int index) throws ArgumentException {
        String arg = context.getArg(index);
        if (arg == null || arg.isEmpty()) {
            throw new ArgumentException("Missing required argument at index " + index);
        }
        return arg;
    }

    /**
     * Get integer argument at index
     */
    public Integer getInt(int index) {
        String arg = context.getArg(index);
        if (arg == null) return null;
        try {
            return Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            errors.add("Invalid integer at index " + index + ": " + arg);
            return null;
        }
    }

    /**
     * Get required integer argument at index
     */
    public int getRequiredInt(int index) throws ArgumentException {
        String arg = context.getArg(index);
        if (arg == null) {
            throw new ArgumentException("Missing required integer at index " + index);
        }
        try {
            return Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            throw new ArgumentException("Invalid integer at index " + index + ": " + arg);
        }
    }

    /**
     * Get double argument at index
     */
    public Double getDouble(int index) {
        String arg = context.getArg(index);
        if (arg == null) return null;
        try {
            return Double.parseDouble(arg);
        } catch (NumberFormatException e) {
            errors.add("Invalid number at index " + index + ": " + arg);
            return null;
        }
    }

    /**
     * Get required double argument at index
     */
    public double getRequiredDouble(int index) throws ArgumentException {
        String arg = context.getArg(index);
        if (arg == null) {
            throw new ArgumentException("Missing required number at index " + index);
        }
        try {
            return Double.parseDouble(arg);
        } catch (NumberFormatException e) {
            throw new ArgumentException("Invalid number at index " + index + ": " + arg);
        }
    }

    /**
     * Get boolean argument at index
     */
    public Boolean getBoolean(int index) {
        String arg = context.getArg(index);
        if (arg == null) return null;
        String lower = arg.toLowerCase();
        if (lower.equals("true") || lower.equals("1") || lower.equals("yes") || lower.equals("on")) {
            return true;
        }
        if (lower.equals("false") || lower.equals("0") || lower.equals("no") || lower.equals("off")) {
            return false;
        }
        errors.add("Invalid boolean at index " + index + ": " + arg);
        return null;
    }

    /**
     * Get required boolean argument at index
     */
    public boolean getRequiredBoolean(int index) throws ArgumentException {
        String arg = context.getArg(index);
        if (arg == null) {
            throw new ArgumentException("Missing required boolean at index " + index);
        }
        String lower = arg.toLowerCase();
        if (lower.equals("true") || lower.equals("1") || lower.equals("yes") || lower.equals("on")) {
            return true;
        }
        if (lower.equals("false") || lower.equals("0") || lower.equals("no") || lower.equals("off")) {
            return false;
        }
        throw new ArgumentException("Invalid boolean at index " + index + ": " + arg);
    }

    /**
     * Get player argument at index (online only)
     */
    public Player getPlayer(int index) {
        String arg = context.getArg(index);
        if (arg == null) return null;
        Player player = Bukkit.getPlayer(arg);
        if (player == null) {
            errors.add("Player not found: " + arg);
        }
        return player;
    }

    /**
     * Get required player argument at index
     */
    public Player getRequiredPlayer(int index) throws ArgumentException {
        String arg = context.getArg(index);
        if (arg == null) {
            throw new ArgumentException("Missing required player name at index " + index);
        }
        Player player = Bukkit.getPlayer(arg);
        if (player == null) {
            throw new ArgumentException("Player not found: " + arg);
        }
        return player;
    }

    /**
     * Get offline player argument at index
     */
    @SuppressWarnings("deprecation")
    public OfflinePlayer getOfflinePlayer(int index) {
        String arg = context.getArg(index);
        if (arg == null) return null;
        return Bukkit.getOfflinePlayer(arg);
    }

    /**
     * Get UUID argument at index
     */
    public UUID getUUID(int index) {
        String arg = context.getArg(index);
        if (arg == null) return null;
        try {
            return UUID.fromString(arg);
        } catch (IllegalArgumentException e) {
            errors.add("Invalid UUID at index " + index + ": " + arg);
            return null;
        }
    }

    /**
     * Get required UUID argument at index
     */
    public UUID getRequiredUUID(int index) throws ArgumentException {
        String arg = context.getArg(index);
        if (arg == null) {
            throw new ArgumentException("Missing required UUID at index " + index);
        }
        try {
            return UUID.fromString(arg);
        } catch (IllegalArgumentException e) {
            throw new ArgumentException("Invalid UUID at index " + index + ": " + arg);
        }
    }

    /**
     * Check if argument exists at index
     */
    public boolean hasArg(int index) {
        return context.hasArg(index);
    }

    /**
     * Get number of arguments
     */
    public int getArgCount() {
        return context.getArgCount();
    }

    /**
     * Check if there are any parsing errors
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Get all parsing errors
     */
    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }

    /**
     * Exception thrown when required argument is missing or invalid
     */
    public static class ArgumentException extends Exception {
        public ArgumentException(String message) {
            super(message);
        }
    }
}

