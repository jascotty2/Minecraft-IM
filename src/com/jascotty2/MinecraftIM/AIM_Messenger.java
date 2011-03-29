/**
 * Programmer: Jacob Scott
 * Program Name: AIM_Messenger
 * Description:
 * Date: Mar 24, 2011
 */
package com.jascotty2.MinecraftIM;

import com.levelonelabs.aim.AIMAdapter;
import com.levelonelabs.aim.AIMBuddy;
import com.levelonelabs.aim.AIMClient;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * @author jacob
 */
public class AIM_Messenger extends AIMAdapter { // extends Thread implements Runnable

    Messenger callbackMessenger;
    AIMClient client = null;
    String username, password;
    public String sendTo;
    AIMBuddy sendToUser = null;
    HashMap<String, ArrayList<OfflineMessage>> offlineMessages = new HashMap<String, ArrayList<OfflineMessage>>();

    public class OfflineMessage {

        public Date initDate;
        public String message;

        public OfflineMessage(String msg) {
            initDate = new Date();
            message = msg;
        }
    }

    public AIM_Messenger(Messenger callback) {
        callbackMessenger = callback;
    }

    /**
     * Makes a connection to the TOC server.
     *
     * @return  returns whether or not the connection was successful
     */
    public boolean connect(String uname, String pass) {
        username = uname;
        password = pass;
        client = new AIMClient(uname, pass, "MinecraftIM", true);
        client.defaltGroup = "MinecraftIM";

        client.signOn();
        //client.setPermitMode(AIMClient.PERMIT_ALL);

        sendTo = callbackMessenger.sendToUsername;
        if (sendTo != null && sendTo.length() > 0) {
            sendToUser = new AIMBuddy(sendTo, "MinecraftIM");
            client.addBuddy(sendToUser);
        }
        client.addAIMListener(this);
        //this.start();

        return true;
    }

    public void sendMessage(String msg) {
        if (sendToUser != null) {
            if (sendToUser.isOnline()) {
                client.sendMessage(sendToUser, msg);
                //client.sendMesg(sendTo, msg);
            } else {
                if (!offlineMessages.containsKey(sendToUser.getName())) {
                    offlineMessages.put(sendToUser.getName(), new ArrayList<OfflineMessage>());
                }
                offlineMessages.get(sendToUser.getName()).add(new OfflineMessage(msg));
            }
        }
    }

    public void sendMessage(String to, String msg) {
        if (sendTo.length() != 0) {
            if (sendToUser != null && to.equalsIgnoreCase(sendToUser.getName())) {
                sendMessage(msg);
            } else {
                AIMBuddy sendto = client.getBuddy(to);
                if(sendto==null){
                    sendto = new AIMBuddy(to);
                    sendto.setGroup("MinecraftIM");
                }
                sendMessage(sendto, msg);
            }
        }
    }

    public void sendMessage(AIMBuddy buddy, String msg) {
        if (buddy != null) {
            if (buddy.isOnline()) {
                client.sendMessage(buddy, msg);
            } else {
                if (!offlineMessages.containsKey(buddy.getName())) {
                    offlineMessages.put(buddy.getName(), new ArrayList<OfflineMessage>());
                }
                offlineMessages.get(buddy.getName()).add(new OfflineMessage(msg));
            }
        }
    }

    @Override
    public void handleMessage(AIMBuddy buddy, String request) {
        callbackMessenger.messageRecieved(buddy.getName(), request);
    }

    @Override
    public void handleBuddySignOn(AIMBuddy buddy, String info) {
        //System.out.println("signon: " + buddy.getName() + " (" + buddy.getGroup() + ")");
        //forward any saved messages
        if (offlineMessages.containsKey(buddy.getName())) {
            ArrayList<OfflineMessage> msgs = offlineMessages.get(buddy.getName());
            for (OfflineMessage msg : msgs) {
                client.sendMessage(buddy, String.format("[%s] %s", msg.initDate.toString(), msg.message));
            }
            offlineMessages.remove(sendToUser.getName());
        }
    }
    
} // end class AIM_Messenger

