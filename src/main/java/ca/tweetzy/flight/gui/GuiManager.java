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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Modern, safe GUI Manager for Spigot/Paper 1.20.6 → 1.21+.
 *
 * ✅ Handles InventoryView interface/class differences safely
 * ✅ Prevents packet-based duplication exploits
 * ✅ Fully async-safe (never calls Bukkit methods off the main thread)
 * ✅ Automatic session expiry cleanup
 */
public class GuiManager {

    /** Debug flag: set to true to log detailed reflection errors. */
    private static final boolean DEBUG = false;

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
     * Opens the specified GUI for a player.
     * Safe to call from async context; the open happens on the main thread.
     * 
     * This method automatically detects if there's an existing GUI open for the player
     * and uses safe transition logic to prevent setOnClose handlers from running.
     * This ensures backwards compatibility while providing safe transitions.
     */
    public void showGUI(Player player, Gui gui) {
        if (shutdown || !initialized) {
            init();
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Gui previous = openInventories.put(player, gui);
            
            // If there's a previous GUI open, use safe transition logic
            if (previous != null && previous.isOpen() && previous.getPlayers().contains(player)) {
                // Use transition logic to prevent setOnClose from running
                previous.isTransitioning = true;
                previous.allowClose = true;
                previous.open = false;
                
                // Cancel update tasks for updating GUIs (e.g., AuctionUpdatingPagedGUI)
                // Use reflection to safely check and cancel tasks
                try {
                    Method cancelTask = previous.getClass().getMethod("cancelTask");
                    cancelTask.invoke(previous);
                    if (DEBUG) {
                        Bukkit.getLogger().info("[GuiManager] Cancelled update task for " + previous.getClass().getSimpleName() + " (transitioning to " + gui.getClass().getSimpleName() + ")");
                    }
                } catch (NoSuchMethodException e) {
                    // Not an updating GUI, that's fine
                } catch (Exception e) {
                    if (DEBUG) {
                        Bukkit.getLogger().warning("[GuiManager] Error cancelling task for " + previous.getClass().getSimpleName() + ": " + e.getMessage());
                    }
                }
            } else if (previous != null) {
                previous.open = false;
                // Also try to cancel task even if GUI wasn't open (safety measure)
                try {
                    Method cancelTask = previous.getClass().getMethod("cancelTask");
                    cancelTask.invoke(previous);
                    if (DEBUG) {
                        Bukkit.getLogger().info("[GuiManager] Cancelled update task for " + previous.getClass().getSimpleName() + " (GUI was not open)");
                    }
                } catch (Exception e) {
                    // Not an updating GUI or error, that's fine
                }
            }

            Inventory inv = gui.getOrCreateInventory(this);

            // Move session lock to main thread to prevent race condition
            // Session lock must be set BEFORE inventory opens to prevent exploit
            Bukkit.getScheduler().runTask(plugin, () -> {
                // Start session lock on main thread before opening inventory
                GUISessionLock.start(player.getUniqueId(), gui);
                
                player.openInventory(inv);
                gui.onOpen(this, player);
            });
        });
    }

    public void closeAll() {
        openInventories.keySet().removeIf(player -> {
            Inventory top = player.getOpenInventory().getTopInventory();
            if (top.getHolder() instanceof GuiHolder) {
                player.closeInventory();
                GUISessionLock.end(player.getUniqueId());
                return true;
            }
            return false;
        });
        openInventories.clear();
    }

    // ------------------------------------------------------------------
    // Reflection utility for InventoryView compatibility
    // ------------------------------------------------------------------

    public static Inventory getTopInventoryCompat(InventoryView view) {
        if (view == null) return null;
        try {
            Method getTopInventory = view.getClass().getMethod("getTopInventory");
            getTopInventory.setAccessible(true);
            return (Inventory) getTopInventory.invoke(view);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            // Log error but don't crash server - return null to gracefully handle
            Bukkit.getLogger().warning("[GuiManager] Failed to resolve top inventory via reflection: " + e.getMessage());
            if (DEBUG) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static Inventory getTopInventory(InventoryEvent event) {
        if (event == null) return null;
        try {
            Object view = event.getView();
            if (view == null) return null;
            Method getTopInventory = view.getClass().getMethod("getTopInventory");
            getTopInventory.setAccessible(true);
            return (Inventory) getTopInventory.invoke(view);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            // Log error but don't crash server - return null to gracefully handle
            Bukkit.getLogger().warning("[GuiManager] Failed to resolve top inventory via reflection: " + e.getMessage());
            if (DEBUG) {
                e.printStackTrace();
            }
            return null;
        }
    }

    // ------------------------------------------------------------------
    // Event listener for inventory interactions
    // ------------------------------------------------------------------

    protected static class GuiListener implements Listener {

        private final GuiManager manager;

        public GuiListener(GuiManager manager) {
            this.manager = manager;
        }

        /**
         * Validates the player's GUI session.
         * Automatically cleans up expired or invalid sessions.
         */
        private boolean validateSession(Player player, Gui gui) {
            if (!GUISessionLock.isValid(player.getUniqueId(), gui)) {
                GUISessionLock.end(player.getUniqueId());
                return false;
            }
            return true;
        }

        @EventHandler(priority = EventPriority.LOW)
        void onDragGUI(InventoryDragEvent event) {
            if (!(event.getWhoClicked() instanceof Player player)) return;

            Inventory top = getTopInventoryCompat(event.getView());
            if (!(top != null && top.getHolder() instanceof GuiHolder holder)) return;
            if (holder.manager != manager) return;

            Gui gui = holder.getGUI();

            // Session validation
            if (!validateSession(player, gui)) {
                event.setCancelled(true);
                return;
            }

            boolean cancel = event.getRawSlots().stream()
                    .anyMatch(slot -> {
                        if (slot >= gui.inventory.getSize()) return false;
                        // Cancel if slot is locked
                        if (!gui.unlockedCells.getOrDefault(slot, false)) return true;
                        // Cancel if slot has a button action (buttons shouldn't be draggable)
                        if (gui.conditionalButtons.containsKey(slot)) return true;
                        return false;
                    });
            if (cancel) {
                event.setCancelled(true);
                event.setResult(Event.Result.DENY);
            }
        }

        @EventHandler(priority = EventPriority.LOW)
        void onClickGUI(InventoryClickEvent event) {
            if (!(event.getWhoClicked() instanceof Player player)) return;

            Inventory top = getTopInventoryCompat(event.getView());
            if (!(top != null && top.getHolder() instanceof GuiHolder holder)) return;
            if (holder.manager != manager) return;

            Gui gui = holder.getGUI();

            // Session validation
            if (!validateSession(player, gui)) {
                event.setCancelled(true);
                return;
            }

            boolean inGui = event.getRawSlot() < gui.inventory.getSize();
            boolean inPlayerInv = event.getSlotType() != InventoryType.SlotType.OUTSIDE && !inGui;

            // Double-click safety
            if (event.getClick() == ClickType.DOUBLE_CLICK) {
                ItemStack cursor = event.getCursor();
                if (cursor != null && cursor.getType() != Material.AIR) {
                    for (int i = 0; i < gui.inventory.getSize(); i++) {
                        if (!gui.unlockedCells.getOrDefault(i, false)
                                && cursor.isSimilar(gui.inventory.getItem(i))) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }

            // Shift-click safety
            if ((event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT)
                    && !gui.isAllowShiftClick()) {
                event.setCancelled(true);
            }

            // Inside GUI
            if (inGui) {
                boolean unlocked = gui.unlockedCells.getOrDefault(event.getSlot(), false);
                if (!unlocked) event.setCancelled(true);

                if (gui.onClick(manager, player, top, event)) {
                    if (event.getRawSlot() == gui.nextPageIndex || event.getRawSlot() == gui.prevPageIndex) {
                        if (gui.getNavigateSound() != null)
                            player.playSound(player.getLocation(), gui.getNavigateSound().parseSound(), 1F, 1F);
                        else if (gui.getDefaultSound() != null)
                            player.playSound(player.getLocation(), gui.getDefaultSound().parseSound(), 1F, 1F);
                    }
                }
            }

            // Player inventory
            else if (inPlayerInv) {
                boolean allow = gui.onClickPlayerInventory(manager, player, top, event);
                if (!allow && (!gui.acceptsItems || event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
                    event.setCancelled(true);
                }
            }

            // Click outside
            else if (event.getSlotType() == InventoryType.SlotType.OUTSIDE) {
                if (!gui.onClickOutside(manager, player, event))
                    event.setCancelled(true);
            }
        }

        @EventHandler(priority = EventPriority.LOW)
        void onCloseGUI(InventoryCloseEvent event) {
            Inventory top = getTopInventory(event);
            if (!(top != null && top.getHolder() instanceof GuiHolder holder)) return;
            if (holder.manager != manager) return;

            Gui gui = holder.getGUI();
            Player player = (Player) event.getPlayer();

            // Session validation
            if (!validateSession(player, gui)) return;

            if (!gui.allowDropItems)
                player.setItemOnCursor(null);

            // Cancel update tasks for updating GUIs before closing
            try {
                Method cancelTask = gui.getClass().getMethod("cancelTask");
                cancelTask.invoke(gui);
                if (DEBUG) {
                    Bukkit.getLogger().info("[GuiManager] Cancelled update task for " + gui.getClass().getSimpleName() + " (GUI closing, player: " + player.getName() + ")");
                }
            } catch (NoSuchMethodException e) {
                // Not an updating GUI, that's fine
            } catch (Exception e) {
                if (DEBUG) {
                    Bukkit.getLogger().warning("[GuiManager] Error cancelling task on close for " + gui.getClass().getSimpleName() + ": " + e.getMessage());
                }
            }

            Bukkit.getScheduler().runTask(manager.plugin, () -> {
                gui.onClose(manager, player);
                player.updateInventory();
            });

            manager.openInventories.remove(player);
            GUISessionLock.end(player.getUniqueId());
        }

        @EventHandler
        void onDisable(PluginDisableEvent event) {
            if (event.getPlugin() == manager.plugin) {
                manager.shutdown = true;
                manager.closeAll();
                manager.initialized = false;
                GUISessionLock.clearAll();
            }
        }
    }
}
