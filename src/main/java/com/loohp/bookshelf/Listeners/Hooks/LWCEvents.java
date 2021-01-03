package com.loohp.bookshelf.Listeners.Hooks;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Permission.Access;
import com.griefcraft.model.Protection;
import com.griefcraft.model.Protection.Type;
import com.griefcraft.scripting.Module;
import com.griefcraft.scripting.event.*;
import com.loohp.bookshelf.API.Events.PlayerOpenBookshelfEvent;
import com.loohp.bookshelf.Bookshelf;
import com.loohp.bookshelf.ObjectHolders.LWCRequestOpenData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class LWCEvents implements Module {

    public static void hookLWC() {
        LWC.getInstance().getModuleLoader().registerModule(Bookshelf.plugin, new LWCEvents());
    }

    @Override
    public void onReload(LWCReloadEvent event) {
    }

    @Override
    public void load(LWC event) {
    }

    @Override
    public void onAccessRequest(LWCAccessEvent event) {
        if (!event.getPlayer().hasPermission("bookshelf.use")) {
            return;
        }
        Bukkit.getScheduler().runTaskLater(Bookshelf.plugin, () -> {
            Player player = event.getPlayer();
            if (!Bookshelf.requestOpen.containsKey(player)) {
                return;
            }
            LWCRequestOpenData data = Bookshelf.requestOpen.get(player);
            String loc = data.getKey();
            Protection protection = event.getProtection();
            if (LWC.getInstance().getPlugin().getLWC().canAccessProtection(player, protection) || !event.getAccess().equals(Access.NONE)) {
                if (event.getProtection().getType().equals(Type.DONATION)) {
                    if (!Bookshelf.isDonationView.contains(player.getUniqueId())) {
                        Bookshelf.isDonationView.add(player.getUniqueId());
                    }
                }

                PlayerOpenBookshelfEvent pobe = new PlayerOpenBookshelfEvent(player, loc, data.getBlockFace(), data.isCancelled());
                Bukkit.getPluginManager().callEvent(pobe);

                if (!pobe.isCancelled()) {
                    Inventory inv = Bookshelf.keyToContentMapping.get(loc);
                    Bukkit.getScheduler().runTask(Bookshelf.plugin, () -> player.openInventory(inv));
                    if (!Bookshelf.bookshelfSavePending.contains(loc)) {
                        Bookshelf.bookshelfSavePending.add(loc);
                    }
                }
            }
            Bookshelf.requestOpen.remove(player);
        }, 1);
    }

    @Override
    public void onBlockInteract(LWCBlockInteractEvent event) {
    }

    @Override
    public void onCommand(LWCCommandEvent event) {
    }

    @Override
    public void onDestroyProtection(LWCProtectionDestroyEvent event) {
    }

    @Override
    public void onDropItem(LWCDropItemEvent event) {
    }

    @Override
    public void onEntityInteract(LWCEntityInteractEvent event) {
    }

    @Override
    public void onEntityInteractProtection(LWCProtectionInteractEntityEvent event) {
    }

    @Override
    public void onMagnetPull(LWCMagnetPullEvent event) {
    }

    @Override
    public void onPostRegistration(LWCProtectionRegistrationPostEvent event) {
    }

    @Override
    public void onPostRemoval(LWCProtectionRemovePostEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        Bookshelf.lwcCancelOpen.add(player.getUniqueId());
        Bukkit.getScheduler().runTaskLater(Bookshelf.plugin, () -> Bookshelf.lwcCancelOpen.remove(player.getUniqueId()), 5);
    }

    @Override
    public void onProtectionInteract(LWCProtectionInteractEvent event) {
        Player player = event.getPlayer();
        if (!event.getResult().equals(Result.CANCEL)) {
            return;
        }
        if (player == null) {
            return;
        }
        Bookshelf.lwcCancelOpen.add(player.getUniqueId());
        Bukkit.getScheduler().runTaskLater(Bookshelf.plugin, () -> Bookshelf.lwcCancelOpen.remove(player.getUniqueId()), 5);
    }

    @Override
    public void onRedstone(LWCRedstoneEvent event) {
    }

    @Override
    public void onRegisterEntity(LWCProtectionRegisterEntityEvent event) {
    }

    @Override
    public void onRegisterProtection(LWCProtectionRegisterEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        Bookshelf.lwcCancelOpen.add(player.getUniqueId());
        Bukkit.getScheduler().runTaskLater(Bookshelf.plugin, () -> Bookshelf.lwcCancelOpen.remove(player.getUniqueId()), 5);
    }

    @Override
    public void onSendLocale(LWCSendLocaleEvent event) {
    }
}
