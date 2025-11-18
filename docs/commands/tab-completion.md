# Tab Completion

Tab completion provides intelligent suggestions to players when they type commands. Flight makes it easy to implement context-aware tab completion.

## Basic Tab Completion

The `tab` method in your command class provides tab completion:

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
                .collect(Collectors.toList());
    }
    
    return Collections.emptyList();
}
```

## Player Name Suggestions

A common use case is suggesting player names:

```java
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
```

## Context-Aware Suggestions

Provide different suggestions based on previous arguments:

```java
@Override
protected List<String> tab(CommandContext context) {
    int argCount = context.getArgCount();
    
    if (argCount == 1) {
        // First argument: suggest actions
        String typed = context.getArg(0, "").toLowerCase();
        return List.of("give", "remove", "set").stream()
                .filter(s -> s.startsWith(typed))
                .collect(Collectors.toList());
    }
    
    if (argCount == 2) {
        String action = context.getArg(0, "").toLowerCase();
        
        if (action.equals("give") || action.equals("remove")) {
            // Suggest player names for give/remove
            String typed = context.getArg(1, "").toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(typed))
                    .collect(Collectors.toList());
        } else if (action.equals("set")) {
            // Suggest values for set
            String typed = context.getArg(1, "").toLowerCase();
            return List.of("true", "false").stream()
                    .filter(s -> s.startsWith(typed))
                    .collect(Collectors.toList());
        }
    }
    
    return Collections.emptyList();
}
```

## Material Suggestions

Suggest materials for item-related commands:

```java
@Override
protected List<String> tab(CommandContext context) {
    int argCount = context.getArgCount();
    
    if (argCount == 1) {
        String typed = context.getArg(0, "").toUpperCase();
        return Arrays.stream(Material.values())
                .filter(Material::isItem)  // Only items
                .filter(m -> !m.isAir())     // Exclude air
                .map(Material::name)
                .filter(name -> name.startsWith(typed))
                .limit(20)  // Limit results for performance
                .collect(Collectors.toList());
    }
    
    return Collections.emptyList();
}
```

## Number Suggestions

Suggest common numbers for amount-related commands:

```java
@Override
protected List<String> tab(CommandContext context) {
    int argCount = context.getArgCount();
    
    if (argCount == 2) {
        // Suggest common amounts
        String typed = context.getArg(1, "");
        List<String> amounts = List.of("1", "5", "10", "16", "32", "64", "100");
        
        if (typed.isEmpty()) {
            return amounts;
        }
        
        return amounts.stream()
                .filter(s -> s.startsWith(typed))
                .collect(Collectors.toList());
    }
    
    return Collections.emptyList();
}
```

## Permission-Based Suggestions

Show different suggestions based on permissions:

```java
@Override
protected List<String> tab(CommandContext context) {
    int argCount = context.getArgCount();
    CommandSender sender = context.getSender();
    
    if (argCount == 1) {
        String typed = context.getArg(0, "").toLowerCase();
        List<String> suggestions = new ArrayList<>();
        
        // Basic options for everyone
        suggestions.add("help");
        suggestions.add("info");
        
        // Admin-only options
        if (sender.hasPermission("myplugin.admin")) {
            suggestions.add("reload");
            suggestions.add("reset");
        }
        
        // Owner-only options
        if (sender.hasPermission("myplugin.owner")) {
            suggestions.add("delete");
        }
        
        return suggestions.stream()
                .filter(s -> s.startsWith(typed))
                .collect(Collectors.toList());
    }
    
    return Collections.emptyList();
}
```

## Complete Example

Here's a complete example with multiple argument levels:

```java
public class AdminCommand extends Command {
    
    public AdminCommand() {
        super(AllowedExecutor.PLAYER, "admin");
    }

