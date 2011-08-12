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

package net.kano.joscar.snaccmd;

import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.EncodedStringInfo;
import net.kano.joscar.LiveWritable;
import net.kano.joscar.MinimalEncoder;
import net.kano.joscar.OscarTools;
import net.kano.joscar.tlv.ImmutableTlvChain;
import net.kano.joscar.tlv.Tlv;
import net.kano.joscar.tlv.TlvChain;
import net.kano.joscar.tlv.TlvTools;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A data structure used to transmit one's "info" ("profile"), away message,
 * and capability blocks.
 */
public class InfoData implements LiveWritable {
    /**
     * An away message string indicating that one has come back from away.
     * It is of note that this is just an empty string, or <code>""</code>.
     */
    public static final String NOT_AWAY = "";

    /**
     * Reads a user info data block from the given data block. Calling this
     * method is equvalent to calling {@link #readInfoDataFromChain
     * readInfoDataFromChain(TlvChain.readChain(block)}.
     *
     * @param block the data block containing user info
     * @return a user info data object read from the given data block
     */
    public static InfoData readInfoData(ByteBlock block) {
        return readInfoDataFromChain(TlvTools.readChain(block));
    }

    /**
     * Reads a user info data block from the given TLV chain. (A user info data
     * block is simply a series of TLV's.)
     *
     * @param chain the TLV chain containing user info TLV's
     * @return a user info data block read from the given TLV chain
     */
    public static InfoData readInfoDataFromChain(TlvChain chain) {
        DefensiveTools.checkNull(chain, "chain");

        String awayType = chain.getString(TYPE_AWAY_FMT);
        Tlv awayTlv = chain.getLastTlv(TYPE_AWAY);
        String infoType = chain.getString(TYPE_INFO_FMT);
        Tlv infoTlv = chain.getLastTlv(TYPE_INFO);
        Tlv capTlv = chain.getLastTlv(TYPE_CAPS);
        Tlv certTlv = chain.getLastTlv(TYPE_CERTIFICATE_INFO);

        String awayMessage = null;
        if (awayTlv != null) {
            awayMessage
                    = OscarTools.getInfoString(awayTlv.getData(), awayType);
        }

        String info = null;
        if (infoTlv != null) {
            info = OscarTools.getInfoString(infoTlv.getData(), infoType);
        }

        CapabilityBlock[] caps = null;
        if (capTlv != null) {
            caps = CapabilityBlock.getCapabilityBlocks(capTlv.getData());
        }

        CertificateInfo certInfo = null;
        if (certTlv != null) {
            certInfo = CertificateInfo.readCertInfoBlock(certTlv.getData());
        }

        return new InfoData(info, awayMessage, caps, certInfo);
    }

    /**
     * A TLV type containing the "format" of the user info. This is generally of
     * the form <code>text/x-aolrtf; charset=us-ascii</code>.
     */
    private static final int TYPE_INFO_FMT = 0x0001;

    /**
     * A TLV type containing the user info text.
     */
    private static final int TYPE_INFO = 0x0002;

    /**
     * a TLV type containing the "format" of the away message. Generally of the
     * form <code>text/x-aolrtf; charset=us-ascii</code>.
     */
    private static final int TYPE_AWAY_FMT = 0x0003;

    /**
     * A TLV type containing the away message text.
     */
    private static final int TYPE_AWAY = 0x0004;

    /**
     * A TLV type containing a list of capability blocks.
     */
    private static final int TYPE_CAPS = 0x0005;

    /** A TLV type containing a certificate information block. */
    private static final int TYPE_CERTIFICATE_INFO = 0x0006;

    /**
     * The user info text in this structure.
     */
    private final String info;

    /**
     * The away message text in this structure.
     */
    private final String awayMessage;

    /**
     * The capability block list in this structure.
     */
    private final CapabilityBlock[] caps;

    /** A block of certificate information for the associated user. */
    private final CertificateInfo certInfo;

    /**
     * Creates a new <code>InfoData</code> structure containing only an away
     * message. Note that the <code>awayMessage</code> can be {@link #NOT_AWAY}
     * (an empty string), indicating that the user is back from being away.
     *
     * @param awayMessage the away message, or {@link #NOT_AWAY} (an empty
     *        string) to indicate that the user is back from being away
     */
    public InfoData(String awayMessage) {
        this(null, awayMessage, null, null);
    }

