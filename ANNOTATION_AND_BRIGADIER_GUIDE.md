# Annotation-Based Commands & Brigadier Support Guide

This guide shows you how to use the new annotation-based command system and Brigadier support.

---

## 📚 **Table of Contents**

1. [Annotation-Based Commands](#annotation-based-commands)
2. [Brigadier Support](#brigadier-support)
3. [Comparison](#comparison)
4. [Examples](#examples)

---

## 🎯 **Annotation-Based Commands**

Annotation-based commands allow you to register commands using annotations instead of extending the `Command` class. This is **optional** and works alongside the existing command system.

### **Benefits:**
- ✅ Less boilerplate code
- ✅ Cleaner, more readable code
- ✅ Easy to organize commands in classes
- ✅ Works with existing CommandManager

### **Annotations:**

#### `@Command`
Marks a method as a command handler.

```java
@Command(
    name = "example",
    aliases = {"ex", "test"},
    executor = AllowedExecutor.PLAYER,
    permission = "flight.example",
    description = "Example command",
    syntax = "/example [message]",
    async = false
)
public ReturnType exampleCommand(CommandContext context) {
    // Command logic
    return ReturnType.SUCCESS;
}
```

#### `@SubCommand`
Marks a method as a subcommand handler.

```java
@SubCommand(
    parent = "shop",
    name = "buy",
    executor = AllowedExecutor.PLAYER,
    permission = "flight.shop.buy",
    description = "Buy items from shop"
)
public ReturnType shopBuy(CommandContext context) {
    // Subcommand logic
    return ReturnType.SUCCESS;
}
```

#### `@TabComplete`
Marks a method as a tab completer.

```java
@TabComplete(command = "example")
public List<String> exampleTabComplete(CommandContext context) {
    return List.of("option1", "option2", "option3");
}
```

### **Usage Example:**

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

    @Command(
        name = "hello",
        executor = AllowedExecutor.PLAYER,
        description = "Say hello"
    )
    public ReturnType helloCommand(CommandContext context) {
        Player player = context.getPlayer();
        player.sendMessage("§aHello, " + player.getName() + "!");
        return ReturnType.SUCCESS;
    }

    @SubCommand(
        parent = "hello",
        name = "world",
        executor = AllowedExecutor.PLAYER
    )
    public ReturnType helloWorld(CommandContext context) {
        context.getSender().sendMessage("§aHello, World!");
        return ReturnType.SUCCESS;
    }

    @TabComplete(command = "hello")
    public List<String> helloTabComplete(CommandContext context) {
        return List.of("world", "there", "everyone");
    }
}
```

### **Method Signatures:**

- **Command methods** must accept `CommandContext` and return `ReturnType` or `boolean`
- **Tab complete methods** must accept `CommandContext` and return `List<String>`

---

## 🚀 **Brigadier Support**

Brigadier is Minecraft's modern command framework (introduced in 1.13). It provides:
- Better tab completion
- Improved command suggestions
- More efficient command parsing

**Note:** Only works on Minecraft 1.13+ servers.

### **Usage Example:**

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
            "mycommand",
            "My command description",
            "permission.node",
            List.of("mc", "mycmd"), // Aliases
            (sender, label, args) -> {
                // Command execution
                sender.sendMessage("§aCommand executed!");
                return true;
            },
            (sender, alias, args) -> {
                // Tab completion
                if (args.length == 1) {
                    return List.of("option1", "option2");
                }
                return new ArrayList<>();
            }
        );

        brigadierManager.registerBrigadierCommand("mycommand", command);
    }
}
```

### **Benefits:**
- ✅ Better tab completion performance
- ✅ Native Minecraft command suggestions
- ✅ More efficient command parsing
- ✅ Better integration with Minecraft's command system

---

## 📊 **Comparison**

### **Traditional Command (Extending Command class):**
```java
public class MyCommand extends Command {
    public MyCommand() {
        super(AllowedExecutor.PLAYER, "mycommand");
    }

    @Override
    protected ReturnType execute(CommandContext context) {
        // Logic
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> tab(CommandContext context) {
        return List.of("option1", "option2");
    }

    @Override
    public String getPermissionNode() {
        return "flight.mycommand";
    }

    @Override
    public String getSyntax() {
        return "/mycommand";
    }

    @Override
    public String getDescription() {
        return "My command";
    }
}

// Registration
commandManager.addCommand(new MyCommand());
```

### **Annotation-Based Command:**
```java
public class MyCommands {
    @Command(
        name = "mycommand",
        executor = AllowedExecutor.PLAYER,
        permission = "flight.mycommand",
        description = "My command"
    )
    public ReturnType myCommand(CommandContext context) {
        // Logic
        return ReturnType.SUCCESS;
    }

    @TabComplete(command = "mycommand")
    public List<String> myCommandTab(CommandContext context) {
        return List.of("option1", "option2");
    }
}

// Registration
AnnotationCommandHandler handler = new AnnotationCommandHandler(plugin, commandManager);
handler.registerCommands(new MyCommands());
```

### **Brigadier Command:**
```java
BrigadierCommandManager brigadier = new BrigadierCommandManager(plugin);
org.bukkit.command.Command command = brigadier.createBrigadierCommand(
    "mycommand",
    "My command",
    "flight.mycommand",
    new ArrayList<>(),
    (sender, label, args) -> {
        sender.sendMessage("§aCommand executed!");
        return true;
    },
    (sender, alias, args) -> List.of("option1", "option2")
);
brigadier.registerBrigadierCommand("mycommand", command);
```

---

## 🎓 **When to Use What?**

### **Use Traditional Commands When:**
- You need complex command logic
- You want full control over command structure
- You're already using the traditional system

### **Use Annotation-Based Commands When:**
- You want cleaner, more organized code
- You have many simple commands
- You want to group related commands in classes
- You prefer declarative style

### **Use Brigadier Commands When:**
- You're targeting Minecraft 1.13+
- You need better tab completion performance
- You want native Minecraft command integration
- You need advanced command suggestions

---

## 📝 **Complete Example**

Here's a complete example combining all approaches:

```java
public class CommandExamples {
    private final JavaPlugin plugin;
    private final CommandManager commandManager;
    private final BrigadierCommandManager brigadierManager;

    public CommandExamples(JavaPlugin plugin, CommandManager commandManager) {
        this.plugin = plugin;
        this.commandManager = commandManager;
        this.brigadierManager = new BrigadierCommandManager(plugin);
    }

    public void registerAll() {
        // 1. Traditional command
        commandManager.addCommand(new TraditionalCommand());

        // 2. Annotation-based commands
        AnnotationCommandHandler handler = new AnnotationCommandHandler(plugin, commandManager);
        handler.registerCommands(this);

        // 3. Brigadier commands (if available)
        if (BrigadierCommandManager.isAvailable()) {
            registerBrigadierCommands();
        }
    }

    // Annotation-based command
    @Command(
        name = "annotated",
        executor = AllowedExecutor.PLAYER,
        description = "Annotation-based command"
    )
    public ReturnType annotatedCommand(CommandContext context) {
        context.getSender().sendMessage("§aThis is an annotation-based command!");
        return ReturnType.SUCCESS;
    }

    // Brigadier command
    private void registerBrigadierCommands() {
        org.bukkit.command.Command command = brigadierManager.createBrigadierCommand(
            "brigadier",
            "Brigadier command",
            "flight.brigadier",
            new ArrayList<>(),
            (sender, label, args) -> {
                sender.sendMessage("§aThis is a Brigadier command!");
                return true;
            },
            (sender, alias, args) -> List.of("option1", "option2")
        );
        brigadierManager.registerBrigadierCommand("brigadier", command);
    }
}
```

---

