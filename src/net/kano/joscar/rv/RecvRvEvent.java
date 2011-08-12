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

package net.kano.joscar.rv;

import net.kano.joscar.DefensiveTools;
import net.kano.joscar.snac.SnacPacketEvent;
import net.kano.joscar.snaccmd.icbm.RvCommand;

/**
 * An event that occurs when a new rendezvous {@linkplain RvCommand command} or
 * {@linkplain net.kano.joscar.snaccmd.icbm.RvResponse response} is received in
 * a {@linkplain RvSession rendezvous session}.
 */
public class RecvRvEvent extends SnacPacketEvent {
    /**
     * An event type indicating that a rendezvous command was received.
     *
     * @see #getRvCommand()
     */
    public static final Object TYPE_RV = "TYPE_RV";
    /**
     * An event type indicating that a RV response was received.
     *
     * @see #getRvResponseCode
     */
    public static final Object TYPE_RESPONSE = "TYPE_RESPONSE";

    /** The type of event that this object represents. */
    private final Object type;

    /** The RV processor on which the RV command/response was received. */
    private final RvProcessor rvProcessor;
    /** The RV session on which the RV command/response was received. */
    private final RvSession rvSession;

    /** The RV command recieved, if any. */
    private final RvCommand rvCommand;
    /** The response code of the response received, if any. */
    private final int responseCode;

    /**
     * Creates a new incoming rendezvous event with the given properties and a
     * type of {@link #TYPE_RV}.
     *
     * @param other the SNAC packet event on which this event was received
     * @param processor the RV processor on which this event was recieved
     * @param session the RV session on which this event was received
     * @param command the RV command that was received
     */
    protected RecvRvEvent(SnacPacketEvent other,
            RvProcessor processor, RvSession session, RvCommand command) {
        this(TYPE_RV, other, processor, session, command, -1);

        DefensiveTools.checkNull(command, "command");
    }

    /**
     * Creates a new incoming rendezvous event with the given properties and a
     * type of {@link #TYPE_RESPONSE}.
     *
     * @param other the SNAC packet event on which this event was received
     * @param processor the RV processor on which this event was recieved
     * @param session the RV session on which this event was received
     * @param resultCode the result code of the received RV response
     */
    protected RecvRvEvent(SnacPacketEvent other,
            RvProcessor processor, RvSession session, int resultCode) {
        this(TYPE_RV, other, processor, session, null, resultCode);

        DefensiveTools.checkRange(resultCode, "resultCode", -1);
    }

    /**
     * Creates a new incoming rendezvous event with the given properties.
     *
     * @param type
     * @param other the SNAC packet event on which this event was received
     * @param rvProcessor the RV processor on which this event was recieved
     * @param rvSession the RV session on which this event was received
     * @param rvCommand the RV command that was received, if any
     * @param responseCode the result code of the received RV response
     */
    private RecvRvEvent(Object type, SnacPacketEvent other,
            RvProcessor rvProcessor, RvSession rvSession, RvCommand rvCommand,
            int responseCode) {
        super(other);

        DefensiveTools.checkNull(type, "type");
        DefensiveTools.checkNull(rvProcessor, "rvProcessor");
        DefensiveTools.checkNull(rvSession, "rvSession");

        if (type != TYPE_RV && type != TYPE_RESPONSE) {
            throw new IllegalArgumentException("type (" + type + ") must be " +
                    "one of TYPE_RV and TYPE_RESPONSE");
        }

        if (rvCommand != null && responseCode != -1) {
            throw new IllegalArgumentException("only one of rvCommand and " +
                    "responseCode can have a non-null or nonnegative value");
        }

        this.type = type;
        this.rvProcessor = rvProcessor;
        this.rvSession = rvSession;
        this.rvCommand = rvCommand;
        this.responseCode = responseCode;
    }

    /**
     * Returns the type of event that this object represents. Will be one of
     * {@link #TYPE_RV} and {@link #TYPE_RESPONSE}. If {@link #TYPE_RV},
     * the value returned by {@link #getRvResponseCode} will be <code>-1</code>
     * and the value returned by {@link #getRvCommand} will be
     * non-<code>null</code>. If {@link #TYPE_RESPONSE}, the converse will be
     * true: the value returned by {@link #getRvResponseCode} will <i>not</i> be
     * <code>-1</code> and the value returned by {@link #getRvCommand} will be
     * <code>null</code>.
     *
     * @return the event type represented by this object
     */
    public final Object getRvEventType() { return type; }

    /**
     * Returns the RV processor on which the associated RV command/response was
     * received.
     *
     * @return the RV processor for which this event occurred
     */
    public final RvProcessor getRvProcessor() { return rvProcessor; }

    /**
     * Returns the RV session on which the associated RV command/response was
     * received.
     *
     * @return the RV session on which this event occurred
     */
    public final RvSession getRvSession() { return rvSession; }

    /**
     * Returns the RV command that was received, if any. Note that this method
     * will return <code>null</code> if this event is not a {@link #TYPE_RV}
     * event.
     *
     * @return the RV command that was received, or <code>null</code> if this is
     *         not a RV command receipt event
     */
    public final RvCommand getRvCommand() { return rvCommand; }

    /**
     * Returns the RV response code that was received, if any. Note that this
     * method will return <code>-1</code> if this event is not a {@link
     * #TYPE_RESPONSE} event.
     *
     * @return the RV response code that was received, or <code>-1</code> if
     *         this is not a RV response receipt event
     */
    public final int getRvResponseCode() { return responseCode; }

    public String toString() {
        return "RecvRvEvent for " + rvSession.getScreenname() + ": " +
                (type == TYPE_RV
                ? "rvCommand=" + rvCommand
                : "rvResponseCode=" + responseCode);
    }
}
