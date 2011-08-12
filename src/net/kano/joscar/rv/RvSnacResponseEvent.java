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

package net.kano.joscar.rv;

import net.kano.joscar.DefensiveTools;
import net.kano.joscar.snac.SnacPacketEvent;

/**
 * An event that occurs when a non-RV response to a RV command or RV response is
 * received. For example, if one attempts to send a file to a user who is not
 * currently online, a {@link net.kano.joscar.snaccmd.error.SnacError} will
 * likely be received.
 *
 * @see SnacPacketEvent
 */
public class RvSnacResponseEvent extends SnacPacketEvent {
    /** The RV processor on which this response was received. */
    private final RvProcessor rvProcessor;
    /** The RV session on which this response was received. */
    private final RvSession rvSession;

    /**
     * Creates a new RV SNAC response event with the given properties.
     *
     * @param cause the SNAC packet event that was received in response to an
     *        RV command
     * @param rvProcessor the RV processor on which this response was received
     * @param rvSession the RV session whose initial RV command caused this
     *        response
     */
    protected RvSnacResponseEvent(SnacPacketEvent cause,
            RvProcessor rvProcessor, RvSession rvSession) {
        super(cause);

        DefensiveTools.checkNull(rvProcessor, "rvProcessor");
        DefensiveTools.checkNull(rvSession, "rvSession");

        this.rvProcessor = rvProcessor;
        this.rvSession = rvSession;
    }

    /**
     * Returns the RV processor on which the associated SNAC response was
     * received.
     *
     * @return the RV processor on which the associated SNAC response was
     *         recieved
     */
    public final RvProcessor getRvProcessor() { return rvProcessor; }

    /**
     * Returns the RV session on which the associated SNAC response was
     * received. While the SNAC response likely did not contain a rendezvous
     * session ID (and thus does not intrinsically have an associated RV
     * session), it is still associated with an RV session in the sense that
     * the RV command to which it is a response was sent over a RV session. I
     * hope that makes sense.
     *
     * @return the RV session on which the associated SNAC response was recieved
     */
    public final RvSession getRvSession() { return rvSession; }
}
