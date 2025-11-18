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

package ca.tweetzy.flight.gui.config;

import ca.tweetzy.flight.comp.enums.CompMaterial;
import ca.tweetzy.flight.gui.Gui;
import ca.tweetzy.flight.gui.config.action.GuiConfigActionRegistry;
import ca.tweetzy.flight.gui.methods.Clickable;
import ca.tweetzy.flight.utils.QuickItem;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Renders dynamic content from GUI configs.
 * Handles paged items, templates, and conditional rendering.
 */
public final class GuiConfigDynamicRenderer {

    /**
     * Render dynamic content for a GUI.
     * 
     * @param gui The GUI to render content for
     * @param config The GUI config
     * @param context The GUI config context
     */
    public static void renderDynamicContent(@NonNull Gui gui, @NonNull GuiConfig config, @NonNull GuiConfigContext context) {
        if (config.getDynamic() == null) {
            return;
        }

        GuiConfigDynamicSection dynamic = config.getDynamic();

        for (GuiConfigDynamicSection.GuiConfigDynamicItem itemDef : dynamic.getItems()) {
            renderDynamicItem(gui, itemDef, dynamic, context);
        }
    }

    /**
     * Render a single dynamic item definition.
     */
    private static void renderDynamicItem(
            @NonNull Gui gui,
            @NonNull GuiConfigDynamicSection.GuiConfigDynamicItem itemDef,
            @NonNull GuiConfigDynamicSection dynamic,
            @NonNull GuiConfigContext context
    ) {
        // Check condition
        if (itemDef.getCondition() != null && !itemDef.getCondition().isEmpty()) {
            if (!GuiConfigExpressionEngine.evaluateBoolean(itemDef.getCondition(), context)) {
                return; // Condition not met
            }
        }

        // Get source data
        Object sourceObj = getSourceValue(itemDef.getSource(), context);
        if (sourceObj == null) {
            return;
        }

        // Parse slots
        List<Integer> slots = new ArrayList<>();
        if (itemDef.getSlots() != null && !itemDef.getSlots().isEmpty()) {
            slots = GuiConfigSlotHelper.parseSlots(itemDef.getSlots());
        }

        // Get template
        GuiConfigDynamicSection.GuiConfigTemplate template = null;
        if (itemDef.getTemplate() != null) {
            template = dynamic.getTemplates().get(itemDef.getTemplate());
        }

        if (template == null) {
            return; // No template found
        }

        // Handle different types
        if ("paged".equals(itemDef.getType())) {
            renderPagedItems(gui, sourceObj, template, slots, itemDef.getPerPage(), context);
        } else {
            // Default: render all items
            renderAllItems(gui, sourceObj, template, slots, context);
        }
    }

    /**
     * Render paged items.
     */
    private static void renderPagedItems(
            @NonNull Gui gui,
            @NonNull Object source,
            @NonNull GuiConfigDynamicSection.GuiConfigTemplate template,
            @NonNull List<Integer> slots,
            int perPage,
            @NonNull GuiConfigContext context
    ) {
        List<?> items = convertToList(source);
        if (items == null || items.isEmpty()) {
            return;
        }

        // Calculate pagination
        int currentPage = getPageFromContext(context);
        int startIndex = (currentPage - 1) * perPage;
        int endIndex = Math.min(startIndex + perPage, items.size());

        // Render items for current page
        int slotIndex = 0;
        for (int i = startIndex; i < endIndex && slotIndex < slots.size(); i++) {
            Object item = items.get(i);
            int slot = slots.get(slotIndex);
            
            // Set item in context for template rendering
            context.setVariable("item", item);
            context.setVariable("item_index", i);
            
            ItemStack itemStack = renderTemplate(template, context);
            if (itemStack != null) {
                // Check template condition
                if (template.getCondition() == null || 
                    template.getCondition().isEmpty() ||
                    GuiConfigExpressionEngine.evaluateBoolean(template.getCondition(), context)) {
                    
                    // Set action if specified
                    if (template.getAction() != null && !template.getAction().isEmpty()) {
                        String action = GuiConfigExpressionEngine.resolveVariables(template.getAction(), context);
                        gui.setButton(slot, itemStack, createClickHandler(action, context, item));
                    } else {
                        gui.setItem(slot, itemStack);
                    }
                }
            }
            
            slotIndex++;
        }

        // Clean up context
        context.setVariable("item", null);
        context.setVariable("item_index", null);
    }

