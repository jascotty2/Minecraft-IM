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

import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.flapcmd.SnacPacket;
import net.kano.joscar.snaccmd.FullUserInfo;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A SNAC command sent indicating to the client his or her own user information.
 * Normally sent in response to a {@link MyInfoRequest}.
 *
 * @snac.src server
 * @snac.cmd 0x01 0x0f
 *
 * @see MyInfoRequest
 */
public class YourInfoCmd extends ConnCommand {
    /** The user info block sent in this command. */
    private final FullUserInfo userInfo;

    /**
     * Generates a new self-information command from the given incoming SNAC
     * packet.
     *
     * @param packet an incoming self-information packet
     */
    protected YourInfoCmd(SnacPacket packet) {
        super(CMD_YOUR_INFO);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

        userInfo = FullUserInfo.readUserInfo(snacData);
    }

    /**
     * Creates a new outgoing self-info command with the given user information
     * block.
     *
     * @param info the user information block for the client
     */
    public YourInfoCmd(FullUserInfo info) {
        super(CMD_YOUR_INFO);

        DefensiveTools.checkNull(info, "info");

        this.userInfo = info;
    }

    /**
     * Returns the user information block contained in this command. This will
     * normally be a block containing the client's warning level, user flags,
     * and so on.
     *
     * @return the client's user information
     */
    public final FullUserInfo getUserInfo() {
        return userInfo;
    }

    public void writeData(OutputStream out) throws IOException {
        userInfo.write(out);
    }

    public String toString() {
        return "YourInfoCmd: " + userInfo;
    }
}
