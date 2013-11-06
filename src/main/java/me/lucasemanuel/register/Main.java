/**
 *  Name: Main.java
 *  Date: 22:06:35 - 13 aug 2012
 * 
 *  Author: LucasEmanuel @ bukkit forums
 *  
 *  
 *  Copyright 2013 Lucas Arnström
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *  
 *
 *  Filedescription:
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
	
	private ConsoleLogger	logger;
	
	public void onEnable() {
		
		ConsoleLogger.init(this);
		
		logger = new ConsoleLogger("Main");
		
		// Configuration
		
		Config.load(this);
		FileConfiguration config = this.getConfig();
		
		if (config.getString("APIkeys.register") == "" 
				|| config.getString("scripts.register")  == "" 
				|| config.getString("APIkeys.promote")   == "" 
				|| config.getString("scripts.promote")   == "" 
				|| config.getString("APIkeys.resetpass") == "" 
				|| config.getString("scripts.resetpass") == "" 
				|| config.getString("commandRegionName") == "") {
			
			logger.severe("Config not configured! Exiting!");
			return;
		}
		
		// Commands
		
		Commands executor = new Commands(this);
		
		this.getCommand("reg").setExecutor(executor);
		this.getCommand("mpromote").setExecutor(executor);
		this.getCommand("mdemote").setExecutor(executor);
		
		// Misc
		Utils.init(this);
		
		// Finished
		
		logger.debug("Spelplaneten Register Started");
	}
}