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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	
	private ConsoleLogger logger;
	
	public void onEnable() {
		
		logger = new ConsoleLogger(this, "Main");
		
		
		// Configuration
		
		Config.load(this);
		FileConfiguration config = this.getConfig();
		
		if(config.getString("APIkeys.register") == "" || config.getString("scripts.register") == "" || 
		   config.getString("APIkeys.promote")  == "" || config.getString("scripts.promote")  == "") {
			
			logger.severe("Config not configured! Exiting!");
			return;
		}
		
		
		// Commands
		
		CmdExec executore = new CmdExec(this);
		
		this.getCommand("reg").setExecutor(executore);
		this.getCommand("promote").setExecutor(executore);
		this.getCommand("demote").setExecutor(executore);
		
		
		// Finished
		
		logger.debug("Started");
	}
}

class CmdExec implements CommandExecutor {
	
	private ConsoleLogger logger;
	private Main plugin;
	
	@SuppressWarnings("serial")
	private final HashMap<String, Integer> idlist = new HashMap<String, Integer>() {{
		put("larling", 38);
		put("medlem", 39);
		put("pro", 40);
	}};
	
	public CmdExec(Main instance) {
		this.plugin = instance;
		this.logger = new ConsoleLogger(instance, "CommandExecutor");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if(cmd.getName().toLowerCase().equals("reg")) {
			
			if(args.length != 2) {
				sender.sendMessage(ChatColor.RED + "Felaktig anv�ndning!");
				return false;
			}
			
			if(!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.RED + "Du m�ste vara en spelare f�r att kunna anv�nda detta kommando!");
					return true;
			}
			
			String email    = args[0];
			String password = args[1];
			String name     = ((Player)sender).getName();
			
			if(checkComplexity(password) == false) {
				sender.sendMessage(ChatColor.RED + "Ditt l�senord m�ste vara minst 5 tecken l�ngt!");
				return true;
			}
			
			logger.debug("Registering user: " + name);
			
			String urlString = this.plugin.getConfig().getString("scripts.register") + "?key=" + this.plugin.getConfig().getString("APIkeys.register") + "&username=" + name + "&email=" + email + "&pass=" + password;
			
			String answer = sendGETdata(urlString);
			
			if(answer != null) {
				switch(answer) {
					
					case "0":
						sender.sendMessage(ChatColor.GREEN + "Du har registrerats p� forumet!");
						((Player)sender).chat("/sync");
						break;
						
					case "1":
						sender.sendMessage(ChatColor.RED + "E-post redan registrerat!");
						break;
						
					case "2":
						sender.sendMessage(ChatColor.RED + "Anv�ndarnamnet finns redan!");
						break;
						
					default:
						sender.sendMessage(ChatColor.RED + "N�got verkar ha g�tt snett! Kontakta admin/mod!");
				}
			}
			else {
				sender.sendMessage(ChatColor.RED + "Oops! N�got gick visst fel! Kontakta admin/mod.");
			}
			
			return true;
		}
		else if(cmd.getName().toLowerCase().equals("promote")) {
			//TODO promote command - high priority
			
			return promoteDemote(sender, args);
		}
		else if(cmd.getName().toLowerCase().equals("demote")) {
			//TODO demote command - high priority
			
			return promoteDemote(sender, args);
		}
		
		return false;
	}
	
	private boolean promoteDemote(CommandSender sender, String[] args) {
		
		if(args.length != 2) return false;
		
		if(!this.idlist.containsKey(args[1])) {
			sender.sendMessage(ChatColor.RED + "Den ranken existerar inte!");
			return true;
		}
		
		String playername = args[0];
		int rank = this.idlist.get(args[1]);
		
		String urlString = this.plugin.getConfig().getString("scripts.promote") + "?key=" + this.plugin.getConfig().getString("APIkeys.promote") + "&username=" + playername + "&rank=" + rank;
		String answer = sendGETdata(urlString);
		
		if(answer != null) {
			switch(answer) {
				
				case "0":
					sender.sendMessage(ChatColor.GREEN + "Rank�ndring genomf�rd!");
					break;
				
				case "1":
					sender.sendMessage(ChatColor.RED + "Rank�ndringen kunde inte genomf�ras!");
					break;
					
				case "2":
					sender.sendMessage(ChatColor.RED + "Spelarnamnet finns inte!");
					break;
					
				default:
					sender.sendMessage(ChatColor.RED + "Ooops n�got verkar ha g�tt snett! Kolla loggen!");
			}
		}
		else {
			sender.sendMessage(ChatColor.RED + "Verkar inte ha f�tt n�got svar fr�n hemsidan! Kolla loggen och f�rs�k igen.");
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
		
		return null;
	}
	
	private boolean checkComplexity(String password) {
		
		/* 
		 * H�r �ndras s�ttet den kontrollerar l�senord p�, 
		 * just nu kollar den endast om l�senordet �r mindre �n 5 tecken eller inte.
		 */
		
		if(password.length() < 5) return false;
		
		return true;
	}
}
