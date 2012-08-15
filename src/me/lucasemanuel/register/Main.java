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
		
		
		
		Config.load(this);
		FileConfiguration config = this.getConfig();
		
		if(config.getString("APIkeys.register") == "" || config.getString("scripts.register") == "" || 
		   config.getString("APIkeys.promote")  == "" || config.getString("scripts.promote")  == "") {
			
			logger.severe("Config not configured! Exiting!");
			return;
		}
		
		
		
		CmdExec executore = new CmdExec(this);
		
		this.getCommand("reg").setExecutor(executore);
		this.getCommand("promote").setExecutor(executore);
		this.getCommand("demote").setExecutor(executore);
		
		
		
		logger.debug("Started");
	}
}

class CmdExec implements CommandExecutor {
	
	private ConsoleLogger logger;
	private Main plugin;
	
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
			logger.debug("Sending url: " + urlString);
			
			String answer = null;
			
			try {
				
				URL url = new URL(urlString);
			    URLConnection connection = url.openConnection();
			 
			    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			    answer = in.readLine();
			    
			    logger.debug("Answer = " + answer);
			    
			    in.close();
			    
			} catch (IOException e) {
				logger.severe(e.getMessage());
				sender.sendMessage(ChatColor.RED + "Oops! N�got gick visst fel! Kontakta admin/mod!");
				return true;
			}
			
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
			
			return true;
		}
		else if(cmd.getName().toLowerCase().equals("promote")) {
			//TODO promote command - high priority
			
			return true;
		}
		else if(cmd.getName().toLowerCase().equals("demote")) {
			//TODO demote command - high priority
			
			return true;
		}
		
		return false;
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
