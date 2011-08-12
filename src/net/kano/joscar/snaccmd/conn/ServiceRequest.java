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
import net.kano.joscar.snaccmd.MiniRoomInfo;
import net.kano.joscar.snaccmd.chat.ChatCommand;
import net.kano.joscar.tlv.ImmutableTlvChain;
import net.kano.joscar.tlv.Tlv;
import net.kano.joscar.tlv.TlvChain;
import net.kano.joscar.tlv.TlvTools;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A command sent to request an OSCAR server that supports a particular SNAC
 * family. Normally responded-to with a {@link ServiceRedirect}.
 *
 * @snac.src client
 * @snac.cmd 0x01 0x04
 *
 * @see ServiceRedirect
 */
public class ServiceRequest extends ConnCommand {
    /** A TLV type containing miniature chat room information. */
    private static final int TYPE_ROOM_INFO = 0x0001;

    /** The SNAC family being requested. */
    private final int family;
    /** The chat room being joined, if any. */
    private final MiniRoomInfo roomInfo;

    /**
     * Generates a new service request command from the given incoming SNAC
     * packet.
     *
     * @param packet the incoming service request packet
     */
    protected ServiceRequest(SnacPacket packet) {
        super(CMD_SERVICE_REQ);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

        family = BinaryTools.getUShort(snacData, 0);

        ByteBlock tlvBlock = snacData.subBlock(2);

        TlvChain chatChain = TlvTools.readChain(tlvBlock);

        Tlv chatInfoTlv = chatChain.getLastTlv(TYPE_ROOM_INFO);

        if (chatInfoTlv != null) {
            ByteBlock chatBlock = chatInfoTlv.getData();

            roomInfo = MiniRoomInfo.readMiniRoomInfo(chatBlock);
        } else {
            roomInfo = null;
        }
    }

    /**
     * Creates a new service request command requesting the given SNAC family.
     *
     * @param snacFamily the SNAC family to request
     */
    public ServiceRequest(int snacFamily) {
        this(snacFamily, null);
    }

    /**
     * Creates a new service request for the {@link ChatCommand#FAMILY_CHAT}
     * family with the given chat room information block. Joining a chat room
     * is simply a service request. See {@link
     * net.kano.joscar.snaccmd.rooms.JoinRoomCmd} and {@link
     * net.kano.joscar.snaccmd.rooms.RoomResponse} for details.
     * <br>
     * <br>
     * Using this constructor is equivalent to using {@link #ServiceRequest(int,
     *  MiniRoomInfo) new ServiceRequest(ChatCommand.FAMILY_CHAT, roomInfo)}.
     *
     * @param roomInfo the room information block for the room being joined
     */
    public ServiceRequest(MiniRoomInfo roomInfo) {
        this(ChatCommand.FAMILY_CHAT, roomInfo);
    }

    /**
     * Creates a new service request command requesting the given SNAC family
     * and providing the given chat room information block. While
     * <code>snacFamily</code> should normally be {@link
     * ChatCommand#FAMILY_CHAT} if a room information block is sent, if you
     * really want to you can make it whatever you want.
     *
     * @param snacFamily the SNAC family being requested
     * @param roomInfo a chat room information block representing the room being
     *        joined
     */
    public ServiceRequest(int snacFamily, MiniRoomInfo roomInfo) {
        super(CMD_SERVICE_REQ);

        DefensiveTools.checkRange(snacFamily, "snacFamily", 0);

        this.family = snacFamily;
        this.roomInfo = roomInfo;
    }

    /**
     * Returns the SNAC family requested in this service request.
     *
     * @return the requested SNAC family
     */
    public final int getRequestedFamily() {
        return family;
    }

    /**
     * Returns the chat room information block sent with this request, or
     * <code>null</code> if none was sent.
     *
     * @return the associated chat room information block
     */
    public final MiniRoomInfo getRoomInfo() {
        return roomInfo;
    }

    public void writeData(OutputStream out) throws IOException {
        BinaryTools.writeUShort(out, family);

        if (roomInfo != null) {
            ByteBlock roomInfoBlock = ByteBlock.createByteBlock(roomInfo);
            new Tlv(TYPE_ROOM_INFO, roomInfoBlock).write(out);
        }
    }

    public String toString() {
        return "ServiceRequest for family 0x" + Integer.toHexString(family)
                + (roomInfo == null ? "" : " (chat room: <" + roomInfo + ">)");
    }
}
