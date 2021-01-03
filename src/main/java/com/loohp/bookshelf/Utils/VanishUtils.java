package com.loohp.bookshelf.Utils;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.earth2me.essentials.Essentials;
import com.loohp.bookshelf.Bookshelf;
import de.myzelyam.api.vanish.VanishAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class VanishUtils {

    private static Essentials essentials = null;

    public static boolean isVanished(Player player) {
        if (essentials == null) {
            essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
        }

        if (Bookshelf.VanishHook) {
            if (VanishAPI.isInvisible(player)) {
                return true;
            }
        }
        if (Bookshelf.CMIHook) {
            CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
            if (user.isVanished()) {
                return true;
            }
        }
        if (Bookshelf.EssentialsHook) {

            if (essentials != null) {
                return essentials.getUser(player).isVanished();
            }
        }
        return false;
    }

}



