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

import com.jascotty2.minecraftim.MSN_Messenger.SocketFactor;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import net.sf.jml.MsnContact;
import net.sf.jml.MsnList;
import net.sf.jml.MsnMessenger;
import net.sf.jml.MsnSwitchboard;
import net.sf.jml.MsnUserStatus;
import net.sf.jml.event.MsnAdapter;
import net.sf.jml.exception.IncorrectPasswordException;
import net.sf.jml.message.MsnInstantMessage;

public class MSN_MsnAdapter extends MsnAdapter {

	MSN_Messenger callback;
	HashMap<String, MsnContact> contacts = new HashMap<String, MsnContact>();

	public MSN_MsnAdapter(MSN_Messenger callback) {
		this.callback = callback;
	}

	@Override
	public void contactStatusChanged(MsnMessenger messenger, MsnContact contact) {
		if ((contact.getOldStatus().equals(MsnUserStatus.OFFLINE))
				&& (!contact.getStatus().equals(MsnUserStatus.OFFLINE))) {
			System.out.println("User " + contact.getDisplayName() + " logged in.");
		}
		if ((!contact.getOldStatus().equals(MsnUserStatus.OFFLINE))
				&& (contact.getStatus().equals(MsnUserStatus.OFFLINE))) {
			System.out.println("User " + contact.getDisplayName() + " logged out.");
		}
	}

	@Override
	public void loginCompleted(MsnMessenger messenger) {
		MinecraftIM.Log(messenger.getOwner().getEmail() + " login success");
	}

	@Override
	public void logout(MsnMessenger mm) {
		MinecraftIM.Log(mm.getOwner().getEmail() + " logout");
	}

	@Override
	public void exceptionCaught(MsnMessenger messenger, Throwable throwable) {
		if ((throwable instanceof IncorrectPasswordException)) {
			MinecraftIM.Log("User profile error:   Password or email are incorrect.");
		} else if ((throwable instanceof IOException)) {
			MinecraftIM.Log("Connection error");
		} else {
			MinecraftIM.Log("MSN exception: " + throwable);
		}
	}

	@Override
	public void switchboardStarted(MsnSwitchboard switchboard) {
		callback.newSwitchboard(switchboard);
		try {
			SocketFactor.createSocket(switchboard.getMessenger().getConnection().getRemoteIP(),
					switchboard.getMessenger().getConnection().getRemotePort());
		} catch (UnknownHostException e) {
			MinecraftIM.Log("Error: trying to connect to unknown host.");
		} catch (IOException e) {
			MinecraftIM.Log("Error: I/O problem");
		}
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
		System.out.println("recieved msg: " + mc.getEmail().getEmailAddress() +":  " +mim.getContent());
		ms.sendMessage(mim);
		callback.callbackMessenger.messageRecieved(mc.getEmail().getEmailAddress(), mim.getContent());
	}
}
