/*
 * Template a class must use to get messages
 */

package com.jascotty2.MinecraftIM;

/**
 *
 * @author jacob
 */
public interface ChatMessageHandler {
    /**
     * Called when MinecraftIM receives a message from the authorized user
     * @param message what was received 
     * @return if this method used the command, and don't do anything else
     */
    public boolean messageHandled(String message);
    /**
     * Called after any message received 
     * @param fromUser what username this message came from
     * @param message what was received 
     * @return if this method used the command, and don't do anything else
     */
    public boolean messageHandled(String fromUser, String message);
}
