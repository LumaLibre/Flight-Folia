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

import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.function.BiFunction;

/**
 * Sign GUI for multi-line text input.
 * 
 * @author Kiran Hart
 */
public class SignGUI implements Listener {
    
    private final Plugin plugin;
    private final Player player;
    private final String[] lines;
    private final BiFunction<Player, String[], Response> onComplete;
    private Location signLocation;
    private boolean opened = false;
    
    private SignGUI(@NonNull Builder builder) {
        this.plugin = builder.plugin;
        this.player = builder.player;
        this.lines = builder.lines != null ? Arrays.copyOf(builder.lines, 4) : new String[4];
        this.onComplete = builder.onComplete;
    }
    
    /**
     * Open the sign GUI
     */
    public void open() {
        if (opened) {
            return;
        }
        
        opened = true;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        
        // Find a suitable location for the sign
        Location playerLoc = player.getLocation();
        signLocation = playerLoc.clone().add(0, 5, 0);
        
        // Create a temporary sign
        Block block = signLocation.getBlock();
        block.setType(Material.OAK_SIGN);
        
        Sign sign = (Sign) block.getState();
        for (int i = 0; i < lines.length && i < 4; i++) {
            sign.setLine(i, lines[i] != null ? lines[i] : "");
        }
        sign.update();
        
        // Send sign edit packet to player (1.20+)
        try {
            // Try to use the new method if available
            player.getClass().getMethod("openSign", org.bukkit.block.Sign.class).invoke(player, sign);
        } catch (Exception e) {
            // Fallback for older versions - this may not work perfectly
            plugin.getLogger().warning("SignGUI may not work properly on this server version");
        }
    }
    
    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if (!event.getPlayer().equals(player)) {
            return;
        }
        
        if (!event.getBlock().getLocation().equals(signLocation)) {
            return;
        }
        
        event.setCancelled(true);
        
        String[] lines = new String[4];
        for (int i = 0; i < 4; i++) {
            lines[i] = event.getLine(i);
        }
        
        Response response = onComplete.apply(player, lines);
        
        // Clean up sign
        signLocation.getBlock().setType(Material.AIR);
        
        if (response == Response.close()) {
            close();
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (event.getPlayer().equals(player)) {
            close();
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getPlayer().equals(player)) {
            return;
        }
        
        if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getClickedBlock() != null && event.getClickedBlock().getLocation().equals(signLocation)) {
                event.setCancelled(true);
            }
        }
    }
    
    /**
     * Close the sign GUI
     */
    public void close() {
        if (opened) {
            opened = false;
            HandlerList.unregisterAll(this);
            
            // Clean up sign
            if (signLocation != null) {
                signLocation.getBlock().setType(Material.AIR);
            }
        }
    }
    
    /**
     * Response type for sign GUI
     */
    public static class Response {
        private final boolean close;
        
        private Response(boolean close) {
            this.close = close;
        }
        
        public static Response close() {
            return new Response(true);
        }
        
        public static Response keepOpen() {
            return new Response(false);
        }
        
        public boolean shouldClose() {
            return close;
        }
    }
    
    /**
     * Builder for SignGUI
     */
    public static class Builder {
        private Plugin plugin;
        private Player player;
        private String[] lines;
        private BiFunction<Player, String[], Response> onComplete;
        
        public Builder plugin(@NonNull Plugin plugin) {
            this.plugin = plugin;
            return this;
        }
        
        public Builder player(@NonNull Player player) {
            this.player = player;
            return this;
        }
        
        public Builder lines(@NonNull String... lines) {
            this.lines = lines;
            return this;
        }
        
        public Builder onComplete(@NonNull BiFunction<Player, String[], Response> onComplete) {
            this.onComplete = onComplete;
            return this;
        }
        
        public SignGUI build() {
            if (plugin == null || player == null || onComplete == null) {
                throw new IllegalStateException("Plugin, player, and onComplete must be set");
            }
            return new SignGUI(this);
        }
        
        public SignGUI open() {
            SignGUI gui = build();
            gui.open();
            return gui;
        }
    }
    
    /**
     * Create a new builder
     */
    public static Builder builder() {
        return new Builder();
    }
}

