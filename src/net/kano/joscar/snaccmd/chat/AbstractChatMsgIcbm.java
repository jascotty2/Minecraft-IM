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
import net.kano.joscar.snaccmd.AbstractIcbm;
import net.kano.joscar.tlv.ImmutableTlvChain;
import net.kano.joscar.tlv.Tlv;
import net.kano.joscar.tlv.TlvChain;
import net.kano.joscar.tlv.TlvTools;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A base class for chat-message ICBM commands, both incoming and outgoing.
 */
public abstract class AbstractChatMsgIcbm extends AbstractIcbm {
    /** A TLV type sent if this message was sent to the entire chat room. */
    private static final int TYPE_IS_PUBLIC = 0x0001;
    /** A TLV type containing the chat message block. */
    private static final int TYPE_MSGBLOCK = 0x0005;

    /** The chat message block. */
    private final ChatMsg chatMsg;
    /** ICBM-type-specific TLV's. */
    private final TlvChain chatTlvs;

    /**
     * Creates a new chat ICBM with the given SNAC command subtype and with
     * properties read from the given incoming packet.
     *
     * @param command the SNAC command subtype of this command
     * @param packet a chat ICBM packet
     */
    protected AbstractChatMsgIcbm(int command, SnacPacket packet) {
        super(ChatCommand.FAMILY_CHAT, command, packet);

        TlvChain chain = TlvTools.readChain(getChannelData());

        Tlv msgTlv = chain.getLastTlv(TYPE_MSGBLOCK);
        if (msgTlv != null) {
            ByteBlock msgBlock = msgTlv.getData();

            chatMsg = ChatMsg.readChatMsg(msgBlock);
        } else {
            chatMsg = null;
        }

        chatTlvs = chain;
    }

    /**
     * Creates a new outgoing chat ICBM with the given properties.
     *
     * @param command this ICBM's SNAC command subtype
     * @param messageId a (normally unique) ICBM message ID
     * @param chatMsg the message to send to the channel
     */
    protected AbstractChatMsgIcbm(int command, long messageId,
            ChatMsg chatMsg) {
        super(ChatCommand.FAMILY_CHAT, command, messageId, CHANNEL_CHAT);

        this.chatMsg = chatMsg;
        chatTlvs = null;
    }

    /**
     * Returns this ICBM's embedded chat message.
     *
     * @return the chat message in this ICBM
     */
    public final ChatMsg getMessage() {
        return chatMsg;
    }

    /**
     * Returns the extra command-specific TLV's sent in this chat message. Will
     * be <code>null</code> if this message was not read from an incoming
     * stream.
     *
     * @return this ICBM's command-type-specific TLV's
     */
    protected final TlvChain getChatTlvs() {
        return chatTlvs;
    }

    protected void writeChannelData(OutputStream out) throws IOException {
        if (chatMsg != null) {
            ByteBlock msgBlock = ByteBlock.createByteBlock(chatMsg);

            new Tlv(TYPE_MSGBLOCK, msgBlock).write(out);
        }
        writeChatTlvs(out);
    }

    /**
     * Writes the extra command-type-specific TLV's to be sent with this ICBM
     * to the given stream.
     *
     * @param out the stream to which to write
     * @throws IOException if an I/O error occurs
     */
    protected abstract void writeChatTlvs(OutputStream out) throws IOException;
}
