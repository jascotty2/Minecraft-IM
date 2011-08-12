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
import net.kano.joscar.flapcmd.SnacPacket;
import net.kano.joscar.snaccmd.FullUserInfo;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A command sent when a buddy signs on or when some property of the buddy (such
 * as idle time) has changed.
 *
 * @snac.src server
 * @snac.cmd 0x03 0x0b
 */
public class BuddyStatusCmd extends BuddyCommand {
    /** The user information block for the buddy being updated. */
    private FullUserInfo userInfo;

    /**
     * Generates a new buddy status update command from the given incoming SNAC
     * packet.
     *
     * @param packet a buddy status update packet
     */
    protected BuddyStatusCmd(SnacPacket packet) {
        super(CMD_BUDDY_STATUS);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

        userInfo = FullUserInfo.readUserInfo(snacData);
    }

    /**
     * Creates a new outgoing buddy status update command with the given user
     * information block.
     *
     * @param userInfo the user information block for the user being updated
     */
    public BuddyStatusCmd(FullUserInfo userInfo) {
        super(CMD_BUDDY_STATUS);

        DefensiveTools.checkNull(userInfo, "userInfo");

        this.userInfo = userInfo;
    }

    /**
     * Returns the user info block that was updated.
     *
     * @return the updated user info block
     */
    public final FullUserInfo getUserInfo() {
        return userInfo;
    }

    public void writeData(OutputStream out) throws IOException {
        userInfo.write(out);
    }

    public String toString() {
        return "BuddyStatusCmd: userinfo=" + userInfo;
    }
}
