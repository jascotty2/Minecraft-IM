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
 *  File created by keith @ Apr 24, 2003
 *
 */

package net.kano.joscar.flapcmd;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.flap.FlapCommand;
import net.kano.joscar.flap.FlapPacket;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A FLAP command sent when a FLAP-level error occurs.
 *
 * @flap.chan 0x03
 */
public class FlapErrorCmd extends FlapCommand {
    /** The FLAP channel where FLAP error commands live. */
    public static final int CHANNEL_ERROR = 0x03;

    /** The error code. */
    private final int code;

    /**
     * Generates a new FLAP error command from the given incoming FLAP packet.
     *
     * @param packet an incoming FLAP error packet
     */
    protected FlapErrorCmd(FlapPacket packet) {
        super(CHANNEL_ERROR);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock flapData = packet.getData();

        code = BinaryTools.getUShort(flapData, 0);
    }

    /**
     * Creates a new outgoing FLAP error command with the given error code.
     *
     * @param code the error code to send in this FLAP error command
     */
    public FlapErrorCmd(int code) {
        super(CHANNEL_ERROR);

        DefensiveTools.checkRange(code, "code", 0);

        this.code = code;
    }

    /**
     * Returns the error code sent in this FLAP error command.
     *
     * @return this FLAP error command's error code
     */
    public final int getErrorCode() { return code; }

    public void writeData(OutputStream out) throws IOException {
        BinaryTools.writeUShort(out, code);
    }

    public String toString() {
        return "FlapErrorCmd: code=0x" + Integer.toHexString(code);
    }
}
