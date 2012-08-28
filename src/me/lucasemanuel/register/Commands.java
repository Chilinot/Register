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
				sender.sendMessage(ChatColor.RED + "Du m�ste vara en spelare f�r att kunna anv�nda detta kommando!");
				return true;
			}
			
			Player player = (Player) sender;
			
			if(!isInRegion(player.getLocation())) {
				sender.sendMessage(ChatColor.RED + "Du m�ste befinna dig vid regelskyltarna f�r att kunna anv�nda detta kommando!");
				return true;
			}
			
			if(args.length != 2) {
				player.sendMessage(ChatColor.RED + "Du m�ste skriva b�de email och l�senord!");
				return false;
			}
			
			String email    = args[0];
			String password = args[1];
			String name     = player.getName();
			
			if(checkComplexity(password) == false) {
				player.sendMessage(ChatColor.RED + "Ditt l�senord m�ste vara minst 5 tecken l�ngt!");
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
			sender.sendMessage(ChatColor.RED + "Felaktig anv�ndning!");
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
					sender.sendMessage(ChatColor.GREEN + "Rank�ndring lyckad!");
					
					Player player = Bukkit.getPlayer(playername); 
					if(player != null) player.chat("/sync"); 
					else sender.sendMessage(ChatColor.GREEN + "Spelaren verkar inte vara online! S�g �t denna att anv�nda /sync n�sta g�ng denne logger in!");
					
					break;
					
				case "1":
					sender.sendMessage(ChatColor.RED + "Rank�ndringen kunde inte genomf�ras!");
					break;
					
				case "2":
					sender.sendMessage(ChatColor.RED + playername + " �r redan " + args[1] + " ingen �ndring gjordes.");
					break;
					
				case "3":
					sender.sendMessage(ChatColor.RED + playername + " �r en moderator. Du kan inte promota/demota en s�dan medlem");
					break;
					
				case "4":
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
		 * H�r �ndras s�ttet den kontrollerar l�senord p�, 
		 * just nu kollar den endast om l�senordet �r mindre �n 5 tecken eller inte.
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
