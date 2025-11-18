# Annotation-Based Commands

Annotation-based commands allow you to register commands using annotations instead of extending the `Command` class. This approach reduces boilerplate code and makes it easy to organize commands in classes.

## Overview

Annotation-based commands provide:

- **Less boilerplate** - No need to extend Command class
- **Cleaner code** - Commands are just methods
- **Easy organization** - Group related commands in classes
- **Works with existing system** - Uses the same CommandManager

## Getting Started

### 1. Create a Command Class

Create a class to hold your annotated commands:

```java
public class MyCommands {
    private final JavaPlugin plugin;
    private final CommandManager commandManager;

    public MyCommands(JavaPlugin plugin, CommandManager commandManager) {
        this.plugin = plugin;
        this.commandManager = commandManager;
    }

    public void register() {
        // Register all annotated commands from this class
        AnnotationCommandHandler handler = new AnnotationCommandHandler(plugin, commandManager);
        handler.registerCommands(this);
    }
}
```

### 2. Annotate Your Methods

Use `@Command` to mark methods as commands:

```java
@Command(
    name = "hello",
    aliases = {"hi", "greet"},
    executor = AllowedExecutor.PLAYER,
    permission = "myplugin.hello",
    description = "Say hello",
    syntax = "/hello [name]"
)
public ReturnType helloCommand(CommandContext context) {
    Player player = context.getPlayer();
    String name = context.getArg(0);
    
    if (name != null) {
        player.sendMessage("Hello, " + name + "!");
    } else {
        player.sendMessage("Hello, " + player.getName() + "!");
    }
    
    return ReturnType.SUCCESS;
}
```

### 3. Register Commands

Register your command class:

```java
MyCommands commands = new MyCommands(plugin, commandManager);
commands.register();
```

## @Command Annotation

The `@Command` annotation marks a method as a command handler.

### Parameters

```java
@Command(
    name = "commandname",              // Required: Command name
    aliases = {"alias1", "alias2"},   // Optional: Command aliases
    executor = AllowedExecutor.PLAYER, // Optional: Who can execute (default: BOTH)
    permission = "myplugin.command",    // Optional: Permission node (default: "")
    description = "Command description", // Optional: Description (default: "")
    syntax = "/command <args>",         // Optional: Syntax (default: "")
    async = false                      // Optional: Run async (default: false)
)
```

### Method Signature

Command methods must:
- Accept `CommandContext` as the only parameter
- Return `ReturnType` or `boolean`

```java
@Command(name = "example")
public ReturnType exampleCommand(CommandContext context) {
    // Command logic
    return ReturnType.SUCCESS;
}

// Or return boolean
@Command(name = "example")
public boolean exampleCommand(CommandContext context) {
    // Command logic
    return true; // true = SUCCESS, false = FAIL
}
```

## @SubCommand Annotation

Use `@SubCommand` to create subcommands:

```java
@SubCommand(
    parent = "shop",                    // Required: Parent command name
    name = "buy",                       // Required: Subcommand name
    aliases = {"purchase"},            // Optional: Aliases
    executor = AllowedExecutor.PLAYER, // Optional: Executor
    permission = "myplugin.shop.buy",  // Optional: Permission
    description = "Buy items from shop" // Optional: Description
)
public ReturnType shopBuy(CommandContext context) {
    Player player = context.getPlayer();
    // Subcommand logic
    return ReturnType.SUCCESS;
}
```

The parent command will be created automatically if it doesn't exist.

## @TabComplete Annotation

Use `@TabComplete` to provide tab completion:

```java
@TabComplete(command = "hello")
public List<String> helloTabComplete(CommandContext context) {
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
```

Tab complete methods must:
- Accept `CommandContext` as the only parameter
- Return `List<String>`

## Complete Example

Here's a complete example with multiple commands:

