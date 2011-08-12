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
 *  File created by keith @ Feb 27, 2003
 *
 */

package net.kano.joscar.snaccmd.chat;

import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.flapcmd.SnacPacket;
import net.kano.joscar.snaccmd.FullRoomInfo;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A SNAC command containing various information about a chat room.
 *
 * @snac.src server
 * @snac.cmd 0x0e 0x02
 */
public class RoomInfoUpdate extends ChatCommand {
    /** The room information block sent in this room update. */
    private final FullRoomInfo roomInfo;

    /**
     * Creates a new room information update command from the given incoming
     * SNAC packet.
     *
     * @param packet a room information update packet
     */
    protected RoomInfoUpdate(SnacPacket packet) {
        super(CMD_ROOM_UPDATE);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

        roomInfo = FullRoomInfo.readRoomInfo(snacData);
    }

    /**
     * Creates a new outgoing room information command with the given room
     * information.
     *
     * @param roomInfo the room information being updated
     */
    public RoomInfoUpdate(FullRoomInfo roomInfo) {
        super(CMD_ROOM_UPDATE);

        DefensiveTools.checkNull(roomInfo, "roomInfo");

        this.roomInfo = roomInfo;
    }

    /**
     * Returns the room information contained in this room information update
     * command.
     *
     * @return the updated room information
     */
    public final FullRoomInfo getRoomInfo() {
        return roomInfo;
    }

    public void writeData(OutputStream out) throws IOException {
        roomInfo.write(out);
    }

    public String toString() {
        return "RoomInfoUpdate for " + roomInfo;
    }
}
