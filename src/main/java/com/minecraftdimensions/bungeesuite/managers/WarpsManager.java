package com.minecraftdimensions.bungeesuite.managers;

import com.minecraftdimensions.bungeesuite.BungeeSuite;
import com.minecraftdimensions.bungeesuite.configs.MainConfig;
import com.minecraftdimensions.bungeesuite.objects.BSPlayer;
import com.minecraftdimensions.bungeesuite.objects.Location;
import com.minecraftdimensions.bungeesuite.objects.Messages;
import com.minecraftdimensions.bungeesuite.objects.Warp;
import com.minecraftdimensions.bungeesuite.redis.RedisManager;
import com.minecraftdimensions.bungeesuite.tasks.SendPluginMessage;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.config.ServerInfo;

import java.io.ByteArrayOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;

public class WarpsManager {
    static HashMap<String, Warp> warps;
    public static String OUTGOING_CHANNEL = "bsuite:warps-out";


    public static void loadWarpLocations() throws SQLException {
        warps = new HashMap<String, Warp>();
        ResultSet res = SQLManager.sqlQuery("SELECT * FROM BungeeWarps");
        while (res.next()) {
            createWarp(res.getString("warpname"), new Location(res.getString("server"), res.getString("world"), res.getDouble("x"), res.getDouble("y"), res.getDouble("z"), res.getFloat("yaw"), res.getFloat("pitch")), res.getBoolean("hidden"), res.getBoolean("global"));
        }
        res.close();
    }

    public static void createWarp(String name, Location loc, boolean hidden, boolean global) {
        Warp s = new Warp(name, loc, hidden, global);
        warps.put(name.toLowerCase(), s);
    }

    public static void setWarp(BSPlayer sender, String name, Location loc, boolean hidden, boolean global) throws SQLException {
        Warp w;
        if (doesWarpExist(name)) {
            w = warps.get(name);
            w.setLocation(loc);
            w.setGlobal(global);
            w.setHidden(hidden);
            SQLManager.standardQuery("UPDATE BungeeWarps SET server='" + loc.getServer().getName() + "', world='" + loc.getWorld() + "', x=" + loc.getX() + ", y=" + loc.getY() + ", z=" + loc.getZ() + ", yaw=" + loc.getYaw() + ", pitch= " + loc.getPitch() + ", hidden=" + hidden + ", global = " + global + " WHERE warpname='" + name + "'");
            sender.sendMessage(Messages.WARP_UPDATED);
        } else {
            w = new Warp(name, loc, hidden, global);
            warps.put(name.toLowerCase(), w);
            SQLManager.standardQuery("INSERT INTO BungeeWarps (warpname, server, world, x, y, z, yaw, pitch, hidden, global)VALUES ('" + name + "', '" + loc.getServer().getName() + "','" + loc.getWorld() + "'," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch() + ", " + hidden + ", " + global + ")");
            sender.sendMessage(Messages.WARP_CREATED);
        }
    }

    public static void deleteWarp(BSPlayer sender, String warp) throws SQLException {
        if (doesWarpExist(warp)) {
            Warp w = getWarp(warp.toLowerCase());
            warps.remove(w.getName().toLowerCase());
            SQLManager.standardQuery("DELETE FROM BungeeWarps WHERE warpname='" + w.getName() + "'");
            sender.sendMessage(Messages.WARP_DELETED);
        } else {
            sender.sendMessage(Messages.WARP_DOES_NOT_EXIST);
        }
    }

    public static Warp getWarp(String name) {
        return warps.get(name);
    }

    public static Warp getSimilarWarp(String name) {
        if (warps.containsKey(name)) {
            return warps.get(name);
        }
        for (String warp : warps.keySet()) {
            if (warp.toLowerCase().equals(name)) {
                return warps.get(warp);
            }
        }
        return null;
    }

    public static boolean doesWarpExist(String name) {
        return warps.containsKey(name.toLowerCase());
    }

