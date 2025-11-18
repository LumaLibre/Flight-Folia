package ca.tweetzy.flight.gui.template;

import ca.tweetzy.flight.FlightPlugin;
import ca.tweetzy.flight.gui.Gui;
import ca.tweetzy.flight.gui.config.GuiConfigLoader;
import ca.tweetzy.flight.utils.Common;
import ca.tweetzy.flight.utils.QuickItem;
import ca.tweetzy.flight.comp.enums.CompMaterial;
import lombok.NonNull;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Modern base GUI class
 */
public abstract class BaseGUI extends Gui {

    private final Gui parent;

    public BaseGUI(@NonNull String title, int rows) {
        this(null, title, rows);
    }

    public BaseGUI(Gui parent, @NonNull String title, int rows) {
        super(rows, parent);
        this.parent = parent;
        setTitle(Common.colorize(title));
        setDefaultSound(null);
        setDefaultItem(QuickItem.of(CompMaterial.BLACK_STAINED_GLASS_PANE).name(" ").make());
    }

    public BaseGUI(@NonNull String title) {
        this(null, title, 6);
    }

    /**
     * Draw the GUI content (must implement)
     */
    protected abstract void draw();

    /**
     * Adds a back or exit button depending on whether parent exists
     */
    protected void applyBackExit() {
        if (parent == null) {
            setButton(getBackExitButtonSlot(), getExitButton(), click -> click.gui.close());
        } else {
            setButton(getBackExitButtonSlot(), getBackButton(), click -> click.manager.showGUI(click.player, parent));
        }
    }

    /**
     * Overrides back button to show a specific GUI
     */
    protected void applyBackExit(Gui override) {
        setButton(getBackExitButtonSlot(), getBackButton(), click -> click.manager.showGUI(click.player, override));
    }

    /**
     * Returns all slots to fill with content
     */
    protected List<Integer> fillSlots() {
        return IntStream.rangeClosed(0, 44).boxed().toList();
    }

    // Abstract button definitions
    protected abstract ItemStack getBackButton();
    protected abstract ItemStack getExitButton();
    protected abstract ItemStack getPreviousButton();
    protected abstract ItemStack getNextButton();
    protected abstract int getPreviousButtonSlot();
    protected abstract int getNextButtonSlot();

    protected int getBackExitButtonSlot() {
        return rows * 9 - 9;
    }

    /**
     * Load configuration from a config file.
     * This will automatically set up the config loader if not already set.
     * 
     * @param configName The name of the config file (without .yml extension)
     * @return true if config was loaded successfully, false otherwise
     */
    public boolean loadFromConfig(@NonNull String configName) {
        // Try to get plugin from FlightPlugin
        JavaPlugin plugin = null;
        try {
            plugin = FlightPlugin.getInstance();
        } catch (Exception e) {
            // Not a FlightPlugin, try to get from GuiManager
            if (guiManager != null && guiManager.getPlugin() instanceof JavaPlugin) {
                plugin = (JavaPlugin) guiManager.getPlugin();
            }
        }

        if (plugin == null) {
            return false;
        }

        // Set config loader if not already set
        if (configLoader == null) {
            setConfigLoader(new GuiConfigLoader(plugin));
        }

        return super.loadFromConfig(configName);
    }

    /**
     * Load configuration from a config file with a specific plugin.
     * Useful when the GUI is not part of a FlightPlugin.
     * 
     * @param plugin The plugin instance
     * @param configName The name of the config file (without .yml extension)
     * @return true if config was loaded successfully, false otherwise
     */
    protected boolean loadFromConfig(@NonNull JavaPlugin plugin, @NonNull String configName) {
        if (configLoader == null) {
            setConfigLoader(new GuiConfigLoader(plugin));
        }

        return super.loadFromConfig(configName);
    }
}
