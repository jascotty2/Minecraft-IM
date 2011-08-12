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
import net.kano.joscar.snaccmd.FullUserInfo;
import net.kano.joscar.snaccmd.InfoData;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A SNAC command containing a certain type of information about another user.
 * Normally sent in response to a {@link GetInfoCmd} and an {@link
 * OldGetInfoCmd}.
 *
 * @snac.src server
 * @snac.cmd 0x02 0x06
 *
 * @see GetInfoCmd
 */
public class UserInfoCmd extends LocCommand {
    /**
     * A user information block for the user whose information was sent in this
     * command.
     */
    private final FullUserInfo userInfo;
    /** The info data received. */
    private final InfoData infoData;

    /**
     * Generates a new user information command from the given incoming SNAC
     * packet.
     *
     * @param packet an incoming user information packet
     */
    protected UserInfoCmd(SnacPacket packet) {
        super(CMD_USER_INFO);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

        userInfo = FullUserInfo.readUserInfo(snacData);

        ByteBlock infoBlock = snacData.subBlock(userInfo.getTotalSize());

        infoData = InfoData.readInfoData(infoBlock);
    }

    /**
     * Creates a new outgoing user information command with the given user
     * information block and, well, user information block.
     *
     * @param userInfo a set of information about the user whose info is being
     *        returned
     * @param infoData a block of "info data"
     */
    public UserInfoCmd(FullUserInfo userInfo, InfoData infoData) {
        super(CMD_USER_INFO);

        this.userInfo = userInfo;
        this.infoData = infoData;
    }

    /**
     * Returns a user information block for the user whose information is
     * contained in this command.
     *
     * @return a user information block for the user whose information was sent
     *         in this command
     */
    public final FullUserInfo getUserInfo() {
        return userInfo;
    }

    /**
     * Returns the block of "info data" returned in this command.
     *
     * @return returns the block of "info data" sent in this command
     */
    public final InfoData getInfoData() {
        return infoData;
    }

    public void writeData(OutputStream out) throws IOException {
        if (userInfo != null) {
            userInfo.write(out);
            
            if (infoData != null) infoData.write(out);
        }
    }

    public String toString() {
        return "UserInfoCmd: user=<" + userInfo + ">, info=<" + infoData + ">";
    }
}
