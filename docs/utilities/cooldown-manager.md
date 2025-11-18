# Cooldown Manager

CooldownManager provides an easy way to manage player cooldowns for actions.

## Basic Usage

```java
CooldownManager cooldowns = new CooldownManager(plugin);

// Set a cooldown
cooldowns.setCooldown(player, "teleport", 30); // 30 seconds

// Check if on cooldown
if (cooldowns.isOnCooldown(player, "teleport")) {
    long remaining = cooldowns.getCooldownSeconds(player, "teleport");
    player.sendMessage("Cooldown: " + remaining + " seconds");
} else {
    // Execute action
    player.teleport(location);
    cooldowns.setCooldown(player, "teleport", 30);
}
```

## Methods

### Setting Cooldowns

```java
// Per-player cooldown
cooldowns.setCooldown(player, "action", 60);

// Global cooldown (all players)
cooldowns.setGlobalCooldown("action", 60);

// Persistent cooldown (survives restarts)
cooldowns.setPersistentCooldown("key", 3600);
```

### Checking Cooldowns

```java
// Check if on cooldown
boolean onCooldown = cooldowns.isOnCooldown(player, "action");

// Get remaining seconds
long seconds = cooldowns.getCooldownSeconds(player, "action");

// Get remaining milliseconds
long ms = cooldowns.getCooldownMillis(player, "action");
```

### Bypass Permission

```java
// Set bypass permission
cooldowns.setBypassPermission("myplugin.cooldown.bypass");

// Players with this permission bypass all cooldowns
```

## Complete Example

```java
public class TeleportCommand extends Command {
    private final CooldownManager cooldowns;

    public TeleportCommand(CooldownManager cooldowns) {
        super(AllowedExecutor.PLAYER, "teleport");
        this.cooldowns = cooldowns;
    }

    @Override
    protected ReturnType execute(CommandContext context) {
        Player player = context.getPlayer();

        if (cooldowns.isOnCooldown(player, "teleport")) {
            long remaining = cooldowns.getCooldownSeconds(player, "teleport");
            player.sendMessage("§cYou must wait " + remaining + " seconds!");
            return ReturnType.FAIL;
        }

        // Teleport logic
        player.teleport(location);
        cooldowns.setCooldown(player, "teleport", 30);
        player.sendMessage("§aTeleported!");

        return ReturnType.SUCCESS;
    }

    // ... other methods
}
```

## See Also

- [Commands](../commands.md) - Using cooldowns in commands
- [Utilities](../utilities.md) - Other utilities