    /**
     * Render all items (non-paged).
     */
    private static void renderAllItems(
            @NonNull Gui gui,
            @NonNull Object source,
            @NonNull GuiConfigDynamicSection.GuiConfigTemplate template,
            @NonNull List<Integer> slots,
            @NonNull GuiConfigContext context
    ) {
        List<?> items = convertToList(source);
        if (items == null || items.isEmpty()) {
            return;
        }

        int slotIndex = 0;
        for (Object item : items) {
            if (slotIndex >= slots.size()) {
                break; // No more slots
            }

            int slot = slots.get(slotIndex);
            
            // Set item in context for template rendering
            context.setVariable("item", item);
            context.setVariable("item_index", slotIndex);
            
            ItemStack itemStack = renderTemplate(template, context);
            if (itemStack != null) {
                // Check template condition
                if (template.getCondition() == null || 
                    template.getCondition().isEmpty() ||
                    GuiConfigExpressionEngine.evaluateBoolean(template.getCondition(), context)) {
                    
                    // Set action if specified
                    if (template.getAction() != null && !template.getAction().isEmpty()) {
                        String action = GuiConfigExpressionEngine.resolveVariables(template.getAction(), context);
                        gui.setButton(slot, itemStack, createClickHandler(action, context, item));
                    } else {
                        gui.setItem(slot, itemStack);
                    }
                }
            }
            
            slotIndex++;
        }

        // Clean up context
        context.setVariable("item", null);
        context.setVariable("item_index", null);
    }

    /**
     * Render a template into an ItemStack.
     */
    @Nullable
    private static ItemStack renderTemplate(
            @NonNull GuiConfigDynamicSection.GuiConfigTemplate template,
            @NonNull GuiConfigContext context
    ) {
        Player player = context.getPlayer();
        
        // Parse material
        String materialStr = template.getMaterial();
        if (materialStr != null) {
            materialStr = GuiConfigItemParser.resolveString(materialStr, context);
        }
        
        CompMaterial material = 
            CompMaterial.matchCompMaterial(
                materialStr != null ? materialStr : "STONE"
            ).orElse(CompMaterial.STONE);
        
        QuickItem quickItem = QuickItem.of(material);
        
        // Parse name
        if (template.getName() != null) {
            String name = GuiConfigItemParser.resolveString(template.getName(), context);
            quickItem.name(name);
        }
        
        // Parse lore
        if (!template.getLore().isEmpty()) {
            List<String> lore = new ArrayList<>();
            for (String loreLine : template.getLore()) {
                lore.add(GuiConfigItemParser.resolveString(loreLine, context));
            }
            quickItem.lore(player, lore);
        }
        
        // Parse glow
        if (template.isGlow()) {
            quickItem.glow(true);
        }
        
        return quickItem.make();
    }

    /**
     * Create a click handler for dynamic items.
     */
    @NonNull
    private static Clickable createClickHandler(
            @NonNull String action,
            @NonNull GuiConfigContext context,
            @Nullable Object item
    ) {
        return click -> {
            // Set item in context for action execution
            if (item != null) {
                context.setVariable("item", item);
            }
            
            GuiConfigActionRegistry.executeAction(click, context, action);
            
            // Clean up
            if (item != null) {
                context.setVariable("item", null);
            }
        };
    }

    /**
     * Get source value from context.
     */
    @Nullable
    private static Object getSourceValue(@Nullable String sourcePath, @NonNull GuiConfigContext context) {
        if (sourcePath == null || sourcePath.isEmpty()) {
            return null;
        }
        
        // Resolve path with variables
        sourcePath = GuiConfigExpressionEngine.resolveVariables(sourcePath, context);
        
        // Get from context
        return context.getVariable(sourcePath);
    }

    /**
     * Convert object to list.
     */
    @Nullable
    private static List<?> convertToList(@NonNull Object obj) {
        if (obj instanceof List) {
            return (List<?>) obj;
        } else if (obj instanceof Collection) {
            return new ArrayList<>((Collection<?>) obj);
        } else if (obj instanceof Object[]) {
            return java.util.Arrays.asList((Object[]) obj);
        }
        return null;
    }

    /**
     * Get current page from context.
     */
    private static int getPageFromContext(@NonNull GuiConfigContext context) {
        Object pageObj = context.getVariable("page");
        if (pageObj instanceof Number) {
            return ((Number) pageObj).intValue();
        }
        return 1;
    }
}

