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

package ca.tweetzy.flight.utils;

import ca.tweetzy.flight.utils.colors.ColorFormatter;
import lombok.NonNull;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Modern message builder with Adventure API support and legacy fallback.
 * 
 * Automatically uses Adventure API when available (Paper/Adventure plugin),
 * otherwise falls back to legacy BungeeCord chat components.
 * 
 * @author Kiran Hart
 */
public class ComponentMessage {
    
    private static final boolean ADVENTURE_AVAILABLE;
    private static final Class<?> COMPONENT_CLASS;
    private static final Class<?> AUDIENCE_CLASS;
    private static final Class<?> CLICK_EVENT_CLASS;
    private static final Class<?> HOVER_EVENT_CLASS;
    private static final Class<?> TEXT_COMPONENT_CLASS;
    
    static {
        boolean adventure = false;
        Class<?> component = null;
        Class<?> audience = null;
        Class<?> clickEvent = null;
        Class<?> hoverEvent = null;
        Class<?> textComponent = null;
        
        try {
            // Check for Adventure API
            component = Class.forName("net.kyori.adventure.text.Component");
            audience = Class.forName("net.kyori.adventure.audience.Audience");
            clickEvent = Class.forName("net.kyori.adventure.text.event.ClickEvent");
            hoverEvent = Class.forName("net.kyori.adventure.text.event.HoverEvent");
            textComponent = Class.forName("net.kyori.adventure.text.TextComponent");
            adventure = true;
        } catch (ClassNotFoundException e) {
            // Adventure API not available, use legacy
        }
        
        ADVENTURE_AVAILABLE = adventure;
        COMPONENT_CLASS = component;
        AUDIENCE_CLASS = audience;
        CLICK_EVENT_CLASS = clickEvent;
        HOVER_EVENT_CLASS = hoverEvent;
        TEXT_COMPONENT_CLASS = textComponent;
    }
    
    private final List<MessagePart> parts = new ArrayList<>();
    
    private ComponentMessage() {}
    
    /**
     * Create a new ComponentMessage builder
     */
    public static ComponentMessage create() {
        return new ComponentMessage();
    }
    
    /**
     * Add plain text to the message
     */
    public ComponentMessage text(@NonNull String text) {
        parts.add(new MessagePart(text, null, null, null, null));
        return this;
    }
    
    /**
     * Add text with a click action
     */
    public ComponentMessage text(@NonNull String text, @NonNull ClickAction action, @NonNull String value) {
        parts.add(new MessagePart(text, action, value, null, null));
        return this;
    }
    
    /**
     * Add text with hover text
     */
    public ComponentMessage hover(@NonNull String hoverText) {
        if (!parts.isEmpty()) {
            parts.get(parts.size() - 1).hoverText = hoverText;
        }
        return this;
    }
    
    /**
     * Add text with hover item
     */
    public ComponentMessage hoverItem(@NonNull ItemStack item) {
        if (!parts.isEmpty()) {
            parts.get(parts.size() - 1).hoverItem = item;
        }
        return this;
    }
    
    /**
     * Add text with both click action and hover text
     */
    public ComponentMessage text(@NonNull String text, @NonNull ClickAction action, @NonNull String value, @NonNull String hoverText) {
        parts.add(new MessagePart(text, action, value, hoverText, null));
        return this;
    }
    
    /**
     * Add a newline
     */
    public ComponentMessage newline() {
        parts.add(new MessagePart("\n", null, null, null, null));
        return this;
    }
    
    /**
     * Send the message to a CommandSender
     */
    public void send(@NonNull CommandSender sender) {
        if (ADVENTURE_AVAILABLE && sender instanceof Player) {
            sendAdventure((Player) sender);
        } else {
            sendLegacy(sender);
        }
    }
    
    /**
     * Send the message to multiple CommandSenders
     */
    public void send(@NonNull CommandSender... senders) {
        for (CommandSender sender : senders) {
            send(sender);
        }
    }
    
    /**
     * Send the message to a list of CommandSenders
     */
    public void send(@NonNull List<? extends CommandSender> senders) {
        for (CommandSender sender : senders) {
            send(sender);
        }
    }
    
    /**
     * Send using Adventure API (Paper/Adventure plugin)
     */
    private void sendAdventure(@NonNull Player player) {
        try {
            // Get Adventure API methods via reflection
            Object component = buildAdventureComponent();
            
            // Get player as Audience
            Object audience = getAdventureAudience(player);
            
            // Send message
            audience.getClass().getMethod("sendMessage", COMPONENT_CLASS).invoke(audience, component);
        } catch (Exception e) {
            // Fallback to legacy if Adventure API fails
            sendLegacy(player);
        }
    }
    
    /**
     * Build Adventure API component
     */
    private Object buildAdventureComponent() throws Exception {
        List<Object> children = new ArrayList<>();
        
        for (MessagePart part : parts) {
            String processedText = part.text != null ? ColorFormatter.process(part.text) : "";
            Object textComponent = TEXT_COMPONENT_CLASS.getMethod("text", String.class).invoke(null, processedText);
            
            // Apply click event
            if (part.clickAction != null && part.clickValue != null) {
                Object clickEvent = createAdventureClickEvent(part.clickAction, part.clickValue);
                textComponent.getClass().getMethod("clickEvent", CLICK_EVENT_CLASS).invoke(textComponent, clickEvent);
            }
            
            // Apply hover event
            if (part.hoverText != null) {
                Object hoverEvent = createAdventureHoverEvent(part.hoverText);
                textComponent.getClass().getMethod("hoverEvent", HOVER_EVENT_CLASS).invoke(textComponent, hoverEvent);
            } else if (part.hoverItem != null) {
                Object hoverEvent = createAdventureHoverEventItem(part.hoverItem);
                textComponent.getClass().getMethod("hoverEvent", HOVER_EVENT_CLASS).invoke(textComponent, hoverEvent);
            }
            
            children.add(textComponent);
        }
        
        // Build final component with children
        if (children.size() == 1) {
            return children.get(0);
        } else {
            // Use Component.join() or build with children
            Object joinSeparator = TEXT_COMPONENT_CLASS.getMethod("empty").invoke(null);
            return COMPONENT_CLASS.getMethod("join", COMPONENT_CLASS, List.class)
                .invoke(null, joinSeparator, children);
        }
    }
    