```java
public class ShopCommands {
    private final JavaPlugin plugin;
    private final CommandManager commandManager;

    public ShopCommands(JavaPlugin plugin, CommandManager commandManager) {
        this.plugin = plugin;
        this.commandManager = commandManager;
    }

    public void register() {
        AnnotationCommandHandler handler = new AnnotationCommandHandler(plugin, commandManager);
        handler.registerCommands(this);
    }

    // Main shop command
    @Command(
        name = "shop",
        executor = AllowedExecutor.PLAYER,
        description = "Shop commands"
    )
    public ReturnType shopCommand(CommandContext context) {
        Player player = context.getPlayer();
        player.sendMessage("§aShop Commands:");
        player.sendMessage("§e/shop buy §7- Buy items");
        player.sendMessage("§e/shop sell §7- Sell items");
        return ReturnType.SUCCESS;
    }

    // Buy subcommand
    @SubCommand(
        parent = "shop",
        name = "buy",
        executor = AllowedExecutor.PLAYER,
        permission = "myplugin.shop.buy",
        description = "Buy items from shop"
    )
    public ReturnType shopBuy(CommandContext context) {
        Player player = context.getPlayer();
        
        ArgumentParser parser = new ArgumentParser(context);
        String itemName = parser.getString(0);
        
        if (itemName == null) {
            player.sendMessage("§cUsage: /shop buy <item>");
            return ReturnType.INVALID_SYNTAX;
        }
        
        // Buy logic here
        player.sendMessage("§aBought " + itemName);
        return ReturnType.SUCCESS;
    }

    // Sell subcommand
    @SubCommand(
        parent = "shop",
        name = "sell",
        executor = AllowedExecutor.PLAYER,
        permission = "myplugin.shop.sell",
        description = "Sell items to shop"
    )
    public ReturnType shopSell(CommandContext context) {
        Player player = context.getPlayer();
        
        ArgumentParser parser = new ArgumentParser(context);
        String itemName = parser.getString(0);
        
        if (itemName == null) {
            player.sendMessage("§cUsage: /shop sell <item>");
            return ReturnType.INVALID_SYNTAX;
        }
        
        // Sell logic here
        player.sendMessage("§aSold " + itemName);
        return ReturnType.SUCCESS;
    }

    // Tab completion for shop command
    @TabComplete(command = "shop")
    public List<String> shopTabComplete(CommandContext context) {
        int argCount = context.getArgCount();
        
        if (argCount == 1) {
            String typed = context.getArg(0, "").toLowerCase();
            return List.of("buy", "sell").stream()
                    .filter(s -> s.startsWith(typed))
                    .collect(Collectors.toList());
        }
        
        return Collections.emptyList();
    }

    // Tab completion for shop buy
    @TabComplete(command = "shop buy")
    public List<String> shopBuyTabComplete(CommandContext context) {
        int argCount = context.getArgCount();
        
        if (argCount == 1) {
            // Suggest available items
            String typed = context.getArg(0, "").toUpperCase();
            return Arrays.stream(Material.values())
                    .filter(Material::isItem)
                    .map(Material::name)
                    .filter(name -> name.startsWith(typed))
                    .limit(20)
                    .collect(Collectors.toList());
        }
        
        return Collections.emptyList();
    }
}
```

## Async Commands

You can mark commands to run asynchronously:

```java
@Command(
    name = "async",
    async = true  // Run asynchronously
)
public ReturnType asyncCommand(CommandContext context) {
    // This runs on an async thread
    // Be careful with Bukkit API calls!
    return ReturnType.SUCCESS;
}
```

**Note:** When using async commands, be careful with Bukkit API calls. Most Bukkit API is not thread-safe.

## Benefits

### 1. Less Boilerplate

**Traditional:**
```java
public class HelloCommand extends Command {
    public HelloCommand() {
        super(AllowedExecutor.PLAYER, "hello");
    }
    
    @Override
    protected ReturnType execute(CommandContext context) {
        // Logic
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
        return "Say hello";
    }
}
```

**Annotation-Based:**
```java
@Command(
    name = "hello",
    executor = AllowedExecutor.PLAYER,
    permission = "myplugin.hello",
    syntax = "/hello",
    description = "Say hello"
)
public ReturnType helloCommand(CommandContext context) {
    // Logic
    return ReturnType.SUCCESS;
}
```

### 2. Easy Organization

Group related commands in a single class:

```java
public class AdminCommands {
    // All admin commands in one place
}

public class PlayerCommands {
    // All player commands in one place
}
```

## When to Use Annotations

### Use Annotations When:
- You have many simple commands
- You want cleaner, more organized code
- You prefer declarative style
- You want to group related commands

### Use Traditional Commands When:
- You need complex command logic
- You want full control over command structure
- You're already using the traditional system
- You need fine-grained control

## See Also

- [Basic Commands](basic.md) - Traditional command creation
- [CommandContext](command-context.md) - Using CommandContext
- [ArgumentParser](argument-parser.md) - Type-safe argument parsing

