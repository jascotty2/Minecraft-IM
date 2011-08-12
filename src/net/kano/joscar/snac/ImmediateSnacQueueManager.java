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

package net.kano.joscar.snac;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A SNAC queue manager that sends all SNACs immediately, only queueing SNACs
 * during a pause.
 */
public class ImmediateSnacQueueManager implements SnacQueueManager {
    /** A map from SNAC processors to their respective SNAC queues. */
    private final Map queues = new IdentityHashMap();

    public synchronized void attached(ClientSnacProcessor processor) {
        queues.put(processor, new SnacQueue());
    }

    public synchronized void detached(ClientSnacProcessor processor) {
        queues.remove(processor);
    }

    /**
     * Returns (and creates, if necessary) a SNAC queue object for the given
     * SNAC processor.
     *
     * @param processor the SNAC processor whose SNAC queue object should be
     *        returned
     * @return a SNAC queue object for the given SNAC processor
     */
    private synchronized SnacQueue getQueue(ClientSnacProcessor processor) {
        return (SnacQueue) queues.get(processor);
    }

    public void pause(ClientSnacProcessor processor) {
        SnacQueue queue = getQueue(processor);

        synchronized(queue) {
            queue.paused = true;
        }
    }

    public void unpause(ClientSnacProcessor processor) {
        SnacQueue queue = getQueue(processor);

        List dequeued;
        synchronized(queue) {
            queue.paused = false;

            dequeued = new ArrayList(queue.queue);
            queue.queue.clear();
        }

        // dequeue any queued snacs
        for (Iterator it = dequeued.iterator(); it.hasNext();) {
            SnacRequest req = (SnacRequest) it.next();
            it.remove();

            sendSnac(processor, req);
        }
    }

    /**
     * Sends the given SNAC request over the given processor, bypassing the
     * queue.
     *
     * @param processor the SNAC processor on which to send
     * @param req the request to send
     */
    protected static final void sendSnac(ClientSnacProcessor processor,
            SnacRequest req) {
        processor.sendSnacImmediately(req);
    }

    public void queueSnac(ClientSnacProcessor processor,
            SnacRequest request) {
        SnacQueue queue = getQueue(processor);

        boolean paused;
        synchronized(queue) {
            paused = queue.paused;

            if (paused) queue.queue.addLast(request);
        }

        if (!paused) sendSnac(processor, request);
    }

    public synchronized void clearQueue(ClientSnacProcessor processor) {
        queues.remove(processor);
    }

    /**
     * A class representing a single SNAC queue for a single SNAC processor.
     */
    private static class SnacQueue {
        /** Whether or not this queue is paused. */
        private boolean paused = false;
        /**
         * A list of enqueued requests. Only nonempty if this queue is paused.
         */
        private final LinkedList queue = new LinkedList();
    }
}
