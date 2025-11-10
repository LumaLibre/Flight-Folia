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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * GuiManager - manages custom GUI inventories safely and efficiently.
 */
public class GuiManager {

    private final Plugin plugin;
    private final GuiListener listener = new GuiListener(this);
    private final ConcurrentMap<Player, Gui> openInventories = new ConcurrentHashMap<>();
    private boolean initialized = false;
    private boolean shutdown = false;

    public GuiManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public void init() {
        if (!initialized) {
            Bukkit.getPluginManager().registerEvents(listener, plugin);
            initialized = true;
            shutdown = false;
        }
    }

    public boolean isClosed() {
        return shutdown;
    }

    /**
     * Opens a GUI for the given player.
     */
    public void showGUI(Player player, Gui gui) {
        if (shutdown || !initialized) {
            init();
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Gui previous = openInventories.put(player, gui);
            if (previous != null) {
                previous.open = false;
            }

            Inventory inv = gui.getOrCreateInventory(this);
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.openInventory(inv);
                gui.onOpen(this, player);
            });
        });
    }

    /**
     * Closes all active GUIs for this plugin.
     */
    public void closeAll() {
        openInventories.keySet().removeIf(player -> {
            Inventory top = player.getOpenInventory().getTopInventory();
            if (top.getHolder() instanceof GuiHolder) {
                player.closeInventory();
                return true;
            }
            return false;
        });
        openInventories.clear();
    }

    // ----------------------
    // Listener class
    // ----------------------
    protected static class GuiListener implements Listener {

        private final GuiManager manager;

        public GuiListener(GuiManager manager) {
            this.manager = manager;
        }

        @EventHandler(priority = EventPriority.LOW)
        void onDragGUI(InventoryDragEvent event) {
            if (!(event.getWhoClicked() instanceof Player player)) return;

            InventoryView view = event.getView();
            if (view.getTopInventory().getHolder() instanceof GuiHolder holder && holder.manager == manager) {
                Gui gui = holder.getGUI();
                boolean cancel = event.getRawSlots().stream()
                        .anyMatch(slot -> slot < gui.inventory.getSize() && !gui.unlockedCells.getOrDefault(slot, false));
                if (cancel) {
                    event.setCancelled(true);
                    event.setResult(Event.Result.DENY);
                }
            }
        }

        @EventHandler(priority = EventPriority.LOW)
        void onClickGUI(InventoryClickEvent event) {
            if (!(event.getWhoClicked() instanceof Player player)) return;

            InventoryView view = event.getView();
            Inventory top = view.getTopInventory();
            if (!(top.getHolder() instanceof GuiHolder holder) || holder.manager != manager) return;

            Gui gui = holder.getGUI();
            boolean inGui = event.getRawSlot() < gui.inventory.getSize();
            boolean inPlayerInv = event.getSlotType() != InventoryType.SlotType.OUTSIDE && !inGui;

            // Prevent double clicks or shift clicks messing with locked items
            if (event.getClick() == ClickType.DOUBLE_CLICK) {
                ItemStack cursor = event.getCursor();
                if (cursor != null && cursor.getType() != Material.AIR) {
                    for (int i = 0; i < gui.inventory.getSize(); i++) {
                        if (!gui.unlockedCells.getOrDefault(i, false) && cursor.isSimilar(gui.inventory.getItem(i))) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }

            if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
                if (!gui.isAllowShiftClick()) event.setCancelled(true);
            }

            if (inGui) {
                event.setCancelled(gui.unlockedCells.entrySet().stream()
                        .noneMatch(e -> event.getSlot() == e.getKey() && e.getValue()));
                if (gui.onClick(manager, player, top, event)) {
                    if (event.getRawSlot() == gui.nextPageIndex || event.getRawSlot() == gui.prevPageIndex) {
                        if (gui.getNavigateSound() != null) player.playSound(player.getLocation(), gui.getNavigateSound().parseSound(), 1F, 1F);
                        else if (gui.getDefaultSound() != null) player.playSound(player.getLocation(), gui.getDefaultSound().parseSound(), 1F, 1F);
                    }
                }
            } else if (inPlayerInv) {
                if (!gui.onClickPlayerInventory(manager, player, top, event) && (!gui.acceptsItems || event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
                    event.setCancelled(true);
                }
            } else if (event.getSlotType() == InventoryType.SlotType.OUTSIDE) {
                if (!gui.onClickOutside(manager, player, event)) event.setCancelled(true);
            }
        }

        @EventHandler(priority = EventPriority.LOW)
        void onCloseGUI(InventoryCloseEvent event) {
            InventoryView view = event.getView();
            Inventory top = view.getTopInventory();
            if (!(top.getHolder() instanceof GuiHolder holder) || holder.manager != manager) return;

            Gui gui = holder.getGUI();
            if (!gui.open) return;

            Player player = (Player) event.getPlayer();
            if (!gui.allowDropItems) player.setItemOnCursor(null);

            Bukkit.getScheduler().runTask(manager.plugin, () -> {
                gui.onClose(manager, player);
                player.updateInventory();
            });

            manager.openInventories.remove(player);
        }

        @EventHandler
        void onDisable(PluginDisableEvent event) {
            if (event.getPlugin() == manager.plugin) {
                manager.shutdown = true;
                manager.closeAll();
                manager.initialized = false;
            }
        }
    }
}
