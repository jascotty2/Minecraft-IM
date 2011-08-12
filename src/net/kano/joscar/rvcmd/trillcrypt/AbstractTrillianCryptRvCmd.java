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
 *  File created by keith @ Apr 26, 2003
 *
 */

package net.kano.joscar.rvcmd.trillcrypt;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.snaccmd.CapabilityBlock;
import net.kano.joscar.snaccmd.icbm.RecvRvIcbm;
import net.kano.joscar.snaccmd.icbm.RvCommand;
import net.kano.joscar.tlv.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;

/**
 * A base class for the five Trillian Secure IM rendezvous commands provided in
 * this package.
 */
public abstract class AbstractTrillianCryptRvCmd extends RvCommand {
    /** The protocol version number used by Trillian. */
    public static final int VERSION_DEFAULT = 0x0001;

    /**
     * A command type indicating that a command is a request for a secure
     * conversation.
     */
    public static final int CMDTYPE_REQUEST = 0x0000;
    /**
     * A command type indicating that a command is accepting a secure
     * conversation request.
     */
    public static final int CMDTYPE_ACCEPT = 0x0001;
    /** A command type indicating that an encrypted conversation has begun. */
    public static final int CMDTYPE_BEGIN = 0x0002;
    /**
     * A command type indicating that a command contains an encrypted message.
     */
    public static final int CMDTYPE_MESSAGE = 0x0003;
    /**
     * A command type indicating that a command is closing a secure
     * conversation.
     */
    public static final int CMDTYPE_CLOSE = 0x0004;

    /** A TLV type containing a protocol version number. */
    private static final int TYPE_VERSION = 0x03e7;
    /** A TLV type containing the encryption command type. */
    private static final int TYPE_CMDTYPE = 0x03e8;

    /**
     * Extracts the "command type" value from the given incoming Trillian
     * Encryption RV ICBM. This method will return <code>-1</code> if the given
     * command does not contain a command type value.
     *
     * @param icbm an incoming Trillian Encryption RV ICBM command
     * @return the Trillian Encryption command type contained in the given
     *         RV ICBM, or <code>-1</code> if none is present
     */
    public static int getTrillianCmdType(RecvRvIcbm icbm) {
        DefensiveTools.checkNull(icbm, "icbm");

        ByteBlock rvData = icbm.getRvData();

        if (rvData == null) return -1;

        TlvChain chain = TlvTools.readChain(rvData);

        return chain.getUShort(TYPE_CMDTYPE);
    }

    /**
     * Extracts a <code>BigInteger</code> from the given block of ASCII
     * hexadecimal digits.
     *
     * @param block a block of data containing only ASCII hexadecimal digits
     *        (that is, <code>0-9</code> and <code>a-f</code>)
     * @return a <code>BigInteger</code> read from the given string of
     *         hexadecimal digits
     *
     * @see BigInteger#BigInteger(String, int)
     */
    protected static BigInteger getBigIntFromHexBlock(ByteBlock block) {
        DefensiveTools.checkNull(block, "block");

        String hexString = BinaryTools.getNullPadded(block).getString();

        return new BigInteger(hexString, 16);
    }

    /**
     * Returns a 33-byte block of data containing a 32-digit hexadecimal
     * representation of the given number followed by a null byte
     * (<code>0x00</code>).
     *
     * @param num a number smaller than <code>2<sup>128</sup></code> (that is,
     *        a number whose hexadecimal representation is shorter than 33
     *        digits)
     * @return a block of data representing the number as a null-terminated
     *         hexadecimal ASCII string
     *
     * @throws IOException if the given number is too large to be represented
     *         in 32 hexadecimal digits
     */
    protected static byte[] getBigIntHexBlock(BigInteger num)
            throws IOException, IllegalArgumentException {
        DefensiveTools.checkNull(num, "num");

        byte[] data = num.toString(16).getBytes("US-ASCII");

        if (data.length > 32) {
            throw new IllegalArgumentException("number (" + num + ") is too " +
                    "large to fit into 32 hexadecimal digits");
        }

        ByteArrayOutputStream bout = new ByteArrayOutputStream(33);

        for (int i = data.length; i < 32; i++) {
            bout.write('0');
        }

        // write the number
        bout.write(data);

        // and the final null
        bout.write(0);

        return bout.toByteArray();
    }

    /** The encryption protocol version. */
    private final int version;
    /** The command type. */
    private final int cmdType;
    /** Any extra TLV's. */
    private final TlvChain extraTlvs;

    /**
     * Creates a new Trillian Encryption command from the given incoming
     * Trillian Encryption RV ICBM.
     *
     * @param icbm an incoming Trillian Encryption RV ICBM
     */
    protected AbstractTrillianCryptRvCmd(RecvRvIcbm icbm) {
        super(icbm);

        TlvChain chain = TlvTools.readChain(icbm.getRvData());

        version = chain.getUShort(TYPE_VERSION);
        cmdType = chain.getUShort(TYPE_CMDTYPE);

        MutableTlvChain extras = TlvTools.getMutableCopy(chain);
        extras.removeTlvs(new int[] { TYPE_VERSION, TYPE_CMDTYPE });

        extraTlvs = extras;
    }

    /**
     * Creates a new outgoing Trillian Encryption command with the given command
     * type and a protocol version of {@link #VERSION_DEFAULT}.
     *
     * @param cmdType a Trillian Encryption command type, like {@link
     *        #CMDTYPE_BEGIN}
     */
    protected AbstractTrillianCryptRvCmd(int cmdType) {
        this(VERSION_DEFAULT, cmdType);
    }

    /**
     * Creates a new outgoing Trillian Encryption command with the given command
     * type and protocol version.
     *
     * @param encVersion the Trillian Encryption protocol version being used
     *        (normally {@link #VERSION_DEFAULT})
     * @param cmdType a Trillian Encryption command type, like {@link
     *        #CMDTYPE_BEGIN}
     */
    protected AbstractTrillianCryptRvCmd(int encVersion, int cmdType) {
        super(CMDTYPE_REQUEST, CapabilityBlock.BLOCK_TRILLIANCRYPT);

        DefensiveTools.checkRange(encVersion, "version", 0);
        DefensiveTools.checkRange(cmdType, "cmdType", 0);

        this.version = encVersion;
        this.cmdType = cmdType;
        this.extraTlvs = null;
    }

    /**
     * Returns the protocol version being used. This will normally be {@link
     * #VERSION_DEFAULT}.
     *
     * @return the protocol version number sent in this command
     */
    protected final int getVersion() { return version; }

    /**
     * Returns the Trillian Encryption command type of this command.
     *
     * @return this command's Trillian Encryption command type
     */
    protected final int getCmdType() { return cmdType; }

    /**
     * Returns a list of extra TLV's sent in this command that were not parsed
     * into fields like {@link #getVersion() version} and {@link #getCmdType()
     * cmdType}.
     *
     * @return a list of the "extra" TLV's in this command
     */
    protected final TlvChain getExtraTlvs() { return extraTlvs; }

    public void writeRvData(OutputStream out) throws IOException {
        Tlv.getUShortInstance(TYPE_VERSION, version).write(out);
        Tlv.getUShortInstance(TYPE_CMDTYPE, cmdType).write(out);

        writeExtraTlvs(out);
    }

    /**
     * Writes any "extra" TLV's to be sent in this command to the given stream.
     *
     * @param out the stream to which to write
     *
     * @throws IOException
     */
    protected abstract void writeExtraTlvs(OutputStream out) throws IOException;
}
