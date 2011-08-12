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
 *  File created by keith @ Jun 19, 2003
 *
 */

package net.kano.joscar.snac;

import net.kano.joscar.SeqNum;
import net.kano.joscar.flap.FlapProcessor;
import net.kano.joscar.flapcmd.SnacCommand;

/**
 * A server-side SNAC processor. This class automatically assigns SNAC request
 * ID's between <code>0x80000000</code> and <code>0xffffffff</code>, the
 * required range for server request ID's. No request-response system is
 * supported (as in {@link ClientSnacProcessor}) because this functionality is
 * not used by the server. Additionally, no SNAC queue is implemented, as its
 * intended use, rate limiting, only affects the client side. 
 */
public class ServerSnacProcessor extends AbstractSnacProcessor {
    /** The minimum request ID value. */
    public static final long REQID_MIN = 0x80000000L;
    /** The maximum request ID value. */
    public static final long REQID_MAX = 0xffffffffL;

    /** An object used to track and wrap request ID's. */
    private final SeqNum reqid = new SeqNum(REQID_MIN, REQID_MAX);

    /**
     * Creates a new server-side SNAC processor attached to the given FLAP
     * processor.
     *
     * @param flapProcessor a FLAP processor
     */
    public ServerSnacProcessor(FlapProcessor flapProcessor) {
        super(flapProcessor);
    }

    /**
     * Sends the given SNAC command over this SNAC connection.
     *
     * @param cmd the SNAC command to send
     */
    public final void sendSnac(SnacCommand cmd) {
        sendSnac(reqid.next(), cmd);
    }

    /**
     * Sends the given SNAC command as a response to the client request with the
     * given request ID. Note that the given request ID must not be in the
     * server request ID range ({@link #REQID_MIN} through {@link #REQID_MAX}).
     *
     * @param reqid the request ID of the client request to which the given
     *        command is a response
     * @param cmd the SNAC command to send
     */
    public final void sendResponse(long reqid, SnacCommand cmd) {
        if (reqid >= REQID_MIN && reqid <= REQID_MAX) {
            throw new IllegalArgumentException("response ID (" + reqid + ") "
                    + "must not be in server request ID range (0x"
                    + Long.toHexString(REQID_MIN) + "-0x"
                    + Long.toHexString(REQID_MAX) + ", inclusive)");
        }

        sendSnac(reqid, cmd);
    }
}
