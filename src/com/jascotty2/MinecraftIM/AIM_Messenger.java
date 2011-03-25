/**
 * Programmer: Jacob Scott
 * Program Name: AIM_Messenger
 * Description:
 * Date: Mar 24, 2011
 */
package com.jascotty2.MinecraftIM;

import com.levelonelabs.aim.AIMClient;


import org.bukkit.plugin.Plugin;

/**
 * @author jacob
 */
public class AIM_Messenger extends Thread implements Runnable {

    Messenger callbackMessenger;
    AIMClient client = null;
    String username, password;
    public String sendTo;

    public AIM_Messenger(Messenger callback) {
        callbackMessenger = callback;
    }

    /**
     * Makes a connection to the TOC server.
     *
     * @return  returns whether or not the connection was successful
     */
    public boolean connect(String uname, String pass) {
        sendTo = callbackMessenger.sendToUsername;
        username = uname;
        password = pass;
        client = new AIMClient(uname, pass, "MinecraftIM", true);

        client.signOn();
        client.setPermitMode(AIMClient.PERMIT_ALL);
        
        this.start();

        return true;
    }

    public void sendMessage(String msg) {
        if (sendTo.length() != 0) {
            client.sendMesg(sendTo, msg);
        }
    }
    
    public void sendMessage(String to, String msg) {
        if (sendTo.length() != 0) {
            client.sendMesg(to, msg);
        }
    }

    @Override
    public void run() {
        
        while (true) {
            String msg = client.getMessage();
            //System.out.println(msg);
            if(msg.contains(": ")){
                callbackMessenger.messageRecieved(msg.substring(0, msg.indexOf(": ")),
                        msg.substring(msg.indexOf(": ")+2));
            }
        }
    }
} // end class AIM_Messenger

