/*
 *  Copyright (c) 2003, The Joust Project
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
 *  File created by Keith @ 11:24:58 PM
 *
 */

package net.kano.joscar.rvcmd.chatinvite;

import net.kano.joscar.rvcmd.AbstractRejectRvCmd;
import net.kano.joscar.snaccmd.CapabilityBlock;
import net.kano.joscar.snaccmd.icbm.RecvRvIcbm;

/**
 * A rendezvous command used to indicate that an {@linkplain ChatInvitationRvCmd
 * chat invitation} has been rejected.
 */
public class ChatInviteRejectRvCmd extends AbstractRejectRvCmd {
    /**
     * Creates a new chat invitation rejection command from the given incoming
     * chat invitation rejection RV ICBM.
     *
     * @param icbm an incoming chat invitation rejection RV ICBM command
     */
    public ChatInviteRejectRvCmd(RecvRvIcbm icbm) {
        super(icbm);
    }

    /**
     * Creates a new outgoing chat invitation rejection command with the given
     * rejection code.
     *
     * @param rejectionCode a rejection code, like {@link #REJECTCODE_CANCELLED}
     */
    public ChatInviteRejectRvCmd(int rejectionCode) {
        super(CapabilityBlock.BLOCK_CHAT, rejectionCode);
    }
}