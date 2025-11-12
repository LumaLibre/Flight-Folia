/*
 * Flight
 * Copyright 2022 Kiran Hart
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ca.tweetzy.flight.gui;

import ca.tweetzy.flight.comp.enums.CompMaterial;
import ca.tweetzy.flight.comp.enums.CompSound;
import ca.tweetzy.flight.comp.enums.ServerVersion;
import ca.tweetzy.flight.gui.events.GuiClickEvent;
import ca.tweetzy.flight.gui.events.GuiCloseEvent;
import ca.tweetzy.flight.gui.events.GuiDropItemEvent;
import ca.tweetzy.flight.gui.events.GuiOpenEvent;
import ca.tweetzy.flight.gui.events.GuiPageEvent;
import ca.tweetzy.flight.gui.methods.Clickable;
import ca.tweetzy.flight.gui.methods.Closable;
import ca.tweetzy.flight.gui.methods.Delayable;
import ca.tweetzy.flight.gui.methods.Droppable;
import ca.tweetzy.flight.gui.methods.Openable;
import ca.tweetzy.flight.gui.methods.Pagable;
import ca.tweetzy.flight.utils.Common;
import ca.tweetzy.flight.utils.QuickItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

public class Gui {

    // Core inventory fields
    protected Inventory inventory;
    protected String title;
    protected GuiType inventoryType = GuiType.STANDARD;
    protected int rows;
    protected int page = 1;
    protected int pages = 1;

    // Behavior flags
    protected boolean acceptsItems = false;
    protected boolean allowDropItems = true;
    protected boolean allowClose = true;
    protected boolean useLockedCells = false;
    protected boolean allowShiftClick = false;

    // Slot management
    protected final Map<Integer, Boolean> unlockedCells = new HashMap<>();
    protected final Map<Integer, ItemStack> cellItems = new HashMap<>();
    protected final Map<Integer, Map<ClickType, Clickable>> conditionalButtons = new HashMap<>();

    // Default items
    protected ItemStack blankItem = QuickItem.of(CompMaterial.BLACK_STAINED_GLASS_PANE).name(" ").lore(" ").make();
    protected static final ItemStack AIR = CompMaterial.AIR.parseItem();

    // Pagination items
    protected int nextPageIndex = -1, prevPageIndex = -1;
    protected ItemStack nextPageItem, prevPageItem;
    protected ItemStack nextPage, prevPage;
    protected Gui parent = null;

    // Event handlers
    protected Clickable defaultClicker = null;
    protected Clickable privateDefaultClicker = null;
    protected Clickable playerInvClicker = null;
    protected Delayable delayClicker = null;
    protected Openable opener = null;
    protected Closable closer = null;
    protected Droppable dropper = null;
    protected Pagable pager = null;

    // Sounds
    protected CompSound defaultSound = CompSound.UI_BUTTON_CLICK;
    protected CompSound navigateSound = CompSound.ENTITY_BAT_TAKEOFF;

    // Click delays
    protected final Map<Integer, Long> slotLastClicked = new HashMap<>();
    protected final Map<Integer, Long> slotClickDelays = new HashMap<>();
    protected long globalClickDelay = -1;
    protected long globalLastClicked = -1;
    protected boolean globalClickInitialized = false;

    protected boolean open = false;
    protected GuiManager guiManager;

    // Constructors
    public Gui() {
        this.rows = 3;
    }

    public Gui(@NotNull GuiType type) {
        this.inventoryType = type;
        this.rows = switch (type) {
            case HOPPER, DISPENSER -> 1;
            default -> 3;
        };
    }

    public Gui(@Nullable Gui parent) {
        this(3, parent);
    }

    public Gui(int rows) {
        this.rows = Math.max(1, Math.min(6, rows));
    }

    public Gui(int rows, @Nullable Gui parent) {
        this.parent = parent;
        this.rows = Math.max(1, Math.min(6, rows));
    }

    // --------------------------------------
    // Public API Methods
    // --------------------------------------

    @NotNull
    public List<Player> getPlayers() {
        return inventory == null ? Collections.emptyList()
                : inventory.getViewers().stream()
                .filter(Player.class::isInstance)
                .map(Player.class::cast)
                .toList();
    }

    @NotNull
    public Gui setGlobalClickDelay(long delay) {
        this.globalClickDelay = delay;
        return this;
    }

    @NotNull
    public Gui setSlotClickDelay(int slot, long delay) {
        this.slotClickDelays.put(slot, delay);
        return this;
    }

    @NotNull
    public Gui setSlotClickDelay(int row, int col, long delay) {
        final int cell = col + row * inventoryType.columns;
        this.slotClickDelays.put(cell, delay);
        return this;
    }

    @NotNull
    public Gui setAction(int cell, @Nullable Clickable action) {
        setConditional(cell, null, action);
        return this;
    }

    @NotNull
    public Gui setAction(int row, int col, @Nullable Clickable action) {
        setConditional(col + row * inventoryType.columns, null, action);
        return this;
    }

    @NotNull
    public Gui setAction(int cell, @Nullable ClickType type, @Nullable Clickable action) {
        setConditional(cell, type, action);
        return this;
    }

    @NotNull
    public Gui setAction(int row, int col, @Nullable ClickType type, @Nullable Clickable action) {
        setConditional(col + row * inventoryType.columns, type, action);
        return this;
    }

    @NotNull
    public Gui setActionForRange(int cellFirst, int cellLast, @Nullable Clickable action) {
        for (int cell = cellFirst; cell <= cellLast; ++cell) {
            setConditional(cell, null, action);
        }
        return this;
    }

    @NotNull
    public Gui setActionForRange(int cellRowFirst, int cellColFirst, int cellRowLast, int cellColLast, @Nullable Clickable action) {
        final int last = cellColLast + cellRowLast * inventoryType.columns;
        for (int cell = cellColFirst + cellRowFirst * inventoryType.columns; cell <= last; ++cell) {
            setConditional(cell, null, action);
        }
        return this;
    }

    @NotNull
    public Gui setActionForRange(int cellFirst, int cellLast, @Nullable ClickType type, @Nullable Clickable action) {
        for (int cell = cellFirst; cell <= cellLast; ++cell) {
            setConditional(cell, type, action);
        }
        return this;
    }

    @NotNull
    public Gui setActionForRange(int cellRowFirst, int cellColFirst, int cellRowLast, int cellColLast, @Nullable ClickType type, @Nullable Clickable action) {
        final int last = cellColLast + cellRowLast * inventoryType.columns;
        for (int cell = cellColFirst + cellRowFirst * inventoryType.columns; cell <= last; ++cell) {
            setConditional(cell, type, action);
        }
        return this;
    }

    @NotNull
    public Gui clearActions(int cell) {
        conditionalButtons.remove(cell);
        return this;
    }

    @NotNull
    public Gui clearActions(int row, int col) {
        return clearActions(col + row * inventoryType.columns);
    }

    @NotNull
    public Gui setOnOpen(@Nullable Openable action) {
        opener = action;
        return this;
    }

    @NotNull
    public Gui setOnClose(@Nullable Closable action) {
        closer = action;
        return this;
    }

    @NotNull
    public Gui setOnDrop(@Nullable Droppable action) {
        dropper = action;
        return this;
    }

    public boolean isOpen() {
        if (inventory != null && inventory.getViewers().isEmpty()) open = false;
        return open;
    }

    public boolean getAcceptsItems() {
        return acceptsItems;
    }

    public Gui setAcceptsItems(boolean acceptsItems) {
        this.acceptsItems = acceptsItems;
        return this;
    }

    public boolean getAllowDrops() {
        return allowDropItems;
    }

    public Gui setAllowDrops(boolean allow) {
        this.allowDropItems = allow;
        return this;
    }

    public boolean getAllowClose() {
        return allowClose;
    }

    public Gui setAllowClose(boolean allow) {
        this.allowClose = allow;
        return this;
    }

    public boolean isAllowShiftClick() {
        return allowShiftClick;
    }

    public void setAllowShiftClick(boolean allowShiftClick) {
        this.allowShiftClick = allowShiftClick;
    }

    @NotNull
    public Gui setTitle(String title) {
        if (title == null) title = "";
        if (!Objects.equals(title, this.title)) {
            this.title = title;
            if (inventory != null) {
                List<Player> viewers = getPlayers();
                boolean prevAllowClose = allowClose;
                exit();
                Inventory old = inventory;
                createInventory();
                inventory.setContents(old.getContents());
                viewers.forEach(p -> p.openInventory(inventory));
                allowClose = prevAllowClose;
            }
        }
        return this;
    }

    public int getRows() {
        return rows;
    }

    @NotNull
    public Gui setRows(int rows) {
        if (inventoryType != GuiType.HOPPER && inventoryType != GuiType.DISPENSER)
            this.rows = Math.max(1, Math.min(6, rows));
        return this;
    }

    @NotNull
    public Gui setDefaultAction(@Nullable Clickable action) {
        defaultClicker = action;
        return this;
    }

    @NotNull
    protected Gui setPrivateDefaultAction(@Nullable Clickable action) {
        privateDefaultClicker = action;
        return this;
    }

    @NotNull
    protected Gui setPlayerInventoryAction(@Nullable Clickable action) {
        playerInvClicker = action;
        return this;
    }

    @NotNull
    protected Gui setClickDelayAction(@Nullable Delayable action) {
        delayClicker = action;
        return this;
    }

    @NotNull
    public Gui setDefaultItem(@Nullable ItemStack item) {
        blankItem = item;
        return this;
    }

    @Nullable
    public ItemStack getDefaultItem() {
        return blankItem;
    }

    public Gui close() {
        allowClose = true;
        getPlayers().forEach(Player::closeInventory);
        return this;
    }

    public Gui exit() {
        allowClose = true;
        open = false;
        getPlayers().forEach(Player::closeInventory);
        return this;
    }

    @Nullable
    public Gui getParent() {
        return parent;
    }

    @NotNull
    public GuiType getType() {
        return inventoryType;
    }

    @NotNull
    public Gui setDefaultSound(CompSound sound) {
        defaultSound = sound;
        return this;
    }

    @NotNull
    public Gui setNavigateSound(CompSound sound) {
        navigateSound = sound;
        return this;
    }

    public CompSound getDefaultSound() {
        return defaultSound;
    }

    public CompSound getNavigateSound() {
        return navigateSound;
    }

    public void setUseLockedCells(boolean useLockedCells) {
        this.useLockedCells = useLockedCells;
    }

    public boolean isUseLockedCells() {
        return useLockedCells;
    }

    // --------------------------------------
    // Item and slot methods
    // --------------------------------------

    @Nullable
    public ItemStack getItem(int cell) {
        return inventory != null && unlockedCells.getOrDefault(cell, false) ? inventory.getItem(cell) : cellItems.get(cell);
    }

    @Nullable
    public ItemStack getItem(int row, int col) {
        return getItem(col + row * inventoryType.columns);
    }

    @NotNull
    public Gui setItem(int cell, @Nullable ItemStack item) {
        cellItems.put(cell, item);
        if (inventory != null && cell >= 0 && cell < inventory.getSize()) inventory.setItem(cell, item);
        return this;
    }

    @NotNull
    public Gui setItem(int row, int col, @Nullable ItemStack item) {
        return setItem(col + row * inventoryType.columns, item);
    }

    @NotNull
    public Gui setItems(int[] cells, @Nullable ItemStack item) {
        Arrays.stream(cells).forEach(i -> setItem(i, item));
        return this;
    }

    @NotNull
    public Gui setItems(int start, int end, @Nullable ItemStack item) {
        IntStream.rangeClosed(start, end).forEach(i -> setItem(i, item));
        return this;
    }

    @NotNull
    public Gui setButton(int cell, @Nullable ItemStack item, @Nullable Clickable action) {
        setItem(cell, item);
        setConditional(cell, null, action);
        return this;
    }

    @NotNull
    public Gui setButton(int row, int col, @Nullable ItemStack item, @Nullable Clickable action) {
        return setButton(col + row * inventoryType.columns, item, action);
    }

    @NotNull
    public Gui setButton(int cell, @Nullable ItemStack item, @Nullable ClickType type, @Nullable Clickable action) {
        setItem(cell, item);
        setConditional(cell, type, action);
        return this;
    }

    @NotNull
    public Gui setButton(int row, int col, @Nullable ItemStack item, @Nullable ClickType type, @Nullable Clickable action) {
        return setButton(col + row * inventoryType.columns, item, type, action);
    }

    // --------------------------------------
    // Unlock cells
    // --------------------------------------

    @NotNull
    public Gui setUnlocked(int cell) {
        unlockedCells.put(cell, true);
        return this;
    }

    @NotNull
    public Gui setUnlocked(int row, int col) {
        return setUnlocked(col + row * inventoryType.columns);
    }

    @NotNull
    public Gui setUnlocked(int cell, boolean open) {
        unlockedCells.put(cell, open);
        return this;
    }

    @NotNull
    public Gui setUnlocked(int row, int col, boolean open) {
        return setUnlocked(col + row * inventoryType.columns, open);
    }

    @NotNull
    public Gui setUnlockedRange(int start, int end) {
        return setUnlockedRange(start, end, true);
    }

    @NotNull
    public Gui setUnlockedRange(int start, int end, boolean open) {
        IntStream.rangeClosed(start, end).forEach(i -> unlockedCells.put(i, open));
        return this;
    }

    @NotNull
    public Gui setUnlockedRange(int rowStart, int colStart, int rowEnd, int colEnd) {
        return setUnlockedRange(rowStart, colStart, rowEnd, colEnd, true);
    }

    @NotNull
    public Gui setUnlockedRange(int rowStart, int colStart, int rowEnd, int colEnd, boolean open) {
        int last = colEnd + rowEnd * inventoryType.columns;
        for (int i = colStart + rowStart * inventoryType.columns; i <= last; i++) unlockedCells.put(i, open);
        return this;
    }

    // --------------------------------------
    // Pagination
    // --------------------------------------

    public void setPages(int pages) {
        this.pages = Math.max(1, pages);
        if (page > pages) setPage(pages);
    }

    public void setPage(int page) {
        changePage(page - this.page);
    }

    public void changePage(int direction) {
        setPageInternal(this.page + direction);
    }

    private void setPageInternal(int newPage) {
        int lastPage = page;
        page = Math.max(1, Math.min(pages, newPage));
        if (pager != null && page != lastPage) {
            pager.onPageChange(new GuiPageEvent(this, guiManager, lastPage, page));
            updatePageNavigation();
        }
    }

    /**
     * Set a listener for page change events.
     * Called when the page is changed using next/prev or setPage().
     */
    @NotNull
    public Gui setOnPage(@Nullable Pagable pager) {
        this.pager = pager;
        return this;
    }

    public void nextPage() {
        if (page < pages) setPageInternal(page + 1);
    }

    public void prevPage() {
        if (page > 1) setPageInternal(page - 1);
    }

    public void setNextPage(int cell, @NotNull ItemStack item) {
        nextPageIndex = cell;
        nextPage = item;
        if (page < pages) setButton(cell, item, ClickType.LEFT, e -> nextPage());
    }

    public void setNextPage(int row, int col, @NotNull ItemStack item) {
        setNextPage(col + row * inventoryType.columns, item);
    }

    public void setPrevPage(int cell, @NotNull ItemStack item) {
        prevPageIndex = cell;
        prevPage = item;
        if (page > 1) setButton(cell, item, ClickType.LEFT, e -> prevPage());
    }

    public void setPrevPage(int row, int col, @NotNull ItemStack item) {
        setPrevPage(col + row * inventoryType.columns, item);
    }

    protected void updatePageNavigation() {
        if (prevPage != null) setButton(prevPageIndex, page > 1 ? prevPage : prevPageItem, ClickType.LEFT, page > 1 ? e -> prevPage() : null);
        if (nextPage != null) setButton(nextPageIndex, page < pages ? nextPage : nextPageItem, ClickType.LEFT, page < pages ? e -> nextPage() : null);
    }

    // --------------------------------------
    // Inventory creation
    // --------------------------------------

    @NotNull
    protected Inventory getOrCreateInventory(@NotNull GuiManager manager) {
        return inventory != null ? inventory : generateInventory(manager);
    }

    @NotNull
    protected Inventory generateInventory(@NotNull GuiManager manager) {
        this.guiManager = manager;
        createInventory();
        int size = rows * inventoryType.columns;
        for (int i = 0; i < size; i++) inventory.setItem(i, cellItems.getOrDefault(i, unlockedCells.getOrDefault(i, false) ? AIR : blankItem));
        return inventory;
    }

    protected void createInventory() {
        InventoryType type = inventoryType == null ? InventoryType.CHEST : inventoryType.type;
        GuiHolder holder = new GuiHolder(guiManager, this);
        if (type == InventoryType.HOPPER || type == InventoryType.DISPENSER)
            inventory = holder.newInventory(type, title == null ? "" : Common.colorize(trimTitle(title)));
        else
            inventory = holder.newInventory(rows * 9, title == null ? "" : Common.colorize(trimTitle(title)));
    }

    // --------------------------------------
    // Event handling
    // --------------------------------------

    protected boolean onClick(@NotNull GuiManager manager, @NotNull Player player,
                              @NotNull Inventory inventory, @NotNull InventoryClickEvent event) {

        // --- SESSION VALIDATION ---
        if (!GUISessionLock.isValid(player.getUniqueId(), this)) {
            event.setCancelled(true);
            return false;
        }

        int cell = event.getSlot();
        Map<ClickType, Clickable> conditionals = conditionalButtons.get(cell);

        Clickable button = null;
        if (conditionals != null) button = conditionals.getOrDefault(event.getClick(), conditionals.get(null));

        if (button != null) {
            long now = System.currentTimeMillis();

            // Global click delay - properly initialized to prevent first-click bypass
            if (globalClickDelay != -1) {
                if (globalClickInitialized && now - globalLastClicked < globalClickDelay) {
                    if (delayClicker != null)
                        delayClicker.onClick(globalLastClicked, globalClickDelay,
                                new GuiClickEvent(manager, this, player, event, cell, true));
                    return false;
                }
                globalLastClicked = now;
                globalClickInitialized = true;
            }

            // Slot-specific delay
            long slotDelay = slotClickDelays.getOrDefault(cell, -1L);
            long lastSlotClick = slotLastClicked.getOrDefault(cell, -1L);
            if (slotDelay != -1 && lastSlotClick != -1 && now - lastSlotClick < slotDelay) {
                if (delayClicker != null)
                    delayClicker.onClick(lastSlotClick, slotDelay,
                            new GuiClickEvent(manager, this, player, event, cell, true));
                return false;
            }
            slotLastClicked.put(cell, now);

            // --- DOUBLE-CLICK EXPLOIT PREVENTION ---
            if (event.getClick() == ClickType.DOUBLE_CLICK) {
                ItemStack cursor = event.getCursor();
                if (cursor != null && cursor.getType() != Material.AIR) {
                    for (int i = 0; i < inventory.getSize(); i++) {
                        if (!unlockedCells.getOrDefault(i, false) &&
                                cursor.isSimilar(inventory.getItem(i))) {
                            event.setCancelled(true);
                            return false;
                        }
                    }
                }
            }

            // --- SHIFT-CLICK PROTECTION ---
            if ((event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT)
                    && !allowShiftClick) {
                event.setCancelled(true);
                return false;
            }

            button.onClick(new GuiClickEvent(manager, this, player, event, cell, true));
            return true;
        } else {
            if (defaultClicker != null)
                defaultClicker.onClick(new GuiClickEvent(manager, this, player, event, cell, true));
            else if (privateDefaultClicker != null)
                privateDefaultClicker.onClick(new GuiClickEvent(manager, this, player, event, cell, true));
            return button != null;
        }
    }

    protected boolean onClickPlayerInventory(@NotNull GuiManager manager, @NotNull Player player,
                                             @NotNull Inventory openInv, @NotNull InventoryClickEvent event) {

        if (!GUISessionLock.isValid(player.getUniqueId(), this)) {
            event.setCancelled(true);
            return false;
        }

        if (playerInvClicker == null) return false;
        playerInvClicker.onClick(new GuiClickEvent(manager, this, player, event, event.getSlot(), true));

        if (!acceptsItems || event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            event.setCancelled(true);
            return false;
        }

        return true;
    }

    protected boolean onClickOutside(@NotNull GuiManager manager, @NotNull Player player,
                                     @NotNull InventoryClickEvent event) {

        if (!GUISessionLock.isValid(player.getUniqueId(), this)) {
            event.setCancelled(true);
            return false;
        }

        if (!allowDropItems) {
            event.setCancelled(true);
            return false;
        }

        return dropper == null || dropper.onDrop(new GuiDropItemEvent(manager, this, player, event));
    }

    protected void onOpen(@NotNull GuiManager manager, @NotNull Player player) {
        // --- REGISTER SESSION ---
        GUISessionLock.start(player.getUniqueId(), this);
        open = true;
        guiManager = manager;
        if (opener != null) opener.onOpen(new GuiOpenEvent(manager, this, player));
    }

    protected void onClose(@NotNull GuiManager manager, @NotNull Player player) {
        if (!allowClose) {
            manager.showGUI(player, this);
            return;
        }

        GUISessionLock.end(player.getUniqueId());
        boolean showParent = open && parent != null;
        if (closer != null) closer.onClose(new GuiCloseEvent(manager, this, player));
        
        // Validate parent GUI before reopening to prevent exploitation
        if (showParent && parent != null) {
            // Ensure parent is a valid GUI instance and not the same as current
            if (parent != this) {
                manager.showGUI(player, parent);
            }
        }
    }

    public void update() {
        if (inventory == null) return;
        int size = rows * inventoryType.columns;
        for (int i = 0; i < size; i++) inventory.setItem(i, cellItems.getOrDefault(i, unlockedCells.getOrDefault(i, false) ? AIR : blankItem));
    }

    public void reset() {
        if (inventory != null) inventory.clear();
        cellItems.clear();
        unlockedCells.clear();
        conditionalButtons.clear();
        page = 1;
    }

    // --------------------------------------
    // Utility
    // --------------------------------------

    private static String trimTitle(String title) {
        if (title == null) return "";
        int maxLength = ServerVersion.isServerVersionAtLeast(ServerVersion.V1_14) ? 32 : 16;
        return title.length() > maxLength ? title.substring(0, maxLength) : title;
    }

    protected void setConditional(int cell, @Nullable ClickType type, @Nullable Clickable clicker) {
        conditionalButtons.computeIfAbsent(cell, k -> new HashMap<>()).put(type, clicker);
    }
}
