# Basic Commands

The traditional way to create commands in Flight is by extending the `Command` class. This approach gives you full control over command execution and is the foundation of Flight's command system.

## Creating Your First Command

To create a command, extend the `Command` class and implement the required methods:

```java
public class HelloCommand extends Command {

    public HelloCommand() {
        super(AllowedExecutor.PLAYER, "hello");
    }

    @Override
    protected ReturnType execute(CommandContext context) {
        Player player = context.getPlayer();
        player.sendMessage("Hello, " + player.getName() + "!");
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> tab(CommandContext context) {
        return Collections.emptyList();
    }

    @Override
    public String getPermissionNode() {
        return "myplugin.hello";
    }

    @Override
    public String getSyntax() {
        return "/hello";
    }

    @Override
    public String getDescription() {
        return "Say hello!";
    }
}
```

## Constructor

The constructor takes two parameters:

1. **AllowedExecutor** - Who can execute this command:

   - `AllowedExecutor.PLAYER` - Only players
   - `AllowedExecutor.CONSOLE` - Only console
   - `AllowedExecutor.BOTH` - Both players and console

2. **Command names** - The command name(s) and aliases:
   ```java
   super(AllowedExecutor.PLAYER, "hello", "hi", "greet");
   ```

## Execute Method

The `execute` method is called when the command is run. It receives a `CommandContext` that provides access to:

- The command sender
- Command arguments
- Helper methods for argument access

```java
@Override
protected ReturnType execute(CommandContext context) {
    // Get the sender
    CommandSender sender = context.getSender();

    // Get arguments
    String firstArg = context.getArg(0); // First argument (or null)
    String secondArg = context.getArg(1); // Second argument (or null)

    // Check if argument exists
    if (!context.hasArg(0)) {
        sender.sendMessage("Usage: /hello <name>");
        return ReturnType.INVALID_SYNTAX;
    }

    // Join remaining arguments
    String message = context.joinArgs(1); // Join all args from index 1

    return ReturnType.SUCCESS;
}
```

## ReturnType

The `execute` method must return a `ReturnType`:

- `ReturnType.SUCCESS` - Command executed successfully
- `ReturnType.FAIL` - Command failed (generic failure)
- `ReturnType.INVALID_SYNTAX` - Invalid syntax (shows syntax help message)
- `ReturnType.REQUIRES_PLAYER` - Command requires a player (shows player-only message)
- `ReturnType.REQUIRES_CONSOLE` - Command requires console (shows console-only message)

## Tab Completion

The `tab` method provides tab completion suggestions:

```java
@Override
protected List<String> tab(CommandContext context) {
    int argCount = context.getArgCount();

    if (argCount == 0) {
        return List.of("option1", "option2", "option3");
    }

    if (argCount == 1) {
        String typed = context.getArg(0, "").toLowerCase();
        return List.of("option1", "option2", "option3").stream()
                .filter(s -> s.startsWith(typed))
                .collect(Collectors.toList());
    }

    return Collections.emptyList();
}
```

## Required Methods

You must implement these three methods:

### getPermissionNode()

Returns the permission node required to use this command. Return `null` for no permission requirement.

```java
@Override
public String getPermissionNode() {
    return "myplugin.hello";
}
```

### getSyntax()

Returns the command syntax for help messages:

```java
@Override
public String getSyntax() {
    return "/hello [name]";
}
```

### getDescription()

Returns a description of what the command does:

```java
@Override
public String getDescription() {
    return "Say hello to someone";
}
```

## Complete Example

Here's a complete example of a command with multiple arguments and tab completion:

