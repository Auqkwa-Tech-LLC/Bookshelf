package com.loohp.bookshelf.listeners.hooks;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.loohp.bookshelf.Bookshelf;
import com.loohp.bookshelf.api.Events.PlayerOpenBookshelfEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class SuperiorSkyblock2Events implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSuperiorSkyblock2Check(PlayerOpenBookshelfEvent event) {

        if (!Bookshelf.TownyHook) {
            return;
        }

        Player player = event.getPlayer();

        Island island = SuperiorSkyblockAPI.getIslandAt(event.getLocation());

        if (island == null) {
            return;
        }

        if (!island.hasPermission(SuperiorSkyblockAPI.getPlayer(player.getUniqueId()), IslandPermission.CHEST_ACCESS)) {
            event.setCancelled(true);
        }

    }

}
