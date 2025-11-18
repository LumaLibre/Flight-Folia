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

import ca.tweetzy.flight.gui.config.GuiConfigContext;
import ca.tweetzy.flight.gui.events.GuiClickEvent;
import lombok.NonNull;

import java.util.Map;

/**
 * Built-in action handler for closing the GUI.
 */
public final class CloseAction implements GuiConfigActionHandler {

    @Override
    public boolean execute(@NonNull GuiClickEvent event, @NonNull GuiConfigContext context, @NonNull String actionString, @NonNull Map<String, String> parameters) {
        event.gui.close();
        return true;
    }
}

