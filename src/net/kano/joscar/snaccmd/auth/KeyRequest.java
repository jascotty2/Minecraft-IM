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
 *  File created by keith @ Feb 18, 2003
 *
 */

package net.kano.joscar.snaccmd.auth;

import net.kano.joscar.DefensiveTools;
import net.kano.joscar.flapcmd.SnacPacket;
import net.kano.joscar.tlv.ImmutableTlvChain;
import net.kano.joscar.tlv.Tlv;
import net.kano.joscar.tlv.TlvChain;
import net.kano.joscar.tlv.TlvTools;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A SNAC command used to request an "authorization key" which can then be used
 * for logging in. This is the first step in the authorization process.
 *
 * @snac.src client
 * @snac.cmd 0x17 0x06
 * @see KeyResponse
 */
public class KeyRequest extends AuthCommand {
    /** A TLV type containing the user's screenname. */
    private static final int TYPE_SN = 0x0001;

    /** The user's screenname. */
    private final String sn;

    /**
     * Generates a new key request command from the given incoming SNAC packet.
     *
     * @param packet a key request packet
     */
    protected KeyRequest(SnacPacket packet) {
        super(CMD_KEY_REQ);

        DefensiveTools.checkNull(packet, "packet");

        TlvChain chain = TlvTools.readChain(packet.getData());

        sn = chain.getString(TYPE_SN);
    }

    /**
     * Creates a new outgoing key request command with the given screenname.
     *
     * @param sn the screenname requesting an authorization key
     */
    public KeyRequest(String sn) {
        super(CMD_KEY_REQ);

        this.sn = sn;
    }

    /**
     * Returns the screenname sent in this key request.
     *
     * @return the user's screenname
     */
    public final String getScreenname() {
        return sn;
    }

    public void writeData(OutputStream out) throws IOException {
        if (sn != null) Tlv.getStringInstance(TYPE_SN, sn).write(out);

        // winaim sends these.
        new Tlv(0x004b).write(out);
        new Tlv(0x005a).write(out);
    }

    public String toString() {
        return "KeyRequest for screenname " + sn;
    }
}
