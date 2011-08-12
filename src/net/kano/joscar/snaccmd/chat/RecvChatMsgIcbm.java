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
 *  File created by keith @ Mar 6, 2003
 *
 */

package net.kano.joscar.snaccmd.chat;

import net.kano.joscar.ByteBlock;
import net.kano.joscar.flapcmd.SnacPacket;
import net.kano.joscar.snaccmd.FullUserInfo;
import net.kano.joscar.tlv.Tlv;
import net.kano.joscar.tlv.TlvChain;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A SNAC command containing a message sent to a chat room by another user.
 *
 * @snac.src server
 * @snac.cmd 0x0e 0x06
 */
public class RecvChatMsgIcbm extends AbstractChatMsgIcbm {
    /**
     * A TLV type containing the user info block of the sender of this message.
     */
    private static final int TYPE_USERINFO = 0x0003;

    /** A user info block for the user who sent this message. */
    private final FullUserInfo senderInfo;

    /**
     * Generates a new incoming chat message ICBM from the given incoming SNAC
     * packet.
     *
     * @param packet an incoming chat message ICBM SNAC packet
     */
    protected RecvChatMsgIcbm(SnacPacket packet) {
        super(ChatCommand.CMD_RECV_CHAT_MSG, packet);

        TlvChain chain = getChatTlvs();

        Tlv userTlv = chain.getLastTlv(TYPE_USERINFO);

        if (userTlv != null) {
            ByteBlock userBlock = userTlv.getData();

            senderInfo = FullUserInfo.readUserInfo(userBlock);
        } else {
            senderInfo = null;
        }
    }

    /**
     * Creates a new outgoing chat message command with the given properties.
     *
     * @param messageId a message ID for this message
     * @param senderInfo a user info block for the user who sent this message,
     *        or <code>null</code> for none
     * @param chatMsg the chat message sent
     */
    public RecvChatMsgIcbm(long messageId, FullUserInfo senderInfo,
            ChatMsg chatMsg) {
        super(ChatCommand.CMD_RECV_CHAT_MSG, messageId, chatMsg);

        this.senderInfo = senderInfo;
    }

    /**
     * Returns a user info block for the user who sent this message.
     *
     * @return a user info block for the sender of this message
     */
    public final FullUserInfo getSenderInfo() {
        return senderInfo;
    }

    protected void writeChatTlvs(OutputStream out) throws IOException {
        if (senderInfo != null) {
            ByteBlock senderBlock = ByteBlock.createByteBlock(senderInfo);
            new Tlv(TYPE_USERINFO, senderBlock).write(out);
        }
    }

    public String toString() {
        return "RecvChatMsgIcbm: user=<" + senderInfo + "> - " + super.toString();
    }
}
