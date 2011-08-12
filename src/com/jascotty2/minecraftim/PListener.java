package com.jascotty2.minecraftim;

//import java.net.InetSocketAddress;
import java.util.HashMap;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PListener extends PlayerListener {

    private final MinecraftIM plugin;
    private HashMap<Player, Long> kickedPL = new HashMap<Player, Long>();

    public PListener(MinecraftIM callback) {
        plugin = callback;
    }

    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (plugin.mess.notifyOnPlayer) {
            plugin.sendNotify(event.getPlayer().getName() + " Joined");
//            InetSocketAddress ip = event.getPlayer().getAddress();
//            String name = event.getPlayer().getName();
//            plugin.sendNotify(String.format("%s Joined from %s  (%s)",
//                    name, ip.toString(), ip.getHostName().toString()));
        }
    }

    @Override
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

    @Override
    public void onPlayerKick(PlayerKickEvent event) {
        if (plugin.mess.notifyOnPlayer && !event.isCancelled()) {
            kickedPL.put(event.getPlayer(), System.currentTimeMillis());
            plugin.sendNotify(String.format("%s was Kicked: %s", 
                    event.getPlayer().getName(), event.getReason()));
        }
    }
    
    @Override
    public void onPlayerChat(PlayerChatEvent event){
        /*if (plugin.mess.recieveChat()) {
            String name = event.getPlayer().getName();
            plugin.sendNotify(String.format("[%s] %s", name, event.getMessage()));
        }*/
        plugin.mess.publicChat(String.format("[%s] %s", event.getPlayer().getName(), event.getMessage()));
    }
}
