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

import ca.tweetzy.flight.command.brigadier.BrigadierCommandManager;
import ca.tweetzy.flight.command.brigadier.ModernBrigadierManager;
import ca.tweetzy.flight.utils.Common;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Modern command manager that combines all command systems
 * Provides a unified API for traditional, annotation-based, and Brigadier commands
 */
@Getter
public final class ModernCommandManager {

    private final JavaPlugin plugin;
    private final CommandManager commandManager;
    private final AnnotationCommandHandler annotationHandler;
    private final BrigadierCommandManager brigadierManager;
    private final ModernBrigadierManager modernBrigadierManager;
    private final AsyncCommandExecutor asyncExecutor;

    public ModernCommandManager(@NonNull JavaPlugin plugin) {
        this.plugin = plugin;
        this.commandManager = new CommandManager(plugin);
        this.annotationHandler = new AnnotationCommandHandler(plugin, commandManager);
        this.brigadierManager = new BrigadierCommandManager(plugin);
        this.modernBrigadierManager = new ModernBrigadierManager(plugin);
        this.asyncExecutor = new AsyncCommandExecutor(plugin);
    }

    /**
     * Get the traditional command manager
     */
    public CommandManager getTraditional() {
        return commandManager;
    }

    /**
     * Get the annotation command handler
     */
    public AnnotationCommandHandler getAnnotation() {
        return annotationHandler;
    }

    /**
     * Get the Brigadier command manager (1.13+)
     */
    public BrigadierCommandManager getBrigadier() {
        return brigadierManager;
    }

    /**
     * Get the modern Brigadier manager with CommandDispatcher
     */
    public ModernBrigadierManager getModernBrigadier() {
        return modernBrigadierManager;
    }

    /**
     * Get the async command executor
     */
    public AsyncCommandExecutor getAsync() {
        return asyncExecutor;
    }

    /**
     * Check if modern Brigadier features are available
     */
    public boolean isModernBrigadierAvailable() {
        return ModernBrigadierManager.isAvailable();
    }

    /**
     * Check if basic Brigadier is available
     */
    public boolean isBrigadierAvailable() {
        return BrigadierCommandManager.isAvailable();
    }

    /**
     * Register commands from an object (annotation-based)
     */
    public ModernCommandManager registerAnnotations(@NonNull Object instance) {
        annotationHandler.registerCommands(instance);
        return this;
    }

    /**
     * Add a traditional command
     */
    public ModernCommandManager addCommand(@NonNull Command command) {
        commandManager.addCommand(command);
        return this;
    }

    /**
     * Add multiple traditional commands
     */
    public ModernCommandManager addCommands(@NonNull Command... commands) {
        commandManager.addCommands(commands);
        return this;
    }

    /**
     * Create a main command
     */
    public MainCommand createMainCommand(@NonNull String name) {
        return commandManager.addMainCommand(name);
    }

    /**
     * Log command system status
     */
    public void logStatus() {
        Common.log("&8&m-----------------------------------------------------");
        Common.log("&aCommand System Status:");
        Common.log("&7Traditional Commands: &aEnabled");
        Common.log("&7Annotation Commands: &aEnabled");
        Common.log("&7Brigadier Support: " + (isBrigadierAvailable() ? "&aAvailable" : "&cNot Available (Requires 1.13+)"));
        Common.log("&7Modern Brigadier: " + (isModernBrigadierAvailable() ? "&aAvailable" : "&cNot Available (Requires 1.13+)"));
        Common.log("&7Async Execution: &aEnabled");
        Common.log("&8&m-----------------------------------------------------");
    }
}

