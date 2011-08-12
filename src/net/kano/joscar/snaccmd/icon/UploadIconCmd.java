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
 *  File created by keith @ Mar 2, 2003
 *
 */

package net.kano.joscar.snaccmd.icon;

import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.Writable;
import net.kano.joscar.flapcmd.SnacPacket;
import net.kano.joscar.tlv.ImmutableTlvChain;
import net.kano.joscar.tlv.Tlv;
import net.kano.joscar.tlv.TlvChain;
import net.kano.joscar.tlv.TlvTools;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A SNAC command sent to upload one's buddy icon to the buddy icon server.
 * Normally responded-to with an {@link UploadIconAck}.
 *
 * @snac.src client
 *
 * @see UploadIconAck
 */
public class UploadIconCmd extends IconCommand {
    /** A TLV type containing the raw icon data. */
    private static final int TYPE_ICON_DATA = 0x0001;

    /** An object used to write the icon data. */
    private final Writable iconWritable;
    /** The icon data. */
    private final ByteBlock iconData;

    /**
     * Generates an upload icon command from the given incoming SNAC packet.
     *
     * @param packet an incoming icon upload packet
     */
    protected UploadIconCmd(SnacPacket packet) {
        super(CMD_UPLOAD_ICON);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

        TlvChain chain = TlvTools.readChain(snacData);

        Tlv iconTlv = chain.getLastTlv(TYPE_ICON_DATA);

        if (iconTlv != null) {
            iconData = iconTlv.getData();
        } else {
            iconData = null;
        }
        iconWritable = iconData;
    }

    /**
     * Creates a new icon upload command with the given icon data writer.
     *
     * @param iconData an object used to write the icon data to the connection
     *
     * @see net.kano.joscar.FileWritable
     */
    public UploadIconCmd(Writable iconData) {
        super(CMD_UPLOAD_ICON);

        this.iconWritable = iconData;
        this.iconData = null;
    }

    /**
     * Returns the icon data being uploaded. Note that this will be
     * <code>null</code> if this is an outgoing upload icon command, as the
     * icon data is not stored in a local byte block but rather written directly
     * from the given <code>Writable</code>.
     *
     * @return the icon data being uploaded
     */
    public final ByteBlock getIconData() {
        return iconData;
    }

    public void writeData(OutputStream out) throws IOException {
        if (iconWritable != null) {
            new Tlv(TYPE_ICON_DATA, iconWritable).write(out);
        }
    }

    public String toString() {
        return "UploadIconCmd";
    }
}
