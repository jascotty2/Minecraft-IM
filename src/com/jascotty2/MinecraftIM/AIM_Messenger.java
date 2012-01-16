/**
 * Programmer: Jacob Scott
 * Program Name: AIM_Messenger
 * Description:
 * Date: Mar 24, 2011
 */
package com.jascotty2.minecraftim;

import net.kano.joscar.ByteBlock;
import net.kano.joscar.OscarTools;
import net.kano.joscar.net.ClientConn;
import net.kano.joscar.net.ClientConnEvent;
import net.kano.joscar.flap.ClientFlapConn;
import net.kano.joscar.flapcmd.SnacCommand;
import net.kano.joscar.snac.*;
import net.kano.joscar.snaccmd.*;
import net.kano.joscar.snaccmd.chat.ChatMsg;
import net.kano.joscar.snaccmd.conn.ServiceRequest;
import net.kano.joscar.snaccmd.icbm.SendImIcbm;
import net.kano.joscar.snaccmd.rooms.JoinRoomCmd;
import net.kano.joscar.snaccmd.ssi.SsiDataCmd;
import net.kano.joscar.snaccmd.ssi.SsiItem;
import net.kano.joscar.ssiitem.BuddyItem;
import com.jascotty2.minecraftim.kano.joscardemo.security.*;
import com.jascotty2.minecraftim.kano.joscardemo.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author jacob
 */
public class AIM_Messenger extends Abstract_Messenger /* implements ChatConnListener */ { // extends Thread implements Runnable

	Messenger callbackMessenger;
	String username, password;
	public String sendTo;
	boolean connected = false;
	protected Map<String, Buddy> buddies = new HashMap<String, Buddy>();
	//protected Map<Integer, Group> groups = new HashMap<Integer, Group>();
	protected static final int DEFAULT_SERVICE_PORT = 5190;
	protected DefaultClientFactoryList factoryList = new DefaultClientFactoryList();
	protected ClientFlapConn loginFlapConn = null, mainConn = null;
	protected ClientSnacProcessor loginSnacProcessor = null;
	protected LoginConn loginConn = null;
	protected BosFlapConn bosConn = null;
	private SecureSession secureSession = SecureSession.getInstance();
	protected Set<ServiceConn> services = new HashSet<ServiceConn>();
	protected Map<String, ChatConn> chats = new HashMap<String, ChatConn>();
	protected SnacManager snacMgr = new SnacManager(new PendingSnacListener() {

		public void dequeueSnacs(SnacRequest[] pending) {
			System.out.println("dequeuing " + pending.length + " snacs");
			for (int i = 0; i < pending.length; i++) {
				handleRequest(pending[i]);
			}
		}
	});

	public AIM_Messenger(Messenger callback) {
		callbackMessenger = callback;
	}

	public long maxMessageSize() {
		return 1024;
	}

	public String getScreenname() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public SecureSession getSecureSession() {
		return secureSession;
	}

	public void setScreennameFormat(String screenname) {
		username = screenname;
	}

	public void startBosConn(String server, int port, ByteBlock cookie) {
		bosConn = new BosFlapConn(server, port, this, cookie);
		bosConn.connect();
	}

	public void loginFailed(String reason) {
		System.out.println("AIM login failed: " + reason);
	}

	public void registerSnacFamilies(BasicConn conn) {
		snacMgr.register(conn);
	}

	public void connectToService(int snacFamily, String host, ByteBlock cookie) {
		ServiceConn conn = new ServiceConn(host, DEFAULT_SERVICE_PORT, this,
				cookie, snacFamily);

		conn.connect();
	}

	public void serviceFailed(ServiceConn conn) {
	}

	public void serviceConnected(ServiceConn conn) {
		services.add(conn);
	}

	public void serviceReady(ServiceConn conn) {
		snacMgr.dequeueSnacs(conn);
	}

	public void serviceDied(ServiceConn conn) {
		services.remove(conn);
		snacMgr.unregister(conn);
	}

	public void joinChat(int exchange, String roomname) {
		FullRoomInfo roomInfo = new FullRoomInfo(exchange, roomname, "us-ascii", "en");
		handleRequest(new SnacRequest(new JoinRoomCmd(roomInfo), null));
	}

	public void connectToChat(FullRoomInfo roomInfo, String host,
			ByteBlock cookie) {
		ChatConn conn = new ChatConn(host, DEFAULT_SERVICE_PORT, this, cookie,
				roomInfo);

		conn.addChatListener(new MyChatConnListener());

		conn.connect();
	}

	public ChatConn getChatConn(String name) {
		return chats.get(OscarTools.normalize(name));
	}

	public synchronized void handleRequest(SnacRequest request) {
		int family = request.getCommand().getFamily();
		if (snacMgr.isPending(family)) {
			snacMgr.addRequest(request);
			return;
		}

		BasicConn conn = snacMgr.getConn(family);

		if (conn != null) {
			conn.sendRequest(request);
		} else {
			// it's time to request a service
			if (!(request.getCommand() instanceof ServiceRequest)) {
//				System.out.println("requesting " + Integer.toHexString(family) + " service.");
				snacMgr.setPending(family, true);
				snacMgr.addRequest(request);
				request(new ServiceRequest(family));
			} else {
				System.out.println("eep! can't find a service redirector server for " + family);
			}
		}
	}

	public SnacRequest request(SnacCommand cmd) {
		return request(cmd, null);
	}

	private SnacRequest request(SnacCommand cmd, SnacRequestListener listener) {
		SnacRequest req = new SnacRequest(cmd, listener);
		handleRequest(req);
		return req;
	}

