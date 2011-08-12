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
 * A FLAP command sent immediately before a FLAP connection is closed. Note that
 * sending this is always optional, but always means the connection will close
 * immediately. Normally, the server sends this command (possibly with a
 * {@linkplain #getCode disconnection code}), the client sends an {@linkplain
 * #CloseFlapCmd() empty} one back, and the TCP connection terminates.
 *
 * @flap.chan 0x04
 */
public class CloseFlapCmd extends FlapCommand {
    /** The FLAP channel on which close commands reside. */
    public static final int CHANNEL_CLOSE = 0x04;

    /**
     * A disconnection code indicating that this screenname has been logged into
     * elsewhere, and thus you have been disconnected.
     */
    public static final int CODE_LOGGED_IN_ELSEWHERE = 0x0001;

    /** A TLV type containing the disconnection code. */
    private static final int TYPE_CODE = 0x0009;
    /** A TLV containing a related URL. */
    private static final int TYPE_URL = 0x000b;

    /** A disconnection code. */
    private final int code;
    /** A related URL. */
    private final String url;

    /**
     * Generates a new FLAP close command from the given incoming FLAP packet.
     *
     * @param packet an incoming FLAP close packet
     */
    protected CloseFlapCmd(FlapPacket packet) {
        super(CHANNEL_CLOSE);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock flapData = packet.getData();

        TlvChain chain = TlvTools.readChain(flapData);

        code = chain.getUShort(TYPE_CODE);

        url = chain.getString(TYPE_URL);
    }

    /**
     * Creates a new empty outgoing FLAP close command without a disconnection
     * code or associated URL. Using this constructor is equivalent to using
     * {@link #CloseFlapCmd(int, String) new FlapCloseCmd(-1, null)}.
     */
    public CloseFlapCmd() {
        this(-1, null);
    }

    /**
     * Creates a new outgoing FLAP close command with the given disconnection
     * code and no associated URL. Using this constructor is equivalent to using
     * {@link #CloseFlapCmd(int, String) new FlapCloseCmd(code, null)}.
     *
     * @param code a disconnection code, like {@link #CODE_LOGGED_IN_ELSEWHERE},
     *        or <code>-1</code> to indicate that no code should be sent
     */
    public CloseFlapCmd(int code) {
        this(code, null);
    }

    /**
     * Creates a new outgoing FLAP close command with the given disconnection
     * code and related URL.
     *
     * @param code a disconnection code, like {@link #CODE_LOGGED_IN_ELSEWHERE},
     *        or <code>-1</code> if no code should be sent
     * @param url a (HTTP) URL where a description of this error can be found,
     *        or <code>null</code> if no URL should be sent
     */
    public CloseFlapCmd(int code, String url) {
        super(CHANNEL_CLOSE);

        DefensiveTools.checkRange(code, "code", -1);

        this.code = code;
        this.url = url;
    }

    /**
     * Returns the "disconnection code" sent in this close command, or
     * <code>-1</code> if none was sent. Normally {@link
     * #CODE_LOGGED_IN_ELSEWHERE} or <code>-1</code>.
     *
     * @return the "disconnection code" sent in this command, or <code>-1</code>
     *         if none was sent
     */
    public final int getCode() { return code; }

    /**
     * Returns the "error URL" at which a description or support for the given
     * error can be found, or <code>null</code> if no such URL was sent. Note
     * that AOL's AIM servers only send <code>http://www.aol.com</code> here.
     *
     * @return an "error URL," or <code>null</code> if none was sent
     */
    public final String getUrl() { return url; }

    public void writeData(OutputStream out) throws IOException {
        if (code != -1) Tlv.getUShortInstance(TYPE_CODE, code).write(out);
        if (url != null) Tlv.getStringInstance(TYPE_URL, url).write(out);
    }

    public String toString() {
        return "CloseFlapCmd: code=0x" + Integer.toHexString(code)
                + ", url=" + url;
    }
}
