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

package net.kano.joscar.snaccmd.conn;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.flapcmd.SnacPacket;
import net.kano.joscar.tlv.ImmutableTlvChain;
import net.kano.joscar.tlv.Tlv;
import net.kano.joscar.tlv.TlvChain;
import net.kano.joscar.tlv.TlvTools;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A SNAC command sent to alert the user that he or she should upgrade to a
 * newer version of AOL Instant Messenger.
 *
 * @snac.src server
 * @snac.cmd 0x01 0x13
 */
public class UpdateAdvisory extends ConnCommand {
    /**
     * An update advisory type indicating that the given update is "mandatory,"
     * and that the client cannot connect until he or she updates.
     */
    public static final int TYPE_MANDATORY_UPDATE = 1;
    /**
     * An update advisory type indicating that the given update is recommended,
     * but not (yet) necessary.
     */
    public static final int TYPE_RECOMMENDED_UPDATE = 2;
    /**
     * An update advisory type indicating that this is not an update advisory
     * but in fact contains a "message of the day." Supposedly, anyway. I've
     * never seen this one.
     */
    public static final int TYPE_SYSTEM_BULLETIN = 3;
    /** An update advisory type indicating that there is nothing to upgrade. */
    public static final int TYPE_OKAY = 4;

    /** A TLV type containing a textual message. */
    private static final int TYPE_MESSAGE = 0x000b;

    /** The upgrade advisory code. */
    private final int type;
    /** The upgrade advisory message. */
    private final String message;

    /**
     * Generates a new update advisory command from the given incoming SNAC
     * packet.
     *
     * @param packet an incoming update advisory packet
     */
    protected UpdateAdvisory(SnacPacket packet) {
        super(CMD_UPDATE);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

        type = BinaryTools.getUShort(snacData, 0);

        ByteBlock tlvBlock = snacData.subBlock(2);
        TlvChain chain = TlvTools.readChain(tlvBlock);

        message = chain.getString(TYPE_MESSAGE);
    }

    /**
     * Creates a new outgoing update advisory command of the given type and with
     * no message. Using this constructor is equivalent to calling {@link
     * #UpdateAdvisory(int, String) new UpdateAdvisory(type, null)}.
     *
     * @param type the update advisory type, like {@link
     *        #TYPE_RECOMMENDED_UPDATE}
     */
    public UpdateAdvisory(int type) {
        this(type, null);
    }

    /**
     * Creates a new outgoing update advisory command with the given type and
     * message.
     *
     * @param type he update advisory type, like {@link #TYPE_MANDATORY_UPDATE}
     * @param message a message to be displayed to the user
     */
    public UpdateAdvisory(int type, String message) {
        super(CMD_UPDATE);

        DefensiveTools.checkRange(type, "type", 0);

        this.type = type;
        this.message = message;
    }

    /**
     * Returns the type of this advisory. Normally one of {@link
     * #TYPE_MANDATORY_UPDATE}, {@link #TYPE_RECOMMENDED_UPDATE}, {@link
     * #TYPE_SYSTEM_BULLETIN}, and {@link #TYPE_OKAY}.
     *
     * @return the advisory type code
     */
    public final int getAdvisoryType() {
        return type;
    }

    /**
     * Returns the message associated with this update advisory.
     *
     * @return the update advisory message
     */
    public final String getMessage() {
        return message;
    }

    public void writeData(OutputStream out) throws IOException {
        BinaryTools.writeUShort(out, type);
        if (message != null) {
            Tlv.getStringInstance(TYPE_MESSAGE, message).write(out);
        }
    }

    public String toString() {
        return "UpdateAdvisory: type=" + type + ", message="
                + message;
    }
}
