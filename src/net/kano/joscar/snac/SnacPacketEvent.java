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
import net.kano.joscar.flap.FlapPacketEvent;
import net.kano.joscar.flapcmd.SnacCommand;
import net.kano.joscar.flapcmd.SnacPacket;

/**
 * An event fired when a SNAC packet is received on a SNAC processor.
 */
public class SnacPacketEvent extends FlapPacketEvent {
    /**
     * The SNAC processor on which this packet was received.
     */
    private final AbstractSnacProcessor snacProcessor;

    /**
     * The packet that was received.
     */
    private final SnacPacket snacPacket;

    /**
     * The SNAC command that was received.
     */
    private final SnacCommand snacCommand;

    /**
     * Creates a <code>SnacPacketEvent</code> that is a duplicate of the given
     * object.
     *
     * @param other the object to copy
     */
    protected SnacPacketEvent(SnacPacketEvent other) {
        this(other, other.getSnacProcessor(), other.getSnacPacket(), other.getSnacCommand());
    }

    /**
     * Creates a new <code>SnacPacketEvent</code> with the given properties.
     *
     * @param other a <code>FlapPacketEvent</code> to copy
     * @param snacProcessor the SNAC processor on which the packet was received
     * @param snacPacket the SNAC packet that was received
     * @param snacCommand the SNAC command that was received, or
     *        <code>null</code> if no SNAC command object is associated with
     *        this event
     */
    protected SnacPacketEvent(FlapPacketEvent other,
            AbstractSnacProcessor snacProcessor,
            SnacPacket snacPacket, SnacCommand snacCommand) {
        super(other);

        DefensiveTools.checkNull(snacProcessor, "snacProcessor");
        DefensiveTools.checkNull(snacPacket, "snacPacket");

        this.snacProcessor = snacProcessor;
        this.snacPacket = snacPacket;
        this.snacCommand = snacCommand;
    }

    /**
     * Returns the SNAC processor on which a packet was received.
     *
     * @return the associated SNAC connection
     */
    public final AbstractSnacProcessor getSnacProcessor() {
        return snacProcessor;
    }

    /**
     * Returns the SNAC packet that was received.
     *
     * @return the SNAC packet that was received
     */
    public final SnacPacket getSnacPacket() { return snacPacket; }

    /**
     * Returns a <code>SnacCommand</code> generated from the SNAC packet that was
     * received.
     *
     * @return the SNAC command that was received
     */
    public final SnacCommand getSnacCommand() { return snacCommand; }
}
