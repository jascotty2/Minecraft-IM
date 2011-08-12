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
 *  File created by keith @ Mar 3, 2003
 *
 */

package net.kano.joscar.snaccmd.ssi;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.flapcmd.SnacPacket;
import net.kano.joscar.tlv.ImmutableTlvChain;
import net.kano.joscar.tlv.Tlv;
import net.kano.joscar.tlv.TlvChain;
import net.kano.joscar.tlv.TlvTools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A SNAC command containing a set of "rights" associated with server-stored
 * information. Normally sent in response to a {@link SsiRightsRequest}.
 *
 * @snac.src server
 * @snac.cmd 0x13 0x03
 *
 * @see SsiRightsRequest
 */
public class SsiRightsCmd extends SsiCommand {
    /** A TLV type containing the maximum numbers of each SSI item type. */
    private static final int TYPE_MAXIMA = 0x0004;

    /**
     * The maximum numbers of each SSI item type, where the index of the array
     * is the SSI item type.
     */
    private final int[] maxima;

    /**
     * Generates a new SSI rights command from the given incoming SNAC packet.
     *
     * @param packet an incoming SSI rights packet
     */
    protected SsiRightsCmd(SnacPacket packet) {
        super(CMD_RIGHTS);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

        TlvChain chain = TlvTools.readChain(snacData);

        Tlv maximaTlv = chain.getLastTlv(TYPE_MAXIMA);

        if (maximaTlv != null) {
            ByteBlock block = maximaTlv.getData();

            maxima = new int[block.getLength() / 2];

            for (int i = 0; i < maxima.length; i++) {
                maxima[i] = BinaryTools.getUShort(block, i*2);
            }
        } else {
            maxima = null;
        }
    }

    /**
     * Creates a new outgoing SSI rights command with the given list of maxima.
     * See {@link #getMaxima() getMaxima} for details on the format of the given
     * array. Note that <code>maxima</code> can be <code>null</code>.
     *
     * @param maxima a list of maximum numbers of SSI item types
     */
    public SsiRightsCmd(int[] maxima) {
        super(CMD_RIGHTS);

        this.maxima = (int[]) (maxima == null ? null : maxima.clone());
    }

    /**
     * Returns a list of maximum numbers of each SSI item type. The format of
     * this array is such that the maximum number of items of type
     * <code><i>i</i></code> is <code>ssiRightsCmd.getMaxima()[<i>i</i>]</code>.
     * Thus, the maximum number of buddies allowed on one's buddy list, where
     * buddy items are type {@link SsiItem#TYPE_BUDDY} (<code>0x00</code>), is
     * <code>ssiRightsCmd.getMaxima()[SsiItem.TYPE_BUDDY]</code>. You may notice
     * that AOL's AIM servers will send a list of many more than the eight types
     * supported by joscar. Some of those types are used for ICQ; others are
     * undocumented and, as far as I have seen, never used by WinAIM.
     *
     * @return a list of the maximum numbers of items of each item type
     */
    public final int[] getMaxima() {
        return (int[]) (maxima == null ? null : maxima.clone());
    }

    public void writeData(OutputStream out) throws IOException {
        if (maxima != null) {
            ByteArrayOutputStream maximout = new ByteArrayOutputStream();

            try {
                for (int i = 0; i < maxima.length; i++) {
                    BinaryTools.writeUShort(maximout, maxima[i]);
                }
            } catch (IOException impossible) { }

            ByteBlock maximaBlock = ByteBlock.wrap(maximout.toByteArray());

            new Tlv(TYPE_MAXIMA, maximaBlock).write(out);
        }
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer(maxima.length * 12);

        if (maxima != null) {
            for (int i = 0; i < maxima.length; i++) {
                buffer.append("0x");
                buffer.append(Integer.toHexString(i));
                buffer.append(": ");
                buffer.append(maxima[i]);
                buffer.append(", ");
            }
        }
        return "SsiRightsCmd: " + maxima.length + " maxima: " + buffer;
    }
}
