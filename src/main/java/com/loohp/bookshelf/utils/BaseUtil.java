package com.loohp.bookshelf.utils;

import org.bukkit.ChatColor;

public class BaseUtil {

    private BaseUtil() {
    }

    public static String fixColor(String toColor) {
        return ChatColor.translateAlternateColorCodes('&', toColor);
    }

}
