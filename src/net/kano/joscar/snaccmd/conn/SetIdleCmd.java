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
 *  File created by keith @ Feb 22, 2003
 *
 */

package net.kano.joscar.snaccmd.conn;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.flapcmd.SnacPacket;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A SNAC command used to set how long the user has been idle. This command
 * should only be sent once when the user becomes idle; the server will handle
 * increasing the amount of idle time as time progresses. When the user becomes
 * unidle, the command should be sent with an idle time value of zero
 *  (<code>SetIdleCmd.IDLETIME_NOT_IDLE</code>).
 *
 * @snac.src client
 * @snac.cmd 0x01 0x11
 */
public class SetIdleCmd extends ConnCommand {
    /** An idle time value ({@value}) indicating that the user is not idle. */
    public static final long IDLETIME_NOT_IDLE = 0;

    /** The number of seconds the user has been idle. */
    private final long seconds;

    /**
     * Generates a new set idle time command from the given incoming SNAC
     * packet.
     *
     * @param packet the incoming set idle time packet
     */
    protected SetIdleCmd(SnacPacket packet) {
        super(CMD_SET_IDLE);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();
        seconds = BinaryTools.getUInt(snacData, 0);
    }

    /**
     * Creates a new outgoing set idle time command with the given number of
     * seconds idle. If <code>seconds</code> is <code>0</code>, the idle time
     * is reset to zero and the user is marked "active" until another set idle
     * time command is sent with a nonzero value.
     *
     * @param seconds the number of seconds the user has been idle
     */
    public SetIdleCmd(long seconds) {
        super(CMD_SET_IDLE);

        DefensiveTools.checkRange(seconds, "seconds", 0);

        this.seconds = seconds;
    }

    /**
     * Returns the number of seconds the user has been idle, as sent in this
     * command.
     *
     * @return the user's idle time, in seconds
     */
    public final long getSecondsIdle() {
        return seconds;
    }

    public void writeData(OutputStream out) throws IOException {
        BinaryTools.writeUInt(out, seconds);
    }

    public String toString() {
        return "SetIdleCmd: seconds=" + seconds;
    }
}
