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

package net.kano.joscar.snaccmd.icbm;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.Writable;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A data structure containing various settings related to instant messaging.
 */
public class ParamInfo implements Writable {
    /**
     * The value of "max channel" that the client must send when setting
     * parameter information.
     */
    public static final int MAXCHAN_DEFAULT = 0x0000;

    /**
     * A bit mask indicating something. I'm not sure what this does, but it is
     * normally on by default (that is, when AOL's AIM servers send a {@link
     * ParamInfoCmd}, this bit is set.
     */
    public static final long FLAG_CHANMSGS_ALLOWED    = 0x00000001;
    /**
     * A bit mask indicating that receiving {@link MissedMessagesCmd}s is
     * supported.
     */
    public static final long FLAG_MISSEDCALLS_ALLOWED = 0x00000002;
    /**
     * A bit mask indicating that receiving {@link RecvTypingNotification}s is
     * supported.
     */
    public static final long FLAG_TYPING_NOTIFICATION = 0x00000010;

    /** The maximum ICBM channel supported. */
    private final int maxChannel;
    /** A set of flags indicating various settings. */
    private final long flags;
    /** The maximum length of a message that can be sent and received. */
    private final int maxMsgLen;
    /** The maximum warning level that one can have to send a message. */
    private final int maxSenderWarning;
    /** The maximum warning level that one can have to receive a message. */
    private final int maxReceiverWarning;
    /** The minimum distance between IM's to or from the client. */
    private final long minMsgInterval;

    /**
     * Returns an ICBM parameter information block read from the given block of
     * binary data.
     *
     * @param block the block of data containing ICBM parameter information
     * @return an ICBM parameter information block read from the given data
     *         block
     */
    protected static ParamInfo readParamInfo(ByteBlock block) {
        DefensiveTools.checkNull(block, "block");

        if (block.getLength() < 16) return null;

        int maxchan = BinaryTools.getUShort(block, 0);
        long flags = BinaryTools.getUInt(block, 2);
        int maxmsglen = BinaryTools.getUShort(block, 6);
        int maxsendwarn = BinaryTools.getUShort(block, 8);
        int maxrecvwarn = BinaryTools.getUShort(block, 10);
        long minint = BinaryTools.getUInt(block, 12);

        return new ParamInfo(maxchan, flags, maxmsglen, maxsendwarn,
                maxrecvwarn, minint);
    }

    /**
     * Creates a new outgoing parameter information block with the given
     * properties and a "max channel" of {@link #MAXCHAN_DEFAULT}. Normally
     * {@link #MAXCHAN_DEFAULT} is required for setting parameter information as
     * a client.
     * <br>
     * <br>
     * The value of the <code>flags</code> argument can be constructed as such:
     * <pre>
new ParamInfo(ParamInfo.FLAGS_MISSEDCALLS_ALLOWED
        | ParamInfo.FLAGS_TYPING_NOTIFICATION
        | ParamInfo.FLAGS_CHANMSGS_ALLOWED, ...);
     * </pre>
     *
     * @param flags a set of flags, like {@link #FLAG_TYPING_NOTIFICATION}
     *        (WinAIM uses <code>FLAGS_MISSEDCALLS_ALLOWED
     *        | FLAGS_TYPING_NOTIFICATION | FLAGS_CHANMSGS_ALLOWED</code>)
     * @param maxMsgLen the maximum length of an IM that can be sent from or
     *        to the client (WinAIM uses <code>8000</code>)
     * @param maxSenderWarning the maximum warning level of either the client
     *        or those sending messages to the client, as a percentage times ten
     *        (WinAIM uses <code>999</code>, for <code>99.9%</code>)
     * @param maxReceiverWarning the maximum warning level of either the client
     *        or those to whom the client is sending messages, as a percentage
     *        times ten (WinAIM uses <code>999</code>, for <code>99.9%</code>)
     * @param minMsgInterval the minimum time interval necessary for sending
     *        messages to or from the client, in milliseconds (WinAIM uses
     *        <code>0</code>)
     */
    public ParamInfo(long flags, int maxMsgLen, int maxSenderWarning,
            int maxReceiverWarning, long minMsgInterval) {
        this(MAXCHAN_DEFAULT, flags, maxMsgLen, maxSenderWarning,
                maxReceiverWarning, minMsgInterval);
    }

