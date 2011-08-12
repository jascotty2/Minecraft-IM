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

package net.kano.joscar.rvproto.rvproxy;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.LiveWritable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A data structure used to send and receive commands to and from an AOL Proxy
 * Server.
 *
 * @see RvProxyCmd
 * @see RvProxyCmdFactory
 */
public final class RvProxyPacket implements LiveWritable {
    /** A packet version code used by WinAIM by default .*/
    public static final int PACKETVERSION_DEFAULT = 0x044a;

    /** A command type for error commands. */
    public static final int CMDTYPE_ERROR = 0x0001;
    /**
     * A command type for the first command sent to the server when creating
     * a connection over an AOL Proxy Server.
     */
    public static final int CMDTYPE_INIT_SEND = 0x0002;
    /**
     * A command type for the first command sent to the server when "receiving"
     * a connection over an AOL Proxy Server.
     */
    public static final int CMDTYPE_INIT_RECV = 0x0004;
    /** A command type for an acknowledgement packet. */
    public static final int CMDTYPE_ACK = 0x0003;
    /**
     * A command type for a command indicating that the proxy initialization
     * process has finished.
     */
    public static final int CMDTYPE_READY = 0x0005;

    /** The set of flags normally sent by the AOL Proxy Server. */
    public static final int FLAGS_DEFAULT_FROM_SERVER = 0x0220;
    /**
     * The set of flags normally sent by a client connected to an AOL Proxy
     * Server.
     */
    public static final int FLAGS_DEFAULT_FROM_CLIENT = 0x0000;

    /**
     * Reads an AOL Proxy Server packet from the given stream. Note that this
     * method will block until either a complete packet is read, until an
     * invalid packet is read, or until the end of the stream is reached (or
     * until an <code>IOException</code> is thrown). Note that if a complete,
     * valid packet cannot be read, <code>null</code> is returned.
     *
     * <br>If this method returns <code>null</code>, no guarantees can be made
     * about the contents and state of the given stream; part of a packet may or
     * may not have been read. In general, the right thing to do after this
     * method returns <code>null</code> is to close the underlying stream.
     *
     * @param in the stream from which to read an RV proxy packet
     * @return a <code>RvProxyPacket</code> read from the given stream, or
     *         <code>null</code> if no valid packet could be read
     * @throws IOException
     */
    public static RvProxyPacket readPacket(InputStream in) throws IOException {
        DefensiveTools.checkNull(in, "in");

        byte[] lenBytes = new byte[2];
        for (int i = 0; i < lenBytes.length;) {
            int count = in.read(lenBytes, i, lenBytes.length - i);

            if (count == -1) return null;

            i += count;
        }

        ByteBlock lenBlock = ByteBlock.wrap(lenBytes);

        int restLength = BinaryTools.getUShort(lenBlock, 0);

        int packetSize = restLength + 2;

        if (restLength < 10) return null;

        byte[] restBytes = new byte[restLength];

        for (int i = 0; i < restBytes.length;) {
            int count = in.read(restBytes, i, restBytes.length - i);

            if (count == -1) return null;

            i += count;
        }

        ByteBlock rest = ByteBlock.wrap(restBytes);

        int packetVersion = BinaryTools.getUShort(rest, 0);
        int cmdType = BinaryTools.getUShort(rest, 2);
        int flags = BinaryTools.getUShort(rest, 8);

        ByteBlock data = rest.subBlock(10);

        return new RvProxyPacket(packetVersion, cmdType, flags, data,
                packetSize);
    }

    /** The "packet version" value for this packet. */
    private final int packetVersion;
    /** This packet's command type code. */
    private final int commandType;
    /** A set of flags sent in this packet. */
    private final int flags;
    /** The command-specific data sent in this packet. */
    private final ByteBlock data;
    /** The size of this packet, in bytes. */
    private final int totalSize;

