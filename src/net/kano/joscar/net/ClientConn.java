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

import net.kano.joscar.CopyOnWriteArrayList;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.MiscTools;
import net.kano.joscar.flap.ClientFlapConn;

import javax.net.SocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Iterator;

/**
 *
 * A somewhat simple, asynchronous interface to a TCP-based outgoing or "client"
 * connection. Provides a concept of connection state as well as a {@linkplain
 * ClientConnStreamHandler stream handler} that processes the socket's data
 * streams, once connected, in its own thread.
 * <br>
 * <br>
 * One shortcoming of this class is that it does not by default support a
 * proxy of any kind. That is, there are no connection states relating to
 * connecting to a proxy. However, one <i>can</i> {@linkplain #setSocketFactory
 * use} a custom <code>javax.net.SocketFactory</code> that connects to the proxy
 * and then returns that <code>Socket</code>, or even something more complicated
 * (like returning a custom subclass of <code>Socket</code> in the socket
 * factory for some sort of proxy with extrastream metadata).
 * <br>
 * <br>
 * As stated above, this class provides a notion of the current state of a
 * connection. The following is a list of all possible state transitions and
 * each transition's meaning. Note that when the state changes, all {@linkplain
 * #addConnListener listeners} are notified of the old state, the new state, and
 * a {@linkplain ClientConnEvent#getReason "reason" object} whose type varies
 * (though it is most commonly a <code>java.lang.Exception</code> or
 * a <code>java.lang.Error</code>). The type and meaning of this reason object
 * for each state transition is given below.
 * <dl>
 *
 * <dt><code>STATE_NOT_CONNECTED</code> -&gt; <code>STATE_INITING</code> -&gt;
 * <code>STATE_FAILED</code></dt>
 * <dd>
 * Creating the connection thread failed
 * <br>
 * Reason object: the <code>java.lang.Throwable</code> that caused the failure
 * </dd>
 *
 * <dt><code>STATE_NOT_CONNECTED</code> -&gt; <code>STATE_INITING</code> -&gt;
 * <code>STATE_RESOLVING</code> -&gt; <code>STATE_FAILED</code></dt>
 * <dd>
 * Looking up the specified hostname failed (maybe the host does not
 * exist)
 * <br>
 * Reason object: a <code>java.net.UnknownHostException</code></code>
 * </dd>
 *
 * <dt><code>STATE_NOT_CONNECTED</code> -&gt; <code>STATE_INITING</code> -&gt;
 * <i>[<code>STATE_RESOLVING</code> (optional)]</i> -&gt;
 * <code>STATE_CONNECTING</code> -&gt; <code>STATE_FAILED</code></dt>
 * <dd>
 * Making a TCP connection to the given server on the given port failed
 * <br>
 * Reason object: a <code>java.net.IOException</code>
 * </dd>
 *
 * <dt><code>STATE_NOT_CONNECTED</code> -&gt; <code>STATE_INITING</code> -&gt;
 * <i>[<code>STATE_RESOLVING</code> (optional)]</i> -&gt;
 * <code>STATE_CONNECTING</code> -&gt; <code>STATE_CONNECTED</code> -&gt;
 * <code>STATE_NOT_CONNECTED</code></dt>
 * <dd>
 * This is the normal, healthy progression of the connection state
 * <br>
 * Reason object:
 * <dl>
 * <dt>If {@link #disconnect} was called</dt>
 * <dd>The reason object will be {@link #REASON_ON_PURPOSE}</dd>
 * <dt>If a connection/socket error occurred that closed the connection</dt>
 * <dd>The reason object will be the <code>java.lang.IOException</code> that
 * caused the connection to close</dd>
 * <dt>If the connection was closed in another way (that is, if the
 * {@linkplain #setStreamHandler stream handler} returned normally)</dt>
 * <dd>The reason object will be {@link #REASON_CONN_CLOSED}</dd>
 * </dl>
 * </dd>
 * </dl>
 * Just to make things more confusing, if you call <code>disconnect</code>
 * during a connection, the state will revert to
 * <code>STATE_NOT_CONNECTED</code> no matter what state it's currently in.
 * <br>
 * <br>
 * Note that this class has various means of setting both a hostname and an
 * IP address to use for connecting. More importantly, note that
 * <code>connect</code> will <i>fail</i> if both of these are set <i>and</i> if
 * neither of these is set. So what does this mean? Yes, it means only one of
 * these values can be non-<code>null</code> when <code>connect</code> is
 * called.
 * <br>
 * <br>
 * You may wonder why I bothered to allow one to set both a hostname and an
 * <code>InetAddress</code>. I did this because often one wants to resolve a
 * hostname in the same thread as one is connecting on, as resolving and
 * connecting normally happen in succession when making a connection.
 * <br>
 * <br>
 * Note that as far as this author is aware, every method in this class is
 * completely thread-safe. One should also note that {@linkplain
 * #addConnListener connection listeners}' listener methods are called with a
 * lock on the <code>ClientConn</code> and that the {@linkplain
 * #setStreamHandler stream handler}'s <code>handleStream</code> method is not.
 * <br>
 * Also note that <code>ClientConn</code>s are created with no stream handler,
 * no connection listeners, and a default socket factory (<code>null</code>).
 * A typical usage of <code>ClientConn</code>, then, might be as follows:
 * <pre>
ClientConn conn = new ClientConn("joust.kano.net", 80);
conn.addConnListener(myConnectionListener);
conn.setStreamHandler(myStreamHandler);
<i>conn.setSocketFactory(new SomeSortOfProxySocketFactoryYouMade());</i>

conn.connect();
System.out.println("Connecting...");
 * </pre>
 * Note that the italicized portion of the above code sets a custom socket
 * factory. This step is by no means required, but is useful for creating
 * connections through a proxy. See {@link #setSocketFactory setSocketFactory}
 * for details. Also note that the code prints a <code>"Connecting..."</code>
 * message <i>after</i> calling <code>connect</code>. If you don't understand
 * that this class is <i>completely asynchronous</i> by now, I don't think you
 * ever will.
 */
