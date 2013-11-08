/**
 *  Name: Commands.java
 *  Date: 16:28:41 - 16 aug 2012
 * 
 *  Author: LucasEmanuel @ bukkit forums
 *  
 *  
 *  Copyright 2013 Lucas Arnstr�m
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

import me.lucasemanuel.register.threads.RegisterThread;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

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
			sender.sendMessage(ChatColor.RED + "Du m�ste vara en spelare f�r att kunna anv�nda detta kommando!");
			return true;
		}
		
		Player player = (Player) sender;
		
		if (!Utils.isInRegion(player.getLocation())) {
			sender.sendMessage(ChatColor.RED + "Du m�ste befinna dig vid regelskyltarna f�r att kunna anv�nda detta kommando!");
			return true;
		}
		if (args.length != 2) {
			player.sendMessage(ChatColor.RED + "Du m�ste skriva b�de email och l�senord!");
			return false;
		}
		
		String name     = player.getName();
		String email    = args[0];
		String password = args[1];
		
		if(!Utils.emailValidator(email)) {
			player.sendMessage(ChatColor.RED + "Eposten " + ChatColor.GREEN + email + ChatColor.RED + " �r inte giltig, var god kolla s� du skrivit r�tt." + ChatColor.BLUE + " Exempel: dittnamn@gmail.com");
			return true;
		}
		if(!Utils.passwordValidator(password)) {
			player.sendMessage(ChatColor.RED + "L�senordet " + ChatColor.BLUE + password + ChatColor.RED + " m�ste ha minst en siffra och vara minst 5-20 tecken l�ng!");
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
			sender.sendMessage(ChatColor.RED + "Felaktig anv�ndning!");
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
		
		new BukkitRunnable() {
			@Override
			public void run() {
				
				// Runs a-sync to the server.
				@SuppressWarnings("serial")
				final HashMap<String, String> data = new HashMap<String, String>() {{
					put("key" ,     Utils.encrypt(api_key,    encryption_key));
					put("username", Utils.encrypt(playername, encryption_key));
					put("rank",     Utils.encrypt(rank,       encryption_key));
				}};
				
				final String answer = Utils.sendWebPost(url, data);
				
				// Runs sync to the server.
				new BukkitRunnable() {
					@Override
					public void run() {
						if (answer != null) {
							switch (answer.charAt(answer.length() - 1)) {
							
								case '0':
									sender.sendMessage(ChatColor.GREEN + "Rank�ndring lyckad!");
									
									Player player = Bukkit.getPlayerExact(playername);
									if (player != null && player.isOnline())
										player.chat("/sync");
									else
										sender.sendMessage(ChatColor.GREEN + "Spelaren verkar inte vara online! S�g �t denna att anv�nda /sync n�sta g�ng denne logger in!");
									
									break;
								
								case '1':
									sender.sendMessage(ChatColor.RED + "Rank�ndringen kunde inte genomf�ras!");
									break;
								
								case '2':
									sender.sendMessage(ChatColor.RED + playername + " �r redan " + args[1] + " ingen �ndring gjordes.");
									break;
								
								case '3':
									sender.sendMessage(ChatColor.RED + playername + " �r en moderator. Du kan inte promota/demota en s�dan medlem");
									break;
								
								case '4':
									sender.sendMessage(ChatColor.RED + playername + " finns inte!");
									break;
								
								default:
									sender.sendMessage(ChatColor.RED + "Felaktigt svar fr�n hemsidan!");
									break;
							}
						}
						else {
							sender.sendMessage(ChatColor.RED + "Verkar inte ha f�tt n�got svar fr�n hemsidan! Kolla loggen och f�rs�k igen.");
						}
					}
				}.runTask(plugin);
			}
		}.runTaskAsynchronously(plugin);
		
		return true;
	}
}
