# GUI Templates

Flight provides pre-built GUI templates to speed up development. These templates handle common patterns and reduce boilerplate code.

## BaseGUI

`BaseGUI` is a base class that provides common functionality:

```java
public class MyGUI extends BaseGUI {
    
    public MyGUI(Player player) {
        super("My GUI", 4);
    }

    @Override
    protected void draw() {
        // Your GUI content
    }

    @Override
    protected ItemStack getBackButton() {
        return QuickItem.of(Material.ARROW).name("&aBack").make();
    }

    @Override
    protected ItemStack getExitButton() {
        return QuickItem.of(Material.BARRIER).name("&cExit").make();
    }

    @Override
    protected ItemStack getPreviousButton() {
        return QuickItem.of(Material.ARROW).name("&aPrevious").make();
    }

    @Override
    protected ItemStack getNextButton() {
        return QuickItem.of(Material.ARROW).name("&aNext").make();
    }

    @Override
    protected int getPreviousButtonSlot() {
        return 27;
    }

    @Override
    protected int getNextButtonSlot() {
        return 35;
    }
}
```

### Features

- Automatic back/exit button handling
- Config loading support
- Common button definitions

## PagedGUI

`PagedGUI` extends `BaseGUI` and provides pagination:

```java
public class ItemListGUI extends PagedGUI<ItemStack> {
    
    public ItemListGUI(Player player, List<ItemStack> items) {
        super(null, "Items", 4, items);
    }

    @Override
    protected ItemStack makeDisplayItem(ItemStack item) {
        return item;
    }

    @Override
    protected void onClick(ItemStack item, GuiClickEvent click) {
        click.player.sendMessage("Clicked: " + item.getType());
    }

    // ... implement required button methods
}
```

See [Pagination](pagination.md) for more details.

## MaterialPickerGUI

A template for selecting materials (if available in your Flight version):

```java
MaterialPickerGUI picker = new MaterialPickerGUI(player, material -> {
    player.sendMessage("Selected: " + material);
});
guiManager.showGUI(player, picker);
```

## SoundPickerGUI

A template for selecting sounds (if available in your Flight version):

```java
SoundPickerGUI picker = new SoundPickerGUI(player, sound -> {
    player.sendMessage("Selected: " + sound);
});
guiManager.showGUI(player, picker);
```

## See Also

- [Basic GUI](basic-gui.md) - Basic GUI creation
- [Pagination](pagination.md) - Pagination system

