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
 *  File created by keith @ Mar 1, 2003
 *
 */

package net.kano.joscar.snaccmd.conn;

import net.kano.joscar.flapcmd.SnacPacket;
import net.kano.joscar.snaccmd.ExtraInfoBlock;

/**
 * A SNAC command sent to tell the client what his or her current "extra
 * information" is (such as a buddy icon and iChat availability message).
 *
 * @snac.src server
 * @snac.cmd 0x01 0x21
 */
public class ExtraInfoAck extends AbstractExtraInfoCmd {
    /**
     * Generates a new extra info acknowledgement command from the given
     * incoming SNAC packet.
     *
     * @param packet the incoming icon acknowledgement packet
     */
    protected ExtraInfoAck(SnacPacket packet) {
        super(CMD_EXTRA_ACK, packet);
    }

    /**
     * Creates a new outgoing extra info acknowledgement command with the given
     * list of extra information blocks. Note that neither <code>blocks</code>
     * nor any of its elements can be <code>null</code>.
     *
     * @param blocks the extra information blocks to send in this command
     */
    public ExtraInfoAck(ExtraInfoBlock[] blocks) {
        super(CMD_EXTRA_ACK, blocks);
    }
}
