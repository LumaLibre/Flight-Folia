# Command System Usage Examples

This document shows practical examples of using the new CommandContext and ArgumentParser features.

---

## 📚 **Table of Contents**

1. [Basic CommandContext Usage](#basic-commandcontext-usage)
2. [ArgumentParser - Optional Arguments](#argumentparser---optional-arguments)
3. [ArgumentParser - Required Arguments](#argumentparser---required-arguments)
4. [Complete Real-World Example](#complete-real-world-example)
5. [Tab Completion Examples](#tab-completion-examples)

---

## 🎯 **Basic CommandContext Usage**

### Before (Old Way):
```java
@Override
protected ReturnType execute(CommandSender sender, String... args) {
    if (!(sender instanceof Player)) {
        return ReturnType.REQUIRES_PLAYER;
    }
    
    Player player = (Player) sender;
    
    if (args.length == 0) {
        player.sendMessage("§cUsage: /example <action>");
        return ReturnType.INVALID_SYNTAX;
    }
    
    String action = args[0];
    String target = args.length > 1 ? args[1] : null;
    
    // Manual string joining for remaining args
    StringBuilder remaining = new StringBuilder();
    for (int i = 2; i < args.length; i++) {
        if (i > 2) remaining.append(" ");
        remaining.append(args[i]);
    }
    
    // Use arguments...
    return ReturnType.SUCCESS;
}
```

### After (New Way with CommandContext):
```java
@Override
protected ReturnType execute(CommandContext context) {
    // Type-safe player access (already checked)
    Player player = context.getPlayer();
    
    // Get arguments with helper methods
    String action = context.getArg(0);
    String target = context.getArg(1); // null if doesn't exist
    
    // Check if argument exists
    if (action == null) {
        context.getSender().sendMessage("§cUsage: /example <action>");
        return ReturnType.INVALID_SYNTAX;
    }
    
    // Easy string joining
    String remaining = context.joinArgs(2); // Join all args from index 2
    
    // Use arguments...
    return ReturnType.SUCCESS;
}
```

**Benefits:**
- ✅ No manual type casting
- ✅ Cleaner argument access
- ✅ Built-in string joining
- ✅ Type-safe player access

---

## 🔧 **ArgumentParser - Optional Arguments**

Use optional arguments when parameters might not be provided:

```java
@Override
protected ReturnType execute(CommandContext context) {
    ArgumentParser parser = new ArgumentParser(context);
    
    // Optional arguments return null if missing/invalid
    String action = parser.getString(0);
    Player target = parser.getPlayer(1);
    Integer amount = parser.getInt(2);
    Double multiplier = parser.getDouble(3);
    Boolean enabled = parser.getBoolean(4);
    
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
    
    return ReturnType.SUCCESS;
}
```

**Supported Optional Types:**
- `getString(index)` - String
- `getInt(index)` - Integer (null if invalid)
- `getDouble(index)` - Double (null if invalid)
- `getBoolean(index)` - Boolean (null if invalid)
- `getPlayer(index)` - Player (null if not found)
- `getOfflinePlayer(index)` - OfflinePlayer
- `getUUID(index)` - UUID (null if invalid)

---

## ⚠️ **ArgumentParser - Required Arguments**

Use required arguments when parameters must be provided:

```java
@Override
protected ReturnType execute(CommandContext context) {
    ArgumentParser parser = new ArgumentParser(context);
    
    try {
        // Required arguments throw ArgumentException if missing/invalid
        String action = parser.getRequiredString(0);
        Player target = parser.getRequiredPlayer(1);
        int amount = parser.getRequiredInt(2);
        double multiplier = parser.getRequiredDouble(3);
        boolean enabled = parser.getRequiredBoolean(4);
        
        // All arguments are guaranteed to be valid here
        // No null checks needed!
        
        // Execute command logic...
        return ReturnType.SUCCESS;
        
    } catch (ArgumentParser.ArgumentException e) {
        // Handle missing/invalid argument
        context.getSender().sendMessage("§cError: " + e.getMessage());
        context.getSender().sendMessage("§cUsage: /command <action> <player> <amount> <multiplier> <enabled>");
        return ReturnType.INVALID_SYNTAX;
    }
}
```

**Supported Required Types:**
- `getRequiredString(index)` - String (throws if missing)
- `getRequiredInt(index)` - int (throws if missing/invalid)
- `getRequiredDouble(index)` - double (throws if missing/invalid)
- `getRequiredBoolean(index)` - boolean (throws if missing/invalid)
- `getRequiredPlayer(index)` - Player (throws if missing/not found)
- `getRequiredUUID(index)` - UUID (throws if missing/invalid)

---

## 🌟 **Complete Real-World Example**

Here's a complete example combining everything:

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
            player.sendMessage("§aGave " + amount + " items to " + target.getName());
            // ... actual item giving code ...
        } else {
            // Give money logic
            player.sendMessage("§aGave $" + amount + " to " + target.getName());
            // ... actual money giving code ...
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
                    .toList();
        }
        
        if (argCount == 2) {
            // Suggest player names
            String typed = context.getArg(1, "").toLowerCase();
            return context.getSender().getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(typed))
                    .toList();
        }
        
        if (argCount == 3) {
            // Suggest common amounts
            return List.of("1", "5", "10", "64", "100");
        }
        
        return Collections.emptyList();
    }
    
    @Override
    public String getPermissionNode() {
        return "flight.give";
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

---

## 📝 **Tab Completion Examples**

### Simple Tab Completion:
```java
@Override
protected List<String> tab(CommandContext context) {
    int argCount = context.getArgCount();
    
    if (argCount == 0) {
        return List.of("option1", "option2", "option3");
    }
    
    if (argCount == 1) {
        // Filter based on what they've typed
        String typed = context.getArg(0, "").toLowerCase();
        return List.of("option1", "option2", "option3").stream()
                .filter(s -> s.startsWith(typed))
                .toList();
    }
    
    return Collections.emptyList();
}
```

### Advanced Tab Completion with Player Suggestions:
```java
@Override
protected List<String> tab(CommandContext context) {
    int argCount = context.getArgCount();
    
    if (argCount == 1) {
        // Suggest player names
        String typed = context.getArg(0, "").toLowerCase();
        return context.getSender().getServer().getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(typed))
                .toList();
    }
    
    return Collections.emptyList();
}
```

---

## 🎓 **Key Takeaways**

1. **CommandContext** provides:
   - Type-safe access to sender and arguments
   - Convenient helper methods
   - Cleaner, more readable code

2. **ArgumentParser** provides:
   - Type-safe argument parsing
   - Automatic validation
   - Error collection
   - Required vs optional variants

3. **Best Practices:**
   - Use `CommandContext` for all new commands
   - Use `ArgumentParser` for complex argument parsing
   - Use required arguments when parameters are mandatory
   - Use optional arguments with null checks when parameters are optional
   - Always check `parser.hasErrors()` when using optional arguments

