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

import ca.tweetzy.flight.config.yaml.YamlConfiguration;
import ca.tweetzy.flight.gui.GuiType;
import lombok.NonNull;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Loads and caches GUI config files with inheritance support.
 * Thread-safe for concurrent access.
 */
public final class GuiConfigLoader {

    private final Plugin plugin;
    private final File guisFolder;
    private final Map<String, GuiConfig> configCache = new ConcurrentHashMap<>();
    private final Set<String> loadingConfigs = ConcurrentHashMap.newKeySet(); // Prevent circular dependencies

    public GuiConfigLoader(@NonNull Plugin plugin) {
        this.plugin = plugin;
        this.guisFolder = new File(plugin.getDataFolder(), "guis");
        
        if (!this.guisFolder.exists()) {
            this.guisFolder.mkdirs();
        }
    }

    /**
     * Load a GUI config by name.
     * Configs are cached after first load.
     * 
     * @param configName The name of the config (without .yml extension)
     * @return The loaded config, or null if not found
     */
    @Nullable
    public GuiConfig loadConfig(@NonNull String configName) {
        // Check cache first
        if (configCache.containsKey(configName)) {
            return configCache.get(configName);
        }

        // Prevent circular dependencies
        if (loadingConfigs.contains(configName)) {
            plugin.getLogger().warning("Circular dependency detected in GUI config: " + configName);
            return null;
        }

        loadingConfigs.add(configName);
        
        try {
            File configFile = new File(guisFolder, configName + ".yml");
            
            if (!configFile.exists()) {
                loadingConfigs.remove(configName);
                return null;
            }

            YamlConfiguration yamlConfig = new YamlConfiguration();
            try (java.io.FileReader reader = new java.io.FileReader(configFile, java.nio.charset.StandardCharsets.UTF_8)) {
                yamlConfig.load(reader);
            }

            GuiConfig config = parseConfig(configName, yamlConfig);
            
            if (config != null) {
                configCache.put(configName, config);
            }
            
            loadingConfigs.remove(configName);
            return config;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load GUI config: " + configName, e);
            loadingConfigs.remove(configName);
            return null;
        }
    }

    /**
     * Parse a YAML config into a GuiConfig object.
     */
    @Nullable
    private GuiConfig parseConfig(@NonNull String configName, @NonNull YamlConfiguration yamlConfig) {
        try {
            // Check for inheritance
            Object extendsObj = yamlConfig.get("extends");
            String extendsConfig = extendsObj != null ? extendsObj.toString() : null;
            GuiConfig parentConfig = null;
            
            if (extendsConfig != null && !extendsConfig.isEmpty()) {
                parentConfig = loadConfig(extendsConfig);
            }

            // Parse GUI section
            Object guiSection = yamlConfig.get("gui");
            if (guiSection == null && parentConfig == null) {
                return null; // No GUI section and no parent
            }

            // Merge with parent if exists
            Map<String, Object> guiData = new HashMap<>();
            if (parentConfig != null) {
                // Start with parent data (we'll merge child overrides)
                guiData.putAll(getGuiDataFromConfig(parentConfig));
            }
            
            if (guiSection instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> childData = (Map<String, Object>) guiSection;
                guiData.putAll(childData); // Child overrides parent
            }

            // Parse config
            String title = getString(guiData, "title", parentConfig != null ? parentConfig.getTitle() : null);
            int rows = getInt(guiData, "rows", parentConfig != null ? parentConfig.getRows() : 6);
            GuiType type = parseGuiType(getString(guiData, "type", null));
            if (type == null && parentConfig != null) {
                type = parentConfig.getType();
            }
            if (type == null) {
                type = GuiType.STANDARD;
            }

            // Parse background
            GuiConfig.GuiConfigBackground background = parseBackground(
                (Map<String, Object>) guiData.get("background"),
                parentConfig != null ? parentConfig.getBackground() : null
            );

            // Parse buttons
            Map<String, GuiConfigButton> buttons = parseButtons(
                (Map<String, Object>) guiData.get("buttons"),
                parentConfig != null ? parentConfig.getButtons() : null
            );

            // Parse dynamic section
            GuiConfigDynamicSection dynamic = parseDynamicSection(
                (Map<String, Object>) guiData.get("dynamic"),
                parentConfig != null ? parentConfig.getDynamic() : null
            );

            // Parse variables
            Map<String, String> variables = parseVariables(
                (Map<String, Object>) guiData.get("variables"),
                parentConfig != null ? parentConfig.getVariables() : null
            );

            return new GuiConfig(
                configName,
                extendsConfig,
                title,
                rows,
                type,
                background,
                buttons,
                dynamic,
                variables
            );
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to parse GUI config: " + configName, e);
            return null;
        }
    }

