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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

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
		
		if(cmd.getName().toLowerCase().equals("reg")) {
			
			if(!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "Du måste vara en spelare för att kunna använda detta kommando!");
				return true;
			}
			
			Player player = (Player) sender;
			
			if(!isInRegion(player.getLocation())) {
				sender.sendMessage(ChatColor.RED + "Du måste befinna dig vid regelskyltarna för att kunna använda detta kommando!");
				return true;
			}
			
			if(args.length != 2) {
				player.sendMessage(ChatColor.RED + "Du måste skriva både email och lösenord!");
				return false;
			}
			
			String email    = args[0];
			String password = args[1];
			String name     = player.getName();
			
			if(checkComplexity(password) == false) {
				player.sendMessage(ChatColor.RED + "Ditt lösenord måste vara minst 5 tecken långt!");
				return true;
			}
			
			logger.debug("Registering user: " + name);
			
			String urlString = this.plugin.getConfig().getString("scripts.register") + "?key=" + this.plugin.getConfig().getString("APIkeys.register") + "&username=" + name + "&email=" + email + "&pass=" + password;
			
			new RegisterThread(player, urlString);
			
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
		
		logger.debug("Answer from sendGETdata: '" + answer + "'");
		
		if(answer != null) {
			switch(answer) {
				
				case "0":
					sender.sendMessage(ChatColor.GREEN + "Rankändring lyckad!");
					
					Player player = Bukkit.getPlayer(playername); 
					if(player != null) player.chat("/sync"); 
					else sender.sendMessage(ChatColor.GREEN + "Spelaren verkar inte vara online! Säg åt denna att använda /sync nästa gång denne logger in!");
					
					break;
					
				case "1":
					sender.sendMessage(ChatColor.RED + "Rankändringen kunde inte genomföras!");
					break;
					
				case "2":
					sender.sendMessage(ChatColor.RED + playername + " är redan " + args[1] + " ingen ändring gjordes.");
					break;
					
				case "3":
					sender.sendMessage(ChatColor.RED + playername + " är en moderator. Du kan inte promota/demota en sådan medlem");
					break;
					
				case "4":
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
		
		String regionname = this.plugin.getConfig().getString("commandRegionName");
		
		if (regionname == null) {
            return true;
        }
        ApplicableRegionSet set = getWGSet(playerlocation);
        if (set == null) {
            return false;
        }
        for (ProtectedRegion r : set) {
            if (r.getId().equalsIgnoreCase(regionname)) {
                return true;
            }
        }
        return false;
	}
	
	private static ApplicableRegionSet getWGSet(Location loc) {
        WorldGuardPlugin wg = getWorldGuard();
        if (wg == null) {
            return null;
        }
        RegionManager rm = wg.getRegionManager(loc.getWorld());
        if (rm == null) {
            return null;
        }
        return rm.getApplicableRegions(com.sk89q.worldguard.bukkit.BukkitUtil.toVector(loc));
    }
 
    public static WorldGuardPlugin getWorldGuard() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
 
        // WorldGuard may not be loaded
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            return null;
        }
        return (WorldGuardPlugin) plugin;
    }
}
