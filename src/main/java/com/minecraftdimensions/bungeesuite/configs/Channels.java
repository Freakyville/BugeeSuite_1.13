package com.minecraftdimensions.bungeesuite.configs;

import com.minecraftdimensions.bungeesuite.configlibrary.Config;

import java.io.File;

public class Channels {
	 private static String configpath = File.separator+"plugins"+File.separator+"BungeeSuite"+File.separator+"channels.yml";
	 public static Config channelsConfig = new Config(configpath);
	 
	 public static void reload(){
		 channelsConfig = new Config(configpath);
	 }
}
