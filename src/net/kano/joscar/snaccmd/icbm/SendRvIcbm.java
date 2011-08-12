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

package net.kano.joscar.snaccmd.icbm;

import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.LiveWritable;
import net.kano.joscar.OscarTools;
import net.kano.joscar.StringBlock;
import net.kano.joscar.flapcmd.SnacPacket;
import net.kano.joscar.snaccmd.CapabilityBlock;
import net.kano.joscar.tlv.ImmutableTlvChain;
import net.kano.joscar.tlv.TlvChain;
import net.kano.joscar.tlv.TlvTools;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A SNAC command used to send a rendezvous command to another user.
 *
 * @snac.src client
 * @snac.cmd 0x04 0x06
 *
 * @see RecvRvIcbm
 */
public class SendRvIcbm extends AbstractRvIcbm {
    /** The screenname of the recipient of this rendezvous command. */
    private final String sn;

    /**
     * Generates a new send-rendezvous command from the given incoming SNAC
     * packet.
     *
     * @param packet an incoming send-rendezvous packet
     */
    protected SendRvIcbm(SnacPacket packet) {
        super(IcbmCommand.CMD_SEND_ICBM, packet);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock channelData = getChannelData();

        StringBlock snInfo = OscarTools.readScreenname(channelData);
        sn = snInfo.getString();

        ByteBlock tlvBlock = channelData.subBlock(snInfo.getTotalSize());
        TlvChain chain = TlvTools.readChain(tlvBlock);
        processRvTlvs(chain);
    }

    /**
     * Creates a new outgoing rendezvous command with the given properties.
     *
     * @param sn the screenname to whom to send this rendezvous
     * @param icbmMessageId an ICBM message ID to attach to this command
     * @param status a status code, like {@link #RVSTATUS_REQUEST}
     * @param rvSessionId the ID of the rendezvous session on which this
     *        rendezvous is being sent
     * @param cap the capability block associated with this rendezvous command
     * @param rvDataWriter an object used to write the rendezvous-specific
     *        data to the connection
     */
    public SendRvIcbm(String sn, long icbmMessageId, int status,
            long rvSessionId, CapabilityBlock cap, LiveWritable rvDataWriter) {
        super(IcbmCommand.CMD_SEND_ICBM, icbmMessageId, status, rvSessionId,
                cap, rvDataWriter);

        DefensiveTools.checkNull(sn, "sn");

        this.sn = sn;
    }

    /**
     * Creates a new outgoing rendezvous to the given user with the properties
     * given by the given <code>RvCommand</code>.
     *
     * @param sn the screenname to whom this rendezvous command is being sent
     * @param icbmMessageId an ICBM message ID for this RV ICBM
     * @param rvSessionId a rendezvous session ID on which this rendezvous
     *        exists
     * @param command a rendezvous command that will be used to create this
     *        rendezvous packet
     */
    public SendRvIcbm(String sn, long icbmMessageId, long rvSessionId,
            RvCommand command) {
        super(IcbmCommand.CMD_SEND_ICBM, icbmMessageId, rvSessionId, command);

        DefensiveTools.checkNull(sn, "sn");

        this.sn = sn;
    }

    /**
     * Returns the screenname of the user to whom this rendezvous is addressed.
     *
     * @return the receiver's screenname
     */
    public final String getScreenname() { return sn; }

    protected final void writeChannelData(OutputStream out)
            throws IOException {
        OscarTools.writeScreenname(out, sn);
        
        writeRvTlvs(out);
    }
}
