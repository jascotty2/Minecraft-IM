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

package net.kano.joscar.snaccmd.loc;

import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.MinimalEncoder;
import net.kano.joscar.OscarTools;
import net.kano.joscar.flapcmd.SnacPacket;
import net.kano.joscar.tlv.ImmutableTlvChain;
import net.kano.joscar.tlv.Tlv;
import net.kano.joscar.tlv.TlvChain;
import net.kano.joscar.tlv.TlvTools;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A SNAC command used to store a list of "chat interests." Normally
 * responded-to with a {@link SetInterestsAck}.
 *
 * @snac.src client
 * @snac.cmd 0x02 0x0f
 *
 * @see SetInterestsAck
 */
public class SetInterestsCmd extends LocCommand {
    /** A TLV type containing a charset. */
    private static final int TYPE_CHARSET = 0x001c;
    /** A TLV type containing an interest. */
    private static final int TYPE_INTEREST = 0x000b;

    /** The interests being set. */
    private final String[] interests;

    /**
     * Generates a new set-chat-interests command from the given incoming SNAC
     * command.
     *
     * @param packet an incoming set-chat-interests packet
     */
    protected SetInterestsCmd(SnacPacket packet) {
        super(CMD_SET_INTERESTS);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

        TlvChain chain = TlvTools.readChain(snacData);

        String charset = chain.getString(TYPE_CHARSET);

        Tlv[] interestTlvs = chain.getTlvs(TYPE_INTEREST);

        List interestList = new ArrayList();

        for (int i = 0; i < interestTlvs.length; i++) {
            ByteBlock interestBytes = interestTlvs[i].getData();
            String interest = OscarTools.getString(interestBytes, charset);
            interestList.add(interest);
        }

        interests = (String[]) interestList.toArray(new String[0]);
    }

    /**
     * Creates a new set-chat-interests command setting the given list of
     * interests.
     *
     * @param interests the list of chat interests to set
     */
    public SetInterestsCmd(String[] interests) {
        super(CMD_SET_INTERESTS);

        this.interests = (String[]) (interests == null
                ? null
                : interests.clone());
    }

    /**
     * Returns the chat interests being set. Note that this will always be
     * non-<code>null</code> if read from an incoming set-interests command,
     * even if no interests are being set. In that case, the returned array will
     * simply have a length of <code>0</code>.
     *
     * @return the chat interests being set
     */
    public final String[] getInterests() {
        return (String[]) (interests == null ? null : interests.clone());
    }

    public void writeData(OutputStream out) throws IOException {
        if (interests != null && interests.length > 0) {
            MinimalEncoder enc = new MinimalEncoder();
            enc.updateAll(interests);

            Tlv.getStringInstance(TYPE_CHARSET, enc.getCharset()).write(out);
            for (int i = 0; i < interests.length; i++) {
                byte[] interestBytes = enc.encode(interests[i]).getData();
                new Tlv(TYPE_INTEREST, ByteBlock.wrap(interestBytes)).write(out);
            }
        }
    }

    public String toString() {
        return "SetInterestsCmd: interests=" + Arrays.asList(interests);
    }
}