    /**
     * An object to be used to write the command-specific packet data to the an
     * outgoing stream.
     */
    private final LiveWritable dataWriter;

    /**
     * Creates a new AOL Proxy Server packet with the given properties.
     *
     * @param packetVersion the "version" code of the packet
     * @param cmdType this packet's command type code
     * @param flags the set of bit flags sent in this packet
     * @param data a block of command-specific data in this packet
     * @param totalSize the total size of this packet, as read from an incoming
     *        block of binary data
     */
    protected RvProxyPacket(int packetVersion, int cmdType, int flags,
            ByteBlock data, int totalSize) {
        DefensiveTools.checkRange(packetVersion, "packetVersion", 0);
        DefensiveTools.checkRange(cmdType, "cmdType", 0);
        DefensiveTools.checkRange(flags, "flags", 0);
        DefensiveTools.checkRange(totalSize, "totalSize", -1);

        this.packetVersion = packetVersion;
        this.commandType = cmdType;
        this.flags = flags;
        this.data = data;
        this.totalSize = totalSize;
        this.dataWriter = null;
    }

    /**
     * Creates a new outgoing AOL Proxy Server packet.
     *
     * @param rvProxyCmd an <code>RvProxyCmd</code> whose properties will be
     *        used for this packet
     */
    public RvProxyPacket(final RvProxyCmd rvProxyCmd) {
        this.packetVersion = rvProxyCmd.getPacketVersion();
        this.commandType = rvProxyCmd.getCommandType();
        this.flags = rvProxyCmd.getFlags();
        this.data = null;
        this.dataWriter = new LiveWritable() {
            public void write(OutputStream out) throws IOException {
                rvProxyCmd.writeCommandData(out);
            }
        };
        this.totalSize = -1;
    }

    /**
     * Returns the "packet version" for this packet. This will normally be
     * {@link #PACKETVERSION_DEFAULT}.
     *
     * @return this packet's "packet version"
     */
    public final int getPacketVersion() { return packetVersion; }

    /**
     * Returns this packet's command type. This will normally be one of
     * {@linkplain #CMDTYPE_INIT_SEND the <code>CMDTYPE_<i>*</i></code>
     * constants} defined in this class.
     *
     * @return this packet's command type code
     */
    public final int getCommandType() { return commandType; }

    /**
     * Returns the bit flags sent in this packet. This will normally be either
     * {@link #FLAGS_DEFAULT_FROM_CLIENT} or {@link #FLAGS_DEFAULT_FROM_SERVER}.
     *
     * @return this packet's set of bit flags
     */
    public final int getFlags() { return flags; }

    /**
     * Returns the "command-specific" data sent in this packet. Note that this
     * value will be <code>null</code> if this packet was not read from an
     * incoming stream but was instead created manually.
     *
     * @return this packet's command-specific data block
     */
    public final ByteBlock getCommandData() { return data; }

    /**
     * Returns the total size of this packet, in bytes, as read from an incoming
     * stream. Note that this value will be <code>-1</code> if this packet was
     * not read from an incoming stream (via {@link #readPacket}).
     *
     * @return the total size of this packet, in bytes, or <code>-1</code> if
     *         this packet was not read from an incoming stream
     */
    public final int getTotalSize() { return totalSize; }

    public void write(OutputStream out) throws IOException {
        ByteArrayOutputStream hout = new ByteArrayOutputStream(50);

        BinaryTools.writeUShort(hout, packetVersion);
        BinaryTools.writeUShort(hout, commandType);
        BinaryTools.writeUInt(hout, 0);
        BinaryTools.writeUShort(hout, flags);

        if (dataWriter != null) dataWriter.write(hout);
        else if (data != null) data.write(hout);

        BinaryTools.writeUShort(out, hout.size());
        hout.writeTo(out);
    }

    public String toString() {
        return "RvProxyPacket: commandType=0x" + Integer.toHexString(commandType)
                + ", flags=0x" + Integer.toHexString(flags);
    }
}