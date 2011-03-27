/**
 * Programmer: Jacob Scott
 * Program Name: Messenger
 * Description:
 * Date: Mar 24, 2011
 */
package com.jascotty2.MinecraftIM;

import com.jascotty2.CheckInput;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;

import org.bukkit.util.config.Configuration;

/**
 * @author jacob
 */
public class Messenger {

    // files used by plugin
    public final static String configname = "config.yml";
    public final static File pluginFolder = new File("plugins", MinecraftIM.name);
    public final static File configfile = new File(pluginFolder, configname);
    // plugin this is using 
    MinecraftIM callbackPlugin;
    // settings
    public String dispname = "Console";
    public String sendToUsername = "";
    protected String username = "";
    protected String password = "";
    protected Protocol useProtocol = Protocol.AIM;
    public boolean notifyOnPlayer = true, recieveChatMsgs = false, pingReply = false;
    public long tempChatLen = 180;
    Date lastChat = null;
    public String pingResp = "";
    // messenger handlers
    AIM_Messenger aimMess = null;

    public class RunCommander implements CommandSender {

        boolean op = false;
        String toUser = "";

        public RunCommander(boolean asOp) {
            op = asOp;
        }

        public RunCommander(boolean asOp, String sendTo) {
            op = asOp;
            toUser = sendTo;
        }

        public void sendMessage(String str) {
            if (toUser != null && !toUser.isEmpty()) {
                sendNotify(str, toUser);
            } else {
                sendNotify(str);
            }
        }

        public boolean isOp() {
            return op;
        }

        public Server getServer() {
            return callbackPlugin.getServer();
        }
    }

    public enum Protocol {

        AIM
    }

    public Messenger(MinecraftIM callback) {
        callbackPlugin = callback;
    }

    public final boolean load() {
        if (loadConfig()) {
            if (useProtocol == Protocol.AIM) {
                aimMess = new AIM_Messenger(this);
            }
            return true;
        }
        return false;
    }

    public boolean connect() {
        if (useProtocol == Protocol.AIM) {
            return aimMess.connect(username, password);
        }
        return false;
    }

    //public void sendMessage(String to, String message) {
    public void sendNotify(String message, String to) {
        // remove existing chatcolors & replace with html color tags
        while (message.contains("\u00A7")) {
            int pos = message.indexOf('\u00A7');
            String tag = chatColorToHTML(message.charAt(message.indexOf('\u00A7') + 1));
            if (message.lastIndexOf('\u00A7') == pos) {
                message = String.format("%s<span style='color:%s'>%s</span>", message.substring(0, pos),
                        tag, message.substring(pos + 2));
            } else {
                int pos2 = message.indexOf('\u00A7', pos + 1);
                message = String.format("%s<span style='color:%s'>%s</span>%s", message.substring(0, pos),
                        tag, message.substring(pos + 2, pos2), message.substring(pos2));
            }
        }
        if (useProtocol == Protocol.AIM) {
            aimMess.sendMessage(to, message);
        }
    }

    public void sendNotify(String message) {
        // remove existing chatcolors & replace with html color tags
        while (message.contains("\u00A7")) {
            int pos = message.indexOf('\u00A7');
            String tag = chatColorToHTML(message.charAt(message.indexOf('\u00A7') + 1));
            if (message.lastIndexOf('\u00A7') == pos) {
                message = String.format("%s<span style='color:%s'>%s</span>", message.substring(0, pos),
                        tag, message.substring(pos + 2));
            } else {
                int pos2 = message.indexOf('\u00A7', pos + 1);
                message = String.format("%s<span style='color:%s'>%s</span>%s", message.substring(0, pos),
                        tag, message.substring(pos + 2, pos2), message.substring(pos2));
            }
        }
        if (useProtocol == Protocol.AIM) {
            aimMess.sendMessage(message);
        }
    }

    public String chatColorToHTML(char chatCol) {
        /*
        #       0 is black
        #       1 is dark blue
        #       2 is dark green
        #       3 is dark sky blue
        #       4 is red
        #       5 is magenta
        #       6 is gold or amber
        #       7 is light grey
        #       8 is dark grey
        #       9 is medium blue
        #       a is light green
        #       b is cyan
        #       c is orange-red
        #       d is pink
        #       e is yellow
        #       f is white*/
        switch (chatCol) {
            case '0':
                return "#000000";
            case '1':
                return "#000066";
            case '2':
                return "#006600";
            case '3':
                return "#336699";
            case '4':
                return "#FF0000";
            case '5':
                return "#CC00CC";
            case '6':
                return "#CC9900";
            case '7':
                return "#999999";
            case '8':
                return "#666666";
            case '9':
                return "#0000FF";
            case 'a':
                return "#00CC00";
            case 'b':
                return "#00CCCC";
            case 'c':
                return "#FF3300";
            case 'd':
                return "#FF6666";
            case 'e':
                return "#FFFF00";
            case 'f':
                return "#333333"; // send gray, not white
            //return "span style='color:#FFFFFF'";
        }
        return "#000000'";
    }

