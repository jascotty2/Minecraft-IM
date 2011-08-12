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
 *  File created by keith @ Feb 21, 2003
 *
 */

package net.kano.joscar.snaccmd.conn;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.flapcmd.SnacPacket;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A command sent to indicate that the client is aware of the rate limits
 * provided in a previously received {@link RateInfoCmd}.
 *
 * @snac.src client
 * @snac.cmd 0x01 0x08
 */
public class RateAck extends ConnCommand {
    /** The rate classes acknowledged. */
    private final int[] classes;

    /**
     * Generates a new rate class acknowledgement command from the given
     * incoming SNAC packet.
     *
     * @param packet an incoming rate acknowledgement packet
     */
    protected RateAck(SnacPacket packet) {
        super(CMD_RATE_ACK);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();
        classes = new int[snacData.getLength() / 2];

        for (int i = 0; i < classes.length; i++) {
            classes[i] = BinaryTools.getUShort(snacData, i*2);
        }
    }

    /**
     * Creates a new rate limit acknowledgement packet, acknowledging the given
     * rate class ID numbers.
     *
     * @param classes a list of numbers of rate classes acknowledged by the
     *        client
     */
    public RateAck(int[] classes) {
        super(CMD_RATE_ACK);

        this.classes = (int[]) (classes == null ? null : classes.clone());
    }

    /**
     * Returns the numbers of the rate classes being acknowledged.
     *
     * @return the rate class numbers being acknowledged
     */
    public final int[] getClasses() {
        return (int[]) (classes == null ? null : classes.clone());
    }

    public void writeData(OutputStream out) throws IOException {
        if (classes != null) {
            for (int i = 0; i < classes.length; i++) {
                BinaryTools.writeUShort(out, classes[i]);
            }
        }
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("RateAck for classes: ");
        if (classes != null) {
            for (int i = 0; i < classes.length; i++) {
                buffer.append(classes[i]).append(", ");
            }
        }

        return buffer.toString();
    }
}
