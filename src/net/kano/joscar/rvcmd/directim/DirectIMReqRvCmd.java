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
 *  File created by keith @ Apr 28, 2003
 *
 */

package net.kano.joscar.rvcmd.directim;

import net.kano.joscar.rvcmd.AbstractRequestRvCmd;
import net.kano.joscar.rvcmd.RvConnectionInfo;
import net.kano.joscar.snaccmd.CapabilityBlock;
import net.kano.joscar.snaccmd.icbm.RecvRvIcbm;
import net.kano.joscar.tlv.TlvChain;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A rendezvous command used to request or redirect a Direct IM ("IM Image")
 * session. Note that this command may be sent multiple times during a session
 * to use multiple redirects (until one works).
 *
 * @see net.kano.joscar.rvproto.directim
 */
public class DirectIMReqRvCmd extends AbstractRequestRvCmd {
    /** The connection information block sent in this request. */
    private final RvConnectionInfo connInfo;

    /**
     * Creates a new direct IM request RV command from the given incoming direct
     * IM request RV ICBM.
     *
     * @param icbm an incoming direct IM request RV ICBM command
     */
    public DirectIMReqRvCmd(RecvRvIcbm icbm) {
        super(icbm);

        TlvChain chain = getRvTlvs();

        connInfo = RvConnectionInfo.readConnectionInfo(chain);
    }

    /**
     * Creates a new outgoing initial direct IM request. That is, an outgoing
     * direct IM request RV command with a request type of {@link
     * #REQTYPE_INITIALREQUEST}.
     *
     * @param connInfo a connection information block describing the connection
     *        to be made
     */
    public DirectIMReqRvCmd(RvConnectionInfo connInfo) {
        this(REQTYPE_INITIALREQUEST, connInfo);
    }

    /**
     * Creates a new outgoing direct IM request / redirect (depending on the
     * value of <code>requestType</code>) with the given connection information.
     *
     * @param requestType the "request type" for this command, like {@link
     *        #REQTYPE_REDIRECT}
     * @param connInfo a connection information block describing the connection
     *        to be made
     */
    public DirectIMReqRvCmd(int requestType, RvConnectionInfo connInfo) {
        super(CapabilityBlock.BLOCK_DIRECTIM, requestType);

        this.connInfo = connInfo;
    }

    /**
     * Returns the connection information sent in this request.
     *
     * @return this request's connection information block
     */
    public final RvConnectionInfo getConnInfo() { return connInfo; }

    protected void writeRvTlvs(OutputStream out) throws IOException {
        if (connInfo != null) connInfo.write(out);
    }

    protected boolean hasServiceData() { return false; }

    protected void writeServiceData(OutputStream out) throws IOException { }

    public String toString() {
        return "DirectIMReqRvCmd: " + connInfo;
    }
}
