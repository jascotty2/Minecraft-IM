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

package net.kano.joscar.snaccmd.icbm;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.OscarTools;
import net.kano.joscar.flapcmd.SnacPacket;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A SNAC command used to "warn" another user.
 *
 * @snac.src client
 * @snac.cmd 0x04 0x08
 *
 * @see net.kano.joscar.snaccmd.conn.WarningNotification
 */
public class WarnCmd extends IcbmCommand {
    /** An anonymity code indicating that this warning should be anonymous. */
    public static final int CODE_ANONYMOUS = 0x0001;
    /**
     * An anonymity code indicating that this warning should not be anonymous.
     */
    public static final int CODE_NOT_ANONYMOUS = 0x0000;

    /** An "anonymity code." */
    private final int anonymityCode;
    /** The user to warn. */
    private final String warnee;

    /**
     * Generates a warn-user ocmmand from the given incoming SNAC packet.
     *
     * @param packet an incoming warn-user packet
     */
    protected WarnCmd(SnacPacket packet) {
        super(CMD_WARN);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

        anonymityCode = BinaryTools.getUShort(snacData, 0);

        ByteBlock snBlock = snacData.subBlock(2);
        warnee = OscarTools.readScreenname(snBlock).getString();
    }

    /**
     * Creates a new warning command to warn the given user
     * <i>non-anonymously</i>. Using this constructor is equvalent to using
     * {@link #WarnCmd(String, boolean) new WarnCmd(warnee, false)}.
     *
     * @param warnee the screenname of the user to warn
     */
    public WarnCmd(String warnee) {
        this(warnee, CODE_NOT_ANONYMOUS);
    }

    /**
     * Creates a new warning command to warn the given user, anonymously if
     * <code>anonymous</code> is <code>true</code>.
     *
     * @param warnee the screenname of the user to warn
     * @param anonymous whether or not this warning should be "anonymous"
     */
    public WarnCmd(String warnee, boolean anonymous) {
        this(warnee, anonymous ? CODE_ANONYMOUS : CODE_NOT_ANONYMOUS);
    }

    /**
     * Creates a new warning command to warn the given user with the given
     * "anonymity code." The anonymity code should normally be one of
     * {@link #CODE_ANONYMOUS} and {@link #CODE_NOT_ANONYMOUS}.
     *
     * @param warnee the screenname of the user to warn
     * @param anonymityCode a code indicating whether or not the warning should
     *        be anonymous
     */
    public WarnCmd(String warnee, int anonymityCode) {
        super(CMD_WARN);

        DefensiveTools.checkNull(warnee, "warnee");

        this.anonymityCode = anonymityCode;
        this.warnee = warnee;
    }

    /**
     * Returns whether this warning was intended to be "anonymous."
     *
     * @return whether this warning is anonymous
     */
    public final boolean isAnonymous() {
        return anonymityCode == CODE_ANONYMOUS;
    }

    /**
     * Returns the "anonymity code" of this warning command. Normally one of
     * {@link #CODE_ANONYMOUS} and {@link #CODE_NOT_ANONYMOUS}.
     *
     * @return the anonymity code of this warning command
     */
    public final int getAnonymityCode() { return anonymityCode; }

    /**
     * Returns the screenname of the user to be warned.
     *
     * @return the screenname of the user being warned
     */
    public final String getWarnee() { return warnee; }

    public void writeData(OutputStream out) throws IOException {
        BinaryTools.writeUShort(out, anonymityCode);
        OscarTools.writeScreenname(out, warnee);
    }

    public String toString() {
        return "WarnCmd: warning " + warnee + ", anonymous=" + (
                anonymityCode == CODE_ANONYMOUS ? "yes" :
                anonymityCode == CODE_NOT_ANONYMOUS ? "no"
                : "0x" + anonymityCode);
    }
}
