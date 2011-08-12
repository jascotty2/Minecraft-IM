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
 *  File created by keith @ Feb 22, 2003
 *
 */

package net.kano.joscar.snaccmd.loc;

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
 * A SNAC command containing location-related "rights." Currently the only
 * supported value is the maximum length of a user's "info."
 *
 * @snac.src server
 * @snac.cmd 0x02 0x03
 */
public class LocRightsCmd extends LocCommand {
    /** A TLV type containing the maximum info length. */
    private static final int TYPE_MAX_INFO_LEN = 0x0001;

    /** The maximum length of a user's "info." */
    private final int maxInfoLength;

    /**
     * Generates a new location rights command from the given incoming SNAC
     * packet.
     *
     * @param packet an incoming location rights packet
     */
    protected LocRightsCmd(SnacPacket packet) {
        super(CMD_RIGHTS_RESP);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

        TlvChain chain = TlvTools.readChain(snacData);

        maxInfoLength = chain.getUShort(TYPE_MAX_INFO_LEN);
    }

    /**
     * Creates a new location rights command with the given maximum info length.
     *
     * @param maxInfoLength the maximum length of one's "info"
     */
    public LocRightsCmd(int maxInfoLength) {
        super(CMD_RIGHTS_RESP);

        DefensiveTools.checkRange(maxInfoLength, "maxInfoLength", 0);

        this.maxInfoLength = maxInfoLength;
    }

    /**
     * Returns the maximum length, in bytes, of one's {@linkplain SetInfoCmd
     * user info}.
     *
     * @return the maximum "info" length
     */
    public final int getMaxInfoLength() {
        return maxInfoLength;
    }

    public void writeData(OutputStream out) throws IOException {
        Tlv.getUShortInstance(TYPE_MAX_INFO_LEN, maxInfoLength).write(out);
    }

    public String toString() {
        return "LocRightsCmd: max info length=" + maxInfoLength;
    }
}
