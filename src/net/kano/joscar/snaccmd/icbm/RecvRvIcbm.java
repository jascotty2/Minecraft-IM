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
import net.kano.joscar.flapcmd.SnacPacket;
import net.kano.joscar.snaccmd.CapabilityBlock;
import net.kano.joscar.snaccmd.FullUserInfo;
import net.kano.joscar.tlv.ImmutableTlvChain;
import net.kano.joscar.tlv.TlvChain;
import net.kano.joscar.tlv.TlvTools;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A SNAC command containing rendezvous information.
 *
 * @snac.src server
 * @snac.cmd 0x01 0x07
 *
 * @see SendRvIcbm
 */
public class RecvRvIcbm extends AbstractRvIcbm {
    /** A block describing the sender of this rendezvous ICBM. */
    private final FullUserInfo sender;

    /**
     * Generates an incoming rendezvous ICBM command from the given incoming
     * SNAC packet.
     *
     * @param packet an incoming rendezvous ICBM packet
     */
    protected RecvRvIcbm(SnacPacket packet) {
        super(IcbmCommand.CMD_ICBM, packet);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock channelData = getChannelData();

        sender = FullUserInfo.readUserInfo(channelData);
        ByteBlock tlvBlock = channelData.subBlock(sender.getTotalSize());
        TlvChain chain = TlvTools.readChain(tlvBlock);
        processRvTlvs(chain);
    }

    /**
     * Creates a new outgoing client-bound ICBM with the given properties.
     * 
     * @param icbmMessageId an ICBM message ID to associate with this rendezvous
     *        command
     * @param status a status code, like {@link #RVSTATUS_REQUEST}
     * @param rvSessionId a rendezvous session ID on which this rendezvous
     *        exists
     * @param cap this rendezvous's associated capability block
     * @param rvDataWriter an object used to write the rendezvous-specific data
     * @param sender an object describing the user who sent this rendezvous
     */
    public RecvRvIcbm(long icbmMessageId, int status, long rvSessionId,
            CapabilityBlock cap, LiveWritable rvDataWriter,
            FullUserInfo sender) {
        super(IcbmCommand.CMD_ICBM, icbmMessageId, status, rvSessionId, cap,
                rvDataWriter);

        DefensiveTools.checkNull(sender, "sender");

        this.sender = sender;
    }

    /**
     * Returns a user information block for the user who sent this rendezvous.
     *
     * @return user information for the sender of this rendezvous
     */
    public final FullUserInfo getSender() { return sender; }

    protected final void writeChannelData(OutputStream out)
            throws IOException {
        sender.write(out);
        writeRvTlvs(out);
    }

    public String toString() {
        return "RecvRvIcbm: sender=<" + sender + ">, on top of "
                + super.toString();
    }
}
