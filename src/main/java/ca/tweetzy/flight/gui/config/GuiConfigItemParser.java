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

import ca.tweetzy.flight.FlightPlugin;
import ca.tweetzy.flight.comp.enums.CompMaterial;
import ca.tweetzy.flight.config.ConfigEntry;
import ca.tweetzy.flight.hooks.PlaceholderAPIHook;
import ca.tweetzy.flight.settings.FlightTranslator;
import ca.tweetzy.flight.settings.TranslationFile;
import ca.tweetzy.flight.utils.Common;
import ca.tweetzy.flight.utils.QuickItem;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses ItemStack configurations from GUI configs.
 * Handles Settings/Translations references, placeholders, and expressions.
 */
public final class GuiConfigItemParser {

    private static final Pattern SETTINGS_PATTERN = Pattern.compile("\\{settings\\.([^}]+)\\}");
    private static final Pattern TRANSLATIONS_PATTERN = Pattern.compile("\\{translations\\.([^}]+)\\}");
    
    /**
     * Parse an ItemStack from a button config.
     * 
     * @param button The button config
     * @param context The GUI config context
     * @return The parsed ItemStack
     */
    @NonNull
    public static ItemStack parseItem(@NonNull GuiConfigButton button, @NonNull GuiConfigContext context) {
        Player player = context.getPlayer();
        
        // Parse material
        String materialStr = resolveSettingsReferences(button.getMaterial(), context);
        CompMaterial material = CompMaterial.matchCompMaterial(materialStr).orElse(CompMaterial.STONE);
        
        QuickItem quickItem = QuickItem.of(material);
        
        // Set amount
        if (button.getAmount() > 1) {
            quickItem.amount(button.getAmount());
        }
        
        // Parse name
        if (button.getName() != null) {
            String name = resolveString(button.getName(), context);
            quickItem.name(name);
        }
        
        // Parse lore
        if (!button.getLore().isEmpty()) {
            List<String> lore = new ArrayList<>();
            for (String loreLine : button.getLore()) {
                lore.add(resolveString(loreLine, context));
            }
            quickItem.lore(player, lore);
        }
        
        // Parse model data
        if (button.getModelData() != null) {
            quickItem.modelData(button.getModelData());
        }
        
        // Parse glow
        if (button.isGlow()) {
            quickItem.glow(true);
        }
        
        // Parse enchantments
        if (!button.getEnchantments().isEmpty()) {
            for (String enchantStr : button.getEnchantments()) {
                // Format: ENCHANTMENT_NAME:LEVEL or just ENCHANTMENT_NAME
                String[] parts = enchantStr.split(":");
                if (parts.length >= 1) {
                    try {
                        // Try to parse as XEnchantment or standard Enchantment
                        // This is a simplified version - full implementation would use XEnchantment
                        // For now, we'll skip enchantment parsing if it fails
                    } catch (Exception e) {
                        // Skip invalid enchantment
                    }
                }
            }
        }
        
        return quickItem.make();
    }

    /**
     * Resolve a string with Settings/Translations references and variables.
     */
    @NonNull
    public static String resolveString(@Nullable String input, @NonNull GuiConfigContext context) {
        if (input == null) {
            return "";
        }

        String result = input;
        
        // Resolve Settings references: {settings.path.to.entry}
        result = resolveSettingsReferences(result, context);
        
        // Resolve Translations references: {translations.path.to.entry}
        result = resolveTranslationsReferences(result, context);
        
        // Resolve variables: ${variable}
        result = GuiConfigExpressionEngine.resolveVariables(result, context);
        
        // Apply PlaceholderAPI if available
        result = PlaceholderAPIHook.tryReplace(context.getPlayer(), result);
        
        // Colorize
        result = Common.colorize(result);
        
        return result;
    }

    /**
     * Resolve Settings references in a string.
     */
    @NonNull
    private static String resolveSettingsReferences(@NonNull String input, @NonNull GuiConfigContext context) {
        String result = input;
        Matcher matcher = SETTINGS_PATTERN.matcher(result);
        
        while (matcher.find()) {
            String settingsPath = matcher.group(1);
            String replacement = getSettingsValue(settingsPath);
            result = result.replace(matcher.group(0), replacement != null ? replacement : "");
        }
        
        return result;
    }

