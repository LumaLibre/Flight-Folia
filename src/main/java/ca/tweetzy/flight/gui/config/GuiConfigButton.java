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
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a configurable button/item in a GUI config.
 * Immutable for thread safety.
 */
@Getter
public final class GuiConfigButton {

    private final String id;
    private final List<Integer> slots;
    private final String material;
    private final String name;
    private final List<String> lore;
    private final String action;
    private final List<ClickType> clickTypes;
    private final boolean enabled;
    private final String condition;
    private final boolean glow;
    private final Map<String, Object> data;
    private final int amount;
    private final Integer modelData;
    private final List<String> enchantments;

    public GuiConfigButton(
            @NonNull String id,
            @NonNull List<Integer> slots,
            @NonNull String material,
            @Nullable String name,
            @Nullable List<String> lore,
            @Nullable String action,
            @Nullable List<ClickType> clickTypes,
            boolean enabled,
            @Nullable String condition,
            boolean glow,
            @Nullable Map<String, Object> data,
            int amount,
            @Nullable Integer modelData,
            @Nullable List<String> enchantments
    ) {
        this.id = id;
        this.slots = new ArrayList<>(slots);
        this.material = material;
        this.name = name;
        this.lore = lore != null ? new ArrayList<>(lore) : new ArrayList<>();
        this.action = action;
        this.clickTypes = clickTypes != null ? new ArrayList<>(clickTypes) : new ArrayList<>();
        this.enabled = enabled;
        this.condition = condition;
        this.glow = glow;
        this.data = data != null ? new HashMap<>(data) : new HashMap<>();
        this.amount = amount;
        this.modelData = modelData;
        this.enchantments = enchantments != null ? new ArrayList<>(enchantments) : new ArrayList<>();
    }

    /**
     * Get slots as an immutable list
     */
    @NonNull
    public List<Integer> getSlots() {
        return new ArrayList<>(slots);
    }

    /**
     * Get lore as an immutable list
     */
    @NonNull
    public List<String> getLore() {
        return new ArrayList<>(lore);
    }

    /**
     * Get click types as an immutable list
     */
    @NonNull
    public List<ClickType> getClickTypes() {
        return new ArrayList<>(clickTypes);
    }

    /**
     * Get data as an immutable map
     */
    @NonNull
    public Map<String, Object> getData() {
        return new HashMap<>(data);
    }

    /**
     * Get enchantments as an immutable list
     */
    @NonNull
    public List<String> getEnchantments() {
        return new ArrayList<>(enchantments);
    }
}

