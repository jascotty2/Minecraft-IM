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
 *  File created by keith @ Feb 26, 2003
 *
 */

package net.kano.joscar.snaccmd.rooms;

import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.flapcmd.SnacPacket;
import net.kano.joscar.snaccmd.MiniRoomInfo;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A SNAC command used to request more information about a chat room whose
 * {@linkplain MiniRoomInfo "mini room information"} is known. This request is
 * normally answered with a {@link RoomResponse}. This command can be used after
 * being invited to a chat room and receiving a <code>MiniRoomInfo</code> from
 * the associated {@link net.kano.joscar.rvcmd.chatinvite.ChatInvitationRvCmd}.
 *
 * @snac.src client
 * @snac.cmd 0x0d 0x04
 *
 * @author Stephen Flynn
 */
public class RoomInfoReq extends RoomCommand {
    /** The miniature room information block contained in this command. */
    private MiniRoomInfo roomInfo;

    /**
     * Generates a more-room-information command from the given incoming SNAC
     * packet.
     *
     * @param packet an incoming room more info request packet
     */
    protected RoomInfoReq(SnacPacket packet) {
        super(CMD_MORE_ROOM_INFO);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

        roomInfo = MiniRoomInfo.readMiniRoomInfo(snacData);
    }

    /**
     * Creates a new outgoing room-more-info request for the room described by
     * the given miniature room information block.
     *
     * @param roomInfo a miniature room information block describing the room
     *        whose information is being requested
     */
    public RoomInfoReq(MiniRoomInfo roomInfo) {
        super(CMD_MORE_ROOM_INFO);

        this.roomInfo = roomInfo;
    }

    public void writeData(OutputStream out) throws IOException {
        if (roomInfo != null) roomInfo.write(out);
    }

    public String toString() {
        return "RoomInfoReq: " + roomInfo;
    }
}