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
import net.kano.joscar.flapcmd.SnacPacket;
import net.kano.joscar.tlv.ImmutableTlvChain;
import net.kano.joscar.tlv.Tlv;
import net.kano.joscar.tlv.TlvChain;
import net.kano.joscar.tlv.TlvTools;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A command sent in response to an {@link ConfirmAcctCmd}.
 *
 * @snac.src server
 * @snac.cmd 0x07 0x07
 *
 * @see ConfirmAcctCmd
 */
public class ConfirmAck extends AcctCommand {
    /**
     * A result code indicating that the confirmation request succeeded and
     * a confirmation email has been sent to this screenname's registered email
     * address.
     */
    public static final int RESULT_SUCCESS = 0x0001;
    /**
     * A result code indicating that confirmation is currently unavailable for
     * some reason or other.
     */
    public static final int RESULT_UNAVAILABLE = 0x0003;
    /**
     * A result code indicating that a confirmation email is not necessary
     * because this screen name has already been confirmed.
     */
    public static final int RESULT_ALREADY_CONFIRMED = 0x001e;

    /** A TLV type containing an error URL. */
    private static final int TYPE_ERROR_URL = 0x0004;

    /** The confirmation request's result code. */
    private final int result;
    /** The confirmation request's error URL. */
    private final String errorUrl;

    /**
     * Generates a confirmation request response from the given incoming SNAC
     * packet.
     *
     * @param packet a confirmation request response SNAC packet
     */
    protected ConfirmAck(SnacPacket packet) {
        super(CMD_CONFIRM_ACK);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

        result = BinaryTools.getUShort(snacData, 0);

        ByteBlock tlvBlock = snacData.subBlock(2);

        TlvChain chain = TlvTools.readChain(tlvBlock);

        errorUrl = chain.getString(TYPE_ERROR_URL);
    }

    /**
     * Creates a new outgoing confirmation request response with the given
     * result code and no error URL.
     *
     * @param result a result code
     */
    public ConfirmAck(int result) {
        this(result, null);
    }

    /**
     * Creates a new outgoing confirmation request error response with the given
     * result code and error URL.
     *
     * @param result this response's result code, like {@link #RESULT_SUCCESS}
     * @param errorURL a URL explaining an error that occurred
     */
    public ConfirmAck(int result, String errorURL) {
        super(CMD_CONFIRM_ACK);

        DefensiveTools.checkRange(result, "result", 0);

        this.result = result;
        this.errorUrl = errorURL;
    }

    /**
     * Returns this response's result code. Generally one of {@link
     * #RESULT_SUCCESS}, {@link #RESULT_UNAVAILABLE}, and {@link
     *  #RESULT_ALREADY_CONFIRMED}.
     *
     * @return the result code associated with this response
     */
    public final int getResult() {
        return result;
    }

    /**
     * A URL describing any error that occurred. Generally only sent when
     * the {@linkplain #getResult result code} is not {@link #RESULT_SUCCESS}.
     *
     * @return this response's error URL
     */
    public final String getErrorUrl() {
        return errorUrl;
    }

    public void writeData(OutputStream out) throws IOException {
        BinaryTools.writeUShort(out, result);

        if (errorUrl != null) {
            Tlv.getStringInstance(TYPE_ERROR_URL, errorUrl).write(out);
        }
    }

    public String toString() {
        return "ConfirmAck with result " + result + " (url=" + errorUrl + ")";
    }
}
