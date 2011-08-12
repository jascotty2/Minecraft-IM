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

import net.kano.joscar.flapcmd.SnacPacket;

/**
 * A SNAC command sent to inform the server of versions of SNAC families
 * supported by the client.
 *
 * @snac.src client
 * @snac.cmd 0x01 0x17
 */
public class ClientVersionsCmd extends FamilyVersionsCmd {
    /**
     * Generates a client version command from the given incoming SNAC packet.
     *
     * @param packet an incoming client version SNAC packet
     */
    protected ClientVersionsCmd(SnacPacket packet) {
        super(CMD_CLIENT_VERS, packet);
    }

    /**
     * Creates a new outgoing client version list command with the given SNC
     * family information blocks. Note that only the {@linkplain
     * SnacFamilyInfo#getFamily family} and {@linkplain
     * SnacFamilyInfo#getVersion family version} fields will be sent.
     *
     * @param families a block of SNAC family information to send in this
     *        command
     */
    public ClientVersionsCmd(SnacFamilyInfo[] families) {
        super(CMD_CLIENT_VERS, families);
    }
}
