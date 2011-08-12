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
 *  File created by keith @ Feb 23, 2003
 *
 */

package net.kano.joscar.snaccmd.loc;

import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.OscarTools;
import net.kano.joscar.StringBlock;
import net.kano.joscar.flapcmd.SnacPacket;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A SNAC command used to request another user's directory information. Normally
 * responded-to with a {@link DirInfoCmd}.
 *
 * @snac.src client
 * @snac.cmd 0x02 0x0b
 *
 * @see DirInfoCmd
 */
public class GetDirInfoCmd extends LocCommand {
    /**
     * The screenname of the user whose directory information is being
     * requested.
     */
    private final String sn;

    /**
     * Generates a new directory information request from the given incoming
     * SNAC command.
     *
     * @param packet an incoming directory information request packet
     */
    protected GetDirInfoCmd(SnacPacket packet) {
        super(CMD_GET_DIR);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

        StringBlock snData = OscarTools.readScreenname(snacData);
        sn = snData == null ? null : snData.getString();
    }

    /**
     * Creates a new directory information request for the given user.
     *
     * @param sn the screenname of the user whose directory information is being
     *        requested
     */
    public GetDirInfoCmd(String sn) {
        super(CMD_GET_DIR);

        this.sn = sn;
    }

    /**
     * Returns the screenname of the user whose directory information is being
     * requested.
     *
     * @return the screenname of the user whose directory information is being
     *         requested
     */
    public final String getScreenname() {
        return sn;
    }

    public void writeData(OutputStream out) throws IOException {
        if (sn != null) OscarTools.writeScreenname(out, sn);
    }

    public String toString() {
        return "GetDirInfoCmd for " + sn;
    }
}
