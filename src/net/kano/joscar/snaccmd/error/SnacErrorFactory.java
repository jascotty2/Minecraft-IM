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
 *  File created by keith @ Mar 28, 2003
 *
 */

package net.kano.joscar.snaccmd.error;

import net.kano.joscar.flapcmd.SnacCommand;
import net.kano.joscar.flapcmd.SnacPacket;
import net.kano.joscar.snac.CmdType;
import net.kano.joscar.snac.SnacCmdFactory;
import net.kano.joscar.snaccmd.acct.AcctCommand;
import net.kano.joscar.snaccmd.auth.AuthCommand;
import net.kano.joscar.snaccmd.buddy.BuddyCommand;
import net.kano.joscar.snaccmd.chat.ChatCommand;
import net.kano.joscar.snaccmd.conn.ConnCommand;
import net.kano.joscar.snaccmd.icbm.IcbmCommand;
import net.kano.joscar.snaccmd.icon.IconCommand;
import net.kano.joscar.snaccmd.invite.InviteCommand;
import net.kano.joscar.snaccmd.loc.LocCommand;
import net.kano.joscar.snaccmd.popup.PopupCommand;
import net.kano.joscar.snaccmd.rooms.RoomCommand;
import net.kano.joscar.snaccmd.search.SearchCommand;
import net.kano.joscar.snaccmd.ssi.SsiCommand;

/**
 * A base class for both the client and server error factories.
 */
public abstract class SnacErrorFactory implements SnacCmdFactory {
    /** The supported SNAC error command types. */
    private static final CmdType[] SUPPORTED_TYPES = new CmdType[] {
        new CmdType(AuthCommand.FAMILY_AUTH, SnacError.CMD_ERROR),
        new CmdType(ConnCommand.FAMILY_CONN, SnacError.CMD_ERROR),
        new CmdType(LocCommand.FAMILY_LOC, SnacError.CMD_ERROR),
        new CmdType(BuddyCommand.FAMILY_BUDDY, SnacError.CMD_ERROR),
        new CmdType(PopupCommand.FAMILY_POPUP, SnacError.CMD_ERROR),
        new CmdType(AcctCommand.FAMILY_ACCT, SnacError.CMD_ERROR),
        new CmdType(RoomCommand.FAMILY_ROOM, SnacError.CMD_ERROR),
        new CmdType(ChatCommand.FAMILY_CHAT, SnacError.CMD_ERROR),
        new CmdType(InviteCommand.FAMILY_INVITE, SnacError.CMD_ERROR),
        new CmdType(SearchCommand.FAMILY_SEARCH, SnacError.CMD_ERROR),
        new CmdType(IconCommand.FAMILY_ICON, SnacError.CMD_ERROR),
        new CmdType(SsiCommand.FAMILY_SSI, SnacError.CMD_ERROR),
        new CmdType(IcbmCommand.FAMILY_ICBM, SnacError.CMD_ERROR),
    };

    /** Creates a new SNAC error factory. */
    protected SnacErrorFactory() { }

    public CmdType[] getSupportedTypes() {
        return (CmdType[]) SUPPORTED_TYPES.clone();
    }

    public SnacCommand genSnacCommand(SnacPacket packet) {
        if (packet.getCommand() != SnacError.CMD_ERROR) return null;

        return new SnacError(packet);
    }
}
