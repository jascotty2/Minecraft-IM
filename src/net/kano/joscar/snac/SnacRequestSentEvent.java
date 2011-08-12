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
 *  File created by keith @ Apr 3, 2003
 *
 */

package net.kano.joscar.snac;

import net.kano.joscar.DefensiveTools;
import net.kano.joscar.flap.FlapProcessor;

/**
 * An event fired when an outgoing SNAC request is sent over a SNAC connection.
 */
public class SnacRequestSentEvent {
    /**
     * The FLAP processor on which the request was sent.
     */
    private final FlapProcessor flapProcessor;

    /**
     * The SNAC processor on which the request was sent.
     */
    private final ClientSnacProcessor snacProcessor;

    /**
     * The request that was sent.
     */
    private final SnacRequest request;

    /**
     * The time at which the request was sent, in milliseconds since unix epoch.
     */
    private final long sentTime;

    /**
     * Creates a new event with the given properties.
     *
     * @param flapProcessor the FLAP processor on which the request was sent
     * @param snacProcessor the SNAC processor on which the request was sent
     * @param request the request that was sent
     * @param sentTime the time at which it was sent, in milliseconds since
     *        unix epoch
     */
    protected SnacRequestSentEvent(FlapProcessor flapProcessor,
            ClientSnacProcessor snacProcessor, SnacRequest request, long sentTime) {
        DefensiveTools.checkNull(flapProcessor, "flapProcessor");
        DefensiveTools.checkNull(snacProcessor, "snacProcessor");
        DefensiveTools.checkNull(request, "request");
        
        this.flapProcessor = flapProcessor;
        this.snacProcessor = snacProcessor;
        this.request = request;
        this.sentTime = sentTime;
    }

    /**
     * Returns the FLAP processor on which the associated request was sent.
     *
     * @return the FLAP processor on which the associated request was sent
     */
    public FlapProcessor getFlapProcessor() {
        return flapProcessor;
    }

    /**
     * Returns the SNAC processor on which the associated request was sent.
     *
     * @return the SNAC processor on which the associated request was sent
     */
    public ClientSnacProcessor getSnacProcessor() {
        return snacProcessor;
    }

    /**
     * Returns the request that was sent.
     *
     * @return the request that was sent
     */
    public SnacRequest getRequest() { return request; }

    /**
     * Returns the time at which the associated request was sent. This is
     * returned in the format returned by <code>System.currentTimeMillis</code>,
     * in milliseconds since the unix epoch.
     *
     * @return the time at which the associated request was sent, in
     *         milliseconds since the unix epoch
     */
    public long getSentTime() { return sentTime; }
}
