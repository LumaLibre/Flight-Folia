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

package ca.tweetzy.flight.gui.helper;

import ca.tweetzy.flight.comp.enums.CompMaterial;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The current file has been created by Kiran Hart
 * Date Created: December 23 2021
 * Time Created: 12:02 a.m.
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise
 */

public final class InventorySafeMaterials {

    /**
     * It creates a temporary inventory, sets the item in the first slot to the material, and if the item is not null, it adds it to the list
     *
     * @return A list of all valid materials in the game.
     */
    public static List<CompMaterial> get() {
        final List<CompMaterial> list = new ArrayList<>();

        final Inventory drawer = Bukkit.createInventory(null, 9, "Valid Materials");

        for (int i = 0; i < CompMaterial.values().length; i++) {
            final CompMaterial material = CompMaterial.values()[i];

            try {
                drawer.setItem(0, material.parseItem());
                if (drawer.getItem(0) != null) {
                    drawer.setItem(0, null);
                    list.add(material);
                }
            } catch (IllegalArgumentException ignored) {
            }

        }

        return list.stream().sorted(Comparator.comparing(CompMaterial::name)).toList();
    }
}
