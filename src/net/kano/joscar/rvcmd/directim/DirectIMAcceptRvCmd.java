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
 *  File created by Keith @ 11:05:47 PM
 *
 */

package net.kano.joscar.rvcmd.directim;

import net.kano.joscar.rvcmd.AbstractAcceptRvCmd;
import net.kano.joscar.snaccmd.CapabilityBlock;
import net.kano.joscar.snaccmd.icbm.RecvRvIcbm;

/**
 * A rendezvous command used to indicate that a {@linkplain DirectIMReqRvCmd
 * direct IM request} has been accepted and a connection attempt is being made.
 */
public class DirectIMAcceptRvCmd extends AbstractAcceptRvCmd {
    /**
     * Creates a new direct IM acceptance command from the given incoming direct
     * IM acceptance RV ICBM.
     *
     * @param icbm an incoming direct IM acceptance RV ICBM command
     */
    public DirectIMAcceptRvCmd(RecvRvIcbm icbm) {
        super(icbm);
    }

    /**
     * Creates a new outgoing (unencrypted) direct IM acceptance command.
     */
    public DirectIMAcceptRvCmd() {
        this(false);
    }

    /**
     * Creates a new outgoing acceptance command for either an encrypted or a
     * normal direct IM invitation.
     *
     * @param encrypted whether or not the direct IM connection being accepted
     *        is a secure/encrypted connection
     */ 
    public DirectIMAcceptRvCmd(boolean encrypted) {
        super(CapabilityBlock.BLOCK_DIRECTIM, encrypted);
    }
}