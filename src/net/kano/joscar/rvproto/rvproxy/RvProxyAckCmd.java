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
import java.net.Inet4Address;

/**
 * A RV Proxy command indicating that the initialization of an AOL Proxy Server
 * connection is complete and the user on the receiving end may be told to
 * connect.
 *
 * @rvproxy.src server
 */
public class RvProxyAckCmd extends RvProxyCmd {
    /** An IP address to give to the receiving user to create a connection. */
    private final Inet4Address ip;
    /** A "port" value to give to the receiving user. */
    private final int port;

    /**
     * Creates a new RV proxy connection acknowledgement command from the given
     * incoming RV proxy connection acknowledgement packet.
     *
     * @param header an incoming connection acknowledgement packet
     */
    protected RvProxyAckCmd(RvProxyPacket header) {
        super(header);

        ByteBlock data = header.getCommandData();

        port = BinaryTools.getUShort(data, 0);

        ip = BinaryTools.getIPFromBytes(data, 2);
    }

    /**
     * Creates a new outgoing RV proxy connection acknowledgement command with
     * the given proxy server IP address and "port" values.
     *
     * @param ip the IP address for the "receiving user" (the user to whom an
     *        AOL Proxy Server connection invitation is being sent) to connect
     *        to to begin the connection
     * @param port a "port" value for the receiving user to send in its {@link
     *        RvProxyInitRecvCmd}
     */
    public RvProxyAckCmd(Inet4Address ip, int port) {
        super(RvProxyPacket.CMDTYPE_ACK);

        DefensiveTools.checkNull(ip, "ip");
        DefensiveTools.checkRange(port, "port", -1);

        this.ip = ip;
        this.port = port;
    }

    /**
     * Returns the proxy's IP address, as sent in this command. This value
     * should be sent to the "receiving user" (the user to whom an AOL Proxy
     * Server connection invitation is being sent) as the IP address to which
     * that user should connect.
     *
     * @return the AOL Proxy Server's IP address, as sent in this command
     */
    public final Inet4Address getProxyIpAddress() { return ip; }

    /**
     * Returns a "port" value to send to the "receiving user." (The "receiving
     * user" is the user to whom an AOL Proxy Server connection invitation is
     * being sent.) As of this writing, the significance of this value is not
     * known (it is <i>not</i> the port on which the user should connect to the
     * proxy server; that port appears to always be <code>5190</code>). What is
     * known is that this value must be passed by the receiving user in his
     * {@link RvProxyInitRecvCmd} (once again as the "port" value).
     *
     * @return the "port" value used to send to the receiving user
     */
    public final int getProxyPort() { return port; }

    public void writeCommandData(OutputStream out) throws IOException {
        if (port != -1) {
            BinaryTools.writeUShort(out, port);

            if (ip != null) {
                out.write(ip.getAddress());
            }
        }
    }

    public String toString() {
        return "RvProxyAckCmd: ip=" + ip + ", port=" + port;
    }
}