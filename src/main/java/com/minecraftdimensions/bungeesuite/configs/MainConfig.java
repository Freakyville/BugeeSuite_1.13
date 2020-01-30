package com.minecraftdimensions.bungeesuite.configs;

import com.minecraftdimensions.bungeesuite.configlibrary.Config;

import java.io.File;

public class MainConfig {
    public static String configpath = File.separator + "plugins" + File.separator + "BungeeSuite" + File.separator + "config.yml";
    public static Config config = new Config( configpath );

    /* MySQL Information */
    public static String host = config.getString( "Database.Host", "localhost" );
    public static String database = config.getString( "Database.Database", "minecraft" );
    public static String port = config.getString( "Database.Port", "3306" );
    public static String username = config.getString( "Database.Username", "username" );
    public static String password = config.getString( "Database.Password", "password" );
    public static int threads = config.getInt( "Database.Threads", 5 );
    public static boolean motd = config.getBoolean( "MOTD.Enabled", true );
    public static boolean newPlayerBroadcast = config.getBoolean( "NewPlayerBroadcast", true );
    public static boolean broadcastProxyConnectionMessages = config.getBoolean( "BroadcastProxyConnectionMessages", true );
    public static int playerDisconnectDelay = config.getInt( "PlayerDisconnectDelay", 10 );
    /* Redis Information */
    public static String redis_host = config.getString( "Redis.Host", "127.0.0.1" );
    public static int redis_port = config.getInt( "Redis.Port", 0000 );
    public static String redis_password = config.getString( "Redis.Password", "password" );
    public static int redis_timeout = config.getInt( "Redis.Timeout", 5000 );

    public static void reloadConfig() {
        config = null;
        config = new Config( configpath );
        motd = config.getBoolean( "MOTD.Enabled", true );
        newPlayerBroadcast = config.getBoolean( "NewPlayerBroadcast", true );
        broadcastProxyConnectionMessages = config.getBoolean( "BroadcastProxyConnectionMessages", true );
        playerDisconnectDelay = config.getInt( "PlayerDisconnectDelay", 10 );
    }
}
