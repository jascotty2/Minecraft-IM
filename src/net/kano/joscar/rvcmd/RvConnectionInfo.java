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
 *  File created by keith @ Apr 28, 2003
 *
 */

package net.kano.joscar.rvcmd;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.LiveWritable;
import net.kano.joscar.tlv.Tlv;
import net.kano.joscar.tlv.TlvChain;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;

/**
 * A data structure containing information about a destination address for a
 * client-to-client rendezvous TCP connection.
 * <br>
 * <br>
 * Briefly, <code>RvConnectionInfo</code> represents a set of {@linkplain
 * AbstractRvCmd#getRvTlvs RV TLV's} common to all TCP-connection-based
 * rendezvous requests (and redirects). When a user sends a Direct IM
 * invitation, for example, TLV's containing his ("internal") IP address and a
 * port on which the recipient can connect for a Direct IM connection. Between
 * the user and the recipient, the server adds another field to the command: the
 * user's "external" IP address, or his or her address from <i>its</i>
 * perspective. Finally, the recipient attempts to connect to either address (if
 * they are different). (If he or she cannot connect, he sends a "redirect"
 * request with his own <code>RvConnectionInfo</code>.)
 * <br>
 * <br>
 * New to the rendezvous client-to-client system is the AOL Proxy Server, a
 * proxy hosted by AOL (at <code>ars.oscar.aol.com</code>) to allow two
 * firewalled users to send files and hold Direct IM sessions even though
 * neither can accept incoming TCP connections. This class accommodates for this
 * with the fields <code>proxyIP</code> and <code>proxied</code>. For details on
 * AOL Proxy Server support, see {@link net.kano.joscar.rvproto.rvproxy}.
 * <br>
 * <br>
 * The protocol for initiating such connection-based rendezvouses provides
 * the concept of an "internal" and an "external" IP address. The need for such
 * a distinction comes from the fact that when one is behind a firewall one may
 * have a "LAN IP address" and a "WAN IP address," the LAN IP address being
 * a computer's IP address on the internal network and the WAN IP address being
 * the IP address of that same computer to users from outside the internal
 * network.
 * <br>
 * <br>
 * When attempting to initiate a direct TCP connection, a client normally
 * provides its "internal" or "LAN" IP address to the server. (In most cases,
 * when not behind a firewall, this address is also the "external" IP address.)
 * During the request's transmission from the source client to the recipient of
 * the request, the server adds a field to the request containing the IP address
 * of the source client from its perspective outside the user's internal
 * network (unless, of course, one is connecting from inside an internal network
 * at AOL :).
 */
public class RvConnectionInfo implements LiveWritable {
    /** A TLV type containing the IP address of an AOL Proxy Server. */
    private static final int TYPE_PROXYIP = 0x0002;
    /** A TLV type containing what a client believes to be its IP address. */
    private static final int TYPE_INTERNALIP = 0x0003;
    /**
     * A TLV type containing what the OSCAR server believes to be a user's IP
     * address.
     */
    private static final int TYPE_EXTERNALIP = 0x0004;
    /** A TLV type containing a TCP port. */
    private static final int TYPE_PORT = 0x0005;
    /**
     * A TLV type present if a connection information block describes a
     * connection via an AOL Proxy Server.
     */
    private static final int TYPE_PROXIED = 0x0010;
    /**
     * A TLV type present if the associated connection is to be encrypted over
     * SSL.
     */
    private static final int TYPE_ENCRYPTED = 0x0011;

    /** Whether the associated connection is to be encrypted over SSL. */
    private final boolean encrypted;
    /**
     * Whether or not this connection information block describes a "proxied"
     * connection.
     */
    private final boolean proxied;
    /** The IP address of an AOL Proxy Server. */
    private final InetAddress proxyIP;
    /** The user's "internal" IP address. */
    private final InetAddress internalIP;
    /**
     * The user's "external" IP address, normally determined by the OSCAR
     * server.
     */
    private final InetAddress externalIP;
    /** A port on which to connect to the specified addresses. */
    private final int port;

