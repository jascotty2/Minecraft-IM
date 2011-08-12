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
 *  File created by keith @ Feb 28, 2003
 *
 */

package net.kano.joscar.snaccmd.invite;

import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.flapcmd.SnacPacket;
import net.kano.joscar.tlv.ImmutableTlvChain;
import net.kano.joscar.tlv.Tlv;
import net.kano.joscar.tlv.TlvChain;
import net.kano.joscar.tlv.TlvTools;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A SNAC command used to request that AOL send an email to someone to invite
 * him or her to join AIM. Normally responded-to with an {@link
 * InviteFriendAck}.
 *
 * @snac.src client
 * @snac.cmd 0x06 0x02
 */
public class InviteFriendCmd extends InviteCommand {
    /** A TLV type containing the email address to invite. */
    private static final int TYPE_EMAIL = 0x0011;
    /** A TLV type containing an invitation message. */
    private static final int TYPE_MSG = 0x0015;

    /** The email address to whom this invitation is addressed. */
    private final String email;
    /** The message to include in the invitation. */
    private final String message;

    /**
     * Generates a new invite-a-friend command from the given incoming SNAC
     * packet.
     *
     * @param packet an incoming invite-a-friend packet
     */
    protected InviteFriendCmd(SnacPacket packet) {
        super(CMD_INVITE_FRIEND);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock block = packet.getData();

        TlvChain chain = TlvTools.readChain(block);

        email = chain.getString(TYPE_EMAIL);
        message = chain.getString(TYPE_MSG);
    }

    /**
     * Creates a new outgoing invite-a-friend request to the given email address
     * and with the given invitation message.
     *
     * @param email the email address of the person to whom the invitation
     *        should be sent
     * @param message a message, like "HAY JOIN AIMZ D00D"
     */
    public InviteFriendCmd(String email, String message) {
        super(CMD_INVITE_FRIEND);

        this.email = email;
        this.message = message;
    }

    /**
     * Returns the email address to whom an invitation should be sent.
     *
     * @return the email address to whom an invitatoin should be sent
     */
    public final String getEmail() {
        return email;
    }

    /**
     * Returns an "invitation message" to include in the invitation.
     *
     * @return the invitation message to send
     */
    public final String getMessage() {
        return message;
    }

    public void writeData(OutputStream out) throws IOException {
        if (email != null) {
            Tlv.getStringInstance(TYPE_EMAIL, email).write(out);
        }
        if (message != null) {
            Tlv.getStringInstance(TYPE_MSG, message).write(out);
        }
    }

    public String toString() {
        return "InviteFriendCmd for " + email + ", message: " + message;
    }
}
