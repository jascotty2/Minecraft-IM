/**
 * Programmer: Jacob Scott
 * Program Name: Abstract_Messenger
 * Description: standardized implementation of a messenger class
 * Date: Mar 29, 2011
 */
package com.jascotty2.MinecraftIM;

/**
 *
 * @author jacob
 */
public interface Abstract_Messenger {
    //public void newMessenger(Messenger callback);
    public boolean connect(String uname, String pass);
    public void sendMessage(String msg);
    public void sendMessage(String to, String msg);
    public long maxMessageSize();
}
