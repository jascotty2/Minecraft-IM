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

import net.kano.joscar.flapcmd.SnacCommand;
import net.kano.joscar.snaccmd.conn.SnacFamilyInfo;

/**
 * A base class for commands in the "chat" <code>0x0e</code> SNAC family.
 */
public abstract class ChatCommand extends SnacCommand {
    /** The family code for this SNAC family. */
    public static final int FAMILY_CHAT = 0x000e;

    /** A set of SNAC family information for this family. */
    public static final SnacFamilyInfo FAMILY_INFO
            = new SnacFamilyInfo(FAMILY_CHAT, 0x0001, 0x0010, 0x739);

    /** A command subtype for sending a message to a chat room. */
    public static final int CMD_SEND_CHAT_MSG = 0x0005;

    /** A command subtype for a room information update notification. */
    public static final int CMD_ROOM_UPDATE = 0x0002;
    /** A command subtype for a command sent when users enter a chat room. */
    public static final int CMD_USERS_JOINED = 0x0003;
    /** A command subtype for a command sent when users exit a chat room. */
    public static final int CMD_USERS_LEFT = 0x0004;
    /** A command subtype for a chat message command. */
    public static final int CMD_RECV_CHAT_MSG = 0x0006;

    /**
     * Creates a new SNAC command in the chat family.
     *
     * @param command the SNAC command subtype
     */
    protected ChatCommand(int command) {
        super(FAMILY_CHAT, command);
    }
}
