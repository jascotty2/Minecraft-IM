/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: (TODO)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jascotty2.minecraftim;

//import java.net.InetSocketAddress;
import java.util.HashMap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PListener implements Listener {

    private final MinecraftIM plugin;
    private HashMap<Player, Long> kickedPL = new HashMap<Player, Long>();

    public PListener(MinecraftIM callback) {
        plugin = callback;
    }

	@EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (plugin.mess.notifyOnPlayer) {
            plugin.sendNotify(event.getPlayer().getName() + " Joined");
//            InetSocketAddress ip = event.getPlayer().getAddress();
//            String name = event.getPlayer().getName();
//            plugin.sendNotify(String.format("%s Joined from %s  (%s)",
//                    name, ip.toString(), ip.getHostName().toString()));
        }
    }

	@EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event){
        if (plugin.mess.notifyOnPlayer) {
            if(
            kickedPL.containsKey(event.getPlayer())){
                if(System.currentTimeMillis() -  kickedPL.get(event.getPlayer()) < 500) return;
            }
            plugin.sendNotify(String.format("%s Logged out",
                    event.getPlayer().getName()));
        }
    }

	@EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent event) {
        if (plugin.mess.notifyOnPlayer && !event.isCancelled()) {
            kickedPL.put(event.getPlayer(), System.currentTimeMillis());
            plugin.sendNotify(String.format("%s was Kicked: %s", 
                    event.getPlayer().getName(), event.getReason()));
        }
    }
    
	@EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChat(PlayerChatEvent event){
        /*if (plugin.mess.recieveChat()) {
            String name = event.getPlayer().getName();
            plugin.sendNotify(String.format("[%s] %s", name, event.getMessage()));
        }*/
        plugin.mess.publicChat(String.format("[%s] %s", event.getPlayer().getName(), event.getMessage()));
    }
}
