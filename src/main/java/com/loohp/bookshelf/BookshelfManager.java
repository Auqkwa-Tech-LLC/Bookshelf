package com.loohp.bookshelf;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;

public class BookshelfManager {

    private static File file;
    private static JSONObject json;
    private static final JSONParser parser = new JSONParser();
    private static final File BackupFolder = new File(Bookshelf.plugin.getDataFolder().getPath() + "/Backup", "bookshelf");

    public static void intervalSaveToFile() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(Bookshelf.plugin, BookshelfManager::save, 200, 600);
    }

    public synchronized static void reload() {
        if (!Bookshelf.plugin.getDataFolder().exists()) {
            if (Bookshelf.plugin.getDataFolder().mkdir()) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[Bookshelf] Created Bookshelf datafolder");
            }
        }

        file = new File(Bookshelf.plugin.getDataFolder().getAbsolutePath() + "/bookshelfdata.json");

        if (!file.exists()) {
            try (PrintWriter pw = new PrintWriter(file, "UTF-8")) {
                pw.print("{");
                pw.print("}");
                pw.flush();
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            String fileName = new SimpleDateFormat("yyyy'-'MM'-'dd'_'HH'-'mm'-'ss'_'zzz'_bookshelfdata.json'").format(new Date()).replace(":", ";");

            if (BackupFolder.mkdirs()) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[Bookshelf] Creating Bookshelf backupfolder");
            }

            File outfile = new File(BackupFolder, fileName);

            try (InputStream in = new FileInputStream(file)) {
                Files.copy(in, outfile.toPath());
            } catch (IOException e) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[Bookshelf] Failed to make backup for bookshelfdata.json");
            }
        }
        if (BackupFolder.exists()) {
            for (File file : BackupFolder.listFiles()) {
                try {
                    String fileName = file.getName();
                    if (fileName.matches("^[0-9]{4}-[0-9]{2}-[0-9]{2}_[0-9]{2}-[0-9]{2}-[0-9]{2}_.*_bookshelfdata\\.json$")) {
                        Date timestamp = new SimpleDateFormat("yyyy'-'MM'-'dd'_'HH'-'mm'-'ss'_'zzz'_bookshelfdata.json'").parse(fileName.replace(";", ":"));
                        if ((System.currentTimeMillis() - timestamp.getTime()) > 2592000000L) {
                            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[Bookshelf] Removing Backup/Backup/" + fileName + " as it is from 30 days ago.");
                            Files.delete(file.toPath());
                        }
                    }
                } catch (java.text.ParseException | IOException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            json = (JSONObject) parser.parse(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized static void save() {
        try {
            JSONObject toSave = json;

            TreeMap<String, Object> treeMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            treeMap.putAll(toSave);

            Gson g = new GsonBuilder().setPrettyPrinting().create();
            String prettyJsonString = g.toJson(treeMap);

            try (PrintWriter clear = new PrintWriter(file)) {
                clear.print("");
            }

            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                writer.write(prettyJsonString);
                writer.flush();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static JSONObject getJsonObject() {
        return json;
    }

    public static String getTitle(String key) {
        if (!json.containsKey(key)) {
            return null;
        }
        JSONObject value = (JSONObject) json.get(key);
        if (value.containsKey("Title")) {
            return (String) value.get("Title");
        }
        return null;
    }

    public static String getInventoryHash(String key) {
        if (!json.containsKey(key)) {
            return null;
        }
        JSONObject value = (JSONObject) json.get(key);
        if (value.containsKey("Inventory")) {
            return (String) value.get("Inventory");
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static void setTitle(String key, String title) {
        if (!json.containsKey(key)) {
            json.put(key, new JSONObject());
        }
        JSONObject value = (JSONObject) json.get(key);
        value.put("Title", title);
    }

    @SuppressWarnings("unchecked")
    public static void setInventoryHash(String key, String hash) {
        if (!json.containsKey(key)) {
            json.put(key, new JSONObject());
        }
        JSONObject value = (JSONObject) json.get(key);
        value.put("Inventory", hash);
    }

    public static void removeShelf(String key) {
        json.remove(key);
    }

    public static boolean contains(String key) {
        return json.containsKey(key);
    }
}
