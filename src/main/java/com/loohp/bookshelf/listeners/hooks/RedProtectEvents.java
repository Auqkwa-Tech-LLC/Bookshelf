package com.loohp.bookshelf.listeners.hooks;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import com.loohp.bookshelf.Bookshelf;
import com.loohp.bookshelf.api.Events.PlayerOpenBookshelfEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class RedProtectEvents implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRedProtectCheck(PlayerOpenBookshelfEvent event) {

        if (!Bookshelf.RedProtectHook) {
            return;
        }

        Player player = event.getPlayer();
        RedProtect redProtect = RedProtect.get();

        if (redProtect.getAPI().getRegion(event.getLocation()) == null) {
            return;
        }

        if (!redProtect.getAPI().getRegion(event.getLocation()).canChest(player)) {
            player.sendMessage(redProtect.getLanguageManager().get("_redprotect.prefix") + " " + redProtect.getLanguageManager().get("playerlistener.region.cantdoor"));
            event.setCancelled(true);
        }
    }

}
