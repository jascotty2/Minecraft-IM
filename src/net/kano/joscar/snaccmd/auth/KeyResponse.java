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

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.flapcmd.SnacPacket;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A command sent in response to {@link KeyRequest} that provides the user with
 * an "authorization key."
 *
 * @snac.src server
 * @snac.cmd 0x17 0x07
 * @see KeyRequest
 */
public class KeyResponse extends AuthCommand {
    /** The authorization key. */
    private final ByteBlock key;

    /**
     * Generates a key response command from the given incoming SNAC packet.
     *
     * @param packet a key response packet
     */
    protected KeyResponse(SnacPacket packet) {
        super(AuthCommand.CMD_KEY_RESP);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock data = packet.getData();

        if (data.getLength() < 2) {
            key = null;
            return;
        }

        int len = BinaryTools.getUShort(data, 0);

        key = data.subBlock(2, len);
    }

    /**
     * Creates an outgoing key response command with the given key.
     *
     * @param key the "authorization key" to send
     */
    public KeyResponse(ByteBlock key) {
        super(AuthCommand.CMD_KEY_RESP);

        this.key = key;
    }

    /**
     * Returns the authorization key provided in this response.
     *
     * @return an "authorization key" for use in {@linkplain AuthRequest
     *         authorizing}
     */
    public final ByteBlock getKey() {
        return key;
    }

    public void writeData(OutputStream out) throws IOException {
        int len = (key == null ? 0 : (int) key.getWritableLength());
        BinaryTools.writeUShort(out, len);
        if (key != null) key.write(out);
    }

    public String toString() {
        return "KeyResponse: key=" + BinaryTools.getAsciiString(key);
    }
}
