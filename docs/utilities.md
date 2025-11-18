# Utilities

Flight provides many utility classes to simplify common tasks in plugin development.

## Overview

Utility classes include:

- **QuickItem** - Easy item building
- **CooldownManager** - Player cooldown management
- **Input System** - Player input handling
- **Color Formatting** - Colors and gradients
- **ComponentMessage** - Component-based messages
- **Common Utilities** - Various helper classes

## Quick Examples

### QuickItem

```java
ItemStack item = QuickItem.of(Material.DIAMOND_SWORD)
    .name("&a&lLegendary Sword")
    .lore("&7This is a powerful sword!")
    .glow()
    .make();
```

### CooldownManager

```java
CooldownManager cooldowns = new CooldownManager(plugin);
cooldowns.setCooldown(player, "teleport", 30); // 30 second cooldown

if (cooldowns.isOnCooldown(player, "teleport")) {
    long remaining = cooldowns.getCooldownSeconds(player, "teleport");
    player.sendMessage("Cooldown: " + remaining + " seconds");
}
```

### Input

```java
new Input(plugin, player) {
    @Override
    public boolean onInput(String input) {
        player.sendMessage("You entered: " + input);
        return true; // Close input
    }
    
    @Override
    public String getTitle() {
        return "Enter your name";
    }
    
    @Override
    public String getSubtitle() {
        return "";
    }
    
    @Override
    public String getActionBar() {
        return "Type in chat";
    }
};
```

## Documentation Sections

### [QuickItem](utilities/quick-item.md)
Building items easily with QuickItem.

### [Cooldown Manager](utilities/cooldown-manager.md)
Managing player cooldowns.

### [Input System](utilities/input-system.md)
Handling player input.

### [Color Formatting](utilities/color-formatting.md)
Colors and gradient formatting.

### [Component Message](utilities/component-message.md)
Component-based messages.

### [Common Utilities](utilities/common-utilities.md)
Other utility classes.

## See Also

- [GUI System](gui.md) - Use utilities in GUIs
- [Commands](commands.md) - Use utilities in commands

