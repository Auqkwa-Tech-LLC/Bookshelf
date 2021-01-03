package com.loohp.bookshelf.listeners.hooks;

import com.loohp.bookshelf.Bookshelf;
import com.loohp.bookshelf.api.Events.PlayerOpenBookshelfEvent;
import com.loohp.bookshelf.utils.BaseUtil;
import com.wasteofplastic.askyblock.ASkyBlock;
import com.wasteofplastic.askyblock.ASkyBlockAPI;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ASkyBlockEvents implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onASkyBlockCheck(PlayerOpenBookshelfEvent event) {

        if (!Bookshelf.ASkyBlockHook) {
            return;
        }

        Player player = event.getPlayer();
        Location location = event.getLocation();
        ASkyBlockAPI aSkyBlockAPI = ASkyBlockAPI.getInstance();

        if (aSkyBlockAPI.getIslandAt(event.getLocation()) == null) {
            return;
        }

        if (!aSkyBlockAPI.getIslandAt(location).getOwner().equals(player.getUniqueId())) {
            if (!aSkyBlockAPI.getIslandAt(location).getMembers().contains(player.getUniqueId())) {
                String message = ASkyBlock.getPlugin().myLocale(player.getUniqueId()).islandProtected;
                player.sendMessage(BaseUtil.fixColor(message));
                event.setCancelled(true);
            }
        }
    }

}
