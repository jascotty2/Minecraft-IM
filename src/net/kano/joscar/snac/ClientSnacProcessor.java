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
 *  File created by keith @ Feb 17, 2003
 *
 */

package net.kano.joscar.snac;

import net.kano.joscar.CopyOnWriteArrayList;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.MiscTools;
import net.kano.joscar.SeqNum;
import net.kano.joscar.flap.FlapProcessor;
import net.kano.joscar.flapcmd.SnacCommand;
import net.kano.joscar.flapcmd.SnacPacket;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A client-side SNAC processor. In addition to the functionality provided by
 * {@link AbstractSnacProcessor}, a <code>ClientSnacProcessor</code> provides a
 * request-response system (see {@link SnacRequest}, {@link #sendSnac sendSnac})
 * with automatic request ID generation and management.
 * <br>
 * <br>
 * <code>ClientSnacProcessor</code> passes two additional (see {@link
 * AbstractSnacProcessor}) types of exceptions thrown during SNAC processing to
 * its attached <code>FlapProcessor</code>'s error handlers using
 * <code>FlapProcessor</code>'s <code>handleException</code> method, which
 * in turn causes the exceptions to be passed to your own error handlers. The
 * extra error types used are {@link #ERRTYPE_SNAC_REQUEST_LISTENER} and {@link
 * #ERRTYPE_SNAC_RESPONSE_LISTENER}. See individual documentation for each for
 * further detail.
 * <br>
 * <br>
 * A <code>ClientSnacProcessor</code> intercepts incoming SNAC packets if they
 * match the request ID of a previous outgoing request (see below for details).
 * The process is as follows:<br>
 * If the request ID matches that of a previous outgoing request,
 * <ul>
 * <li> An event is passed to each of the processor's {@linkplain
 * #addGlobalResponseListener global response listeners} </li>
 * <li> An event is passed to that request's listeners (see {@link
 * SnacRequest}) </li>
 * <li> Internal processing of the packet stops </li>
 * </ul>
 *
 * And finally, a bit about SNAC requests. The OSCAR protocol and SNAC data
 * structure are defined such that each individual SNAC packet has its own
 * request ID, a four-byte integer (or any other way you want to represent it,
 * actually). This combined with one other aspect of the protocol allows for
 * interesting things to be done with regard to automated connection handling.
 * That other aspect is that for any command sent to an OSCAR server, all
 * direct responses to that command that are sent back to the client have the
 * <i>same</i> request ID as the original request. This means that, for example,
 * an application can request two user directory searches at once and display
 * the results to each in separate windows, all based on their request ID. This
 * also allows for more complicated things like determining network lag between
 * an OSCAR server by matching up request ID's with packet send times. See
 * {@link SnacRequest} for more information on how to utilize the request
 * system.
 * <br>
 * <br>
 * <code>ClientSnacProcessor</code> logs to the Java Logging API namespace
 * <code>"net.kano.joscar.snac"</code> on the levels <code>Level.FINE</code>
 * and <code>Level.FINER</code> in order to, hopefully, ease the debugging
 * SNAC-related applications. For information on how to access these logs,
 * see the Java Logging API reference at the <a
 * href="http://java.sun.com/j2se">J2SE website</a>.
 */
public class ClientSnacProcessor extends AbstractSnacProcessor {
    /** A logger for logging SNAC-related events. */
    private static final Logger logger
            = Logger.getLogger("net.kano.joscar.snac");

    /**
     * An error type indicating that an exception was thrown while calling a
     * {@linkplain SnacRequest SNAC request} {@linkplain SnacRequestListener
     * response listener} to handle a response to a SNAC request or another
     * request-related event. In this case, the extra error information (the
     * value returned by {@link
     * net.kano.joscar.flap.FlapExceptionEvent#getReason getReason()}) will be
     * the <code>SnacRequest</code> whose listener threw the exception.
     */
    public static final Object ERRTYPE_SNAC_REQUEST_LISTENER
            = "ERRTYPE_SNAC_REQUEST_LISTENER";

    /**
     * An error type indicating that an exception was thrown while calling a
     * {@linkplain #addGlobalResponseListener global SNAC response listener} to
     * handle a response to a SNAC request. In this case, the extra error
     * information (the value returned by {@link
     * net.kano.joscar.flap.FlapExceptionEvent#getReason getReason()}) will be
     * the <code>SnacResponseListener</code> that threw the exception.
     */
    public static final Object ERRTYPE_SNAC_RESPONSE_LISTENER
            = "ERRTYPE_SNAC_RESPONSE_LISTENER";

    /**
     * The default SNAC request "time to live," in seconds.
     *
     * @see #setRequestTtl
     */
    public static final int REQUEST_TTL_DEFAULT = 15*60;

    /** The minimum request ID value. */
    public static final long REQID_MIN = 0;

    /** The maximum request ID value before it wraps to {@link #REQID_MIN}. */
    public static final long REQID_MAX = 0x80000000L - 1L;

    /** An object used to generate sequential SNAC request ID's. */
    private final SeqNum reqid = new SeqNum(REQID_MIN, REQID_MAX);

    /** A lock for processing a request event (timeout, sent, etc.). */
    private final Object requestEventLock = new Object();

    /**
     * The outgoing SNAC request listeners registered on this SNAC connection.
     */
    private final CopyOnWriteArrayList requestListeners
            = new CopyOnWriteArrayList();

    /**
     * The SNAC request response listeners registered on this SNAC connection.
     */
    private final CopyOnWriteArrayList responseListeners
            = new CopyOnWriteArrayList();

    /** The "time to live" of SNAC requests. */
    private int requestTtl = REQUEST_TTL_DEFAULT;

    /**
     * A map from request ID's (<code>Integer</code>s) to
     *  <code>RequestInfo</code>s, which contain <code>SnacRequest</code>s.
     */
    private final Map requests = new HashMap();

    /** A list of requests that have been sent (not just queued). */
    private final List requestQueue = new LinkedList();

    /** Whether or not this SNAC connection is currently paused. */
    private boolean paused = false;

    /** A SNAC queue manager for this processor. */
    private SnacQueueManager queueManager;
    {
        setSnacQueueManager(null);
    }

    /**
     * Creates a SNAC processor with no SNAC factories installed and the default
     * request time-to-live, attached to the given FLAP processor.
     *
     * @param processor the FLAP processor to attach to
     */
    public ClientSnacProcessor(FlapProcessor processor) {
        super(processor);
    }

    /**
     * Pauses this SNAC processor. A paused SNAC processor does not send any
     * SNAC commands to the server until a call to {@link #unpause()}. Note that
     * if this method is called while the processor is already paused, no action
     * will be taken. Note that SNAC commands can still be {@linkplain #sendSnac
     * sent} while the processor is paused; however, they will not be sent to
     * the server until unpausing.
     *
     * @see #unpause()
     * @see #isPaused()
     *
     * @see net.kano.joscar.snaccmd.conn.PauseCmd
     */
    public synchronized final void pause() {
        if (paused) return;

        queueManager.pause(this);

        paused = true;
    }

    /**
     * Unpauses this SNAC processor if previously paused with a call to {@link
     * #pause}. SNAC commands sent during the paused period will begin to be
     * sent to the server (depending on the implementation of the {@linkplain
     * #setSnacQueueManager queue manager}).
     *
     * @see #pause()
     * @see #isPaused()
     *
     * @see net.kano.joscar.snaccmd.conn.ResumeCmd
     */
    public synchronized final void unpause() {
        if (!paused) return;

        queueManager.unpause(this);

        paused = false;
    }

    /**
     * Returns whether this SNAC processor is currently paused.
     *
     * @return whether this SNAC processor is currently paused
     *
     * @see #pause
     */
    public synchronized final boolean isPaused() { return paused; }

    /**
     * Attaches to the given FLAP processor without clearing any SNAC queues.
     * Effectively "moves" this SNAC connection transparently to the given
     * processor. Note that if this processor is {@linkplain #pause paused},
     * a call to <code>migrate</code> will <i>not</i> unpause it. Unpausing must
     * be done explicitly with a call to {@link #unpause}.
     *
     * @param processor a new FLAP processor to use for this SNAC connection
     *
     * @see net.kano.joscar.snaccmd.conn.MigrationNotice
     */
    public final void migrate(FlapProcessor processor) {
        super.migrate(processor);
    }

    /**
     * Adds a global request listener to listen for outgoing SNAC requests sent
     * on this connection. The given listener will be used as if it had been
     * manually added to each outgoing request.
     *
     * @param l the listener to add
     */
    public final void addGlobalRequestListener(OutgoingSnacRequestListener l) {
        DefensiveTools.checkNull(l, "l");

        requestListeners.addIfAbsent(l);
    }

    /**
     * Removes a global request listener from the list of listeners.
     *
     * @param l the listener to remove
     */
    public final void removeGlobalRequestListener(
            OutgoingSnacRequestListener l) {
        DefensiveTools.checkNull(l, "l");

        requestListeners.remove(l);
    }

    /**
     * Adds a "global response listener" to listen for incoming SNAC request
     * responses. The given listener will be notified of any incoming responses
     * to previously sent outgoing SNAC requests. See {@linkplain
     * ClientSnacProcessor above} for details on when global response listeners'
     * event handling methods are called.
     *
     * @param l the listener to add
     */
    public final void addGlobalResponseListener(SnacResponseListener l) {
        DefensiveTools.checkNull(l, "l");

        responseListeners.addIfAbsent(l);
    }

    /**
     * Removes a "global response listener" from the list of listeners.
     *
     * @param l the listener to remove
     */
    public final void removeGlobalResponseListener(SnacResponseListener l) {
        DefensiveTools.checkNull(l, "l");

        responseListeners.remove(l);
    }

    /**
     * Sets this SNAC processor's SNAC queue manager. A SNAC queue manager
     * has almost complete control over when individual SNAC commands are
     * actually sent to the server. If <code>mgr</code> is <code>null</code>,
     * as is the default value, all SNACs will be sent to the server immediately
     * (the queue manager will be set to an {@link ImmediateSnacQueueManager})
     *
     * @param mgr the new SNAC queue manager, or <code>null</code> to send all
     *        SNACs immediately
     */
    public synchronized final void setSnacQueueManager(SnacQueueManager mgr) {
        if (queueManager != null) {
            // tell the old queue manager to forget about us
            queueManager.clearQueue(this);
            queueManager.detached(this);
        }

        // we allow null for the manager argument, which means we use our own
        // immediate SNAC queue manager
        if (mgr == null) mgr = new ImmediateSnacQueueManager();

        queueManager = mgr;

        queueManager.attached(this);

        // keep everything synchronized
        if (paused) mgr.pause(this);
    }

    /**
     * Calls {@link #setSnacQueueManager setSnacQueueManager(null)} if and only
     * if the given <code>mgr</code> is the current SNAC queue manager. If the
     * given SNAC queue manager is not the current SNAC queue manager, no change
     * takes place.
     *
     * @param mgr the SNAC queue manager to unset, if set
     */
    public synchronized final void unsetSnacQueueManager(SnacQueueManager mgr) {
        if (queueManager == mgr) setSnacQueueManager(null);
    }

    /**
     * Returns this SNAC processor's current SNAC queue manager. Note that
     * this value will <i>never</i> be <code>null</code>, even after an explicit
     * call to {@link #setSnacQueueManager setSnacQueueManager(null)}. See that
     * method's documentation for details.
     *
     * @return this SNAC processor's current SNAC queue manager
     */
    public synchronized final SnacQueueManager getSnacQueueManager() {
        return queueManager;
    }

    /**
     * Sets the "time to live" for SNAC requests, in seconds. After roughly this
     * amount of time, SNAC requests will be removed from the request list,
     * and any future responses will be processed as if they were normal
     * <code>SnacPacket</code>s and not responses to requests.
     * <br>
     * <br>
     * Note that this value must be at least zero. A value of zero enables
     * several special cases to use less memory and CPU time involved in sending
     * SNAC requests. A value of zero also means that SNAC requests' listeners
     * will <i>never</i> be called with responses, as request ID's are not
     * stored at all. Additionally, with a value of zero, SNAC requests'
     * listeners will <i>never</i> be called with timeout events, as all
     * requests will "time out" immediately.
     *
     * @param requestTtl the new "time to live" for SNAC requests, in seconds
     */
    public synchronized void setRequestTtl(int requestTtl) {
        DefensiveTools.checkRange(requestTtl, "requestTtl", 0);

        this.requestTtl = requestTtl;
    }

    /**
     * Returns the current "time to live" for SNAC requests, in seconds.
     *
     * @return the current SNAC request "time to live"
     */
    public synchronized int getRequestTtl() { return requestTtl; }

    /**
     * Removes the given request from the request list and passes a
     * <code>SnacRequestTimeoutEvent</code> to the request itself.
     *
     * @param reqInfo the request to timeout
     */
    private final void timeoutRequest(RequestInfo reqInfo) {
        FlapProcessor processor;
        int ttl;
        synchronized(this) {
            processor = getFlapProcessor();
            ttl = requestTtl;
        }
        SnacRequest request = reqInfo.getRequest();
        SnacRequestTimeoutEvent event = new SnacRequestTimeoutEvent(
                processor, this, request, ttl);

        if (logger.isLoggable(Level.FINER)) {
            logger.finer("Snac request timed out: " + request);
        }

        synchronized(requestEventLock) {
            // tell the global listeners
            if (!requestListeners.isEmpty()) {
                for (Iterator it = requestListeners.iterator(); it.hasNext();) {
                    OutgoingSnacRequestListener l
                            = (OutgoingSnacRequestListener) it.next();

                    try {
                        l.handleTimeout(event);
                    } catch (Throwable t) {
                        processor.handleException(ERRTYPE_SNAC_REQUEST_LISTENER,
                                t, l);
                    }
                }
            }

            // tell the request itself
            request.timedOut(event);
        }
    }

    /**
     * "Times out" all requests on the request list.
     */
    private final void clearAllRequests() {
        synchronized(requests) {
            for (Iterator it = requests.values().iterator(); it.hasNext();) {
                RequestInfo reqInfo = (RequestInfo) it.next();

                timeoutRequest(reqInfo);
            }

            requests.clear();
            requestQueue.clear();
        }
    }

    /**
     * Removes any SNAC requests who were sent long enough ago such that their
     * lifetime has passed the {@link #requestTtl}.
     */
    private final void cleanRequests() {
        int ttl;
        synchronized(this) {
            ttl = requestTtl;
        }

        List timedout = new LinkedList();
        synchronized(requests) {
            if (requestQueue.isEmpty()) return;

            if (ttl == 0) {
                clearAllRequests();
                return;
            }

            long time = System.currentTimeMillis();

            long ttlms = ttl * 1000;

            for (Iterator it = requestQueue.iterator(); it.hasNext();) {
                RequestInfo reqInfo = (RequestInfo) it.next();

                long sentTime = reqInfo.getSentTime();
                if (sentTime == -1) continue;

                long diff = time - sentTime;

                // these are in the order in which they were sent, so once we've
                // found one that was sent more recently than the TTL states,
                // we've found them all. so we break.
                if (diff < ttlms) break;

                // queue this request up to be timed out
                timedout.add(reqInfo);

                // and remove the request from the queue and the reqid map
                it.remove();
                requests.remove(new Long(reqInfo.getRequest().getReqid()));
            }
        }

        // we time out the requests outside of the lock
        for (Iterator it = timedout.iterator(); it.hasNext();) {
            RequestInfo reqInfo = (RequestInfo) it.next();

            timeoutRequest(reqInfo);
        }
    }

    /**
     * Sends the given <code>SnacRequest</code> to the attached FLAP connection.
     * It may not immediately be sent in future releases due to possible
     * features such as rate limiting prevention.
     *
     * @param request the SNAC request to send
     */
    public final void sendSnac(SnacRequest request) {
        DefensiveTools.checkNull(request, "request");

        SnacCommand command = request.getCommand();

        RequestInfo reqInfo = registerSnacRequest(request);

        long reqid = reqInfo.getRequest().getReqid();

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Queueing Snac request #" + reqid + ": " + command);
        }

        SnacQueueManager queueMgr;
        synchronized(this) {
            queueMgr = queueManager;
        }
        queueMgr.queueSnac(this, request);

        if (logger.isLoggable(Level.FINER)) {
            logger.finer("Finished queueing Snac request #" + reqid);
        }
    }

    /**
     * Sends the given SNAC request to the server, bypassing the SNAC request
     * queue and any {@linkplain #pause pausing} status that may be present.
     * Note that using this method for normal SNAC command sending is not
     * recommended for the reasons above.
     *
     * @param request the request to send
     *
     * @see #setSnacQueueManager
     */
    public final void sendSnacImmediately(SnacRequest request) {
        DefensiveTools.checkNull(request, "request");

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Sending SNAC request " + request);
        }

        RequestInfo reqInfo = registerSnacRequest(request);

        if (reqInfo.getSentTime() != -1) {
            throw new IllegalArgumentException("SNAC request " + request
                    + " was already sent");
        }

        int ttl;
        synchronized(this) {
            ttl = requestTtl;
        }

        long reqid = reqInfo.getRequest().getReqid();
        sendSnac(reqid, request.getCommand());

        fireSentEvent(reqInfo);

        synchronized(requests) {
            if (ttl != 0) {
                requestQueue.add(reqInfo);
            } else {
                requests.remove(new Long(reqid));
            }
        }

        if (logger.isLoggable(Level.FINER)) {
            logger.finer("Finished sending SNAC request " + request);
        }
    }

    /**
     * Performs the various tasks necessary for notifying any request listeners
     * (including our own <code>RequestInfo</code>) that a SNAC request has been
     * sent.
     *
     * @param reqInfo the request information object whose request was sent
     */
    private void fireSentEvent(RequestInfo reqInfo) {
        SnacRequest request = reqInfo.getRequest();
        long now = System.currentTimeMillis();

        // record the sent time locally
        reqInfo.sent(now);

        // create the event object
        FlapProcessor processor;
        synchronized(this) {
            processor = getFlapProcessor();
        }

        SnacRequestSentEvent event = new SnacRequestSentEvent(processor,
                this, request, now);

        synchronized(requestEventLock) {
            // tell the global request listeners first
            if (!requestListeners.isEmpty()) {
                for (Iterator it = requestListeners.iterator(); it.hasNext();) {
                    OutgoingSnacRequestListener l
                            = (OutgoingSnacRequestListener) it.next();

                    try {
                        l.handleSent(event);
                    } catch (Throwable t) {
                        processor.handleException(ERRTYPE_SNAC_REQUEST_LISTENER,
                                t, l);
                    }
                }
            }

            // then tell the request itself about it
            request.sent(event);
        }
    }

    /**
     * Registers a SNAC request, giving it a request ID and remembering that ID
     * for future reference. If the given request has already been registered,
     * no change takes place, but its corresponding <code>RequestInfo</code> is
     * still returned.
     *
     * @param request the request to register
     * @return a <code>RequestInfo</code> corresponding to the given request
     */
    private RequestInfo registerSnacRequest(SnacRequest request) {
        synchronized(requests) {
            if (request.getReqid() != -1) {
                return (RequestInfo) requests.get(new Long(request.getReqid()));
            }

            long id = reqid.next();

            request.setReqid(id);

            Long key = new Long(id);

            cleanRequests();

            RequestInfo reqInfo = new RequestInfo(request);

            requests.put(key, reqInfo);

            return reqInfo;
        }
    }

    /**
     * Detaches from the currently attached FLAP processor, if any. This method
     * effectively <b>resets this SNAC processor</b>, causing any information
     * about the current connection such as queued SNAC commands or SNAC request
     * ID's to be <b>discarded</b>. This method is thus <b>not</b> useful for
     * {@linkplain net.kano.joscar.snaccmd.conn.MigrationNotice migrating}. Note
     * that this processor will be unpaused if it is currently paused.
     *
     * @see #migrate
     */
    public synchronized final void detach() {
        if (!isAttached()) return;

        super.detach();

        paused = false;

        queueManager.clearQueue(this);
    }

    protected final boolean continueHandling(SnacPacketEvent event) {
        boolean logFiner = logger.isLoggable(Level.FINER);

        FlapProcessor processor = event.getFlapProcessor();

        SnacPacket snacPacket = event.getSnacPacket();

        Long key = new Long(snacPacket.getReqid());
        RequestInfo reqInfo;
        synchronized(requests) {
            reqInfo = (RequestInfo) requests.get(key);
        }

        if (reqInfo == null) return true;

        SnacRequest request = reqInfo.getRequest();
        if (logFiner) {
            logger.finer("This Snac packet is a response to a request!");
        }

        SnacResponseEvent sre = new SnacResponseEvent(event, request);

        if (!responseListeners.isEmpty()) {
            for (Iterator it = responseListeners.iterator(); it.hasNext();) {
                SnacResponseListener l = (SnacResponseListener) it.next();

                try {
                    l.handleResponse(sre);
                } catch (Throwable t) {
                    processor.handleException(ERRTYPE_SNAC_RESPONSE_LISTENER,
                            t, l);
                }
            }
        }

        try {
            request.gotResponse(sre);

        } catch (Throwable t) {
            processor.handleException(ERRTYPE_SNAC_REQUEST_LISTENER, t,
                    request);
        }

        return false;
    }

    /**
     * A simple class holding a SNAC request and related request-specific
     * information.
     */
    private static class RequestInfo {
        /**
         * The SNAC request with which this object is associated.
         */
        private final SnacRequest request;

        /**
         * The number of milliseconds between the unix epoch and the time at
         * which this <code>RequestInfo</code>'s request was sent.
         */
        private long sent = -1;

        /**
         * Creates a new <code>RequestInfo</code> for the given request.
         *
         * @param request the request to associate with this
         *        <code>RequestInfo</code>
         */
        public RequestInfo(SnacRequest request) {
            this.request = request;
        }

        /**
         * Marks the request associated with this object as sent.
         *
         * @param time the time at which the request was sent
         * @throws IllegalStateException if this object's associated request has
         *         already been sent
         */
        public synchronized final void sent(long time)
                throws IllegalStateException {
            if (sent != -1) {
                throw new IllegalStateException(request + " was already sent " +
                        ((System.currentTimeMillis() - sent)/1000)
                        + " seconds ago");
            }
            sent = time;
        }

        /**
         * Returns the request associated with this <code>RequestInfo</code>.
         *
         * @return this <code>RequestInfo</code>'s associated SNAC request
         */
        public final SnacRequest getRequest() { return request; }

        /**
         * Returns the time, in milliseconds since unix epoch, at which the
         * associated request was sent. If this request has not yet been sent,
         * <code>-1</code> will be returned.
         *
         * @return the time at which the associated SNAC request was sent, or
         *         <code>-1</code> if it was not yet sent
         */
        public synchronized final long getSentTime() { return sent; }

        public String toString() {
            return "Request " + MiscTools.getClassName(request.getCommand())
                    + ": " + (sent == -1 ? "not sent"
                    : "sent " + ((System.currentTimeMillis() - sent) / 1000)
                    + "s ago");
        }
    }
}
