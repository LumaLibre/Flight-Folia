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

import ca.tweetzy.flight.gui.Gui;
import ca.tweetzy.flight.gui.events.GuiClickEvent;
import lombok.NonNull;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Date Created: April 10 2022
 * Time Created: 10:00 p.m.
 *
 * @author Kiran Hart
 */
public abstract class PagedGUI<T> extends BaseGUI {

    protected final Gui parent;
    private List<T> items = new ArrayList<>();


    public PagedGUI(final Gui parent, @NonNull final String title, final int rows, @NonNull final List<T> items) {
        super(parent, title, rows);
        this.parent = parent;
        this.items = items;
    }

    public PagedGUI(@NonNull final String title, final int rows, @NonNull final List<T> items) {
        this(null, title, rows, items);
    }

    @Override
    protected void draw() {
        reset();
        handleTitle();

        if (this.items != null) {
            final List<T> itemsToFill = this.items.stream().skip((page - 1) * (long) this.fillSlots().size()).limit(this.fillSlots().size()).toList();
            pages = (int) Math.max(1, Math.ceil(this.items.size() / (double) this.fillSlots().size()));

            setPrevPage(getPreviousButtonSlot(), getPreviousButton());
            setNextPage(getNextButtonSlot(), getNextButton());
            setOnPage(e -> draw());

            for (int i = 0; i < this.rows * 9; i++) {
                if (this.fillSlots().contains(i) && this.fillSlots().indexOf(i) < itemsToFill.size()) {
                    final T object = itemsToFill.get(this.fillSlots().indexOf(i));
                    setButton(i, this.makeDisplayItem(object), click -> this.onClick(object, click));
                }
            }
        }


        drawAdditional();
        applyBackExit();
    }

    protected void handleTitle() {

    }

    protected void drawAdditional() {
    }

    public Gui getGuiParent() {
        return this.parent;
    }

    protected abstract ItemStack makeDisplayItem(final T object);

    protected abstract void onClick(final T object, final GuiClickEvent clickEvent);

    protected void updateItems(@NonNull final List<T> items) {
        this.items = items;
    }
}
