package com.minecraftdimensions.bungeesuite.redis;

import com.minecraftdimensions.bungeesuite.managers.LoggingManager;
import com.minecraftdimensions.bungeesuite.managers.PlayerManager;
import com.minecraftdimensions.bungeesuite.managers.TeleportManager;
import com.minecraftdimensions.bungeesuite.managers.WarpsManager;
import com.minecraftdimensions.bungeesuite.objects.Location;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.Server;
import redis.clients.jedis.*;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class RedisManager {
    private JedisPoolConfig poolConfig;
    private JedisPool jedisPool;
    private String host;
    private String password;
    private int port;
    private int timeout;
    private Jedis subjedis;

    private static RedisManager instance;

    private RedisManager() {

    }

    public RedisManager init(String host, String password, int port, int timeout) {
        this.host = host;
        this.password = password;
        this.port = port;
        this.timeout = timeout;
        poolConfig = buildPoolConfig();
        setup();
        listen();
        return instance;
    }

    public static RedisManager getInstance() {
        if (instance == null) {
            instance = new RedisManager();
        }
        return instance;
    }

    private void setup() {
        jedisPool = new JedisPool(poolConfig,
                host,
                port,
                timeout,
                password,
                false);
    }


    public void listen() {
        JedisShardInfo jedisShardInfo = new JedisShardInfo(host, port);
        jedisShardInfo.setPassword(password);
        subjedis = new Jedis(jedisShardInfo);
        CompletableFuture.runAsync(() -> {
            subjedis.subscribe(new JedisPubSub() {
                @Override
                public void onMessage(String channel, String message) {
                    try {
                        String[] args = message.split(";");
                        if (channel.equalsIgnoreCase("WARP_REQUEST")) {
                            ProxyServer.getInstance().getLogger().info("WARP REDIS REQUEST: " + message);
                            if (args[0].equalsIgnoreCase("WarpPlayer")) {
                                WarpsManager.sendPlayerToWarp(args[1], args[2], args[3], Boolean.parseBoolean(args[4]), Boolean.parseBoolean(args[5]));
                            } else if (args[0].equalsIgnoreCase("SetWarp")) {
                                WarpsManager.setWarp(PlayerManager.getPlayer(args[1]), args[2], new Location(args[3], args[4], Double.parseDouble(args[5]), Double.parseDouble(args[6]), Double.parseDouble(args[7]), Long.parseLong(args[8]), Long.parseLong(args[9])), Boolean.parseBoolean(args[10]), Boolean.parseBoolean(args[11]));
                            } else if (args[0].equalsIgnoreCase("DeleteWarp")) {
                                WarpsManager.deleteWarp(PlayerManager.getPlayer(args[1]), args[2]);
                            } else if (args[0].equalsIgnoreCase("GetWarpsList")) {
                                WarpsManager.getWarpsList(args[1], Boolean.parseBoolean(args[2]), Boolean.parseBoolean(args[3]), Boolean.parseBoolean(args[4]), Boolean.parseBoolean(args[5]));
                            } else if (args[0].equalsIgnoreCase("SendVersion")) {
                                LoggingManager.log(message);
                            }
                        } else if (channel.equalsIgnoreCase("TELEPORT_REQUEST")) {
                            ProxyServer.getInstance().getLogger().info("TELEPORT REDIS REQUEST: " + message);
                            if (args[0].equalsIgnoreCase("TpAccept")) {
                                TeleportManager.acceptTeleportRequest(PlayerManager.getPlayer(args[1]));
                            } else if (args[0].equalsIgnoreCase("TeleportToLocation")) {
                                String[] locs = args[2].split("~!~");
                                TeleportManager.teleportPlayerToLocation(PlayerManager.getPlayer(args[1]), new Location(ProxyServer.getInstance().getPlayer(args[1]).getServer().getInfo(), locs[0], Double.parseDouble(locs[1]), Double.parseDouble(locs[2]), Double.parseDouble(locs[3])));
                            } else if (args[0].equalsIgnoreCase("PlayersTeleportBackLocation")) {
                                TeleportManager.setPlayersTeleportBackLocation(PlayerManager.getPlayer(args[1]), new Location(ProxyServer.getInstance().getPlayer(args[1]).getServer().getInfo(), args[2], Double.parseDouble(args[3]), Double.parseDouble(args[4]), Double.parseDouble(args[5])));
                            } else if (args[0].equalsIgnoreCase("PlayersDeathBackLocation")) {
                                TeleportManager.setPlayersDeathBackLocation(PlayerManager.getPlayer(args[1]), new Location(ProxyServer.getInstance().getPlayer(args[1]).getServer().getInfo(), args[2], Double.parseDouble(args[3]), Double.parseDouble(args[4]), Double.parseDouble(args[5])));
                            } else if (args[0].equalsIgnoreCase("TeleportToPlayer")) {
                                TeleportManager.teleportPlayerToPlayer(args[1], args[2], args[2], Boolean.parseBoolean(args[3]), Boolean.parseBoolean(args[4]));
                            } else if (args[0].equalsIgnoreCase("TpaHereRequest")) {
                                TeleportManager.requestPlayerTeleportToYou(args[1], args[2]);
                            } else if (args[0].equalsIgnoreCase("TpaRequest")) {
                                TeleportManager.requestToTeleportToPlayer(args[1], args[2]);
                            } else if (args[0].equalsIgnoreCase("TpDeny")) {
                                TeleportManager.denyTeleportRequest(PlayerManager.getPlayer(args[1]));
                            } else if (args[0].equalsIgnoreCase("TpAll")) {
                                TeleportManager.tpAll(args[1], args[2]);
                            } else if (args[0].equalsIgnoreCase("SendPlayerBack")) {
                                TeleportManager.sendPlayerToLastBack(PlayerManager.getPlayer(args[1]), Boolean.parseBoolean(args[3]), Boolean.parseBoolean(args[4]));
                            } else if (args[0].equalsIgnoreCase("ToggleTeleports")) {
                                TeleportManager.togglePlayersTeleports(PlayerManager.getPlayer(args[1]));
                            }
                        } else {
                            ProxyServer.getInstance().getLogger().info("UNKNOWN REDIS REQUEST: " + message);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }, "WARP_REQUEST", "TELEPORT_REQUEST");
        });
    }

    public void publish(String data, String channel) {
        CompletableFuture.runAsync(() -> {
            ProxyServer.getInstance().getLogger().info("WARP REDIS PUBLISH: " + data + " (" + channel + ")");
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.publish(channel, data);
            }
        });
    }

    private JedisPoolConfig buildPoolConfig() {
        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(128);
        poolConfig.setMaxIdle(128);
        poolConfig.setMinIdle(16);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMinEvictableIdleTimeMillis(Duration.ofSeconds(60).toMillis());
        poolConfig.setTimeBetweenEvictionRunsMillis(Duration.ofSeconds(30).toMillis());
        poolConfig.setNumTestsPerEvictionRun(3);
        poolConfig.setBlockWhenExhausted(true);
        return poolConfig;
    }
}
