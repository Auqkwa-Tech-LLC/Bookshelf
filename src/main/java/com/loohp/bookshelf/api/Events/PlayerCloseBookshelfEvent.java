package com.loohp.bookshelf.api.Events;

import com.loohp.bookshelf.Bookshelf;
import com.loohp.bookshelf.utils.BookshelfUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class PlayerCloseBookshelfEvent extends Event {

    final Player player;
    final Block block;
    final Location location;
    final String key;
    final Inventory inventory;

    public PlayerCloseBookshelfEvent(Player player, String key) {
        this.player = player;
        this.key = key;
        this.location = BookshelfUtils.keyLoc(key);
        this.block = location.getBlock();
        this.inventory = Bookshelf.keyToContentMapping.get(key);
    }

    public Player getPlayer() {
        return player;
    }

    public Block getBlock() {
        return block;
    }

    public Location getLocation() {
        return location;
    }

    public String getKey() {
        return key;
    }

    public Inventory getInventory() {
        return inventory;
    }

    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
