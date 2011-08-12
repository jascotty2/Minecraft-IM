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
import net.kano.joscar.snaccmd.FullRoomInfo;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A SNAC command used to join or create a chat room whose name is already
 * known. This command should normally be used when joining a room to which one
 * has not been already invited. The response to this command, normally a {@link
 * RoomResponse}, contains a room information block and a cookie, which can then
 * be passed to a {@link net.kano.joscar.snaccmd.conn.ServiceRequest} which will
 * then provide you with a connection to the server on which the requested chat
 * room resides.
 *
 * @snac.src client
 * @snac.cmd 0x0d 0x08
 *
 * @see RoomResponse
 * @see net.kano.joscar.snaccmd.conn.ServiceRequest
 */
public class JoinRoomCmd extends RoomCommand {
    /** A set of room information for the room attempting to be joined. */
    private final FullRoomInfo roomInfo;

    /**
     * Generates a join-room command from the given incoming SNAC packet.
     *
     * @param packet the incoming room join request packet
     */
    protected JoinRoomCmd(SnacPacket packet) {
        super(CMD_JOIN_ROOM);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

        roomInfo = FullRoomInfo.readRoomInfo(snacData);
    }

    /**
     * Creates a new outgoing join-room request for the room described by the
     * given room information block.
     *
     * @param roomInfo a room information block describing the room to be joined
     */
    public JoinRoomCmd(FullRoomInfo roomInfo) {
        super(CMD_JOIN_ROOM);

        this.roomInfo = roomInfo;
    }

    /**
     * Returns the room information block describing the room attempting to be
     * joined.
     *
     * @return the room information block for the room being joined
     */
    public final FullRoomInfo getRoomInfo() {
        return roomInfo;
    }

    public void writeData(OutputStream out) throws IOException {
        if (roomInfo != null) roomInfo.write(out);
    }

    public String toString() {
        return "JoinRoomCmd: " + roomInfo;
    }
}
