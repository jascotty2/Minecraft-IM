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
 *  File created by keith @ Mar 3, 2003
 *
 */

package net.kano.joscar.snaccmd.ssi;

import net.kano.joscar.DefensiveTools;
import net.kano.joscar.flapcmd.SnacPacket;

/**
 * A SNAC command used to delete specific server-stored "items." Normally
 * responded-to with a {@link SsiDataModResponse}.
 *
 * @snac.src client
 * @snac.cmd 0x13 0x0a
 */
public class DeleteItemsCmd extends ItemsCmd {
    /**
     * Generates a new item deletion command from the given incoming SNAC
     * packet.
     *
     * @param packet an incoming SSI item deletion packet
     */
    protected DeleteItemsCmd(SnacPacket packet) {
        super(CMD_DELETE_ITEMS, packet);

        DefensiveTools.checkNull(packet, "packet");
    }

    /**
     * Creates a new outgoing item deletion command with the given list of items
     * to delete. Note that normally only the group ID, buddy ID, and item type
     * need to be set in each item, though this is certainly not required.
     *
     * @param items the items to delete from the server
     */
    public DeleteItemsCmd(SsiItem[] items) {
        super(CMD_DELETE_ITEMS, items);
    }
}
