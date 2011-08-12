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

import net.kano.joscar.CopyOnWriteArrayList;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.flap.FlapProcessor;
import net.kano.joscar.flapcmd.SnacCommand;
import net.kano.joscar.snac.ClientSnacProcessor;
import net.kano.joscar.snac.CmdType;
import net.kano.joscar.snac.OutgoingSnacRequestListener;
import net.kano.joscar.snac.SnacPacketEvent;
import net.kano.joscar.snac.SnacPacketListener;
import net.kano.joscar.snac.SnacRequestSentEvent;
import net.kano.joscar.snac.SnacRequestTimeoutEvent;
import net.kano.joscar.snac.SnacResponseEvent;
import net.kano.joscar.snac.SnacResponseListener;
import net.kano.joscar.snaccmd.conn.RateChange;
import net.kano.joscar.snaccmd.conn.RateClassInfo;
import net.kano.joscar.snaccmd.conn.RateInfoCmd;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Keeps track of the current "rate" on a SNAC connection. See {@link
 * RateClassInfo} for details on the algorithm used.
 * <br>
 * <br>
 * To use a rate monitor, one could use code such as:
 * <pre>
RateMonitor monitor = new RateMonitor(snacProcessor);
 * </pre>
 * The rate monitor will attach itself to the given SNAC processor and compute
 * the rate for each rate class without any further effort. A few notes:
 * <ul>
 * <li> <code>RateMonitor</code> assumes that the default joscar implementations
 * of {@link RateInfoCmd} and {@link RateChange} are passed to its packet
 * listeners. If you have provided {@linkplain ClientSnacProcessor#getCmdFactoryMgr
 * custom SNAC command factories} that use custom implementations of those SNAC
 * commands, you will need to call {@link #setRateClasses(RateClassInfo[])
 * setRateClasses} and {@link #updateRateClass(int, RateClassInfo)
 * updateRateClass} on your own. (Note that this issue will not be a problem for
 * the great majority of joscar users, and if you don't know what it all means,
 * ignore it.) </li>
 *  <li> <code>RateMonitor</code>'s behavior is undefined if it is attached to a
 * SNAC processor that is already connected. That is, <b>only create
 * <code>RateMonitor</code>s for <code>ClientSnacProcessor</code>s which are not
 * already connected</b>. Also note that when a SNAC processor is disconnected
 * or detached, one should either throw out any rate monitors attached to it or
 * call {@link #reset} for each. </li>
 * </ul>
 *
 * Rate monitors can themselves be monitored via rate listeners, which listen
 * for rate-related events. If any calls to these listeners' listener methods
 * produces an error, the FLAP processor underneath a given rate monitor's
 * attached SNAC processor will be notified of the error with its {@linkplain
 * net.kano.joscar.flap.FlapProcessor#handleException
 * <code>handleException</code> method}. The type of the error will be {@link
 * #ERRTYPE_RATE_LISTENER}, and the info/reason object will be the
 * <code>RateListener</code> whose listener method threw the exception.
 * <br>
 * <br>
 * Rate monitors have a concept of an "error margin" to allow for error when
 * computing whether or not a rate class is rate-limited. This value defaults
 * to {@link #ERRORMARGIN_DEFAULT}, or <code>100</code> ms as of this writing. A
 * value of <code>100</code> ms is <i>very</i> conservative value, as the
 * largest recorded deviation from the actual average is less than
 * <code>5</code> ms. The value of the error margin indicates how much error is
 * allowed, in milliseconds, between the actual rate and the computed rate. The
 * effect of setting this to, say, <code>50</code>ms, means that the rate
 * monitor will not decide that it is un-rate-limited until its rate is
 * <code>50</code>ms <i>above</i> the server-specified "rate cleared average."
 * <br>
 * <br>
 * Once rate class information has been received from the server, a {@link
 * RateClassMonitor} is created for each rate class. For more information on
 * what exactly a rate class is, see {@link RateClassInfo}. After the initial
 * rate class information is received, rate classes are never added or removed,
 * and the SNAC commands which each rate class contains cannot be changed, so it
 * is safe to keep references to <code>RateClassMonitor</code>s until new rate
 * class information is received (as on a new connection). (You can be notified
 * of rate class information's arrival by {@linkplain #addListener adding a
 * rate listener}.
 * <br>
 * <br>
 * <code>RateMonitor</code> logs to the Java Logging API namespace
 * <code>"net.kano.joscar.ratelim"</code> on the levels <code>Level.FINE</code>
 * and <code>Level.FINER</code> in order to, hopefully, ease the debugging
 * rate-limiting-related applications. For information on how to access these
 * logs, see the Java Logging API reference at the <a
 * href="http://java.sun.com/j2se">J2SE website</a>.
 * <br>
 * <br>
 * Most of the interesting rate limiting information is provided by the child of
 * this class, {@link RateClassMonitor}. To access rate information for IM's,
 * one could use code such as the following:
 * <pre>
CmdType cmd = new CmdType(IcbmCommand.FAMILY_ICBM,
        IcbmCommand.CMD_IM);
RateClassMonitor classMonitor = rateMonitor.getMonitor(type);
System.out.println("Current IM rate average: "
        + classMonitor.getCurrentAvg() + "ms");
 * </pre>
 */
public class RateMonitor {
    /**
     * An error type indicating that an exception occurred when calling a
     * rate listener method. The error info object for an error of this type
     * will be the listener that threw the exception.
     */
    public static final Object ERRTYPE_RATE_LISTENER = "ERRTYPE_RATE_LISTENER";

    /** A default rate average error margin. */
    public static final int ERRORMARGIN_DEFAULT = 100;

    /** A logger object for this class. */
    private static final Logger logger
            = Logger.getLogger("net.kano.joscar.ratelim");

    /** The SNAC processor to which this rate monitor is attached. */
    private ClientSnacProcessor snacProcessor;

    /** A list of listeners for rate-related events. */
    private final CopyOnWriteArrayList listeners = new CopyOnWriteArrayList();

    /**
     * A "listener event lock" used to prevent overlapping or other lacks of
     * synchronization of listener callbacks.
      */
    private final Object listenerEventLock = new Object();

    /** A map from rate class numbers to rate class monitors. */
    private Map classToMonitor = new HashMap(10);
    /** A map from SNAC command types to rate class monitors. */
    private Map typeToMonitor = new HashMap(500);
    /**
     * The default rate class monitor (for commands which are not a member of a
     * specific rate class).
      */
    private RateClassMonitor defaultMonitor = null;

    /** The current error margin for this rate monitor. */
    private int errorMargin = ERRORMARGIN_DEFAULT;

    /** A listener used to determine when SNAC commands are sent. */
    private OutgoingSnacRequestListener requestListener
            = new OutgoingSnacRequestListener() {
        public void handleSent(SnacRequestSentEvent e) {
            updateRate(e);
        }

        public void handleTimeout(SnacRequestTimeoutEvent event) { }
    };

    /** A listener used to handle rate information packets. */
    private SnacResponseListener responseListener
            = new SnacResponseListener() {
        public void handleResponse(SnacResponseEvent e) {
            SnacCommand cmd = e.getSnacCommand();

            if (cmd instanceof RateInfoCmd) {
                RateInfoCmd ric = (RateInfoCmd) cmd;

                setRateClasses(ric.getRateClassInfos());
            }
        }
    };
    /** A listener used to handle rate change packets. */
    private SnacPacketListener packetListener = new SnacPacketListener() {
        public void handleSnacPacket(SnacPacketEvent e) {
            SnacCommand cmd = e.getSnacCommand();

            if (cmd instanceof RateChange) {
                RateChange rc = (RateChange) cmd;

                RateClassInfo rateInfo = rc.getRateInfo();
                if (rateInfo != null) {
                    int code = rc.getChangeCode();
                    updateRateClass(code, rateInfo);
                }
            }
        }
    };

    /**
     * Creates a new rate monitor for the given SNAC processor.
     *
     * @param processor the SNAC processor whose rates should be monitored
     */
    public RateMonitor(ClientSnacProcessor processor) {
        DefensiveTools.checkNull(processor, "processor");

        this.snacProcessor = processor;

        processor.addGlobalRequestListener(requestListener);
        processor.addPacketListener(packetListener);
        processor.addGlobalResponseListener(responseListener);
    }

    /**
     * "Detaches" this rate monitor from the SNAC processor to which it is
     * attached. This monitor will stop listening for events on the given
     * processor.
     */
    public final void detach() {
        ClientSnacProcessor oldProcessor;
        synchronized(this) {
            if (snacProcessor == null) return;

            snacProcessor.removeGlobalRequestListener(requestListener);
            snacProcessor.removePacketListener(packetListener);
            snacProcessor.removeGlobalResponseListener(responseListener);

            oldProcessor = snacProcessor;
            snacProcessor = null;
        }

        synchronized(listenerEventLock) {
            for (Iterator it = listeners.iterator(); it.hasNext();) {
                RateListener l = (RateListener) it.next();

                l.detached(this, oldProcessor);
            }
        }
    }

    /**
     * Resets this rate monitor to the state it was in when first created. All
     * rate class and all rate class monitors are discarded until new rate
     * information is received after a call to this method.
     */
    public synchronized final void reset() {
        typeToMonitor.clear();
        classToMonitor.clear();
        defaultMonitor = null;

        synchronized(listenerEventLock) {
            for (Iterator it = listeners.iterator(); it.hasNext();) {
                RateListener l = (RateListener) it.next();

                l.reset(this);
            }
        }
    }

    /**
     * Returns the SNAC processor which this rate monitor is monitoring.
     *
     * @return this rate monitor's attached SNAC processor
     */
    public synchronized final ClientSnacProcessor getSnacProcessor() {
        return snacProcessor;
    }

    /**
     * Adds a listener for rate-monitor-related events.
     *
     * @param l the listener to add
     */
    public final void addListener(RateListener l) {
        DefensiveTools.checkNull(l, "l");

        listeners.addIfAbsent(l);
    }

    /**
     * Removes the given listener from this rate monitor's listener list,
     * if present. (Note that the given listener value cannot be
     * <code>null</code>.)
     *
     * @param l the listener to remove
     */
    public final void removeListener(RateListener l) {
        DefensiveTools.checkNull(l, "l");

        listeners.remove(l);
    }

    /**
     * Clears all rate information present in this rate monitor and stores the
     * given rate information. After a call to this method, the given rate
     * information will be used in calculating the rate.
     * <br>
     * <br>
     * Note that calling this method is not normally necessary as rate class
     * information is automatically set upon receiving the associated commands
     * on the attached SNAC processor. See {@linkplain RateMonitor above} for
     * details.
     *
     * @param rateInfos the list of rate class information blocks to use
     */
    public final void setRateClasses(RateClassInfo[] rateInfos) {
        DefensiveTools.checkNull(rateInfos, "rateInfos");
        rateInfos = (RateClassInfo[]) rateInfos.clone();
        DefensiveTools.checkNullElements(rateInfos, "rateInfos");

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Got rate classes for monitor " + this);
        }

        synchronized(this) {
            reset();

            for (int i = 0; i < rateInfos.length; i++) {
                RateClassInfo rateInfo = rateInfos[i];

                setRateClass(rateInfo);
            }
        }

        synchronized(listenerEventLock) {
            for (Iterator it = listeners.iterator(); it.hasNext();) {
                RateListener listener = (RateListener) it.next();

                try {
                    listener.gotRateClasses(this);
                } catch (Throwable t) {
                    handleException(ERRTYPE_RATE_LISTENER, t, listener);
                }
            }
        }
    }

    /**
     * "Sets up" a single rate class, adding its command types to the
     * {@linkplain #typeToMonitor type-to-monitor map} and the {@linkplain
     * #classToMonitor class-to-monitor map}, both pointing to a newly created
     * <code>RateClassMonitor</code> for the given rate class.
     *
     * @param rateInfo the rate class information block whose rate class is to
     *        be set up
     */
    private synchronized void setRateClass(RateClassInfo rateInfo) {
        DefensiveTools.checkNull(rateInfo, "rateInfo");

        RateClassMonitor monitor = new RateClassMonitor(this, rateInfo);
        classToMonitor.put(new Integer(rateInfo.getRateClass()), monitor);

        CmdType[] cmdTypes = rateInfo.getCommands();
        if (cmdTypes != null) {
            if (cmdTypes.length == 0) {
                // if there aren't any member SNAC commands for this rate
                // class, this is the "fallback" rate class, or the
                // "default queue"
                if (defaultMonitor == null) defaultMonitor = monitor;

            } else {
                // there are command types associated with this rate class,
                // so, for speed, we put them into a map
                for (int i = 0; i < cmdTypes.length; i++) {
                    typeToMonitor.put(cmdTypes[i], monitor);
                }
            }
        }
    }

    /**
     * Updates rate class information in this rate monitor with the given rate
     * class information block.
     *
     * Note that calling this method is not normally necessary, as rate class
     * information is automatically updated when the associated commands are
     * received from the server. See {@linkplain RateMonitor above} for details.
     *
     * @param changeCode the "change code" sent in the rate class change packet
     * @param rateInfo the rate information block sent in the rate class change
     *        packet
     */
    public void updateRateClass(int changeCode, RateClassInfo rateInfo) {
        DefensiveTools.checkRange(changeCode, "changeCode", 0);
        DefensiveTools.checkNull(rateInfo, "rateInfo");

        RateClassMonitor monitor = getMonitor(rateInfo.getRateClass());

        monitor.updateRateInfo(changeCode, rateInfo);

        synchronized(listenerEventLock) {
            for (Iterator it = listeners.iterator(); it.hasNext();) {
                RateListener listener = (RateListener) it.next();

                try {
                    listener.rateClassUpdated(this, monitor, rateInfo);
                } catch (Throwable t) {
                    handleException(ERRTYPE_RATE_LISTENER, t, listener);
                }
            }
        }
    }

    /**
     * Handles an exception by passing it to the attached SNAC processor's
     * attached FLAP processor.
     *
     * @param type the type of exception
     * @param t the exception itself
     * @param info an object providing more information about the associated
     *        exception
     *
     * @see FlapProcessor#handleException
     */
    private void handleException(Object type, Throwable t, Object info) {
        ClientSnacProcessor processor;
        synchronized(this) {
            processor = snacProcessor;
        }

        if (processor != null) {
            processor.getFlapProcessor().handleException(type, t, info);

        } else {
            logger.warning("Rate monitor couldn't process error because "
                    + "not attached to SNAC processor: " + t.getMessage()
                    + " (reason obj: " + info + ")");
        }
    }

    /**
     * Updates the rate for the rate class associated with the request
     * associated with the given event.
     *
     * @param e a SNAC request send event
     */
    private void updateRate(SnacRequestSentEvent e) {
        CmdType cmdType = CmdType.ofCmd(e.getRequest().getCommand());

        RateClassMonitor monitor = getMonitor(cmdType);

        if (monitor == null) return;

        monitor.updateRate(e.getSentTime());
    }

    /**
     * Fires a rate limit status event to all registered listeners.
     *
     * @param monitor the rate class monitor whose rate class's limited state
     *        has changed
     * @param limited whether or not the associated rate class is rate-limited
     */
    void fireLimitedEvent(RateClassMonitor monitor, boolean limited) {
        synchronized(listenerEventLock) {
            for (Iterator it = listeners.iterator(); it.hasNext();) {
                RateListener listener = (RateListener) it.next();

                try {
                    listener.rateClassLimited(this, monitor, limited);
                } catch (Throwable t) {
                    handleException(ERRTYPE_RATE_LISTENER, t, listener);
                }
            }
        }
    }


    /**
     * Sets this rate monitor's error margin. See {@linkplain RateMonitor above}
     * for details on what this means.
     *
     * @param errorMargin a new error margin
     *
     * @throws IllegalArgumentException if the given error margin is negative
     */
    public synchronized final void setErrorMargin(int errorMargin)
            throws IllegalArgumentException {
        DefensiveTools.checkRange(errorMargin, "errorMargin", 0);

        this.errorMargin = errorMargin;
    }

    /**
     * Returns this rate monitor's current error margin value. See {@linkplain
     * RateMonitor above} for details on what this means.
     *
     * @return this rate monitor's error margin value
     */
    public final synchronized int getErrorMargin() { return errorMargin; }

    /**
     * Returns the rate class monitor for the rate class identified by the given
     * rate class ID number.
     *
     * @param rateClass a rate class ID number
     * @return the rate class monitor associated with the given rate class ID
     *         number
     */
    private synchronized RateClassMonitor getMonitor(int rateClass) {
        Integer key = new Integer(rateClass);
        return (RateClassMonitor) classToMonitor.get(key);
    }

    /**
     * Returns the rate class monitor used to handle commands of the given type.
     * Note that the returned value will never be <code>null</code> unless no
     * rate information has yet been set or no default rate class was specified
     * by the server.
     *
     * @param type the type of command whose associated rate class monitor is to
     *        be returned
     * @return the rate class monitor associated with the given command type
     */
    public final synchronized RateClassMonitor getMonitor(CmdType type) {
        DefensiveTools.checkNull(type, "type");

        RateClassMonitor queue = (RateClassMonitor) typeToMonitor.get(type);

        if (queue == null) queue = defaultMonitor;

        return queue;
    }

    /**
     * Returns all of the rate class monitors currently being used.
     *
     * @return all of the rate class monitors in use in this rate monitor
     */
    public final synchronized RateClassMonitor[] getMonitors() {
        return (RateClassMonitor[])
                classToMonitor.values().toArray(new RateClassMonitor[0]);
    }
}
