/**
 *  Name:    Utils.java
 *  Created: 13:21:14 - 6 nov 2013
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

package me.lucasemanuel.register;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class Utils {
	
	private static Main plugin;
	private static ConsoleLogger logger;
	
	public static void init(Main instance) {
		Utils.plugin = instance;
		Utils.logger = new ConsoleLogger("Utils");
	}
	
	/**
	 * Send data to a webserver using _POST.
	 * 
	 * @param urlString - Url to the server.
	 * @param data - A map with the data to send.
	 * @return - Answer from the webserver, null if failed.
	 */
	public static String sendWebPost(String urlString, Map<String, String> data) {
		String answer = null;
		
		try {
			URL url = new URL(urlString);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			
			con.setDoInput(true);
			con.setDoOutput(true);
			con.setUseCaches(false);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			
			// Send data
			DataOutputStream dataOut = new DataOutputStream(con.getOutputStream());
			
			StringBuilder param = new StringBuilder();
			
			boolean first = true;
			for(Entry<String, String> e : data.entrySet()) {
				if(first) {
					param.append(e.getKey());
					first = false;
				}
				else {
					param.append('&');
					param.append(e.getKey());
				}
				param.append('=');
				param.append(URLEncoder.encode(e.getValue(), "UTF-8"));
			}
			
			dataOut.writeBytes(param.toString());
			dataOut.flush();
			dataOut.close();
			
			// Receive data
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			answer = in.readLine();
			
			in.close();
		}
		catch (IOException e) {
			logger.severe(e.toString());
			e.printStackTrace();
		}
		
		return answer;
	}
	
	/**
	 * Encrypts string with AES using the given key.
	 * 
	 * @param input          - String to encrypt.
	 * @param encryption_key - Encryption key.
	 * @return               - Encrypted string, null if failed to encrypt.
	 */
	public static String encrypt(String input, String encryption_key) {
		byte[] crypted = null;
		
		try {
			SecretKeySpec skey = new SecretKeySpec(encryption_key.getBytes(), "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, skey);
			crypted = cipher.doFinal(input.getBytes());
		}
		catch (Exception e) {
			logger.severe(e.toString());
			e.printStackTrace();
		}
		
		return crypted == null ? null : new String(Base64.encodeBase64(crypted));
	}
	
	/**
	 * Validiera lösenordet
	 * 
	 * @param password
	 * @return true giltigt lösenord, false ogiltigt lösenord
	 */
	public static boolean passwordValidator(final String password) {
		final Pattern pattern;
		final Matcher matcher;
		
		final String PASSWORD_PATTERN = "((?=.*\\d)(?=.*[a-z]).{5,20})";
		
		pattern = Pattern.compile(PASSWORD_PATTERN);
		matcher = pattern.matcher(password);
		
		return matcher.matches();
	}
	
	/**
	 * Validiera emailadressen som personen skriver in
	 * 
	 * @param email
	 * @return true giltig email, false ogiltig email Taget från: http://www.mkyong.com/regular-expressions/how-to-validate-email-address-with-regular-expression/
	 */
	public static boolean emailValidator(final String email) {
		final Pattern pattern;
		final Matcher matcher;
		
		final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
		
		pattern = Pattern.compile(EMAIL_PATTERN);
		matcher = pattern.matcher(email);
		
		return matcher.matches();
	}
	
	public static boolean isInRegion(Location playerlocation) {
		
		String regionname = plugin.getConfig().getString("commandRegionName");
		
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
	
	private static WorldGuardPlugin getWorldGuard() {
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
		
		// WorldGuard may not be loaded
		if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
			return null;
		}
		return (WorldGuardPlugin) plugin;
	}
	
}
