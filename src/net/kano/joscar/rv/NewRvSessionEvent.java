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

/**
 * An event that occurs when a new rendezvous session is created. Note that an
 * event of this type occurs even if a new session is created manually via
 * a <code>RvProcessor</code>'s <code>createNewSession</code> method. See
 * {@linkplain #getSessionType <code>getSessionType</code> documentation} for
 * details.
 */
public class NewRvSessionEvent {
    /**
     * An event type indicating that a rendezvous session was created
     * "remotely," via an RV command with a new rendezvous session ID being
     * received. In practice, this means an initial rendezvous request was
     * received, such as a request inviting the user to a chat room.
     */
    public static final Object TYPE_INCOMING = "TYPE_INCOMING";
    /**
     * An event type indicating that a rendezvous session was created locally
     * before sending an initial RV request on that session to another user.
     * In practice, this means {@link RvProcessor#createRvSession} was called.
     *
     * @see RvProcessor#createRvSession
     */
    public static final Object TYPE_OUTGOING = "TYPE_OUTGOING";

    /** The RV processor on which the associated session was created. */
    private final RvProcessor rvProcessor;
    /** The session that was created. */
    private final RvSession newSession;
    /** The new session's "session type." */
    private final Object sessionType;

    /**
     * Creates a new new-rendezvous-session event with the given properties.
     *
     * @param rvProcessor the RV processor on which the given session was
     *        created
     * @param newSession the session that was created
     * @param sessionType the event type, like {@link #TYPE_INCOMING}
     */
    protected NewRvSessionEvent(RvProcessor rvProcessor, RvSession newSession,
            Object sessionType) {
        DefensiveTools.checkNull(rvProcessor, "rvProcessor");
        DefensiveTools.checkNull(newSession, "newSession");

        if (sessionType != TYPE_INCOMING
                && sessionType != TYPE_OUTGOING) {
            throw new IllegalArgumentException("sessionType (" + sessionType
                    + ") must be either TYPE_INCOMING or " +
                    "TYPE_OUTGOING");
        }

        this.rvProcessor = rvProcessor;
        this.newSession = newSession;
        this.sessionType = sessionType;
    }

    /**
     * Returns the RV processor on which the associated RV session was created.
     *
     * @return the RV processor on which the associated session was created
     */
    public final RvProcessor getRvProcessor() { return rvProcessor; }

    /**
     * Returns the session that was created.
     *
     * @return the session that was created
     */
    public final RvSession getSession() { return newSession; }

    /**
     * Returns the session creation event type. Will be one of {@link
     * #TYPE_INCOMING} and {@link #TYPE_OUTGOING}.
     *
     * @return the new session's "session type"
     */
    public final Object getSessionType() { return sessionType; }
}
