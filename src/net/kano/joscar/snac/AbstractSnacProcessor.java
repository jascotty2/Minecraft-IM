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
 *  File created by keith @ Jun 19, 2003
 *
 */

package net.kano.joscar.snac;

import net.kano.joscar.CopyOnWriteArrayList;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.flap.FlapPacketEvent;
import net.kano.joscar.flap.FlapProcessor;
import net.kano.joscar.flap.VetoableFlapPacketListener;
import net.kano.joscar.flapcmd.SnacCommand;
import net.kano.joscar.flapcmd.SnacFlapCmd;
import net.kano.joscar.flapcmd.SnacPacket;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides an easy interface to listening for incoming SNAC packets as well as
 * sending SNAC commands over a FLAP connection. An
 * <code>AbstractSnacProcessor</code> provides a system for "preprocessing"
 * SNAC's before they are formally handled and processed and a system for
 * listening for incoming packets (and optionally "vetoing" their further
 * processing).
 * <br>
 * <br>
 * <code>AbstractSnacProcessor</code> passes two types of exceptions thrown
 * during SNAC processing to its attached <code>FlapProcessor</code>'s error
 * handlers, using <code>FlapProcessor</code>'s <code>handleException</code>
 * method (which in turn causes the exceptions to be passed to your own error
 * handlers). The error types used are {@link #ERRTYPE_SNAC_PACKET_PREPROCESSOR}
 * and {@link #ERRTYPE_SNAC_PACKET_LISTENER}. See individual documentation for
 * each for further detail.
 * <br>
 * <br>
 * It may also be of interest to note that <code>AbstractSnacProcessor</code>
 * attaches a <i>vetoable</i> packet listener to any attached
 * <code>FlapProcessor</code>, effectively removing any incoming SNAC packet
 * from the FlapProcessor's event queue. In practice this means that if you
 * attach a SNAC processor to a <code>FlapProcessor</code> on which you are
 * listening for FLAP packets, your packet listener will not be called when a
 * channel-2 packet (SNAC packet) is received on that
 * <code>FlapProcessor</code>. Instead, it will be processed as a
 * <code>SnacPacket</code> and passed to any listeners on the SNAC processor.
 * <br>
 * <br>
 * Upon receipt of a SNAC packet, the packet is processed in the following
 * order:
 * <ol>
 * <li> It is passed through all registered SNAC preprocessors </li>
 * <li> A <code>SnacCommand</code> is generated (see <a
 * href="#factories">below</a>) </li>
 * <li> An event is passed to the subclass, like {@link ClientSnacProcessor} or
 * {@link ServerSnacProcessor}; see the subclasses' documentation for
 * details </li>
 * <li> An event is passed to each of the registered <i>vetoable</i>
 * packet listeners, halting immediately if a listener says to </li>
 * <li> If no vetoable listener has halted processing, an event is next passed
 * to all registered non-vetoable (that is, normal
 * <code>SnacPacketListener</code>) packet listeners.</li>
 * </ul>
 * </ol>
 *
 * <a name="factories"></a>The process of generating a <code>SnacCommand</code>
 * from a <code>SnacPacket</code> is as such:
 * <ol>
 * <li> First, a suitable <code>SnacCmdFactory</code> must be found
 * <ol>
 * <li> If a factory is registered for the exact command type (SNAC family and
 * command ("subtype")) of the received packet, then that factory is used </li>
 * <li> Otherwise, if a factory is registered for the entire SNAC family of the
 * received packet (via <code>registerSnacFactory(new CmdType(family),
 * factory)</code>, for example), that factory is used </li>
 * <li> Otherwise, if a factory is registered for all commands (via
 * <code>registerSnacFactory(CmdType.CMDTYPE_ALL, factory)</code>, for example),
 * then that factory is used </li>
 * <li> Otherwise, if a default SNAC factory list is set and not
 * <code>null</code>, the above three steps are repeated for the factories
 * registered by the default factory list </li>
 * </ol>
 * </li>
 * <li> If a factory has been found, a <code>SnacCommand</code> is generated
 * with a call to the factory's <code>genSnacCommand</code> method </li>
 * </ol>
 *
 * The above system allows one to customize the <code>SnacCommand</code>s passed
 * to your packet listeners, in order to, for example, process an extra field
 * in a certain command that has been added to the protocol since this library's
 * release. This can be done by registering your own SNAC command factories
 * with the appropriate command types (see {@link CmdFactoryMgr} and {@link
 * #getCmdFactoryMgr getCmdFactoryMgr}).
 * <br>
 * <br>
 * <code>AbstractSnacProcessor</code> logs to the Java Logging API namespace
 * <code>"net.kano.joscar.snac"</code> on the levels <code>Level.FINE</code>
 * and <code>Level.FINER</code> in order to, hopefully, ease the debugging
 * SNAC-related applications. For information on how to access these logs,
 * see the Java Logging API reference at the <a
 * href="http://java.sun.com/j2se">J2SE website</a>.
 */
public abstract class AbstractSnacProcessor {
    /**
     * An error type indicating that an exception was thrown while calling
     * a {@linkplain #addPreprocessor registered SNAC preprocessor} to
     * process an incoming SNAC packet. In this case, the extra error
     * information (the value returned by {@link
     * net.kano.joscar.flap.FlapExceptionEvent#getReason getReason()}) will be
     * the <code>SnacPreprocessor</code> that threw the exception.
     */
    public static final Object ERRTYPE_SNAC_PACKET_PREPROCESSOR
            = "ERRTYPE_SNAC_PACKET_PREPROCESSOR";
    /**
     * An error type indicating that an exception was thrown while calling
     * a {@linkplain #addPacketListener registered SNAC packet listener} or
     * {@linkplain #addVetoablePacketListener vetoable packet listener} to
     * handle an incoming SNAC packet. In this case, the extra error information
     * (the value returned by {@link
     * net.kano.joscar.flap.FlapExceptionEvent#getReason getReason()}) will be
     * the <code>VetoableFlapPacketListener</code> or
     * <code>FlapPacketListener</code> from whence the exception was thrown.
     */
    public static final Object ERRTYPE_SNAC_PACKET_LISTENER
            = "ERRTYPE_SNAC_PACKET_LISTENER";

    /** A logger for logging SNAC-related events. */
    private static final Logger logger
            = Logger.getLogger("net.kano.joscar.snac");

    /** Whether or not this SNAC processor is attached to a FLAP processor. */
    private boolean attached = false;

    /** The FLAP processor to which this SNAC processor is attached. */
    private FlapProcessor flapProcessor;

    /** A lock for processing a read SNAC packet. */
    private final Object readLock = new Object();

    /**
     * This SNAC processor's command factory manager, used for finding an
     * appropriate SNAC factory upon the receipt of a SNAC packet.
     */
    private final CmdFactoryMgr factories = new CmdFactoryMgr();

    /**
     * The SNAC preprocessors registered on this SNAC connection.
     */
    private final CopyOnWriteArrayList preprocessors
            = new CopyOnWriteArrayList();

    /**
     * The vetoable packet listeners registered on this SNAC connection.
     */
    private final CopyOnWriteArrayList vetoableListeners
            = new CopyOnWriteArrayList();

    /**
     * The SNAC packet listeners registered on this SNAC connection.
     */
    private final CopyOnWriteArrayList packetListeners
            = new CopyOnWriteArrayList();

    /**
     * The FLAP packet listener we add to whichever FLAP processor to which we
     * become attached.
     */
    private VetoableFlapPacketListener flapPacketListener
            = new VetoableFlapPacketListener() {
                public Object handlePacket(FlapPacketEvent e) {
                    if (e.getFlapCommand() instanceof SnacFlapCmd) {
                        if (logger.isLoggable(Level.FINER)) {
                            logger.finer("SnacProcessor intercepted channel-2 snac "
                                    + "command");
                        }

                        processPacket(e);

                        return STOP_PROCESSING_LISTENERS;
                    } else {
                        return CONTINUE_PROCESSING;
                    }
                }
            };

    /**
     * Creates a new SNAC processor attached to the given FLAP processor.
     *
     * @param flapProcessor the FLAP processor to which to attach
     */
    protected AbstractSnacProcessor(FlapProcessor flapProcessor) {
        DefensiveTools.checkNull(flapProcessor, "flapProcessor");

        this.flapProcessor = flapProcessor;
        attached = true;

        setupFlapProcessor();
    }

    /**
     * Sets up the attached FLAP processor by adding to it this SNAC processor's
     * FLAP packet listener.
     */
    private void setupFlapProcessor() {
        getFlapProcessor().addVetoablePacketListener(flapPacketListener);
    }

    /**
     * Resets the attached FLAP processor by removing from it this SNAC
     * processor's FLAP packet listener.
     */
    private void resetFlapProcessor() {
        getFlapProcessor().removeVetoablePacketListener(flapPacketListener);
    }

    /**
     * "Reattaches" or "migrates" to the given FLAP processor. This method
     * intends to leave the "old" FLAP processor as it was before this SNAC
     * processor attached to it, and to leave this SNAC processor as if it had
     * originally been attached to the given processor.
     *
     * @param processor the FLAP processor to which to migrate
     */
    protected synchronized void migrate(FlapProcessor processor) {
        DefensiveTools.checkNull(processor, "processor");

        if (!attached) {
            throw new IllegalStateException("cannot migrate when no longer "
                    + "attached");
        }

        resetFlapProcessor();

        flapProcessor = processor;

        setupFlapProcessor();
    }

    /**
     * Returns the FLAP processor to which this SNAC processor is attached.
     *
     * @return this SNAC processor's FLAP processor
     */
    public synchronized final FlapProcessor getFlapProcessor() {
        return flapProcessor;
    }

    /**
     * Returns whether this SNAC processor is attached to its FLAP processor.
     * (An attached SNAC processor handles SNAC FLAP packets.)
     *
     * @return whether this SNAC processor is attached to its FLAP processor
     *
     * @see #detach()
     */
    public synchronized final boolean isAttached() { return attached; }

    /**
     * Detaches from the current FLAP processor without clearing any queued
     * SNAC requests.
     */
    public synchronized void detach() {
        if (!attached) return;

        resetFlapProcessor();
        attached = false;
    }

    /**
     * Adds a packet listener to listen for incoming SNAC packets.
     *
     * @param l the listener to add
     */
    public final void addPacketListener(SnacPacketListener l) {
        DefensiveTools.checkNull(l, "l");

        packetListeners.addIfAbsent(l);
    }

    /**
     * Removes a packet listener from the list of listeners.
     *
     * @param l the listener to remove
     */
    public final void removePacketListener(SnacPacketListener l) {
        DefensiveTools.checkNull(l, "l");

        packetListeners.remove(l);
    }

    /**
     * Adds a <i>vetoable</i> packet listener to this SNAC processor. A vetoable
     * SNAC packet listener has the ability to halt the processing of a given
     * packet upon its receipt.
     *
     * @param l the listener to add.
     */
    public final void addVetoablePacketListener(VetoableSnacPacketListener l) {
        DefensiveTools.checkNull(l, "l");

        vetoableListeners.addIfAbsent(l);
    }

    /**
     * Removes a vetoable packet listener from the list of listeners.
     * @param l the listener to remove
     */
    public final void removeVetoablePacketListener(
            VetoableSnacPacketListener l) {
        DefensiveTools.checkNull(l, "l");

        vetoableListeners.remove(l);
    }

    /**
     * Adds a SNAC preprocessor to the list of preprocessors. Preprocessors
     * are the first listeners called when a SNAC packet arrives, and are
     * allowed to modify the contents of a packet.
     *
     * @param p the preprocessor to add
     */
    public final void addPreprocessor(SnacPreprocessor p) {
        DefensiveTools.checkNull(p, "p");

        preprocessors.addIfAbsent(p);
    }

    /**
     * Removes a SNAC preprocessor from the list of SNAC preprocessors.
     * @param p the preprocessor to remove
     */
    public final void removePreprocessor(SnacPreprocessor p) {
        DefensiveTools.checkNull(p, "p");

        preprocessors.remove(p);
    }

    /**
     * Returns this SNAC processor's SNAC command factory manager.
     *
     * @return this SNAC processor's SNAC command factory manager
     */
    public final CmdFactoryMgr getCmdFactoryMgr() { return factories; }

    /**
     * Returns whether or not the given SNAC packet event should be passed to
     * packet listeners. Note that when this method is called, preprocessors
     * have already been invoked. (Note that the default implementation always
     * returns <code>true</code>.)
     *
     * @param event the event
     * @return whether or not the given SNAC packet event should be passed to
     *         vetoable and normal packet listeners
     */
    protected boolean continueHandling(SnacPacketEvent event) { return true; }

    /**
     * Processes an incoming FLAP packet. The packet is processed through
     * the list of preprocessors, a SnacCommand is generated, vetoable listeners
     * are called, and, finally, packet listeners are called.
     *
     * @param e the FLAP packet event to process
     */
    private void processPacket(FlapPacketEvent e) {
        boolean logFine = logger.isLoggable(Level.FINE);
        boolean logFiner = logger.isLoggable(Level.FINER);

        FlapProcessor processor;
        synchronized(this) {
            if (!attached) return;
            processor = flapProcessor;
        }

        SnacFlapCmd flapCmd = ((SnacFlapCmd) e.getFlapCommand());
        SnacPacket snacPacket = flapCmd.getSnacPacket();

        synchronized(readLock) {
            MutableSnacPacket mutablePacket = null;
            for (Iterator it = preprocessors.iterator(); it.hasNext();) {
                SnacPreprocessor preprocessor = (SnacPreprocessor) it.next();

                if (mutablePacket == null) {
                    mutablePacket = new MutableSnacPacket(snacPacket);
                }

                if (logFiner) {
                    logger.finer("Running snac preprocessor " + preprocessor);
                }

                try {
                    preprocessor.process(mutablePacket);

                } catch (Throwable t) {
                    if (logFiner) {
                        logger.finer("Preprocessor " + preprocessor
                                + " threw exception " + t);
                    }
                    processor.handleException(ERRTYPE_SNAC_PACKET_PREPROCESSOR,
                            t, preprocessor);
                    continue;
                }
            }

            if (mutablePacket != null && mutablePacket.isChanged()) {
                snacPacket = mutablePacket.toSnacPacket();
            }

            SnacCommand cmd = generateSnacCommand(snacPacket);

            if (logFine) {
                logger.fine("Converted Snac packet " + snacPacket + " to "
                        + cmd);
            }

            SnacPacketEvent event = new SnacPacketEvent(e, this, snacPacket,
                    cmd);
            if (!continueHandling(event)) return;

            for (Iterator it = vetoableListeners.iterator(); it.hasNext();) {
                VetoableSnacPacketListener listener
                        = (VetoableSnacPacketListener) it.next();

                if (logFiner) {
                    logger.finer("Running vetoable Snac packet listener "
                            + listener);
                }

                Object result;
                try {
                    result = listener.handlePacket(event);
                } catch (Throwable t) {
                    processor.handleException(ERRTYPE_SNAC_PACKET_LISTENER, t,
                            listener);
                    continue;
                }
                if (result != VetoableSnacPacketListener.CONTINUE_PROCESSING) {
                    return;
                }
            }

            for (Iterator it = packetListeners.iterator(); it.hasNext();) {
                SnacPacketListener listener = (SnacPacketListener) it.next();

                if (logFiner) {
                    logger.finer("Running Snac packet listener " + listener);
                }

                try {
                    listener.handleSnacPacket(event);
                } catch (Throwable t) {
                    processor.handleException(ERRTYPE_SNAC_PACKET_LISTENER, t,
                            listener);
                }
            }

            if (logFiner) logger.finer("Finished processing Snac");
        }
    }

    /**
     * Generates a <code>SnacCommand</code> from the given
     * <code>SnacPacket</code> using the user-registered and default factories.
     *
     * @param packet the packet from which the <code>SnacCommand</code> should
     *        be generated
     * @return an appropriate <code>SnacCommand</code> for the given packet
     */
    private SnacCommand generateSnacCommand(SnacPacket packet) {
        CmdType type = new CmdType(packet.getFamily(), packet.getCommand());

        SnacCmdFactory factory = factories.findFactory(type);

        if (factory == null) return null;

        return factory.genSnacCommand(packet);
    }

    /**
     * Sends the given SNAC command with the given SNAC request ID over the
     * currently attached FLAP processor.
     *
     * @param reqid the request ID of the SNAC packet to send
     * @param cmd the SNAC command to use in generating the SNAC packet
     */
    protected final void sendSnac(long reqid, SnacCommand cmd) {
        FlapProcessor flapProcessor;
        synchronized(this) {
            if (!attached) return;
            flapProcessor = getFlapProcessor();
        }
        flapProcessor.sendFlap(new SnacFlapCmd(reqid, cmd));
    }
}
