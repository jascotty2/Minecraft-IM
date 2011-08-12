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
import net.kano.joscar.OscarTools;
import net.kano.joscar.StringBlock;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An RV Proxy command sent upon connecting to an AOL Proxy Server when
 * initiating a proxy-based connection.
 *
 * @rvproxy.src client
 */
public class RvProxyInitSendCmd extends RvProxyCmd {
    /** The sending client's screenname. */
    private final String sn;
    /** An ICBM message ID "cookie" to send in this command. */
    private final long icbmMessageId;

    /**
     * Reads an RV proxy connection initialization command from the given
     * incoming RV proxy packet.
     *
     * @param header an incoming connection initialization RV proxy packet
     */
    protected RvProxyInitSendCmd(RvProxyPacket header) {
        super(header);

        ByteBlock data = header.getCommandData();

        StringBlock snInfo = OscarTools.readScreenname(data);

        if (snInfo != null) {
            sn = snInfo.getString();

            ByteBlock rest = data.subBlock(snInfo.getTotalSize());

            icbmMessageId = BinaryTools.getLong(rest, 0);

        } else {
            sn = null;
            icbmMessageId = 0;
        }
    }

    /**
     * Creates a new outgoing RV proxy initialization command with the given
     * initiating screenname and ICBM message ID "cookie."
     *
     * @param sn the screenname of the user initializing this connection (that
     *        is, your client's screenname)
     * @param icbmMessageId the ICBM message ID of the rendezvous request
     *        command sent to request the associated proxied connection
     */
    public RvProxyInitSendCmd(String sn, long icbmMessageId) {
        super(RvProxyPacket.CMDTYPE_INIT_SEND);

        DefensiveTools.checkNull(sn, "sn");

        this.sn = sn;
        this.icbmMessageId = icbmMessageId;
    }

    /**
     * Returns the screenname field contained in this command. This value will
     * normally be the screenname of the user connecting to the proxy (that is,
     * the connecting client's screenname).
     *
     * @return the connecting client's screenname
     */
    public final String getScreenname() { return sn; }

    /**
     * Returns the ICBM message ID "cookie" sent in this command. This is a
     * "cookie" that appears to be used to allow the server to pair up two users
     * connecting to the AOL proxy server to form a connection. In practice,
     * this value is the ICBM message ID of the RV ICBM used to request the
     * associated connection (such as a file transfer request).
     *
     * @return this command's ICBM message ID "cookie"
     */
    public final long getIcbmMessageId() { return icbmMessageId; }

    public void writeCommandData(OutputStream out) throws IOException {
        if (sn != null) {
            OscarTools.writeScreenname(out, sn);

            BinaryTools.writeLong(out, icbmMessageId);
        }
    }

    public String toString() {
        return "RvProxyInitSendCmd: sn=" + sn
                + ", icbmMessageId=" + icbmMessageId;
    }
}