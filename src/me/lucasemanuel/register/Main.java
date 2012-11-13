/**
 *  Name: Main.java
 *  Date: 22:06:35 - 13 aug 2012
 * 
 *  Author: LucasEmanuel @ bukkit forums
 *  
 *  
 *  Description:
 *  
 *  
 *  
 * 
 * 
 */

package me.lucasemanuel.register;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	
	private ConsoleLogger logger;
	
	public void onEnable() {
		
		logger = new ConsoleLogger(this, "Main");
		
		
		// Configuration 
		
		Config.load(this);
		FileConfiguration config = this.getConfig();
		
		if(config.getString("APIkeys.register") == "" || config.getString("scripts.register") == "" || 
		   config.getString("APIkeys.promote")  == "" || config.getString("scripts.promote")  == "" ||
		   config.getString("commandRegionName") == "") {
			
			logger.severe("Config not configured! Exiting!");
			return;
		}
		
		
		// Commands
		
		Commands executor = new Commands(this);
		
		this.getCommand("reg").setExecutor(executor);
		this.getCommand("glömtlösen").setExecutor(executor);
		this.getCommand("mpromote").setExecutor(executor);
		this.getCommand("mdemote").setExecutor(executor);
		
		
		// Finished
		
		logger.debug("Maera Register Started");
	}
}