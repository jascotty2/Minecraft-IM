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
 *  File created by Keith @ 4:14:43 AM
 *
 */

package net.kano.joscar.rvcmd;

import net.kano.joscar.MiscTools;
import net.kano.joscar.tlv.Tlv;
import net.kano.joscar.snaccmd.CapabilityBlock;
import net.kano.joscar.snaccmd.icbm.RecvRvIcbm;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A base class for "acceptance" rendezvous commands, or commands with no RV
 * data and a status code of {@link #RVSTATUS_ACCEPT}.
 */
public abstract class AbstractAcceptRvCmd extends AbstractRvCmd {
    /** A TLV type present if the rendezvous being accepted is a secure RV. */
    private static final int TYPE_ENCRYPTED = 0x0011;

    /** Whether the rendezvous being accepted is a secure rendezvous. */
    private final boolean encrypted;

    /**
     * Creates a new RV "accept" command from the given incoming acceptance RV
     * ICBM.
     *
     * @param icbm an incoming "acceptance" RV ICBM
     */
    protected AbstractAcceptRvCmd(RecvRvIcbm icbm) {
        super(icbm);

        encrypted = getRvTlvs().hasTlv(TYPE_ENCRYPTED);
    }

    /**
     * Creates a new outgoing RV "accept" command for an unencrypted rendezvous
     * with the given associated capability block. Using this constructor is
     * equivalent to using {@link #AbstractAcceptRvCmd(CapabilityBlock, boolean)
     * AbstractAcceptRvCmd(cap, false)}.
     *
     * @param cap the capability block associated with this RV command
     */
    protected AbstractAcceptRvCmd(CapabilityBlock cap) {
        this(cap, false);
    }

    /**
     * Creates a new outgoing RV "accept" command with the given associated
     * capability block. If <code>encrypted</code> is <code>true</code>, this
     * acceptance RV command will indicate that a "secure" or "encrypted"
     * rendezvous is being accepted (such as a secure file transfer).
     *
     * @param cap the capability block associated with this RV command
     * @param encrypted whether the rendezvous being accepted is a secure
     *        rendezvous
     */
    protected AbstractAcceptRvCmd(CapabilityBlock cap, boolean encrypted) {
        super(RVSTATUS_ACCEPT, cap);

        this.encrypted = encrypted;
    }

    /**
     * Returns whether the rendezvous being accepted is encrypted ("secure").
     *
     * @return whether the rendezvous being accepted is a secure rendezvous
     */
    public final boolean isEncrypted() { return encrypted; }

    protected final void writeHeaderRvTlvs(OutputStream out)
            throws IOException {
        if (encrypted) new Tlv(TYPE_ENCRYPTED).write(out);
    }

    /**
     * Provides a default implementation for writing this command's RV TLV's.
     * This implementation does not write any TLV's to the given stream (as most
     * acceptance RV commands contain no TLV's).
     *
     * @param out the stream to which to write
     *
     * @throws IOException if an I/O error occurs
     */
    protected void writeRvTlvs(OutputStream out) throws IOException { }

    public String toString() {
        return MiscTools.getClassName(this) + ": encrypted=" + encrypted;
    }
}