    /**
     * Reads a connection information block from the given RV TLV chain. Note
     * that this method will <i>never</i> return <code>null</code> even if the
     * given TLV chain contains no connection information TLV's.
     *
     * @param chain a TLV chain containing a rendezvous connection information
     *        block
     * @return a <code>RvConnectionInfo</code> read from the given TLV's
     */
    public static RvConnectionInfo readConnectionInfo(TlvChain chain) {
        DefensiveTools.checkNull(chain, "chain");

        Tlv internalIpTlv = chain.getLastTlv(TYPE_INTERNALIP);
        Inet4Address internalIP = null;
        if (internalIpTlv != null) {
            internalIP = BinaryTools.getIPFromBytes(internalIpTlv.getData(), 0);
        }

        Tlv externalIpTlv = chain.getLastTlv(TYPE_EXTERNALIP);
        Inet4Address externalIP = null;
        if (externalIpTlv != null) {
            externalIP = BinaryTools.getIPFromBytes(externalIpTlv.getData(), 0);
        }

        Tlv proxyIpTlv = chain.getLastTlv(TYPE_PROXYIP);
        Inet4Address proxyIP = null;
        if (proxyIpTlv != null) {
            proxyIP = BinaryTools.getIPFromBytes(proxyIpTlv.getData(), 0);
        }

        int port = chain.getUShort(TYPE_PORT);

        boolean proxied = chain.hasTlv(TYPE_PROXIED);

        boolean encrypted = chain.hasTlv(TYPE_ENCRYPTED);

        return new RvConnectionInfo(internalIP, externalIP, proxyIP, port,
                proxied, encrypted);
    }

    /**
     * Creates a new connection information block with the given "internal IP
     * address" and port.
     *
     * @param internalIP the client's IP address
     * @param port a TCP port on which the recipient should connect, or
     *        <code>-1</code> to not specify this field
     * @return a <code>RvConnectionInfo</code> containing the given internal IP
     *         address and TCP port
     */
    public static RvConnectionInfo createForOutgoingRequest(
            InetAddress internalIP, int port) {
        DefensiveTools.checkNull(internalIP, "internalIP");

        return new RvConnectionInfo(internalIP, null, null, port, false, false);
    }

    /**
     * Creates a new connection information block with the given "internal IP
     * address" and port for a secure (SSL) connection to the given host.
     *
     * @param internalIP the client's IP address
     * @param port a TCP port on which the recipient should connect, or
     *        <code>-1</code> to not specify this field
     * @return a <code>RvConnectionInfo</code> containing the given internal IP
     *         address and TCP port
     */
    public static RvConnectionInfo createForOutgoingSecureRequest(
            InetAddress internalIP, int port) {
        DefensiveTools.checkNull(internalIP, "internalIP");

        return new RvConnectionInfo(internalIP, null, null, port, false, true);
    }

    /**
     * Creates a new connection information block describing a connection via
     * the AOL Proxy Server with the given proxy server IP address and "port
     * number." Note that this port number is not used as a TCP port but rather
     * is simply a value sent in the AOL Proxy Server protocol. As of this
     * writing, it seems that one should always connect to the AOL proxy server
     * on port <code>5190</code>.
     *
     * @param proxyIP the IP address of the AOL Proxy Server over which the
     *        connection should be made
     * @param port a "port" value used in the AOL Proxy Server protocol
     * @return a <code>RvConnectionInfo</code> containing the given proxy IP and
     *         port value (and, of course, the <code>proxied</code> flag set to
     *         <code>true</code>)
     */
    public static RvConnectionInfo createForOutgoingProxiedRequest(
            Inet4Address proxyIP, int port) {
        DefensiveTools.checkNull(proxyIP, "proxyIP");

        return new RvConnectionInfo(null, null, proxyIP, port, true, false);
    }

    /**
     * Creates a new connection information block with the given properties.
     * <br>
     * <br>
     * Note that normally a client does not define the <code>externalIP</code>
     * value (that is, the <code>externalIP</code> value should normally be
     * <code>null</code>). This value is normally inserted by the OSCAR server.
     * <br>
     * <br>
     * Also note that there are two normal "formats" for an outgoing
     * <code>RvConnectionInfo</code>, as described below. Note that in the table
     * below "set" means "non-<code>null</code>" for objects and "nonnegative"
     * for integers (the value of <code>port</code>).
     * <table>
     * <tr><th><code>internalIP</code></th><th><code>externalIP</code></th>
     * <th><code>proxyIP</code></th><th><code>port</code></th>
     * <th><code>proxied</code></th><th><code>encrypted</code></th></tr>
     * <tr><td>set</td><td>not set</td><td>not set</td>
     * <td>set</td><td><code>false</code></td><td><code>true</code> or
     * <code>false</code></td></tr>
     * <tr><td>not set</td><td>not set</td><td>set</td>
     * <td>set</td><td><code>true</code></td><td><code>true</code> or
     * <code>false</code></td></tr>
     * </table>
     *
     * @param internalIP the client's "internal IP address," the local IP
     *        address from the perspective of the client
     * @param externalIP the client's "external IP address," the IP address from
     *        the perspective of the OSCAR server
     * @param proxyIP the IP address of an AOL Proxy Server through which a
     *        connection should be made
     * @param port a TCP port on which the recipient of this block should
     *        connect to the given IP address, or <code>-1</code> for none
     * @param proxied whether or not the described connection is "proxied"
     * @param encrypted whether or not the described connection is to be made
     *        via SSL
     *
     * @see #createForOutgoingRequest
     * @see #createForOutgoingProxiedRequest
     */
    public RvConnectionInfo(InetAddress internalIP, Inet4Address externalIP,
            Inet4Address proxyIP, int port, boolean proxied,
            boolean encrypted) {
        DefensiveTools.checkRange(port, "port", -1);

        this.internalIP = internalIP;
        this.externalIP = externalIP;
        this.proxyIP = proxyIP;
        this.port = port;
        this.proxied = proxied;
        this.encrypted = encrypted;
    }

