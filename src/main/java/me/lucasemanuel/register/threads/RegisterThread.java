/**
 *  Name: RegisterThread.java
 *  Date: 01:27:47 - 18 aug 2012
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

package me.lucasemanuel.register.threads;

import java.util.HashMap;

import me.lucasemanuel.register.Main;
import me.lucasemanuel.register.Utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class RegisterThread extends Thread {
	
	private final Main   plugin;
	
	private final String url;
	private final String encryption_key;
	private final String api_key;
	
	private final String playername;
	private final String email;
	private final String password;
	
	public RegisterThread(
			Main p_instance, 
			final String playername, 
			final String email, 
			final String password, 
			final String url, 
			final String encryption_key, 
			final String api_key) {
	
		this.url            = url;
		this.encryption_key = encryption_key;
		this.api_key        = api_key;
		
		this.playername     = playername;
		this.email          = email;
		this.password       = password;
		
		this.plugin         = p_instance;
		
		start();
	}
	
	public void run() {
		
		@SuppressWarnings("serial")
		HashMap<String, String> data = new HashMap<String, String>() {{
			put("key",      Utils.encrypt(api_key,    encryption_key));
			put("username", Utils.encrypt(playername, encryption_key));
			put("email",    Utils.encrypt(email,      encryption_key));
			put("pass",     Utils.encrypt(password,   encryption_key));
		}};
		
		final String answer = Utils.sendWebPost(url, data);
		
		if (answer != null) {
			
			// Schedules a new task that runs synchronously to the server.
			new BukkitRunnable() {
				@Override
				public void run() {
					
					Player player = Bukkit.getPlayerExact(playername);
					
					if(player == null || !player.isOnline()) return;
					
					// A small hack to fix issues when the plugin doesn't get a clean number as answer.
					switch (answer.charAt(answer.length() -1)) {
						case '0':
							player.sendMessage(ChatColor.GREEN + "Grattis " + player.getName() + "! Du har registrerats som medlem på Spelplanetens forum.");
							player.sendMessage(ChatColor.GREEN + "Du kan nu använda vårt forum genom att gå till " + ChatColor.AQUA + "www.spelplaneten.net/forum " + ChatColor.GREEN + "och klicka på Logga In.");
							player.sendMessage(ChatColor.GREEN + "Logga sedan in med följande uppgifter:");
							player.sendMessage(ChatColor.GREEN + "Användarnamn: " + ChatColor.DARK_AQUA + player.getName() + ChatColor.GREEN + " eller " + ChatColor.DARK_AQUA + email);
							player.sendMessage(ChatColor.GREEN + "Lösenord: " + ChatColor.DARK_AQUA + password);
							player.chat("/sync");
							break;
						
						case '1':
							player.sendMessage(ChatColor.RED + "E-post redan registrerat!");
							break;
						
						case '2':
							player.sendMessage(ChatColor.RED + "Användarnamnet finns redan!");
							break;
						
						default:
							player.sendMessage(ChatColor.RED + "Något verkar ha gått snett! Kontakta admin/mod!");
							System.out.println("SEVERE! Register failed to register user! Answer == Default! Answer=" + answer);
							break;
					}
				}
			}.runTask(plugin);
		}
		else {
			System.out.println("SEVERE! Register failed to register user! Answer == null! Playername=" + playername);
		}
	}
}