public class ClientConn {
    /**
     * A state indicating that this FLAP client is not connected to a server.
     */
    public static final State STATE_NOT_CONNECTED = new State("NOT_CONNECTED");
    /**
     * A state indicating that this FLAP client is preparing to connect. This
     * state normally does not last for more than a few milliseconds.
     */
    public static final State STATE_INITING = new State("INITING");
    /**
     * A state indicating that the given hostname is being resolved to an IP
     * address before connecting.
     */
    public static final State STATE_RESOLVING = new State("RESOLVING");
    /**
     * A state indicating that a TCP connection attempt is being made to the
     * given server on the given port.
     */
    public static final State STATE_CONNECTING = new State("CONNECTING");
    /**
     * A state indicating that a TCP connection has succeeded and is currently
     * open.
     */
    public static final State STATE_CONNECTED = new State("CONNECTED");
    /**
     * A state indicating that some stage of the connection failed. See
     * {@link ClientFlapConn} documentation for details on state transitions
     * and meanings.
     */
    public static final State STATE_FAILED = new State("FAILED");
    /**
     * A reason indicating that the reason for a state change to
     * <code>NOT_CONNECTED</code> was that <code>disconnect</code> was called.
     */
    public static final State REASON_ON_PURPOSE = new State("ON_PURPOSE");
    /**
     * A reason indicating that the reason for a state change to
     * <code>NOT_CONNECTED</code> was that the socket was closed for some
     * reason. This normally means some sort of network failure.
     */
    public static final State REASON_CONN_CLOSED = new State("CONN_CLOSED");

    /**
     * The current state of the connection.
     */
    private State state = STATE_NOT_CONNECTED;

    /**
     * The hostname we are supposed to connect to.
     */
    private String host = null;
    /**
     * The IP address we are supposed to connect to.
     */
    private InetAddress ip = null;
    /**
     * The port we are supposed to connect to.
     */
    private int port = -1;

    /**
     * A list of connection listeners (state change listeners).
     */
    private CopyOnWriteArrayList<ClientConnListener> connListeners = new CopyOnWriteArrayList<ClientConnListener>();

    /** A socket factory for generating outgoing sockets. */
    private SocketFactory socketFactory = null;
    /**
     * The TCP socket on which this FLAP connection is being held, if any.
     */
    private Socket socket = null;
    /**
     * The current connection thread.
     */
    private ConnectionThread connThread = null;

    /** An object to handle the socket after we've created it. */
    private ClientConnStreamHandler streamHandler = null;

    /**
     * Creates a <code>ClientConn</code> with no hostname/IP or port. (You can
     * use {@link #setHost}, {@link #setIpAddress}, and {@link #setPort} to set
     * them later.)
     */
    public ClientConn() { }

