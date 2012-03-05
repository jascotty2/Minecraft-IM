/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: (TODO)
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

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.channels.UnresolvedAddressException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import net.sf.cindy.Message;
import net.sf.cindy.Session;
//import net.sf.cindy.SessionListener;
import net.sf.jml.Email;
import net.sf.jml.MsnContact;
import net.sf.jml.MsnSwitchboard;
import net.sf.jml.MsnUserStatus;
import net.sf.jml.event.MsnAdapter;
import net.sf.jml.impl.MsnContactImpl;
import net.sf.jml.impl.MsnMessengerFactory;
import net.sf.jml.impl.SimpleMessenger;
import net.sf.jml.message.MsnEmailActivityMessage;
import net.sf.jml.message.MsnInstantMessage;
import net.sf.jml.net.SessionListener;
import net.sf.jml.protocol.outgoing.OutgoingXFR;

/**
 * @author jacob
 */
public class MSN_Messenger extends Abstract_Messenger {

	protected Messenger callbackMessenger;
	protected String email;
	protected String password;
	public String sendTo;
	public Email sendToEmail;
	//MsnMessenger messenger;
	SimpleMessenger messenger;
	MSN_MsnAdapter msnAdapter;
	private SessionListener incomingMessageListener;
	private Socket socket;
	private HashMap<String, MsnSwitchboard> switchboard = new HashMap<String, MsnSwitchboard>();

	public MSN_Messenger(Messenger callback) {
		callbackMessenger = callback;
	}

	@Override
	public boolean connect(String uname, String pass) {
		email = uname;
		password = pass;
		//create MsnMessenger instance
		messenger = (SimpleMessenger) MsnMessengerFactory.createMsnMessenger(email, password);
		messenger.getOwner().setInitStatus(MsnUserStatus.ONLINE);

		msnAdapter = new MSN_MsnAdapter(this);
		incomingMessageListener = new MsnIncomingMessageListener(msnAdapter);

		messenger.addSessionListener(incomingMessageListener);
		messenger.addListener(msnAdapter);

		try {
			this.messenger.login();
			this.socket = SocketFactor.createSocket("messenger.hotmail.com", 1863);
		} catch (UnresolvedAddressException e) {
			MinecraftIM.Log(Level.SEVERE, "ERROR: Unresolved address. Connection could not be etablised.");
		} catch (UnknownHostException e) {
			MinecraftIM.Log(Level.SEVERE, "ERROR: Unknown Host. Socket could not be created.");
		} catch (IOException e) {
			MinecraftIM.Log(Level.SEVERE, "ERROR: I/O problem. Socket could not be created.");
		} catch (NullPointerException localNullPointerException1) {
		}
		try {
			this.socket.getInetAddress().getHostName();
			this.socket.getInetAddress().getHostAddress();
			this.socket.getPort();
			this.socket.getLocalAddress().getHostName();
			this.socket.getLocalAddress().getHostAddress();
			this.socket.getLocalPort();
		} catch (NullPointerException e) {
			MinecraftIM.Log(Level.SEVERE, "Connection error: ", e);
		}
		return messenger.getConnection() != null;
	}

	@Override
	public void disconnect() {
		if (messenger != null) {
			messenger.logout();
		}
	}

	@Override
	public void sendMessage(String msg) {
		if (sendToEmail == null && sendTo != null) {
			sendToEmail = Email.parseStr(sendTo);
		}

		if (sendToEmail != null) {
			MsnContact to = msnAdapter.contacts.get(sendToEmail.getEmailAddress());
			if (to == null) {
				messenger.addFriend(sendToEmail, "");
				callbackMessenger.queueOfflineMessage(sendToEmail.getEmailAddress(), msg);
			} else if (to.getStatus() == MsnUserStatus.AWAY) {
				callbackMessenger.queueOfflineMessage(sendToEmail.getEmailAddress(), msg);
			} else {
				messenger.sendText(sendToEmail, msg);
			}

		}
	}

	@Override
	public void sendMessage(String to, String str) {
		//messenger.sendText(Email.parseStr(to), msg);
		MsnInstantMessage msg = new MsnInstantMessage();
		msg.setContent(str);
		if (!switchboard.containsKey(to)) {
			//switchboard.put(to, new MsnSwitchboard());
			if(waiting.containsKey(to)) {
				waiting.get(to).add(str);
			} else {
				waiting.put(to, new ArrayList<String>());
				waiting.get(to).add(str);
				System.out.println("need switchboard for " + to);
				requestSwitchboardCreation();
			}
		} else {
			switchboard.get(to).sendText(str);
		}
	}

	HashMap<String, ArrayList<String>> waiting = new HashMap<String, ArrayList<String>>();
	
