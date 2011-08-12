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
 *  File created by keith @ Mar 6, 2003
 *
 */

package net.kano.joscar.snaccmd.icbm;

import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.flapcmd.SnacPacket;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A SNAC command indicating to the client what his or her current ICBM
 * parameter settings are. Normally sent in response to a {@link ParamInfoRequest},
 * and optionally responded-to by the client with a {@link SetParamInfoCmd}
 * to adjust the settings provided in this command.
 *
 * @snac.src server
 * @snac.cmd 0x04 0x05
 */
public class ParamInfoCmd extends IcbmCommand {
    /** The parameter information block. */
    private final ParamInfo paramInfo;

    /**
     * Generates a new ICBM parameter information command from the given
     * incoming SNAC packet.
     *
     * @param packet an incoming ICBM parameter information packet
     */
    protected ParamInfoCmd(SnacPacket packet) {
        super(CMD_PARAM_INFO);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

        paramInfo = ParamInfo.readParamInfo(snacData);
    }

    /**
     * Creates a new outgoing default ICBM parameter information command with
     * the given parameter information block.
     *
     * @param paramInfo the parameter information block to send to the client
     */
    public ParamInfoCmd(ParamInfo paramInfo) {
        super(CMD_PARAM_INFO);

        this.paramInfo = paramInfo;
    }

    /**
     * Returns the ICBM parameter information block sent in this command.
     *
     * @return the ICBM parameter information sent in this command
     */
    public final ParamInfo getParamInfo() {
        return paramInfo;
    }

    public void writeData(OutputStream out) throws IOException {
        if (paramInfo != null) paramInfo.write(out);
    }

    public String toString() {
        return "ParamInfoCmd: paramInfo=<" + paramInfo + ">";
    }
}
