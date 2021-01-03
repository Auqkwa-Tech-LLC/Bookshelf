package com.loohp.bookshelf.Utils;

import com.loohp.bookshelf.Bookshelf;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class HopperUtils {

    private static final HashMap<World, Long> PerWorldHopperTransferCurrentTick = new HashMap<>();
    private static final HashMap<World, Long> PerWorldHopperTransferSpeed = new HashMap<>();
    private static final HashMap<World, Long> PerWorldHopperTransferAmount = new HashMap<>();

    public static void hopperMinecartCheck() {
        for (World world : Bukkit.getWorlds()) {
            if (Bukkit.spigot().getConfig().contains("world-settings." + world.getName() + ".ticks-per.hopper-transfer")) {
                long rate = Bukkit.spigot().getConfig().getLong("world-settings." + world.getName() + ".ticks-per.hopper-transfer");
                PerWorldHopperTransferSpeed.put(world, rate);
            }
            if (Bukkit.spigot().getConfig().contains("world-settings." + world.getName() + ".hopper-amount")) {
                long amount = Bukkit.spigot().getConfig().getLong("world-settings." + world.getName() + ".hopper-amount");
                PerWorldHopperTransferAmount.put(world, amount);
            }
        }
        Bookshelf.HopperMinecartTaskID = Bukkit.getScheduler().runTaskTimer(Bookshelf.plugin, () -> {
            long start = System.currentTimeMillis();
            long startNano = System.nanoTime();
            for (World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    if (entity.getType().equals(EntityType.MINECART_HOPPER)) {
                        HopperMinecart hoppercart = (HopperMinecart) entity;
                        if (!hoppercart.isEnabled()) {
                            continue;
                        }
                        hoppercart.getLocation().getBlock().getRelative(BlockFace.UP);

                        Block bookshelfBlock = hoppercart.getLocation().getBlock().getRelative(BlockFace.UP);
                        if (!bookshelfBlock.getType().equals(Material.BOOKSHELF)) {
                            continue;
                        }
                        String key = BookshelfUtils.locKey(bookshelfBlock.getLocation());
                        if (!Bookshelf.keyToContentMapping.containsKey(key)) {
                            continue;
                        }

                        Inventory inventory = hoppercart.getInventory();
                        Inventory bookshelfInv = Bookshelf.keyToContentMapping.get(key);
                        for (int i = 0; i < bookshelfInv.getSize(); i++) {
                            ItemStack item = bookshelfInv.getItem(i);
                            if (item == null) {
                                continue;
                            }
                            if (InventoryUtils.stillHaveSpace(inventory, item.getType())) {

                                if (Bookshelf.BlockLockerHook) {
                                    if (BlockLockerUtils.isLocked(bookshelfBlock)) {
                                        break;
                                    }
                                }
                                if (Bookshelf.LWCHook) {
                                    if (LWCUtils.checkHopperFlagOut(bookshelfBlock)) {
                                        break;
                                    }
                                    if (!LWCUtils.checkHopperFlagIn(hoppercart)) {
                                        break;
                                    }
                                }

                                ItemStack additem = item.clone();
                                additem.setAmount(1);
                                ItemStack beforeEvent = additem.clone();
                                InventoryMoveItemEvent event = new InventoryMoveItemEvent(inventory, additem, bookshelfInv, true);
                                if (event.isCancelled()) {
                                    break;
                                }
                                additem = event.getItem();
                                if (beforeEvent.equals(additem)) {
                                    item.setAmount(item.getAmount() - 1);
                                    bookshelfInv.setItem(i, item);
                                }
                                inventory.addItem(additem);
                                Bookshelf.bookshelfSavePending.add(key);
                                break;
                            }
                        }
                    }
                }
            }
            long end = System.currentTimeMillis();
            long endNano = System.nanoTime();
            Bookshelf.lastHoppercartTime = endNano - startNano;
            if ((end - start) > 500) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[Bookshelf] Hopper Minecart Item Move Function took more than 500ms! (" + (end - start) + "ms)");
            }
        }, 0, 1).getTaskId();
    }

    public static void hopperCheck() {
        Bookshelf.HopperTaskID = Bukkit.getScheduler().runTaskTimer(Bookshelf.plugin, () -> {
            long start = System.currentTimeMillis();
            long startNano = System.nanoTime();
            HashMap<World, Long> perform = new HashMap<>();
            for (World world : Bukkit.getWorlds()) {
                long currentTick = 0;
                if (PerWorldHopperTransferCurrentTick.containsKey(world)) {
                    currentTick = PerWorldHopperTransferCurrentTick.get(world);
                }
                long rate = Bookshelf.HopperTicksPerTransfer;
                if (PerWorldHopperTransferSpeed.containsKey(world)) {
                    rate = PerWorldHopperTransferSpeed.get(world);
                }
                currentTick++;
                if (currentTick > rate) {
                    currentTick = 1;
                }
                PerWorldHopperTransferCurrentTick.put(world, currentTick);
                if (currentTick == 1) {
                    long amount = Bookshelf.HopperAmount;
                    if (PerWorldHopperTransferAmount.containsKey(world)) {
                        amount = PerWorldHopperTransferAmount.get(world);
                    }
                    perform.put(world, amount);
                }
            }
            for (Entry<String, Inventory> entry : Bookshelf.keyToContentMapping.entrySet()) {
                Location loc = BookshelfUtils.keyLoc(entry.getKey());
                if (!perform.containsKey(loc.getWorld())) {
                    continue;
                }
                long amount = perform.get(loc.getWorld());
                Block blockBelow = loc.getBlock().getRelative(BlockFace.DOWN);
                if (!loc.getBlock().getType().equals(Material.BOOKSHELF)) {
                    continue;
                }
                if (blockBelow.getType().equals(Material.HOPPER)) {
                    if (!blockBelow.isBlockPowered() && !blockBelow.isBlockIndirectlyPowered()) {
                        Hopper h = (Hopper) blockBelow.getState();
                        Inventory inventory = h.getInventory();
                        Inventory bookshelfInv = entry.getValue();
                        for (int i = 0; i < bookshelfInv.getSize(); i++) {
                            ItemStack item = bookshelfInv.getItem(i);
                            if (item == null) {
                                continue;
                            }
                            if (InventoryUtils.stillHaveSpace(inventory, item.getType())) {
                                if (isAllow(loc.getBlock())) {
                                    ItemStack additem = item.clone();
                                    int num = item.getAmount();
                                    if (num > amount) {
                                        num = (int) amount;
                                    }
                                    additem.setAmount(num);
                                    ItemStack beforeEvent = additem.clone();
                                    InventoryMoveItemEvent event = new InventoryMoveItemEvent(inventory, additem, bookshelfInv, true);
                                    if (event.isCancelled()) {
                                        break;
                                    }
                                    additem = event.getItem();
                                    if (beforeEvent.equals(additem)) {
                                        item.setAmount(item.getAmount() - num);
                                        bookshelfInv.setItem(i, item);
                                    }
                                    inventory.addItem(additem);
                                    Bookshelf.bookshelfSavePending.add(entry.getKey());
                                    break;
                                } else {
                                    break;
                                }
                            }
                        }
                    }
                }

                Block bookshelfBlock = loc.getBlock();
                List<Block> hoppers = getHoppersIn(bookshelfBlock, BlockFace.UP, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST);
                for (Block hopper : hoppers) {
                    if (hopper.isBlockPowered() || hopper.isBlockIndirectlyPowered()) {
                        continue;
                    }
                    org.bukkit.block.Hopper h = (org.bukkit.block.Hopper) hopper.getState();
                    Inventory inventory = h.getInventory();
                    Inventory bookshelfInv = entry.getValue();
                    for (int i = 0; i < inventory.getSize(); i++) {
                        ItemStack item = inventory.getItem(i);
                        if (item == null) {
                            continue;
                        }
                        if (Bookshelf.UseWhitelist) {
                            if (Bookshelf.Whitelist.contains(item.getType().toString().toUpperCase())) {
                                if (stillHaveSpaceCheck(entry, amount, bookshelfBlock, hopper, inventory, bookshelfInv, i, item))
                                    break;
                            }
                        } else {
                            if (stillHaveSpaceCheck(entry, amount, bookshelfBlock, hopper, inventory, bookshelfInv, i, item))
                                break;
                        }
                    }
                }
            }
            long end = System.currentTimeMillis();
            long endNano = System.nanoTime();
            //Bukkit.getConsoleSender().sendMessage("(" + (endNano - startNano) + "ns) / (" + (end - start) + "ms)");
            Bookshelf.lastHopperTime = endNano - startNano;
            if ((end - start) > 500) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[Bookshelf] Hopper Block Item Move Function took more than 500ms! (" + (end - start) + "ms)");
            }
        }, 0, 1).getTaskId();
    }

    private static boolean stillHaveSpaceCheck(Entry<String, Inventory> entry, long amount, Block bookshelfBlock, Block hopper, Inventory inventory, Inventory bookshelfInv, int i, ItemStack item) {
        if (InventoryUtils.stillHaveSpace(bookshelfInv, item.getType())) {
            if (Bookshelf.LWCHook) {
                if (LWCUtils.checkHopperFlagOut(hopper)) {
                    return true;
                }
                if (!LWCUtils.checkHopperFlagIn(bookshelfBlock)) {
                    return true;
                }
            }
            if (Bookshelf.BlockLockerHook) {
                if (BlockLockerUtils.canRedstone(bookshelfBlock)) {
                    return true;
                }
            }
            ItemStack additem = item.clone();
            int num = item.getAmount();
            if (num > amount) {
                num = (int) amount;
            }
            additem.setAmount(num);
            ItemStack beforeEvent = additem.clone();
            InventoryMoveItemEvent event = new InventoryMoveItemEvent(bookshelfInv, additem, inventory, false);
            if (event.isCancelled()) {
                return true;
            }
            additem = event.getItem();
            if (beforeEvent.equals(additem)) {
                item.setAmount(item.getAmount() - num);
                inventory.setItem(i, item);
            }
            bookshelfInv.addItem(additem);
            Bookshelf.bookshelfSavePending.add(entry.getKey());
            return true;
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    public static List<Block> getHoppersIn(Block block, BlockFace... faces) {
        List<Block> hoppers = new ArrayList<>();
        if (Bookshelf.version.isLegacy()) {
            for (BlockFace face : faces) {
                Block relative = block.getRelative(face);
                if (relative.getType().equals(Material.HOPPER)) {
                    if (relative.getRelative(((org.bukkit.block.data.type.Hopper) relative.getBlockData()).getFacing()).equals(block)) {
                        hoppers.add(relative);
                    }
                }
            }
            return hoppers;
        } else {
            for (BlockFace face : faces) {
                if (!block.getRelative(face).getType().equals(Material.HOPPER)) {
                    continue;
                }
                Hopper hopper = (Hopper) block.getRelative(face).getState();
                int data = hopper.getRawData();
                BlockFace relative;
                switch (data) {
                    case 2:
                    case 10:
                        relative = BlockFace.NORTH;
                        break;
                    case 3:
                    case 11:
                        relative = BlockFace.SOUTH;
                        break;
                    case 4:
                    case 12:
                        relative = BlockFace.WEST;
                        break;
                    case 5:
                    case 13:
                        relative = BlockFace.EAST;
                        break;
                    case 0:
                    default:
                        relative = BlockFace.DOWN;
                }
                if (block.getRelative(face).getRelative(relative).equals(block)) {
                    hoppers.add(block.getRelative(face));
                }
            }
        }
        return hoppers;
    }

    public static boolean isAllow(Block block) {
        if (Bookshelf.BlockLockerHook) {
            if (BlockLockerUtils.canRedstone(block)) {
                return false;
            }
        }
        if (Bookshelf.LWCHook) {
            if (LWCUtils.checkHopperFlagOut(block)) {
                return false;
            }
            return LWCUtils.checkHopperFlagIn(block.getRelative(BlockFace.DOWN));
        }
        return true;
    }

}
