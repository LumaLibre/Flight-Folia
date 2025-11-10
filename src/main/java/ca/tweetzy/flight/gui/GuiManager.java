package ca.tweetzy.flight.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The current file has been created by Kiran Hart
 * Date Created: March 02 2021
 * Time Created: 4:32 p.m.
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise
 *
 * Patched to include GUISessionLock validation and InventoryView-safe handling to prevent
 * packet-delay / UI-desync duplication exploits.
 */
public class GuiManager {

    final Plugin plugin;
    final UUID uuid = UUID.randomUUID();
    final GuiListener listener = new GuiListener(this);
    final Map<Player, Gui> openInventories = new HashMap<>();
    private final Object lock = new Object();
    private boolean initialized = false;
    private boolean shutdown = false;

    public GuiManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public void init() {
        Bukkit.getPluginManager().registerEvents(listener, plugin);
        initialized = true;
        shutdown = false;
    }

    /**
     * Check to see if this manager cannot open any more GUI screens
     *
     * @return true if the owning plugin has shutdown
     */
    public boolean isClosed() {
        return shutdown;
    }

    /**
     * Create and display a GUI interface for a player
     *
     * @param player player to open the interface for
     * @param gui    GUI to use
     */
    public void showGUI(Player player, Gui gui) {
        if (shutdown && plugin.isEnabled()) {
            init();
        } else if (!initialized) {
            init();
        }

        // run async to avoid blocking the main thread while preparing inventory contents
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Gui openInv = openInventories.get(player);
            if (openInv != null) {
                // original behavior: mark old gui as not open
                openInv.open = false;
            }

            // NEW: Invalidate any previous server-side session and set the new active GUI.
            // This must happen before opening the inventory so delayed packets for the old GUI are invalid.
            GUISessionLock.start(player.getUniqueId(), gui);

            // create or reuse the inventory instance
            Inventory inv = gui.getOrCreateInventory(this);

            // open on main thread
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.openInventory(inv);
                gui.onOpen(this, player);
                synchronized (lock) {
                    openInventories.put(player, gui);
                }
            });
        });
    }

    /**
     * Close all active GUIs
     */
    public void closeAll() {
        synchronized (lock) {
            openInventories.entrySet().stream()
                    .filter(e -> e.getKey().getOpenInventory().getTopInventory().getHolder() instanceof GuiHolder)
                    .collect(Collectors.toList()) // to prevent concurrency exceptions
                    .forEach(e -> e.getKey().closeInventory());
            openInventories.clear();
        }
    }

    protected static class GuiListener implements Listener {

        final GuiManager manager;

        public GuiListener(GuiManager manager) {
            this.manager = manager;
        }

        @EventHandler(priority = EventPriority.LOW)
        void onDragGUI(InventoryDragEvent event) {
            if (!(event.getWhoClicked() instanceof Player)) {
                return;
            }

            final InventoryView view = event.getView();
            final Inventory top = view.getTopInventory();

            // verify this is a Flight GUI
            if (!(top.getHolder() instanceof GuiHolder)) return;
            final GuiHolder holder = (GuiHolder) top.getHolder();
            if (!holder.manager.uuid.equals(manager.uuid)) return;

            final Gui gui = holder.getGUI();
            final Player player = (Player) event.getWhoClicked();

            // session validation: reject if this is not the active server-side GUI
            if (!GUISessionLock.isValid(player.getUniqueId(), gui)) {
                event.setCancelled(true);
                // force-close to resync the client
                Bukkit.getScheduler().runTask(manager.plugin, () -> {
                    try {
                        manager.openInventories.remove(player);
                        player.closeInventory();
                    } catch (Throwable ignored) {}
                });
                return;
            }

            // ensure dragged slots don't touch locked GUI cells
            if (event.getRawSlots().stream().filter(slot -> gui.inventory.getSize() > slot).anyMatch(slot -> !gui.unlockedCells.getOrDefault(slot, false))) {
                event.setCancelled(true);
                event.setResult(Event.Result.DENY);
            }
        }

        @EventHandler(priority = EventPriority.LOW)
        void onClickGUI(InventoryClickEvent event) {
            if (!(event.getWhoClicked() instanceof Player)) {
                return;
            }

            final Player player = (Player) event.getWhoClicked();
            final InventoryView view = event.getView();
            final Inventory top = view.getTopInventory();

            // verify this is a Flight GUI
            if (!(top.getHolder() instanceof GuiHolder)) return;
            final GuiHolder holder = (GuiHolder) top.getHolder();
            if (!holder.manager.uuid.equals(manager.uuid)) return;

            final Gui gui = holder.getGUI();

            // session validation: reject if this is not the active server-side GUI
            if (!GUISessionLock.isValid(player.getUniqueId(), gui)) {
                event.setCancelled(true);
                // force-close to resync the client and remove tracking
                Bukkit.getScheduler().runTask(manager.plugin, () -> {
                    try {
                        manager.openInventories.remove(player);
                        player.closeInventory();
                    } catch (Throwable ignored) {}
                });
                return;
            }

            // SHIFT-click handling
            if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
                event.setCancelled(!gui.isAllowShiftClick());
                if (gui.isAllowShiftClick() && gui.onClick(manager, player, top, event)) {
                    playClickSound(player, gui, event.getRawSlot());
                }
                return;
            }

            // DOUBLE-CLICK protection (cancel when cursor item matches a GUI element)
            if (event.getClick() == ClickType.DOUBLE_CLICK) {
                final ItemStack cursor = event.getCursor();
                if (cursor != null && cursor.getType() != Material.AIR) {
                    int slot = 0;
                    for (ItemStack it : gui.inventory.getContents()) {
                        if (!gui.unlockedCells.getOrDefault(slot++, false) && it != null && cursor.isSimilar(it)) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
                return;
            }

            // Click outside GUI (drop area)
            if (event.getSlotType() == InventoryType.SlotType.OUTSIDE) {
                if (!gui.onClickOutside(manager, player, event)) {
                    event.setCancelled(true);
                }
                return;
            }

            // Determine whether click is in GUI (top) or player inventory (bottom)
            final boolean inGui = event.getRawSlot() < top.getSize();

            if (inGui) {
                // GUI area
                boolean unlocked = gui.unlockedCells.entrySet()
                        .stream()
                        .anyMatch(e -> e.getKey() == event.getSlot() && e.getValue());

                // if the slot is locked, cancel the event
                event.setCancelled(!unlocked);

                // process button press
                if (gui.onClick(manager, player, top, event)) {
                    playClickSound(player, gui, event.getRawSlot());
                }
            } else {
                // Player inventory area
                if (gui.onClickPlayerInventory(manager, player, top, event)) {
                    playDefaultSound(player, gui);
                } else if (!gui.acceptsItems || event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                    event.setCancelled(true);
                }
            }
        }

        @EventHandler(priority = EventPriority.LOW)
        void onCloseGUI(InventoryCloseEvent event) {
            final InventoryView view = event.getView();
            final Inventory top = view.getTopInventory();

            if (top.getHolder() != null && top.getHolder() instanceof GuiHolder && ((GuiHolder) top.getHolder()).manager.uuid.equals(manager.uuid)) {
                Gui gui = ((GuiHolder) top.getHolder()).getGUI();

                if (!gui.open) {
                    return;
                }
                final Player player = (Player) event.getPlayer();
                if (!gui.allowDropItems) {
                    player.setItemOnCursor(null);
                }

                // NEW: End the player's GUI session so delayed interactions from previous GUIs are no longer valid.
                GUISessionLock.end(player.getUniqueId());

                if (manager.shutdown) {
                    gui.onClose(manager, player);
                } else {
                    Bukkit.getScheduler().runTaskLater(manager.plugin, () -> {
                        gui.onClose(manager, player);
                        player.updateInventory();
                    }, 1);
                }
                manager.openInventories.remove(player);
            }
        }

        @EventHandler
        void onDisable(PluginDisableEvent event) {
            if (event.getPlugin() == manager.plugin) {
                // uh-oh! Abandon ship!!
                manager.shutdown = true;
                manager.closeAll();
                manager.initialized = false;
            }
        }

        /* -------------------------
         * Helper sound methods
         * -------------------------
         * Kept small and non-invasive so higher-level APIs don't need changes.
         */
        private void playClickSound(Player player, Gui gui, int rawSlot) {
            if (rawSlot == gui.nextPageIndex || rawSlot == gui.prevPageIndex) {
                if (gui.getNavigateSound() != null)
                    player.playSound(player.getLocation(), gui.getNavigateSound().parseSound(), 1F, 1F);
                else if (gui.getDefaultSound() != null)
                    player.playSound(player.getLocation(), gui.getDefaultSound().parseSound(), 1F, 1F);
            } else {
                if (gui.getDefaultSound() != null)
                    player.playSound(player.getLocation(), gui.getDefaultSound().parseSound(), 1F, 1F);
            }
        }

        private void playDefaultSound(Player player, Gui gui) {
            if (gui.getDefaultSound() != null)
                player.playSound(player.getLocation(), gui.getDefaultSound().parseSound(), 1F, 1F);
        }
    }
}
