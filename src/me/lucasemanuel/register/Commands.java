/**
 *  Name: Commands.java
 *  Date: 16:28:41 - 16 aug 2012
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
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {
	
	private ConsoleLogger logger;
	private Main plugin;
	
	@SuppressWarnings("serial")
	private final HashMap<String, Integer> idlist = new HashMap<String, Integer>() {{
		put("larling", 38);
		put("medlem", 39);
		put("pro", 40);
	}};
	
	public Commands(Main instance) {
		this.plugin = instance;
		this.logger = new ConsoleLogger(instance, "CommandExecutor");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		//TODO
		/***********
		 * Kolla så att spelaren är i specifierad WorldGuard region
		 * annars ska den skriva ut att man måste gå till spawn och följa
		 * signsen eller något.
		 */
		if(cmd.getName().toLowerCase().equals("reg")) {
			
			if(args[0] == "" && args[1] == "") {
				sender.sendMessage(ChatColor.RED + "Du måste skriva både email och lösenord!");
				return false;
			}
			
			if(!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "Du måste vara en spelare för att kunna använda detta kommando!");
				return true;
			}
			
			if(!isInRegion(((Player)sender).getLocation())) {
				sender.sendMessage(ChatColor.RED + "Du måste befinna dig vid regelskyltarna för att kunna använda detta kommando!");
				return true;
			}
			
			String email    = args[0];
			String password = args[1];
			String name     = ((Player)sender).getName();
			
			if(checkComplexity(password) == false) {
				sender.sendMessage(ChatColor.RED + "Ditt lösenord måste vara minst 5 tecken långt!");
				return true;
			}
			
			logger.debug("Registering user: " + name);
			
			String urlString = this.plugin.getConfig().getString("scripts.register") + "?key=" + this.plugin.getConfig().getString("APIkeys.register") + "&username=" + name + "&email=" + email + "&pass=" + password;
			
			String answer = sendGETdata(urlString);
			
			if(answer != null) {
				switch(answer) {
					
					case "0":
						sender.sendMessage(ChatColor.GREEN + "Grattis " + name + "! Du har registrerats på forumet med ranken " + ChatColor.LIGHT_PURPLE + "Lärling");
						((Player)sender).chat("/sync");
						break;
						
					case "1":
						sender.sendMessage(ChatColor.RED + "E-post redan registrerat!");
						break;
						
					case "2":
						sender.sendMessage(ChatColor.RED + "Användarnamnet finns redan!");
						break;
						
					default:
						sender.sendMessage(ChatColor.RED + "Något verkar ha gått snett! Kontakta admin/mod!");
				}
			}
			else {
				sender.sendMessage(ChatColor.RED + "Oops! Något gick visst fel! Kontakta admin/mod.");
			}
			
			return true;
		}
		else if(cmd.getName().toLowerCase().equals("mpromote")) {
			return promoteDemote(sender, args);
		}
		else if(cmd.getName().toLowerCase().equals("mdemote")) {
			return promoteDemote(sender, args);
		}
		
		return false;
	}
	
	private boolean promoteDemote(CommandSender sender, String[] args) {
		
		String playername = args[0];
		
		if(args.length != 2) {
			sender.sendMessage(ChatColor.RED + "Felaktig användning!");
			return false;
		}
		
		if(!this.idlist.containsKey(args[1])) {
			sender.sendMessage(ChatColor.RED + args[1] + " ranken existerar inte!");
			return true;
		}
		
		int rank = this.idlist.get(args[1]);
		
		String urlString = this.plugin.getConfig().getString("scripts.promote") + "?key=" + this.plugin.getConfig().getString("APIkeys.promote") + "&username=" + playername + "&rank=" + rank;
		String answer = sendGETdata(urlString);
		
		if(answer != null) {
			switch(answer) {
				
				case "0":
					sender.sendMessage(ChatColor.GREEN + playername + " har nu rank " + args[1]);
					break;
				
				case "1":
					sender.sendMessage(ChatColor.RED + "Rankändringen kunde inte genomföras!");
					break;
					
				case "2":
					sender.sendMessage(ChatColor.RED + playername + " är en moderator eller högre. Du kan inte " + args[1] + "a en sådan medlem");
					break;
					
				case "3":
					sender.sendMessage(ChatColor.RED + playername + " finns inte!");
					break;
					
				default:
					sender.sendMessage(ChatColor.RED + "Ooops något verkar ha gått snett! Skyll på Lucas!");
			}
		}
		else {
			sender.sendMessage(ChatColor.RED + "Verkar inte ha fått något svar från hemsidan! Kolla loggen och försök igen.");
		}
		
		return true;
	}
	
	private String sendGETdata(String urlString) {
		
		String answer = null;
		
		try {
			
			logger.debug("Sending url: " + urlString);
			
			URL url = new URL(urlString);
		    URLConnection connection = url.openConnection();
		 
		    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		    answer = in.readLine();
		    
		    logger.debug("Answer: " + answer);
		    
		    in.close();
		    
		} catch (IOException e) {
			logger.severe(e.getMessage());
			return null;
		}
		
		return answer;
	}
	
	private boolean checkComplexity(String password) {
		
		/* 
		 * Här ändras sättet den kontrollerar lösenord på, 
		 * just nu kollar den endast om lösenordet är mindre än 5 tecken eller inte.
		 */
		
		if(password.length() < 5) return false;
		
		return true;
	}
	
	private boolean isInRegion(Location playerlocation) {
		
		FileConfiguration config = this.plugin.getConfig();
		
		double x = playerlocation.getX();
		double y = playerlocation.getY();
		double z = playerlocation.getZ();
		
		double minX = config.getDouble("regregion.minX");
		double minY = config.getDouble("regregion.minY");
		double minZ = config.getDouble("regregion.minZ");
		
		double maxX = config.getDouble("regregion.maxX");
		double maxY = config.getDouble("regregion.maxY");
		double maxZ = config.getDouble("regregion.maxZ");
		
		if( (x <= maxX && x >= minX) && (y <= maxY && y <= minY) && (z <= maxZ && z >= minZ) ) {
			return true;
		}
		else {
			return false;
		}
	}
}
