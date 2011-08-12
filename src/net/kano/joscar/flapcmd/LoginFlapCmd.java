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
 *  File created by keith @ Feb 19, 2003
 *
 */

package net.kano.joscar.flapcmd;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.flap.FlapCommand;
import net.kano.joscar.flap.FlapPacket;
import net.kano.joscar.tlv.ImmutableTlvChain;
import net.kano.joscar.tlv.Tlv;
import net.kano.joscar.tlv.TlvChain;
import net.kano.joscar.tlv.TlvTools;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A command sent to identify oneself on a new FLAP connection with a FLAP
 * protocol version and, optionally, a "login cookie" provided by another OSCAR
 * server. This is always the first packet sent and received on a FLAP
 * connection.
 *
 * @flap.chan 1
 */
public class LoginFlapCmd extends FlapCommand {
    /**
     * The FLAP channel on which this command resides.
     */
    public static final int CHANNEL_LOGIN = 0x0001;

    /**
     * I guess this is the FLAP protocol version joscar implements; this is
     * what should be sent in a FLAP version command. <code>1</code> is the only
     * publically known FLAP protocol version.
     */
    public static final long VERSION_DEFAULT = 0x00000001;

    /**
     * The type of the TLV that contains the cookie block.
     */
    private static final int TYPE_COOKIE = 0x0006;

    /**
     * The version of the FLAP protocol in use.
     */
    private final long version;

    /**
     * The connection cookie.
     */
    private final ByteBlock cookie;

    /**
     * Generates a <code>LoginCookieCmd</code> from the given packet.
     *
     * @param packet the packet from which this object should be generated
     */
    public LoginFlapCmd(FlapPacket packet) {
        super(CHANNEL_LOGIN);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock flapData = packet.getData();

        version = BinaryTools.getUInt(flapData, 0);

        ByteBlock tlvData = flapData.subBlock(4);
        TlvChain chain = TlvTools.readChain(tlvData);

        Tlv cookieTlv = chain.getLastTlv(TYPE_COOKIE);
        if (cookieTlv != null) cookie = cookieTlv.getData();
        else cookie = null;
    }

    /**
     * Creates a <code>LoginCookieCmd</code> with the {@linkplain
     * #VERSION_DEFAULT default FLAP version} and no login cookie.
     */
    public LoginFlapCmd() {
        this(VERSION_DEFAULT, null);
    }

    /**
     * Creates a <code>LoginCookieCmd</code> with the given FLAP version and
     * no login cookie. This constructor is useful for server developers because
     * no login cookie is ever sent by the server; also, no login cookie is sent
     * by the client or the server upon initial connection to the login ("auth")
     * server.
     *
     * @param version the FLAP protocol version in use on the FLAP connection on
     *        which this command will be sent
     */
    public LoginFlapCmd(long version) {
        this(version, null);
    }

    /**
     * Creates a <code>LoginCookieCmd</code> with the {@linkplain
     * #VERSION_DEFAULT default FLAP version} and the given login cookie.
     *
     * @param cookie the login cookie for the connection on which this command
     *        will be sent
     */
    public LoginFlapCmd(ByteBlock cookie) {
        this(VERSION_DEFAULT, cookie);
    }

    /**
     * Creates a new <code>LoginCookieCmd</code> with the given FLAP protocol
     * version and with the given cookie.
     *
     * @param version the FLAP protocol version in use
     * @param cookie a login "cookie" provided by another OSCAR connection
     */
    public LoginFlapCmd(long version, ByteBlock cookie) {
        super(CHANNEL_LOGIN);

        DefensiveTools.checkRange(version, "version", 0);

        this.version = version;
        this.cookie = cookie;
    }

    /**
     * Returns the FLAP protocol version declared in this command.
     *
     * @return the FLAP version of the FLAP connection on which this packet was
     *         received
     */
    public final long getVersion() { return version; }

    /**
     * Returns the login cookie associated with this command.
     *
     * @return this command's login cookie
     */
    public final ByteBlock getCookie() { return cookie; }

    public void writeData(OutputStream out) throws IOException {
        BinaryTools.writeUInt(out, version);
        if (cookie != null) new Tlv(TYPE_COOKIE, cookie).write(out);
    }
}
