# Brigadier Support

Brigadier is Minecraft's modern command framework introduced in version 1.13. Flight provides support for Brigadier commands, offering better tab completion performance and native Minecraft command integration.

## Overview

Brigadier support provides:

- **Better tab completion** - More efficient and responsive
- **Native integration** - Works with Minecraft's command system
- **Improved suggestions** - Better command suggestions
- **Version requirement** - Only works on Minecraft 1.13+

## Checking Availability

Before using Brigadier, check if it's available:

```java
if (!BrigadierCommandManager.isAvailable()) {
    plugin.getLogger().warning("Brigadier requires Minecraft 1.13+");
    return;
}
```

## Basic Usage

### 1. Create BrigadierCommandManager

```java
BrigadierCommandManager brigadierManager = new BrigadierCommandManager(plugin);
```

### 2. Create a Command

Use `createBrigadierCommand` to create a command:

```java
org.bukkit.command.Command command = brigadierManager.createBrigadierCommand(
    "mycommand",                                    // Command name
    "My command description",                       // Description
    "myplugin.mycommand",                           // Permission
    List.of("mc", "mycmd"),                        // Aliases
    (sender, label, args) -> {                      // Executor
        sender.sendMessage("§aCommand executed!");
        return true;
    },
    (sender, alias, args) -> {                     // Tab completer
        if (args.length == 1) {
            return List.of("option1", "option2");
        }
        return new ArrayList<>();
    }
);
```

### 3. Register the Command

```java
brigadierManager.registerBrigadierCommand("mycommand", command);
```

## Complete Example

Here's a complete example:

```java
public class BrigadierCommands {
    private final JavaPlugin plugin;
    private final BrigadierCommandManager brigadierManager;

    public BrigadierCommands(JavaPlugin plugin) {
        this.plugin = plugin;
        this.brigadierManager = new BrigadierCommandManager(plugin);
    }

    public void register() {
        // Check if Brigadier is available
        if (!BrigadierCommandManager.isAvailable()) {
            plugin.getLogger().warning("Brigadier requires Minecraft 1.13+");
            return;
        }

        // Create and register a command
        org.bukkit.command.Command command = brigadierManager.createBrigadierCommand(
            "teleport",
            "Teleport to a location",
            "myplugin.teleport",
            List.of("tp"),
            (sender, label, args) -> {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cOnly players can use this command!");
                    return false;
                }

                Player player = (Player) sender;

                if (args.length == 0) {
                    player.sendMessage("§cUsage: /teleport <x> <y> <z>");
                    return false;
                }

                try {
                    double x = Double.parseDouble(args[0]);
                    double y = Double.parseDouble(args[1]);
                    double z = Double.parseDouble(args[2]);

                    Location location = new Location(player.getWorld(), x, y, z);
                    player.teleport(location);
                    player.sendMessage("§aTeleported to " + x + ", " + y + ", " + z);
                    return true;
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    player.sendMessage("§cInvalid coordinates!");
                    return false;
                }
            },
            (sender, alias, args) -> {
                if (args.length <= 3) {
                    // Suggest coordinates based on player location
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        Location loc = player.getLocation();

                        if (args.length == 1) {
                            return List.of(String.valueOf(loc.getBlockX()));
                        } else if (args.length == 2) {
                            return List.of(String.valueOf(loc.getBlockY()));
                        } else if (args.length == 3) {
                            return List.of(String.valueOf(loc.getBlockZ()));
                        }
                    }
                }
                return new ArrayList<>();
            }
        );

        brigadierManager.registerBrigadierCommand("teleport", command);
    }
}
```

## Command Executor

The executor is a functional interface that receives:

- `CommandSender sender` - The command sender
- `String commandLabel` - The command label used
- `String[] args` - Command arguments

It should return `true` if the command succeeded, `false` otherwise.

```java
BrigadierCommandManager.BrigadierCommandExecutor executor = (sender, label, args) -> {
    // Command logic
    return true; // Success
};
```

## Tab Completer

The tab completer receives the same parameters and returns a `List<String>` of suggestions:

```java
BrigadierCommandManager.BrigadierTabCompleter completer = (sender, alias, args) -> {
    if (args.length == 1) {
        // Suggest options
        return List.of("option1", "option2", "option3");
    }
    return new ArrayList<>();
};
```

## Unregistering Commands

You can unregister Brigadier commands:

```java
brigadierManager.unregisterBrigadierCommand("mycommand");
```

## Benefits of Brigadier

### 1. Better Performance

Brigadier commands have better tab completion performance compared to traditional commands, especially with many suggestions.

### 2. Native Integration

Brigadier commands integrate natively with Minecraft's command system, providing a more consistent experience.

### 3. Better Suggestions

Minecraft's command system provides better suggestions and autocomplete for Brigadier commands.

## Version Compatibility

**Important:** Brigadier is only available on Minecraft 1.13+. Always check availability before using:

```java
if (!BrigadierCommandManager.isAvailable()) {
    // Fallback to traditional commands
    commandManager.addCommand(new TraditionalCommand());
} else {
    // Use Brigadier
    brigadierManager.registerBrigadierCommand("command", command);
}
```

## Migration from Traditional Commands

If you have traditional commands and want to migrate to Brigadier:

**Before (Traditional):**

```java
public class MyCommand extends Command {
    @Override
    protected ReturnType execute(CommandContext context) {
        Player player = context.getPlayer();
        player.sendMessage("Hello!");
        return ReturnType.SUCCESS;
    }
}
```

**After (Brigadier):**

```java
org.bukkit.command.Command command = brigadierManager.createBrigadierCommand(
    "mycommand",
    "My command",
    "myplugin.mycommand",
    new ArrayList<>(),
    (sender, label, args) -> {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;
        player.sendMessage("Hello!");
        return true;
    },
    (sender, alias, args) -> new ArrayList<>()
);
```

## Best Practices

1. **Always check availability** before using Brigadier
2. **Provide fallbacks** for older server versions
3. **Use descriptive permissions** and descriptions
4. **Implement proper tab completion** for better UX
5. **Handle errors gracefully** in the executor

## Limitations

- **Version requirement** - Only works on 1.13+
- **No CommandContext** - Uses traditional Bukkit command interface
- **No ArgumentParser** - Manual argument parsing required

## See Also

- [Basic Commands](basic.md) - Traditional command creation
- [Annotation-Based Commands](annotations.md) - Annotation-based commands
- [Tab Completion](tab-completion.md) - Tab completion patterns
