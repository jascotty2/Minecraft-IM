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
 *  File created by keith @ Mar 28, 2003
 *
 */

package net.kano.joscar.snaccmd.icbm;

import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.flapcmd.SnacPacket;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * A SNAC command sent to indicate to a client that one or more messages were
 * "missed." Messages can be missed for various reasons; see {@link
 * MissedMsgInfo} for more information.
 *
 * @snac.src server
 * @snac.cmd 0x04 0x0a
 */
public class MissedMessagesCmd extends IcbmCommand {
    /** The list of missed message blocks. */
    private final MissedMsgInfo[] missedMsgInfos;

    /**
     * Generates a new missed-messages command from the given incoming SNAC
     * packet.
     *
     * @param packet an incoming missed-messages packet
     */
    protected MissedMessagesCmd(SnacPacket packet) {
        super(CMD_MISSED);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock block = packet.getData();

        List messages = new LinkedList();

        for (;;) {
            MissedMsgInfo info = MissedMsgInfo.readMissedMsgInfo(block);
            if (info == null) break;

            messages.add(info);
            block = block.subBlock(info.getTotalSize());
        }

        missedMsgInfos = (MissedMsgInfo[])
                messages.toArray(new MissedMsgInfo[0]);
    }

    /**
     * Creates a new outgoing missed-messages command with the given list of
     * missed message information blocks.
     *
     * @param missedMsgInfos a list of objects describing the missed messages
     */
    public MissedMessagesCmd(MissedMsgInfo[] missedMsgInfos) {
        super(CMD_MISSED);

        this.missedMsgInfos = (MissedMsgInfo[]) (missedMsgInfos == null
                ? null
                : missedMsgInfos.clone());
    }

    /**
     * Returns the list of missed message information blocks sent in this
     * command.
     *
     * @return the list of missed message blocks
     */
    public final MissedMsgInfo[] getMissedMsgInfos() {
        return (MissedMsgInfo[]) missedMsgInfos.clone();
    }

    public void writeData(OutputStream out) throws IOException {
        if (missedMsgInfos != null) {
            for (int i = 0; i < missedMsgInfos.length; i++) {
                missedMsgInfos[i].write(out);
            }
        }
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("MissedMessagesCmd: ");
        if (missedMsgInfos != null) {
            buffer.append(missedMsgInfos.length);
            buffer.append(" missed: ");
            for (int i = 0; i < missedMsgInfos.length; i++) {
                if (i != 0) buffer.append(", ");
                buffer.append(missedMsgInfos[i]);
            }
        }
        return buffer.toString();
    }
}
