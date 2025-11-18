/*
 * Flight
 * Copyright 2023 Kiran Hart
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

import com.cryptomorin.xseries.XEnchantment;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@UtilityClass
public final class ItemUtil {

    /**
     * If the item has a display name, return it, otherwise return the item's type
     *
     * @param itemStack The item stack to get the name of.
     *
     * @return The name of the item.
     */
    public String getItemName(@NonNull final ItemStack itemStack) {
        if (itemStack.hasItemMeta()) {
            ItemMeta meta = itemStack.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                return meta.getDisplayName();
            }
        }
        return ChatUtil.capitalizeFully(itemStack.getType());
    }

    /**
     * If the item has lore, return it, otherwise return an empty list.
     *
     * @param stack The ItemStack to get the lore from.
     *
     * @return The item lore
     */
    public List<String> getItemLore(@NonNull final ItemStack stack) {
        if (!stack.hasItemMeta()) {
            return new ArrayList<>();
        }
        ItemMeta meta = stack.getItemMeta();
        if (meta != null && meta.hasLore()) {
            List<String> existingLore = meta.getLore();
            if (existingLore != null) {
                return new ArrayList<>(existingLore);
            }
        }
        return new ArrayList<>();
    }

    /**
     * It returns a list of all the enchantments on the item
     *
     * @param stack The item stack to get the enchantments from.
     *
     * @return A list of enchants as strings
     */
    public List<String> getItemEnchantments(@NonNull final ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null || !meta.hasEnchants()) {
            return new ArrayList<>();
        }
        Map<Enchantment, Integer> enchants = meta.getEnchants();
        if (enchants.isEmpty()) {
            return new ArrayList<>();
        }
        final List<String> enchantments = new ArrayList<>(enchants.size());
        enchants.forEach((k, i) ->
            enchantments.add(ChatUtil.capitalizeFully(XEnchantment.matchXEnchantment(k).name()))
        );
        return enchantments;
    }
}
