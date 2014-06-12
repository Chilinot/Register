/**
 *  Author:  Lucas Arnström - LucasEmanuel @ Bukkit forums
 *  Contact: lucasarnstrom(at)gmail(dot)com
 *
 *
 *  Copyright 2014 Lucas Arnström
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

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class Config {

    /*
     * -------------------------------- * Settings in config * -------------------------------- */
    public static boolean debug = false;

    public static String APIkeys_register = "";
    public static String APIkeys_promote  = "";

    public static String scripts_register = "";
    public static String scripts_promote  = "";

    public static String encryption_key    = "";
    public static String commandRegionName = "";

    /*
     * -------------------------------- * Do not touch! * -------------------------------- */
    public static void load(Plugin plugin) {
        FileConfiguration conf = plugin.getConfig();
        for(Field field : Config.class.getDeclaredFields()) {
            if(Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers())) {
                String path = field.getName().replaceAll("_", ".");
                try {
                    if(conf.isSet(path)) {
                        field.set(null, conf.get(path));
                    }
                    else {
                        conf.set(path, field.get(null));
                    }
                }
                catch(IllegalAccessException ex) {
                    //
                }
            }
        }
        plugin.saveConfig();
    }
}