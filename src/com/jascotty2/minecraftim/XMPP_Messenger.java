/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: messaging through the xmpp protocol using the smack library
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jascotty2.minecraftim;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

public class XMPP_Messenger extends Abstract_Messenger implements MessageListener {

	Messenger callbackMessenger;
	String username, password;
	public String sendTo;
	XMPPConnection connection = null;
	protected String host = "talk.google.com";
	protected int port = 5222;
	protected String serviceName = "gmail.com";
	protected Map<String, Buddy> buddies = new HashMap<String, Buddy>();
	protected Map<String, Chat> chats = new HashMap<String, Chat>();
	protected RosterListener listener = null;

	public XMPP_Messenger(Messenger callback) {
		callbackMessenger = callback;
	}

	@Override
	public boolean connect(String uname, String pass) {
		try {
			ConnectionConfiguration config = new ConnectionConfiguration(host, port, serviceName);
			disconnect();
			connection = new XMPPConnection(config);
			connection.connect();
			connection.login(uname, pass);

			Roster roster = connection.getRoster();
			Collection<RosterEntry> entries = roster.getEntries();

			for (RosterEntry r : entries) {
				buddies.put(r.getUser(), new Buddy(r.getUser()));
				buddies.get(r.getUser()).setOnline(roster.getPresence(r.getUser()) != null);
			}

			listener = new RosterListener() {

				public void entriesAdded(Collection<String> clctn) {
				}

				public void entriesUpdated(Collection<String> clctn) {
				}

				public void entriesDeleted(Collection<String> clctn) {
				}

				public void presenceChanged(Presence prsnc) {
					String uname = prsnc.getFrom();
					if (uname.contains("/")) {
						uname = uname.substring(0, uname.indexOf('/'));
					}
					Buddy b = buddies.get(uname);
					if (b == null) {
						// add to buddy list
						b = new Buddy(uname);
						buddies.put(uname, b);
					}
					//System.out.println("presence changed: " + uname + ": " + prsnc + "  -" + (prsnc.isAvailable() || prsnc.isAway()));
					b.setOnline(prsnc.isAvailable() || prsnc.isAway());
				}
			};
			roster.addRosterListener(listener);
			roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
			return true;
		} catch (XMPPException ex) {
			MinecraftIM.Log(Level.SEVERE, "Error logging in to XMPP protocal: " + ex.getMessage());
		}
		return false;
	}

	@Override
	public void disconnect() {
		if (connection != null) {
			connection.disconnect(new Presence(Presence.Type.unavailable));
			connection.getRoster().removeRosterListener(listener);
			listener = null;
			connection = null;
		}
	}

	@Override
	public void sendMessage(String msg) {
		sendMessage(sendTo, msg);
	}

	@Override
	public void sendMessage(String to, String msg) {
		if (buddies.containsKey(to)) {
			if (!buddies.get(to).isOnline()) {
				callbackMessenger.queueOfflineMessage(to, msg);
			} else {
				Chat c = chats.get(to);
				if (c == null) {
					c = connection.getChatManager().createChat(to, this);
					chats.put(to, c);
				}
				try {
					c.sendMessage(msg);
					//c.sendMessage(new Message(msg, Message.Type.headline)); // not sure what does..
				} catch (XMPPException ex) {
					MinecraftIM.Log(Level.SEVERE, "Error sending message via XMPP to " + to + ": " + ex.getMessage());
				}
			}
		}
	}

	@Override
	public long maxMessageSize() {
		return 10240; // don't think this is actual max..
	}

	public void processMessage(Chat chat, Message msg) {
		callbackMessenger.messageRecieved(chat.getParticipant(), msg.getBody());
	}
} // end class XMPP_Messenger