    /**
     * Creates a new info data object with the given properties. Any of these
     * can be <code>null</code> to indicate that that field shall not be sent.
     * Note that to unset away (to set "back"), one must use {@link #NOT_AWAY}
     * (which is actually just an empty string) instead of <code>null</code> for
     * the <code>awayMessage</code> argument.
     *
     * @param info the user's user info text
     * @param awayMessage the user's away message
     * @param caps a list of supported capability blocks
     * @param certInfo client certificate information (for Encrypted IM)
     */
    public InfoData(String info, String awayMessage, CapabilityBlock[] caps,
            CertificateInfo certInfo) {
        this.info = info;
        this.awayMessage = awayMessage;
        this.caps = (CapabilityBlock[])
                DefensiveTools.getNonnullArray(caps, "caps");
        this.certInfo = certInfo;
    }

    /**
     * Returns the user info text associated with this object, or
     * <code>null</code> if that field was not included in this object.
     *
     * @return the users user info text (his or her "profile")
     */
    public final String getInfo() {
        return info;
    }

    /**
     * Returns the away message associated with this object, or
     * <code>null</code> if that field was not sent.
     *
     * @return the user's away message
     */
    public final String getAwayMessage() {
        return awayMessage;
    }

    /**
     * Returns a list of capability blocks advertised by this user, or
     * <code>null</code> if this field was not sent. Note that this field will
     * be a zero-length array instead of <code>null</code> if this field was
     * sent but empty.
     *
     * @return the user's supported capability blocks
     */
    public final CapabilityBlock[] getCaps() {
        return (CapabilityBlock[]) (caps == null ? null : caps.clone());
    }

    /**
     * Returns the certificate information block contained in this info data
     * object.
     *
     * @return the associated user's certificate information block, or
     *         <code>null</code> if none was sent
     */
    public final CertificateInfo getCertificateInfo() { return certInfo; }

    /**
     * Returns an "info format string" in the form <code>text/aolrtf;
     * charset=us-ascii</code>, where <code>us-ascii</code> is the given
     * charset.
     *
     * @param charset the charset to use in the returned info format string
     * @return the info format string generated from the given charset
     */
    private static String getFormatString(String charset) {
        return "text/x-aolrtf; charset=" + charset;
    }

    /**
     * Writes two TLV's to the given output stream: one to write the {@linkplain
     * #getFormatString format} of the given text, and one to write the text
     * in the {@linkplain net.kano.joscar.MinimalEncoder minimal encoding} possible.
     *
     * @param text the text to write
     * @param out the stream to write to
     * @param fmtType the TLV type for the text format string
     * @param textType the TLV type for the text itself
     * @throws IOException if an I/O error occurs
     */
    private static void writeInfoTlvs(String text, OutputStream out,
            int fmtType, int textType) throws IOException {
        EncodedStringInfo encInfo = MinimalEncoder.encodeMinimally(text);
        ByteBlock infoBlock = ByteBlock.wrap(encInfo.getData());
        String formatString = getFormatString(encInfo.getCharset());

        Tlv.getStringInstance(fmtType, formatString).write(out);
        new Tlv(textType, infoBlock).write(out);
    }

    public void write(OutputStream out) throws IOException {
        if (info != null) {
            writeInfoTlvs(info, out, TYPE_INFO_FMT, TYPE_INFO);
        }
        if (awayMessage != null) {
            writeInfoTlvs(awayMessage, out, TYPE_AWAY_FMT, TYPE_AWAY);
        }
        if (caps != null) {
            byte[] capBlock = CapabilityBlock.convertToBytes(caps);
            new Tlv(TYPE_CAPS, ByteBlock.wrap(capBlock)).write(out);
        }
        if (certInfo != null) {
            ByteBlock certInfoBlock = ByteBlock.createByteBlock(certInfo);
            new Tlv(TYPE_CERTIFICATE_INFO, certInfoBlock).write(out);
        }
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("InfoData:");
        if (info != null && info.length() > 0) {
            String display = info;
            if (display.length() > 20) {
                display = display.substring(0, 20) + "...";
            }
            buffer.append("  info: ");
            buffer.append(display);
        }
        if (awayMessage != null && awayMessage.length() > 0) {
            String display = awayMessage;
            if (display.length() > 20) {
                display = display.substring(0, 20) + "...";
            }
            buffer.append("  away: ");
            buffer.append(display);
        }
        if (caps != null && caps.length > 0) {
            buffer.append("  capabilities: ");
            buffer.append(caps.length);
        }
        if (certInfo != null) {
            buffer.append("  certinfo: ");
            buffer.append(certInfo);
        }

        return buffer.toString();
    }
}