    /**
     * Creates a new outgoing parameter information block with the given
     * properties.
     * <br>
     * <br>
     * The value of the <code>flags</code> argument can be constructed as such:
     * <pre>
     new ParamInfo(ParamInfo.FLAGS_MISSEDCALLS_ALLOWED
     | ParamInfo.FLAGS_TYPING_NOTIFICATION
     | ParamInfo.FLAGS_CHANMSGS_ALLOWED, ...);
     * </pre>
     *
     * @param maxChannel the highest ICBM channel supported on this connection;
     *        note that this normally must be <code>0</code> when {@linkplain
     *        SetParamInfoCmd setting ICBM parameters} as a client
     * @param flags a set of flags, like {@link #FLAG_TYPING_NOTIFICATION}
     *        (WinAIM uses <code>FLAGS_MISSEDCALLS_ALLOWED
     *        | FLAGS_TYPING_NOTIFICATION | FLAGS_CHANMSGS_ALLOWED</code>)
     * @param maxMsgLen the maximum length of an IM that can be sent from or
     *        to the client (WinAIM uses <code>8000</code>)
     * @param maxSenderWarning the maximum warning level of either the client
     *        or those sending messages to the client, as a percentage times ten
     *        (WinAIM uses <code>999</code>, for <code>99.9%</code>)
     * @param maxReceiverWarning the maximum warning level of either the client
     *        or those to whom the client is sending messages, as a percentage
     *        times ten (WinAIM uses <code>999</code>, for <code>99.9%</code>)
     * @param minMsgInterval the minimum time interval necessary for sending
     *        messages to or from the client, in milliseconds (WinAIM uses
     *        <code>0</code>)
     */
    public ParamInfo(int maxChannel, long flags, int maxMsgLen,
            int maxSenderWarning, int maxReceiverWarning, long minMsgInterval) {
        DefensiveTools.checkRange(maxChannel, "maxChannel", 0);
        DefensiveTools.checkRange(flags, "flags", 0);
        DefensiveTools.checkRange(maxMsgLen, "maxMsgLen", 0);
        DefensiveTools.checkRange(maxSenderWarning, "maxSenderWarning", 0);
        DefensiveTools.checkRange(maxReceiverWarning, "maxReceiverWarning", 0);
        DefensiveTools.checkRange(minMsgInterval, "minMsgInterval", 0);

        this.maxChannel = maxChannel;
        this.flags = flags;
        this.maxMsgLen = maxMsgLen;
        this.maxSenderWarning = maxSenderWarning;
        this.maxReceiverWarning = maxReceiverWarning;
        this.minMsgInterval = minMsgInterval;
    }

    /**
     * Returns the maximum ICBM channel supported on this connection. This is
     * normally <code>2</code> when coming from a server, <code>0</code> when
     * coming from a client.
     *
     * @return the maximum ICBM channel supported
     */
    public final int getMaxChannel() {
        return maxChannel;
    }

    /**
     * Returns the "flags" associated with this parameter information block.
     * This is normally a bitwise combination of {@link #FLAG_CHANMSGS_ALLOWED},
     * {@link #FLAG_MISSEDCALLS_ALLOWED}, and {@link #FLAG_TYPING_NOTIFICATION}.
     * The presence of these values can be accessed as such:
     * <pre>
if ((paramInfo.getFlags() & ParamInfo.FLAG_TYPING_NOTIFICATION) != 0) {
    System.out.println("Typing notification is supported!");
}
     * </pre>
     *
     * @return the ICBM parameter information flags
     */
    public final long getFlags() {
        return flags;
    }

    /**
     * Returns the maximum length, in bytes, of an IM that can be sent to or
     * from the client.
     *
     * @return the maximum length of an instant message to or from the client
     */
    public final int getMaxMsgLen() {
        return maxMsgLen;
    }

    /**
     * Returns the maximum warning level necessary for sending IM's to or from
     * the client, as a percentage times 10. This defaults to <code>999</code>,
     * for <code>99.9%</code>.
     *
     * @return the maximum warning level necessary for sending IM's to or from
     *         the client
     */
    public final int getMaxSenderWarning() {
        return maxSenderWarning;
    }

    /**
     * Returns the maximum warning level necessary for receiving IM's by the
     * client or those sent by the client to another user, as a percentage times
     * 10. This defaults to <code>999</code>, for <code>99.9%</code>.
     *
     * @return the maximum warning level necessary for receiving IM's by the
     *         client and receiving IM's sent by the client to another user
     */
    public final int getMaxReceiverWarning() {
        return maxReceiverWarning;
    }

    /**
     * Returns the minimum time interval, in milliseconds, between any two
     * messages sent or recieved by the client.
     *
     * @return the minimum message interval
     */
    public final long getMinMsgInterval() {
        return minMsgInterval;
    }

    public long getWritableLength() {
        return 16;
    }

    public void write(OutputStream out) throws IOException {
        BinaryTools.writeUShort(out, maxChannel);
        BinaryTools.writeUInt(out, flags);
        BinaryTools.writeUShort(out, maxMsgLen);
        BinaryTools.writeUShort(out, maxSenderWarning);
        BinaryTools.writeUShort(out, maxReceiverWarning);
        BinaryTools.writeUInt(out, minMsgInterval);
    }

    public String toString() {
        return "ParamInfo: " +
                "maxChannel=" + maxChannel +
                ", flags=0x" + Long.toHexString(flags) +
                ", maxMsgLen=" + maxMsgLen +
                ", maxSenderWarning=" + maxSenderWarning +
                ", maxReceiverWarning=" + maxReceiverWarning +
                ", minMsgInterval=" + minMsgInterval;
    }
}
