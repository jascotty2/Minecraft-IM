/**
 * Programmer: Jacob Scott
 * Program Name: Messenger
 * Description:
 * Date: Mar 24, 2011
 */
package com.jascotty2.minecraftim;

import me.jascotty2.io.CheckInput;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

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
    public String dispname = "Console", publicSuffix = " (IM)";
    public String sendToUsername = "";
    protected String username = "";
    protected String password = "";
    protected Protocol useProtocol = Protocol.AIM;
    public boolean notifyOnPlayer = true,
            recieveChatMsgs = false,
            publicChat = false,
            formatColors = true;
    public long tempChatLen = 180;
    // for chat when recieveChatMsgs == false, or publicChat
    HashMap<String, Date> lastChat = new HashMap<String, Date>();
    // for sending a condensed chat block (speed up sending)
    HashMap<String, ArrayList<String>> chatCache = new HashMap<String, ArrayList<String>>();
    int cacheSendWait = 500; // milliseconds
    //private Timer cacheSender = new Timer();
    private SendDelay cacheSender = null;
    // should no longer be needed
    public boolean pingReply = false;
    public String pingResp = "";
    // if message to send, but is offline
    HashMap<String, ArrayList<OfflineMessage>> offlineMessages = new HashMap<String, ArrayList<OfflineMessage>>();
    SimpleDateFormat timestamp = new SimpleDateFormat("MMM dd HH:mm:ss zzz");
    // messenger handlers
    //AIM_Messenger aimMess = null;
    Abstract_Messenger messenger = null;

    public enum Protocol {

        AIM, GTALK, JABBER, XMPP, MSN //, SKYPE
    }

    public Messenger(MinecraftIM callback) {
        callbackPlugin = callback;
    }

    public final boolean load() {
        if (loadConfig()) {
            if (useProtocol == Protocol.AIM) {
                messenger = new AIM_Messenger(this);
            } else if(useProtocol == Protocol.GTALK){
				messenger = new GTalkMessenger(this);
			} else if(useProtocol == Protocol.JABBER){
				messenger = new JabberMessenger(this);
			}
			//else if (useProtocol == Protocol.SKYPE){ messenger = new Skype_Messenger(this); }
            return true;
        }
        return false;
    }

    public boolean connect() {
        return messenger == null ? false : messenger.connect(username, password);
    }

    public void disconnect() {
        if (messenger != null) {
            messenger.disconnect();
        }
    }

    //public void sendMessage(String to, String message) {
    public void sendNotify(String message, String to) {
        // html special chars
        message = message.replaceAll("\\<", "&lt;").replaceAll("\\>", "&gt;");
        if (formatColors) {
            // remove existing chatcolors & replace with html color tags
            while (message.contains("\u00A7")) {
                int pos = message.indexOf('\u00A7');
                String tag = chatColorToHTML(message.charAt(message.indexOf('\u00A7') + 1));
                if (message.lastIndexOf('\u00A7') == pos) {
                    message = String.format("%s<span style='color:%s'>%s</span>", message.substring(0, pos),
                            tag, message.substring(pos + 2));
                } else {
                    // todo: if there is no colored text (or is just spaces) don't format color
                    int pos2 = message.indexOf('\u00A7', pos + 1);
                    message = String.format("%s<span style='color:%s'>%s</span>%s", message.substring(0, pos),
                            tag, message.substring(pos + 2, pos2), message.substring(pos2));
                }
            }
        } else {
            message = message.replaceAll("\\\u00A7.", "");
        }
        if (chatCache.get(to) == null) {
            chatCache.put(to, new ArrayList<String>());
        }
        chatCache.get(to).add(message);
        if (cacheSender != null) {
            cacheSender.cancel();
        }
        cacheSender = new SendDelay(cacheSendWait);
    }

    public void sendNotify(String message) {
        sendNotify(message, sendToUsername);
    }

    public static String chatColorToHTML(char chatCol) {
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
        switch (Character.toLowerCase(chatCol)) {
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
        return "#000000";
    }

    public void messageRecieved(String from, String msg) {
		msg = msg.trim();
        if (from != null && msg != null && from.length() > 0 && msg.length() > 0) {
            if (!callbackPlugin.messageRecieved(from, msg)) {
                if (from.equalsIgnoreCase(sendToUsername)) {
                    if (!processCommands(msg) && !callbackPlugin.messageRecieved(msg)) {
                        if (msg.charAt(0) != '/') {
                            callbackPlugin.getServer().broadcastMessage(String.format("<%s> %s", dispname, msg));
                            MinecraftIM.Log(String.format("<%s> %s", dispname, msg));
                            lastChat.put(from, new Date());
                        } else {
                            //callbackPlugin.getServer().dispatchCommand(new RunCommander(true, sendToUsername), msg.substring(1));
                            callbackPlugin.getServer().dispatchCommand(
                                    new MessengerRunCommander(this, true), msg.substring(1));
                        }
                        if (pingReply) {
                            sendNotify(pingResp);
                        }
                    }
                } else if (publicChat) {
                    callbackPlugin.getServer().broadcastMessage(String.format("<%s%s> %s", from, publicSuffix, msg));
                    MinecraftIM.Log(String.format("<%s%s> %s", from, publicSuffix, msg));
                    lastChat.put(from, new Date());
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

    public void queueOfflineMessage(String to, String msg) {
        if (!offlineMessages.containsKey(to)) {
            offlineMessages.put(to, new ArrayList<OfflineMessage>());
        }
        offlineMessages.get(to).add(new OfflineMessage(msg));
    }

    public void signon(String user) {
        ArrayList<OfflineMessage> msgs = offlineMessages.get(user);
        if (msgs != null) {
            for (OfflineMessage msg : msgs) {
                sendNotify(String.format("[%s] %s", timestamp.format(msg.initDate), msg.message), user);
            }
            offlineMessages.remove(user);
        }
    }

    public boolean recieveChat() {
        return recieveChatMsgs || (lastChat.get(sendToUsername) != null
                && ((new Date()).getTime() - lastChat.get(sendToUsername).getTime()) / 1000 < tempChatLen);
    }

    public boolean publicChatActive() {
        if (!publicChat) {
            return false;
        }
        long now = (new Date()).getTime();
        for (Date d : lastChat.values()) {
            if ((now - d.getTime()) / 1000 < tempChatLen) {
                return true;
            }
        }
        return false;
    }

    public void publicChat(String msg) {
        long now = (new Date()).getTime();
        boolean mainIncl = false;
        for (String u : lastChat.keySet()) {
            if ((now - lastChat.get(u).getTime()) / 1000 < tempChatLen) {
                sendNotify(msg, u);
                if (!mainIncl && u.equalsIgnoreCase(sendToUsername)) {
                    mainIncl = true;
                }
            }
        }
        if (!mainIncl && recieveChatMsgs) {
            sendNotify(msg);
        }
    }

    // for sending the cached messages
    public class SendDelay extends TimerTask {

        public SendDelay(long delay) {
            (new Timer()).schedule(this, delay);
        }

        @Override
        public void run() {
            HashMap<String, ArrayList<String>> temp = new HashMap<String, ArrayList<String>>();
            temp.putAll(chatCache);
            chatCache.clear();
            long maxLen = messenger.maxMessageSize(); // mainly for AIM

            for (String u : temp.keySet()) {
                ArrayList<String> message = new ArrayList<String>();
                int num = temp.get(u).size();
                // if multiple lines, start on a new line
                if (num > 1) {
                    message.add("\n");
                } else {
                    message.add("");
                }

                for (int i = 0; i < num; ++i) {
                    if (message.get(message.size() - 1).length() + temp.get(u).get(i).length() > maxLen) {
                        // prefer split on newlines
                        if (temp.get(u).get(i).length() <= maxLen) {
                            message.add("\n" + temp.get(u).get(i));
                            continue;
                        }
                        // todo: split at beginning of tag, if will cut it off
                        //      then copy the last color tag to the beginning of the next
                        // else, messenger will auto-split that line
                    }
                    if (message.get(message.size() - 1).length() > 1) {
                        message.set(message.size() - 1, message.get(message.size() - 1).concat("\n" + temp.get(u).get(i)));
                    } else {
                        message.set(message.size() - 1, message.get(message.size() - 1).concat(temp.get(u).get(i)));
                    }
                }
                if (message.get(0).length() > 0) {
                    for (String l : message) {
                        messenger.sendMessage(u, l);
                    }
                }
            }
        }

        /*void send(String to, String message) {
        if (useProtocol == Protocol.AIM) {
        aimMess.sendMessage(to, message);
        //aimMess.sendMessage(u, AIMClient.stripHTML(message));
        }
        }*/
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
            publicSuffix = config.getString("publicSuffix", publicSuffix);
            publicChat = config.getBoolean("publicChat", publicChat);
            username = config.getString("username", "");
            password = config.getString("password", "");
            sendToUsername = config.getString("sendto", sendToUsername);
            notifyOnPlayer = config.getBoolean("notifyOnPlayer", notifyOnPlayer);
            recieveChatMsgs = config.getBoolean("recieveChat", recieveChatMsgs);
            formatColors = config.getBoolean("formatColors", formatColors);
            pingReply = config.getBoolean("pingReply", pingReply);
            pingResp = config.getString("pingResp", "");
            String p = config.getString("protocol");
            if (p != null) {
                //if (p.equalsIgnoreCase("skype")) { useProtocol = Protocol.SKYPE;}
                if(p.equalsIgnoreCase("xmpp")){
					useProtocol = Protocol.XMPP;
				} else if(p.equalsIgnoreCase("gtalk")){
					useProtocol = Protocol.GTALK;
				} else if(p.equalsIgnoreCase("jabber")){
					useProtocol = Protocol.JABBER;
				} else if (p.equalsIgnoreCase("msn")) {
                    useProtocol = Protocol.MSN;
                } else {
                    useProtocol = Protocol.AIM;
                }
            }
            if ((p = config.getString("tempChat")) != null) {
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

            if ((p = config.getString("timestamp")) != null) {
                try {
                    SimpleDateFormat ntstamp = new SimpleDateFormat(p);
                    ntstamp.format(new Date());
                    timestamp = ntstamp;
                } catch (Exception e) {
                    MinecraftIM.Log(Level.WARNING, "invalid timestamp format: \"" + p + "\": reverting to default");
                }
            }

            if ((p = config.getString("timezone")) != null) {
                try {
                    TimeZone t = TimeZone.getTimeZone(p);
                    if (t != null) {
                        timestamp.setTimeZone(t);
                    }
                } catch (Exception e) {
                    MinecraftIM.Log(Level.WARNING, "invalid timezone: \"" + p + "\": reverting to default");
                }
            }
            return true;
        } catch (Exception e) {
            MinecraftIM.Log(Level.SEVERE, "Failed to load config ", e);
        }
        return false;
    }
} // end class Messenger

class OfflineMessage {

    public Date initDate;
    public String message;

    public OfflineMessage(String msg) {
        initDate = new Date();
        message = msg;
    }
}

class MessengerRunCommander implements CommandSender {

    boolean op = false;
    String toUser = "";
    Messenger mess = null;
    private final PermissibleBase perm = new PermissibleBase(this);

    public MessengerRunCommander(Messenger m, boolean asOp) {
        mess = m;
        op = asOp;
    }

    public MessengerRunCommander(Messenger m, boolean asOp, String sendTo) {
        mess = m;
        op = asOp;
        toUser = sendTo;
    }

    public void sendMessage(String str) {
        if (mess != null) {
            if (toUser != null && !toUser.isEmpty()) {
                mess.sendNotify(str, toUser);
            } else {
                mess.sendNotify(str);
            }
        }
    }

    public boolean isOp() {
        return op;
    }

    public Server getServer() {
        return mess.callbackPlugin.getServer();
    }

	public boolean isPermissionSet(String string) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public boolean isPermissionSet(Permission prmsn) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public boolean hasPermission(String string) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public boolean hasPermission(Permission prmsn) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public PermissionAttachment addAttachment(Plugin plugin, String string, boolean bln) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public PermissionAttachment addAttachment(Plugin plugin) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public PermissionAttachment addAttachment(Plugin plugin, String string, boolean bln, int i) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public PermissionAttachment addAttachment(Plugin plugin, int i) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void removeAttachment(PermissionAttachment pa) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void recalculatePermissions() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public Set<PermissionAttachmentInfo> getEffectivePermissions() {
		return perm.getEffectivePermissions();
	}

	public void setOp(boolean bln) {
		op = bln;
	}
}
