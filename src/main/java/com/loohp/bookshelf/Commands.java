package com.loohp.bookshelf;

import com.loohp.bookshelf.utils.BaseUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Commands implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, String label, String[] args) {
        if (!label.equalsIgnoreCase("bookshelf")) {
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.GOLD + "[Bookshelf] You are running BookShelf version: " + Bookshelf.plugin.getDescription().getVersion());
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (sender.hasPermission("bookshelf.reload")) {
                Bookshelf.plugin.reloadConfig();
                Bookshelf.loadConfig();
                sender.sendMessage(ChatColor.GREEN + "[Bookshelf] BookShelf has been reloaded!");
            } else {
                sender.sendMessage(Bookshelf.youDoNotHavePermissionUseThisCommand);
            }
            return true;
        }

        sender.sendMessage(BaseUtil.fixColor(Bukkit.spigot().getConfig().getString("messages.unknown-command")));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, String label, String[] args) {
        List<String> tab = new ArrayList<>();
        if (!label.equalsIgnoreCase("bookshelf")) {
            return tab;
        }

        if (args.length <= 1) {
            if (sender.hasPermission("bookshelf.reload")) {
                tab.add("reload");
            }
            if (sender.hasPermission("bookshelf.update")) {
                tab.add("update");
            }
            return tab;
        }
        return tab;
    }
}
