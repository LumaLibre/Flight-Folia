package ca.tweetzy.flight.utils.input;

import org.bukkit.entity.Player;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks active Input instances per player.
 * Allows GUIs to check if a player has an active input session.
 */
public final class InputSessionLock {

    private static final Map<UUID, WeakReference<Input>> ACTIVE_INPUTS = new ConcurrentHashMap<>();

    private InputSessionLock() {}

    /**
     * Registers an active input for a player.
     * Called automatically by Input constructor.
     */
    public static void start(UUID playerId, Input input) {
        if (input == null) return;
        ACTIVE_INPUTS.put(playerId, new WeakReference<>(input));
    }

    /**
     * Unregisters the active input for a player.
     * Called automatically when Input is closed.
     */
    public static void end(UUID playerId) {
        ACTIVE_INPUTS.remove(playerId);
    }

    /**
     * Returns true if the player currently has an active input session.
     * Automatically cleans up garbage-collected inputs.
     */
    public static boolean hasActiveInput(UUID playerId) {
        WeakReference<Input> ref = ACTIVE_INPUTS.get(playerId);
        if (ref == null) return false;
        
        Input input = ref.get();
        if (input == null) {
            // Input was garbage collected, clean up
            ACTIVE_INPUTS.remove(playerId);
            return false;
        }
        
        return true;
    }

    /**
     * Returns true if the player currently has an active input session.
     * Convenience method that takes a Player object.
     */
    public static boolean hasActiveInput(Player player) {
        return player != null && hasActiveInput(player.getUniqueId());
    }

    /**
     * Gets the active input for a player, or null if none.
     */
    public static Input get(UUID playerId) {
        WeakReference<Input> ref = ACTIVE_INPUTS.get(playerId);
        if (ref == null) return null;
        
        Input input = ref.get();
        if (input == null) {
            // Input was garbage collected, clean up
            ACTIVE_INPUTS.remove(playerId);
            return null;
        }
        
        return input;
    }

    /**
     * Gets the active input for a player, or null if none.
     * Convenience method that takes a Player object.
     */
    public static Input get(Player player) {
        return player != null ? get(player.getUniqueId()) : null;
    }

    /**
     * Clears all active input sessions (e.g., on plugin disable).
     */
    public static void clearAll() {
        ACTIVE_INPUTS.clear();
    }
}

