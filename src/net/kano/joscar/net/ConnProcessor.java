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
 *  File created by Keith @ 4:47:43 PM
 *
 */

package net.kano.joscar.net;

import net.kano.joscar.DefensiveTools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * A simple interface for an object that has the ability to be attached and/or
 * detached from a stream or socket.
 */
public abstract class ConnProcessor {
    /**
     * Represents whether this FLAP processor is attached to any input or
     * output streams.
     */
    private boolean attached = false;
    /**
     * The input stream from which FLAP packets are currently being read, or
     * <code>null</code> if there is currently no such stream.
     */
    private InputStream in = null;
    /**
     * The output stream to which FLAP packets are currently being written,
     * or <code>null</code> if there is currently no such stream.
     */
    private OutputStream out = null;

    /**
     * Attaches this connection processor to the given socket's input and output
     * streams. Behavior is undefined if the given socket is not connected (that
     * just means an <code>IOException</code> will probably be thrown).
     * <br>
     * <br>
     * Note that this does not begin any sort of loop or connection; it only
     * sets the values of the input and output streams.
     *
     * @param socket the socket to attach to
     * @throws IOException if an I/O error occurs
     */
    public synchronized final void attachToSocket(Socket socket)
            throws IOException {
        DefensiveTools.checkNull(socket, "socket");

        attachToInput(socket.getInputStream());
        attachToOutput(socket.getOutputStream());
    }

    /**
     * Attaches this connection processor to the given input stream. This stream
     * is from whence packets will be read. Note that <code>in</code> cannot be
     * <code>null</code>; to detach from a stream, use {@link #detach}.
     *
     * @param in the input stream to attach to
     */
    public synchronized final void attachToInput(InputStream in) {
        DefensiveTools.checkNull(in, "in");

        this.in = in;

        attached = true;
    }

    /**
     * Attaches this FLAP processor to the given output stream. This stream is
     * where packets sent via the <code>send</code> method will be written. Note
     * that <code>out</code> cannot be <code>null</code>; to detach from a
     * stream, use {@link #detach}.
     *
     * @param out the output stream to attach to
     */
    public synchronized final void attachToOutput(OutputStream out) {
        DefensiveTools.checkNull(out, "out");

        this.out = out;

        attached = true;
    }

    /**
     * Returns whether this connection processor is currently attached to a
     * stream (input, output, or both).
     *
     * @return whether this connection processor is currently "attached"
     */
    protected synchronized final boolean isAttached() { return attached; }

    /**
     * Returns the input stream to which this processor is currently attached,
     * or <code>null</code> if none is currently set.
     *
     * @return this processor's input stream
     */
    protected synchronized final InputStream getInputStream() { return in; }

    /**
     * Returns the output stream to which this processor is currently attached,
     * or <code>null</code> if none is currently set.
     *
     * @return this processor's output stream
     */
    protected synchronized final OutputStream getOutputStream() { return out; }

    /**
     * Detaches this connection processor from any attached input and/or output
     * stream.
     */
    public synchronized final void detach() {
        in = null;
        out = null;
        attached = false;
    }
}