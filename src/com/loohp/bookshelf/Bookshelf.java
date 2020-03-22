package com.loohp.bookshelf;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.loohp.bookshelf.Debug.Debug;
import com.loohp.bookshelf.Listeners.Events;
import com.loohp.bookshelf.Listeners.GPEvents;
import com.loohp.bookshelf.Listeners.LWCEvents;
import com.loohp.bookshelf.Metrics.Metrics;
import com.loohp.bookshelf.Utils.BookshelfUtils;
import com.loohp.bookshelf.Utils.HopperUtils;
import com.loohp.bookshelf.Utils.LegacyConfigConverter;
import com.loohp.bookshelf.Utils.ParticlesUtils;

public class Bookshelf extends JavaPlugin {
	
	public static Plugin plugin = null;
	
	public static String version = "";
	
	public static FileConfiguration config;
	public static File cfile;
	
	public static boolean LWCHook = false;
	public static boolean WGHook = false;
	public static boolean GPHook = false;
	public static boolean BlockLockerHook = false;
	
	public static boolean EnableHopperDropperSupport = true;
	public static int HopperTaskID = -1;
	public static int HopperMinecartTaskID = -1;
	public static long HopperTicksPerTransfer = 8;
	public static long HopperAmount = 1;
	
	public static Map<String, Inventory> bookshelfContent = new HashMap<String, Inventory>();
	public static List<String> bookshelfSavePending = new ArrayList<String>();
	
	public static Map<Player, BlockFace> lastBlockFace = new HashMap<Player, BlockFace>();
	
	public static Map<Player, String> requestOpen = new HashMap<Player, String>();
	
	public static long BookShelfRows = 2;
	public static boolean UseWhitelist = true;
	public static String Title = "Bookshelf";
	public static List<String> Whitelist = new ArrayList<String>();
	public static boolean particlesEnabled = true;
	
	public static String NoPermissionToReloadMessage = "&cYou do not have permission use this command!";
	
	public static List<Player> cancelOpen = new ArrayList<Player>();
	public static List<Player> isDonationView = new ArrayList<Player>();
	
	public static List<String> isEmittingParticle = new ArrayList<String>();
	
	public static Map<Long, Location> tempRedstone = new HashMap<Long, Location>();

	private static long spawnchunks = 0;
	private static long done = 0;

	@Override
	public void onEnable() {	
		plugin = (Plugin)getServer().getPluginManager().getPlugin("BookShelf");
		
		getServer().getPluginManager().registerEvents(new Debug(), this);
		
		int pluginId = 6748;

		Metrics metrics = new Metrics(this, pluginId);
	    
	    getServer().getPluginManager().registerEvents(new Events(), this);
	    
	    getCommand("bookshelf").setExecutor(new Commands());
		
	    plugin.getConfig().options().copyDefaults(true);
	    plugin.saveConfig();

	    //v2.0.0 upgrade
	    if (plugin.getConfig().contains("BookShelfData")) {
	    	LegacyConfigConverter.convert();
	    }
	    //------
	    
	    if (Bukkit.getServer().getPluginManager().getPlugin("GriefPrevention") != null) {
	    	Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[Bookshelf] Hooked into GriefPrevention!");
	    	getServer().getPluginManager().registerEvents(new GPEvents(), this);
			GPHook = true;
		}
		
		if (Bukkit.getServer().getPluginManager().getPlugin("LWC") != null) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[Bookshelf] Hooked into LWC!");
			LWCEvents.hookLWC();
			LWCHook = true;
		}
		
