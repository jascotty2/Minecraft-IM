/*
 *  Copyright (c) 2003, The Joust Project
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
 *  File created by keith @ Aug 5, 2003
 *
 */

package net.kano.joscar.snaccmd.conn;

import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.flapcmd.SnacPacket;
import net.kano.joscar.snaccmd.ExtraInfoBlockHolder;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * A SNAC command sent in response to a {@link SetEncryptionInfoCmd} to
 * acknowledge that the client has set its encryption information. As of this
 * writing, the significance of this command and its contents is unknown; one
 * may wish to note, however, that normally each {@linkplain
 * #getExtraInfoBlocks() extra info block holder} sent in this command contains
 * two copies (as the {@linkplain ExtraInfoBlockHolder#getFirstBlock() first}
 * <i>and</i> {@linkplain ExtraInfoBlockHolder#getSecondBlock() second} blocks)
 * of the <code>ExtraInfoBlock</code>s sent in the
 * <code>SetEncryptionInfoCmd</code>.
 *
 * @snac.src server
 * @snac.cmd 0x01 0x23
 *
 * @see SetEncryptionInfoCmd
 */
public class EncryptionInfoAck extends ConnCommand {
    /** The extra info blocks sent in this command. */
    private final ExtraInfoBlockHolder[] blocks;

    /**
     * Creates a new encryption information acknowledgement command from the
     * given incoming encryption info ack SNAC packet.
     *
     * @param packet an incoming encryption info acknowledgement SNAC packet
     */
    protected EncryptionInfoAck(SnacPacket packet) {
        super(CMD_ENCINFOACK);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

        this.blocks = ExtraInfoBlockHolder.readBlockHolders(snacData);
    }

    /**
     * Creates a new outgoing encryption information acknowledgement packet with
     * the given list of extra info block holder objects. Note that neither
     * <code>blocks</code> nor any of the elements in <code>blocks</code> can be
     * <code>null</code>.
     *
     * @param blocks a list of extra info block holder objects
     */
    public EncryptionInfoAck(ExtraInfoBlockHolder[] blocks) {
        super(CMD_ENCINFOACK);

        DefensiveTools.checkNull(blocks, "blocks");

        this.blocks = (ExtraInfoBlockHolder[])
                DefensiveTools.getNonnullArray(blocks, "blocks");
    }

    /**
     * Returns the extra info block holders sent in this command. Note that this
     * method will never return <code>null</code>; if no extra info block
     * holders were sent in this command, an empty array will be returned;
     *
     * @return the extra info block holders sent in this command
     */
    public final ExtraInfoBlockHolder[] getExtraInfoBlocks() {
        return (ExtraInfoBlockHolder[]) blocks.clone();
    }

    public void writeData(OutputStream out) throws IOException {
        for (int i = 0; i < blocks.length; i++) {
            blocks[i].write(out);
        }
    }

    public String toString() {
        return "EncryptionInfoAck: blocks=" + Arrays.asList(blocks);
    }
}
