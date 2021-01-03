package com.loohp.bookshelf.Listeners;

import com.loohp.bookshelf.Bookshelf;
import com.loohp.bookshelf.BookshelfManager;
import com.loohp.bookshelf.Utils.BookshelfUtils;
import com.loohp.bookshelf.Utils.CustomListUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;

public class PistonEvents implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (event.isCancelled()) {
            return;
        }
        List<Block> bookshelves;
        bookshelves = new ArrayList<>();
        for (Block block : event.getBlocks()) {
            if (block.getType().equals(Material.BOOKSHELF)) {
                String key = BookshelfUtils.locKey(block.getLocation());
                if (!Bookshelf.keyToContentMapping.containsKey(key)) {
                    if (BookshelfManager.contains("BookShelfData." + key)) {
                        BookshelfUtils.loadBookShelf(key);
                        bookshelves.add(block);
                    }
                } else {
                    bookshelves.add(block);
                }
            }
        }

        save(bookshelves, event.getDirection());
    }

    private void save(List<Block> bookshelves, BlockFace direction) {
        if (bookshelves.isEmpty()) {
            return;
        }

        for (Block bookshelf : CustomListUtils.reverse(bookshelves)) {
            String key = BookshelfUtils.locKey(bookshelf.getLocation());
            Inventory inv = Bookshelf.keyToContentMapping.get(key);
            Location newLoc = bookshelf.getRelative(direction).getLocation().clone();
            String newKey = BookshelfUtils.locKey(newLoc);
            String bsTitle = Bookshelf.Title;
            if (BookshelfManager.getTitle(key) != null) {
                bsTitle = BookshelfManager.getTitle(key);
            }
            BookshelfUtils.safeRemoveBookself(key);

            Bookshelf.addBookshelfToMapping(newKey, inv);
            BookshelfManager.setTitle(newKey, bsTitle);

            BookshelfUtils.saveBookShelf(newKey);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (event.isCancelled()) {
            return;
        }
        List<Block> bookshelves = new ArrayList<>();
        for (Block block : event.getBlocks()) {
            BookshelfEvents.loadBookshelf(bookshelves, block);
        }

        save(bookshelves, event.getDirection());
    }

}
