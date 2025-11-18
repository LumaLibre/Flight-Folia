# ArgumentParser

`ArgumentParser` provides type-safe argument parsing with automatic validation. It eliminates the need for manual string parsing and type conversion, making command argument handling much cleaner and safer.

## Overview

`ArgumentParser` supports two types of argument parsing:

- **Optional Arguments** - Return `null` if missing or invalid
- **Required Arguments** - Throw `ArgumentException` if missing or invalid

## Basic Usage

Create an `ArgumentParser` from a `CommandContext`:

```java
@Override
protected ReturnType execute(CommandContext context) {
    ArgumentParser parser = new ArgumentParser(context);
    
    // Parse arguments
    String action = parser.getString(0);
    Player target = parser.getPlayer(1);
    Integer amount = parser.getInt(2);
    
    // Use parsed arguments
    // ...
    
    return ReturnType.SUCCESS;
}
```

## Optional Arguments

Optional arguments return `null` if missing or invalid. You should check for `null` before using them.

### Supported Optional Types

```java
ArgumentParser parser = new ArgumentParser(context);

// String (always returns String or null)
String name = parser.getString(0);

// Integer (returns Integer or null)
Integer amount = parser.getInt(1);

// Double (returns Double or null)
Double price = parser.getDouble(2);

// Boolean (returns Boolean or null)
// Accepts: true/false, 1/0, yes/no, on/off
Boolean enabled = parser.getBoolean(3);

// Player (returns Player or null if not found)
Player target = parser.getPlayer(4);

// OfflinePlayer (returns OfflinePlayer or null)
OfflinePlayer offline = parser.getOfflinePlayer(5);

// UUID (returns UUID or null if invalid)
UUID id = parser.getUUID(6);
```

### Error Handling

When using optional arguments, check for parsing errors:

```java
ArgumentParser parser = new ArgumentParser(context);

String action = parser.getString(0);
Integer amount = parser.getInt(1);
Player target = parser.getPlayer(2);

// Check for parsing errors
if (parser.hasErrors()) {
    parser.getErrors().forEach(error -> 
        context.getSender().sendMessage("§cError: " + error)
    );
    return ReturnType.INVALID_SYNTAX;
}

// Validate required arguments manually
if (action == null) {
    context.getSender().sendMessage("§cUsage: /command <action> [player] [amount]");
    return ReturnType.INVALID_SYNTAX;
}

// Use parsed arguments (check for null)
if (target != null) {
    // Do something with target player
}

int finalAmount = amount != null ? amount : 1; // Default to 1
```

## Required Arguments

Required arguments throw `ArgumentException` if missing or invalid. Use try-catch to handle errors.

### Supported Required Types

```java
ArgumentParser parser = new ArgumentParser(context);

try {
    // Required arguments throw ArgumentException if missing/invalid
    String action = parser.getRequiredString(0);
    Player target = parser.getRequiredPlayer(1);
    int amount = parser.getRequiredInt(2);
    double price = parser.getRequiredDouble(3);
    boolean enabled = parser.getRequiredBoolean(4);
    UUID id = parser.getRequiredUUID(5);
    
    // All arguments are guaranteed to be valid here
    // No null checks needed!
    
    // Execute command logic...
    return ReturnType.SUCCESS;
    
} catch (ArgumentParser.ArgumentException e) {
    // Handle missing/invalid argument
    context.getSender().sendMessage("§cError: " + e.getMessage());
    context.getSender().sendMessage("§cUsage: /command <action> <player> <amount> <price> <enabled> <uuid>");
    return ReturnType.INVALID_SYNTAX;
}
```

## Complete Example

Here's a complete example combining optional and required arguments:

```java
public class GiveCommand extends Command {
    
    public GiveCommand() {
        super(AllowedExecutor.PLAYER, "give");
    }

    @Override
    protected ReturnType execute(CommandContext context) {
        ArgumentParser parser = new ArgumentParser(context);
        
        // Parse and validate action (required)
        String action;
        try {
            action = parser.getRequiredString(0);
        } catch (ArgumentParser.ArgumentException e) {
            context.getSender().sendMessage("§c" + e.getMessage());
            return ReturnType.INVALID_SYNTAX;
        }
        
        // Validate action value
        if (!action.equalsIgnoreCase("item") && !action.equalsIgnoreCase("money")) {
            context.getSender().sendMessage("§cInvalid action. Use 'item' or 'money'");
            return ReturnType.INVALID_SYNTAX;
        }
        
        // Parse target player (required)
        Player target;
        try {
            target = parser.getRequiredPlayer(1);
        } catch (ArgumentParser.ArgumentException e) {
            context.getSender().sendMessage("§c" + e.getMessage());
            return ReturnType.INVALID_SYNTAX;
        }
        
        // Parse amount (optional, default to 1)
        int amount = parser.getInt(2) != null ? parser.getInt(2) : 1;
        if (amount <= 0) {
            context.getSender().sendMessage("§cAmount must be positive");
            return ReturnType.INVALID_SYNTAX;
        }
        
        // Check for any other parsing errors
        if (parser.hasErrors()) {
            parser.getErrors().forEach(error -> 
                context.getSender().sendMessage("§c" + error)
            );
            return ReturnType.INVALID_SYNTAX;
        }
        
        // Execute the command logic
        Player player = context.getPlayer();
        
        if (action.equalsIgnoreCase("item")) {
            // Give item logic
            ItemStack item = new ItemStack(Material.DIAMOND, amount);
            target.getInventory().addItem(item);
            player.sendMessage("§aGave " + amount + " diamonds to " + target.getName());
        } else {
            // Give money logic (example)
            player.sendMessage("§aGave $" + amount + " to " + target.getName());
        }
        
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> tab(CommandContext context) {
        int argCount = context.getArgCount();
        
        if (argCount == 0) {
            return List.of("item", "money");
        }
        
        if (argCount == 1) {
            String typed = context.getArg(0, "").toLowerCase();
            return List.of("item", "money").stream()
                    .filter(s -> s.startsWith(typed))
                    .collect(Collectors.toList());
        }
        
        if (argCount == 2) {
            // Suggest player names
            String typed = context.getArg(1, "").toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(typed))
                    .collect(Collectors.toList());
        }
        
        if (argCount == 3) {
            // Suggest common amounts
            return List.of("1", "5", "10", "64", "100");
        }
        
        return Collections.emptyList();
    }

    @Override
    public String getPermissionNode() {
        return "myplugin.give";
    }

    @Override
    public String getSyntax() {
        return "/give <item|money> <player> [amount]";
    }

    @Override
    public String getDescription() {
        return "Give items or money to a player";
    }
}
```

## Boolean Parsing

`ArgumentParser` supports multiple boolean formats:

```java
// All of these return true:
parser.getBoolean(0); // "true", "1", "yes", "on"

// All of these return false:
parser.getBoolean(0); // "false", "0", "no", "off"

// Case-insensitive
parser.getBoolean(0); // "TRUE", "True", "YES", "Yes" all work
```

## Player Parsing

### Online Players

```java
// Returns Player if online, null otherwise
Player player = parser.getPlayer(0);

// Required version throws if not found
Player player = parser.getRequiredPlayer(0);
```

### Offline Players

```java
// Returns OfflinePlayer (works for offline players too)
OfflinePlayer offline = parser.getOfflinePlayer(0);
```

## UUID Parsing

```java
// Optional UUID
UUID id = parser.getUUID(0); // null if invalid

// Required UUID
UUID id = parser.getRequiredUUID(0); // throws if invalid
```

## Best Practices

1. **Use required arguments for mandatory parameters** - Makes the code cleaner and errors more explicit
2. **Use optional arguments with defaults** - For optional parameters
3. **Always check `hasErrors()`** when using optional arguments
4. **Provide helpful error messages** when catching `ArgumentException`
5. **Validate parsed values** - Even if parsing succeeds, validate business logic (e.g., amount > 0)

## Comparison: Optional vs Required

### Optional Arguments

**Pros:**
- Flexible - can handle missing arguments gracefully
- Good for optional parameters

**Cons:**
- Need to check for null
- Need to check `hasErrors()` separately
- More verbose code

### Required Arguments

**Pros:**
- Cleaner code - no null checks needed
- Explicit error handling with try-catch
- Guaranteed valid values after parsing

**Cons:**
- Less flexible - must provide all arguments
- Need try-catch block

## See Also

- [CommandContext](command-context.md) - Learn about CommandContext
- [Basic Commands](basic.md) - Basic command creation
- [Tab Completion](tab-completion.md) - Tab completion implementation

