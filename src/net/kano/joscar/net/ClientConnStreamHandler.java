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
 *  File created by keith @ Mar 6, 2003
 *
 */

package net.kano.joscar.net;

import java.io.IOException;
import java.net.Socket;

/**
 * Handles a socket successfully created by a <code>ClientConn</code>. See
 * {@link ClientConn} and {@link ClientConn#setStreamHandler
 * ClientConn.setStreamHandler} for details.
 */
public interface ClientConnStreamHandler {
    /**
     * Called when a socket was successfully opened by a
     * <code>ClientConn</code>. Note that this method is only called once per
     * <code>Socket</code>, and when this method returns the given socket is
     * closed and the given <code>ClientConn</code>'s state becomes
     * <code>ClientConn.NOT_CONNECTED</code>. If an <code>IOException</code> is
     * thrown in this method, it is passed to the <code>ClientConn</code>'s
     * connection listeners and the socket is closed as stated above.
     *
     * @param conn the <code>ClientConn</code> that opened (and that which
     *        "owns") the given socket
     * @param socket the socket that was opened
     *
     * @throws IOException if an I/O error occurs
     */
    void handleStream(ClientConn conn, Socket socket)
            throws IOException;
}