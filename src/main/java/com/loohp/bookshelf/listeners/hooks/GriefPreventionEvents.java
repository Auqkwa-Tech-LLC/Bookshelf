package com.loohp.bookshelf.listeners.hooks;

import com.loohp.bookshelf.Bookshelf;
import com.loohp.bookshelf.api.Events.PlayerOpenBookshelfEvent;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class GriefPreventionEvents implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onGriefPreventionCheck(PlayerOpenBookshelfEvent event) {

        if (!Bookshelf.GriefPreventionHook) {
            return;
        }

        Player player = event.getPlayer();

        if (!player.hasPermission("bookshelf.use")) {
            return;
        }

        GriefPrevention griefPrevention = GriefPrevention.instance;

        if (!griefPrevention.claimsEnabledForWorld(event.getLocation().getWorld())) {
            return;
        }

        Location loc = event.getLocation();
        Claim claim = griefPrevention.dataStore.getClaimAt(loc, false, null);

        if (claim == null) {
            return;
        }

        if (claim.allowContainers(player) != null) {
            event.setCancelled(true);
            player.sendMessage(claim.allowContainers(player));
        }
    }

}