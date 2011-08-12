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
import net.kano.joscar.snac.CmdType;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A SNAC command sent to inform the client of a set of rate-limiting
 * information. It is currently mostly unknown how all this works; see {@link
 * RateClassInfo} for the best explanation so far. Normally sent in response to
 * a {@link RateInfoRequest}.
 *
 * @snac.src server
 * @snac.cmd 0x01 0x07
 *
 * @see RateInfoRequest
 * @see RateAck
 */
public class RateInfoCmd extends ConnCommand {
    /** The list of rate class information blocks sent in this command. */
    private final RateClassInfo[] infos;

    /**
     * Generates a rate information command from the given incoming SNAC
     * command.
     *
     * @param packet an incoming rate information packet
     */
    protected RateInfoCmd(SnacPacket packet) {
        super(CMD_RATE_INFO);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

//        System.out.println("len: " + snacData.getLength());

        int rates = BinaryTools.getUShort(snacData, 0);
        RateClassInfo[] infos = new RateClassInfo[rates];

        ByteBlock block = snacData.subBlock(2);
        for (int i = 0; i < rates; i++) {
            infos[i] = RateClassInfo.readRateClassInfo(block);
            block = block.subBlock((int) infos[i].getWritableLength());
        }

        for (int i = 0; i < rates; i++) {
            int rclass = BinaryTools.getUShort(block, 0);
            int familyCount = BinaryTools.getUShort(block, 2);
            CmdType[] cmds = new CmdType[familyCount];

            if (rclass != infos[i].getRateClass()) {
                // the server sent the classes in the wrong order....
                continue;
            }

            for (int f = 0; f < familyCount; f++) {
                int family = BinaryTools.getUShort(block, 4+f*4);
                int command = BinaryTools.getUShort(block, 4+f*4+2);
                cmds[f] = new CmdType(family, command);
            }
            infos[i].setCommands(cmds);
            block = block.subBlock(4 + familyCount * 4);
        }

        this.infos = infos;
    }

    /**
     * Creates a new outgoing rate information command with the given rate
     * class information blocks.
     *
     * @param infos a set of rate information blocks to send in this command
     */
    public RateInfoCmd(RateClassInfo[] infos) {
        super(CMD_RATE_INFO);

        this.infos = (RateClassInfo[]) (infos == null ? null : infos.clone());
    }

    /**
     * Returns the rate class information blocks sent in this command.
     *
     * @return this command's enclosed rate class information blocks
     */
    public RateClassInfo[] getRateClassInfos() {
        return (RateClassInfo[]) (infos == null ? null : infos.clone());
    }

    public void writeData(OutputStream out) throws IOException {
        int len = infos == null ? 0 : infos.length;
        BinaryTools.writeUShort(out, len);
        if (infos != null) {
            for (int i = 0; i < infos.length; i++) {
                infos[i].write(out);
            }
            for (int i = 0; i < infos.length; i++) {
                CmdType[] families = infos[i].getCommands();
                for (int j = 0; j < families.length; j++) {
                    BinaryTools.writeUShort(out, families[j].getFamily());
                    BinaryTools.writeUShort(out, families[j].getCommand());
                }
            }
        }
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("RateInfoCmd: ");
        for (int i = 0; i < infos.length; i++) {
            buffer.append(infos[i]);
            buffer.append(" - ");
        }

        return buffer.toString();
    }
}
