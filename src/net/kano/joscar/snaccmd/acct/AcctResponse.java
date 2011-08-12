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
 *  File created by keith @ Feb 24, 2003
 *
 */

package net.kano.joscar.snaccmd.acct;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.MiscTools;
import net.kano.joscar.flapcmd.SnacPacket;
import net.kano.joscar.tlv.ImmutableTlvChain;
import net.kano.joscar.tlv.Tlv;
import net.kano.joscar.tlv.TlvChain;
import net.kano.joscar.tlv.TlvTools;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A base class for the several types of account information responses.
 */
public abstract class AcctResponse extends AcctCommand {
    /**
     * An error code indicating that, for some reason, account information
     * cannot be displayed.
     */
    public static final int ERRORCODE_CANNOT_DISPLAY_INFO = 0x0012;
    /**
     * An error code indicating that this account has no associated email
     * address.
     */
    public static final int ERRORCODE_NO_EMAIL = 0x0015;

    /** The only response type seen as of this writing. */
    public static final int TYPE_DEFAULT = 0x0003;

    /**
     * A result code indicating that an account information change was
     * successfully made.
     */
    public static final int RESULT_SUCCESS = 0x0001;
    /**
     * A result code indicating that an account information change failed.
     * Normally accompanied by an {@linkplain #getErrorCode error code}.
     */
    public static final int RESULT_ERROR = 0x0003;

    /** A TLV type containing a screen name. */
    private static final int TYPE_SN = 0x0001;
    /** A TLV type containing an email address. */
    private static final int TYPE_EMAIL = 0x0011;
    /** A TLV type containing an error code. */
    private static final int TYPE_ERROR_CODE = 0x0008;
    /** A TLV type containing an error URL. */
    private static final int TYPE_ERROR_URL = 0x0004;

    /** The type code of this response. */
    private final int type;
    /** The result code of this response. */
    private final int result;

    /** This command's returned screen name. */
    private final String sn;
    /** This command's returned registered email address. */
    private final String email;

    /**
     * This command's returned error code, or <code>-1</code> if none was sent.
     */
    private final int errorCode;
    /** This command's returned error URL. */
    private final String errorUrl;

    /**
     * Generates an account response command of the given command subtype and
     * read from the given incoming SNAC packet.
     *
     * @param command the SNAC command subtype of this command
     * @param packet an account response packet
     */
    protected AcctResponse(int command, SnacPacket packet) {
        super(command);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock block = packet.getData();

        type = BinaryTools.getUShort(block, 0);
        result = BinaryTools.getUShort(block, 2);

        ByteBlock tlvBlock = block.subBlock(4);

        TlvChain chain = TlvTools.readChain(tlvBlock);

        sn = chain.getString(TYPE_SN);
        email = chain.getString(TYPE_EMAIL);

        errorCode = chain.getUShort(TYPE_ERROR_CODE);
        errorUrl = chain.getString(TYPE_ERROR_URL);
    }

    /**
     * Creates an outgoing account response command of the given SNAC subtype
     * and with the given result code. The response's response type will be
     * {@link #TYPE_DEFAULT}.
     *
     * @param command the SNAC command subtype of this command
     * @param result the result code, like {@link #RESULT_SUCCESS}
     */
    protected AcctResponse(int command, int result) {
        this(command, TYPE_DEFAULT, result, null, null, -1, null);
    }

    /**
     * Creates an outgoing account response command of the given SNAC subtype
     * and with the given error code and URL. The response type will be {@link
     * #TYPE_DEFAULT} and the result code will be {@link #RESULT_ERROR}.
     *
     * @param command the SNAC command subtype of this command
     * @param errorCode this command's error code, like {@link
     *        #ERRORCODE_NO_EMAIL}
     * @param errorURL a URL describing this error, or <code>null</code> for
     *        none
     */
    protected AcctResponse(int command, int errorCode, String errorURL) {
        this(command, TYPE_DEFAULT, RESULT_ERROR, null, null, errorCode,
                errorURL);
    }

    /**
     * Creates an outgoing account response command with the given properties.
     *
     * @param command the SNAC command subtype of this command
     * @param type the response type, normally {@link #TYPE_DEFAULT}
     * @param result a result code, like {@link #RESULT_ERROR}
     * @param sn a screen name, or <code>null</code> for none
     * @param email an email address, or <code>null</code> for none
     * @param errorCode an error code, like {@link #ERRORCODE_NO_EMAIL},
     *        or <code>-1</code> for none
     * @param errorUrl an error URL, or <code>null</code> for none
     */
    protected AcctResponse(int command, int type, int result, String sn,
            String email, int errorCode, String errorUrl) {
        super(command);

        DefensiveTools.checkRange(type, "type", 0);
        DefensiveTools.checkRange(result, "result", 0);
        DefensiveTools.checkRange(errorCode, "errorCode", -1);

        this.type = type;
        this.result = result;
        this.sn = sn;
        this.email = email;
        this.errorCode = errorCode;
        this.errorUrl = errorUrl;
    }

    /**
     * Returns the response type of this response. Normally {@link
     * #TYPE_DEFAULT}.
     *
     * @return this response's response type
     */
    public final int getType() {
        return type;
    }

    /**
     * Returns the result code of this response. Normally one of {@link
     * #RESULT_SUCCESS} or {@link #RESULT_ERROR}.
     *
     * @return this response's result code
     */
    public final int getResult() {
        return result;
    }

    /**
     * Returns the screen name sent in this command, or <code>null</code> if
     * none was sent.
     *
     * @return this command's screen name field, or <code>null</code> if none
     *         was sent
     */
    public final String getScreenname() {
        return sn;
    }

    /**
     * Returns the email address under which this screenname is registered, as
     * sent in this command, or <code>null</code> if none was sent.
     *
     * @return this command's registered email address field, or
     *         <code>null</code> if none was sent
     */
    public final String getEmail() {
        return email;
    }

    /**
     * Returns the error code sent in this response, or <code>-1</code> if none
     * was sent.
     *
     * @return this response's error code, or <code>-1</code> if none was sent
     */
    public final int getErrorCode() {
        return errorCode;
    }

    /**
     * Returns the URL explaining the {@linkplain #getErrorCode error code}
     * sent in this response.
     *
     * @return the error URL sent in this command, or <code>null</code> if none
     *         was sent
     */
    public final String getErrorUrl() {
        return errorUrl;
    }

    public void writeData(OutputStream out) throws IOException {
        BinaryTools.writeUShort(out, type);
        BinaryTools.writeUShort(out, result);
        if (sn != null) {
            Tlv.getStringInstance(TYPE_SN, sn).write(out);
        }
        if (email != null) {
            Tlv.getStringInstance(TYPE_EMAIL, email).write(out);
        }
        if (errorCode != -1) {
            Tlv.getUShortInstance(TYPE_ERROR_CODE, errorCode).write(out);
        }
        if (errorUrl != null) {
            Tlv.getStringInstance(TYPE_ERROR_URL, errorUrl).write(out);
        }
    }

    public String toString() {
        return MiscTools.getClassName(this) + ": type=" + type
                + ", result=" + result + ", sn=" + sn + ", email=" + email
                + ", error=" + errorCode + ", errurl=" + errorUrl;
    }
}
