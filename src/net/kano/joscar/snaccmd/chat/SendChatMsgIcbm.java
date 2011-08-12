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

import net.kano.joscar.flapcmd.SnacPacket;
import net.kano.joscar.tlv.Tlv;
import net.kano.joscar.tlv.TlvChain;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A command used to send a message to a chat room.
 *
 * @snac.src client
 * @snac.cmd 0x0e 0x05
 */
public class SendChatMsgIcbm extends AbstractChatMsgIcbm {
    /**
     * A TLV type present if this message should be sent back to the client as a
     * chat room message.
     */
    private static final int TYPE_REFLECT = 0x0006;
    /** A TLV type present if this message is an auto-response. */
    private static final int TYPE_AUTORESPONSE = 0x0007;

    /**
     * Whether or not this message should be "reflected" back to the client as
     * chat room text.
     */
    private boolean toBeReflected;

    /**
     * Generates a new chat message send ICBM from the given incoming SNAC
     * packet.
     *
     * @param packet an incoming send-chat-message SNAC packet
     */
    protected SendChatMsgIcbm(SnacPacket packet) {
        super(ChatCommand.CMD_SEND_CHAT_MSG, packet);

        TlvChain chain = getChatTlvs();

        toBeReflected = chain.hasTlv(TYPE_REFLECT);
    }

    /**
     * Creates a new outgoing chat message ICBM with default properties. The
     * ICBM is created with an ICBM message ID of <code>0</code>, marked as an
     * outgoing chat message that should be reflected back to the client as chat
     * text and that is not an auto-response.
     * <br>
     * <br>
     * Calling this method is equivalent to calling
     * #SendChatMsgIcbm(long, boolean, ChatMsg) new SendChatMsgIcbm(0, true,
     * chatMsg)}.
     *
     * @param chatMsg the chat message to send to the chat room
     */
    public SendChatMsgIcbm(ChatMsg chatMsg) {
        this(0, true, chatMsg);
    }

    /**
     * Creates a new outgoing chat message ICBM with the given properties.
     *
     * @param messageId a message ID for this message
     * @param toBeReflected whether this message should be "reflected" back as
     *        normal chat text
     * @param chatMsg the chat message to send
     */
    public SendChatMsgIcbm(long messageId, boolean toBeReflected,
            ChatMsg chatMsg) {
        super(ChatCommand.CMD_SEND_CHAT_MSG, messageId, chatMsg);

        this.toBeReflected = toBeReflected;
    }

    /**
     * Returns whether or not this message is marked to be "reflected" back to
     * the client as normal chat text. If this is <code>false</code> the message
     * should only be sent to other members of the chat room.
     *
     * @return whether this message should be "reflected" back to the sender as
     *         normal chat text
     */
    public final boolean isToBeReflected() {
        return toBeReflected;
    }

    protected void writeChatTlvs(OutputStream out) throws IOException {
        if (toBeReflected) new Tlv(TYPE_REFLECT).write(out);
    }
}
