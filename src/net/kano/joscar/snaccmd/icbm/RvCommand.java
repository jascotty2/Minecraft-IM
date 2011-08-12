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
 *  File created by keith @ Apr 24, 2003
 *
 */

package net.kano.joscar.snaccmd.icbm;

import net.kano.joscar.DefensiveTools;
import net.kano.joscar.snaccmd.CapabilityBlock;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Represents a single "rendezvous command." Rendezvous commands can be used in
 * {@linkplain SendRvIcbm#SendRvIcbm(String, long, long, RvCommand) sending
 * <code>SendRvIcbm</code>s}, and with the help of {@link net.kano.joscar.rv}
 * can easily be generated from incoming {@link RecvRvIcbm}s.
 */
public abstract class RvCommand {
    /**
     * A status code indicating that a rendezvous is a request. Simply a
     * shortcut for {@link AbstractRvIcbm#RVSTATUS_ACCEPT}.
     */
    public static final int RVSTATUS_REQUEST = AbstractRvIcbm.RVSTATUS_REQUEST;
    /**
     * A status code indicating that a rendezvous has been accepted. Simply a
     * shortcut for {@link AbstractRvIcbm#RVSTATUS_ACCEPT}.
     */
    public static final int RVSTATUS_ACCEPT = AbstractRvIcbm.RVSTATUS_ACCEPT;
    /**
     * A status code indicating that a rendezvous has been rejected or
     * cancelled. Simply a shortcut for {@link AbstractRvIcbm#RVSTATUS_DENY}.
     */
    public static final int RVSTATUS_DENY = AbstractRvIcbm.RVSTATUS_DENY;

    /** The rendezvous status of this command. */
    private final int rvStatus;
    /** The capability block associated with this command. */
    private final CapabilityBlock cap;

    /**
     * Creates a new <code>RvCommand</code> with properties read from the given
     * incoming <code>RecvRvIcbm</code>.
     *
     * @param icbm an incoming RV ICBM command
     */
    protected RvCommand(RecvRvIcbm icbm) {
        DefensiveTools.checkNull(icbm, "icbm");

        rvStatus = icbm.getRvStatus();
        cap = icbm.getCapability();
    }

    /**
     * Creates a new outgoing <code>RvCommand</code> with the given properties.
     *
     * @param rvStatus a rendezvous status code, like {@link #RVSTATUS_ACCEPT}
     * @param cap a capability block ("rendezvous type") associated with this
     *        rendezvous command
     */
    protected RvCommand(int rvStatus, CapabilityBlock cap) {
        DefensiveTools.checkRange(rvStatus, "rvStatus", 0);
        DefensiveTools.checkNull(cap, "cap");

        this.rvStatus = rvStatus;
        this.cap = cap;
    }

    /**
     * Returns the status code for this RV command. Normally one of {@link
     * #RVSTATUS_REQUEST}, {@link #RVSTATUS_ACCEPT}, and {@link #RVSTATUS_DENY}.
     *
     * @return this RV command's status code
     */
    public final int getRvStatus() { return rvStatus; }

    /**
     * Returns this RV command's capability block ("rendezvous type").
     *
     * @return this RV command's associated capability block ("RV type")
     */
    public final CapabilityBlock getCapabilityBlock() { return cap; }

    /**
     * Writes this RV command's "rendezvous data block" to the given stream.
     *
     * @param out the stream to which to write
     *
     * @throws IOException if an I/O error occurs
     */
    public abstract void writeRvData(OutputStream out) throws IOException;
}