	public void handleStateChange(ClientConnEvent e){
		MinecraftIM.Log("Connection state changed to " + e.getNewState()
				+ (e.getReason() != null ? " (" + e.getReason() + ")" : ""));
		if(e.getNewState() == ClientConn.STATE_CONNECTED){
			connected = true;
		} else { // if(e.getNewState() != ClientConn.STATE_CONNECTED) {
			if(connected != (connected = false)){
				MinecraftIM.Log("Connection dropped.. attempting reconnect");
				bosConn.connect();
			}
		}
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

		loginConn = new LoginConn("login.oscar.aol.com", DEFAULT_SERVICE_PORT, this);
		loginConn.connect();

		return true;
	}

	public void disconnect() {
		connected = false;
		if (loginConn != null) {
			loginConn.disconnect();
			loginConn = null;
		}
		if (bosConn != null) {
			bosConn.disconnect();
			bosConn = null;
		}
	}

	public void sendMessage(String msg) {
		sendMessage(sendTo, msg);
	}

	public void sendMessage(String to, String msg) {
		if (buddies.containsKey(to)) {
			if (!buddies.get(to).isOnline()) {
				//System.out.println("offline queued: " + to);
				callbackMessenger.queueOfflineMessage(to, msg);
				return;
			}
		}
		sendMessage(to, msg, false);
	}

	public void sendMessage(String to, String msg, boolean autoresponse) {
		if(bosConn == null || bosConn.getState() != ClientConn.STATE_CONNECTED){
			callbackMessenger.queueOfflineMessage(to, msg);
			return;
		}

//		request(new SendImIcbm(to, new InstantMessage(msg),
//				autoresponse, 0, false, null, null, true));

		request(new SendImIcbm(to, msg, autoresponse));
	}

	public void handleMessage(String buddy, String message) {
		if (!buddy.equalsIgnoreCase("aolsystemmsg")) {
			message = OscarTools.stripHtml(message.replace("<br>", "\n").replace("<BR>", "\n"));
			callbackMessenger.messageRecieved(buddy, message);
		}
	}

	public void handleMessage(String buddy, String message, boolean autoResponse) {
		if (!autoResponse) {
			handleMessage(buddy, message);
		}
	}

	public void handleBuddySignOn(String buddy) {
		if (!buddies.containsKey(buddy)) {
			buddies.put(buddy, new Buddy(buddy));
		}
		buddies.get(buddy).setOnline(true);
		//System.out.println("logon: " + buddy);
		//forward any saved messages
		callbackMessenger.signon(buddy);
	}

	public void handleBuddySignOff(String buddy) {
		if (!buddies.containsKey(buddy)) {
			buddies.put(buddy, new Buddy(buddy));
		}
		buddies.get(buddy).setOnline(false);
		//System.out.println("logoff: " + buddy);
	}

	public void initSSI(SsiDataCmd sdc) {
		SsiItem[] items = sdc.getItems();
		for (int i = 0; i < items.length; ++i) {
//			SsiItemObj obj = bosConn.getItemFactory().getItemObj(items[i]);
//			System.out.println("- " + (obj == null ? (Object) items[i]
//					: (Object) obj));
			if (items[i].getItemType() == SsiItem.TYPE_BUDDY) {
				BuddyItem b = (BuddyItem) bosConn.getItemFactory().getItemObj(items[i]);
				buddies.put(b.getScreenname(), new Buddy(b.getScreenname()));//,
						/*groups.containsKey(b.getGroupId()) ? groups.get(b.getGroupId()).getUsername() : ""));*/
			} /*else if (items[i].getItemType() == SsiItem.TYPE_GROUP
					&& items[i].getParentId() != 0) {
				GroupItem g = (GroupItem) bosConn.getItemFactory().getItemObj(items[i]);
				groups.put(g.getId(), new Group(g.getId(), g.getGroupName()));
			}*/
		}
	}

	private class MyChatConnListener implements ChatConnListener {

		public void connFailed(ChatConn conn, Object reason) {
		}

		public void connected(ChatConn conn) {
		}

		public void joined(ChatConn conn, FullUserInfo[] members) {
			String name = conn.getRoomInfo().getName();
			chats.put(OscarTools.normalize(name), conn);

			System.out.println("*** Joined "
					+ conn.getRoomInfo().getRoomName() + ", members:");
			for (int i = 0; i < members.length; i++) {
				System.out.println("  " + members[i].getScreenname());
			}
		}

		public void left(ChatConn conn, Object reason) {
			String name = conn.getRoomInfo().getName();
			chats.remove(OscarTools.normalize(name));

			System.out.println("*** Left "
					+ conn.getRoomInfo().getRoomName());
		}

		public void usersJoined(ChatConn conn, FullUserInfo[] members) {
			for (int i = 0; i < members.length; i++) {
				System.out.println("*** " + members[i].getScreenname()
						+ " joined " + conn.getRoomInfo().getRoomName());
			}
		}

		public void usersLeft(ChatConn conn, FullUserInfo[] members) {
			for (int i = 0; i < members.length; i++) {
				System.out.println("*** " + members[i].getScreenname()
						+ " left " + conn.getRoomInfo().getRoomName());
			}
		}

		public void gotMsg(ChatConn conn, FullUserInfo sender,
				ChatMsg msg) {
			String msgStr = msg.getMessage();
			String ct = msg.getContentType();
			if (msgStr == null && ct.equals("application/pkcs7-mime")) {
				ByteBlock msgData = msg.getMessageData();

				try {
					msgStr = secureSession.parseChatMessage(conn.getRoomName(),
							sender.getScreenname(), msgData);
				} catch (SecureSessionException e) {
					e.printStackTrace();
				}
			}
			System.out.println("<" + sender.getScreenname()
					+ ":#" + conn.getRoomInfo().getRoomName() + "> "
					+ OscarTools.stripHtml(msgStr));
		}
	}
} // end class AIM_Messenger

