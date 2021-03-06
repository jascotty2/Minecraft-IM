/*
 *  Copyright (c) 2002-2003, The Joust Project
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without 
 *  modification, are permitted provided that the following conditions 
 *  are met:
 *
 *  - Redistributions of source code must retain the above copyright 
 *    notice, this list of conditions and the following disclaimer. 
 *  - Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in 
 *    the documentation and/or other materials provided with the 
 *    distribution. 
 *  - Neither the name of the Joust Project nor the names of its
 *    contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission. 
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 *  FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 *  COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 *  BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 *  CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
 *  LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
 *  ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 *  POSSIBILITY OF SUCH DAMAGE.
 *
 *  File created by keith @ Feb 27, 2003
 *
 */

package net.kano.joscar.snaccmd.chat;

import net.kano.joscar.flapcmd.SnacPacket;
import net.kano.joscar.snaccmd.FullUserInfo;

/**
 * A SNAC command sent to indicate that one or more users have joined a chat
 * room. Also sent upon joining a room to list the users in that room.
 *
 * @snac.src server
 * @snac.cmd 0x0e 0x03
 */
public class UsersJoinedCmd extends UsersCmd {
    /**
     * Creates a new user join command from the given incoming SNAC packet.
     *
     * @param packet an incoming user join SNAC packet
     */
    protected UsersJoinedCmd(SnacPacket packet) {
        super(CMD_USERS_JOINED, packet);
    }

    /**
     * Creates a new outgoing uesr join command with the given list of users.
     *
     * @param users the users who have joined the chat room (or a list of the
     *        users in the chat room if this is sent to a joining user)
     */ 
    public UsersJoinedCmd(FullUserInfo[] users) {
        super(CMD_USERS_JOINED, users);
    }
}
