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

import net.kano.joscar.DefensiveTools;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Represents a single AOL Proxy protocol command to be sent over an AOL Proxy
 * Server connection.
 *
 * @see RvProxyPacket
 */
public abstract class RvProxyCmd {
    /** This command's "packet version. */
    private final int packetVersion;
    /** This command's RV proxy command type. */
    private final int commandType;
    /** A set of flags for this command. */
    private final int flags;

    /**
     * Creates a new RV proxy command with properties read from the given
     * incoming RV proxy packet.
     *
     * @param packet an incoming RV proxy packet
     */
    protected RvProxyCmd(RvProxyPacket packet) {
        this(packet.getPacketVersion(), packet.getCommandType(),
                packet.getFlags());
    }

    /**
     * Creates a new outgoing server-bound RV proxy command with the given
     * command type, a packet version of {@link
     * RvProxyPacket#PACKETVERSION_DEFAULT}, and a flag set containing {@link
     * RvProxyPacket#FLAGS_DEFAULT_FROM_CLIENT}.
     *
     * @param cmdType a RV proxy command type for this command, like {@link
     *        RvProxyPacket#CMDTYPE_INIT_SEND}
     */
    protected RvProxyCmd(int cmdType) {
        this(cmdType, RvProxyPacket.FLAGS_DEFAULT_FROM_CLIENT);
    }

    /**
     * Creates a new outgoing RV proxy command with the given command type and
     * bit flags. The packet version value will be set to {@link
     * RvProxyPacket#PACKETVERSION_DEFAULT}}.
     *
     * @param cmdType a RV proxy command type for this command, like {@link
     *        RvProxyPacket#CMDTYPE_INIT_SEND}
     * @param flags a set of bit flags, like {@link
     *        RvProxyPacket#FLAGS_DEFAULT_FROM_CLIENT}
     */
    protected RvProxyCmd(int cmdType, int flags) {
        this(RvProxyPacket.PACKETVERSION_DEFAULT, cmdType, flags);
    }

    /**
     * Creates a new outgoing RV proxy command with the given command type, bit
     * flags, and packet version.
     *
     * @param packetVersion a "packet version" value for this command; should
     *        normally be {@link RvProxyPacket#PACKETVERSION_DEFAULT}
     * @param cmdType a RV proxy command type for this command, like {@link
     *        RvProxyPacket#CMDTYPE_INIT_SEND}
     * @param flags a set of bit flags, like {@link
     *        RvProxyPacket#FLAGS_DEFAULT_FROM_CLIENT}
     */
    protected RvProxyCmd(int packetVersion, int cmdType, int flags) {
        DefensiveTools.checkRange(packetVersion, "packetVersion", 0);
        DefensiveTools.checkRange(cmdType, "cmdType", 0);
        DefensiveTools.checkRange(flags, "flags", 0);

        this.packetVersion = packetVersion;
        this.commandType = cmdType;
        this.flags = flags;
    }

    /**
     * Returns this RV proxy command's "packet version" value. This will
     * normally be {@link RvProxyPacket#PACKETVERSION_DEFAULT}.
     *
     * @return this RV proxy command's "packet version" value
     */
    public final int getPacketVersion() { return packetVersion; }

    /**
     * Returns this RV proxy command's command type code. This value will
     * normally be one of {@linkplain RvProxyPacket#CMDTYPE_INIT_SEND
     * <code>RvProxyPacket</code>'s <code>CMDTYPE_<i>*</i></code> constants}.
     *
     * @return this command's RV proxy command type code
     */
    public final int getCommandType() { return commandType; }

    /**
     * Returns this RV proxy command's set of bit flags. This value will
     * normally contain either {@link RvProxyPacket#FLAGS_DEFAULT_FROM_CLIENT}
     * or {@link RvProxyPacket#FLAGS_DEFAULT_FROM_SERVER}.
     *
     * @return this command's set of RV proxy command bit flags
     */
    public final int getFlags() { return flags; }

    /**
     * Writes this command's raw "command-specific data" to the given stream.
     *
     * @param out the stream to which to write
     *
     * @throws IOException if an I/O error occurs
     */
    public abstract void writeCommandData(OutputStream out) throws IOException;
}