package com.minecraftdimensions.bungeesuite.managers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class CooldownManager {
    private static CooldownManager ourInstance = new CooldownManager();

    public static CooldownManager getInstance() {
        return ourInstance;
    }

    private CooldownManager() {

    }

    private Set<Cooldown> cooldowns;
    // Map<Type, Map<Permission, Tid>>
    private Map<String, Map<String, Integer>> cooldownIntervals = new HashMap<>();

    public void setCooldown(String type, UUID uuid, LocalDateTime time) {
        this.cooldowns.add(new Cooldown(type, uuid, time));
    }

    public boolean isOnCooldown(String type, int cooldownTime, UUID uuid) {
        Cooldown cooldown = this.cooldowns.stream().filter(f -> f.id.equalsIgnoreCase(type) && f.uuid.equals(uuid)).findFirst().orElse(null);
        if (cooldown != null) {
            Duration duration = Duration.between(cooldown.now, LocalDateTime.now());
            if (duration.getSeconds() < cooldownTime) {
                return true;
            }
        }
        return false;
    }


    private class Cooldown {
        private String id;
        private UUID uuid;
        private LocalDateTime now;

        public Cooldown(String id, UUID uuid, LocalDateTime now) {
            this.id = id;
            this.uuid = uuid;
            this.now = now;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Cooldown cooldown = (Cooldown) o;
            return Objects.equals(id, cooldown.id) &&
                    Objects.equals(uuid, cooldown.uuid);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, uuid);
        }
    }
}
