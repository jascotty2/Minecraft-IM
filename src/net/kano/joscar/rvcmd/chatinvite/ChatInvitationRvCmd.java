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
 *  File created by keith @ Apr 27, 2003
 *
 */

package net.kano.joscar.rvcmd.chatinvite;

import net.kano.joscar.ByteBlock;
import net.kano.joscar.rvcmd.AbstractRequestRvCmd;
import net.kano.joscar.rvcmd.InvitationMessage;
import net.kano.joscar.snaccmd.CapabilityBlock;
import net.kano.joscar.snaccmd.MiniRoomInfo;
import net.kano.joscar.snaccmd.icbm.RecvRvIcbm;
import net.kano.joscar.tlv.Tlv;
import net.kano.joscar.tlv.TlvChain;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A rendezvous command used to invite a user to a ("secure" or normal) chat
 * room.
 */
public class ChatInvitationRvCmd extends AbstractRequestRvCmd {
    /**
     * A TLV type present if the chat room to which the recipient is being
     * invited is a "secure chat room."
     */
    private static final int TYPE_SECURE = 0x0011;
    /** A TLV type containing a key for the secure chat room, if any. */
    private static final int TYPE_SECURITYINFO = 0x2713;

    /** The chat invitation message. */
    private final InvitationMessage invMessage;
    /** A room information block containing information about the chat room. */
    private final MiniRoomInfo roomInfo;

    /** The block of "security info" sent in this invitation. */
    private final ByteBlock securityInfo;

    /**
     * Creates a new chat room invitation RV command from the given incoming
     * chat room invitation RV ICBM command.
     *
     * @param icbm an incoming chat room invitation rendezvous ICBM command
     */
    public ChatInvitationRvCmd(RecvRvIcbm icbm) {
        super(icbm);

        TlvChain chain = getRvTlvs();

        invMessage = InvitationMessage.readInvitationMessage(chain);

        Tlv securityInfoTlv = chain.getLastTlv(TYPE_SECURITYINFO);
        if (securityInfoTlv != null) securityInfo = securityInfoTlv.getData();
        else securityInfo = null;

        ByteBlock serviceData = getServiceData();
        if (serviceData == null) roomInfo = null;
        else roomInfo = MiniRoomInfo.readMiniRoomInfo(serviceData);
    }

    /**
     * Creates a new outgoing chat room invitation command for the room
     * described in the given block and with the given invitation message. Note
     * that if <code>message</code> is <code>null</code>, no invitation message
     * will be sent with this invitation.
     * <br>
     * <br>
     * Note that the object passed as the <code>roomInfo</code> argument should
     * be retrieved from the server's {@link
     * net.kano.joscar.snaccmd.rooms.RoomResponse} command's room information
     * block (although the same information is provided in other places as
     * well). See {@link net.kano.joscar.snaccmd.rooms.RoomResponse#getRoomInfo}
     * and {@link
     * MiniRoomInfo#MiniRoomInfo(net.kano.joscar.snaccmd.FullRoomInfo)} for
     * details.
     *
     * @param roomInfo a room information block describing the chat room to
     *        which the recipient is being invited
     * @param message an invitation message to send with this invitation, or
     *        <code>null</code> for none
     *
     * @see net.kano.joscar.snaccmd.rooms.RoomResponse
     */
    public ChatInvitationRvCmd(MiniRoomInfo roomInfo,
            InvitationMessage message) {
        this(roomInfo, message, null);
    }

    /**
     * Creates a new outgoing chat room invitation command for the room
     * described in the given block and with the given invitation message. Note
     * that if <code>message</code> is <code>null</code>, no invitation message
     * will be sent with this invitation.
     * <br>
     * <br>
     * Note that the object passed as the <code>roomInfo</code> argument should
     * be retrieved from the server's {@link
     * net.kano.joscar.snaccmd.rooms.RoomResponse} command's room information
     * block (although the same information is provided in other places as
     * well). See {@link net.kano.joscar.snaccmd.rooms.RoomResponse#getRoomInfo}
     * and {@link
     * MiniRoomInfo#MiniRoomInfo(net.kano.joscar.snaccmd.FullRoomInfo)} for
     * details.
     *
     * @param roomInfo a room information block describing the chat room to
     *        which the recipient is being invited
     * @param message an invitation message to send with this invitation, or
     *        <code>null</code> for none
     * @param securityInfo a block of secure chat room information
     *
     * @see net.kano.joscar.snaccmd.rooms.RoomResponse
     */
    public ChatInvitationRvCmd(MiniRoomInfo roomInfo,
            InvitationMessage message, ByteBlock securityInfo) {
        super(CapabilityBlock.BLOCK_CHAT);

        this.roomInfo = roomInfo;
        this.invMessage = message;
        this.securityInfo = securityInfo;
    }

    /**
     * Returns the invitation message, if any, included in this invitation.
     *
     * @return the invitation message sent in this invitation, or
     *         <code>null</code> if none was included
     */
    public final InvitationMessage getInvMessage() { return invMessage; }

    /**
     * Returns the block of security information sent in this chat room
     * invitation, if any.
     *
     * @return the block of security information associated with the chat room
     *         to which the recipient is being invited
     */
    public final ByteBlock getSecurityInfo() { return securityInfo; }

    /**
     * Returns the room information block describing the chat room to which
     * the recipient is being invited. The returned room information block can
     * be passed directly to a {@linkplain
     * net.kano.joscar.snaccmd.conn.ServiceRequest#ServiceRequest(MiniRoomInfo)
     * service request}. Note that this method will return <code>null</code> if,
     * for some reason, no room information block was sent. (This is, however,
     * not normal behavior.)
     *
     * @return a chat room information block describing the chat room to which
     *         the recipient of this invitation is being invited (or
     *         <code>null</code> if none was sent)
     */
    public final MiniRoomInfo getRoomInfo() { return roomInfo; }

    protected void writeRvTlvs(OutputStream out) throws IOException {
        if (invMessage != null) {
            invMessage.write(out);
        }
        if (securityInfo != null) {
            new Tlv(TYPE_SECURE).write(out);
            new Tlv(TYPE_SECURITYINFO, securityInfo).write(out);
        }
    }

    protected void writeServiceData(OutputStream out) throws IOException {
        if (roomInfo != null) roomInfo.write(out);
    }

    public String toString() {
        return "ChatInvitationRvCmd: "
                + (securityInfo != null ? "(secure) " : "" )
                + "roomInfo=<" + roomInfo + ">: " + invMessage;
    }
}
