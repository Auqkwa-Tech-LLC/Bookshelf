package com.loohp.bookshelf.listeners.hooks;

import com.google.common.collect.Lists;
import com.loohp.bookshelf.Bookshelf;
import com.loohp.bookshelf.api.Events.PlayerOpenBookshelfEvent;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.RegionResultSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Effect;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.List;

public class WorldGuardEvents implements Listener {

    private static final String youCannotDoThatMessage = ChatColor.RED + "" + ChatColor.BOLD + "Hey!" + ChatColor.GRAY + " Sorry, but you can't open that here.";

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldGuardCheck(PlayerOpenBookshelfEvent event) {

        if (!Bookshelf.WorldGuardHook) {
            return;
        }

        Player player = event.getPlayer();

        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        boolean canBypass = WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer, localPlayer.getWorld());
        if (canBypass) {
            return;
        }

        com.sk89q.worldedit.util.Location loc = BukkitAdapter.adapt(event.getLocation().clone());
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(loc);

        boolean isGlobal = false;
        if (set.size() == 0) {
            isGlobal = true;
        }

        if (testFlag(query, Flags.CHEST_ACCESS, loc, localPlayer).equals("false")) {
            event.setCancelled(true);
            player.sendMessage(youCannotDoThatMessage);
            player.playEffect(event.getLocation().clone().add(0, 1, 0), Effect.SMOKE, 4);
        } else {
            if (testFlag(query, Flags.BUILD, loc, localPlayer).equals("false")) {
                event.setCancelled(true);
                player.sendMessage(youCannotDoThatMessage);
                player.playEffect(event.getLocation().clone().add(0, 1, 0), Effect.SMOKE, 4);
            } else {
                List<ProtectedRegion> regions = Lists.newArrayList();
                if (!isGlobal) {

                    regions.addAll(set.getRegions());

                    ApplicableRegionSet setNoGlobal = new RegionResultSet(regions, null);

                    if (setTestFlag(setNoGlobal, Flags.PASSTHROUGH, localPlayer).equals("false")) {
                        event.setCancelled(true);
                        player.sendMessage(youCannotDoThatMessage);
                        player.playEffect(event.getLocation().clone().add(0, 1, 0), Effect.SMOKE, 4);
                    } else if (setTestFlag(setNoGlobal, Flags.PASSTHROUGH, localPlayer).equals("null")) {
                        event.setCancelled(true);
                        player.sendMessage(youCannotDoThatMessage);
                        player.playEffect(event.getLocation().clone().add(0, 1, 0), Effect.SMOKE, 4);
                    }
                } else {
                    regions.add(WorldGuard.getInstance().getPlatform().getRegionContainer().get(localPlayer.getWorld()).getRegion("__global__"));
                    ApplicableRegionSet setOnlyGlobal = new RegionResultSet(regions, null);
                    if (setTestFlag(setOnlyGlobal, Flags.PASSTHROUGH, localPlayer).equals("false")) {
                        event.setCancelled(true);
                        player.sendMessage(youCannotDoThatMessage);
                        player.playEffect(event.getLocation().clone().add(0, 1, 0), Effect.SMOKE, 4);
                    }
                }
            }
        }
    }

    public String testFlag(RegionQuery query, StateFlag flag, com.sk89q.worldedit.util.Location loc, LocalPlayer localPlayer) {
        if (query.queryState(loc, localPlayer, flag) == null) {
            return "null";
        } else {
            if (query.queryState(loc, localPlayer, flag).equals(State.ALLOW)) {
                return "true";
            } else {
                return "false";
            }
        }
    }

    public String setTestFlag(ApplicableRegionSet set, StateFlag flag, LocalPlayer localPlayer) {
        if (set.queryState(localPlayer, flag) == null) {
            return "null";
        } else {
            if (set.queryState(localPlayer, flag).equals(State.ALLOW)) {
                return "true";
            } else {
                return "false";
            }
        }
    }

}
