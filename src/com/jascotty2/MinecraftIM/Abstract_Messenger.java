/**
 * Programmer: Jacob Scott
 * Program Name: Abstract_Messenger
 * Description: standardized implementation of a messenger class
 * Date: Mar 29, 2011
 */
package com.jascotty2.minecraftim;

/**
 *
 * @author jacob
 */
public abstract class Abstract_Messenger {
	//public void newMessenger(Messenger callback);

	public abstract boolean connect(String uname, String pass);

	public abstract void disconnect();

	public abstract void sendMessage(String msg);

	public abstract void sendMessage(String to, String msg);

	public abstract long maxMessageSize();

	/**
	 * Strip out HTML from a string
	 *
	 * @param line * *
	 * @return the string without HTML
	 */
	public static String stripHTML(String line) {
		StringBuffer sb = new StringBuffer(line);
		String out = "";

		for (int i = 0; i < (sb.length() - 1); i++) {
			if (sb.charAt(i) == '<') {
				// Most tags
				if ((sb.charAt(i + 1) == '/') || ((sb.charAt(i + 1) >= 'a') && (sb.charAt(i + 1) <= 'z'))
						|| ((sb.charAt(i + 1) >= 'A') && (sb.charAt(i + 1) <= 'Z'))) {
					for (int j = i + 1; j < sb.length(); j++) {
						if (sb.charAt(j) == '>') {
							sb = sb.replace(i, j + 1, "");
							i--;
							break;
						}
					}
				} else if (sb.charAt(i + 1) == '!') {
					// Comments
					for (int j = i + 1; j < sb.length(); j++) {
						if ((sb.charAt(j) == '>') && (sb.charAt(j - 1) == '-') && (sb.charAt(j - 2) == '-')) {
							sb = sb.replace(i, j + 1, "");
							i--;
							break;
						}
					}
				}
			}
		}

		out = sb.toString();
		return out;
	}
}
