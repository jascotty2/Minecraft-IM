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
 *  File created by keith @ Jun 4, 2003
 *
 */

package net.kano.joscar.ratelim;

import net.kano.joscar.snac.ClientSnacProcessor;
import net.kano.joscar.snaccmd.conn.RateClassInfo;

/**
 * An interface for listening for rate-related events on a {@link RateMonitor}.
 */
public interface RateListener {
    /**
     * Called when the given rate monitor receives a new set of rate class
     * information. This callback's invocation indicates that all previously
     * held <code>RateClassMonitor</code>s in the given <code>RateMonitor</code>
     * have been discarded and a new set has been generated.
     *
     * @param monitor the rate monitor that received new rate class information
     */
    void gotRateClasses(RateMonitor monitor);

    /**
     * Called when a single rate class's information has been updated.
     *
     * @param rateMonitor the rate monitor whose rate class was updated
     * @param rateClassMonitor the rate class monitor for the associated
     *        rate class
     * @param rateClassInfo the new rate class information
     */
    void rateClassUpdated(RateMonitor rateMonitor,
            RateClassMonitor rateClassMonitor, RateClassInfo rateClassInfo);

    /**
     * Called when a single rate class becomes or stops becoming "rate-limited."
     *
     * @param rateMonitor the rate monitor whose rate class's limited status
     *        changed
     * @param rateClassMonitor the rate class monitor for the associated rate
     *        class
     * @param limited whether or not the rate class is limited
     */
    void rateClassLimited(RateMonitor rateMonitor,
            RateClassMonitor rateClassMonitor, boolean limited);

    /**
     * Called when the given rate monitor was "detached" from the given SNAC
     * processor. See {@link RateMonitor#detach()} for details; in brief, this
     * callback's invocation indicates that no further work should be done
     * involving the given rate monitor, and it is recommended that listeners
     * remove themselves as listeners upon a call to this method.
     *
     * @param rateMonitor the rate monitor that was detached
     * @param processor the SNAC processor from which the given rate monitor was
     *        detached
     */
    void detached(RateMonitor rateMonitor, ClientSnacProcessor processor);

    /**
     * Called when the rate information for the given rate monitor has been
     * reset. See {@link RateMonitor#reset()} for details.
     *
     * @param rateMonitor the rate monitor whose rate information was reset
     */
    void reset(RateMonitor rateMonitor);
}
