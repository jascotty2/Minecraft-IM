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
import net.kano.joscar.MiscTools;
import net.kano.joscar.flapcmd.SnacPacket;
import net.kano.joscar.snaccmd.ExtraInfoBlock;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * A base class for the two SNAC commands which contain only a sequence of
 * {@link ExtraInfoBlock}s.
 */
public abstract class AbstractExtraInfoCmd extends ConnCommand {
    /** The set of extra information blocks contained in this command. */
    private final ExtraInfoBlock[] extraInfos;

    /**
     * Creates a new extra info block command with the given SNAC command
     * subtype and using the data in the given incoming extra info command
     * packet.
     *
     * @param command the SNAC command subtype for this command
     * @param packet an incoming extra info command packet
     */
    protected AbstractExtraInfoCmd(int command, SnacPacket packet) {
        super(command);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

        extraInfos = ExtraInfoBlock.readExtraInfoBlocks(snacData);
    }

    /**
     * Creates a new outgoing extra info blocks command with the given list of
     * extra info blocks. Note that neither <code>blocks</code> nor any of its
     * elements can be <code>null</code>.
     *
     * @param command the SNAC command subtype for this command
     * @param blocks the list of extra info blocks to send in this command
     */
    protected AbstractExtraInfoCmd(int command, ExtraInfoBlock[] blocks) {
        super(command);

        DefensiveTools.checkNull(blocks, "blocks");

        this.extraInfos = (ExtraInfoBlock[])
                DefensiveTools.getNonnullArray(blocks, "blocks");
    }

    /**
     * Returns the list of extra information blocks sent in this command.
     *
     * @return this command's extra information blocks
     */
    public final ExtraInfoBlock[] getExtraInfos() {
        return (ExtraInfoBlock[]) (extraInfos == null ? null
                : extraInfos.clone());
    }

    public void writeData(OutputStream out) throws IOException {
        if (extraInfos != null) {
            ByteBlock.createByteBlock(extraInfos).write(out);
        }
    }

    public String toString() {
        return MiscTools.getClassName(this) + ": blocks=" +
                (extraInfos == null ? null : Arrays.asList(extraInfos));
    }
}