    @Override
    protected ReturnType execute(CommandContext context) {
        // Command logic
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> tab(CommandContext context) {
        int argCount = context.getArgCount();
        CommandSender sender = context.getSender();
        
        if (argCount == 0) {
            // Suggest main actions
            return List.of("give", "remove", "set", "info");
        }
        
        if (argCount == 1) {
            // Filter first argument
            String typed = context.getArg(0, "").toLowerCase();
            return List.of("give", "remove", "set", "info").stream()
                    .filter(s -> s.startsWith(typed))
                    .collect(Collectors.toList());
        }
        
        if (argCount == 2) {
            String action = context.getArg(0, "").toLowerCase();
            
            if (action.equals("give") || action.equals("remove")) {
                // Suggest player names
                String typed = context.getArg(1, "").toLowerCase();
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(typed))
                        .collect(Collectors.toList());
            } else if (action.equals("set")) {
                // Suggest setting names
                String typed = context.getArg(1, "").toLowerCase();
                return List.of("enabled", "disabled", "max", "min").stream()
                        .filter(s -> s.startsWith(typed))
                        .collect(Collectors.toList());
            }
        }
        
        if (argCount == 3) {
            String action = context.getArg(0, "").toLowerCase();
            
            if (action.equals("set")) {
                String setting = context.getArg(1, "").toLowerCase();
                
                if (setting.equals("enabled") || setting.equals("disabled")) {
                    // Suggest boolean values
                    String typed = context.getArg(2, "").toLowerCase();
                    return List.of("true", "false").stream()
                            .filter(s -> s.startsWith(typed))
                            .collect(Collectors.toList());
                } else if (setting.equals("max") || setting.equals("min")) {
                    // Suggest numbers
                    String typed = context.getArg(2, "");
                    return List.of("1", "10", "100", "1000").stream()
                            .filter(s -> s.startsWith(typed))
                            .collect(Collectors.toList());
                }
            } else if (action.equals("give")) {
                // Suggest item names
                String typed = context.getArg(2, "").toUpperCase();
                return Arrays.stream(Material.values())
                        .filter(Material::isItem)
                        .map(Material::name)
                        .filter(name -> name.startsWith(typed))
                        .limit(20)
                        .collect(Collectors.toList());
            }
        }
        
        return Collections.emptyList();
    }

    @Override
    public String getPermissionNode() {
        return "myplugin.admin";
    }

    @Override
    public String getSyntax() {
        return "/admin <give|remove|set|info> [args...]";
    }

    @Override
    public String getDescription() {
        return "Admin commands";
    }
}
```

## Best Practices

1. **Filter suggestions** - Always filter based on what the player has typed
2. **Limit results** - Limit large suggestion lists (e.g., materials) for performance
3. **Context-aware** - Provide different suggestions based on previous arguments
4. **Permission checks** - Only suggest options the player has permission to use
5. **Empty lists** - Return `Collections.emptyList()` when no suggestions

## Performance Tips

1. **Cache common suggestions** - If you have static suggestion lists, cache them
2. **Limit large lists** - Use `.limit()` for large collections
3. **Early returns** - Return early if no suggestions are needed
4. **Efficient filtering** - Use streams efficiently

## Common Patterns

### Pattern 1: Simple Options

```java
if (argCount == 1) {
    String typed = context.getArg(0, "").toLowerCase();
    return List.of("option1", "option2", "option3").stream()
            .filter(s -> s.startsWith(typed))
            .collect(Collectors.toList());
}
```

### Pattern 2: Player Names

```java
if (argCount == 1) {
    String typed = context.getArg(0, "").toLowerCase();
    return Bukkit.getOnlinePlayers().stream()
            .map(Player::getName)
            .filter(name -> name.toLowerCase().startsWith(typed))
            .collect(Collectors.toList());
}
```

### Pattern 3: Materials

```java
if (argCount == 1) {
    String typed = context.getArg(0, "").toUpperCase();
    return Arrays.stream(Material.values())
            .filter(Material::isItem)
            .map(Material::name)
            .filter(name -> name.startsWith(typed))
            .limit(20)
            .collect(Collectors.toList());
}
```

## See Also

- [Basic Commands](basic.md) - Basic command creation
- [CommandContext](command-context.md) - Using CommandContext in tab completion
- [Annotation-Based Commands](annotations.md) - Tab completion with annotations

