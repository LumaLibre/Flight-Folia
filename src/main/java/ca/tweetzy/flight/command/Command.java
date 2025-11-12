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

import ca.tweetzy.flight.utils.Common;
import lombok.NonNull;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Date Created: April 09 2022
 * Time Created: 11:37 p.m.
 *
 * @author Kiran Hart
 */
public abstract class Command {

    private final AllowedExecutor allowedExecutor;
    private final List<String> subCommands = new ArrayList<>();
    private boolean async = false;
    private JavaPlugin plugin;

    protected Command(AllowedExecutor allowedExecutor, String... subCommands) {
        this.allowedExecutor = allowedExecutor;
        this.subCommands.addAll(Arrays.asList(subCommands));
    }

    /**
     * Set whether this command should execute asynchronously
     * @param async true to run async, false for sync (default)
     * @return this command instance
     */
    public Command setAsync(boolean async) {
        this.async = async;
        return this;
    }

    /**
     * Check if this command runs asynchronously
     */
    public boolean isAsync() {
        return async;
    }

    /**
     * Set the plugin instance (required for async execution)
     * Package-private to allow CommandManager to set it
     */
    void setPlugin(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    protected JavaPlugin getPlugin() {
        return plugin;
    }

    public final List<String> getSubCommands() {
        return Collections.unmodifiableList(this.subCommands);
    }

    public final void addSubCommand(String command) {
        this.subCommands.add(command);
    }

    protected boolean isNoConsole() {
        return this.allowedExecutor == AllowedExecutor.PLAYER;
    }

    public AllowedExecutor getAllowedExecutor() {
        return allowedExecutor;
    }

    /**
     * Execute command (legacy method - required for backwards compatibility)
     * New implementations should override {@link #execute(CommandContext)} instead
     */
    protected abstract ReturnType execute(CommandSender sender, String... args);

    /**
     * Execute command with context (modern approach)
     * Override this method for new implementations.
     * Default implementation calls legacy method for backwards compatibility.
     */
    protected ReturnType execute(@NonNull CommandContext context) {
        // Default implementation calls legacy method for backwards compatibility
        return execute(context.getSender(), context.getArgs().toArray(new String[0]));
    }

    /**
     * Tab completion (legacy method - required for backwards compatibility)
     * New implementations should override {@link #tab(CommandContext)} instead
     */
    protected abstract List<String> tab(CommandSender sender, String... args);

    /**
     * Tab completion with context (modern approach)
     * Override this method for new implementations.
     * Default implementation calls legacy method for backwards compatibility.
     */
    protected List<String> tab(@NonNull CommandContext context) {
        // Default implementation calls legacy method for backwards compatibility
        return tab(context.getSender(), context.getArgs().toArray(new String[0]));
    }

    public abstract String getPermissionNode();

    public abstract String getSyntax();

    public abstract String getDescription();

    protected void tell(@NonNull final CommandSender sender, @NonNull final String msg) {
        Common.tell(sender, true, msg);
    }

    protected void tellNoPrefix(@NonNull final CommandSender sender, @NonNull final String msg) {
        Common.tell(sender, false, msg);
    }

    /**
     * Internal method to execute command with async support
     * If async is enabled, the command should use AsyncCommandExecutor
     * for proper async execution with callbacks
     */
    final ReturnType executeInternal(CommandContext context) {
        // Execute synchronously
        // For async execution, use AsyncCommandExecutor in the command implementation
        return execute(context);
    }

    /**
     * Internal method for tab completion
     */
    final List<String> tabInternal(CommandContext context) {
        return tab(context);
    }
}
