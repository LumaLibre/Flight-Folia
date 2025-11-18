# Configurable GUI

Flight supports creating GUIs from YAML configuration files, allowing for easy customization without code changes.

## Overview

Configurable GUIs allow you to:

- Define GUI layout in YAML files
- Use expressions for dynamic content
- Configure buttons with actions
- Support dynamic sections

## Basic Configuration

Create a YAML file (e.g., `guis/shop.yml`):

```yaml
title: "&8Shop"
rows: 4
default-item:
  material: GRAY_STAINED_GLASS_PANE
  name: " "

buttons:
  - slot: 20
    item:
      material: DIAMOND
      name: "&b&lDiamond"
      lore:
        - "&7Price: &a$100"
    actions:
      - type: message
        message: "&aPurchased diamond!"
```

## Loading Configuration

Load the configuration in your GUI:

```java
public class ShopGUI extends Gui implements ConfigurableGui {

    public ShopGUI(Player player) {
        super(player);
        loadFromConfig("guis/shop");
        draw();
    }

    @Override
    protected void draw() {
        // GUI is loaded from config
    }
}
```

## Actions

Configure button actions in YAML:

```yaml
actions:
  - type: message
    message: "Hello!"
  - type: command
    command: "give {player} diamond 1"
  - type: sound
    sound: ENTITY_PLAYER_LEVELUP
  - type: open_gui
    gui: "other_gui"
  - type: close
  - type: back
```

## Dynamic Sections

Use dynamic sections for lists:

```yaml
dynamic-sections:
  items:
    slots: [10, 11, 12, 13, 14, 15, 16]
    item-template:
      material: "{item.material}"
      name: "{item.name}"
    condition: "{items.size} > 0"
```

## Expressions

Use expressions for dynamic values:

```yaml
item:
  name: "&aBalance: &e${player.balance}"
  lore:
    - "&7Items: &e${items.size}"
    - "&7Page: &e${page} / ${pages}"
```

## See Also

- [Basic GUI](basic-gui.md) - Programmatic GUI creation
- [Configuration](../configuration.md) - Configuration system
