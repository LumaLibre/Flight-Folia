# GUI Events

Flight provides comprehensive event handling for GUI interactions.

## Click Events

Handle clicks on GUI items:

```java
setButton(13, item, click -> {
    Player player = click.player;
    ClickType clickType = click.clickType;
    int slot = click.slot;
    
    if (clickType == ClickType.LEFT) {
        player.sendMessage("Left clicked slot " + slot);
    } else if (clickType == ClickType.RIGHT) {
        player.sendMessage("Right clicked slot " + slot);
    }
});
```

## Open Events

Execute code when GUI opens:

```java
setOnOpen(open -> {
    Player player = open.player;
    player.sendMessage("GUI opened!");
    // Initialize GUI state
});
```

## Close Events

Execute code when GUI closes:

```java
setOnClose(close -> {
    Player player = close.player;
    player.sendMessage("GUI closed!");
    // Cleanup
});
```

## Page Events

Handle page changes:

```java
setOnPage(pageEvent -> {
    Player player = pageEvent.player;
    int page = pageEvent.page;
    player.sendMessage("Page changed to " + page);
});
```

## Drop Events

Handle items being dropped:

```java
setOnDrop(drop -> {
    Player player = drop.player;
    ItemStack item = drop.cursor;
    player.sendMessage("Dropped: " + item.getType());
});
```

## Event Information

### GuiClickEvent

- `player` - The player who clicked
- `clickType` - The type of click (LEFT, RIGHT, etc.)
- `slot` - The slot that was clicked
- `gui` - The GUI instance
- `manager` - The GuiManager instance

### GuiOpenEvent

- `player` - The player who opened the GUI
- `gui` - The GUI instance

### GuiCloseEvent

- `player` - The player who closed the GUI
- `gui` - The GUI instance

### GuiPageEvent

- `player` - The player viewing the GUI
- `page` - The new page number
- `gui` - The GUI instance

### GuiDropItemEvent

- `player` - The player who dropped the item
- `cursor` - The item that was dropped
- `gui` - The GUI instance

## See Also

- [Basic GUI](basic-gui.md) - Basic GUI creation
- [Pagination](pagination.md) - Page events

