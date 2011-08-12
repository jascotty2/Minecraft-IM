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

import net.kano.joscar.flapcmd.SnacCommand;
import net.kano.joscar.flapcmd.SnacPacket;
import net.kano.joscar.snac.CmdType;
import net.kano.joscar.snac.SnacCmdFactory;

/**
 * A SNAC command factory for the server-bound commands provided in this
 * package, appropriate for use by an AIM server.
 */
public class ServerConnCmdFactory implements SnacCmdFactory {
    /** The supported SNAC command types. */
    private static final CmdType[] SUPPORTED_TYPES = new CmdType[] {
        new CmdType(ConnCommand.FAMILY_CONN, ConnCommand.CMD_CLIENT_VERS),
        new CmdType(ConnCommand.FAMILY_CONN, ConnCommand.CMD_RATE_REQ),
        new CmdType(ConnCommand.FAMILY_CONN, ConnCommand.CMD_RATE_ACK),
        new CmdType(ConnCommand.FAMILY_CONN, ConnCommand.CMD_MY_INFO_REQ),
        new CmdType(ConnCommand.FAMILY_CONN, ConnCommand.CMD_CLIENT_READY),
        new CmdType(ConnCommand.FAMILY_CONN, ConnCommand.CMD_NOOP),
        new CmdType(ConnCommand.FAMILY_CONN, ConnCommand.CMD_SERVICE_REQ),
        new CmdType(ConnCommand.FAMILY_CONN, ConnCommand.CMD_PAUSE_ACK),
        new CmdType(ConnCommand.FAMILY_CONN, ConnCommand.CMD_SET_IDLE),
        new CmdType(ConnCommand.FAMILY_CONN, ConnCommand.CMD_SETEXTRAINFO),
        new CmdType(ConnCommand.FAMILY_CONN, ConnCommand.CMD_SETENCINFO),
    };

    public CmdType[] getSupportedTypes() {
        return (CmdType[]) SUPPORTED_TYPES.clone();
    }

    public SnacCommand genSnacCommand(SnacPacket packet) {
        if (packet.getFamily() != ConnCommand.FAMILY_CONN) return null;

        int command = packet.getCommand();

        if (command == ConnCommand.CMD_CLIENT_VERS) {
            return new ClientVersionsCmd(packet);
        } else if (command == ConnCommand.CMD_RATE_REQ) {
            return new RateInfoRequest(packet);
        } else if (command == ConnCommand.CMD_RATE_ACK) {
            return new RateAck(packet);
        } else if (command == ConnCommand.CMD_MY_INFO_REQ) {
            return new MyInfoRequest(packet);
        } else if (command == ConnCommand.CMD_CLIENT_READY) {
            return new ClientReadyCmd(packet);
        } else if (command == ConnCommand.CMD_NOOP) {
            return new Noop(packet);
        } else if (command == ConnCommand.CMD_SERVICE_REQ) {
            return new ServiceRequest(packet);
        } else if (command == ConnCommand.CMD_PAUSE_ACK) {
            return new PauseAck(packet);
        } else if (command == ConnCommand.CMD_SET_IDLE) {
            return new SetIdleCmd(packet);
        } else if (command == ConnCommand.CMD_SETEXTRAINFO) {
            return new SetExtraInfoCmd(packet);
        } else if (command == ConnCommand.CMD_SETENCINFO) {
            return new SetEncryptionInfoCmd(packet);
        } else {
            return null;
        }
    }
}
