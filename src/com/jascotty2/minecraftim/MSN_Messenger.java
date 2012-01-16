/**
 * Programmer: Jacob Scott
 * Program Name: MSN_Messenger
 * Description:
 * Date: Aug 14, 2011
 */
package com.jascotty2.minecraftim;

import java.util.HashMap;
import net.sf.jml.Email;
import net.sf.jml.MsnContact;
import net.sf.jml.MsnList;
import net.sf.jml.MsnMessenger;
import net.sf.jml.MsnSwitchboard;
import net.sf.jml.event.MsnAdapter;
import net.sf.jml.impl.MsnMessengerFactory;
import net.sf.jml.message.MsnInstantMessage;

/**
 * @author jacob
 */
public class MSN_Messenger extends Abstract_Messenger {

	Messenger callbackMessenger;
	protected String email;
	protected String password;
	public String sendTo;
	public Email sendToEmail;
	MsnMessenger messenger;
	MsnAdapter listener;
	HashMap<String, MsnContact> contacts = new HashMap<String, MsnContact>();

	public MSN_Messenger(Messenger callback) {
		callbackMessenger = callback;
	}

	@Override
	public boolean connect(String uname, String pass) {
		email = uname;
		password = pass;
		//create MsnMessenger instance
		messenger = MsnMessengerFactory.createMsnMessenger(email, password);

		messenger.addListener((listener = new MessengerListener()));
		//messenger.addMessageListener(listener);
		
		messenger.login();
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
			messenger.sendText(sendToEmail, msg);
		}
	}

	@Override
	public void sendMessage(String to, String msg) {
		messenger.sendText(Email.parseStr(to), msg);
	}

	@Override
	public long maxMessageSize() {
		return 10240; // don't think this is actual max..
	}

	class MessengerListener extends MsnAdapter {

		@Override
		public void loginCompleted(MsnMessenger messenger) {
			MinecraftIM.Log(messenger.getOwner().getEmail() + " login success");
		}

		@Override
		public void logout(MsnMessenger mm) {
			MinecraftIM.Log(messenger.getOwner().getEmail() + " logout");
		}

		@Override
		public void exceptionCaught(MsnMessenger messenger,
				Throwable throwable) {
			MinecraftIM.Log("MSN exception: " + throwable);
		}

		@Override
		public void contactListInitCompleted(MsnMessenger messenger) {
			//get contacts in allow list
			MsnContact[] c = messenger.getContactList().getContactsInList(MsnList.AL);
			for (MsnContact m : c) {
				contacts.put(m.getEmail().getEmailAddress(), m);
				System.out.println("added contact: " + m);
			}
		}

		@Override
		public void contactListSyncCompleted(MsnMessenger messenger) {
			contacts.clear();
			contactListInitCompleted(messenger);
		}

//		@Override
//		public void contactStatusChanged(MsnMessenger mm, MsnContact mc) {
//			if (!contacts.containsKey(mc.getEmail().getEmailAddress())) {
//				contacts.put(mc.getEmail().getEmailAddress(), mc);
//			}
//		}
		@Override
		public void instantMessageReceived(MsnSwitchboard ms, MsnInstantMessage mim, MsnContact mc) {
			callbackMessenger.messageRecieved(mc.getEmail().getEmailAddress(), mim.getContent());
		}
	}
} // end class MSN_Messenger

