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
 *  File created by keith @ Feb 24, 2003
 *
 */

package net.kano.joscar.snaccmd.acct;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.flapcmd.SnacPacket;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A SNAC command sent to request some information about one's AIM account.
 *
 * @snac.src client
 * @snac.cmd 0x07 0x02
 *
 * @see AcctInfoCmd
 */
public class AcctInfoRequest extends AcctCommand {
    /**
     * A type code indicating that this is a request for the user's screenname
     * in the correct format (that is, with the "official" spacing and
     * capitalization seen by other users).
     */
    public static final int TYPE_SN = 0x0001;
    /**
     * A type code indicating that this is a request for the user's registered
     * email address.
     */
    public static final int TYPE_EMAIL = 0x0011;

    /**
     * The only subtype code ever used as of this writing.
     */
    public static final int SUBTYPE_DEFAULT = 0x0000;

    /** The type code of this request. */
    private final int type;
    /** The subtype code of this request. */
    private final int subType;

    /**
     * Generates an account information request command from the given incoming
     * SNAC packet.
     *
     * @param packet the packet from which this object should be read
     */
    protected AcctInfoRequest(SnacPacket packet) {
        super(CMD_INFO_REQ);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

        type = BinaryTools.getUShort(snacData, 0);
        subType = BinaryTools.getUShort(snacData, 2);
    }

    /**
     * Creates an outgoing account information request command with the given
     * type and a subtype of {@link #SUBTYPE_DEFAULT}.
     *
     * @param type the type of this command, like {@link #TYPE_SN}
     */
    public AcctInfoRequest(int type) {
        this(type, SUBTYPE_DEFAULT);
    }

    /**
     * Creates an outgoing account information request command with the given
     * type and subtype.
     *
     * @param type the type of this command, like {@link #TYPE_EMAIL}
     * @param subType the subtype of this command; should probably always be
     *        {@link #SUBTYPE_DEFAULT}
     */
    public AcctInfoRequest(int type, int subType) {
        super(CMD_INFO_REQ);

        DefensiveTools.checkRange(type, "type", 0);
        DefensiveTools.checkRange(subType, "subType", 0);

        this.type = type;
        this.subType = subType;
    }

    /**
     * Returns the "type" code of this request. Will normally be one of
     * {@link #TYPE_SN} or {@link #TYPE_EMAIL}.
     *
     * @return this request's type code
     */
    public final int getType() {
        return type;
    }

    /**
     * Returns the "subtype" code of this request. Will normally be {@link
     * #SUBTYPE_DEFAULT}.
     *
     * @return this request's subtype code
     */
    public final int getSubType() {
        return subType;
    }

    public void writeData(OutputStream out) throws IOException {
        BinaryTools.writeUShort(out, type);
        BinaryTools.writeUShort(out, subType);
    }

    public String toString() {
        return "InfoRequest: type=" + type + ", subtype=" + subType;
    }
}
