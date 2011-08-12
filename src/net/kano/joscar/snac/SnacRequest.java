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

import net.kano.joscar.DefensiveTools;
import net.kano.joscar.flapcmd.SnacCommand;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Encapsulates a single outgoing SNAC request and its corresponding incoming
 * responses. See {@link ClientSnacProcessor} for details on the request system.
 * The general procedure for using a <code>SnacRequest</code> is to create a
 * <code>SnacRequest</code> containing the outgoing request you want to send
 * to the server; adding one or more listeners for future responses; and sending
 * the request over a SNAC connection with a <code>ClientSnacProcessor</code>'s
 * <code>sendSnac</code> method. Your <code>SnacRequestListener</code> (or
 * <code>SnacRequestAdapter</code>) is notified when the request is sent,
 * when it receives a response, and when it "times out."
 * <br>
 * <br>
 * When a request "times out," it is removed from its parent
 * <code>ClientSnacProcessor</code>'s request list. (This is done mainly for
 * memory conservation reasons.) If a response to the request is received after
 * it has "timed out," it will be processed as a normal SNAC packet and not as a
 * response to an outgoing request. The default time after which a request
 * "times out" is, as of this writing, ten minutes, allowing a great deal of
 * time for any response to be received. That "time to live" can be increased,
 * however (or decreased), with <code>ClientSnacProcessor</code>'s
 * {@link ClientSnacProcessor#setRequestTtl setRequestTtl} method.
 * <br>
 * <br>
 * Note that a <code>SnacRequest</code> will pass any exceptions thrown during
 * the processing of a SNAC-request-related event to the
 * <code>FlapProcessor</code> on which the events were received via its
 * <code>handleException</code> method, which will then pass it to any of your
 * registered exception handlers. The exception type will be {@link
 * #ERRTYPE_SNAC_RESPONSE_LISTENER}; see its documentation for details.
 * <br>
 * <br>
 * <code>SnacRequest</code> logs to the Java Logging API namespace
 * <code>"net.kano.joscar.snac"</code> on the level <code>Level.FINER</code>
 * in order to, hopefully, ease the debugging of SNAC request processing code.
 */
public class SnacRequest {
    /**
     * An error type indicating that an exception was thrown during the
     * processing of a SNAC request listener. In this case, the "reason" (the
     * value returned by {@link
     * net.kano.joscar.flap.FlapExceptionEvent#getReason getReason()}) will be
     * the <code>SnacRequestListener</code> which threw the exception.
     */
    public static final Object ERRTYPE_SNAC_RESPONSE_LISTENER
            = "ERRTYPE_SNAC_RESPONSE_LISTENER";

    /**
     * A logger to which to log request-related events.
     */
    private static final Logger logger
            = Logger.getLogger("net.kano.joscar.snac");

    /**
     * The initial outgoing SNAC command that this request represents.
     */
    private final SnacCommand command;

    /**
     * A list of listeners for this-request-specific events.
     */
    private List listeners = null;

    /** A lock for calling listeners' methods. */
    private final Object listenerEventLock = new Object();

    /**
     * A list of responses this request has received, just to waste memory.
     */
    private List responses = null;

    /**
     * The date at which this outgoing request was originally sent.
     */
    private long sentAt = -1;

    /** This request's request ID. */
    private long reqid = -1;

    /**
     * Whether or not we are supposed to store responses to this request
     * locally.
     */
    private boolean storingResponses = false;

    /**
     * A SNAC request response list containing no SNAC responses. This field is
     * present to avoid creating new empty arrays.
     */
    private static final SnacResponseEvent[] NO_SNAC_RESPONSES
            = new SnacResponseEvent[0];

    /**
     * Creates a new <code>SnacRequest</code> for the given command and adds the
     * given event listener to its listener list.
     *
     * @param command the outgoing SNAC command that comprises the outgoing
     *        "request" to the server
     * @param listener a listener for responses and other events related to this
     *        request, or <code>null</code> to ignore responses
     */
    public SnacRequest(SnacCommand command, SnacRequestListener listener) {
        DefensiveTools.checkNull(command, "command");

        this.command = command;
        if (listener != null) addListener(listener);
    }

    /**
     * Sets this request's request ID on its parent
     * <code>ClientSnacProcessor</code>.
     *
     * @param requestID this request's ID
     */
    final void setReqid(long requestID) { this.reqid = requestID; }

    /**
     * Returns this request's request ID on its parent
     * <code>ClientSnacProcessor</code>.
     *
     * @return this request's ID
     */
    final long getReqid() { return reqid; }

    /**
     * Adds a listener for responses and other events related to this request.
     *
     * @param l the listener to add
     */
    public synchronized final void addListener(SnacRequestListener l) {
        DefensiveTools.checkNull(l, "l");

        if (listeners == null) listeners = new ArrayList(4);

        listeners.add(l);
    }

    /**
     * Removes an event listener from this request's event listener list.
     *
     * @param l the listener to remove
     */
    public synchronized final void removeListener(SnacRequestListener l) {
        DefensiveTools.checkNull(l, "l");

        if (listeners != null) listeners.remove(l);
    }

    /**
     * Returns <code>true</code> if there are currently listeners listening
     * for events related to this request. Returns <code>false</code> if there
     * are no listeners associated with this request.
     *
     * @return whether there are listeners associated with this request
     */
    public synchronized final boolean hasListeners() {
        return listeners != null && !listeners.isEmpty();
    }

    /**
     * Returns the <code>SnacCommand</code> that comprises the actual outgoing
     * "request" to the OSCAR server.
     *
     * @return the outgoing <code>SnacCommand</code> associated with this
     *         request
     */
    public final SnacCommand getCommand() { return command; }

    /**
     * Returns whether this request object is currently set to store responses
     * received for this request. This value defaults to <code>false</code>.
     *
     * @return whether responses to this request are being stored
     *
     * @see #getResponses
     */
    public boolean isStoringResponses() { return storingResponses; }

    /**
     * Sets whether to store responses to this request in this object. Defaults
     * to <code>false</code>.
     *
     * @param storingResponses whether to store responses to this request.
     *
     * @see #getResponses
     */
    public void setStoringResponses(boolean storingResponses) {
        this.storingResponses = storingResponses;
    }

    /**
     * Returns the time at which this request was sent. The value returned is in
     * the format returned by <code>System.currentTimeMillis</code>, or
     * milliseconds since the unix epoch.
     *
     * @return the time at which this request was sent
     */
    public synchronized final long getSentTime() { return sentAt; }

    /**
     * Returns the responses received thus far to this request. Will always be
     * empty unless <code>setStoringResponses(true)</code> has been called on
     * this object.
     *
     * @return the responses received thus far for this request
     *
     * @see #setStoringResponses
     */
    public synchronized final SnacResponseEvent[] getResponses() {
        // this is for performance.
        if (responses == null || responses.isEmpty()) return NO_SNAC_RESPONSES;

        return (SnacResponseEvent[])
                responses.toArray(new SnacResponseEvent[responses.size()]);
    }

    /**
     * Called when the command associated with this request has been sent over
     * a SNAC connection.
     *
     * @param event an object describing this event
     */
    final void sent(SnacRequestSentEvent event) {
        boolean logFiner = logger.isLoggable(Level.FINER);

        if (logFiner) logger.finer("Snac request sent: " + this);

        synchronized(this) {
            sentAt = event.getSentTime();
        }

        List listeners = getListenersCopy();

        if (listeners != null) {
            synchronized(listenerEventLock) {
                for (Iterator it = listeners.iterator(); it.hasNext();) {
                    SnacRequestListener listener
                            = (SnacRequestListener) it.next();

                    if (logFiner) {
                        logger.finer("Running response listener " + listener);
                    }

                    try {
                        listener.handleSent(event);
                    } catch (Throwable t) {
                        event.getFlapProcessor().handleException(
                                ERRTYPE_SNAC_RESPONSE_LISTENER, t, listener);
                    }
                }
            }
        }

        if (logFiner) logger.finer("Finished processing Snac request send");
    }

    /**
     * Returns a copy of the list of listeners, for use in non-locking
     * iteration.
     *
     * @return a copy of the listener list
     */
    private List getListenersCopy() {
        List listeners;
        synchronized(this) {
            listeners = this.listeners;
            if (listeners != null && listeners.isEmpty()) listeners = null;
        }
        if (listeners != null) return new ArrayList(listeners);
        else return null;
    }

    /**
     * Called when a SNAC packet has been received in response to this request.
     *
     * @param event an object describing this event
     */
    final void gotResponse(SnacResponseEvent event) {
        boolean logFiner = logger.isLoggable(Level.FINER);

        if (logFiner) {
            logger.finer("Processing response " + event.getSnacPacket()
                    + " to Snac request " + this);
        }

        synchronized(this) {
            if (storingResponses) {
                if (responses == null) responses = new ArrayList(5);
                responses.add(event);
            }
        }

        List listeners = getListenersCopy();

        if (listeners != null) {
            synchronized(listenerEventLock) {
                for (Iterator it = listeners.iterator(); it.hasNext();) {
                    SnacRequestListener listener
                            = (SnacRequestListener) it.next();

                    if (logFiner) {
                        logger.finer("Running response listener " + listener);
                    }

                    try {
                        listener.handleResponse(event);
                    } catch (Throwable t) {
                        event.getFlapProcessor().handleException(
                                ERRTYPE_SNAC_RESPONSE_LISTENER, t, listener);
                    }
                }
            }
        }

        if (logFiner) logger.finer("Finished handling response");
    }

    /**
     * Called when this request "timed out." No further calls to
     * <code>sent</code> or <code>gotResponse</code> will be made after this
     * method is called.
     *
     * @param event an object describing this event
     */
    final void timedOut(SnacRequestTimeoutEvent event) {
        boolean logFiner = logger.isLoggable(Level.FINER);

        if (logFiner) logger.finer("Snac request " + this + " timed out");

        List listeners = getListenersCopy();

        if (listeners != null) {
            synchronized(listenerEventLock) {
                for (Iterator it = listeners.iterator(); it.hasNext();) {
                    SnacRequestListener listener
                            = (SnacRequestListener) it.next();

                    if (logFiner) {
                        logger.finer("Running response listener " + listener
                                + " for request timeout");
                    }

                    try {
                        listener.handleTimeout(event);
                    } catch (Throwable t) {
                        event.getFlapProcessor().handleException(
                                ERRTYPE_SNAC_RESPONSE_LISTENER, t, listener);
                    }
                }
            }
        }

        if (logFiner) logger.finer("Finished handling Snac request timeout");
    }

    public synchronized String toString() {
        return "SnacRequest for " + command + ": listeners: " + listeners
                + ", responses: " + responses;
    }
}
