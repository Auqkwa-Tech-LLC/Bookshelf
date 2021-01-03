package com.loohp.bookshelf.Listeners.Hooks;

import com.loohp.bookshelf.API.Events.PlayerOpenBookshelfEvent;
import com.loohp.bookshelf.Bookshelf;
import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class TownyEvents implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onTownyEventsCheck(PlayerOpenBookshelfEvent event) {

        if (!Bookshelf.TownyHook) {
            return;
        }

        Player player = event.getPlayer();

        if (!PlayerCacheUtil.getCachePermission(player, event.getLocation(), event.getBlock().getType(), TownyPermission.ActionType.BUILD)) {
            event.setCancelled(true);
        }

    }

}