```java
public class GiveItemCommand extends Command {

    public GiveItemCommand() {
        super(AllowedExecutor.PLAYER, "giveitem", "gi");
    }

    @Override
    protected ReturnType execute(CommandContext context) {
        Player player = context.getPlayer();

        // Check for required arguments
        if (!context.hasArg(0) || !context.hasArg(1)) {
            player.sendMessage("Usage: /giveitem <player> <item> [amount]");
            return ReturnType.INVALID_SYNTAX;
        }

        // Get target player
        Player target = Bukkit.getPlayer(context.getArg(0));
        if (target == null) {
            player.sendMessage("Player not found: " + context.getArg(0));
            return ReturnType.FAIL;
        }

        // Get item material
        Material material = Material.matchMaterial(context.getArg(1));
        if (material == null) {
            player.sendMessage("Invalid material: " + context.getArg(1));
            return ReturnType.FAIL;
        }

        // Get amount (optional, default 1)
        int amount = 1;
        if (context.hasArg(2)) {
            try {
                amount = Integer.parseInt(context.getArg(2));
                if (amount <= 0 || amount > 64) {
                    player.sendMessage("Amount must be between 1 and 64");
                    return ReturnType.FAIL;
                }
            } catch (NumberFormatException e) {
                player.sendMessage("Invalid amount: " + context.getArg(2));
                return ReturnType.FAIL;
            }
        }

        // Give the item
        ItemStack item = new ItemStack(material, amount);
        target.getInventory().addItem(item);

        player.sendMessage("Gave " + amount + "x " + material.name() + " to " + target.getName());
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> tab(CommandContext context) {
        int argCount = context.getArgCount();

        if (argCount == 1) {
            // Suggest player names
            String typed = context.getArg(0, "").toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(typed))
                    .collect(Collectors.toList());
        }

        if (argCount == 2) {
            // Suggest common materials
            String typed = context.getArg(1, "").toUpperCase();
            return Arrays.stream(Material.values())
                    .filter(m -> m.isItem() && !m.isAir())
                    .map(Material::name)
                    .filter(name -> name.startsWith(typed))
                    .limit(20)
                    .collect(Collectors.toList());
        }

        if (argCount == 3) {
            // Suggest common amounts
            return List.of("1", "5", "10", "16", "32", "64");
        }

        return Collections.emptyList();
    }

    @Override
    public String getPermissionNode() {
        return "myplugin.giveitem";
    }

    @Override
    public String getSyntax() {
        return "/giveitem <player> <item> [amount]";
    }

    @Override
    public String getDescription() {
        return "Give an item to a player";
    }
}
```

## Registering Commands

Register your commands with the `CommandManager`:

```java
public class MyPlugin extends FlightPlugin {
    private CommandManager commandManager;

    @Override
    protected void onFlight() {
        commandManager = new CommandManager(this);
        commandManager.addCommand(new HelloCommand());
        commandManager.addCommand(new GiveItemCommand());
    }
}
```

## Async Commands

You can mark commands to run asynchronously:

```java
public class AsyncCommand extends Command {
    public AsyncCommand() {
        super(AllowedExecutor.BOTH, "async");
        setAsync(true); // Enable async execution
    }

    @Override
    protected ReturnType execute(CommandContext context) {
        // This will run on an async thread
        // Be careful with Bukkit API calls!
        return ReturnType.SUCCESS;
    }

    // ... other methods
}
```

**Note:** When using async commands, be careful with Bukkit API calls. Most Bukkit API is not thread-safe and should only be called from the main thread. Use `FlightPlugin#getScheduler` to execute code on the main thread.

## Best Practices

1. **Always check for required arguments** before using them
2. **Validate input** (player names, numbers, etc.)
3. **Provide helpful error messages** to users
4. **Use CommandContext** instead of the legacy method signature
5. **Return appropriate ReturnType** values
6. **Implement tab completion** for better user experience
7. **Use descriptive permission nodes** following a consistent pattern

## See Also

- [CommandContext](command-context.md) - Learn about CommandContext
- [ArgumentParser](argument-parser.md) - Type-safe argument parsing
- [Tab Completion](tab-completion.md) - Advanced tab completion patterns
