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
import net.kano.joscar.flapcmd.SnacPacket;
import net.kano.joscar.snaccmd.ExtraInfoBlock;
import net.kano.joscar.snaccmd.FullUserInfo;
import net.kano.joscar.tlv.Tlv;
import net.kano.joscar.tlv.TlvChain;
import net.kano.joscar.tlv.TlvTools;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A SNAC command containing an IM.
 *
 * @snac.src server
 * @snac.cmd 0x04 0x07
 *
 * @see SendImIcbm
 */
public class RecvImIcbm extends AbstractImIcbm {
    /** A TLV type present if the sender supports typing notification. */
    private static final int TYPE_CAN_TYPE = 0x000b;

    /** Whether the sender supports typing notification. */
    private final boolean canType;
    /** Information about the sender of this IM. */
    private final FullUserInfo userInfo;

    /**
     * Generates a new incoming IM ICBM command from the given incoming SNAC
     * packet.
     *
     * @param packet an incoming IM ICBM packet
     */
    protected RecvImIcbm(SnacPacket packet) {
        super(IcbmCommand.CMD_ICBM, packet);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = getChannelData();

        userInfo = FullUserInfo.readUserInfo(snacData);

        ByteBlock tlvBlock = snacData.subBlock(userInfo.getTotalSize());

        TlvChain chain = TlvTools.readChain(tlvBlock);

        processImTlvs(chain);

        canType = chain.hasTlv(TYPE_CAN_TYPE);
    }

    /**
     * Creates a new outgoing client-bound IM ICBM command with the given
     * properties.
     *
     * @param messageId the ICBM message ID to associate with this message
     * @param userInfo a user information block for the sender of this IM
     * @param message the instant message
     * @param autoResponse whether this message is an auto-response
     * @param wantsIcon whether the sender wants the receiver's buddy icon
     * @param iconInfo a set of icon information provided by the sender, or
     *        <code>null</code> if none was provided
     * @param canType whether or not the sender supports typing notification
     * @param expInfoBlocks a list of AIM Expression information blocks
     */
    public RecvImIcbm(long messageId, FullUserInfo userInfo,
            InstantMessage message, boolean autoResponse, boolean wantsIcon,
            OldIconHashInfo iconInfo, ExtraInfoBlock[] expInfoBlocks,
            boolean canType) {
        super(IcbmCommand.CMD_ICBM, messageId, message, autoResponse,
                wantsIcon, iconInfo, expInfoBlocks);

        DefensiveTools.checkNull(userInfo, "userInfo");

        this.canType = canType;
        this.userInfo = userInfo;
    }

    /**
     * Returns a user information block containing information about the sender
     * of this IM.
     *
     * @return a user information block for the sender of this IM
     */
    public final FullUserInfo getSenderInfo() {
        return userInfo;
    }

    /**
     * Returns whether or not the sender supports {@linkplain
     * SendTypingNotification typing notification}.
     *
     * @return whether the sender supports typing notification
     */
    public final boolean canType() {
        return canType;
    }

    protected final void writeChannelData(OutputStream out) throws IOException {
        userInfo.write(out);
        if (canType) new Tlv(TYPE_CAN_TYPE).write(out);

        writeImTlvs(out);
    }

    public String toString() {
        return "RecvImIcbm: message from " + userInfo.getScreenname() + ": "
                + getMessage();
    }
}
