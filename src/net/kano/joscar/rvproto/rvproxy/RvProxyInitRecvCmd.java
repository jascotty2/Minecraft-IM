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
 * An AOL Proxy Server command used to initialize a connection when "receiving"
 * an AOL-Proxy-based connection. That is, a command used to initialize an AOL
 * proxy server connection after one has received an invitation to connect to
 * an AOL Proxy Server for a direct connection, file transfer, or other direct
 * TCP connection based rendezvous type.
 *
 * @rvproxy.src client
 */
public class RvProxyInitRecvCmd extends RvProxyCmd {
    /** The screenname of the connecting client. */
    private final String sn;
    /** An ICBM message ID "cookie" sent in this command. */
    private final long icbmMessageId;
    /** A "port" value sent in this command. */
    private final int port;

    /**
     * Creates a new RV proxy connection initialization command from the given
     * incoming RV proxy packet.
     *
     * @param header an incoming connection initialization RV proxy packet
     */
    protected RvProxyInitRecvCmd(RvProxyPacket header) {
        super(header);

        ByteBlock data = header.getCommandData();

        StringBlock snInfo = OscarTools.readScreenname(data);

        if (snInfo != null) {
            sn = snInfo.getString();

            ByteBlock rest = data.subBlock(snInfo.getTotalSize());

            port = BinaryTools.getUShort(rest, 0);
            icbmMessageId = BinaryTools.getLong(rest, 2);
        } else {
            sn = null;
            icbmMessageId = 0;
            port = -1;
        }
    }

    /**
     * Creates a new outgoing RV proxy receiving-end initialization command with
     * the given properties.
     *
     * @param sn the screenname of the connecting client (that is, if being used
     *        by a client, "your" screenname)
     * @param icbmMessageId an ICBM message ID "cookie," normally the ICBM
     *        message ID of the RV ICBM command which invited the user to
     *        make the associated proxy connection
     * @param port the "port" value sent in the RV ICBM command which invited
     *        user to make the associated proxy connection
     */
    public RvProxyInitRecvCmd(String sn, long icbmMessageId, int port) {
        super(RvProxyPacket.CMDTYPE_INIT_RECV);

        DefensiveTools.checkNull(sn, "sn");
        DefensiveTools.checkRange(port, "port", -1);

        this.sn = sn;
        this.icbmMessageId = icbmMessageId;
        this.port = port;
    }

    /**
     * Returns the screenname value sent in this command. Note that this is
     * normally the screenname of the connecting user.
     *
     * @return the screenname value sent in this command
     */
    public final String getScreenname() { return sn; }

    /**
     * Returns the ICBM message ID "cookie" sent in this command. This value is
     * a "cookie" that appears to be used to match up two clients who desire
     * to make a connection over the AOL Proxy Server.
     *
     * @return the ICBM message ID "cookie" value sent in this command
     */
    public final long getIcbmMessageId() { return icbmMessageId; }

    /**
     * Returns the "port" value sent in this command. As of this writing, the
     * significance of this value is unknown; it is known, however, that this is
     * the same value as the {@linkplain RvProxyAckCmd#getProxyPort port value}
     * sent to the initiating client in a {@link RvProxyAckCmd}.
     *
     * @return the "port" value sent in this command
     */
    public final int getPort() { return port; }

    public void writeCommandData(OutputStream out) throws IOException {
        if (sn != null) {
            OscarTools.writeScreenname(out, sn);

            if (port != -1) {
                BinaryTools.writeUShort(out, port);

                BinaryTools.writeLong(out, icbmMessageId);
            }
        }
    }

    public String toString() {
        return "RvProxyInitRecvCmd: sn=" + sn
                + ", icbmMessageId=" + icbmMessageId
                + ", port=" + port;
    }
}