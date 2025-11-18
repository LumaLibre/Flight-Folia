# Pagination

Flight provides built-in pagination support for GUIs, making it easy to display large lists of items across multiple pages.

## Basic Pagination

Use `setNextPage()` and `setPrevPage()` to add pagination:

```java
public class ItemListGUI extends Gui {
    private final List<ItemStack> items;

    public ItemListGUI(Player player, List<ItemStack> items) {
        super(player);
        this.items = items;
        setTitle("Items");
        setRows(4);
        draw();
    }

    @Override
    protected void draw() {
        // Calculate pagination
        int itemsPerPage = 21; // 3 rows * 7 columns (excluding borders)
        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, items.size());
        pages = (int) Math.ceil((double) items.size() / itemsPerPage);

        // Display items for current page
        int slot = 10; // Start slot
        for (int i = startIndex; i < endIndex; i++) {
            setButton(slot, items.get(i), click -> {
                click.player.sendMessage("Clicked item " + i);
            });
            slot++;
            if (slot % 9 == 8) slot += 2; // Skip border columns
        }

        // Previous page button
        if (page > 1) {
            setPrevPage(27, QuickItem.of(Material.ARROW)
                .name("&aPrevious Page")
                .make());
        }

        // Next page button
        if (page < pages) {
            setNextPage(35, QuickItem.of(Material.ARROW)
                .name("&aNext Page")
                .make());
        }

        // Page indicator
        setItem(31, QuickItem.of(Material.BOOK)
            .name("&7Page &e" + page + " &7/ &e" + pages)
            .make());
    }
}
```

## Using PagedGUI Template

For easier pagination, use the `PagedGUI` template:

```java
public class MyPagedGUI extends PagedGUI<ItemStack> {

    public MyPagedGUI(Player player, List<ItemStack> items) {
        super(null, "Items", 4, items);
    }

    @Override
    protected ItemStack makeDisplayItem(ItemStack item) {
        return item; // Return the item to display
    }

    @Override
    protected void onClick(ItemStack item, GuiClickEvent click) {
        click.player.sendMessage("Clicked: " + item.getType());
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

## Page Navigation

The pagination system automatically handles page navigation. When a player clicks the next/previous page buttons, the GUI redraws with the new page.

## Page Events

Handle page changes:

```java
setOnPage(pageEvent -> {
    Player player = pageEvent.player;
    player.sendMessage("Page changed to " + pageEvent.page);
});
```

## See Also

- [Basic GUI](basic-gui.md) - Basic GUI creation
- [GUI Templates](gui-templates.md) - PagedGUI template
