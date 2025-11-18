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

import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a dynamic content section in a GUI config.
 * Handles paged items, templates, and conditional rendering.
 */
@Getter
public final class GuiConfigDynamicSection {

    private final List<GuiConfigDynamicItem> items;
    private final Map<String, GuiConfigTemplate> templates;

    public GuiConfigDynamicSection(
            @Nullable List<GuiConfigDynamicItem> items,
            @Nullable Map<String, GuiConfigTemplate> templates
    ) {
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.templates = templates != null ? new HashMap<>(templates) : new HashMap<>();
    }

    /**
     * Get items as an immutable list
     */
    @NonNull
    public List<GuiConfigDynamicItem> getItems() {
        return new ArrayList<>(items);
    }

    /**
     * Get templates as an immutable map
     */
    @NonNull
    public Map<String, GuiConfigTemplate> getTemplates() {
        return new HashMap<>(templates);
    }

    /**
     * Represents a dynamic item definition
     */
    @Getter
    public static final class GuiConfigDynamicItem {
        private final String type;
        private final String template;
        private final String slots;
        private final String source;
        private final int perPage;
        private final String condition;

        public GuiConfigDynamicItem(
                @NonNull String type,
                @Nullable String template,
                @Nullable String slots,
                @Nullable String source,
                int perPage,
                @Nullable String condition
        ) {
            this.type = type;
            this.template = template;
            this.slots = slots;
            this.source = source;
            this.perPage = perPage;
            this.condition = condition;
        }
    }

    /**
     * Represents a template for dynamic items
     */
    @Getter
    public static final class GuiConfigTemplate {
        private final String material;
        private final String name;
        private final List<String> lore;
        private final String action;
        private final boolean glow;
        private final String condition;
        private final Map<String, Object> data;

        public GuiConfigTemplate(
                @Nullable String material,
                @Nullable String name,
                @Nullable List<String> lore,
                @Nullable String action,
                boolean glow,
                @Nullable String condition,
                @Nullable Map<String, Object> data
        ) {
            this.material = material;
            this.name = name;
            this.lore = lore != null ? new ArrayList<>(lore) : new ArrayList<>();
            this.action = action;
            this.glow = glow;
            this.condition = condition;
            this.data = data != null ? new HashMap<>(data) : new HashMap<>();
        }

        @NonNull
        public List<String> getLore() {
            return new ArrayList<>(lore);
        }

        @NonNull
        public Map<String, Object> getData() {
            return new HashMap<>(data);
        }
    }
}

