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

import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.flapcmd.SnacPacket;
import net.kano.joscar.tlv.Tlv;
import net.kano.joscar.tlv.TlvChain;
import net.kano.joscar.tlv.TlvTools;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A SNAC command sent in response to a {@link ServiceRequest} to direct
 * the client to an OSCAR server that supports the specified SNAC family.
 *
 * @snac.src server
 * @snac.cmd 0x01 0x05
 *
 * @see ServiceRequest
 */
public class ServiceRedirect extends ConnCommand {
    /** A TLV type containing the SNAC family that the given server supports. */
    private static final int TYPE_FAMILY = 0x000d;
    /** A TLV type containing the host to which the client should connect. */
    private static final int TYPE_HOST = 0x0005;
    /**
     * A TLV type containing a login cookie to provide to the given server upon
     * connecting.
     */
    private static final int TYPE_COOKIE = 0x0006;

    /** The SNAC family that the given server supports. */
    private final int family;
    /** The host to which the client should connect. */
    private final String host;
    /** The port on which the client should connect to the given host. */
    private final int port;
    /** A login cookie to be provided to the given host upon connecting. */
    private final ByteBlock cookie;

    /**
     * Creates a new service redirect command from the given incoming SNAC
     * packet.
     *
     * @param packet the incoming service redirection packet
     */
    protected ServiceRedirect(SnacPacket packet) {
        super(CMD_SERVICE_REDIR);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

        TlvChain chain = TlvTools.readChain(snacData);

        family = chain.getUShort(TYPE_FAMILY);

        String hostString = chain.getString(TYPE_HOST);
        if (hostString != null) {
            int colonPos = hostString.indexOf(':');
            if (colonPos != -1) {
                host = hostString.substring(0, colonPos);
                int portTmp = -1;
                String portString = hostString.substring(colonPos + 1);
                try {
                    portTmp = Integer.parseInt(portString);
                } catch (NumberFormatException e) {
                    // it's okay; we'll use -1
                }
                port = portTmp;
            } else {
                host = hostString;
                port = -1;
            }
        } else {
            host = null;
            port = -1;
        }

        cookie = chain.getLastTlv(TYPE_COOKIE).getData();
    }

    /**
     * Creates a new outgoing service redirection command with the given
     * properties and no port field.
     *
     * @param family the SNAC family supported by the given server
     * @param host the hostname of the server to connect to
     * @param cookie a login cookie to provide to the given server upon
     *        connecting
     */
    public ServiceRedirect(int family, String host, ByteBlock cookie) {
        this(family, host, -1, cookie);
    }

    /**
     * Creates a new outgoing service redirection command with the given
     * properties.
     *
     * @param family the SNAC family supported by the given server
     * @param host the hostname of the server to connect to
     * @param port the port on which to connect to the server, or
     *        <code>-1</code> for none
     * @param cookie a login cookie to provide to the given server upon
     *        connecting
     */
    public ServiceRedirect(int family, String host, int port,
            ByteBlock cookie) {
        super(CMD_SERVICE_REDIR);

        DefensiveTools.checkRange(family, "family", 0);

        this.family = family;
        this.host = host;
        this.port = port;
        this.cookie = cookie;
    }

    /**
     * Returns the SNAC family that is supported by the given host.
     *
     * @return the SNAC family for which this redirection command is providing
     *         a host
     */
    public final int getSnacFamily() { return family; }

    /**
     * Returns the host to which the client should connect for the given
     * service.
     *
     * @return the host that supports the given SNAC family
     */
    public final String getRedirectHost() { return host; }

    /**
     * Returns the port on which the client should connect to the given host,
     * or <code>-1</code> if none was specified.
     *
     * @return the port on which the client should connect to the given host
     */
    public int getRedirectPort() { return port; }

    /**
     * Returns the login cookie that should be {@linkplain
     * net.kano.joscar.flapcmd.LoginFlapCmd FLAP provided} upon connecting to
     * the specified host.
     *
     * @return a FLAP login cookie for the given host
     */
    public final ByteBlock getCookie() { return cookie; }

    public void writeData(OutputStream out) throws IOException {
        Tlv.getUShortInstance(TYPE_FAMILY, family).write(out);
        if (host != null) {
            StringBuffer hostString = new StringBuffer();
            hostString.append(host);
            if (port != -1) {
                hostString.append(':');
                hostString.append(port);
            }
            Tlv.getStringInstance(TYPE_HOST, hostString.toString()).write(out);
        }
        if (cookie != null) new Tlv(TYPE_COOKIE, cookie).write(out);
    }

    public String toString() {
        return "ServiceRedirect: family=0x" + Integer.toHexString(family)
                + ", host=" + host + ", port=" + port + ", cookie="
                + cookie.getLength() + " bytes";
    }
}
