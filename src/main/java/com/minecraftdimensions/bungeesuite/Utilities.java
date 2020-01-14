package com.minecraftdimensions.bungeesuite;

import com.google.common.net.InetAddresses;
import net.md_5.bungee.api.ChatColor;

public class Utilities {
	
	public static boolean isIPAddress(String ip){
		return InetAddresses.isInetAddress(ip);
	}

	public static String colorize(String input) {
		return ChatColor.translateAlternateColorCodes('&', input);
	}
}