    /**
     * Resolve Translations references in a string.
     */
    @NonNull
    private static String resolveTranslationsReferences(@NonNull String input, @NonNull GuiConfigContext context) {
        String result = input;
        Matcher matcher = TRANSLATIONS_PATTERN.matcher(result);
        
        while (matcher.find()) {
            String translationsPath = matcher.group(1);
            String replacement = getTranslationsValue(translationsPath, context.getPlayer());
            result = result.replace(matcher.group(0), replacement != null ? replacement : "");
        }
        
        return result;
    }

    /**
     * Get a Settings value by path.
     * Uses reflection to find the ConfigEntry in FlightSettings subclasses.
     */
    @Nullable
    private static String getSettingsValue(@NonNull String path) {
        try {
            // Try to get from FlightPlugin's core config
            ConfigEntry entry = FlightPlugin.getCoreConfig().getReadEntry(path);
            if (entry != null) {
                return entry.getString();
            }
        } catch (Exception e) {
            // Settings path not found or error accessing
        }
        
        return null;
    }

    /**
     * Get a Translations value by path.
     * Uses reflection to access translation files since the API is private.
     */
    @Nullable
    private static String getTranslationsValue(@NonNull String path, @NonNull Player player) {
        try {
            // Use reflection to access private translationFiles map
            java.lang.reflect.Field translationFilesField = 
                FlightTranslator.class.getDeclaredField("translationFiles");
            translationFilesField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, TranslationFile> translationFiles = 
                (Map<String, TranslationFile>) translationFilesField.get(null);
            
            // Get main language
            java.lang.reflect.Field mainLanguageField = 
                FlightTranslator.class.getDeclaredField("mainLanguage");
            mainLanguageField.setAccessible(true);
            String mainLanguage = (String) mainLanguageField.get(null);
            
            if (mainLanguage != null && translationFiles != null) {
                TranslationFile translationFile = translationFiles.get(mainLanguage);
                if (translationFile != null) {
                    Object value = translationFile.getOr(path, null);
                    if (value instanceof String) {
                        String content = (String) value;
                        // Apply placeholders
                        content = PlaceholderAPIHook.tryReplace(player, content);
                        return Common.colorize(content);
                    } else if (value instanceof List) {
                        // For lists, join with newlines
                        @SuppressWarnings("unchecked")
                        List<String> list = (List<String>) value;
                        if (!list.isEmpty()) {
                            String content = String.join("\n", list);
                            content = PlaceholderAPIHook.tryReplace(player, content);
                            return Common.colorize(content);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Translation path not found or error accessing via reflection
        }
        
        return null;
    }
    
    /**
     * Get a Translations list value by path.
     */
    @Nullable
    public static List<String> getTranslationsList(@NonNull String path, @NonNull Player player) {
        try {
            // Use reflection to access private translationFiles map
            java.lang.reflect.Field translationFilesField = 
                FlightTranslator.class.getDeclaredField("translationFiles");
            translationFilesField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, TranslationFile> translationFiles = 
                (Map<String, TranslationFile>) translationFilesField.get(null);
            
            // Get main language
            java.lang.reflect.Field mainLanguageField = 
                FlightTranslator.class.getDeclaredField("mainLanguage");
            mainLanguageField.setAccessible(true);
            String mainLanguage = (String) mainLanguageField.get(null);
            
            if (mainLanguage != null && translationFiles != null) {
                TranslationFile translationFile = translationFiles.get(mainLanguage);
                if (translationFile != null) {
                    Object value = translationFile.getOr(path, null);
                    if (value instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<String> list = new ArrayList<>((List<String>) value);
                        // Apply placeholders to each line
                        list.replaceAll(line -> {
                            String processed = PlaceholderAPIHook.tryReplace(player, line);
                            return Common.colorize(processed);
                        });
                        return list;
                    } else if (value instanceof String) {
                        String content = (String) value;
                        content = PlaceholderAPIHook.tryReplace(player, content);
                        return Collections.singletonList(Common.colorize(content));
                    }
                }
            }
        } catch (Exception e) {
            // Translation path not found or error
        }
        
        return null;
    }
}

