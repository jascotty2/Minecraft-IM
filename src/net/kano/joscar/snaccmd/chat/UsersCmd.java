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

import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.MiscTools;
import net.kano.joscar.flapcmd.SnacPacket;
import net.kano.joscar.snaccmd.FullUserInfo;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * A base class for the two member-list-based commands in this package,
 * {@link UsersJoinedCmd} and {@link UsersLeftCmd}.
 */
public abstract class UsersCmd extends ChatCommand {
    /** The user info blocks contained in this user command. */
    private final FullUserInfo[] users;

    /**
     * Creates a new user-based command from the given incoming SNAC packet.
     *
     * @param command the command's SNAC command subtype
     * @param packet an incoming user-based SNAC packet
     */
    protected UsersCmd(int command, SnacPacket packet) {
        super(command);

        DefensiveTools.checkNull(packet, "packet");

        List userList = new LinkedList();

        ByteBlock block = packet.getData();

        for (;;) {
            FullUserInfo user = FullUserInfo.readUserInfo(block);
            if (user == null) break;

            userList.add(user);

            block = block.subBlock(user.getTotalSize());
        }

        users = (FullUserInfo[]) userList.toArray(new FullUserInfo[0]);
    }

    /**
     * Creates a new outgoing user-based command with the given SNAC command
     * subtype and list of users.
     *
     * @param command the SNAC command subtype of this command
     * @param users the users this command concerns
     */
    protected UsersCmd(int command, FullUserInfo[] users) {
        super(command);

        this.users = (FullUserInfo[]) (users == null ? null : users.clone());
    }

    /**
     * Returns the list of users sent in this command.
     *
     * @return the list of users
     */
    public final FullUserInfo[] getUsers() {
        return (FullUserInfo[]) (users == null ? null : users.clone());
    }

    public void writeData(OutputStream out) throws IOException {
        if (users != null) {
            for (int i = 0; i < users.length; i++) {
                users[i].write(out);
            }
        }
    }

    public String toString() {
        return MiscTools.getClassName(this) + " for " + users.length + " users";
    }
}
