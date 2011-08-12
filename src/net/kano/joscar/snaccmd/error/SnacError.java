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

package net.kano.joscar.snaccmd.error;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.flapcmd.SnacCommand;
import net.kano.joscar.flapcmd.SnacPacket;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * A SNAC command representing a SNAC error sent in any supported SNAC family.
 * SNAC error commands are normally the command in a family with the command
 * subtype <code>0x01</code>.
 */
public class SnacError extends SnacCommand {
//    public static int CODE_INVALID_ERROR = 0x0000;
    /** An error code indicating that an invalid SNAC command was sent. */
    public static final int CODE_INVALID_SNAC = 0x0001;
    /** An error code indicating that you sent commands too close together. */
    public static final int CODE_TOO_FAST_TO_HOST = 0x0002;
    /**
     * An error code indicating that you sent commands too close together for
     * the receiving user to receive them.
     */
    public static final int CODE_TOO_FAST_TO_CLIENT = 0x0003;
    /**
     * An error code indicating that a user is not "available," which may mean
     * the user has blocked you or that he or she is offline. Note that it does
     * *not* mean that you have blocked that user, like
     * {@link #CODE_IN_LOCAL_PERMIT_DENY} does.
      */
    public static final int CODE_USER_UNAVAILABLE = 0x0004;
    /**
     * An error code indicating that a SNAC family service is
     * "unavailable." This may mean the service no longer exists or that it is
     * temporarily down.
     */
    public static final int CODE_SERVICE_UNAVAILABLE = 0x0005;
    /** An error code indicating that a SNAC family service does not exist. */
    public static final int CODE_SERVICE_UNDEFINED = 0x0006;
    /** An error code indicating that a SNAC command is no longer supported. */
    public static final int CODE_OBSOLETE_SNAC = 0x0007;
    /**
     * An error code indicating that a given action is not supported by the
     * server. Sorry to be so vague; I've never seen this one.
     */
    public static final int CODE_NOT_SUPPORTED_BY_HOST = 0x0008;
    /**
     * An error code indicating that a given action, such as sending a file to
     * a user, is not supported by that user.
     */
    public static final int CODE_NOT_SUPPORTED_BY_CLIENT = 0x0009;
    /**
     * An error code indicating that a message or request was rejected by the
     * client to which it was sent.
     */
    public static final int CODE_REFUSED_BY_CLIENT = 0x000a;
    /**
     * An error code indicating that something is too large. I've never seen
     * this used.
     */
    public static final int CODE_REPLY_TOO_LARGE = 0x000b;
    /** An error code indicating something. I've never seen this used. */
    public static final int CODE_RESPONSES_LOST = 0x000c;
    /**
     * An error code indicating that some request was denied. Sorry for the
     * vagueness; I've never seen this used before.
     */
    public static final int CODE_REQUEST_DENIED = 0x000d;
    /** An error code indicating that a SNAC was malformatted. */
    public static final int CODE_BROKEN_SNAC_DATA = 0x000e;
    /**
     * An error code indicating that the user does not have the necessary
     * permissions to perform an action.
     */
    public static final int CODE_INSUFFICIENT_RIGHTS = 0x000f;
    
    /**
     * An error code indicating that the client attempted to send a message to
     * someone you have blocked.
     */
    public static final int CODE_IN_LOCAL_PERMIT_DENY = 0x0010;
    /**
     * An error code indicating that you cannot send a message to a user because
     * your warning level is too high.
     */
    public static final int CODE_SENDER_WARNING_LEVEL = 0x0011;
    /**
     * An error code indicating that you cannot send a message to a user because
     * his or her warning level is too high.
     */
    public static final int CODE_RECEIVER_WARNING_LEVEL = 0x0012;
    /**
     * An error code indicating that a user is temporarily unavailable.
     */
    public static final int CODE_USER_TEMP_UNAVAILABLE = 0x0013;
    /** An error code meaning something. I've never seen this used. */
    public static final int CODE_NO_MATCH = 0x0014;
    /** An error code meaning something. I've never seen this used. */
    public static final int CODE_LIST_OVERFLOW = 0x0015;
    /** An error code meaning something. I've never seen this used. */
    public static final int CODE_REQUEST_AMBIGUOUS = 0x0016;
    /** An error code meaning something. I've never seen this used. */
    public static final int CODE_QUEUE_FULL = 0x0017;
    /**
     * An error code indicating that a given action cannot be performed while
     * on AOL.
     */
    public static final int CODE_NO_AOL = 0x0018;

    /** The SNAC command subtype of SNAC errors. */
    public static final int CMD_ERROR = 0x0001;

    /** The error code. */
    private final int code;

    /**
     * Generates a new SNAC error command from the given incoming SNAC
     * packet.
     *
     * @param packet an incoming SNAC error packet
     */
    protected SnacError(SnacPacket packet) {
        super(packet.getFamily(), CMD_ERROR);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

        code = BinaryTools.getUShort(snacData, 0);
    }

    /**
     * Creates a new SNAC error command in the given SNAC family and with
     * the given error code.
     *
     * @param family the SNAC family in which this error occurred
     * @param errorCode an error code, like {@link #CODE_INVALID_SNAC}
     */
    public SnacError(int family, int errorCode) {
        super(family, CMD_ERROR);

        DefensiveTools.checkRange(errorCode, "errorCode", 0);

        this.code = errorCode;
    }

    /**
     * Returns the error code associated with this error. Will be one of the
     * <code>CODE_</code>-prefixed constants defined in this class (like
     * {@link #CODE_USER_UNAVAILABLE}).
     *
     * @return the SNAC error code, like {@link #CODE_INVALID_SNAC}
     */
    public final int getErrorCode() { return code; }

    public void writeData(OutputStream out) throws IOException {
        BinaryTools.writeUShort(out, code);
    }

    public String toString() {
        String name = null;
        Field[] fields = SnacError.class.getFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];

            if (!Modifier.isStatic(field.getModifiers())) continue;
            if (!field.getName().startsWith("CODE_")) continue;
            try {
                if (field.getInt(null) == code) {
                    name = field.getName();
                }
            } catch (IllegalAccessException e) {
                continue;
            }
        }
        return "SnacError: code=0x" + Integer.toHexString(code) + " (name: "
                + name + ")";
    }
}
