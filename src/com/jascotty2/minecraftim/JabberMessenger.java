/**
 * Programmer: Jacob Scott
 * Program Name: JabberMessenger
 * Description:
 * Date: Aug 11, 2011
 */

package com.jascotty2.minecraftim;

/**
 * @author jacob
 */
public class JabberMessenger extends XMPP_Messenger {

    public JabberMessenger(Messenger callback) {
		super(callback);
		host = "jabber.org";
		port = 5222;
		serviceName = "jabber.org";
	}
	
} // end class JabberMessenger
