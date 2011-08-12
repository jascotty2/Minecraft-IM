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

package net.kano.joscar.snaccmd.buddy;

import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.OscarTools;
import net.kano.joscar.StringBlock;
import net.kano.joscar.flapcmd.SnacPacket;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A command sent when a buddy signs off.
 *
 * @snac.src server
 * @snac.cmd 0x03 0x0c
 */
public class BuddyOfflineCmd extends BuddyCommand {
    /** The screenname of the buddy who signed off. */
    private final String sn;

    /**
     * Generates a buddy signoff command from the given incoming SNAC packet.
     *
     * @param packet a buddy signoff packet
     */
    protected BuddyOfflineCmd(SnacPacket packet) {
        super(CMD_BUDDY_OFFLINE);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

        StringBlock snInfo = OscarTools.readScreenname(snacData);

        sn = snInfo == null ? null : snInfo.getString();
    }

    /**
     * Creates a new outgoing buddy signoff command for the given screenname.
     *
     * @param sn the screenname of the buddy who signed off
     */
    public BuddyOfflineCmd(String sn) {
        super(CMD_BUDDY_OFFLINE);

        DefensiveTools.checkNull(sn, "sn");

        this.sn = sn;
    }

    /**
     * Returns the screenname of the buddy who signed off.
     *
     * @return the screenname of the buddy who signed off
     */
    public final String getScreenname() {
        return sn;
    }

    public void writeData(OutputStream out) throws IOException {
        OscarTools.writeScreenname(out, sn);
    }

    public String toString() {
        return "BuddyOfflineCmd for " + sn;
    }
}
