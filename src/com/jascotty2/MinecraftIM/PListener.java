package com.jascotty2.MinecraftIM;

import java.net.InetSocketAddress;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerListener;

public class PListener extends PlayerListener {

    private final MinecraftIM plugin;

    public PListener(MinecraftIM callback) {
        plugin = callback;
    }

    @Override
    public void onPlayerJoin(PlayerEvent event) {
        if (plugin.mess.notifyOnPlayer) {
            InetSocketAddress ip = event.getPlayer().getAddress();
            String name = event.getPlayer().getName();
            plugin.sendNotify(String.format("%s Joined from %s  (%s)",
                    name, ip.toString(), ip.getHostName().toString()));
        }
    }

    @Override
    public void onPlayerQuit(PlayerEvent event){
        if (plugin.mess.notifyOnPlayer) {
            String name = event.getPlayer().getName();
            plugin.sendNotify(String.format("%s Logged out", name));
        }
    }
    
    @Override
    public void onPlayerChat(PlayerChatEvent event){
        if (plugin.mess.recieveChat) {
            String name = event.getPlayer().getName();
            plugin.sendNotify(String.format("[%s] %s", name, event.getMessage()));
        }
    }
}