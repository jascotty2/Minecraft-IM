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
 *  File created by keith @ Feb 21, 2003
 *
 */

package net.kano.joscar.snaccmd.conn;

import net.kano.joscar.flapcmd.SnacCommand;
import net.kano.joscar.flapcmd.SnacPacket;
import net.kano.joscar.snac.CmdType;
import net.kano.joscar.snac.SnacCmdFactory;

/**
 * A SNAC command factory for the client-bound commands provided in this
 * package, appropriate for use by an AIM client.
 */
public class ClientConnCmdFactory implements SnacCmdFactory {
    /** A list of command types supported by this factory. */
    private static final CmdType[] SUPPORTED_TYPES = new CmdType[] {
        new CmdType(ConnCommand.FAMILY_CONN, ConnCommand.CMD_SERVER_READY),
        new CmdType(ConnCommand.FAMILY_CONN, ConnCommand.CMD_SERV_VERS),
        new CmdType(ConnCommand.FAMILY_CONN, ConnCommand.CMD_RATE_INFO),
        new CmdType(ConnCommand.FAMILY_CONN, ConnCommand.CMD_YOUR_INFO),
        new CmdType(ConnCommand.FAMILY_CONN, ConnCommand.CMD_WARNED),
        new CmdType(ConnCommand.FAMILY_CONN, ConnCommand.CMD_UPDATE),
        new CmdType(ConnCommand.FAMILY_CONN, ConnCommand.CMD_RATE_CHG),
        new CmdType(ConnCommand.FAMILY_CONN, ConnCommand.CMD_NOOP),
        new CmdType(ConnCommand.FAMILY_CONN, ConnCommand.CMD_SERVICE_REDIR),
        new CmdType(ConnCommand.FAMILY_CONN, ConnCommand.CMD_PAUSE),
        new CmdType(ConnCommand.FAMILY_CONN, ConnCommand.CMD_RESUME),
        new CmdType(ConnCommand.FAMILY_CONN, ConnCommand.CMD_MIGRATE_PLS),
        new CmdType(ConnCommand.FAMILY_CONN, ConnCommand.CMD_EXTRA_ACK),
        new CmdType(ConnCommand.FAMILY_CONN, ConnCommand.CMD_ENCINFOACK),
    };

    public CmdType[] getSupportedTypes() {
        return (CmdType[]) SUPPORTED_TYPES.clone();
    }

    public SnacCommand genSnacCommand(SnacPacket packet) {
        if (packet.getFamily() != ConnCommand.FAMILY_CONN) return null;

        int command = packet.getCommand();

        if (command == ConnCommand.CMD_SERVER_READY) {
            return new ServerReadyCmd(packet);
        } else if (command == ConnCommand.CMD_SERV_VERS) {
            return new ServerVersionsCmd(packet);
        } else if (command == ConnCommand.CMD_RATE_INFO) {
            return new RateInfoCmd(packet);
        } else if (command == ConnCommand.CMD_YOUR_INFO) {
            return new YourInfoCmd(packet);
        } else if (command == ConnCommand.CMD_WARNED) {
            return new WarningNotification(packet);
        } else if (command == ConnCommand.CMD_UPDATE) {
            return new UpdateAdvisory(packet);
        } else if (command == ConnCommand.CMD_RATE_CHG) {
            return new RateChange(packet);
        } else if (command == ConnCommand.CMD_NOOP) {
            return new Noop(packet);
        } else if (command == ConnCommand.CMD_SERVICE_REDIR) {
            return new ServiceRedirect(packet);
        } else if (command == ConnCommand.CMD_PAUSE) {
            return new PauseCmd(packet);
        } else if (command == ConnCommand.CMD_RESUME) {
            return new ResumeCmd(packet);
        } else if (command == ConnCommand.CMD_MIGRATE_PLS) {
            return new MigrationNotice(packet);
        } else if (command == ConnCommand.CMD_EXTRA_ACK) {
            return new ExtraInfoAck(packet);
        } else if (command == ConnCommand.CMD_ENCINFOACK) {
            return new EncryptionInfoAck(packet);
        } else {
            return null;
        }
    }
}
