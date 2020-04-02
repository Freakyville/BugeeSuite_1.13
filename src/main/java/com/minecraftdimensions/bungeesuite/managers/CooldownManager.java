package com.minecraftdimensions.bungeesuite.managers;

import net.md_5.bungee.api.ProxyServer;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {
    private static CooldownManager ourInstance = new CooldownManager();

    public static CooldownManager getInstance() {
        return ourInstance;
    }

    private CooldownManager() {

    }

    private Map<UUID, Map<String, LocalDateTime>> cooldowns = new HashMap<>();

    public void setCooldown(String type, UUID uuid, LocalDateTime time) {
        this.cooldowns.putIfAbsent(uuid, new HashMap<>());
        this.cooldowns.get(uuid).put(type, time);
    }

    public boolean isOnCooldown(String type, int cooldownTime, UUID uuid) {
        if (!this.cooldowns.containsKey(uuid) || !this.cooldowns.get(uuid).containsKey(type)) {
            return false;
        }
        LocalDateTime cooldown = this.cooldowns.get(uuid).get(type);
        if (cooldown != null) {
            Duration duration = Duration.between(cooldown, LocalDateTime.now());
            if (duration.getSeconds() < cooldownTime) {
                return true;
            }
        }
        return false;
    }

    public String getRemaining(String type, int cd, UUID uuid) {
        if (isOnCooldown(type, cd, uuid)) {
            Duration duration = Duration.between(this.cooldowns.get(uuid).get(type), LocalDateTime.now());
            return getTimeString(cd - (int) duration.getSeconds());
        }
        return "";
    }

    private static String getTimeString(int seconds) {
        String time = "";
        int totalSec = seconds;
        int days = totalSec / 86400;
        totalSec = totalSec - (days * 86400);
        int hour = totalSec / 3600;
        totalSec = totalSec - (hour * 3600);
        int min = totalSec / 60;
        int sec = totalSec - (min * 60);
        if (days > 0) {
            time = "" + days + " dage";
        }
        if (hour > 0) {
            if (days > 0)
                time = time + ", ";
            time = time + hour + " timer";
        }
        if (min > 0) {
            if (hour > 0) {
                time = time + " & ";
            }
            time = time + min + " minutter";
        }
        if (sec > 0 && days == 0) {
            if (min > 0 || hour > 0) {
                time = time + ", ";
            }
            time = time + sec + " sekunder";
        }
        if (time.isEmpty())
            time = "1 sekund";
        if (hour == 1 && min == 0 && sec == 0 && days == 0)
            time = time.substring(0, time.length() - 1);
        else if (min == 1 && sec == 0 && days == 0)
            time = time.substring(0, time.length() - 3);
        return time;
    }
}