		if (Bukkit.getServer().getPluginManager().getPlugin("BlockLocker") != null) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[Bookshelf] Hooked into BlockLocker!");
			BlockLockerHook = true;
		}
		
		if (Bukkit.getServer().getPluginManager().getPlugin("WorldGuard") != null) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[Bookshelf] Hooked into WorldGuard!");
			getServer().getPluginManager().registerEvents(new com.loohp.bookshelf.Listeners.WGEvents(), this);
			WGHook = true;
		}
		
		if (getServer().getClass().getPackage().getName().contains("1_15_R1") == true) {
	    	version = "1.15";
	    } else if (getServer().getClass().getPackage().getName().contains("1_14_R1") == true) {
	    	version = "1.14";
	    } else if (getServer().getClass().getPackage().getName().contains("1_13_R2") == true) {
	    	version = "1.13.1";
	    } else if (getServer().getClass().getPackage().getName().contains("1_13_R1") == true) {
	    	version = "1.13";
	    } else if (getServer().getClass().getPackage().getName().contains("1_12_R1") == true) {
	    	version = "legacy1.12";
	    } else if (getServer().getClass().getPackage().getName().contains("1_11_R1") == true) {
	    	version = "legacy1.11";
	    } else if (getServer().getClass().getPackage().getName().contains("1_10_R1") == true) {
	    	version = "legacy1.10";
	    } else if (getServer().getClass().getPackage().getName().contains("1_9_R2") == true) {
	    	version = "legacy1.9.4";
	    } else if (getServer().getClass().getPackage().getName().contains("1_9_R1") == true) {
	    	version = "legacy1.9";
	    } else if (getServer().getClass().getPackage().getName().contains("1_8_R3") == true) {
	    	version = "OLDlegacy1.8.4";
	    } else if (getServer().getClass().getPackage().getName().contains("1_8_R2") == true) {
	    	version = "OLDlegacy1.8.3";
	    } else if (getServer().getClass().getPackage().getName().contains("1_8_R1") == true) {
	    	version = "OLDlegacy1.8";
	    } else {
	    	getServer().getConsoleSender().sendMessage(ChatColor.RED + "[Bookshelf] This version of minecraft is unsupported!");
	    	plugin.getPluginLoader().disablePlugin(this);
	    }
	    
	    loadConfig();
	    
	    BookshelfManager.reload();
	    
	    intervalSave();
	    particles();
	    loadBookshelf();
	    
	    metrics.addCustomChart(new Metrics.SingleLineChart("total_bookshelves", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return BookshelfManager.getJsonObject().size();
            }
        }));
		
		getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[Bookshelf] BookShelf has been Enabled!");
	}

	@Override
	public void onDisable() {
		getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "[Bookshelf] Saving all pending bookshelves..");
		long start = System.currentTimeMillis();
		for (String entry : Bookshelf.bookshelfSavePending) {
			if (Bookshelf.bookshelfContent.containsKey(entry)) {
				BookshelfUtils.saveBookShelf(entry);
			}
		}
		BookshelfManager.save();
		getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "[Bookshelf] Bookshelves saved! (" + (System.currentTimeMillis() - start) + "ms)");
		getServer().getConsoleSender().sendMessage(ChatColor.RED + "[Bookshelf] BookShelf has been Disabled!");
	}
	
	public static void loadConfig() {	
		Bookshelf.BookShelfRows = Bookshelf.plugin.getConfig().getLong("Options.BookShelfRows");
		Bookshelf.UseWhitelist = Bookshelf.plugin.getConfig().getBoolean("Options.UseWhitelist");
		Bookshelf.Whitelist = Bookshelf.plugin.getConfig().getStringList("Options.Whitelist");
		Bookshelf.Title = ChatColor.translateAlternateColorCodes('&', Bookshelf.plugin.getConfig().getString("Options.Title"));
		Bookshelf.NoPermissionToReloadMessage = Bookshelf.plugin.getConfig().getString("Options.NoPermissionToReloadMessage");
		Bookshelf.particlesEnabled = Bookshelf.plugin.getConfig().getBoolean("Options.ParticlesWhenOpened");
		Bookshelf.EnableHopperDropperSupport = Bookshelf.plugin.getConfig().getBoolean("Options.EnableHopperDropperSupport");
		if (Bookshelf.HopperTaskID >= 0) {
			Bukkit.getScheduler().cancelTask(Bookshelf.HopperTaskID);
		}
		if (Bookshelf.HopperMinecartTaskID >= 0) {
			Bukkit.getScheduler().cancelTask(Bookshelf.HopperMinecartTaskID);
		}
		if (Bookshelf.EnableHopperDropperSupport == true) {
			Bookshelf.HopperTicksPerTransfer = Bukkit.spigot().getConfig().getLong("world-settings.default.ticks-per.hopper-transfer");
			Bookshelf.HopperAmount = Bukkit.spigot().getConfig().getLong("world-settings.default.hopper-amount");
			HopperUtils.hopperCheck();
			HopperUtils.hopperMinecartCheck();
		}
	}
	
	public void loadBookshelf() {
		Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[Bookshelf] Counting spawn chunks");
		spawnchunks = 0;
		done = 0;
		for (World world : Bukkit.getWorlds()) {
			spawnchunks = spawnchunks + world.getLoadedChunks().length;
		}
		loadBookshelfProgress();
		Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[Bookshelf] Loading bookshelves in spawn chunks");
		long start = System.currentTimeMillis();
		for (World world : Bukkit.getWorlds()) {
			for (Chunk chunk: world.getLoadedChunks()) {
				for (Block block : BookshelfUtils.getAllBookshelvesInChunk(chunk)) {
					String loc = BookshelfUtils.locKey(block.getLocation());
					if (!Bookshelf.bookshelfContent.containsKey(loc)) {
						if (!BookshelfManager.contains(loc)) {
							String bsTitle = Bookshelf.Title;
							Bookshelf.bookshelfContent.put(loc , Bukkit.createInventory(null, (int) (Bookshelf.BookShelfRows * 9), bsTitle));
							BookshelfManager.setTitle(loc, bsTitle);
							BookshelfUtils.saveBookShelf(loc);
						} else {
							BookshelfUtils.loadBookShelf(loc);
						}
					}
				}
				done = done + 1;
			}
		}
		BookshelfManager.save();
		BookshelfManager.intervalSaveToFile();
		Bukkit.getConsoleSender().sendMessage("[Bookshelf] Preparing bookshelves in spawn chunks: 100%");
		Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[Bookshelf] Bookshelves loaded! (" + (System.currentTimeMillis() - start) + "ms)");
	}
	
	public void loadBookshelfProgress() {
		CompletableFuture.runAsync(()->{
			while (done < spawnchunks) {
				Bukkit.getConsoleSender().sendMessage("[Bookshelf] Preparing bookshelves in spawn chunks: " + Math.round((double) ((double) done / (double) spawnchunks) * 100) + "%");
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Error");
				}
			}
		});
	}
	
	public void intervalSave() {
		new BukkitRunnable() {
			public void run() {
				List<String> removeList = new ArrayList<String>();
				for (String entry : Bookshelf.bookshelfSavePending) {
					if (Bookshelf.bookshelfContent.containsKey(entry)) {
						if (!removeList.contains(entry)) {
							BookshelfUtils.saveBookShelf(entry);
						}
					}
					removeList.add(entry);
				}
				Bookshelf.bookshelfSavePending.clear();
				removeList.clear();
			}
		}.runTaskTimer(this, 0, 40);
	}
	
	public void particles() {
		new BukkitRunnable() {
			public void run() {
				if (Bookshelf.particlesEnabled == true && !Bookshelf.version.contains("legacy")) {
					Bookshelf.isEmittingParticle.clear();
					for (Player player : Bukkit.getOnlinePlayers()) {
						if (player.getOpenInventory() != null) {
							for (Entry<String, Inventory> entry : Bookshelf.bookshelfContent.entrySet()) {
								if (!Bookshelf.isEmittingParticle.contains(entry.getKey())) {
									if (entry.getValue().equals(player.getOpenInventory().getTopInventory())) {
										Location loc = BookshelfUtils.keyLoc(entry.getKey());
										Location loc2 = loc.clone().add(1,1,1);
										DustOptions purple = new DustOptions(Color.fromRGB(153, 51, 255), 1);
										DustOptions yellow = new DustOptions(Color.fromRGB(255, 255, 0), 1);
										for (Location pos : ParticlesUtils.getHollowCube(loc.add(-0.0625, -0.0625, -0.0625), loc2.add(0.0625, 0.0625, 0.0625), 0.1666)) {
											double random = Math.random() * 100;
											if (random > 95) {
												double ranColor = Math.floor(Math.random() * 2) + 1;
												if (ranColor == 1) {
													loc.getWorld().spawnParticle(Particle.REDSTONE, pos, 1, yellow);
												} else if (ranColor == 2) {
													loc.getWorld().spawnParticle(Particle.REDSTONE, pos, 1, purple);
												}
											}
										}
										Bookshelf.isEmittingParticle.add(entry.getKey());
										break;
									}
								}
							}
						}
					}
				}
			}
		}.runTaskTimerAsynchronously(this, 0, 5);
	}
}