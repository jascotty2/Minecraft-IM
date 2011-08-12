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
import net.kano.joscar.MiscTools;
import net.kano.joscar.OscarTools;
import net.kano.joscar.StringBlock;
import net.kano.joscar.flapcmd.SnacPacket;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A base class for the two typing-notification commands in this package. These
 * commands are {@link SendTypingNotification} and {@link
 * RecvTypingNotification}.
 */
public abstract class TypingCmd extends IcbmCommand {
    /** The default value for the <code>nulls</code> field. */
    public static final int NULLS_DEFAULT = 0;
    /** The default value for the <code>code</code> field. */
    public static final int CODE_DEFAULT = 0x0001;

    /** A typing state indicating that the user is typing. */
    public static final int STATE_TYPING = 0x0002;
    /** A typing state indicating that the user typed text, but then stopped. */
    public static final int STATE_PAUSED = 0x0001;
    /**
     * A typing state indicating that the user has not typed anything, or that
     * he or she erased all of what he or she was typing.
     */
    public static final int STATE_NO_TEXT = 0x0000;

    /** Some sort of value. */
    private final long nulls;
    /** Some sort of code. */
    private final int code;
    /**
     * The screenname of the user who is typing or to whom the typing
     * notification is directed.
     */
    private final String sn;
    /** The "typing state" of the user. */
    private final int typingState;

    /**
     * Generates a new typing-notification command from the given incoming SNAC
     * packet and with the given SNAC command subtype.
     *
     * @param command the SNAC command subtype of this command
     * @param packet an incoming typing-notification command
     */
    protected TypingCmd(int command, SnacPacket packet) {
        super(command);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

        nulls = BinaryTools.getLong(snacData, 0);
        code = BinaryTools.getUShort(snacData, 8);

        ByteBlock snBlock = snacData.subBlock(10);
        StringBlock snInfo = OscarTools.readScreenname(snBlock);
        sn = snInfo.getString();

        ByteBlock rest = snBlock.subBlock(snInfo.getTotalSize());
        typingState = BinaryTools.getUShort(rest, 0);
    }

    /**
     * Creates a new typing notification command with the given SNAC command
     * subtype to/from the given screenname with the given typing state. The
     * <code>nulls</code> and <code>code</code> fields are set to their
     * defaults, {@link #NULLS_DEFAULT} and {@link #CODE_DEFAULT}, respectively.
     * Using this constructor is equivalent to using {@link #TypingCmd(int,
     * long, int, String, int) new TypingCmd(command, TypingCmd.NULLS_DEFAULT,
     * TypingCmd.CODE_DEFAULT, sn, typingState)}.
     *
     * @param command the SNAC command subtype of this command
     * @param sn the screenname to or from which this typing notification goes
     *        or comes, respectively
     * @param typingState the typing state, like {@link #STATE_NO_TEXT}
     */
    protected TypingCmd(int command, String sn, int typingState) {
        this(command, NULLS_DEFAULT, CODE_DEFAULT, sn, typingState);
    }

    /**
     * Creates a new typing notification command with the given properties.
     *
     * @param command the SNAC command subtype of this command
     * @param nulls a value for the first eight bytes of the typing notification
     *        command (normally just {@link #NULLS_DEFAULT}, <code>0</code>)
     * @param code some sort of code, normally {@link #CODE_DEFAULT}
     * @param sn the screenname of the source or destination of this command,
     *        depending on context
     * @param typingState the typing state, like {@link #STATE_PAUSED}
     */
    protected TypingCmd(int command, long nulls, int code, String sn,
            int typingState) {
        super(command);

        DefensiveTools.checkRange(code, "code", 0);
        DefensiveTools.checkNull(sn, "sn");
        DefensiveTools.checkRange(typingState, "typingState", 0);

        this.nulls = nulls;
        this.code = code;
        this.sn = sn;
        this.typingState = typingState;
    }

    /**
     * Returns the value of the first eight bytes of the typing notification
     * command. This value is normally {@link #NULLS_DEFAULT}, which is
     * <code>0</code> and can probably be ignored.
     *
     * @return the value of the first eight bytes of the typing notification
     *         command
     */
    public final long getNullBlockValue() { return nulls; }

    /**
     * Returns the value of some sort of code sent in the typing command. This
     * is normally {@link #CODE_DEFAULT}, and can probably be ignored.
     *
     * @return some sort of code associated with this typing notification
     */
    public final int getCode() { return code; }

    /**
     * Returns the screenname of the user who is typing, if this is an incoming
     * typing notification; otherwise, returns the screenname of the user to
     * whom the user is typing.
     *
     * @return the screenname sent in this typing notification
     */
    public final String getScreenname() { return sn; }

    /**
     * Returns the "typing state" sent in this command. Normally one of
     * {@link #STATE_NO_TEXT}, {@link #STATE_TYPING}, or {@link #STATE_PAUSED}.
     *
     * @return this typing notification command's "typing state"
     */
    public final int getTypingState() { return typingState; }

    public void writeData(OutputStream out) throws IOException {
        BinaryTools.writeLong(out, nulls);
        BinaryTools.writeUShort(out, code);
        OscarTools.writeScreenname(out, sn);
        BinaryTools.writeUShort(out, typingState);
    }

    public String toString() {
        return MiscTools.getClassName(this) + " from " + sn
                + " (nulls=" + nulls + ", code=" + code + "): " + (
                typingState == STATE_TYPING ? "typing" :
                typingState == STATE_PAUSED ? "typed" :
                typingState == STATE_NO_TEXT ? "no text"
                : "unknown: " + typingState);
    }
}