    /**
     * Creates a <code>ClientConn</code> for the given hostname and port number.
     * The given hostname and port will be be used to connect to when
     * <code>connect</code> is called.
     *
     * @param host the hostname to connect to when <code>connect</code> is
     *        called
     * @param port the port to connect to when <code>connect</code> is called
     */
    public ClientConn(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Creates a <code>ClientConn</code> for the given IP address and port
     * number. The given IP address and port will be be used to connect to when
     * <code>connect</code> is called.
     *
     * @param ip the hostname to connect to when <code>connect</code> is
     *        called
     * @param port the port to connect to when <code>connect</code> is called
     */
    public ClientConn(InetAddress ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    /**
     * Adds a connection listener to this connection.
     *
     * @param l the listener to add
     */
    public final void addConnListener(ClientConnListener l) {
        DefensiveTools.checkNull(l, "l");

        connListeners.addIfAbsent(l);
    }

    /**
     * Removes a connection listener from this connection.
     *
     * @param l the listener to remove
     */
    public final void removeConnListener(ClientConnListener l) {
        DefensiveTools.checkNull(l, "l");

        connListeners.remove(l);
    }

    /**
     * Returns the socket on which this connection resides, or <code>null</code>
     * if this connection has no underlying socket yet.
     *
     * @return this connection's socket
     */
    public synchronized final Socket getSocket() { return socket; }

    /**
     * Returns the hostname associated with this connection.
     *
     * @return the hostname associated with this connection
     */
    public synchronized final String getHost() {
        return host;
    }

    /**
     * Sets the hostname associated with this connection.
     *
     * @param host the hostname to associate with this connection
     */
    public synchronized final void setHost(String host) {
        this.host = host;
    }

    /**
     * Returns the IP address associated with this connection.
     *
     * @return the IP address associated with this connection
     */
    public synchronized final InetAddress getIpAddress() {
        return ip;
    }

    /**
     * Sets the IP address associated with this connection.
     *
     * @param ip the IP address associated with this connection
     */
    public synchronized final void setIpAddress(InetAddress ip) {
        this.ip = ip;
    }

    /**
     * Returns the TCP port associated with this connection.
     *
     * @return the TCP port associated with this connection
     */
    public synchronized final int getPort() {
        return port;
    }

    /**
     * Sets the TCP port associated with this connection.
     *
     * @param port the TCP port associated with this connection
     */
    public synchronized final void setPort(int port) {
        this.port = port;
    }

    /**
     * Returns the current connection state. This will be one of {@link
     * #STATE_NOT_CONNECTED}, {@link #STATE_INITING}, {@link #STATE_RESOLVING},
     * {@link #STATE_CONNECTING}, {@link #STATE_CONNECTED}, or {@link
     * #STATE_FAILED}; see each value's individual documentation for details.
     *
     * @return the current state of this connection
     */
    public synchronized final State getState() {
        return state;
    }

    /**
     * Sets the state of the connection to the given value, providing the given
     * reason to state listeners. Must be one of {@link
     * #STATE_NOT_CONNECTED}, {@link #STATE_INITING}, {@link #STATE_RESOLVING},
     * {@link #STATE_CONNECTING}, {@link #STATE_CONNECTED}, or {@link
     * #STATE_FAILED}.
     *
     * @param state the new connection state
     * @param reason a "reason" or description of this state change to provide
     *        to state listeners
     */
    private synchronized void setState(State state, Object reason) {
        if (this.state == state || (this.state == STATE_FAILED
                && state == STATE_NOT_CONNECTED)) return;

        State oldState = this.state;
        this.state = state;

        ClientConnEvent event = new ClientConnEvent(this, oldState, this.state,
                reason);

        for (Iterator<ClientConnListener> it = connListeners.iterator(); it.hasNext();) {
            ClientConnListener listener = it.next();

            listener.stateChanged(event);
        }
    }

    /**
     * Sets the socket associated with this connection.
     *
     * @param socket the socket to associate with this connection
     */
    private synchronized void setSocket(Socket socket) {
        this.socket = socket;
    }

    /**
     * Attempts to connect using the values of {@linkplain #setHost host} or
     * {@linkplain #setIpAddress IP address} and {@linkplain #setPort TCP port}
     * which were, presumably, set before this method was called. Upon
     * successful connection, a <code>Socket</code> will be passed to this
     * <code>ClientConn</code>'s {@link #setStreamHandler stream handler}.
     * <br>
     * <br>
     * Note that this method can only be called when the connection is in either
     * of the states {@link #STATE_NOT_CONNECTED} and {@link #STATE_FAILED};
     * otherwise one must call {@link #disconnect} before calling this method.
     *
     * @throws IllegalStateException if a connection attempt is already being
     *         made; if both IP and hostname are both set; if neither IP or
     *         hostname is set; if port is not set
     */
    public synchronized final void connect() throws IllegalStateException {
        if (state != STATE_NOT_CONNECTED && state != STATE_FAILED) {
            throw new IllegalStateException("I am already " +
                    "connected/connecting");
        }
        if (host == null && ip == null) {
            throw new IllegalStateException("either host or ip must be " +
                    "non-null");
        }
        if (host != null && ip != null) {
            throw new IllegalStateException("host and ip may not both be " +
                    "non-null");
        }
        if (port == -1) {
            throw new IllegalStateException("port must not be -1");
        }

        setState(STATE_INITING, null);

        Object dest = (host == null ? (Object) ip : (Object) host);

        connThread = new ConnectionThread(MiscTools.getClassName(this)
                + " to " + dest + ":" + port);

        try {
            connThread.start();
        } catch (Throwable t) {
            setState(STATE_FAILED, t);
        }
    }

    /**
     * Closes this connection and sets the state to
     * <code>STATE_NOT_CONNECTED</code>, with the given exception or error as
     * the state change's {@linkplain ClientConnEvent#getReason reason object}.
     * Note that calling this method will have no effect if the connection state
     * is already <CODE>STATE_NOT_CONNECTED</code> or <code>STATE_FAILED</code>.
     *
     * @param t an exception or error that caused the connection to close
     */
    protected synchronized final void processError(Throwable t) {
        DefensiveTools.checkNull(t, "t");

        if (state == STATE_NOT_CONNECTED || state == STATE_FAILED) return;

        try {
            closeConn();
        } finally {
            setState(STATE_NOT_CONNECTED, t);
        }
    }

    /**
     * If not already disconnected, this disconnects the TCP socket associated
     * with this connection and sets the connection state to
     * <code>STATE_NOT_CONNECTED</code>. Note that if the connection state is
     * already <CODE>STATE_NOT_CONNECTED</code> or <code>STATE_FAILED</code>
     * no state change will take place.
     */
    public synchronized final void disconnect() {
        if (state == STATE_NOT_CONNECTED || state == STATE_FAILED) return;

        try {
            closeConn();
        } finally {
            setState(STATE_NOT_CONNECTED, REASON_ON_PURPOSE);
        }
    }

    /**
     * Closes the currently attached connection and cancels any running
     * connection thread.
     */
    private synchronized void closeConn() {
        // I'm not sure which order is the best for these next few statements
        if (connThread != null) {
            connThread.cancel();
            connThread = null;
        }

        if (socket != null && !socket.isClosed()) {
            try { socket.close(); } catch (IOException ignored) { }
        }
    }

    /**
     * Sets the socket factory this FLAP connection should use to create an
     * outgoing socket. If <code>socketFactory</code> is <code>null</code>, as
     * is the default value, <code>new Socket(..)</code> is used in place of a
     * using a socket factory.
     *
     * @param socketFactory a socket factory to use in creating the outgoing
     *        OSCAR connection, or <code>null</code> to not use a factory
     */
    public synchronized final void setSocketFactory(
            SocketFactory socketFactory) {
        this.socketFactory = socketFactory;
    }

    /**
     * Sets this FLAP connection's socket factory. This factory will be used
     * to create the outgoing socket to the OSCAR server. Note that if this is
     * <code>null</code> (the default value) then <code>new Socket(..)</code> is
     * used in place of using a socket factory to create a socket.
     *
     * @return the socket factory associated with this FLAP connection
     */
    public synchronized final SocketFactory getSocketFactory() {
        return socketFactory;
    }

    /**
     * Sets the "stream handler" for this connection to the given handler. The
     * stream handler is passed a <code>Socket</code> created by this
     * <code>ClientConn</code> as soon as it has been successfully created.
     * <br>
     * <br>
     * Note that <code>streamHandler</code> can be <code>null</code> if you
     * really want it to be; a value of <code>null</code> simply means the
     * connection will be made and immediately dropped.
     * <br>
     * <br>
     * Also note that the stream handler's value will be read <i>after the
     * socket has been created</i>, so it would be feasible (though not
     * recommended) to postpone setting a stream handler until the connection's
     * state has been changed to {@link #STATE_CONNECTED}.
     * <br>
     * <br>
     * Also note that, as one should expect, new <code>ClientConn</code>s are
     * initialized with no stream handler (a value of <code>null</code>).
     *
     * @param streamHandler a "stream handler" for this connection
     *
     * @see ClientConnStreamHandler
     */
    public synchronized final void setStreamHandler(ClientConnStreamHandler
            streamHandler) {
        this.streamHandler = streamHandler;
    }

    /**
     * Returns this connection's "stream handler." See {@linkplain
     * #setStreamHandler above} for details on what this value means. Note that
     * the returned value may be <code>null</code>.
     *
     * @return this connection's stream handler
     */
    public synchronized final ClientConnStreamHandler getStreamHandler() {
        return streamHandler;
    }

    /**
     * Creates a new outgoing socket to the given host on the given port using
     * this FLAP connection's socket factory. If no socket factory is set,
     * a new <code>java.net.Socket</code> is created.
     *
     * @param host the host to which to connect
     * @param port the port on which to connect
     * @return an outgoing socket to the given host and port
     *
     * @throws IOException if an I/O error occurs
     */
    private Socket createSocket(InetAddress host, int port)
            throws IOException {
        // this method can't be synchronized because we shouldn't freeze
        // the state of the connection while waiting for a socket to open; all
        // we need is to know what the current socket factory is, so we lock
        // for that (getSocketFactory is synchronized), then create the socket
        // afterwards.
        SocketFactory factory = getSocketFactory();

        if (factory != null) {
            return factory.createSocket(host, port);
        } else {
            return new Socket(host, port);
        }
    }

    /**
     * A thread to resolve a hostname (if necessary), initiate a TCP connection,
     * and pass the connection over to the data handler.
     */
    private class ConnectionThread extends Thread {
        /** Whether this connection attempt has been cancelled. */
        private boolean cancelled = false;

        /**
         * Cretes a new connection thread with the given thread name.
         *
         * @param name a name for this thread, to pass to {@link
         *        Thread(String)}
         */
        public ConnectionThread(String name) {
            super(name);
        }

        /**
         * Cancels this connection attempt as immediately as possible. Note that
         * <i>no changes will be made to the parent <code>ClientConn</code>
         * after this method is called</i>. Also, as much effort will be made
         * as is practically possible to quickly stop any other processing from
         * being done after a call to this method.
         */
        public void cancel() {
            cancelled = true;
        }

        /**
         * Starts the connection / data reading thread.
         */
		@Override
        public void run() {
            ClientConn conn = ClientConn.this;

            InetAddress ip;
            String host;
            synchronized(conn) {
                ip = conn.getIpAddress();
                host = conn.getHost();
            }

            // resolve the hostname, if any
            if (ip == null) {
                synchronized(conn) {
                    if (cancelled) return;
                    setState(STATE_RESOLVING, null);
                }

                try {
                    ip = InetAddress.getByName(host);

                } catch (UnknownHostException e) {
                    // we couldn't resolve the hostname

                    synchronized(conn) {
                        if (cancelled) return;

                        setState(STATE_FAILED, e);
                    }

                    return;
                }
            }

            // start connecting
            int port;
            synchronized(conn) {
                if (cancelled) return;
                setState(STATE_CONNECTING, null);
                port = conn.getPort();
            }

            Socket socket;
            try {
                socket = createSocket(ip, port);
            } catch (IOException e) {
                synchronized(conn) {
                    if (cancelled) return;
                    setState(STATE_FAILED, e);
                }

                return;
            }

            // store the socket in the ClientConn
            synchronized(conn) {
                if (cancelled) return;

                setSocket(socket);
                setState(STATE_CONNECTED, null);
            }

            // pass the socket off to the stream handler
            try {
                synchronized(conn) {
                    if (cancelled) return;
                }

                getStreamHandler().handleStream(ClientConn.this, socket);

            } catch (IOException e) {
                synchronized(conn) {
                    if (cancelled) return;

                    processError(e);
                }
            } finally {
                synchronized(conn) {
                    if (!cancelled) {
                        try {
                            if (!socket.isClosed()) {
                                try {
                                    socket.close();
                                } catch (IOException ignored) { }
                            }
                        } finally {
                            setState(STATE_NOT_CONNECTED, REASON_CONN_CLOSED);
                        }
                    }

                    // we'd like to have done this while locking on the
                    // ClientConn, but that won't happen if this thread has
                    // been cancelled, so we do it here just in case.
                    if (!socket.isClosed()) {
                        try { socket.close(); } catch (IOException ignored) { }
                    }
                }
            }
        }
    }

    /**
     * Represents a single connection state.
     */
    public static final class State {
        /** The name of this state, for debugging purposes. */
        private final String name;

        /**
         * Creates a new connection state object with the given name.
         *
         * @param name the name of this state, for debugging purposes
         */
        private State(String name) { this.name = name; }

		@Override
        public String toString() { return name; }
    }

}