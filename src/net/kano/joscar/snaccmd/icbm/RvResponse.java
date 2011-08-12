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
 *  File created by keith @ Mar 28, 2003
 *
 */

package net.kano.joscar.snaccmd.icbm;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.OscarTools;
import net.kano.joscar.StringBlock;
import net.kano.joscar.flapcmd.SnacPacket;
import net.kano.joscar.tlv.ImmutableTlvChain;
import net.kano.joscar.tlv.TlvChain;
import net.kano.joscar.tlv.TlvTools;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A SNAC command indicating that a rendezvous failed. Note that this is always
 * sent by WinAIM after a "Send Buddy List" command is sent to it, whether or
 * not the user accepts or declines the Send Buddy List.
 *
 * @snac.src client
 * @snac.cmd 0x04 0x0b
 */
public class RvResponse extends IcbmCommand {
    /**
     * A code indicating that a rendezvous type is not supported by the client.
     */
    public static final int CODE_NOT_SUPPORTED = 0x0000;
    /** A code indicating that a rendezvous was declined. */
    public static final int CODE_DECLINED = 0x0001;
    /**
     * A code indicating that a client is not accepting requests of the given
     * type.
     */
    public static final int CODE_NOT_ACCEPTING = 0x0002;

    /** A TLV type containing a result code. */
    private static final int TYPE_RESULT_CODE = 0x0003;

    /** The rendezvous session of the rendezvous being responded-to. */
    private final long rvSessionId;
    /** The channel on which the rendezvous was sent. */
    private final int channel;
    /**
     * The screenname whose rendezvous request is being denied or of the user
     * who denied an outgoing rendezvous we sent.
     */
    private final String sn;
    /** A result code. */
    private final int code;

    /**
     * Creates a new rendezvous response command from the given incoming SNAC
     * packet.
     *
     * @param packet an incoming rendezvous response packet
     */
    protected RvResponse(SnacPacket packet) {
        super(CMD_RV_RESPONSE);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

        rvSessionId = BinaryTools.getLong(snacData, 0);
        channel = BinaryTools.getUShort(snacData, 8);

        ByteBlock snData = snacData.subBlock(10);
        StringBlock snInfo = OscarTools.readScreenname(snData);
        sn = snInfo.getString();

        ByteBlock rest = snData.subBlock(snInfo.getTotalSize());

        TlvChain chain = TlvTools.readChain(rest);

        code = chain.getUShort(TYPE_RESULT_CODE);
    }

    /**
     * Creates a new outgoing rendezvous error command with the given
     * properties.
     *
     * @param rvSessionId the rendezvous session IDof the rendezvous being 
     *        responded-to
     * @param icbmChannel the ICBM channel on which the original rendezvous was
     *        sent
     * @param sn the screenname whose rendezvous is being responded-to
     * @param resultCode a result code, like {@link #CODE_NOT_SUPPORTED}
     */
    public RvResponse(long rvSessionId, int icbmChannel, String sn,
            int resultCode) {
        super(CMD_RV_RESPONSE);

        DefensiveTools.checkRange(icbmChannel, "icbmChannel", 0);
        DefensiveTools.checkNull(sn, "sn");
        DefensiveTools.checkRange(resultCode, "resultCode", -1);

        this.rvSessionId = rvSessionId;
        this.channel = icbmChannel;
        this.sn = sn;
        this.code = resultCode;
    }

    /**
     * Returns the rendezvous session ID of the rendezvous being responded-to.
     * Note that this value seems to always be <code>0</code> when coming from
     * a Windows AIM client.
     *
     * @return the failed rendezvous's RV session ID
     */
    public final long getRvSessionId() { return rvSessionId; }

    /**
     * Returns the ICBM channel on which the rendezvous to which this command is
     * a response was received.
     *
     * @return the rendezvous's ICBM channel
     */
    public final int getChannel() { return channel; }

    /**
     * Returns the screenname of the user whose rendezvous is being
     * responded-to, or that of the user who is responding to a rendezvous we
     * sent, depending on whether this is an outgoing or incoming response.
     *
     * @return the screenname of the user whose rendezvous is being responded-to
     *         or that of the user responding to a rendezvous we sent
     */
    public final String getScreenname() { return sn; }

    /**
     * Returns the associated error code. Normally one of {@link
     * #CODE_DECLINED}, {@link #CODE_NOT_ACCEPTING}, {@link
     * #CODE_NOT_SUPPORTED}.
     *
     * @return the result code associated with this rendezvous error
     */
    public final int getResultCode() { return code; }

    public void writeData(OutputStream out) throws IOException {
        BinaryTools.writeLong(out, rvSessionId);
        BinaryTools.writeUShort(out, channel);
        OscarTools.writeScreenname(out, sn);
        BinaryTools.writeUShort(out, code);
    }

    public String toString() {
        return "RvResponse: sn=" + sn + ": icbmChannel=" + channel + ", code=0x"
                + Integer.toHexString(code);
    }
}
