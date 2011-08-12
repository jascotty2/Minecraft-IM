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

package net.kano.joscar.rvproto.rvproxy;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An RV Proxy command used to indicate that some part of initializing a
 * connection failed.
 *
 * @rvproxy.src server
 */
public class RvProxyErrorCmd extends RvProxyCmd {
    /**
     * An error code sent upon connecting to an AOL Proxy Server and not sending
     * any initialization commands. It can be assumed that this error code
     * indicates that a connection has "timed out" waiting for data.
     */
    public static final int ERRORCODE_TIMEOUT = 0x001a;

    /** The error code sent in this command. */
    private final int errorCode;

    /**
     * Creates a new RV Proxy error command from the given incoming error
     * packet.
     *
     * @param header an incoming RV Proxy error packet
     */
    protected RvProxyErrorCmd(RvProxyPacket header) {
        super(header);

        ByteBlock data = header.getCommandData();

        errorCode = BinaryTools.getUShort(data, 0);
    }

    /**
     * Creates a new outgoing RV Proxy error command with the given error code.
     *
     * @param errorCode an error code, like {@link #ERRORCODE_TIMEOUT}
     */
    public RvProxyErrorCmd(int errorCode) {
        super(RvProxyPacket.CMDTYPE_ERROR);

        DefensiveTools.checkRange(errorCode, "errorCode", 0);

        this.errorCode = errorCode;
    }

    /**
     * Returns the error code sent in this command. As of this writing the only
     * error code on record is {@link #ERRORCODE_TIMEOUT}.
     *
     * @return this command's error code
     */
    public final int getErrorCode() { return errorCode; }

    public void writeCommandData(OutputStream out) throws IOException { }

    public String toString() {
        return "RvProxyErrorCmd: errorCode=" + errorCode;
    }
}