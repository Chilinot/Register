/**
 *  Name:    PromoteDemoteThread.java
 *  Created: 13:08:41 - 8 nov 2013
 * 
 *  Author:  Lucas Arnström - LucasEmanuel @ Bukkit forums
 *  Contact: lucasarnstrom(at)gmail(dot)com
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
 *
 *  Filedescription:
 *
 * 
 */

package me.lucasemanuel.register.threads;

import java.util.HashMap;

import me.lucasemanuel.register.Main;
import me.lucasemanuel.register.Utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PromoteDemoteThread extends Thread {
	
	private final Main          plugin;
	private final CommandSender sender;
	private final String        playername;
	private final String        rank;
	private final String        url;
	private final String        api_key;
	private final String        encryption_key;
	
	public PromoteDemoteThread(Main plugin,
			CommandSender sender, 
			String playername, 
			String rank, 
			String url, 
			String api_key, 
			String encryption_key) {
		
		this.plugin         = plugin;
		this.sender         = sender;
		this.playername     = playername;
		this.rank           = rank;
		this.url            = url;
		this.api_key        = api_key;
		this.encryption_key = encryption_key;
		
		start();
	}
	
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
							sender.sendMessage(ChatColor.GREEN + "Rankändring lyckad!");
							
							Player player = Bukkit.getPlayerExact(playername);
							if (player != null && player.isOnline())
								player.chat("/sync");
							else
								sender.sendMessage(ChatColor.GREEN + "Spelaren verkar inte vara online! Säg åt denna att använda /sync nästa gång denne logger in!");
							
							break;
						
						case '1':
							sender.sendMessage(ChatColor.RED + "Rankändringen kunde inte genomföras!");
							break;
						
						case '2':
							sender.sendMessage(ChatColor.RED + playername + " har redan den ranken ingen ändring gjordes.");
							break;
						
						case '3':
							sender.sendMessage(ChatColor.RED + playername + " är en moderator. Du kan inte promota/demota en sådan medlem");
							break;
						
						case '4':
							sender.sendMessage(ChatColor.RED + playername + " finns inte!");
							break;
						
						default:
							sender.sendMessage(ChatColor.RED + "Felaktigt svar från hemsidan!");
							break;
					}
				}
				else {
					sender.sendMessage(ChatColor.RED + "Verkar inte ha fått något svar från hemsidan! Kolla loggen och försök igen.");
				}
			}
		}.runTask(plugin);
	}
}
