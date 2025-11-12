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

import ca.tweetzy.flight.command.annotations.Command;
import ca.tweetzy.flight.command.annotations.SubCommand;
import ca.tweetzy.flight.command.annotations.TabComplete;
import ca.tweetzy.flight.utils.Common;
import lombok.NonNull;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Handles annotation-based command registration
 * 
 * Usage:
 * <pre>
 * {@code
 * AnnotationCommandHandler handler = new AnnotationCommandHandler(plugin, commandManager);
 * handler.registerCommands(this); // Register commands from current class
 * }
 * </pre>
 */
public final class AnnotationCommandHandler {

    private final JavaPlugin plugin;
    private final CommandManager commandManager;
    private final Map<String, Method> commandMethods = new HashMap<>();
    private final Map<String, Method> subCommandMethods = new HashMap<>();
    private final Map<String, Method> tabCompleteMethods = new HashMap<>();

    public AnnotationCommandHandler(@NonNull JavaPlugin plugin, @NonNull CommandManager commandManager) {
        this.plugin = plugin;
        this.commandManager = commandManager;
    }

    /**
     * Register all annotated commands from an object
     */
    public void registerCommands(@NonNull Object instance) {
        Class<?> clazz = instance.getClass();
        
        // Process all methods
        for (Method method : clazz.getDeclaredMethods()) {
            // Process @Command annotations
            if (method.isAnnotationPresent(ca.tweetzy.flight.command.annotations.Command.class)) {
                registerCommand(instance, method);
            }
            
            // Process @SubCommand annotations
            if (method.isAnnotationPresent(SubCommand.class)) {
                registerSubCommand(instance, method);
            }
            
            // Process @TabComplete annotations
            if (method.isAnnotationPresent(TabComplete.class)) {
                registerTabComplete(instance, method);
            }
        }
    }

    private void registerCommand(@NonNull Object instance, @NonNull Method method) {
        Command annotation = method.getAnnotation(Command.class);
        
        // Validate method signature
        if (!isValidCommandMethod(method)) {
            Common.log("&cInvalid command method signature for @Command: " + method.getName());
            return;
        }
        
        // Create command wrapper
        CommandWrapper command = new CommandWrapper(
            annotation.name(),
            annotation.aliases(),
            annotation.executor(),
            annotation.permission(),
            annotation.description(),
            annotation.syntax(),
            annotation.async(),
            instance,
            method
        );
        
        // Register with CommandManager
        commandManager.addCommand(command);
        
        // Store for tab completion
        commandMethods.put(annotation.name().toLowerCase(), method);
        
        Common.log("&aRegistered command: /" + annotation.name());
    }

    private void registerSubCommand(@NonNull Object instance, @NonNull Method method) {
        SubCommand annotation = method.getAnnotation(SubCommand.class);
        
        // Validate method signature
        if (!isValidCommandMethod(method)) {
            Common.log("&cInvalid subcommand method signature for @SubCommand: " + method.getName());
            return;
        }
        
        // Get or create parent command
        MainCommand parentCommand = commandManager.getMainCommand(annotation.parent());
        if (parentCommand == null) {
            // Create parent if it doesn't exist
            parentCommand = commandManager.addMainCommand(annotation.parent());
        }
        
        // Create subcommand wrapper
        SubCommandWrapper subCommand = new SubCommandWrapper(
            annotation.name(),
            annotation.aliases(),
            annotation.executor(),
            annotation.permission(),
            annotation.description(),
            annotation.syntax(),
            annotation.async(),
            instance,
            method
        );
        
        // Register subcommand
        parentCommand.addSubCommand(subCommand);
        
        // Store for tab completion
        String key = annotation.parent().toLowerCase() + " " + annotation.name().toLowerCase();
        subCommandMethods.put(key, method);
        
        Common.log("&aRegistered subcommand: /" + annotation.parent() + " " + annotation.name());
    }

    private void registerTabComplete(@NonNull Object instance, @NonNull Method method) {
        TabComplete annotation = method.getAnnotation(TabComplete.class);
        
        // Validate method signature
        if (!isValidTabCompleteMethod(method)) {
            Common.log("&cInvalid tab complete method signature: " + method.getName());
            return;
        }
        
        tabCompleteMethods.put(annotation.command().toLowerCase(), method);
    }

    private boolean isValidCommandMethod(@NonNull Method method) {
        Class<?>[] paramTypes = method.getParameterTypes();
        return paramTypes.length == 1 && paramTypes[0] == CommandContext.class;
    }

