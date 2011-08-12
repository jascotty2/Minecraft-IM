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
 * A SNAC command used to modify server-stored "items" that already exist on the
 * server. Normally responded-to with a {@link SsiDataModResponse}.
 *
 * @snac.src client
 * @snac.cmd 0x13 0x09
 *
 * @see SsiDataModResponse
 */
public class ModifyItemsCmd extends ItemsCmd {
    /**
     * Generates a new modify-SSI-items command from the given incoming SNAC
     * packet.
     *
     * @param packet an incoming SSI modification packet
     */
    protected ModifyItemsCmd(SnacPacket packet) {
        super(CMD_MODIFY_ITEMS, packet);

        DefensiveTools.checkNull(packet, "packet");
    }

    /**
     * Creates a new SSI modification command with the given items. Note that,
     * in essence, this command will <i>replace</i> items with the same
     * type/parentID/subID with the corresponding items you provide here. In
     * other words, this command does not actually "modify" the given items
     * but rather "replaces" them.
     *
     * @param items a list of items to "modify"
     */
    public ModifyItemsCmd(SsiItem[] items) {
        super(CMD_MODIFY_ITEMS, items);
    }
}
