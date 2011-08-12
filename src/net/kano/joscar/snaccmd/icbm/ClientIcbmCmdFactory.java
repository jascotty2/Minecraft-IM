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

import net.kano.joscar.flapcmd.SnacCommand;
import net.kano.joscar.flapcmd.SnacPacket;
import net.kano.joscar.snac.CmdType;
import net.kano.joscar.snac.SnacCmdFactory;
import net.kano.joscar.snaccmd.AbstractIcbm;

/**
 * A SNAC command factory for the client-bound commands provided in this
 * package, appropriate for use by an AIM client.
 */
public class ClientIcbmCmdFactory implements SnacCmdFactory {
    /** The supported SNAC command types. */
    private static final CmdType[] SUPPORTED_TYPES = new CmdType[] {
        new CmdType(IcbmCommand.FAMILY_ICBM, IcbmCommand.CMD_PARAM_INFO),
        new CmdType(IcbmCommand.FAMILY_ICBM, IcbmCommand.CMD_ICBM),
        new CmdType(IcbmCommand.FAMILY_ICBM, IcbmCommand.CMD_RECV_TYPING),
        new CmdType(IcbmCommand.FAMILY_ICBM, IcbmCommand.CMD_MISSED),
        new CmdType(IcbmCommand.FAMILY_ICBM, IcbmCommand.CMD_MSG_ACK),
        new CmdType(IcbmCommand.FAMILY_ICBM, IcbmCommand.CMD_RV_RESPONSE),
    };

    public CmdType[] getSupportedTypes() {
        return (CmdType[]) SUPPORTED_TYPES.clone();
    }

    public SnacCommand genSnacCommand(SnacPacket packet) {
        if (packet.getFamily() != IcbmCommand.FAMILY_ICBM) return null;

        int command = packet.getCommand();

        if (command == IcbmCommand.CMD_PARAM_INFO) {
            return new ParamInfoCmd(packet);
        } else if (command == IcbmCommand.CMD_ICBM) {
            int channel = AbstractIcbm.getIcbmChannel(packet);

            if (channel == AbstractIcbm.CHANNEL_IM) {
                return new RecvImIcbm(packet);
            } else if (channel == AbstractIcbm.CHANNEL_RV) {
                return new RecvRvIcbm(packet);
            } else {
                return null;
            }
        } else if (command == IcbmCommand.CMD_RECV_TYPING) {
            return new RecvTypingNotification(packet);
        } else if (command == IcbmCommand.CMD_MISSED) {
            return new MissedMessagesCmd(packet);
        } else if (command == IcbmCommand.CMD_MSG_ACK) {
            return new MessageAck(packet);
        } else if (command == IcbmCommand.CMD_RV_RESPONSE) {
            return new RvResponse(packet);
        } else {
            return null;
        }
    }
}