    private boolean isValidTabCompleteMethod(@NonNull Method method) {
        Class<?>[] paramTypes = method.getParameterTypes();
        Class<?> returnType = method.getReturnType();
        return paramTypes.length == 1 
            && paramTypes[0] == CommandContext.class
            && (returnType == List.class || returnType == Collection.class);
    }

    /**
     * Command wrapper for annotation-based commands
     */
    private static class CommandWrapper extends ca.tweetzy.flight.command.Command {
        private final Object instance;
        private final Method method;
        private final String permission;
        private final String description;
        private final String syntax;

        public CommandWrapper(String name, String[] aliases, AllowedExecutor executor,
                             String permission, String description, String syntax,
                             boolean async, Object instance, Method method) {
            super(executor, combineNames(name, aliases));
            this.instance = instance;
            this.method = method;
            this.permission = permission;
            this.description = description;
            this.syntax = syntax;
            this.setAsync(async);
            
            method.setAccessible(true);
        }

        @Override
        protected ReturnType execute(CommandContext context) {
            try {
                Object result = method.invoke(instance, context);
                
                // If method returns ReturnType, use it
                if (result instanceof ReturnType) {
                    return (ReturnType) result;
                }
                
                // If method returns boolean, convert
                if (result instanceof Boolean) {
                    return (Boolean) result ? ReturnType.SUCCESS : ReturnType.FAIL;
                }
                
                // Default to success
                return ReturnType.SUCCESS;
            } catch (Exception e) {
                Common.log("&cError executing command: " + e.getMessage());
                e.printStackTrace();
                return ReturnType.FAIL;
            }
        }

        @Override
        protected ReturnType execute(CommandSender sender, String... args) {
            return execute(new CommandContext(sender, args, getSubCommands().get(0)));
        }

        @Override
        protected List<String> tab(CommandContext context) {
            // Tab completion handled separately
            return null;
        }

        @Override
        protected List<String> tab(CommandSender sender, String... args) {
            return tab(new CommandContext(sender, args, getSubCommands().get(0)));
        }

        @Override
        public String getPermissionNode() {
            return permission.isEmpty() ? null : permission;
        }

        @Override
        public String getSyntax() {
            return syntax.isEmpty() ? "/" + getSubCommands().get(0) : syntax;
        }

        @Override
        public String getDescription() {
            return description;
        }

        private static String[] combineNames(String name, String[] aliases) {
            String[] result = new String[aliases.length + 1];
            result[0] = name;
            System.arraycopy(aliases, 0, result, 1, aliases.length);
            return result;
        }
    }

    /**
     * SubCommand wrapper for annotation-based subcommands
     */
    private static class SubCommandWrapper extends ca.tweetzy.flight.command.Command {
        private final Object instance;
        private final Method method;
        private final String permission;
        private final String description;
        private final String syntax;

        public SubCommandWrapper(String name, String[] aliases, AllowedExecutor executor,
                                String permission, String description, String syntax,
                                boolean async, Object instance, Method method) {
            super(executor, combineNames(name, aliases));
            this.instance = instance;
            this.method = method;
            this.permission = permission;
            this.description = description;
            this.syntax = syntax;
            this.setAsync(async);
            
            method.setAccessible(true);
        }

        @Override
        protected ReturnType execute(CommandContext context) {
            try {
                Object result = method.invoke(instance, context);
                
                if (result instanceof ReturnType) {
                    return (ReturnType) result;
                }
                
                if (result instanceof Boolean) {
                    return (Boolean) result ? ReturnType.SUCCESS : ReturnType.FAIL;
                }
                
                return ReturnType.SUCCESS;
            } catch (Exception e) {
                Common.log("&cError executing subcommand: " + e.getMessage());
                e.printStackTrace();
                return ReturnType.FAIL;
            }
        }

        @Override
        protected ReturnType execute(CommandSender sender, String... args) {
            return execute(new CommandContext(sender, args, getSubCommands().get(0)));
        }

        @Override
        protected List<String> tab(CommandContext context) {
            return null;
        }

        @Override
        protected List<String> tab(CommandSender sender, String... args) {
            return tab(new CommandContext(sender, args, getSubCommands().get(0)));
        }

        @Override
        public String getPermissionNode() {
            return permission.isEmpty() ? null : permission;
        }

        @Override
        public String getSyntax() {
            return syntax.isEmpty() ? getSubCommands().get(0) : syntax;
        }

        @Override
        public String getDescription() {
            return description;
        }

        private static String[] combineNames(String name, String[] aliases) {
            String[] result = new String[aliases.length + 1];
            result[0] = name;
            System.arraycopy(aliases, 0, result, 1, aliases.length);
            return result;
        }
    }
}

