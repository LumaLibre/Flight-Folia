# CommandContext

`CommandContext` provides a modern, type-safe way to access command execution data. It eliminates the need for manual type casting and provides convenient helper methods for working with command arguments.

## What is CommandContext?

`CommandContext` is a wrapper around the command execution data that provides:

- Type-safe access to the sender
- Convenient argument access methods
- Helper methods for string operations
- No need for manual type casting

## Basic Usage

Instead of the old method signature:

```java
protected ReturnType execute(CommandSender sender, String... args)
```

Use the modern approach:

```java
protected ReturnType execute(CommandContext context)
```

## Accessing the Sender

```java
@Override
protected ReturnType execute(CommandContext context) {
    // Get the sender
    CommandSender sender = context.getSender();

    // Check if sender is a player
    if (context.isPlayer()) {
        Player player = context.getPlayer(); // Type-safe, no casting needed!
    }

    // Check if sender is console
    if (context.isConsole()) {
        // Handle console
    }

    return ReturnType.SUCCESS;
}
```

## Accessing Arguments

### Basic Argument Access

```java
// Get argument at index (returns null if doesn't exist)
String firstArg = context.getArg(0);
String secondArg = context.getArg(1);

// Get argument with default value
String name = context.getArg(0, "Unknown");

// Check if argument exists
if (context.hasArg(0)) {
    // Argument exists
}

// Get number of arguments
int argCount = context.getArgCount();
```

### Getting Multiple Arguments

```java
// Get all arguments as a list
List<String> allArgs = context.getArgs();

// Get arguments from start index to end
String[] remainingArgs = context.getArgs(1); // All args from index 1

// Get arguments from start to end index
String[] rangeArgs = context.getArgs(1, 3); // Args from index 1 to 3 (exclusive)
```

### Joining Arguments

A common task is joining multiple arguments into a single string (e.g., for messages):

```java
// Join all arguments from index 1 with spaces
String message = context.joinArgs(1);
// "/command Hello World" -> "Hello World"

// Join arguments from start to end index
String partial = context.joinArgs(1, 3);
// "/command Hello World Test" -> "Hello World"
```

## Complete Example

Here's a complete example showing CommandContext usage:

```java
public class MessageCommand extends Command {

    public MessageCommand() {
        super(AllowedExecutor.PLAYER, "message", "msg", "tell");
    }

    @Override
    protected ReturnType execute(CommandContext context) {
        Player player = context.getPlayer(); // Type-safe player access

        // Check for required arguments
        if (!context.hasArg(0)) {
            player.sendMessage("Usage: /message <player> <message>");
            return ReturnType.INVALID_SYNTAX;
        }

        // Get target player
        String targetName = context.getArg(0);
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            player.sendMessage("Player not found: " + targetName);
            return ReturnType.FAIL;
        }

        // Get message (join all remaining arguments)
        String message = context.joinArgs(1);
        if (message.isEmpty()) {
            player.sendMessage("You must provide a message!");
            return ReturnType.INVALID_SYNTAX;
        }

        // Send the message
        target.sendMessage("§7[" + player.getName() + " -> You] §f" + message);
        player.sendMessage("§7[You -> " + target.getName() + "] §f" + message);

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

        return Collections.emptyList();
    }

    @Override
    public String getPermissionNode() {
        return "myplugin.message";
    }

    @Override
    public String getSyntax() {
        return "/message <player> <message>";
    }

    @Override
    public String getDescription() {
        return "Send a private message to a player";
    }
}
```

## CommandContext Methods

### Sender Methods

```java
CommandSender getSender()        // Get the command sender
Player getPlayer()               // Get player (null if not a player)
boolean isPlayer()               // Check if sender is a player
boolean isConsole()              // Check if sender is console
```

### Argument Methods

```java
String getArg(int index)                    // Get argument at index (null if doesn't exist)
String getArg(int index, String defaultValue) // Get argument with default
boolean hasArg(int index)                   // Check if argument exists
int getArgCount()                           // Get number of arguments
List<String> getArgs()                      // Get all arguments as list
String[] getArgs(int start)                 // Get args from start to end
String[] getArgs(int start, int end)        // Get args from start to end (exclusive)
String joinArgs(int start)                  // Join args from start with spaces
String joinArgs(int start, int end)         // Join args from start to end
```

### Other Methods

```java
String getLabel()              // Get the command label used
String getFullCommand()       // Get the full command string
```

## Benefits of CommandContext

### 1. Type Safety

**Before:**

```java
if (!(sender instanceof Player)) {
    return ReturnType.REQUIRES_PLAYER;
}
Player player = (Player) sender; // Manual casting
```

**After:**

```java
Player player = context.getPlayer(); // Type-safe, already checked
```

### 2. Cleaner Argument Access

**Before:**

```java
if (args.length == 0) {
    return ReturnType.INVALID_SYNTAX;
}
String firstArg = args[0];
String secondArg = args.length > 1 ? args[1] : null;
```

**After:**

```java
String firstArg = context.getArg(0);
String secondArg = context.getArg(1); // null if doesn't exist
```

### 3. Easy String Joining

**Before:**

```java
StringBuilder message = new StringBuilder();
for (int i = 1; i < args.length; i++) {
    if (i > 1) message.append(" ");
    message.append(args[i]);
}
String result = message.toString();
```

**After:**

```java
String message = context.joinArgs(1); // Done!
```

## Migration Guide

If you have existing commands using the old method signature, migration is easy:

**Old:**

```java
@Override
protected ReturnType execute(CommandSender sender, String... args) {
    if (!(sender instanceof Player)) {
        return ReturnType.REQUIRES_PLAYER;
    }
    Player player = (Player) sender;
    if (args.length == 0) {
        return ReturnType.INVALID_SYNTAX;
    }
    String arg = args[0];
    // ...
}
```

**New:**

```java
@Override
protected ReturnType execute(CommandContext context) {
    Player player = context.getPlayer(); // Already type-checked
    String arg = context.getArg(0);
    if (arg == null) {
        return ReturnType.INVALID_SYNTAX;
    }
    // ...
}
```

## See Also

- [Basic Commands](basic.md) - Learn the basics of command creation
- [ArgumentParser](argument-parser.md) - Type-safe argument parsing
- [Tab Completion](tab-completion.md) - Using CommandContext in tab completion
