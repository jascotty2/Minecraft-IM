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
 *  File created by keith @ Feb 19, 2003
 *
 */

package net.kano.joscar.snaccmd;

import net.kano.joscar.snac.DefaultSnacCmdFactoryList;
import net.kano.joscar.snac.SnacCmdFactory;
import net.kano.joscar.snaccmd.acct.ServerAcctCmdFactory;
import net.kano.joscar.snaccmd.auth.ServerAuthCmdFactory;
import net.kano.joscar.snaccmd.buddy.ServerBuddyCmdFactory;
import net.kano.joscar.snaccmd.chat.ServerChatCmdFactory;
import net.kano.joscar.snaccmd.conn.ServerConnCmdFactory;
import net.kano.joscar.snaccmd.error.ServerSnacErrorFactory;
import net.kano.joscar.snaccmd.icbm.ServerIcbmCmdFactory;
import net.kano.joscar.snaccmd.icon.ServerIconCmdFactory;
import net.kano.joscar.snaccmd.invite.ServerInviteCmdFactory;
import net.kano.joscar.snaccmd.loc.ServerLocCmdFactory;
import net.kano.joscar.snaccmd.popup.ServerPopupCmdFactory;
import net.kano.joscar.snaccmd.rooms.ServerRoomCmdFactory;
import net.kano.joscar.snaccmd.search.ServerSearchCmdFactory;
import net.kano.joscar.snaccmd.ssi.ServerSsiCmdFactory;

/**
 * Provides a default <code>SnacCmdFactoryList</code> appropriate for use by
 * an AIM server. The included factories produce instances of the
 * <code>SnacCommand</code>s defined in <code>net.kano.joscar.snaccmd</code>'s
 * subpackages.
 */
public class DefaultServerFactoryList extends DefaultSnacCmdFactoryList {
    /**
     * Creates a default SNAC command factory list appropriate for use by AIM
     * servers.
     */
    public DefaultServerFactoryList() {
        super(new SnacCmdFactory[] {
            new ServerAuthCmdFactory(),
            new ServerConnCmdFactory(),
            new ServerLocCmdFactory(),
            new ServerBuddyCmdFactory(),
            new ServerPopupCmdFactory(),
            new ServerAcctCmdFactory(),
            new ServerRoomCmdFactory(),
            new ServerChatCmdFactory(),
            new ServerInviteCmdFactory(),
            new ServerSearchCmdFactory(),
            new ServerIconCmdFactory(),
            new ServerSsiCmdFactory(),
            new ServerIcbmCmdFactory(),
            new ServerSnacErrorFactory(),
        });
    }
}
