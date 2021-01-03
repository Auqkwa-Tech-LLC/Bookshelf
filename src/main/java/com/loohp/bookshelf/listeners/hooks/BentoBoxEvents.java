package com.loohp.bookshelf.listeners.hooks;

import com.loohp.bookshelf.Bookshelf;
import com.loohp.bookshelf.api.Events.PlayerOpenBookshelfEvent;
import com.loohp.bookshelf.utils.BaseUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.eclipse.jdt.annotation.Nullable;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.lists.Flags;

import java.util.Optional;

public class BentoBoxEvents implements Listener {

    private @Nullable String replacement;
    private @Nullable String message;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBentoBoxCheck(PlayerOpenBookshelfEvent event) {

        if (!Bookshelf.BentoBoxHook) {
            return;
        }

        Player player = event.getPlayer();
        Location location = event.getLocation();

        User user = User.getInstance(player);
        Optional<Island> optionalIsland = BentoBox.getInstance().getIslands().getIslandAt(location);

        if (!optionalIsland.isPresent()) {
            return;
        }

        if (!optionalIsland.get().isAllowed(user, Flags.CONTAINER)) {
            if (replacement == null || message == null) {
                replacement = BentoBox.getInstance().getLocalesManager().get("protection.flags.CONTAINER.hint");
                message = BentoBox.getInstance().getLocalesManager().get("protection.protected");
            } else {
                player.sendMessage(BaseUtil.fixColor(message.replace("[description]", replacement)));
                event.setCancelled(true);
            }
        }
    }

}
