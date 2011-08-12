/**
 * Programmer: Jacob Scott
 * Program Name: GTalkMessenger
 * Description:
 * Date: Aug 11, 2011
 */
package com.jascotty2.minecraftim;

/**
 * @author jacob
 */
public class GTalkMessenger extends XMPP_Messenger {

	public GTalkMessenger(Messenger callback) {
		super(callback);
		host = "talk.google.com";
		port = 5222;
		serviceName = "gmail.com";
	}

} // end class GTalkMessenger

