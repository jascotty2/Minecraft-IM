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
 *  File created by keith @ Feb 19, 2003
 *
 */

package net.kano.joscar.flapcmd;

import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.flap.FlapCommand;
import net.kano.joscar.flap.FlapPacket;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A FLAP command that contains a SNAC packet as its FLAP data. This is how
 * the majority of communication between a normal OSCAR client and server takes
 * place. A SNAC packet is not really a "packet" but more appropriately a
 * data structure inside of a channel-2 FLAP packet. Unfortunately for you,
 * however, I've chosen to continue using the name "SNAC packet."
 */
public class SnacFlapCmd extends FlapCommand {
    /**
     * The channel on which SNAC commands reside.
     */
    public static final int CHANNEL_SNAC = 0x0002;

    /**
     * The packet from which this <code>FlapCommand</code> was generated.
     */
    private final SnacPacket packet;

    /**
     * Generates a <code>SnacFlapCmd</code> from the given packet.
     *
     * @param flapPacket the packet from which this command is to be generated
     */
    public SnacFlapCmd(FlapPacket flapPacket) {
        super(CHANNEL_SNAC);

        DefensiveTools.checkNull(flapPacket, "flapPacket");

        ByteBlock flapData = flapPacket.getData();

        packet = SnacPacket.isValidSnacPacket(flapData)
                ? SnacPacket.readSnacPacket(flapData) : null;
    }

    /**
     * Creates a new <code>SnacFlapCmd</code> with the given request ID that
     * uses the given <code>SnacCommand</code> to generate a SNAC packet to
     * write (embedded in a FLAP packet, of course) to a FLAP connection.
     *
     * @param requestId the request ID to use in this command's SNAC packet
     * @param command the command to use to generate a SNAC packet upon writing
     *        to a FLAP connection
     */
    public SnacFlapCmd(long requestId, SnacCommand command) {
        super(CHANNEL_SNAC);

        DefensiveTools.checkRange(requestId, "requestId", 0);

        packet = new SnacPacket(requestId, command);
    }

    /**
     * Returns the SNAC packet embedded in this command, if any. (This may be
     * <code>null</code> if an invalid SNAC packet was sent in a channel-2
     * FLAP.)
     *
     * @return this command's SNAC packet
     */
    public final SnacPacket getSnacPacket() {
        return packet;
    }

    public void writeData(OutputStream out) throws IOException {
        packet.write(out);
    }

    public String toString() {
        return "SnacFlapCmd: packet=" + packet;
    }
}
