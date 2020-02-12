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
        ProxyServer.getInstance().getLogger().info("type: " + type + " - uuid: " + uuid.toString() + " - time: " + time.toString());
        this.cooldowns.putIfAbsent(uuid, new HashMap<>());
        this.cooldowns.get(uuid).put(type, time);
    }

    public boolean isOnCooldown(String type, int cooldownTime, UUID uuid) {
        if (!this.cooldowns.containsKey(uuid) || !this.cooldowns.get(uuid).containsKey(type)) {
            return false;
        }
        LocalDateTime cooldown = this.cooldowns.get(uuid).get(type);
        ProxyServer.getInstance().getLogger().info("type: " + type + " - uuid: " + uuid.toString() + " - cooldown: " + cooldown.toString());
        if (cooldown != null) {
            Duration duration = Duration.between(cooldown, LocalDateTime.now());
            if (duration.getSeconds() < cooldownTime) {
                return true;
            }
        }
        return false;
    }
}
