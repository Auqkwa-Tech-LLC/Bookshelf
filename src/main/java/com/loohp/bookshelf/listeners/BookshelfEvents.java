package com.loohp.bookshelf.listeners;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import com.loohp.bookshelf.Bookshelf;
import com.loohp.bookshelf.BookshelfManager;
import com.loohp.bookshelf.api.Events.PlayerCloseBookshelfEvent;
import com.loohp.bookshelf.api.Events.PlayerOpenBookshelfEvent;
import com.loohp.bookshelf.objectholders.LWCRequestOpenData;
import com.loohp.bookshelf.utils.BookshelfUtils;
import com.loohp.bookshelf.utils.MCVersion;
import com.loohp.bookshelf.utils.NBTUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class BookshelfEvents implements Listener {

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        Bookshelf.loadBookshelf(event.getWorld());
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!Bookshelf.EnableHopperSupport) {
            return;
        }
        Chunk chunk = event.getChunk();
        Bookshelf.bookshelfLoadPending.add(chunk);
        while (Bookshelf.bookshelfRemovePending.contains(chunk)) {
            Bookshelf.bookshelfRemovePending.remove(chunk);
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        if (!Bookshelf.EnableHopperSupport) {
            return;
        }
        Chunk chunk = event.getChunk();
        Bookshelf.bookshelfRemovePending.add(chunk);
        while (Bookshelf.bookshelfLoadPending.contains(chunk)) {
            Bookshelf.bookshelfLoadPending.remove(chunk);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (!event.getPlayer().hasPermission("bookshelf.use")) {
            return;
        }

        if (event.getBlockAgainst().getType().equals(Material.BOOKSHELF)) {
            if (!event.getPlayer().isSneaking() && Bookshelf.lastBlockFace.containsKey(event.getPlayer())) {
                BlockFace face = Bookshelf.lastBlockFace.get(event.getPlayer());

                if (face.equals(BlockFace.EAST) || face.equals(BlockFace.SOUTH) || face.equals(BlockFace.WEST) || face.equals(BlockFace.NORTH)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (!event.getBlockPlaced().getType().equals(Material.BOOKSHELF)) {
            return;
        }

        String loc = BookshelfUtils.locKey(event.getBlockPlaced().getLocation());
        ItemStack item = event.getItemInHand();
        if (Bookshelf.version.isOlderOrEqualTo(MCVersion.V1_13_1)) {
            if (NBTUtils.contains(item, "BookshelfContent") && NBTUtils.contains(item, "BookshelfTitle")) {
                String title = NBTUtils.getString(item, "BookshelfTitle");
                if (!item.getItemMeta().getDisplayName().equals("")) {
                    title = item.getItemMeta().getDisplayName();
                }
                String hash = NBTUtils.getString(item, "BookshelfContent");
                try {
                    Bookshelf.addBookshelfToMapping(loc, BookshelfUtils.fromBase64(hash, title));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                BookshelfManager.setTitle(loc, title);
                BookshelfUtils.saveBookShelf(loc);
                return;
            }
        } else {
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            NamespacedKey keyContent = new NamespacedKey(Bookshelf.plugin, "BookshelfContent");
            NamespacedKey keyTitle = new NamespacedKey(Bookshelf.plugin, "BookshelfTitle");
            if (pdc.has(keyContent, PersistentDataType.STRING) && pdc.has(keyTitle, PersistentDataType.STRING)) {
                String hash = pdc.get(keyContent, PersistentDataType.STRING);
                String title = pdc.get(keyTitle, PersistentDataType.STRING);
                try {
                    Bookshelf.addBookshelfToMapping(loc, BookshelfUtils.fromBase64(hash, title));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                BookshelfManager.setTitle(loc, title);
                BookshelfUtils.saveBookShelf(loc);
                return;
            }
        }

        if (Bookshelf.keyToContentMapping.containsKey(loc)) {
            return;
        }
        if (BookshelfManager.contains(loc)) {
            return;
        }

        String bsTitle = Bookshelf.Title;
        if (event.getItemInHand().hasItemMeta()) {
            if (event.getItemInHand().getItemMeta().hasDisplayName()) {
                if (!event.getItemInHand().getItemMeta().getDisplayName().equals("")) {
                    bsTitle = event.getItemInHand().getItemMeta().getDisplayName();
                }
            }
        }
        Bookshelf.addBookshelfToMapping(loc, Bukkit.createInventory(null, Bookshelf.BookShelfRows * 9, bsTitle));
        BookshelfManager.setTitle(loc, bsTitle);
        BookshelfUtils.saveBookShelf(loc);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (!event.getBlock().getType().equals(Material.BOOKSHELF)) {
            return;
        }

        String loc = BookshelfUtils.locKey(event.getBlock().getLocation());
        if (!Bookshelf.keyToContentMapping.containsKey(loc)) {
            if (!BookshelfManager.contains(loc)) {
                return;
            }
            BookshelfUtils.loadBookShelf(loc);
        }
        Inventory inv = Bookshelf.keyToContentMapping.get(loc);
        for (ItemStack item : inv.getContents()) {
            if (!item.getType().equals(Material.AIR)) {
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), item);
            }
        }
        BookshelfUtils.safeRemoveBookself(loc);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onExplode(EntityExplodeEvent event) {
        if (event.isCancelled()) {
            return;
        }

        List<Block> bookshelves = new ArrayList<>();
        for (Block block : event.blockList()) {
            loadBookshelf(bookshelves, block);
        }

        if (bookshelves.isEmpty()) {
            return;
        }

        for (Block bookshelf : bookshelves) {
            String loc = BookshelfUtils.locKey(bookshelf.getLocation());
            if (!Bookshelf.keyToContentMapping.containsKey(loc)) {
                if (!BookshelfManager.contains(loc)) {
                    return;
                }
                BookshelfUtils.loadBookShelf(loc);
            }
            Inventory inv = Bookshelf.keyToContentMapping.get(loc);
            for (ItemStack item : inv.getContents()) {
                if (!item.getType().equals(Material.AIR)) {
                    bookshelf.getWorld().dropItemNaturally(bookshelf.getLocation(), item);
                }
            }
            BookshelfUtils.safeRemoveBookself(loc);
        }
    }

    static void loadBookshelf(List<Block> bookshelves, Block block) {
        if (block.getType().equals(Material.BOOKSHELF)) {
            String key = BookshelfUtils.locKey(block.getLocation());
            if (!Bookshelf.keyToContentMapping.containsKey(key)) {
                if (BookshelfManager.contains(key)) {
                    BookshelfUtils.loadBookShelf(key);
                    bookshelves.add(block);
                }
            } else {
                bookshelves.add(block);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBurn(BlockBurnEvent event) {
        if (event.getBlock().getType().equals(Material.BOOKSHELF)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onUse(InventoryClickEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (event.getRawSlot() == -999) {
            return;
        }

        if (event.getView().getType().equals(InventoryType.CREATIVE)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        if (Bookshelf.isDonationView.contains(player.getUniqueId())) {
            Inventory clicked = event.getClickedInventory();
            if (Bookshelf.contentToKeyMapping.containsKey(clicked)) {
                if (event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY) || event.getAction().equals(InventoryAction.PICKUP_SOME) || event.getAction().equals(InventoryAction.PICKUP_ALL) || event.getAction().equals(InventoryAction.PICKUP_ONE) || event.getAction().equals(InventoryAction.PICKUP_HALF)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (!Bookshelf.UseWhitelist) {
            return;
        }
        if (event.getAction().equals(InventoryAction.NOTHING) || event.getAction().equals(InventoryAction.UNKNOWN) || event.getAction().equals(InventoryAction.DROP_ALL_CURSOR) || event.getAction().equals(InventoryAction.DROP_ALL_SLOT) || event.getAction().equals(InventoryAction.DROP_ONE_CURSOR) || event.getAction().equals(InventoryAction.DROP_ONE_SLOT)) {
            return;
        }

        if (event.getAction().equals(InventoryAction.HOTBAR_MOVE_AND_READD) || event.getAction().equals(InventoryAction.HOTBAR_SWAP)) {
            if (!Bookshelf.contentToKeyMapping.containsKey(event.getView().getTopInventory())) {
                return;
            }
            int slot = event.getRawSlot();
            int inventorySize = event.getView().getTopInventory().getSize();
            if (slot < inventorySize) {
                if (event.getWhoClicked().getInventory().getItem(event.getHotbarButton()) != null) {
                    if (!Bookshelf.Whitelist.contains(event.getWhoClicked().getInventory().getItem(event.getHotbarButton()).getType().toString().toUpperCase())) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
            return;
        }

        Inventory inv = event.getView().getTopInventory();
        String key = Bookshelf.contentToKeyMapping.get(inv);
        if (key != null) {
            Bookshelf.bookshelfSavePending.add(key);
            if (event.getClick().isShiftClick()) {
                ItemStack clickedOn = event.getCurrentItem();

                if (clickedOn != null) {
                    if (!clickedOn.getType().equals(Material.AIR)) {
                        if (!Bookshelf.Whitelist.contains(clickedOn.getType().toString().toUpperCase())) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }

            }
            Inventory clicked = event.getClickedInventory();
            if (clicked.equals(event.getView().getTopInventory())) {
                ItemStack onCursor = event.getCursor();

                if (onCursor != null) {
                    if (!onCursor.getType().equals(Material.AIR)) {
                        if (!Bookshelf.Whitelist.contains(onCursor.getType().toString().toUpperCase())) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }
        boolean putting = false;
        if (event.getClickedInventory().equals(event.getView().getTopInventory())) {
            putting = true;
        }
        if (event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY) && event.getClickedInventory().equals(event.getView().getBottomInventory())) {
            putting = true;
        }
        if (event.getAction().equals(InventoryAction.HOTBAR_MOVE_AND_READD) || event.getAction().equals(InventoryAction.HOTBAR_SWAP)) {
            if (!Bookshelf.contentToKeyMapping.containsKey(event.getView().getTopInventory())) {
                return;
            }
            int slot = event.getRawSlot();
            int inventorySize = event.getView().getTopInventory().getSize();
            if (slot < inventorySize) {
                putting = true;
            }
        }

        if (!putting) {
            return;
        }

        for (Entry<String, Inventory> entry : Bookshelf.keyToContentMapping.entrySet()) {
            if (entry.getValue().equals(event.getView().getTopInventory())) {
                Location loc = BookshelfUtils.keyLoc(entry.getKey());
                playSound(loc, event.getWhoClicked());
                break;
            }
        }
    }

    private void playSound(Location loc, HumanEntity whoClicked) {
        double random = Math.floor(Math.random() * 3) + 1;
        if (Bookshelf.version.isOld()) {
            whoClicked.getWorld().playSound(loc.add(0.5, 0.5, 0.5), Sound.valueOf("HORSE_ARMOR"), 3, 1);
        } else if (Bookshelf.version.isOlderOrEqualTo(MCVersion.V1_13_1)) {
            whoClicked.getWorld().playSound(loc.add(0.5, 0.5, 0.5), Sound.valueOf("ITEM_ARMOR_EQUIP_LEATHER"), 3, 1);
        } else {
            if (random == 1) {
                whoClicked.getWorld().playSound(loc.add(0.5, 0.5, 0.5), Sound.valueOf("ITEM_BOOK_PUT"), 3, 1);
            } else if (random == 2) {
                whoClicked.getWorld().playSound(loc.add(0.5, 0.5, 0.5), Sound.valueOf("ITEM_ARMOR_EQUIP_LEATHER"), 3, 1);
            } else if (random == 3) {
                whoClicked.getWorld().playSound(loc.add(0.5, 0.5, 0.5), Sound.valueOf("ITEM_BOOK_PAGE_TURN"), 3, 1);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrag(InventoryDragEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!Bookshelf.UseWhitelist) {
            return;
        }

        Inventory inv = event.getView().getTopInventory();
        String key = Bookshelf.contentToKeyMapping.get(inv);
        if (key != null) {
            ItemStack dragged = event.getOldCursor(); // This is the item that is being dragged

            if (!Bookshelf.Whitelist.contains(dragged.getType().toString().toUpperCase())) {
                int inventorySize = inv.getSize();

                for (int i : event.getRawSlots()) {
                    if (i < inventorySize) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }

            int inventorySize = inv.getSize();

            for (int i : event.getRawSlots()) {
                if (i < inventorySize) {
                    Location loc = BookshelfUtils.keyLoc(key);
                    playSound(loc, event.getWhoClicked());
                }
            }
        }


    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onOpen(PlayerInteractEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        if (!Bookshelf.version.isOld()) {
            if (event.getHand() == null || event.getHand().equals(EquipmentSlot.OFF_HAND)) {
                return;
            }
        }

        Player player = event.getPlayer();

        if (player.isSneaking()) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();

        if (clickedBlock == null) {
            return;
        }
        if (!clickedBlock.getType().equals(Material.BOOKSHELF)) {
            return;
        }
        if (Bookshelf.lwcCancelOpen.contains(event.getPlayer().getUniqueId())) {
            return;
        }

        Bookshelf.lastBlockFace.put(event.getPlayer(), event.getBlockFace());
        Bukkit.getScheduler().runTaskLater(Bookshelf.plugin, () -> Bookshelf.lastBlockFace.remove(event.getPlayer()), 2);

        if (event.getBlockFace().equals(BlockFace.UP) || event.getBlockFace().equals(BlockFace.DOWN)) {
            return;
        }

        boolean cancelled = false;

        if (!player.hasPermission("bookshelf.use")) {
            cancelled = true;
        }

        String loc = BookshelfUtils.locKey(clickedBlock.getLocation());
        saveBookshelf(loc);
        if (Bookshelf.LWCHook) {
            Location blockLoc = BookshelfUtils.keyLoc(loc);
            Protection protection = LWC.getInstance().getPlugin().getLWC().findProtection(blockLoc.getBlock());
            if (protection != null) {
                if (!protection.isOwner(player)) {
                    Bookshelf.requestOpen.put(player, new LWCRequestOpenData(loc, event.getBlockFace(), cancelled));
                    return;
                }
            }
        }

        PlayerOpenBookshelfEvent pobe = new PlayerOpenBookshelfEvent(player, loc, event.getBlockFace(), cancelled);
        Bukkit.getPluginManager().callEvent(pobe);

        if (pobe.isCancelled()) {
            return;
        }

        Inventory inv = Bookshelf.keyToContentMapping.get(loc);
        Bukkit.getScheduler().runTask(Bookshelf.plugin, () -> player.openInventory(inv));
        if (!Bookshelf.bookshelfSavePending.contains(loc)) {
            Bookshelf.bookshelfSavePending.add(loc);
        }
    }

    static void saveBookshelf(String loc) {
        if (!Bookshelf.keyToContentMapping.containsKey(loc)) {
            if (!BookshelfManager.contains(loc)) {
                String bsTitle = Bookshelf.Title;
                Bookshelf.addBookshelfToMapping(loc, Bukkit.createInventory(null, Bookshelf.BookShelfRows * 9, bsTitle));
                BookshelfManager.setTitle(loc, bsTitle);
                BookshelfUtils.saveBookShelf(loc);
            } else {
                BookshelfUtils.loadBookShelf(loc);
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Inventory inv = event.getView().getTopInventory();
        String key = Bookshelf.contentToKeyMapping.get(inv);
        if (key != null) {
            PlayerCloseBookshelfEvent pcbe = new PlayerCloseBookshelfEvent((Player) event.getPlayer(), key);
            Bukkit.getPluginManager().callEvent(pcbe);

            Bookshelf.bookshelfSavePending.add(key);
        }
        Bookshelf.isDonationView.remove(event.getPlayer().getUniqueId());
    }
}