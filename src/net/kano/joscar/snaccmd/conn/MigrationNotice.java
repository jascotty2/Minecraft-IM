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

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.flapcmd.SnacPacket;
import net.kano.joscar.tlv.ImmutableTlvChain;
import net.kano.joscar.tlv.Tlv;
import net.kano.joscar.tlv.TlvChain;
import net.kano.joscar.tlv.TlvTools;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A SNAC command sent to tell the client to "migrate" to another server.
 *
 * @snac.src server
 * @snac.cmd 0x01 0x12
 */
public class MigrationNotice extends ConnCommand {
    /** A TLV containing a host to which the client should migrate. */
    private static final int TYPE_HOST = 0x0005;
    /** A TLV containing a login cookie for the new host. */
    private static final int TYPE_COOKIE = 0x0006;

    /** The SNAC families supported by the new host. */
    private final int[] families;
    /** The host to which the client should migrate. */
    private final String host;
    /** The login cookie to use upon connecting to the given server. */
    private final ByteBlock cookie;

    /**
     * Creates a new migration notice command from the given incoming SNAC
     * packet.
     *
     * @param packet an incoming migration notice packet
     */
    protected MigrationNotice(SnacPacket packet) {
        super(CMD_MIGRATE_PLS);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

        int familyCount = BinaryTools.getUShort(snacData, 0);
        families = new int[familyCount];

        for (int i = 0; i < families.length; i++) {
            families[i] = BinaryTools.getUShort(snacData, 2+i*2);
        }

        ByteBlock tlvBlock = snacData.subBlock(2+familyCount*2);

        TlvChain chain = TlvTools.readChain(tlvBlock);

        host = chain.getString(TYPE_HOST);

        cookie = chain.getLastTlv(TYPE_COOKIE).getData();
    }

    /**
     * Creates a new outgoing migration notice command with the given supported
     * SNAC families, hostname, and login cookie for that host. The list of
     * SNAC families may be <code>null</code> to indicate that all families on
     * the current connection are being migrated.
     *
     * @param host the host to which the client should migrate
     * @param cookie a login cookie for the given host
     * @param families a list of SNAC families supported by the given host, or
     *        <code>null</code> if all families supported by the current
     *        connection are supported
     */
    public MigrationNotice(String host, ByteBlock cookie, int[] families) {
        super(CMD_MIGRATE_PLS);

        this.families = (int[]) (families == null ? null : families.clone());
        this.host = host;
        this.cookie = cookie;
    }

    /**
     * Returns the host to which the client should migrate.
     *
     * @return the host to which the client should migrate
     */
    public final String getHost() { return host; }

    /**
     * Returns the login cookie to be used upon migrating (connecting) to the
     * new server.
     *
     * @return a login cookie for the migration destination server
     */
    public final ByteBlock getCookie() { return cookie; }

    /**
     * Returns the SNAC families supported by the host to which the client
     * should migrate, or <code>null</code> if all families on the current
     * connection are being migrated. If this is not <code>null</code>, the
     * current connection should <i>not</i> be dropped, as some families are
     * still supported.
     *
     * @return the migration host's supported SNAC families
     */
    public final int[] getFamilies() {
        return (int[]) (families == null ? null : families.clone());
    }

    public void writeData(OutputStream out) throws IOException {
        int len = families == null ? 0 : families.length;
        BinaryTools.writeUShort(out, len);
        if (families != null) {
            for (int i = 0; i < families.length; i++) {
                BinaryTools.writeUShort(out, families[i]);
            }
        }
        if (host != null) Tlv.getStringInstance(TYPE_HOST, host).write(out);
        if (cookie != null) new Tlv(TYPE_COOKIE, cookie).write(out);
    }

    public String toString() {
        return "MigrationNotice for " + (families == null ? -1
                : families.length) + " families to "
                + host + " (cookie length=" + cookie.getLength() + ")"; 
    }
}
