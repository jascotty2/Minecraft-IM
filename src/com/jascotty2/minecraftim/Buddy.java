
package com.jascotty2.minecraftim;

//import java.util.ArrayList;

public class Buddy {

	String username, name;
	boolean online;

	public Buddy(String name) {
		this.username = name;
	}

	public final void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getUsername() {
		return username;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}

	public boolean isOnline() {
		return online;
	}

//	ArrayList<String> messages = new ArrayList<String>();
//
//	public ArrayList<String> getMessages() {
//		return messages;
//	}
//
//	public void addMessage(String message) {
//		messages.add(message);
//	}
//
//	public void clearMessages() {
//		messages.clear();
//	}
//
//	public boolean hasMessages() {
//		return !messages.isEmpty();
//	}

}