    /**
     * Returns the "internal IP address" contained in this connection
     * information block. See {@linkplain RvConnectionInfo above} for details.
     *
     * @return this connection information block's internal IP address value,
     *         or <code>null</code> if none is present
     */
    public final InetAddress getInternalIP() { return internalIP; }

    /**
     * Returns the "external IP address" contained in this connection
     * information block. See {@linkplain RvConnectionInfo above} for details.
     *
     * @return this connection information block's external IP address value
     *         or <code>null</code> if none is present
     */
    public final InetAddress getExternalIP() { return externalIP; }

    /**
     * Returns whether this connection block describes a connection over an AOL
     * Proxy Server. Normally a {@linkplain #getProxyIP proxy IP address} is
     * specified if this value is <code>true</code>. See {@linkplain
     * RvConnectionInfo above} for details.
     *
     * @return whether or not this connection information block represents a
     *         connection over an AOL Proxy Server
     */
    public final boolean isProxied() { return proxied; }

    /**
     * Returns the IP address of an AOL Proxy Server over which a connection
     * should be made. This value is normally <code>null</code> unless the value
     * of {@link #isProxied} is <code>true</code>. See {@linkplain
     * RvConnectionInfo above} for details.
     *
     * @return the AOL Proxy Server IP address specified in this connection
     *         information block, or <code>null</code> if none was sent
     */
    public final InetAddress getProxyIP() { return proxyIP; }

    /**
     * Returns the TCP port specified in this connection information block. Note
     * that this value is not the port to which the client could connect to the
     * AOL proxy server. See {@link #createForOutgoingProxiedRequest} for
     * details. Note that this method will return <code>-1</code> if no port is
     * present in this block.
     *
     * @return the TCP port the TCP port specified in this connection
     *         information block, or <code>-1</code> of none is present
     */
    public final int getPort() { return port; }

    /**
     * Returns whether or not the associated connection is to be encrypted via
     * SSL.
     *
     * @return whether the associated connection is to be encrypted over SSL
     */
    public final boolean isEncrypted() { return encrypted; }

    /**
     * Writes an IP address (in raw byte format) to the given stream as a TLV
     * of the given type.
     *
     * @param out the stream to which to write
     * @param type the type of the TLV to write
     * @param addr the IP address to write
     *
     * @throws IOException if an I/O error occurs
     */
    private static final void writeIP(OutputStream out, int type,
            InetAddress addr) throws IOException {
        ByteBlock addrBlock = ByteBlock.wrap(addr.getAddress());
        new Tlv(type, addrBlock).write(out);
    }

    public void write(OutputStream out) throws IOException {
        if (internalIP != null) writeIP(out, TYPE_INTERNALIP, internalIP);
        if (externalIP != null) writeIP(out, TYPE_EXTERNALIP, externalIP);
        if (proxyIP != null) writeIP(out, TYPE_PROXYIP, proxyIP);
        if (port != -1) Tlv.getUShortInstance(TYPE_PORT, port).write(out);
        if (proxied) new Tlv(TYPE_PROXIED).write(out);
        if (encrypted) new Tlv(TYPE_ENCRYPTED).write(out);
    }

    public String toString() {
        return "ConnectionInfo: " +
                (proxied ? "(proxied) " : "") +
                (encrypted ? "(encrypted) " : "") +
                "internalIP=" + internalIP +
                ", externalIP=" + externalIP +
                ", proxyIP=" + proxyIP +
                ", port=" + port;
    }
}
