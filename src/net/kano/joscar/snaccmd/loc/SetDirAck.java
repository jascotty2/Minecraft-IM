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

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.flapcmd.SnacPacket;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A SNAC command sent to acknowledge the setting of one's directory
 * information. Normally sent in response to a {@link SetDirInfoCmd}.
 *
 * @snac.src server
 * @snac.cmd 0x02 0x0a
 *
 * @see SetDirInfoCmd
 */
public class SetDirAck extends LocCommand {
    /**
     * A result code indicating that your directory information was set
     * successfully.
     */
    public static final int CODE_SUCCESS = 0x0001;
    /**
     * A result code indicating that your directory information was not set
     * successfully.
     */
    public static final int CODE_ERROR = 0x0002;

    /** A result code. */
    private final int code;

    /**
     * Generates a set-directory-information acknowledgement from the given
     * incoming SNAC packet.
     *
     * @param packet an incoming set-dir-info acknowlegement packet
     */
    protected SetDirAck(SnacPacket packet) {
        super(CMD_SET_DIR_ACK);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

        code = BinaryTools.getUShort(snacData, 0);
    }

    /**
     * Creates a new set-directory-information acknowledgement command with
     * a result code of {@link #CODE_SUCCESS}.
     */
    public SetDirAck() {
        this(CODE_SUCCESS);
    }

    /**
     * Creates a new set-directory-information acknowledgement command with the
     * given result code.
     *
     * @param code a result code, like {@link #CODE_SUCCESS}
     */
    public SetDirAck(int code) {
        super(CMD_SET_DIR_ACK);

        DefensiveTools.checkRange(code, "code", 0);

        this.code = code;
    }

    /**
     * Returns the result code of this acknowledgement. Normally one of {@link
     * #CODE_SUCCESS} and {@link #CODE_ERROR}.
     *
     * @return this acknowledgement command's result code
     */
    public final int getResultCode() {
        return code;
    }

    public void writeData(OutputStream out) throws IOException {
        BinaryTools.writeUShort(out, code);
    }

    public String toString() {
        return "SetDirAck: code=" + code;
    }
}
