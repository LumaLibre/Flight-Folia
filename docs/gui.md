# GUI System

Flight provides a powerful and flexible GUI framework for creating interactive user interfaces in your Spigot plugins. The GUI system supports basic GUIs, pagination, configurable GUIs from YAML files, and pre-built templates.

## Overview

The Flight GUI system offers:

- **Basic GUI** - Create custom GUIs with click handlers
- **Pagination** - Built-in pagination support
- **Configurable GUI** - YAML-based GUI configuration
- **GUI Templates** - Pre-built templates (BaseGUI, PagedGUI, etc.)
- **Sign GUI** - Multi-line text input using signs
- **GUI Events** - Comprehensive event handling

## Quick Start

The simplest way to create a GUI is to extend the `Gui` class:

```java
public class MyGUI extends Gui {
    public MyGUI(Player player) {
        super(player);
        setTitle("My GUI");
        setRows(3);
        draw();
    }

    @Override
    protected void draw() {
        setItem(13, QuickItem.of(Material.DIAMOND)
            .name("&aClick Me!")
            .lore("&7Click this item to do something!")
            .make(), click -> {
                click.player.sendMessage("You clicked the diamond!");
            });
    }
}
```

Open the GUI:

```java
GuiManager guiManager = new GuiManager(plugin);
guiManager.showGUI(player, new MyGUI(player));
```

## Documentation Sections

### [Basic GUI](gui/basic-gui.md)
Learn how to create basic GUIs with click handlers, items, and events.

**Topics covered:**
- Creating your first GUI
- Setting items and buttons
- Click handlers
- GUI events (open, close, drop)
- Best practices

### [Pagination](gui/pagination.md)
Add pagination to your GUIs to display large lists of items.

**Topics covered:**
- Setting up pagination
- Next/previous page buttons
- Page navigation
- Displaying items per page

### [Configurable GUI](gui/configurable-gui.md)
Create GUIs from YAML configuration files for easy customization.

**Topics covered:**
- YAML GUI configuration
- Dynamic sections
- Actions and expressions
- Button configuration

### [GUI Templates](gui/gui-templates.md)
Use pre-built GUI templates to speed up development.

**Topics covered:**
- BaseGUI template
- PagedGUI template
- MaterialPickerGUI
- SoundPickerGUI

### [Sign GUI](gui/sign-gui.md)
Use Sign GUI for multi-line text input from players.

**Topics covered:**
- Creating Sign GUI
- Handling input
- Response handling

### [GUI Events](gui/gui-events.md)
Handle GUI events for advanced interactions.

**Topics covered:**
- Click events
- Open/close events
- Page events
- Drop events

## GuiManager

The `GuiManager` handles GUI registration and display:

```java
GuiManager guiManager = new GuiManager(plugin);

// Show a GUI to a player
guiManager.showGUI(player, new MyGUI(player));

// Get the GUI a player is viewing
Gui currentGUI = guiManager.getGUI(player);
```

## QuickItem

`QuickItem` is a builder for creating items easily:

```java
ItemStack item = QuickItem.of(Material.DIAMOND_SWORD)
    .name("&a&lLegendary Sword")
    .lore("&7This is a powerful sword!")
    .glow()
    .make();
```

See [QuickItem documentation](../utilities/quick-item.md) for more details.

## GUI Types

Flight supports different GUI types:

- `GuiType.STANDARD` - Standard chest GUI (default)
- `GuiType.HOPPER` - Hopper GUI (5 slots)
- `GuiType.DISPENSER` - Dispenser GUI (9 slots)

## See Also

- [QuickItem](../utilities/quick-item.md) - Building items easily
- [Utilities](../utilities.md) - Other useful utilities
- [Commands](commands.md) - Open GUIs from commands

