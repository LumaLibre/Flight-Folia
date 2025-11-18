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

import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for parsing and calculating slot ranges from config strings.
 * Supports formats like: "0-44", "45,47,49", "0-8,9-17", etc.
 */
public final class GuiConfigSlotHelper {

    /**
     * Parse a slot string into a list of slot numbers.
     * Supports:
     * - Single slots: "45"
     * - Ranges: "0-44"
     * - Multiple slots: "45,47,49"
     * - Mixed: "0-8,9-17,45,47"
     *
     * @param slotString The slot string to parse
     * @return List of slot numbers
     */
    @NonNull
    public static List<Integer> parseSlots(@NonNull String slotString) {
        List<Integer> slots = new ArrayList<>();
        
        if (slotString.trim().isEmpty()) {
            return slots;
        }

        // Split by comma to handle multiple ranges/slots
        String[] parts = slotString.split(",");
        
        for (String part : parts) {
            part = part.trim();
            
            if (part.contains("-")) {
                // It's a range
                String[] range = part.split("-");
                if (range.length == 2) {
                    try {
                        int start = Integer.parseInt(range[0].trim());
                        int end = Integer.parseInt(range[1].trim());
                        
                        // Ensure start <= end
                        if (start > end) {
                            int temp = start;
                            start = end;
                            end = temp;
                        }
                        
                        // Add all slots in range
                        for (int i = start; i <= end; i++) {
                            slots.add(i);
                        }
                    } catch (NumberFormatException e) {
                        // Invalid range, skip
                    }
                }
            } else {
                // It's a single slot
                try {
                    int slot = Integer.parseInt(part);
                    slots.add(slot);
                } catch (NumberFormatException e) {
                    // Invalid slot, skip
                }
            }
        }
        
        return slots;
    }

    /**
     * Parse slots from a list of objects (from YAML config).
     * Handles both string and integer values.
     *
     * @param slotList List of slot values (can be String or Integer)
     * @return List of slot numbers
     */
    @NonNull
    public static List<Integer> parseSlotsFromList(@NonNull List<?> slotList) {
        List<Integer> slots = new ArrayList<>();
        
        for (Object obj : slotList) {
            if (obj instanceof Integer) {
                slots.add((Integer) obj);
            } else if (obj instanceof String) {
                slots.addAll(parseSlots((String) obj));
            } else if (obj instanceof Number) {
                slots.add(((Number) obj).intValue());
            }
        }
        
        return slots;
    }
}

