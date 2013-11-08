/**
 *  Name: Commands.java
 *  Date: 16:28:41 - 16 aug 2012
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

import java.util.HashMap;

import me.lucasemanuel.register.threads.PromoteDemoteThread;
import me.lucasemanuel.register.threads.RegisterThread;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {
	
	private ConsoleLogger logger;
	private Main          plugin;
	
	@SuppressWarnings("serial")
	private final HashMap<String, String> idlist = new HashMap<String, String>() {{
			put("bannad",  "7");
			put("medlem",  "49");
			put("vipa",    "43");
			put("vipb",    "44");
			put("vipc",    "45");
			put("helper",  "46");
			put("byggare", "47");
	}};
	
	public Commands(Main instance) {
		this.plugin = instance;
		this.logger = new ConsoleLogger("CommandExecutor");
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		String command = cmd.getName().toLowerCase();
		
		switch(command) {
			case "reg":
				return register(sender, args);
			case "mpromote":
				return promoteDemote(sender, args);
			case "mdemote":
				return promoteDemote(sender, args);
		}
		
		return false;
	}
	
	private boolean register(CommandSender sender, String[] args) {
		
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Du måste vara en spelare för att kunna använda detta kommando!");
			return true;
		}
		
		Player player = (Player) sender;
		
		if (!Utils.isInRegion(player.getLocation())) {
			sender.sendMessage(ChatColor.RED + "Du måste befinna dig vid regelskyltarna för att kunna använda detta kommando!");
			return true;
		}
		if (args.length != 2) {
			player.sendMessage(ChatColor.RED + "Du måste skriva både email och lösenord!");
			return false;
		}
		
		String name     = player.getName();
		String email    = args[0];
		String password = args[1];
		
		if(!Utils.emailValidator(email)) {
			player.sendMessage(ChatColor.RED + "Eposten " + ChatColor.GREEN + email + ChatColor.RED + " är inte giltig, var god kolla så du skrivit rätt." + ChatColor.BLUE + " Exempel: dittnamn@gmail.com");
			return true;
		}
		if(!Utils.passwordValidator(password)) {
			player.sendMessage(ChatColor.RED + "Lösenordet " + ChatColor.BLUE + password + ChatColor.RED + " måste ha minst en siffra och vara minst 5-20 tecken lång!");
			return true;
		}
		
		logger.debug("Registering user: " + name);
		
		String url            = this.plugin.getConfig().getString("scripts.register");
		String encryption_key = this.plugin.getConfig().getString("encryption.key");
		String api_key        = this.plugin.getConfig().getString("APIkeys.register");
		
		new RegisterThread(plugin, player.getName(), email, password, url, encryption_key, api_key);
		
		return true;
	}

	private boolean promoteDemote(final CommandSender sender, final String[] args) {
		
		if (args.length != 2) {
			sender.sendMessage(ChatColor.RED + "Felaktig användning!");
			return false;
		}
		
		if (!this.idlist.containsKey(args[1])) {
			sender.sendMessage(ChatColor.RED + args[1] + " ranken existerar inte!");
			return true;
		}
		
		final String playername     = args[0];
		final String rank           = this.idlist.get(args[1]);
		
		final String url            = this.plugin.getConfig().getString("scripts.promote");
		final String api_key        = this.plugin.getConfig().getString("APIkeys.promote");
		final String encryption_key = this.plugin.getConfig().getString("encryption.key");
		
		new PromoteDemoteThread(plugin, sender, playername, rank, url, api_key, encryption_key);
		
		return true;
	}
}
