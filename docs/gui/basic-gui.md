# Basic GUI

Learn how to create basic GUIs with Flight's GUI system. This guide covers creating GUIs, setting items, handling clicks, and managing GUI events.

## Creating Your First GUI

To create a GUI, extend the `Gui` class and implement the `draw()` method:

```java
public class MyGUI extends Gui {
    private final Player player;

    public MyGUI(Player player) {
        super(player);
        this.player = player;
        setTitle("My GUI");
        setRows(3);
        draw();
    }

    @Override
    protected void draw() {
        // GUI content goes here
    }
}
```

## Setting Items

### Basic Item

Set a simple item without a click handler:

```java
setItem(13, QuickItem.of(Material.DIAMOND)
    .name("&aDiamond")
    .make());
```

### Button with Click Handler

Set an item with a click handler:

```java
setButton(13, QuickItem.of(Material.DIAMOND)
    .name("&aClick Me!")
    .make(), click -> {
        click.player.sendMessage("You clicked the diamond!");
    });
```

### Setting Items by Row/Column

You can set items using row and column coordinates:

```java
// Set item at row 1, column 4 (slot 13 in a 3-row GUI)
setItem(1, 4, QuickItem.of(Material.DIAMOND).make());

// Set button at row 2, column 5
setButton(2, 5, QuickItem.of(Material.EMERALD).make(), click -> {
    click.player.sendMessage("Clicked!");
});
```

### Setting Multiple Items

Set multiple items at once:

```java
// Set items in an array of slots
setItems(new int[]{10, 11, 12, 13, 14, 15, 16},
    QuickItem.of(Material.GLASS_PANE).name(" ").make());

// Set items in a range
setItems(0, 8, QuickItem.of(Material.BLACK_STAINED_GLASS_PANE).name(" ").make());
```

## Click Handlers

### Basic Click Handler

Handle clicks on items:

```java
setButton(13, item, click -> {
    Player player = click.player;
    player.sendMessage("Item clicked!");
});
```

### Click Type Specific Handlers

Handle specific click types:

```java
setButton(13, item, ClickType.LEFT, click -> {
    click.player.sendMessage("Left clicked!");
});

setButton(13, item, ClickType.RIGHT, click -> {
    click.player.sendMessage("Right clicked!");
});

setButton(13, item, ClickType.SHIFT_LEFT, click -> {
    click.player.sendMessage("Shift-left clicked!");
});
```

### Multiple Click Types

You can set different handlers for different click types on the same slot:

```java
setAction(13, ClickType.LEFT, click -> {
    click.player.sendMessage("Left click!");
});

setAction(13, ClickType.RIGHT, click -> {
    click.player.sendMessage("Right click!");
});
```

## GUI Events

### On Open

Execute code when the GUI opens:

```java
setOnOpen(open -> {
    Player player = open.player;
    player.sendMessage("GUI opened!");
});
```

### On Close

Execute code when the GUI closes:

```java
setOnClose(close -> {
    Player player = close.player;
    player.sendMessage("GUI closed!");
});
```

### On Drop

Handle items being dropped in the GUI:

```java
setOnDrop(drop -> {
    Player player = drop.player;
    ItemStack item = drop.cursor;
    player.sendMessage("Dropped: " + item.getType());
});
```

## Complete Example

Here's a complete example with multiple features:

```java
public class ShopGUI extends Gui {
    private final Player player;

    public ShopGUI(Player player) {
        super(player);
        this.player = player;
        setTitle("&8Shop");
        setRows(4);

        setOnOpen(open -> {
            open.player.sendMessage("&aWelcome to the shop!");
        });

        setOnClose(close -> {
            close.player.sendMessage("&cThanks for shopping!");
        });

        draw();
    }

    @Override
    protected void draw() {
        // Fill border with glass panes
        setItems(0, 8, QuickItem.of(Material.BLACK_STAINED_GLASS_PANE).name(" ").make());
        setItems(27, 35, QuickItem.of(Material.BLACK_STAINED_GLASS_PANE).name(" ").make());

        // Left border
        for (int i = 9; i <= 18; i += 9) {
            setItem(i, QuickItem.of(Material.BLACK_STAINED_GLASS_PANE).name(" ").make());
        }

        // Right border
        for (int i = 17; i <= 26; i += 9) {
            setItem(i, QuickItem.of(Material.BLACK_STAINED_GLASS_PANE).name(" ").make());
        }

        // Shop items
        setButton(20, QuickItem.of(Material.DIAMOND)
            .name("&b&lDiamond")
            .lore("&7Price: &a$100")
            .make(), click -> {
                // Buy logic
                click.player.sendMessage("&aPurchased diamond!");
            });

        setButton(22, QuickItem.of(Material.EMERALD)
            .name("&a&lEmerald")
            .lore("&7Price: &a$50")
            .make(), click -> {
                click.player.sendMessage("&aPurchased emerald!");
            });

        setButton(24, QuickItem.of(Material.GOLD_INGOT)
            .name("&6&lGold Ingot")
            .lore("&7Price: &a$25")
            .make(), click -> {
                click.player.sendMessage("&aPurchased gold ingot!");
            });

        // Close button
        setButton(31, QuickItem.of(Material.BARRIER)
            .name("&c&lClose")
            .make(), click -> {
                click.gui.close();
            });
    }
}
```

## Opening GUIs

Use `GuiManager` to open GUIs:

```java
GuiManager guiManager = new GuiManager(plugin);
guiManager.showGUI(player, new ShopGUI(player));
```

## GUI Configuration

### Setting Title

```java
setTitle("&8My GUI");
```

### Setting Rows

```java
setRows(6); // 6 rows = 54 slots
```

### Default Item

Set a default item for empty slots:

```java
setDefaultItem(QuickItem.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").make());
```

### Accepting Items

Allow players to place items in the GUI:

```java
setAcceptsItems(true);
```

### Allowing Drops

Allow players to drop items in the GUI:

```java
setAllowDrops(true);
```

### Preventing Close

Prevent players from closing the GUI:

```java
setAllowClose(false);
```

## Click Delays

### Global Click Delay

Set a global click delay for all slots:

```java
setGlobalClickDelay(500); // 500ms delay
```

### Slot-Specific Click Delay

Set a click delay for a specific slot:

```java
setSlotClickDelay(13, 1000); // 1 second delay for slot 13
```

## Best Practices

1. **Always call `draw()`** after setting up the GUI
2. **Use QuickItem** for building items easily
3. **Handle errors gracefully** in click handlers
4. **Close GUIs properly** when done
5. **Use appropriate row counts** (1-6 rows)
6. **Set default items** for better visual appearance

## See Also

- [Pagination](pagination.md) - Add pagination to GUIs
- [QuickItem](../../utilities/quick-item.md) - Building items
- [GUI Templates](gui-templates.md) - Pre-built templates
