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

package net.kano.joscar.snaccmd.loc;

import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.flapcmd.SnacPacket;
import net.kano.joscar.snaccmd.InfoData;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A SNAC command used to set one's "user info" fields, such as away message
 * and "info."
 */
public class SetInfoCmd extends LocCommand {
    /** The block of "info data" to set. */
    private final InfoData infoData;

    /**
     * Generates a new set-info command from the given incoming SNAC packet.
     *
     * @param packet an incoming set-info packet
     */
    protected SetInfoCmd(SnacPacket packet) {
        super(CMD_SET_INFO);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

        infoData = InfoData.readInfoData(snacData);
    }

    /**
     * Creates a new set-info command with the given info data block.
     *
     * @param infoData a block of "info data" to set
     */
    public SetInfoCmd(InfoData infoData) {
        super(CMD_SET_INFO);

        this.infoData = infoData;
    }

    /**
     * Returns the "info data" block being set.
     *
     * @return this command's "info data" that is being set
     */
    public final InfoData getInfoData() {
        return infoData;
    }

    public void writeData(OutputStream out) throws IOException {
        if (infoData != null) infoData.write(out);
    }

    public String toString() {
        return "SetInfoCmd: info=" + infoData;
    }
}
