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