    public void messageRecieved(String from, String msg) {
        if (from != null && msg != null && from.length() > 0 && msg.length() > 0) {
            if (!callbackPlugin.messageRecieved(from, msg)) {
                if (from.equalsIgnoreCase(sendToUsername)) {
                    if (!processCommands(msg) && !callbackPlugin.messageRecieved(msg)) {
                        if (msg.charAt(0) != '/') {
                            callbackPlugin.getServer().broadcastMessage(String.format("<%s> %s", dispname, msg));
                            MinecraftIM.Log(String.format("<%s> %s", dispname, msg));
                            lastChat = new Date();
                        } else {
                            //callbackPlugin.getServer().getOnlinePlayers()[0].performCommand(msg);
                            callbackPlugin.getServer().dispatchCommand(new RunCommander(true), msg.substring(1));
                        }
                        if (pingReply) {
                            //aimMess.sendMessage(pingResp);
                            sendNotify(pingResp);
                        }
                    }
                } else {
                    //aimMess.sendMessage(String.format("%s tried to send: %s", from, msg));
                    //aimMess.sendMessage(from, "Unauthorized!");
                    sendNotify(String.format("%s tried to send: %s", from, msg));
                    sendNotify("Unauthorized!", from);
                }
            }
        }
    }

    public boolean processCommands(String msg) {
        if (msg.equalsIgnoreCase("help")) {
            sendNotify("Minecraft Messenger " + callbackPlugin.getDescription().getVersion() + "\n"
                    + "Commands: \n"
                    + ChatColor.RED.toString() + "ping" + ChatColor.BLACK.toString() + "           pong :)");
        } else if (msg.equalsIgnoreCase("ping")) {
            sendNotify("Pong :)");
        } else {
            return false;
        }
        return true;
    }

    public boolean recieveChat(){
        return recieveChatMsgs || (lastChat != null && ((new Date()).getTime() - lastChat.getTime())/1000 < tempChatLen);
    }

    protected boolean loadConfig() {
        try {
            if (!configfile.exists()) {
                pluginFolder.mkdirs();
                try {
                    MinecraftIM.Log(Level.WARNING, configname + " not found. Creating new file.");
                    configfile.createNewFile();
                    InputStream res = Messenger.class.getResourceAsStream("/config.yml");
                    FileWriter tx = new FileWriter(configfile);
                    try {
                        for (int i = 0; (i = res.read()) > 0;) {
                            tx.write(i);
                        }
                    } finally {
                        tx.flush();
                        tx.close();
                        res.close();
                    }
                } catch (IOException ex) {
                    MinecraftIM.Log(Level.SEVERE, "Failed creating new config file ", ex);
                }
            }

            Configuration config = new Configuration(configfile);
            config.load();

            dispname = config.getString("DisplayName", dispname);
            username = config.getString("username", "");
            password = config.getString("password", "");
            sendToUsername = config.getString("sendto", sendToUsername);
            notifyOnPlayer = config.getBoolean("notifyOnPlayer", notifyOnPlayer);
            recieveChatMsgs = config.getBoolean("recieveChat", recieveChatMsgs);
            pingReply = config.getBoolean("pingReply", pingReply);
            pingResp = config.getString("pingResp", "");
            String p = config.getString("protocol");
            if (p != null) {
                if (p.equalsIgnoreCase("aim")) {
                    useProtocol = Protocol.AIM;
                }
            }
            p = config.getString("tempChat");
            if (p != null) {
                try {
                    tempChatLen = CheckInput.GetBigInt_TimeSpanInSec(p, 'm').longValue();
                } catch (Exception ex) {
                    MinecraftIM.Log(Level.WARNING, "tempChat has an illegal value", ex);
                }
            }

            if (sendToUsername.equalsIgnoreCase(username)) {
                MinecraftIM.Log("Username and SendTo cannot be the same");
                sendToUsername = "";
            }
            return true;
        } catch (Exception e) {
            MinecraftIM.Log(Level.SEVERE, "Failed to load config ", e);
        }
        return false;
    }
} // end class Messenger

