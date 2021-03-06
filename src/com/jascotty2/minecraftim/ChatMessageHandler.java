/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: Template a class must use to get messages
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

/**
 *
 * @author jacob
 */
public interface ChatMessageHandler {
    /**
     * Called when MinecraftIM receives a message from the authorized user
     * @param message what was received 
     * @return if this method used the command, and don't do anything else
     */
    public boolean messageHandled(String message);
    /**
     * Called after any message received 
     * @param fromUser what username this message came from
     * @param message what was received 
     * @return if this method used the command, and don't do anything else
     */
    public boolean messageHandled(String fromUser, String message);
}
