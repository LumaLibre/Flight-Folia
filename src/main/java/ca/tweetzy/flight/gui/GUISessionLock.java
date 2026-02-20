package ca.tweetzy.flight.gui;

import ca.tweetzy.flight.FlightPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks the active server-side Gui instance per player.
 * Prevents old/delayed client packets from interacting with
 * previous GUI instances (UI desync / dupe exploits).
 *
 * Now memory-safe using WeakReferences and supports optional auto-expiry.
 */
public final class GUISessionLock {

    /** Debug flag: set to true to log blocked invalid GUI interactions. */
    private static final boolean DEBUG = false;

    /** How long a session remains valid after opening (in ms). */
    private static final long SESSION_TIMEOUT_MS = 2 * 60 * 1000; // 2 minutes

    private static final Map<UUID, Session> ACTIVE_GUI = new ConcurrentHashMap<>();

    private GUISessionLock() {}

    /** Internal holder class for player GUI session data. */
    private static final class Session {
        final WeakReference<Gui> guiRef;
        final long timestamp;

        Session(Gui gui) {
            this.guiRef = new WeakReference<>(gui);
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > SESSION_TIMEOUT_MS;
        }
    }

    /** Mark this Gui as the current active GUI for the player (overwrites any previous session). */
    public static void start(UUID playerId, Gui gui) {
        if (gui == null) return;
        ACTIVE_GUI.put(playerId, new Session(gui));
    }

    /**
     * Returns true if the given Gui is still the active, valid GUI for the player.
     * Also automatically cleans up expired or garbage-collected sessions.
     */
    public static boolean isValid(UUID playerId, Gui gui) {
        Session session = ACTIVE_GUI.get(playerId);
        if (session == null) return false;

        Gui stored = session.guiRef.get();

        if (stored == null || stored != gui || session.isExpired()) {
            ACTIVE_GUI.remove(playerId); // cleanup stale/expired session
            if (DEBUG && stored != gui) {
                Bukkit.getLogger().info("[GUISessionLock] Invalid or expired GUI packet blocked for " + playerId);
            }
            return false;
        }

        return true;
    }

    /** Ends the player's GUI session (called on GUI close or when a new one opens). */
    public static void end(UUID playerId) {
        ACTIVE_GUI.remove(playerId);
    }

    /** Returns the current GUI for the player, or null if none active. */
    public static Gui get(UUID playerId) {
        Session session = ACTIVE_GUI.get(playerId);
        if (session == null || session.isExpired()) {
            ACTIVE_GUI.remove(playerId);
            return null;
        }
        return session.guiRef.get();
    }

    /** Optional: manually clear all GUI session locks (e.g., on plugin disable). */
    public static void clearAll() {
        ACTIVE_GUI.clear();
    }

    /** Optional debug utility: print all active GUI sessions (safe to call). */
    public static void dumpActiveSessions() {
        if (!DEBUG) return;
        FlightPlugin.getInstance().getScheduler().runAsync((t) -> {
            Bukkit.getLogger().info("[GUISessionLock] === ACTIVE GUI SESSIONS ===");
            ACTIVE_GUI.forEach((uuid, session) -> {
                Player p = Bukkit.getPlayer(uuid);
                String name = p != null ? p.getName() : "Offline";
                Gui g = session.guiRef.get();
                Bukkit.getLogger().info(" - " + name + " → " + (g != null ? g.getClass().getSimpleName() : "null"));
            });
        });
    }
}
