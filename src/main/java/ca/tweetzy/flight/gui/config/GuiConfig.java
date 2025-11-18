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

import ca.tweetzy.flight.gui.GuiType;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a complete GUI configuration.
 * Immutable for thread safety.
 */
@Getter
public final class GuiConfig {

    private final String name;
    private final String extendsConfig;
    private final String title;
    private final int rows;
    private final GuiType type;
    private final GuiConfigBackground background;
    private final Map<String, GuiConfigButton> buttons;
    private final GuiConfigDynamicSection dynamic;
    private final Map<String, String> variables;

    public GuiConfig(
            @NonNull String name,
            @Nullable String extendsConfig,
            @Nullable String title,
            int rows,
            @Nullable GuiType type,
            @Nullable GuiConfigBackground background,
            @Nullable Map<String, GuiConfigButton> buttons,
            @Nullable GuiConfigDynamicSection dynamic,
            @Nullable Map<String, String> variables
    ) {
        this.name = name;
        this.extendsConfig = extendsConfig;
        this.title = title;
        this.rows = rows;
        this.type = type != null ? type : GuiType.STANDARD;
        this.background = background;
        this.buttons = buttons != null ? new HashMap<>(buttons) : new HashMap<>();
        this.dynamic = dynamic;
        this.variables = variables != null ? new HashMap<>(variables) : new HashMap<>();
    }

    /**
     * Get buttons as an immutable map
     */
    @NonNull
    public Map<String, GuiConfigButton> getButtons() {
        return new HashMap<>(buttons);
    }

    /**
     * Get variables as an immutable map
     */
    @NonNull
    public Map<String, String> getVariables() {
        return new HashMap<>(variables);
    }

    /**
     * Represents background configuration
     */
    @Getter
    public static final class GuiConfigBackground {
        private final String material;
        private final String name;
        private final boolean enabled;
        private final String slots;

        public GuiConfigBackground(
                @Nullable String material,
                @Nullable String name,
                boolean enabled,
                @Nullable String slots
        ) {
            this.material = material;
            this.name = name;
            this.enabled = enabled;
            this.slots = slots;
        }
    }
}

