/**
 *  Name: RegisterThread.java
 *  Date: 01:27:47 - 18 aug 2012
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class RegisterThread extends Thread {
	
	private final Main 		plugin;
	
	private final String	urlString;
	private final Player	player;
	private final String	email;
	private final String	password;
	
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
			
			new BukkitRunnable() {
				@Override
				public void run() {
					switch (foo) {
						case "0":
							player.sendMessage(ChatColor.GREEN + "Grattis " + player.getName() + "! Du har registrerats som medlem p� Spelplanetens forum.");
							player.sendMessage(ChatColor.GREEN + "Du kan nu anv�nda v�rt forum genom att g� till " + ChatColor.AQUA + "www.spelplaneten.net/forum " + ChatColor.GREEN + "och klicka p� Logga In.");
							player.sendMessage(ChatColor.GREEN + "Logga sedan in med f�ljande uppgifter:");
							player.sendMessage(ChatColor.GREEN + "Anv�ndarnamn: " + ChatColor.DARK_AQUA + player.getName() + ChatColor.GREEN + " eller " + ChatColor.DARK_AQUA + email);
							player.sendMessage(ChatColor.GREEN + "L�senord: " + ChatColor.DARK_AQUA + password);
							player.chat("/sync");
							break;
						
						case "1":
							player.sendMessage(ChatColor.RED + "E-post redan registrerat!");
							break;
						
						case "2":
							player.sendMessage(ChatColor.RED + "Anv�ndarnamnet finns redan!");
							break;
						
						default:
							player.sendMessage(ChatColor.RED + "N�got verkar ha g�tt snett! Kontakta admin/mod!");
							break;
					}
				}
			}.runTask(plugin);
		}
		else {
			player.sendMessage(ChatColor.RED + "Oops! N�got gick visst fel! Kontakta admin/mod.");
		}
	}
}
