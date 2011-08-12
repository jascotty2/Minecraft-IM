/**
 * Programmer: Jacob Scott
 * Program Name: MinecraftIM
 * Description:
 * Date: Mar 24, 2011
 */
package com.jascotty2.minecraftim;


import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author jacob
 */
public class MinecraftIM extends JavaPlugin {

    protected final static Logger logger = Logger.getLogger("Minecraft");
    public static final String name = "MinecraftIM";
    public Messenger mess = new Messenger(this);
    public PListener playerListener = new PListener(this);
    ArrayList <ChatMessageHandler> messages = new ArrayList <ChatMessageHandler>();

    public void onDisable() {
        mess.disconnect();
        Log("Disabled");
    }

    public void onEnable() {
        Log("Starting Version " + this.getDescription().getVersion());
        
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLAYER_KICK, playerListener, Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Priority.Monitor, this);
        startIM();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
            String commandLabel, String[] args) {
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("reload") && sender.isOp()) {
                if (startIM()) {
                    sender.sendMessage("Successfully reloaded");
                } else {
                    sender.sendMessage("An error occurred while reloading");
                }
            }
        }
        return true;
    }

    boolean startIM() {
        mess.disconnect();
        if(mess.load()){
            Log("Config Loaded");
        }else{
            Log("Config Load Error");
            return false;
        }
        if (mess.connect()) {
            Log("Connecting to IM account");
            Log("sending connect confirm to " + mess.sendToUsername);
            mess.sendNotify("MinecraftIM enabled");
            
            return true;
        } else {
            Log(Level.SEVERE, "Error connecting to IM protocol");
            return false;
        }
    }

    public void sendNotify(String message){
        mess.sendNotify(message);
    }

    public void registerMessageHandler(ChatMessageHandler toAdd){
        if(!messages.contains(toAdd)){
            messages.add(toAdd);
        }
    }
    public void removeMessageHandler(ChatMessageHandler toRem){
        if(!messages.contains(toRem)){
            messages.remove(toRem);
        }
    }

    public boolean messageRecieved(String fromUser, String message){
        for(ChatMessageHandler c : messages){
            if(c.messageHandled(fromUser, message)){
                return true;
            }
        }
        return false;
    }

    public boolean messageRecieved(String message){
        for(ChatMessageHandler c : messages){
            if(c.messageHandled(message)){
                return true;
            }
        }
        return false;
    }

    public static void Log(String txt) {
        logger.log(Level.INFO, String.format("[%s] %s", name, txt));
    }

    public static void Log(String txt, Object params) {
        logger.log(Level.INFO, String.format("[%s] %s", name, txt == null ? "" : txt), params);
    }

    public static void Log(Level loglevel, String txt) {
        logger.log(loglevel, String.format("[%s] %s", name, txt == null ? "" : txt));
    }

    public static void Log(Level loglevel, String txt, Object params) {
        logger.log(loglevel, String.format("[%s] %s", name, txt == null ? "" : txt), params);
    }

    public static void Log(Level loglevel, String txt, Exception params) {
        if (txt == null) {
            Log(loglevel, params);
        } else {
            logger.log(loglevel, String.format("[%s] %s", name, txt == null ? "" : txt), (Exception) params);
        }
    }

    public static void Log(Level loglevel, String txt, Object[] params) {
        logger.log(loglevel, String.format("[%s] %s", name, txt == null ? "" : txt), params);
    }

    public static void Log(Level loglevel, Exception err) {
        logger.log(loglevel, String.format("[%s] %s", name, err == null ? "? unknown exception ?" : err.getMessage()), err);
    }
} // end class MinecraftIM

