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

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.OscarTools;
import net.kano.joscar.StringBlock;
import net.kano.joscar.flapcmd.SnacPacket;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A SNAC command used by older clients to request a specific type of
 * information about another user. Normally responded-to with a {@link
 * UserInfoCmd}. Newer clients use the {@linkplain GetInfoCmd new Get Info
 * command}.
 *
 * @snac.src client
 * @snac.cmd 0x02 0x05
 *
 * @see GetInfoCmd
 * @see UserInfoCmd
 */
public class OldGetInfoCmd extends LocCommand {
    /**
     * An information request type indicating a request for the user's "info."
     */
    public static final int TYPE_INFO = 0x0001;
    /**
     * An information request type indicating a request for the user's away
     * message.
     */
    public static final int TYPE_AWAYMSG = 0x0003;
    /**
     * An information request type indicating a request for a list of the user's
     * {@linkplain net.kano.joscar.snaccmd.CapabilityBlock capabilities}.
     */
    public static final int TYPE_CAPS = 0x0004;

    /** The screenname of the user whose information is being requested. */
    private final String sn;
    /** The type of information being requested. */
    private final int type;

    /**
     * Generates a new info request command from the given incoming SNAC
     * packet.
     *
     * @param packet an incoming info request packet
     */
    protected OldGetInfoCmd(SnacPacket packet) {
        super(CMD_OLD_GET_INFO);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

        type = BinaryTools.getUShort(snacData, 0);


        ByteBlock snBlock = snacData.subBlock(2);

        StringBlock snInfo = OscarTools.readScreenname(snBlock);

        sn = snInfo == null ? null : snInfo.getString();
    }

    /**
     * Creates a new information request for the given user of the given type.
     *
     * @param sn the screenname of the user whose information is being requested
     * @param type the type of information being requested, like {@link
     *        #TYPE_AWAYMSG}
     */
    public OldGetInfoCmd(int type, String sn) {
        super(CMD_OLD_GET_INFO);

        DefensiveTools.checkRange(type, "type", 0);
        DefensiveTools.checkNull(sn, "sn");

        this.type = type;
        this.sn = sn;
    }

    /**
     * Returns the "request type." Normally one of {@link #TYPE_INFO}, {@link
     * #TYPE_AWAYMSG}, and {@link #TYPE_CAPS}.
     *
     * @return the type of information being requested
     */
    public final int getRequestType() {
        return type;
    }

    /**
     * Returns the screenname of the user whose info is being requested.
     *
     * @return the screenname of the user whose information is being requested
     */
    public final String getScreenname() {
        return sn;
    }

    public void writeData(OutputStream out) throws IOException {
        BinaryTools.writeUShort(out, type);

        OscarTools.writeScreenname(out, sn);
    }

    public String toString() {
        return "OldGetInfoCmd for " + sn + ", type=" + type;
    }
}
