package com.loohp.bookshelf.Listeners;

import com.loohp.bookshelf.Bookshelf;
import com.loohp.bookshelf.Utils.*;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DispenserEvents implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDropper(BlockDispenseEvent event) {
        if (!Bookshelf.EnableDropperSupport) {
            return;
        }
        if (event.isCancelled()) {
            return;
        }
        if (!event.getBlock().getType().equals(Material.DROPPER)) {
            return;
        }
        Block relative = DropperUtils.getDropperRelative(event.getBlock());
        if (!relative.getType().equals(Material.BOOKSHELF)) {
            return;
        }
        String key = BookshelfUtils.locKey(relative.getLocation());
        BookshelfEvents.saveBookshelf(key);
        if (Bookshelf.LWCHook) {
            if (!LWCUtils.checkHopperFlagIn(relative)) {
                event.setCancelled(true);
                return;
            }
        }
        if (Bookshelf.BlockLockerHook) {
            if (BlockLockerUtils.canRedstone(relative)) {
                event.setCancelled(true);
                return;
            }
        }
        event.setCancelled(true);
        Inventory bookshelf = Bookshelf.keyToContentMapping.get(key);
        org.bukkit.block.Dropper d = (org.bukkit.block.Dropper) event.getBlock().getState();
        Inventory dropper = d.getInventory();
        List<ItemStack> newList = new ArrayList<>(Arrays.asList(dropper.getContents()));
        newList.add(event.getItem());
        Collections.shuffle(newList);
        for (ItemStack each : newList) {
            if (each == null) {
                continue;
            }
            if (Bookshelf.UseWhitelist) {
                if (!Bookshelf.Whitelist.contains(each.getType().toString().toUpperCase())) {
                    continue;
                }
            }
            if (!InventoryUtils.stillHaveSpace(bookshelf, each.getType())) {
                continue;
            }
            ItemStack additem = each.clone();
            additem.setAmount(1);
            bookshelf.addItem(additem);
            boolean removed = false;
            for (int i = 0; i < dropper.getSize(); i = i + 1) {
                ItemStack removeitem = dropper.getItem(i);
                if (removeitem == null) {
                    continue;
                }
                if (removeitem.equals(each)) {
                    removeitem.setAmount(removeitem.getAmount() - 1);
                    dropper.setItem(i, removeitem);
                    removed = true;
                    break;
                }
            }
            if (!removed) {
                new BukkitRunnable() {
                    public void run() {
                        for (int i = 0; i < dropper.getSize(); i = i + 1) {
                            ItemStack removeitem = dropper.getItem(i);
                            if (removeitem == null) {
                                continue;
                            }
                            if (removeitem.equals(each)) {
                                removeitem.setAmount(removeitem.getAmount() - 1);
                                dropper.setItem(i, removeitem);
                                break;
                            }
                        }
                    }
                }.runTaskLater(Bookshelf.plugin, 1);
            }
            Bookshelf.bookshelfSavePending.add(key);
            if (Bookshelf.version.isLegacy()) {
                event.getBlock().getWorld().playSound(event.getBlock().getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1, 1);
            }
            return;
        }
        if (Bookshelf.version.isLegacy()) {
            event.getBlock().getWorld().playSound(event.getBlock().getLocation(), Sound.BLOCK_DISPENSER_FAIL, 1, 1);
        }
    }

}
