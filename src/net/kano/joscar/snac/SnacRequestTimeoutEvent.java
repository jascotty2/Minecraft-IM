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
 *  File created by keith @ Apr 2, 2003
 *
 */

package net.kano.joscar.snac;

import net.kano.joscar.DefensiveTools;
import net.kano.joscar.flap.FlapProcessor;

/**
 * An event fired when a SNAC request "times out." This is not a bad thing; in
 * fact, it happens to almost every SNAC request. See {@link SnacRequest} for
 * details.
 */
public class SnacRequestTimeoutEvent {
    /**
     * The FLAP connection on which this request timed out.
     */
    private final FlapProcessor flapProcessor;

    /**
     * The SNAC processor on which this request timed out.
     */
    private final ClientSnacProcessor snacProcessor;

    /**
     * The request that timed out.
     */
    private final SnacRequest request;

    /**
     * The maximum "time to live" for SNAC requests at the time the request
     * timed out.
     */
    private final int ttl;

    /**
     * Creates a new timeout event with the given properties.
     *
     * @param flapProcessor the FLAP connection on which the given request timed
     *        out
     * @param snacProcessor the SNAC connection on which the given request timed
     *        out
     * @param request the request that timed out
     * @param ttl the time-to-live that the associated request's lifetime
     *        exceeded
     */
    protected SnacRequestTimeoutEvent(FlapProcessor flapProcessor,
            ClientSnacProcessor snacProcessor, SnacRequest request, int ttl) {
        DefensiveTools.checkNull(flapProcessor, "flapProcessor");
        DefensiveTools.checkNull(snacProcessor, "snacProcessor");
        DefensiveTools.checkNull(request, "request");
        
        this.flapProcessor = flapProcessor;
        this.snacProcessor = snacProcessor;
        this.request = request;
        this.ttl = ttl;
    }

    /**
     * Returns the FLAP connection on which the associated request timed out.
     *
     * @return the FLAP connection associated with this event
     */
    public FlapProcessor getFlapProcessor() {
        return flapProcessor;
    }

    /**
     * Returns the SNAC connection on which the associated request timed out.
     *
     * @return the SNAC connection associated with this event
     */
    public ClientSnacProcessor getSnacProcessor() {
        return snacProcessor;
    }

    /**
     * Returns the request that timed out.
     *
     * @return the request that timed out
     */
    public SnacRequest getRequest() { return request; }

    /**
     * Returns the maximum "time to live," in seconds, that this request's
     * lifetime exceeded, causing it to time out.
     *
     * @return the "time to live" that this request exceeded, in seconds
     */
    public int getTtl() { return ttl; }
}