    @NonNull
    private Map<String, Object> getGuiDataFromConfig(@NonNull GuiConfig config) {
        Map<String, Object> data = new HashMap<>();
        if (config.getTitle() != null) data.put("title", config.getTitle());
        data.put("rows", config.getRows());
        data.put("type", config.getType().name());
        // Note: We don't reconstruct full structure, just what we need for merging
        return data;
    }

    @Nullable
    private GuiConfig.GuiConfigBackground parseBackground(@Nullable Map<String, Object> backgroundData, @Nullable GuiConfig.GuiConfigBackground parent) {
        if (backgroundData == null) {
            return parent;
        }

        String material = getString(backgroundData, "material", parent != null ? parent.getMaterial() : null);
        String name = getString(backgroundData, "name", parent != null ? parent.getName() : null);
        boolean enabled = getBoolean(backgroundData, "enabled", parent == null || parent.isEnabled());
        String slots = getString(backgroundData, "slots", parent != null ? parent.getSlots() : null);

        return new GuiConfig.GuiConfigBackground(material, name, enabled, slots);
    }

    @NonNull
    private Map<String, GuiConfigButton> parseButtons(@Nullable Map<String, Object> buttonsData, @Nullable Map<String, GuiConfigButton> parentButtons) {
        Map<String, GuiConfigButton> buttons = new HashMap<>();
        
        // Start with parent buttons
        if (parentButtons != null) {
            buttons.putAll(parentButtons);
        }

        if (buttonsData == null) {
            return buttons;
        }

        for (Map.Entry<String, Object> entry : buttonsData.entrySet()) {
            String buttonId = entry.getKey();
            Object buttonData = entry.getValue();

            if (!(buttonData instanceof Map)) {
                continue;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> buttonMap = (Map<String, Object>) buttonData;

            // Get parent button if exists
            GuiConfigButton parentButton = buttons.get(buttonId);

            // Parse slots
            List<Integer> slots = parseSlots(buttonMap.get("slots"), parentButton);
            
            String material = getString(buttonMap, "material", parentButton != null ? parentButton.getMaterial() : "STONE");
            String name = getString(buttonMap, "name", parentButton != null ? parentButton.getName() : null);
            List<String> lore = getStringList(buttonMap, "lore", parentButton != null ? parentButton.getLore() : null);
            String action = getString(buttonMap, "action", parentButton != null ? parentButton.getAction() : null);
            List<ClickType> clickTypes = parseClickTypes(buttonMap.get("click-types"), parentButton);
            boolean enabled = getBoolean(buttonMap, "enabled", parentButton == null || parentButton.isEnabled());
            String condition = getString(buttonMap, "condition", parentButton != null ? parentButton.getCondition() : null);
            boolean glow = getBoolean(buttonMap, "glow", parentButton != null && parentButton.isGlow());
            Map<String, Object> data = getMap(buttonMap, "data", parentButton != null ? parentButton.getData() : null);
            int amount = getInt(buttonMap, "amount", parentButton != null ? parentButton.getAmount() : 1);
            Integer modelData = getIntOrNull(buttonMap, "model-data", parentButton != null ? parentButton.getModelData() : null);
            List<String> enchantments = getStringList(buttonMap, "enchantments", parentButton != null ? parentButton.getEnchantments() : null);

            buttons.put(buttonId, new GuiConfigButton(
                buttonId,
                slots,
                material,
                name,
                lore,
                action,
                clickTypes,
                enabled,
                condition,
                glow,
                data,
                amount,
                modelData,
                enchantments
            ));
        }

        return buttons;
    }

    @NonNull
    private List<Integer> parseSlots(@Nullable Object slotsObj, @Nullable GuiConfigButton parent) {
        if (slotsObj == null) {
            if (parent != null) {
                return parent.getSlots();
            }
            return new ArrayList<>();
        }

        if (slotsObj instanceof List) {
            return GuiConfigSlotHelper.parseSlotsFromList((List<?>) slotsObj);
        } else if (slotsObj instanceof String) {
            return GuiConfigSlotHelper.parseSlots((String) slotsObj);
        } else if (slotsObj instanceof Number) {
            return Collections.singletonList(((Number) slotsObj).intValue());
        }

        return new ArrayList<>();
    }

    @NonNull
    private List<ClickType> parseClickTypes(@Nullable Object clickTypesObj, @Nullable GuiConfigButton parent) {
        if (clickTypesObj == null) {
            if (parent != null) {
                return parent.getClickTypes();
            }
            return new ArrayList<>();
        }

        List<ClickType> clickTypes = new ArrayList<>();
        
        if (clickTypesObj instanceof List) {
            for (Object obj : (List<?>) clickTypesObj) {
                if (obj instanceof String) {
                    try {
                        clickTypes.add(ClickType.valueOf(((String) obj).toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        // Invalid click type, skip
                    }
                }
            }
        } else if (clickTypesObj instanceof String) {
            try {
                clickTypes.add(ClickType.valueOf(((String) clickTypesObj).toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Invalid click type
            }
        }

        return clickTypes;
    }

    @Nullable
    private GuiConfigDynamicSection parseDynamicSection(@Nullable Map<String, Object> dynamicData, @Nullable GuiConfigDynamicSection parent) {
        if (dynamicData == null) {
            return parent;
        }

        List<GuiConfigDynamicSection.GuiConfigDynamicItem> items = new ArrayList<>();
        Map<String, GuiConfigDynamicSection.GuiConfigTemplate> templates = new HashMap<>();

        // Merge with parent
        if (parent != null) {
            items.addAll(parent.getItems());
            templates.putAll(parent.getTemplates());
        }

        // Parse items
        Object itemsObj = dynamicData.get("items");
        if (itemsObj instanceof List) {
            for (Object itemObj : (List<?>) itemsObj) {
                if (itemObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> itemMap = (Map<String, Object>) itemObj;
                    String type = getString(itemMap, "type", "paged");
                    String template = getString(itemMap, "template", null);
                    String slots = getString(itemMap, "slots", null);
                    String source = getString(itemMap, "source", null);
                    int perPage = getInt(itemMap, "per-page", 45);
                    String condition = getString(itemMap, "condition", null);
                    
                    items.add(new GuiConfigDynamicSection.GuiConfigDynamicItem(type, template, slots, source, perPage, condition));
                }
            }
        }

        // Parse templates
        Object templatesObj = dynamicData.get("templates");
        if (templatesObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> templatesMap = (Map<String, Object>) templatesObj;
            
            for (Map.Entry<String, Object> entry : templatesMap.entrySet()) {
                String templateId = entry.getKey();
                Object templateObj = entry.getValue();
                
                if (templateObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> templateMap = (Map<String, Object>) templateObj;
                    
                    String material = getString(templateMap, "material", null);
                    String name = getString(templateMap, "name", null);
                    List<String> lore = getStringList(templateMap, "lore", null);
                    String action = getString(templateMap, "action", null);
                    boolean glow = getBoolean(templateMap, "glow", false);
                    String condition = getString(templateMap, "condition", null);
                    Map<String, Object> data = getMap(templateMap, "data", null);
                    
                    templates.put(templateId, new GuiConfigDynamicSection.GuiConfigTemplate(
                        material, name, lore, action, glow, condition, data
                    ));
                }
            }
        }

        return new GuiConfigDynamicSection(items, templates);
    }

    @NonNull
    private Map<String, String> parseVariables(@Nullable Map<String, Object> variablesData, @Nullable Map<String, String> parentVariables) {
        Map<String, String> variables = new HashMap<>();
        
        if (parentVariables != null) {
            variables.putAll(parentVariables);
        }

        if (variablesData != null) {
            for (Map.Entry<String, Object> entry : variablesData.entrySet()) {
                variables.put(entry.getKey(), entry.getValue().toString());
            }
        }

        return variables;
    }

    @Nullable
    private GuiType parseGuiType(@Nullable String typeStr) {
        if (typeStr == null) {
            return null;
        }
        
        try {
            return GuiType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // Helper methods for safe type extraction
    @Nullable
    private String getString(@NonNull Map<String, Object> map, @NonNull String key, @Nullable String defaultValue) {
        Object value = map.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    private int getInt(@NonNull Map<String, Object> map, @NonNull String key, int defaultValue) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    @Nullable
    private Integer getIntOrNull(@NonNull Map<String, Object> map, @NonNull String key, @Nullable Integer defaultValue) {
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private boolean getBoolean(@NonNull Map<String, Object> map, @NonNull String key, boolean defaultValue) {
        Object value = map.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return defaultValue;
    }

    @Nullable
    private List<String> getStringList(@NonNull Map<String, Object> map, @NonNull String key, @Nullable List<String> defaultValue) {
        Object value = map.get(key);
        if (value instanceof List) {
            List<String> result = new ArrayList<>();
            for (Object obj : (List<?>) value) {
                result.add(obj.toString());
            }
            return result;
        }
        return defaultValue;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private Map<String, Object> getMap(@NonNull Map<String, Object> map, @NonNull String key, @Nullable Map<String, Object> defaultValue) {
        Object value = map.get(key);
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return defaultValue;
    }

    /**
     * Clear the config cache.
     */
    public void clearCache() {
        configCache.clear();
    }

    /**
     * Reload a specific config.
     */
    @Nullable
    public GuiConfig reloadConfig(@NonNull String configName) {
        configCache.remove(configName);
        return loadConfig(configName);
    }
}

