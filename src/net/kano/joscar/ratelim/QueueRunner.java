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

import net.kano.joscar.CopyOnWriteArraySet;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.snac.ClientSnacProcessor;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

/**
 * "Runs" a set of <code>RateQueue</code>s, dequeuing SNACs at appropriate
 * times. Note that this class does not create its own thread; intended usage
 * is as follows:
 * <pre>
QueueRunner runner = new QueueRunner();
new Thread(runner).start();
 * </pre>
 */
class QueueRunner implements Runnable {
    /** A lock used in synchronizing updates with the running thread. */
    private final Object lock = new Object();

    /** Whether or not this queue has been updated. */
    private boolean updated = true;

    /** The list of queues to "run." */
    private final Set queues = new CopyOnWriteArraySet();

    public void run() {
        long minWait = 0;
        for (;;) {
            synchronized(lock) {
                if (!updated) {
                    // if we haven't been updated, we need to wait until a
                    // call to update() or until we need to send the next
                    // command (this time is specified in minWait, if non-
                    // zero)
                    try {
                        if (minWait == 0) lock.wait();
                        else lock.wait(minWait);
                    } catch (InterruptedException ignored) { }
                }

                // and set this flag back to off while we're in the lock
                updated = false;
            }

            // now we go through the queues to send any "ready" commands and
            // figure out when the next command should be sent (so we can
            // wait that long in the next iteration of the outer loop)
            minWait = 0;
            for (Iterator it = queues.iterator(); it.hasNext();) {
                RateQueue queue = (RateQueue) it.next();

                boolean finished;
                long wait = 0;
                synchronized(queue) {
                    // if the queue is paused or there aren't any requests,
                    // we can skip it
                    if (queue.getParentMgr().isPaused()
                            || !queue.hasRequests()) {
                        continue;
                    }

                    // if there are one or more commands that can be sent
                    // right now, dequeue them
                    if (isReady(queue)) dequeueReady(queue);

                    // see whether the queue needs to be waited upon (if it
                    // doesn't have any queued commands, there's nothing to
                    // wait for -- we'll be notified with a call to
                    // update() if any are added)
                    finished = !queue.hasRequests();

                    // and, if necessary, compute how long we need to wait
                    // for this queue (the time until the next command can
                    // be sent)
                    if (!finished) wait = getWaitTime(queue);
                }

                // and if there's nothing more to wait for, move on to the
                // next queue
                if (finished) continue;

                // we make sure wait isn't zero, because that would cause us
                // to wait forever, which we don't want to do unless we
                // explicitly decide to do so.
                if (wait < 1) wait = 1;

                // now change the minimum waiting time if necessary
                if (minWait == 0 || wait < minWait)  minWait = wait;
            }
        }
    }

    /**
     * Returns the "optimal wait time" for the given queue.
     *
     * @param queue a rate queue
     * @return the optimal wait time for the given queue
     *
     * @see RateClassMonitor#getOptimalWaitTime()
     */
    private long getWaitTime(RateQueue queue) {
        return queue.getRateClassMonitor().getOptimalWaitTime();
    }

    /**
     * Returns whether the given queue is "ready" to send the next request. (A
     * rate queue is "ready" if its {@linkplain #getWaitTime(RateQueue) wait
     * time} is zero.
     *
     * @param queue a rate queue
     * @return whether the given queue is "ready"
     */
    private boolean isReady(RateQueue queue) {
        return getWaitTime(queue) <= 0;
    }

    /**
     * Dequeues all "ready" requests in the given rate queue.
     *
     * @param queue a rate queue
     */
    private void dequeueReady(RateQueue queue) {
        ConnectionQueueMgr connMgr = queue.getParentMgr();
        ClientSnacProcessor processor = connMgr.getSnacProcessor();
        RateLimitingQueueMgr rateMgr = connMgr.getParentQueueMgr();

        synchronized(queue) {
            for (;;) {
                if (!queue.hasRequests() || !isReady(queue)) break;

                rateMgr.sendSnac(processor, queue.dequeue());
            }
        }
    }

    /**
     * Tells the queue runner thread that the given connection queue manager has
     * been updated. (This indicates to the queue runner that it should
     * recalculate when to next send SNAC requests in queues under the given
     * queue manager.)
     *
     * @param updated the connection queue manager that has been updated
     */
    public void update(ConnectionQueueMgr updated) {
        forceUpdate();
    }

    /**
     * Tells the queue runner that the given rate queue has been updated. (This
     * indicates to the queue runner that it should recalculate when to next
     * send SNAC requests in the given queue.)
     *
     * @param updated the rate queue that has been updated
     */
    public void update(RateQueue updated) {
        forceUpdate();
    }

    /**
     * Tells the queue runner thread that a major change has taken place. This
     * indicates to the queue runner that it should recalculate when to next
     * send SNAC requests in all registered queues.
     */
    public void update() {
        forceUpdate();
    }

    /**
     * Tells the queue runner to recalculate SNAC queue wait times.
     */
    private void forceUpdate() {
        synchronized(lock) {
            updated = true;
            lock.notifyAll();
        }
    }

    /**
     * Adds the given queue to this queue runner's queue list.
     *
     * @param queue the queue to add
     */
    public void addQueue(RateQueue queue) {
        DefensiveTools.checkNull(queue, "queue");

        queues.add(queue);

        update(queue);
    }

    /**
     * Adds the given queues to this queue runner's queue list.
     *
     * @param rateQueues the queues to add
     */
    public void addQueues(RateQueue[] rateQueues) {
        DefensiveTools.checkNull(rateQueues, "rateQueues");

        queues.addAll(Arrays.asList(rateQueues));
    }

    /**
     * Removes the given queue, if present, from this queue runner's queue list.
     *
     * @param queue the queue to remove
     */
    public void removeQueue(RateQueue queue) {
        DefensiveTools.checkNull(queue, "queue");

        queues.remove(queue);
    }

    /**
     * Removes the given queues, if present, from this queue runner's queue
     * list.
     *
     * @param rateQueues the queues to remove
     */
    public void removeQueues(RateQueue[] rateQueues) {
        DefensiveTools.checkNull(rateQueues, "rateQueues");

        queues.removeAll(Arrays.asList(rateQueues));
    }
}
