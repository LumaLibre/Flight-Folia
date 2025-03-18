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
import ca.tweetzy.flight.comp.enums.CompSound;
import ca.tweetzy.flight.gui.Gui;
import ca.tweetzy.flight.gui.events.GuiClickEvent;
import ca.tweetzy.flight.gui.helper.InventoryBorder;
import ca.tweetzy.flight.utils.ChatUtil;
import ca.tweetzy.flight.utils.Common;
import ca.tweetzy.flight.utils.Inflector;
import ca.tweetzy.flight.utils.QuickItem;
import ca.tweetzy.flight.utils.input.TitleInput;
import com.cryptomorin.xseries.XSound;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Date Created: April 20 2022
 * Time Created: 2:02 p.m.
 *
 * @author Kiran Hart
 */
public final class SoundPickerGUI extends BaseGUI {

    private final Gui parent;
    private final String inputTitle, inputSubtitle;
    private final String searchQuery;
    private final BiConsumer<GuiClickEvent, CompSound> selected;


    public SoundPickerGUI(final Gui parent, final String titleOverride, final String searchQuery, final String inputTitle, final String inputSubtitle, @NonNull final BiConsumer<GuiClickEvent, CompSound> selected) {
        super(parent, titleOverride == null ? "&eSound Selector" : Common.colorize(titleOverride));
        this.searchQuery = searchQuery;
        this.inputTitle = inputTitle;
        this.inputSubtitle = inputSubtitle;
        this.selected = selected;
        this.parent = parent;
        draw();
    }

    public SoundPickerGUI(final String titleOverride, final String inputTitle, final String inputSubtitle, final String searchQuery, @NonNull final BiConsumer<GuiClickEvent, CompSound> selected) {
        this(null, titleOverride, searchQuery, inputTitle, inputSubtitle, selected);
    }

    public SoundPickerGUI(final String titleOverride, final String searchQuery, @NonNull final BiConsumer<GuiClickEvent, CompSound> selected) {
        this(null, titleOverride, searchQuery, "&eSound Search", "&fType the search term into chat", selected);
    }

    public SoundPickerGUI(Gui parent, String title, String searchQuery, BiConsumer<GuiClickEvent, CompSound> selected) {
        this(parent, title, searchQuery, "&eSound Search", "&fType the search term into chat", selected);
    }

    @Override
    protected void draw() {
        reset();

        List<CompSound> validSounds = Arrays.asList(CompSound.values());
        if (this.searchQuery != null) {
            validSounds = validSounds.stream().filter(mat -> Common.match(this.searchQuery, mat.name()) || Common.match(this.searchQuery, Inflector.getInstance().pluralize(mat.name()))).collect(Collectors.toList());
        }

        final List<CompSound> itemsToFill = validSounds.stream().skip((page - 1) * (long) this.fillSlots().size()).limit(this.fillSlots().size()).collect(Collectors.toList());
        pages = (int) Math.max(1, Math.ceil(validSounds.size() / (double) this.fillSlots().size()));

        setPrevPage(5, 3, this.getPreviousButton());
        setNextPage(5, 5, this.getNextButton());
        setOnPage(e -> draw());

        for (int i = 0; i < this.rows * 9; i++) {
            if (this.fillSlots().contains(i) && this.fillSlots().indexOf(i) < itemsToFill.size()) {
                final CompSound material = itemsToFill.get(this.fillSlots().indexOf(i));
                setButton(i, buildIcon(material), click -> this.selected.accept(click, material));
            }
        }

        setButton(5, 4, buildSearchButton(), click -> {
            click.gui.exit();

            new TitleInput(FlightPlugin.getInstance(), click.player, Common.colorize(this.inputTitle), Common.colorize(this.inputSubtitle)) {
                @Override
                public boolean onResult(String string) {
                    if (string.isEmpty()) return false;
                    click.manager.showGUI(click.player, new SoundPickerGUI(SoundPickerGUI.this.parent, SoundPickerGUI.this.title, string, SoundPickerGUI.this.inputTitle, SoundPickerGUI.this.inputSubtitle, SoundPickerGUI.this.selected));
                    return true;
                }

                @Override
                public void onExit(Player player) {
                    click.manager.showGUI(click.player, SoundPickerGUI.this);
                }
            };
        });

        if (this.searchQuery != null)
            setButton(5, 7, buildResetButton(), click -> click.manager.showGUI(click.player, new SoundPickerGUI(SoundPickerGUI.this.parent, SoundPickerGUI.this.title, null, SoundPickerGUI.this.inputTitle, SoundPickerGUI.this.inputSubtitle, this.selected)));

        applyBackExit(this.parent);
    }

    protected ItemStack buildIcon(@NonNull final CompSound sound) {
        return QuickItem.of(CompMaterial.MUSIC_DISC_5).name("&e&l" + ChatUtil.capitalizeFully(sound.friendlyName().replace(".", " "))).lore("&7Click to select this material").make();
    }

    protected ItemStack buildSearchButton() {
        return QuickItem.of(CompMaterial.DARK_OAK_SIGN).name("&b&lSearch").lore("&7Click to search sounds").make();
    }

    protected ItemStack buildResetButton() {
        return QuickItem.of(CompMaterial.LAVA_BUCKET).name("&c&lClear Search").lore("&7Click to clear your search").make();
    }

    @Override
    protected ItemStack getNextButton() {
        return QuickItem.of(CompMaterial.ARROW).name("&ENext").make();
    }

    @Override
    protected ItemStack getPreviousButton() {
        return QuickItem.of(CompMaterial.ARROW).name("&ePrevious").make();
    }

    @Override
    protected ItemStack getBackButton() {
        return QuickItem.of(CompMaterial.DARK_OAK_DOOR).name("&EBack").lore("&7Click to go back").make();
    }

    @Override
    protected ItemStack getExitButton() {
        return QuickItem.of(CompMaterial.BARRIER).name("&cExit").lore("&7Click to exit menu").make();
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
}
