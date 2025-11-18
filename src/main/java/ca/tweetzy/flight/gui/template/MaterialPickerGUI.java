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

package ca.tweetzy.flight.gui.template;

import ca.tweetzy.flight.FlightPlugin;
import ca.tweetzy.flight.comp.enums.CompMaterial;
import ca.tweetzy.flight.gui.Gui;
import ca.tweetzy.flight.gui.events.GuiClickEvent;
import ca.tweetzy.flight.gui.helper.InventoryBorder;
import ca.tweetzy.flight.gui.helper.InventorySafeMaterials;
import ca.tweetzy.flight.utils.ChatUtil;
import ca.tweetzy.flight.utils.Common;
import ca.tweetzy.flight.utils.Filterer;
import ca.tweetzy.flight.utils.Inflector;
import ca.tweetzy.flight.utils.QuickItem;
import ca.tweetzy.flight.utils.input.TitleInput;
import com.cryptomorin.xseries.XEnchantment;
import lombok.NonNull;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Date Created: April 20 2022
 * Time Created: 2:02 p.m.
 *
 * @author Kiran Hart
 */
public class MaterialPickerGUI extends BaseGUI {

    private final Gui parent;
    private final String inputTitle, inputSubtitle;
    private final String searchQuery;
    protected final BiConsumer<GuiClickEvent, ItemStack> selected;


    public MaterialPickerGUI(final Gui parent, final String titleOverride, final String searchQuery, final String inputTitle, final String inputSubtitle, @NonNull final BiConsumer<GuiClickEvent, ItemStack> selected) {
        super(parent, titleOverride == null ? "&eMaterial Selector" : titleOverride, 6);
        this.searchQuery = searchQuery;
        this.inputTitle = inputTitle;
        this.inputSubtitle = inputSubtitle;
        this.selected = selected;
        this.parent = parent;
        draw();
    }

    public MaterialPickerGUI(final String titleOverride, final String inputTitle, final String inputSubtitle, final String searchQuery, @NonNull final BiConsumer<GuiClickEvent, ItemStack> selected) {
        this(null, titleOverride, searchQuery, inputTitle, inputSubtitle, selected);
    }

    public MaterialPickerGUI(final String titleOverride, final String searchQuery, @NonNull final BiConsumer<GuiClickEvent, ItemStack> selected) {
        this(null, titleOverride, searchQuery, "&eMaterial Search", "&fType the search term into chat", selected);
    }

    public MaterialPickerGUI(Gui parent, String title, String searchQuery, BiConsumer<GuiClickEvent, ItemStack> selected) {
        this(parent, title, searchQuery, "&eMaterial Search", "&fType the search term into chat", selected);
    }

    @Override
    protected void draw() {
        reset();

        List<ItemStack> validMaterials = InventorySafeMaterials.get().stream().map(CompMaterial::parseItem).collect(Collectors.toList());

        // load in enchantments
        for (XEnchantment value : XEnchantment.values()) {
            final Enchantment enchantment = value.getEnchant();
            if (enchantment == null) continue;

            for (int i = 1; i <= enchantment.getMaxLevel(); i++)
                validMaterials.add(QuickItem.of(CompMaterial.ENCHANTED_BOOK).enchant(value.getEnchant(), i).make());
        }

        if (this.searchQuery != null) {
            validMaterials = validMaterials.stream().filter(mat -> Filterer.searchByItemInfo(this.searchQuery, mat)).toList();
        }

        final List<ItemStack> itemsToFill = validMaterials.stream().skip((page - 1) * (long) this.fillSlots().size()).limit(this.fillSlots().size()).toList();
        pages = (int) Math.max(1, Math.ceil(validMaterials.size() / (double) this.fillSlots().size()));

        setPrevPage(5, 3, this.getPreviousButton());
        setNextPage(5, 5, this.getNextButton());
        setOnPage(e -> draw());

        for (int i = 0; i < this.rows * 9; i++) {
            if (this.fillSlots().contains(i) && this.fillSlots().indexOf(i) < itemsToFill.size()) {
                final ItemStack material = itemsToFill.get(this.fillSlots().indexOf(i));
                setButton(i, buildIcon(material), click -> this.selected.accept(click, material));
            }
        }

        setButton(5, 4, buildSearchButton(), click -> {
            click.gui.exit();

            new TitleInput(FlightPlugin.getInstance(), click.player, Common.colorize(this.inputTitle), Common.colorize(this.inputSubtitle)) {
                @Override
                public boolean onResult(String string) {
                    if (string.isEmpty()) return false;
                    click.manager.showGUI(click.player, new MaterialPickerGUI(MaterialPickerGUI.this.parent, MaterialPickerGUI.this.title, string, MaterialPickerGUI.this.inputTitle, MaterialPickerGUI.this.inputSubtitle, MaterialPickerGUI.this.selected));
                    return true;
                }

                @Override
                public void onExit(Player player) {
                    click.manager.showGUI(click.player, MaterialPickerGUI.this);
                }
            };
        });

        if (this.searchQuery != null)
            setButton(5, 7, buildResetButton(), click -> click.manager.showGUI(click.player, new MaterialPickerGUI(MaterialPickerGUI.this.parent, MaterialPickerGUI.this.title, null, MaterialPickerGUI.this.inputTitle, MaterialPickerGUI.this.inputSubtitle, this.selected)));

        drawExtra();

        applyBackExit(this.parent);
    }

    protected ItemStack buildIcon(@NonNull final ItemStack itemStack) {
        return QuickItem.of(itemStack)
                .name("&e&l" + ChatUtil.capitalizeFully(itemStack.getType()))
                .lore("&7Click to select this material")
                .make();
    }

    protected ItemStack buildSearchButton() {
        return QuickItem.of(CompMaterial.DARK_OAK_SIGN).name("&b&lSearch").lore("&7Click to search materials").make();
    }

    protected ItemStack buildResetButton() {
        return QuickItem
                .of(CompMaterial.LAVA_BUCKET)
                .name("&c&lClear Search")
                .lore("&7Click to clear your search")
                .make();
    }

    protected void drawExtra(){}

    @Override
    protected ItemStack getNextButton() {
        return QuickItem.of(CompMaterial.ARROW).name("&ENext").make();
    }

    @Override
    protected ItemStack getPreviousButton() {
        return QuickItem.of(CompMaterial.ARROW).name("&ePrevious").make();
    }

    @Override
    protected int getPreviousButtonSlot() {
        return 48;
    }

    @Override
    protected int getNextButtonSlot() {
        return 50;
    }

    @Override
    protected List<Integer> fillSlots() {
        return InventoryBorder.getInsideBorders(5);
    }

    @Override
    protected ItemStack getBackButton() {
        return QuickItem.of(CompMaterial.DARK_OAK_DOOR).name("&EBack").lore("&7Click to go back").make();
    }

    @Override
    protected ItemStack getExitButton() {
        return QuickItem.of(CompMaterial.BARRIER).name("&cExit").lore("&7Click to exit menu").make();
    }
}
