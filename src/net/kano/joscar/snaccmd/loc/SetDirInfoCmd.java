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
 *  File created by keith @ Feb 28, 2003
 *
 */

package net.kano.joscar.snaccmd.loc;

import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.flapcmd.SnacPacket;
import net.kano.joscar.snaccmd.DirInfo;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A SNAC command used to set one's directory information. Normally responded-to
 * with a {@link SetDirAck}.
 *
 * @snac.src client
 * @snac.cmd 0x02 0x09
 *
 * @see SetDirAck
 */
public class SetDirInfoCmd extends LocCommand {
    /** The directory information being set. */
    private final DirInfo dirInfo;

    /**
     * Generates a set-directory-info command from the given incoming SNAC
     * packet.
     *
     * @param packet an incoming set-directory-information packet
     */
    protected SetDirInfoCmd(SnacPacket packet) {
        super(CMD_SET_DIR);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

        dirInfo = DirInfo.readDirInfo(snacData);
    }

    /**
     * Creates a new outgoing set-directory-information command with the given
     * directory information, or <code>null</code> to clear directory info.
     *
     * @param dirInfo the directory information to set, or <code>null</code> to
     *        clear the directory information currently set
     */
    public SetDirInfoCmd(DirInfo dirInfo) {
        super(CMD_SET_DIR);

        this.dirInfo = dirInfo;
    }

    /**
     * Returns the directory information to be set, or <code>null</code> to
     * clear the currently set directory information.
     *
     * @return the directory information being set
     */
    public final DirInfo getDirInfo() {
        return dirInfo;
    }

    public void writeData(OutputStream out) throws IOException {
        if (dirInfo != null) dirInfo.write(out);
    }

    public String toString() {
        return "SetDirInfoCmd: " + dirInfo;
    }
}
