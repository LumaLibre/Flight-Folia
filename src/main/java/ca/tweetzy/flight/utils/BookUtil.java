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

package ca.tweetzy.flight.utils;

import ca.tweetzy.flight.utils.colors.ColorFormatter;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Book utilities for creating and managing written books.
 * 
 * @author Kiran Hart
 */
@UtilityClass
public class BookUtil {
    
    /**
     * Create a written book
     * 
     * @param title The book title
     * @param author The book author
     * @param pages The book pages (each string is a page)
     * @return The book ItemStack
     */
    @NonNull
    public ItemStack createBook(@NonNull String title, @NonNull String author, @NonNull String... pages) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        
        if (meta != null) {
            meta.setTitle(ColorFormatter.process(title));
            meta.setAuthor(ColorFormatter.process(author));
            
            List<String> pageList = new ArrayList<>();
            for (String page : pages) {
                pageList.add(ColorFormatter.process(page));
            }
            meta.setPages(pageList);
            
            book.setItemMeta(meta);
        }
        
        return book;
    }
    
    /**
     * Create a written book from a list of pages
     * 
     * @param title The book title
     * @param author The book author
     * @param pages The book pages
     * @return The book ItemStack
     */
    @NonNull
    public ItemStack createBook(@NonNull String title, @NonNull String author, @NonNull List<String> pages) {
        return createBook(title, author, pages.toArray(new String[0]));
    }
    
    /**
     * Give a book to a player
     * 
     * @param player The player
     * @param book The book to give
     */
    public void giveBook(@NonNull Player player, @NonNull ItemStack book) {
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(book);
        } else {
            player.getWorld().dropItemNaturally(player.getLocation(), book);
        }
    }
    
    /**
     * Open a book for a player
     * 
     * @param player The player
     * @param book The book to open
     */
    public void openBook(@NonNull Player player, @NonNull ItemStack book) {
        if (book.getType() == Material.WRITTEN_BOOK || book.getType() == Material.WRITABLE_BOOK) {
            player.openBook(book);
        }
    }
    
    /**
     * Create and give a book to a player
     * 
     * @param player The player
     * @param title The book title
     * @param author The book author
     * @param pages The book pages
     */
    public void createAndGive(@NonNull Player player, @NonNull String title, @NonNull String author, @NonNull String... pages) {
        ItemStack book = createBook(title, author, pages);
        giveBook(player, book);
    }
}

