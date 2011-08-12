/*
 *  Copyright (c) 2003, The Joust Project
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
 *  File created by Keith @ 3:58:22 AM
 *
 */

package net.kano.joscar.rvcmd.voice;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.rvcmd.AbstractRequestRvCmd;
import net.kano.joscar.rvcmd.RvConnectionInfo;
import net.kano.joscar.snaccmd.CapabilityBlock;
import net.kano.joscar.snaccmd.icbm.RecvRvIcbm;
import net.kano.joscar.tlv.TlvChain;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A rendezvous command used to request a voice chat session.
 */
public class VoiceReqRvCmd extends AbstractRequestRvCmd {
    /** The voice chat protocol version used by WinAIM. */
    public static final long VERSION_DEFAULT = 0x00000001L;

    /** The voice chat protocol version sent in this command. */
    private final long version;
    /** The connection information block. */
    private final RvConnectionInfo connInfo;

    /**
     * Creates a new voice chat request command from the given incoming voice
     * chat request RV ICBM.
     *
     * @param icbm an incoming voice chat request RV ICBM command
     */
    public VoiceReqRvCmd(RecvRvIcbm icbm) {
        super(icbm);

        TlvChain chain = getRvTlvs();

        connInfo = RvConnectionInfo.readConnectionInfo(chain);

        ByteBlock data = getServiceData();
        if (data != null) {
            version = BinaryTools.getUInt(data, 0);
        } else {
            version = -1;
        }
    }

    /**
     * Creates a new outgoing voice chat request with the given connection
     * information block and a protocol version number of {@link
     * #VERSION_DEFAULT}.
     * <br>
     * <br>
     * Using this constructor is equivalent to using {@link #VoiceReqRvCmd(long,
     * RvConnectionInfo) new VoiceReqRvCmd(VERSION_DEFAULT, connInfo)}.
     *
     * @param connInfo a block of connection information
     */
    public VoiceReqRvCmd(RvConnectionInfo connInfo) {
        this(VERSION_DEFAULT, connInfo);
    }

    /**
     * Creates a new outgoing voice chat request with the given protocol version
     * and the given connection information block.
     *
     * @param version a protocol version; normally {@link #VERSION_DEFAULT}
     * @param connInfo a connection information block, or <code>null</code> to
     *        not send any connection information in this command
     */
    public VoiceReqRvCmd(long version, RvConnectionInfo connInfo) {
        super(CapabilityBlock.BLOCK_VOICE);

        DefensiveTools.checkRange(version, "version", -1);

        this.connInfo = connInfo;
        this.version = version;
    }

    /**
     * Returns the voice chat protocol version sent in this command, or
     * <code>-1</code> if none was sent. This value is normally {@link
     * #VERSION_DEFAULT}.
     *
     * @return the voice chat protocol version sent in this command, or
     *         <code>-1</code> if none was sent
     */
    public final long getVersion() { return version; }

    /**
     * Returns the connection information block sent in this command. Note that
     * this method will never return <code>null</code>; the returned object's
     * fields will simply be empty if no connection information information
     * was sent.
     *
     * @return an object containing the connection information sent in this
     *         command
     */
    public final RvConnectionInfo getConnInfo() { return connInfo; }

    protected void writeRvTlvs(OutputStream out) throws IOException {
        if (connInfo != null) connInfo.write(out);
    }

    protected boolean hasServiceData() { return version != -1; }

    protected void writeServiceData(OutputStream out) throws IOException {
        if (version != -1) BinaryTools.writeUInt(out, version);
    }

    public String toString() {
        return "VoiceReqRvCmd: version=" + version + ", connInfo=" + connInfo;
    }
}