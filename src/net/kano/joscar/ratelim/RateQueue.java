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
import net.kano.joscar.snac.SnacRequest;

import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages a single queue for a single rate class of a SNAC connection. This
 * class basically wraps a plain old queue, and its existence is thus rather
 * questionable. However, functionality might be added later, so this class
 * stays.
 */
public class RateQueue {
    /** A logger to log rate-related events. */
    private static final Logger logger
            = Logger.getLogger("net.kano.joscar.ratelim");

    /** The "parent" connection manager for this rate queue. */
    private final ConnectionQueueMgr parentMgr;

    /** The actual request queue. */
    private final LinkedList queue = new LinkedList();

    /** The rate class monitor for this rate queue. */
    private final RateClassMonitor rateMonitor;

    /**
     * Creates a new rate queue with the given "parent" connection queue manager
     * using the given rate monitor.
     *
     * @param parentMgr this rate queue's parent connection queue manager
     * @param monitor the rate class monitor to use for this rate queue
     */
    RateQueue(ConnectionQueueMgr parentMgr, RateClassMonitor monitor) {
        DefensiveTools.checkNull(parentMgr, "parentMgr");
        DefensiveTools.checkNull(monitor, "monitor");

        this.parentMgr = parentMgr;
        this.rateMonitor = monitor;
    }

    /**
     * Returns this rate queue's "parent" connection queue manager.
     *
     * @return this rate queue's "parent" connection queue manager
     */
    public ConnectionQueueMgr getParentMgr() { return parentMgr; }

    /**
     * Returns the rate class monitor associated with this rate queue.
     *
     * @return this rate queue's associated rate class monitor
     */
    public RateClassMonitor getRateClassMonitor() { return rateMonitor; }

    /**
     * Returns the number of requests currently waiting in this queue.
     *
     * @return the number of requests currently waiting in this queue
     */
    public synchronized int getQueueSize() { return queue.size(); }

    /**
     * Returns whether any requests are waiting in this queue.
     *
     * @return whether any requests are currently in this queue
     */
    public synchronized boolean hasRequests() { return !queue.isEmpty(); }

    /**
     * Adds a request to this queue.
     *
     * @param req the request to enqueue
     */
    synchronized void enqueue(SnacRequest req) {
        DefensiveTools.checkNull(req, "req");

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Enqueuing " + req.getCommand() + " within ratequeue " +
                    "(class " + rateMonitor.getRateInfo().getRateClass()
                    + ")...");
        }

        queue.add(req);
    }

    /**
     * Removes the oldest request from this queue.
     *
     * @return the request that was removed
     */
    synchronized SnacRequest dequeue() {
        if (queue.isEmpty()) return null;

        SnacRequest request = (SnacRequest) queue.removeFirst();

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Dequeueing " + request.getCommand()
                    + " from ratequeue (class "
                    + rateMonitor.getRateInfo().getRateClass() + ")...");
        }

        return request;
    }

    /**
     * Dequeues all requests in this queue, adding them in order from oldest
     * to newest to the given collection.
     *
     * @param dest the collection to which the dequeued requests should be added
     */
    synchronized void dequeueAll(Collection dest) {
        dest.addAll(queue);
        queue.clear();
    }

    /**
     * Removes all requests from this queue.
     */
    synchronized void clear() {
        queue.clear();
    }
}
