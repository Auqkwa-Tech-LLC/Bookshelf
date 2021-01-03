package com.loohp.bookshelf.Listeners;

import com.loohp.bookshelf.Bookshelf;
import com.loohp.bookshelf.BookshelfManager;
import com.loohp.bookshelf.Utils.BookshelfUtils;
import com.loohp.bookshelf.Utils.MCVersion;
import com.loohp.bookshelf.Utils.MaterialUtils;
import com.loohp.bookshelf.Utils.NBTUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class CreativeEvents implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCreativePickBlock(InventoryCreativeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getClick().equals(ClickType.CREATIVE)) {
            Player player = (Player) event.getWhoClicked();
            ItemStack item = event.getCursor();
            if (item.getType().equals(Material.BOOKSHELF) && player.getGameMode().equals(GameMode.CREATIVE) && player.isSneaking() && player.hasPermission("bookshelf.copynbt")) {
                Block block = null;
                if (Bookshelf.version.isNewerOrEqualTo(MCVersion.V1_14)) {
                    block = player.getTargetBlockExact(10, FluidCollisionMode.NEVER);
                } else {
                    block = player.getTargetBlock(MaterialUtils.getNonSolidSet(), 10);
                }
                if (block.getType().equals(Material.BOOKSHELF)) {
                    String key = BookshelfUtils.locKey(block.getLocation());
                    BookshelfEvents.saveBookshelf(key);
                    String hash = BookshelfUtils.toBase64(Bookshelf.keyToContentMapping.get(key));
                    String title = BookshelfManager.getTitle(key);
                    if (Bookshelf.version.isOlderOrEqualTo(MCVersion.V1_13_1)) {
                        item = NBTUtils.set(item, hash, "BookshelfContent");
                        item = NBTUtils.set(item, title, "BookshelfTitle");
                    } else {
                        ItemMeta meta = item.getItemMeta();
                        PersistentDataContainer pdc = meta.getPersistentDataContainer();
                        pdc.set(new NamespacedKey(Bookshelf.plugin, "BookshelfContent"), PersistentDataType.STRING, hash);
                        pdc.set(new NamespacedKey(Bookshelf.plugin, "BookshelfTitle"), PersistentDataType.STRING, title);
                        item.setItemMeta(meta);
                    }
                    event.setCursor(item);
                }
            }
        }
    }

}