	private void requestSwitchboardCreation() {
		OutgoingXFR msg = new OutgoingXFR(messenger.getActualMsnProtocol());
		msg.setAttachment(MsnUserStatus.ONLINE);
		messenger.send(msg, true);
		System.out.println("requesting new switchboard..");
	}

	protected void newSwitchboard(MsnSwitchboard swb) {
		String usr = swb.getAllContacts()[0].getEmail().getEmailAddress();
		System.out.println("new switchboard recieved for " + usr);
		if(waiting.containsKey(usr)) {
			switchboard.put(usr, swb);
			for(String msg : waiting.get(usr)) {
				swb.sendText(msg);
			}
			waiting.remove(usr);
		}
	}
	
	@Override
	public long maxMessageSize() {
		return 10240; // don't think this is actual max..
	}

	public static class MsnIncomingMessageListener implements SessionListener {

		private ArrayList<String> lstMessages;
		private MsnAdapter msnAdapter;

		public MsnIncomingMessageListener(MsnAdapter msnAdapter) {
			this.msnAdapter = msnAdapter;
			this.lstMessages = new ArrayList<String>();
		}

		public void messageReceived(Session arg0, Message message) throws Exception {
			System.out.println(message.toString());
			if (message.toString().contains("LST")) {
				this.lstMessages.add(message.toString());
			}

			if ((message.toString().contains("MSG")) && (message.toString().contains("text/x-msmsgsemailnotification"))) {
				fireEmailNotification(message.toString());
			}
		}

		public ArrayList<String> getLSTmessages() {
			return this.lstMessages;
		}

		private void fireEmailNotification(String message) {
			MsnContactImpl contact = new MsnContactImpl(null);
			MsnEmailActivityMessage emailMessage = new MsnEmailActivityMessage();

			String[] messageSplitted = message.split("\r\n");
			int messageLength = messageSplitted.length;

			String senderName = "";
			String messageSubject = "";
			String senderEmail = "";

			for (int i = 0; i < messageLength; i++) {
				if ((senderName.compareTo("") == 0) && (messageSplitted[i].contains("From"))) {
					senderName = messageSplitted[i].split("From: ")[1];
				}
				if ((messageSubject.compareTo("") == 0) && (messageSplitted[i].contains("Subject"))) {
					messageSubject = messageSplitted[i].split("Subject: ")[1];
				}
				if ((senderEmail.compareTo("") == 0) && (messageSplitted[i].contains("From-Addr"))) {
					senderEmail = messageSplitted[i].split("From-Addr: ")[1];
				}
			}

			System.out.println(Email.parseStr(senderEmail) + " EmailString :\"" + senderEmail + "\"");
			contact.setEmail(Email.parseStr(senderEmail));
			contact.setDisplayName(senderName);

			emailMessage.setSrcFolder(messageSubject);

			this.msnAdapter.activityEmailNotificationReceived(null, emailMessage, contact);
		}

		public void exceptionCaught(Session arg0, Throwable arg1) throws Exception {
		}

		public void messageSent(Session arg0, Message arg1) throws Exception {
		}

		public void sessionClosed(Session arg0) throws Exception {
		}

		public void sessionEstablished(Session arg0) throws Exception {
		}

		public void sessionIdle(Session arg0) throws Exception {
		}

		public void sessionTimeout(Session arg0) throws Exception {
		}

		public void sessionEstablished(net.sf.jml.net.Session sn) throws Exception {
		}

		public void sessionClosed(net.sf.jml.net.Session sn) throws Exception {
		}

		public void sessionIdle(net.sf.jml.net.Session sn) throws Exception {
		}

		public void sessionTimeout(net.sf.jml.net.Session sn) throws Exception {
		}

		public void messageReceived(net.sf.jml.net.Session sn, net.sf.jml.net.Message msg) throws Exception {
		}

		public void messageSent(net.sf.jml.net.Session sn, net.sf.jml.net.Message msg) throws Exception {
		}

		public void exceptionCaught(net.sf.jml.net.Session sn, Throwable thrwbl) throws Exception {
		}
	}

	public static class SocketFactor {

		private static ArrayList<SSLSocket> socketRegistry = new ArrayList();

		public static Socket createSocket(String host, int port)
				throws UnknownHostException, IOException {
			SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			SSLSocket sslsocket = (SSLSocket) factory.createSocket(host, port);
			socketRegistry.add(sslsocket);
			return sslsocket;
		}

		public static boolean deactivateSocket(String host) {
			SSLSocket socket = null;

			for (int i = 0; i < socketRegistry.size(); i++) {
				socket = (SSLSocket) socketRegistry.get(i);
				if (socket.getInetAddress().getHostAddress() == null ? host == null : socket.getInetAddress().getHostAddress().equals(host)) {
					System.out.println("Socket at " + host + " deactiveted.");
					return socketRegistry.remove(socket);
				}
			}

			return false;
		}
	}
} // end class MSN_Messenger

