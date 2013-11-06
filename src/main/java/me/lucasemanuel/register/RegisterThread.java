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

package me.lucasemanuel.register;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class RegisterThread extends Thread {
	
	private final Main   plugin;
	
	private final String urlString;
	private final Player player;
	private final String email;
	private final String password;
	
	public RegisterThread(Main p_instance, final Player player, final String email, final String password, final String urlString) {
		this.urlString = urlString;
		this.player = player;
		this.email = email;
		this.password = password;
		
		this.plugin = p_instance;
		
		start();
	}
	
	public void run() {
		
		String answer = null;
		
		try {
			URL url = new URL(urlString);
			URLConnection connection = url.openConnection();
			
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			answer = in.readLine();
			
			in.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		if (answer != null) {
			
			final String foo = answer;
			
			// Schedules a new task that runs synchronously to the server.
			new BukkitRunnable() {
				@Override
				public void run() {
					switch (foo) {
						case "0":
							player.sendMessage(ChatColor.GREEN + "Grattis " + player.getName() + "! Du har registrerats som medlem på Spelplanetens forum.");
							player.sendMessage(ChatColor.GREEN + "Du kan nu använda vårt forum genom att gå till " + ChatColor.AQUA + "www.spelplaneten.net/forum " + ChatColor.GREEN + "och klicka på Logga In.");
							player.sendMessage(ChatColor.GREEN + "Logga sedan in med följande uppgifter:");
							player.sendMessage(ChatColor.GREEN + "Användarnamn: " + ChatColor.DARK_AQUA + player.getName() + ChatColor.GREEN + " eller " + ChatColor.DARK_AQUA + email);
							player.sendMessage(ChatColor.GREEN + "Lösenord: " + ChatColor.DARK_AQUA + password);
							player.chat("/sync");
							break;
						
						case "1":
							player.sendMessage(ChatColor.RED + "E-post redan registrerat!");
							break;
						
						case "2":
							player.sendMessage(ChatColor.RED + "Användarnamnet finns redan!");
							break;
						
						default:
							player.sendMessage(ChatColor.RED + "Något verkar ha gått snett! Kontakta admin/mod!");
							break;
					}
				}
			}.runTask(plugin);
		}
		else {
			player.sendMessage(ChatColor.RED + "Oops! Något gick visst fel! Kontakta admin/mod.");
		}
	}
}
