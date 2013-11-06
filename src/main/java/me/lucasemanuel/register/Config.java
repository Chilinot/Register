package me.lucasemanuel.register;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public class Config {
	
	/*
	 * -------------------------------- * Settings in config * -------------------------------- */
	public static boolean debug            = false;
	
	public static String APIkeys_register  = "";
	public static String APIkeys_promote   = "";
	
	public static String scripts_register  = "";
	public static String scripts_promote   = "";
	
	public static String encryption_key	   = "";
	public static String commandRegionName = "";
	
	/*
	 * -------------------------------- * Do not touch! * -------------------------------- */
	public static void load(Plugin plugin) {
		FileConfiguration conf = plugin.getConfig();
		for (Field field : Config.class.getDeclaredFields()) {
			if (Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers())) {
				String path = field.getName().replaceAll("_", ".");
				try {
					if (conf.isSet(path)) {
						field.set(null, conf.get(path));
					}
					else {
						conf.set(path, field.get(null));
					}
				}
				catch (IllegalAccessException ex) {
					//
				}
			}
		}
		plugin.saveConfig();
	}
}