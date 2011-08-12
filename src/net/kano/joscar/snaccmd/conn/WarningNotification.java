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
import net.kano.joscar.snaccmd.MiniUserInfo;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A SNAC command indicating that the client has been "warned" by another user.
 *
 * @snac.src server
 * @snac.cmd 0x01 0x10
 */
public class WarningNotification extends ConnCommand {
    /** The new warning level. */
    private final int level;
    /** The user who warned us. */
    private final MiniUserInfo by;

    /**
     * Creates a new warning notification command from the given incoming SNAC
     * packet.
     *
     * @param packet the incoming warning notification packet
     */
    protected WarningNotification(SnacPacket packet) {
        super(CMD_WARNED);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();
        level = BinaryTools.getUShort(snacData, 0);

        by = MiniUserInfo.readUserInfo(snacData.subBlock(2));
    }

    /**
     * Creates a new outgoing anonymous warning notification command with the
     * given new client warning level. Using this constructor is equvalent to
     * using {@link #WarningNotification(int, MiniUserInfo) new
     * WarningNotification(level, null)}.
     *
     * @param level the client's new warning level
     */
    public WarningNotification(int level) {
        this(level, null);
    }

    /**
     * Creates a new outgoing warning notification command with the given
     * properties.
     *
     * @param level the client's new warning level
     * @param by the user who warned the client, or <code>null</code> if the
     *        user was warned "anonymously"
     */
    public WarningNotification(int level, MiniUserInfo by) {
        super(CMD_WARNED);

        DefensiveTools.checkRange(level, "level", 0);

        this.level = level;
        this.by = by;
    }

    /**
     * The client's new warning level.
     *
     * @return the new warning level
     */
    public final int getNewLevel() {
        return level;
    }

    /**
     * A miniature user information block for the user who warned the client,
     * or <code>null</code> if the client was warned anonymously.
     *
     * @return an object representing the user who warned the client
     */
    public final MiniUserInfo getWarner() {
        return by;
    }

    public void writeData(OutputStream out) throws IOException {
        BinaryTools.writeUShort(out, level);
        if (by != null) by.write(out);
    }

    public String toString() {
        return "WarningNotification: warned to " + level + "% by <" + by + ">";
    }
}
