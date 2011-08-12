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
 *  File created by keith @ Mar 28, 2003
 *
 */

package net.kano.joscar.snac;

/**
 * Provides an interface for processing and intercepting SNAC packets received
 * on a SNAC processor, optionally halting further processing of any
 * given packet.
 */
public interface VetoableSnacPacketListener {
    /**
     * A value indicating that the SNAC processor should stop passing the packet
     * through other vetoable and non-vetoable listeners. This value indicates
     * that further internal processing may be performed on the packet, though
     * as of this writing no such processing is done either way. It is suggested
     * to use this instead of <code>STOP_PROCESSING_ALL</code>, however, to
     * allow for future expansion of the SNAC processing code internal to
     * joscar.
     */
    Object STOP_PROCESSING_LISTENERS = new Object();

    /**
     * A value indicating the SNAC processor should stop all further
     * processing of a packet immediately. As of this writing, this is
     * functionally equivalent to <code>STOP_PROCESSING_LISTENERS</code>, as
     * no further processing is done anyway.
     */
    Object STOP_PROCESSING_ALL = new Object();

    /**
     * A value indicating that the SNAC processor should continue processing
     * the given packet normally.
     */
    Object CONTINUE_PROCESSING = new Object();

    /**
     * Called when a new packet arrives on a SNAC connection. See individual
     * documentation for {@link #CONTINUE_PROCESSING}, {@link
     * #STOP_PROCESSING_LISTENERS}, and {@link #STOP_PROCESSING_ALL} for details
     * on when to return which value.
     *
     * @param event an object describing the event
     * @return one of {@link #CONTINUE_PROCESSING}, {@link
     *         #STOP_PROCESSING_LISTENERS}, and {@link #STOP_PROCESSING_ALL}
     */
    Object handlePacket(SnacPacketEvent event);
}
