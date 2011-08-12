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
 *  File created by Keith @ 3:52:28 PM
 *
 */

package net.kano.joscar.rv;

import net.kano.joscar.snaccmd.icbm.RvCommand;

/**
 * Represents a single "rendezvous session."
 * <br>
 * <br>
 * Briefly, every rendezvous command
 * sent between two buddies contains a "session ID" which is used to group
 * a set of commands logically. For example, consider a situation where Alice
 * sends Bob a file send request, Bob sends an acceptance command back, and then
 * halfway through getting the file Bob cancels the file transfer, sending a
 * rejection command. Each of the request, acceptance, and rejection commands
 * are <i>rendezvous commands</i>, and each command contains the same "session
 * ID" as the initial file send request Alice sent.
 * <br>
 * <br>
 * These "sessions" are never formally created or ended on the OSCAR protocol
 * level, but this author decided creating a class for it would be the best way
 * to do it. Thus, to send a rendezvous using a <code>RvProcessor</code>, first
 * one must create a new <code>RvSession</code> (see {@link
 * RvProcessor#createRvSession}) and then send the RV commands through that.
 * <br>
 * <br>
 * You may ask why something so obviously functional and not prone to extension
 * is an interface. I did this for two reasons: firstly, an RV session is a very
 * abstract thing -- as I said earlier, sessions are never created or destroyed
 * formally in the OSCAR protocol. Secondly, I did it to avoid what would
 * otherwise be the tediously long type name <code>RvProcessor.RvSession</code>.
 * Besides, RV sessions shouldn't necessarily be so deeply intertwined with
 * RV processors as to force their association by using that type name.
 */
public interface RvSession {
    /**
     * Adds a listener for incoming events on this session.
     *
     * @param l the listener to add
     */
    void addListener(RvSessionListener l);

    /**
     * Removes the given listener from this session's listener list.
     *
     * @param l the listener to remove
     */
    void removeListener(RvSessionListener l);

    /**
     * Returns the RV processor on which this session resides.
     *
     * @return the RV processor on which this session resides.
     */
    RvProcessor getRvProcessor();

    /**
     * Returns the RV session ID of this session.
     *
     * @return this session's RV session ID
     */
    long getRvSessionId();

    /**
     * Returns the screenname of the user with whom this session exists.
     *
     * @return the screenname of the user with whom this session exists
     */
    String getScreenname();

    /**
     * Sends the given RV command to the user with whom this session exists,
     * with an ICBM message ID of <code>0</code>.
     * <br>
     * <br>
     * Note that calling this method will have <i>no effect</i> if the
     * underlying <code>RvProcessor</code> is not currently attached to a SNAC
     * connection. See {@link RvProcessor#attachToSnacProcessor} for details.
     *
     * @param command the RV command to send
     *
     * @see net.kano.joscar.snaccmd.icbm.SendRvIcbm
     * @see #sendRv(RvCommand, long)
     */
    void sendRv(RvCommand command);

    /**
     * Sends the given RV command to the user with whom this session exists,
     * giving the outgoing RV ICBM the given message ID.
     * <br>
     * <br>
     * Note that calling this method will have <i>no effect</i> if the
     * underlying <code>RvProcessor</code> is not currently attached to a SNAC
     * connection. See {@link RvProcessor#attachToSnacProcessor} for details.
     *
     * @param command the RV command to send
     * @param icbmMessageId an ICBM message ID for the outgoing RV ICBM
     *
     * @see net.kano.joscar.snaccmd.icbm.SendRvIcbm
     * @see #sendRv(RvCommand)
     */
    void sendRv(RvCommand command, long icbmMessageId);

    /**
     * Sends the given RV response code to the user with whom this session
     * exists. Note that calling this method will have <i>no effect</i> if the
     * underlying <code>RvProcessor</code> is not currently attached to a SNAC
     * connection. See {@link RvProcessor#attachToSnacProcessor} for details.
     *
     * @param code the RV response code to send
     *
     * @see net.kano.joscar.snaccmd.icbm.RvResponse
     */
    void sendResponse(int code);
}