    /**
     * Create Adventure API click event
     */
    private Object createAdventureClickEvent(@NonNull ClickAction action, @NonNull String value) throws Exception {
        Class<?> actionTypeClass = CLICK_EVENT_CLASS.getDeclaredClasses()[0];
        Object actionType = actionTypeClass.getField(action.getAdventureName()).get(null);
        String safeValue = value != null ? value : "";
        return CLICK_EVENT_CLASS.getConstructor(
            actionTypeClass,
            String.class
        ).newInstance(actionType, safeValue);
    }
    
    /**
     * Create Adventure API hover event (text)
     */
    private Object createAdventureHoverEvent(@NonNull String text) throws Exception {
        Class<?> actionTypeClass = HOVER_EVENT_CLASS.getDeclaredClasses()[0];
        Object actionType = actionTypeClass.getField("SHOW_TEXT").get(null);
        String processedText = text != null ? ColorFormatter.process(text) : "";
        Object component = TEXT_COMPONENT_CLASS.getMethod("text", String.class)
            .invoke(null, processedText);
        return HOVER_EVENT_CLASS.getConstructor(
            actionTypeClass,
            COMPONENT_CLASS
        ).newInstance(actionType, component);
    }
    
    /**
     * Create Adventure API hover event (item)
     */
    private Object createAdventureHoverEventItem(@NonNull ItemStack item) throws Exception {
        // This is more complex and requires NMS/item serialization
        // For now, fall back to text hover
        return createAdventureHoverEvent(ItemUtil.getItemName(item));
    }
    
    /**
     * Get Adventure API Audience from Player
     */
    private Object getAdventureAudience(@NonNull Player player) throws Exception {
        // Paper provides Player.getAudience() or similar
        try {
            return player.getClass().getMethod("getAudience").invoke(player);
        } catch (NoSuchMethodException e) {
            // Try alternative method
            return AUDIENCE_CLASS.cast(player);
        }
    }
    
    /**
     * Send using legacy BungeeCord chat components
     */
    private void sendLegacy(@NonNull CommandSender sender) {
        ComponentBuilder builder = new ComponentBuilder();
        
        for (MessagePart part : parts) {
            TextComponent component = new TextComponent(ColorFormatter.process(part.text));
            
            // Apply click event
            if (part.clickAction != null && part.clickValue != null) {
                component.setClickEvent(new ClickEvent(
                    ClickEvent.Action.valueOf(part.clickAction.name()),
                    part.clickValue
                ));
            }
            
            // Apply hover event
            if (part.hoverText != null) {
                String processedHover = ColorFormatter.process(part.hoverText);
                BaseComponent[] hoverComponents = new ComponentBuilder(processedHover).create();
                component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverComponents));
            } else if (part.hoverItem != null) {
                // Legacy item hover (requires NMS)
                // For now, use text hover as fallback
                String itemName = ItemUtil.getItemName(part.hoverItem);
                BaseComponent[] hoverComponents = new ComponentBuilder(itemName != null ? itemName : "").create();
                component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverComponents));
            }
            
            builder.append(component);
        }
        
        if (sender instanceof Player) {
            ((Player) sender).spigot().sendMessage(builder.create());
        } else {
            // Console doesn't support components, send plain text
            sender.sendMessage(toPlainText());
        }
    }
    
    /**
     * Convert to plain text (for console/fallback)
     */
    public String toPlainText() {
        StringBuilder sb = new StringBuilder();
        for (MessagePart part : parts) {
            String text = part.text != null ? part.text : "";
            sb.append(ColorFormatter.process(text));
        }
        return sb.toString();
    }
    
    /**
     * Check if Adventure API is available
     */
    public static boolean isAdventureAvailable() {
        return ADVENTURE_AVAILABLE;
    }
    
    /**
     * Click action types
     */
    public enum ClickAction {
        RUN_COMMAND("run_command"),
        SUGGEST_COMMAND("suggest_command"),
        OPEN_URL("open_url"),
        COPY_TO_CLIPBOARD("copy_to_clipboard"),
        CHANGE_PAGE("change_page");
        
        private final String adventureName;
        
        ClickAction(String adventureName) {
            this.adventureName = adventureName;
        }
        
        public String getAdventureName() {
            return adventureName.toUpperCase();
        }
    }
    
    /**
     * Internal message part
     */
    private static class MessagePart {
        final String text;
        final ClickAction clickAction;
        final String clickValue;
        String hoverText;
        ItemStack hoverItem;
        
        MessagePart(String text, ClickAction clickAction, String clickValue, String hoverText, ItemStack hoverItem) {
            this.text = text;
            this.clickAction = clickAction;
            this.clickValue = clickValue;
            this.hoverText = hoverText;
            this.hoverItem = hoverItem;
        }
    }
}

