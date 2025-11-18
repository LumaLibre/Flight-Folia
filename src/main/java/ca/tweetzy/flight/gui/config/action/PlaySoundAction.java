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

package ca.tweetzy.flight.gui.config.action;

import ca.tweetzy.flight.comp.enums.CompSound;
import ca.tweetzy.flight.gui.config.GuiConfigContext;
import ca.tweetzy.flight.gui.config.GuiConfigExpressionEngine;
import ca.tweetzy.flight.gui.events.GuiClickEvent;
import lombok.NonNull;

import java.util.Map;

/**
 * Built-in action handler for playing sounds.
 * Usage: play_sound(UI_BUTTON_CLICK, volume=1.0, pitch=1.0)
 */
public final class PlaySoundAction implements GuiConfigActionHandler {

    @Override
    public boolean execute(@NonNull GuiClickEvent event, @NonNull GuiConfigContext context, @NonNull String actionString, @NonNull Map<String, String> parameters) {
        String soundName = parameters.getOrDefault("sound", "");
        
        if (soundName.isEmpty()) {
            // Try to extract from action string: play_sound(UI_BUTTON_CLICK)
            int start = actionString.indexOf('(');
            int end = actionString.indexOf(')');
            if (start > 0 && end > start) {
                String args = actionString.substring(start + 1, end);
                // Find first comma
                int commaIndex = args.indexOf(',');
                if (commaIndex > 0) {
                    soundName = args.substring(0, commaIndex).trim();
                } else {
                    soundName = args.trim();
                }
            }
        }
        
        if (soundName.isEmpty()) {
            return false;
        }
        
        // Resolve sound name with variables
        soundName = GuiConfigExpressionEngine.resolveVariables(soundName, context);
        
        // Parse volume and pitch
        float volume = Float.parseFloat(parameters.getOrDefault("volume", "1.0"));
        float pitch = Float.parseFloat(parameters.getOrDefault("pitch", "1.0"));
        
        // Try to match CompSound
        CompSound sound = CompSound.matchCompSound(soundName).orElse(CompSound.UI_BUTTON_CLICK);
        
        // Play sound
        event.player.playSound(event.player.getLocation(), sound.parseSound(), volume, pitch);
        
        return true;
    }
}

