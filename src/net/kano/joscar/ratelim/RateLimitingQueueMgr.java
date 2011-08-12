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
 *  File created by keith @ May 25, 2003
 *
 */

package net.kano.joscar.ratelim;

import net.kano.joscar.DefensiveTools;
import net.kano.joscar.snac.ClientSnacProcessor;
import net.kano.joscar.snac.SnacQueueManager;
import net.kano.joscar.snac.SnacRequest;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * A SNAC queue manager which uses a <code>RateMonitor</code> to determine when
 * to send SNAC commands to avoid becoming rate-limited.
 * <br>
 * <br>
 * Note that a <code>RateLimitingQueueMgr</code> creates its own
 * <code>RateMonitor</code> for each SNAC processor to which it is added. This
 * behavior has several noteworthy implications:
 * <ul>
 * <li> As per the <code>RateMonitor</code> documentation,
 * <code>RateMonitor</code>s should not be attached to SNAC processors which are
 * already connected. Thus, <b>a <code>RateLimitingQueueMgr</code> should not be
 * set as the SNAC queue manager for a SNAC processor which has already
 * connected</b>. </li>
 * <li> To avoid calculating the rate more than once, it is not recommended to
 * create one's own <code>RateMonitor</code> for SNAC processors which use a
 * <code>RateLimitingQueueMgr</code> for a SNAC queue manager. To retrieve the
 * <code>RateMonitor</code> used by the <code>RateLimitingQueueMgr</code> for a
 * given SNAC processor, simply use code such as the following:
 * <br>
 * <br>
 * <code> RateMonitor mon = rateLimitingQueueMgr.{@linkplain
 * #getQueueMgr(ClientSnacProcessor) getQueueMgr}(snacProcessor).{@linkplain
 * ConnectionQueueMgr#getRateMonitor getRateMonitor}() </code></li>
 * </ul>
 *
 * One may also wish to note that each instance of
 * <code>RateLimitingQueueMgr</code> starts its own thread to manage the queue.
 * It may be desirable to add a single rate limiting queue manager to every SNAC
 * processor, or to split them up by giving each logical OSCAR connection (that
 * is, each screenname) its own instance, or to give each SNAC processor its
 * own. At the time of this writing, the threading / queue management code is
 * not optimal, and it may consume a rather large amount of CPU when added to
 * too many SNAC processors. In buzzword terms, it may not scale well. Thus, for
 * now, it may be recommended to give each SNAC processor its own individual
 * <code>RateLimitingQueueMgr</code>.
 * <br>
 * <br>
 * A <code>RateLimitingQueueMgr</code> delegates most actual functionality to
 * a set of "child" {@link ConnectionQueueMgr}s. See {@link #getQueueMgr
 * getQueueMgr} and {@link #getQueueMgrs getQueueMgrs} for information on how
 * to use these after assigning the <code>RateLimitingQueueMgr</code> to a SNAC
 * processor.
 *
 * @see RateMonitor
 */
public class RateLimitingQueueMgr implements SnacQueueManager {
    /** A map from SNAC processors to connection managers. */
    private final Map connMgrs = new IdentityHashMap();

    /** A thread to "run" the SNAC queues controlled by this queue manager. */
    private final QueueRunner runner = new QueueRunner();
    {
        new Thread(runner).start();
    }

    /**
     * Returns this rate manager's "queue runner."
     *
     * @return this rate manager's queue runner thread object
     */
    final QueueRunner getRunner() { return runner; }

    /**
     * Sends the given SNAC request on the given SNAC processor.
     *
     * @param processor the SNAC processor on which the given request should be
     *        sent
     * @param request the request to send
     *
     * @see ClientSnacProcessor#sendSnacImmediately
     */
    void sendSnac(ClientSnacProcessor processor, SnacRequest request) {
        processor.sendSnacImmediately(request);
    }

    /**
     * Returns a list of all SNAC processor queue managers currently being used.
     * One <code>ConnectionQueueMgr</code> exists for each SNAC processor for
     * which this <code>RateLimitingQueueMgr</code> is set.
     *
     * @return a list of all single-SNAC-processor queue managers currently in
     *         use
     */
    public final ConnectionQueueMgr[] getQueueMgrs() {
        synchronized(connMgrs) {
            return (ConnectionQueueMgr[])
                    connMgrs.values().toArray(new ConnectionQueueMgr[0]);
        }
    }

    /**
     * Returns the single-SNAC-processor queue manager for the given SNAC
     * processor. Note that if this <code>RateLimitingQueueMgr</code> is not set
     * as the given SNAC processor's queue manager this method will return
     * <code>null</code>.
     *
     * @param processor a SNAC processor
     * @return the SNAC processor queue manager in use for the given SNAC
     *         processor, or <code>null</code> if none is in use for the given
     *         SNAC processor
     */
    public final ConnectionQueueMgr getQueueMgr(ClientSnacProcessor processor) {
        DefensiveTools.checkNull(processor, "processor");

        synchronized(connMgrs) {
            return (ConnectionQueueMgr) connMgrs.get(processor);
        }
    }

    public void attached(ClientSnacProcessor processor) {
        synchronized(connMgrs) {
            connMgrs.put(processor, new ConnectionQueueMgr(this, processor));
        }
    }

    public void detached(ClientSnacProcessor processor) {
        ConnectionQueueMgr mgr;
        synchronized(connMgrs) {
            mgr = (ConnectionQueueMgr) connMgrs.remove(processor);
        }

        mgr.detach();
    }

    public void queueSnac(ClientSnacProcessor processor, SnacRequest request) {
        DefensiveTools.checkNull(request, "request");

        getQueueMgr(processor).queueSnac(request);
    }

    public void clearQueue(ClientSnacProcessor processor) {
        getQueueMgr(processor).clearQueue();
    }

    public void pause(ClientSnacProcessor processor) {
        getQueueMgr(processor).pause();
    }

    public void unpause(ClientSnacProcessor processor) {
        getQueueMgr(processor).unpause();
    }
}

