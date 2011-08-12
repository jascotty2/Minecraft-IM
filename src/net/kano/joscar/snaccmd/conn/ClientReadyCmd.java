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

package net.kano.joscar.snaccmd.conn;

import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.flapcmd.SnacPacket;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A SNAC command sent to indicate that the client has finished initializing
 * the session and is ready to "go online." This also tells the server more
 * than the previously sent {@link ClientVersionsCmd} about your SNAC family
 * versions.
 *
 * @snac.src client
 * @snac.cmd 0x01 0x02
 */
public class ClientReadyCmd extends ConnCommand {
    /** A list of SNAC family information objects to be sent in this command. */
    private final SnacFamilyInfo[] infos;

    /**
     * Creates a new client ready command from the given incoming SNAC packet.
     *
     * @param packet an incoming client-ready packet
     */
    protected ClientReadyCmd(SnacPacket packet) {
        super(CMD_CLIENT_READY);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

        infos = new SnacFamilyInfo[snacData.getLength()/8];

        for (int i = 0; i < infos.length; i++) {
            infos[i] = SnacFamilyInfo.readSnacFamilyInfo(snacData);

            snacData = snacData.subBlock(8);
        }
    }

    /**
     * Creates a new outgoing client ready command with the given SNAC family
     * information blocks.
     *
     * @param infos the SNAC family information blocks to send with this command
     */
    public ClientReadyCmd(SnacFamilyInfo[] infos) {
        super(CMD_CLIENT_READY);

        this.infos = (SnacFamilyInfo[]) (infos == null ? null : infos.clone());
    }

    /**
     * Returns the SNAC family information blocks sent with this command.
     *
     * @return this command's SNAC family information blocks
     */
    public final SnacFamilyInfo[] getSnacFamilyInfos() {
        return (SnacFamilyInfo[]) (infos == null ? null : infos.clone());
    }

    public void writeData(OutputStream out) throws IOException {
        if (infos != null) {
            for (int i = 0; i < infos.length; i++) {
                infos[i].write(out);
            }
        }
    }
}
