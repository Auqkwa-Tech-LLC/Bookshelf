package com.loohp.bookshelf.listeners.hooks;

import com.loohp.bookshelf.Bookshelf;
import com.loohp.bookshelf.api.Events.PlayerOpenBookshelfEvent;
import me.angeschossen.lands.api.integration.LandsIntegration;
import me.angeschossen.lands.api.land.Area;
import me.angeschossen.lands.api.role.enums.RoleSetting;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class LandEvents implements Listener {

    private static LandsIntegration landsAddon;

    public static void setup() {
        landsAddon = (landsAddon == null) ? new LandsIntegration(Bookshelf.plugin) : landsAddon;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLandCheck(PlayerOpenBookshelfEvent event) {

        if (!Bookshelf.LandHook) {
            return;
        }

        Player player = event.getPlayer();

        Area area = landsAddon.getAreaByLoc(event.getLocation());

        if (area == null) {
            return;
        }

        if (!area.canSetting(player, RoleSetting.INTERACT_CONTAINER, true)) {
            event.setCancelled(true);
        }

    }

}
