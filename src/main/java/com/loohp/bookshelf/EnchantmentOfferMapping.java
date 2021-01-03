package com.loohp.bookshelf;

import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class EnchantmentOfferMapping {

    public static final ConcurrentHashMap<Player, HashMap<ItemStack, EnchantmentOffer[]>> enchantOffers = new ConcurrentHashMap<>();

}