    public static void getWarpsList(String sender, boolean server, boolean global, boolean hidden, boolean bypass) {
        BSPlayer s = PlayerManager.getPlayer(sender);
        if (!(server || global || hidden)) {
            s.sendMessage(ChatColor.RED + "No warps to display");
        }
        String serverString = ChatColor.GOLD + "Server warps: \n";
        String globalString = ChatColor.GOLD + "Global warps: \n";
        String hiddenString = ChatColor.GOLD + "Hidden warps: \n";
        for (Warp w : warps.values()) {
            if (w.isGlobal()) {
                globalString += w.getName() + ", ";
            } else if (w.isHidden()) {
                hiddenString += w.getName() + ", ";
            } else if (s.getServer().getInfo().equals(w.getLocation().getServer())) {
                serverString += w.getName() + ", ";
            } else if (bypass) {
                globalString += w.getName() + ", ";
            }
        }
        if (server) {
            if (serverString.length() == 17) {
                serverString += ChatColor.RED + " none  ";
            }
            s.sendMessage(serverString.substring(0, serverString.length() - 2));
        }
        if (global) {
            if (globalString.length() == 17) {
                globalString += ChatColor.RED + " none  ";
            }

            s.sendMessage(globalString.substring(0, globalString.length() - 2));
        }
        if (hidden) {
            if (hiddenString.length() == 17) {
                hiddenString += ChatColor.RED + " none  ";
            }
            s.sendMessage(hiddenString.substring(0, hiddenString.length() - 2));
        }
    }


    public static void sendPlayerToWarp(String sender, String player, String warp, boolean permission, boolean bypass, int cd) {

        BSPlayer s = PlayerManager.getPlayer(sender);
        BSPlayer p = PlayerManager.getSimilarPlayer(player);
        if (p == null) {
            if (!sender.equalsIgnoreCase("CONSOLE")) {
                s.sendMessage(Messages.PLAYER_NOT_ONLINE);
            }
            return;
        }
        Warp w = warps.get(warp.toLowerCase());
        if (w == null) {
            if (!sender.equalsIgnoreCase("CONSOLE")) {
                s.sendMessage(Messages.WARP_DOES_NOT_EXIST);
            }
            return;
        }
        if (!permission) {
            if (!sender.equalsIgnoreCase("CONSOLE")) {
                s.sendMessage(Messages.WARP_NO_PERMISSION);
            }
            return;
        }
        if (!w.isGlobal() && !w.isHidden()) {
            if (!w.getLocation().getServer().equals(p.getServer().getInfo()) && !bypass) {
                if (!sender.equalsIgnoreCase("CONSOLE")) {
                    s.sendMessage(Messages.WARP_SERVER);
                }
                return;
            }
        }
        if(MainConfig.cooldownEnabled){
            if (!sender.equalsIgnoreCase("CONSOLE")) {
                if (CooldownManager.getInstance().isOnCooldown("WARP", cd, s.getProxiedPlayer().getUniqueId())) {
                    s.sendMessage(Messages.COOLDOWN.replace("{cooldown}", ""));
                    return;
                }
            }
            CooldownManager.getInstance().setCooldown("WARP", s.getProxiedPlayer().getUniqueId(), LocalDateTime.now());
        }
        Location l = w.getLocation();
        p.sendMessage(Messages.PLAYER_WARPED.replace("{warp}", w.getName()));
        if (!p.equals(s)) {
            if (!sender.equalsIgnoreCase("CONSOLE")) {
                s.sendMessage(Messages.PLAYER_WARPED_OTHER.replace("{player}", p.getName()).replace("{warp}", w.getName()));
            }
        }
        StringBuilder stringToSend = new StringBuilder();
        stringToSend.append("TeleportPlayerToLocation;");
        stringToSend.append(l.getServer().getName()).append(";");
        stringToSend.append(p.getName()).append(";");
        stringToSend.append(l.getWorld()).append(";");
        stringToSend.append(l.getX()).append(";");
        stringToSend.append(l.getY()).append(";");
        stringToSend.append(l.getZ()).append(";");
        stringToSend.append(l.getYaw()).append(";");
        stringToSend.append(l.getPitch());
        RedisManager.getInstance().publish(stringToSend.toString(), "WARP_RESPONSE");
        if (!l.getServer().equals(p.getServer().getInfo())) {
            p.sendToServer(l.getServer().getName());
        }

    }

    public static void sendPluginMessageTaskTP(ServerInfo server, ByteArrayOutputStream b) {
        BungeeSuite.proxy.getScheduler().runAsync(BungeeSuite.instance, new SendPluginMessage(OUTGOING_CHANNEL, server, b));
    }
}

