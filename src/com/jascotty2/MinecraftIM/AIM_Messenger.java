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

/**
 * @author jacob
 */
public class AIM_Messenger extends AIMAdapter implements Abstract_Messenger { // extends Thread implements Runnable

    Messenger callbackMessenger;
    AIMClient client = null;
    String username, password;
    public String sendTo;
    AIMBuddy sendToUser = null;

    public long maxMessageSize() {
        return 1024;
    }

    public AIM_Messenger(Messenger callback) {
        callbackMessenger = callback;
    }

    /**
     * Makes a connection to the TOC server.
     *
     * @param uname
     * @param pass 
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

    public void disconnect() {
        if (client != null) {
            client.signOff();
            client = null;
        }
    }

    public void sendMessage(String msg) {
        sendMessage(sendToUser, msg);
    }

    public void sendMessage(String to, String msg) {
        if (sendTo.length() != 0) {
            if (sendToUser != null && to.equalsIgnoreCase(sendToUser.getName())) {
                sendMessage(msg);
            } else {
                AIMBuddy sendto = client.getBuddy(to);
                if (sendto == null) {
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
                //client.sendMesg(sendTo, msg);
            } else {
                callbackMessenger.queueOfflineMessage(buddy.getName(), msg);
            }
        }
    }

    @Override
    public void handleMessage(AIMBuddy buddy, String request) {
        if (!buddy.getName().equals("aolsystemmsg")) {
            callbackMessenger.messageRecieved(buddy.getName(), request);
        }
    }

    @Override
    public void handleBuddySignOn(AIMBuddy buddy, String info) {
        //System.out.println("signon: " + buddy.getName() + " (" + buddy.getGroup() + ")");
        //forward any saved messages
        callbackMessenger.signon(buddy.getName());
    }
} // end class AIM_Messenger

