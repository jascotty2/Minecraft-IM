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
 *  File created by keith @ Apr 28, 2003
 *
 */

package net.kano.joscar.rvproto.directim;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.ImEncodedString;
import net.kano.joscar.ImEncodingParams;
import net.kano.joscar.LiveWritable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A data structure containing information about a Direct IM message or typing
 * notification. Note that this is only a header and does not contain the
 * message (and attached images or other media) itself.
 */
public final class DirectImHeader implements LiveWritable {
    /** The Direct IM protocol version string used by WinAIM. */
    public static final String DCVERSION_DEFAULT = "ODC2";

    /**
     * A flag indicating that a packet is a typing-notification packet. Note
     * that this flag is always sent in typing notification headers, whether
     * the header means that the user is typing, has stopped typing, or has
     * erased what he has typed.
     * <br>
     * <br>
     * If this flag is combined with {@link #FLAG_TYPING}, the user is currently
     * typing a message.<br>
     * If this flag is combined with {@link #FLAG_TYPED}, the user has typed a
     * message, but has momentarily stopped typing.<br>
     * If this flag is not sent with either {@link #FLAG_TYPING} <i>or</i>
     * {@link #FLAG_TYPED}, the user has erased any message he had previously
     * been typing.
     */
    public static final long FLAG_TYPINGPACKET = 0x02;
    /**
     * A flag indicating, when combined with {@link #FLAG_TYPINGPACKET}, that
     * the user is typing a message.
     */
    public static final long FLAG_TYPING = 0x08;
    /**
     * A flag indicating, when combined with {@link #FLAG_TYPINGPACKET}, that
     * the user has typed a message, but has momentarily stopped typing.
     */
    public static final long FLAG_TYPED = 0x04;

    /** A flag indicating that a message is an "auto-response." */
    public static final long FLAG_AUTORESPONSE = 0x01;

    /**
     * Creates a new Direct IM header that indicates that the user is typing
     * a message. That is, this method creates a direct IM header with all
     * {@linkplain #setDefaults defaults} set and the flags
     * <code>{@link #FLAG_TYPINGPACKET} | {@link #FLAG_TYPING}</code>.
     *
     * @return a new Direct IM header indicating that the user is typing a
     *         message
     */
    public static DirectImHeader createTypingHeader() {
        DirectImHeader hdr = new DirectImHeader();

        hdr.setDefaults();
        hdr.setFlags(FLAG_TYPINGPACKET | FLAG_TYPING);

        return hdr;
    }

    /**
     * Creates a new Direct IM header that indicates that the user has typed a
     * message, but has momentarily stopped typing. That is, this method creates
     * a direct IM header with all {@linkplain #setDefaults defaults} set and
     * the flags <code>{@link #FLAG_TYPINGPACKET} | {@link #FLAG_TYPED}</code>.
     *
     * @return a new Direct IM header indicating that the user has typed a
     *         message, but has stopped typing
     */
    public static DirectImHeader createTypedHeader() {
        DirectImHeader hdr = new DirectImHeader();

        hdr.setDefaults();
        hdr.setFlags(FLAG_TYPINGPACKET | FLAG_TYPED);

        return hdr;
    }

    /**
     * Creates a new Direct IM header that indicates that the user has erased
     * the message he or she had previously typed. That is, this method creates
     * a direct IM header with all {@linkplain #setDefaults defaults} set and
     * the flags <code>{@link #FLAG_TYPINGPACKET}</code>.
     *
     * @return a new Direct IM header indicating that the user is typing a
     *         message
     */
    public static DirectImHeader createTypingErasedHeader() {
        DirectImHeader hdr = new DirectImHeader();

        hdr.setDefaults();
        hdr.setFlags(FLAG_TYPINGPACKET);

        return hdr;
    }

    /**
     * Creates a new direct IM header appropriate for sending the given message.
     * Note that the returned header does <i>not</i> contain the given message;
     * rather, it is simply appropriate for sending as a header for the message
     * body itself.
     *
     * @param message a message for which a Direct IM header should be returned
     * @return a direct IM header appropriate for sending with the given message
     */
    public static DirectImHeader createMessageHeader(ImEncodedString message) {
        return createMessageHeader(message, false);
    }

    /**
     * Creates a new direct IM header appropriate for sending the given message.
     * Note that the returned header does <i>not</i> contain the given message;
     * rather, it is simply appropriate for sending as a header for the message
     * body itself.
     *
     * @param message a message for which a Direct IM header should be returned
     * @param autoresponse whether or not the given message is an
     *        "auto-response"
     * @return a direct IM header appropriate for sending with the given message
     */
    public static DirectImHeader createMessageHeader(ImEncodedString message,
            boolean autoresponse) {
        DefensiveTools.checkNull(message, "message");

        DirectImHeader hdr = new DirectImHeader();

        hdr.setDefaults();
        hdr.setFlags(autoresponse ? FLAG_AUTORESPONSE : 0);
        hdr.setEncoding(message.getEncoding());
        hdr.setDataLength(message.getBytes().length);

        return hdr;
    }

    /** The direct IM protocol version string to send in this message. */
    private String dcVersion = null;
    /** A message ID. */
    private long messageId = 0;
    /** The length of the data being sent. */
    private long dataLength = -1;
    /**
     * An object describing the encoding of the message block to follow this
     * header.
     */
    private ImEncodingParams encoding = null;
    /** A set of bit flags for this header. */
    private long flags = -1;
    /** The screenname of the user from which this message is being sent. */
    private String sn = null;
    /**
     * The size of this header, in bytes, if read from an incoming block of
     * bytes.
     */
    private int headerSize = -1;

    /**
     * Creates a new direct IM header with all values set to <code>-1</code>
     * or <code>null</code>, depending on type.
     *
     * @see #setDefaults
     */
    public DirectImHeader() { }

    /**
     * Creates a new direct IM header object with the same properties as the
     * given header object.
     *
     * @param header a direct IM header object to copy
     */
    public DirectImHeader(DirectImHeader header) {
        DefensiveTools.checkNull(header, "header");

        this.dcVersion = header.dcVersion;
        this.messageId = header.messageId;
        this.dataLength = header.dataLength;
        this.encoding = header.encoding;
        this.flags = header.flags;
        this.sn = header.sn;
    }

    /**
     * Creates a new Direct IM header from the data in the given stream. Note
     * that this method will block until a full header has been read from the
     * given stream. This method will return <code>null</code> if no valid
     * header can be read.
     *
     * @param in the stream from which to read a direct IM header
     * @return a direct IM header read from the given stream, or
     *         <code>null</code> if none could be read
     *
     * @throws IOException if an I/O error occurs
     */
    public static DirectImHeader readDirectIMHeader(InputStream in)
            throws IOException {
        DefensiveTools.checkNull(in, "in");

        // read the six-byte meta-header containing the ODC version and the
        // length of the real header
        byte[] miniHeader = new byte[6];
        for (int i = 0; i < miniHeader.length;) {
            int count = in.read(miniHeader, i, miniHeader.length - i);

            if (count == -1) return null;

            i += count;
        }

        // create a header object
        DirectImHeader hdr = new DirectImHeader();

        // extract those two values (the version and the header length)
        ByteBlock miniHeaderBlock = ByteBlock.wrap(miniHeader);
        ByteBlock verBlock = miniHeaderBlock.subBlock(0, 4);

        hdr.setDcVersion(BinaryTools.getAsciiString(verBlock));

        int headerLen = BinaryTools.getUShort(miniHeaderBlock, 4);
        if (headerLen < 6) return null;

        hdr.setHeaderSize(headerLen);

        // now read the real header. note that headerLen includes the length of
        // the mini-header.
        byte[] headerData = new byte[headerLen - 6];
        for (int i = 0; i < headerData.length;) {
            int count = in.read(headerData, i, headerData.length - i);

            if (count == -1) return null;

            i += count;
        }

        // okay.
        ByteBlock header = ByteBlock.wrap(headerData);

        hdr.setMessageId(BinaryTools.getLong(header, 6));
        hdr.setDataLength(BinaryTools.getUInt(header, 22));
        int charsetCode = BinaryTools.getUShort(header, 26);
        int charsetSubcode = BinaryTools.getUShort(header, 28);
        hdr.setEncoding(new ImEncodingParams(charsetCode, charsetSubcode));
        hdr.setFlags(BinaryTools.getUInt(header, 30));

        ByteBlock snBlock = header.subBlock(38, 16);
        hdr.setScreenname(BinaryTools.getNullPadded(snBlock).getString());

        return hdr;
    }

    /**
     * Returns the direct IM protocol version string sent in this header. This
     * is normally {@link #DCVERSION_DEFAULT}.
     *
     * @return the direct IM protocol version string sent in this header
     */
    public synchronized final String getDcVersion() { return dcVersion; }

    /**
     * Returns the message ID for this header.
     *
     * @return this header's message ID
     */
    public synchronized final long getMessageId() { return messageId; }

    /**
     * Returns the length of the message body data to follow this header.
     *
     * @return the length of the data that will follow this header, in bytes
     */
    public synchronized final long getDataLength() { return dataLength; }

    /**
     * Returns an object describing the encoding being used for the message body
     * to follow this header.
     *
     * @return an object describing the encoding to be used to decode the
     *         message body that follows this header
     */
    public synchronized final ImEncodingParams getEncoding() { return encoding; }

    /**
     * Returns a set of bit flags for this header. Will normally be a
     * combination of any of the {@linkplain #FLAG_TYPINGPACKET
     * <code>FLAG_<i>*</i></code> flags}. To test for a particular flag, one
     * might use code resembling the following:
     * <pre>
if ((directImHeader.getFlags() & DirectImHeader.FLAG_AUTORESPONSE) != 0) {
    System.out.println("Message is an auto-response!");
}
     * </pre>
     *
     * @return a set of flags contained in this direct IM header
     */
    public synchronized final long getFlags() { return flags; }

    /**
     * Returns the screenname sent in this header. Note that this value should
     * be ignored, as while it is supposed to be the screenname of the user
     * sending this message, any screenname can actually be sent. (WinAIM
     * ignores this value.)
     *
     * @return the screenname sent in this header
     */
    public synchronized final String getScreenname() { return sn; }

    /**
     * Returns the size of this header, in bytes, if read from an incoming
     * stream. Note that this value will be <code>-1</code> if this header was
     * not read from an incoming stream.
     *
     * @return the size of this header, in bytes, or <code>-1</code> if this
     *         header was not read from an incoming stream
     */
    public synchronized final int getHeaderSize() { return headerSize; }

    /**
     * Sets the direct connection version string to send in this header.
     *
     * @param dcVersion the direct connection version string to send in this
     *        header
     */
    public synchronized final void setDcVersion(String dcVersion) {
        this.dcVersion = dcVersion;
    }

    /**
     * Sets the message ID for this header.
     *
     * @param messageId the message ID to send in this header
     */
    public synchronized final void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    /**
     * Sets the length of the message data to follow this header.
     *
     * @param dataLength the data length value to send in this header
     */
    public synchronized final void setDataLength(long dataLength) {
        this.dataLength = dataLength;
    }

    /**
     * Sets the encoding method used to encode the message body to follow this
     * header. Note that if this value is <code>null</code>, a charset and
     * charsubset of <code>0</code> and <code>0</code> will be sent with this
     * command, suitable for sending a typing notification or other non-message
     * header.
     *
     * @param encoding the encoding method used to encode the message body to
     *        follow this header, or <code>null</code> for none
     */
    public synchronized final void setEncoding(ImEncodingParams encoding) {
        this.encoding = encoding;
    }

    /**
     * Sets the bit flags to send in this header. This should be a bitwise
     * combination of any of {@linkplain #FLAG_TYPINGPACKET the
     * <code>FLAG_<i>*</i></code> constants}.
     *
     * @param flags a set of flags to send in this header
     */
    public synchronized final void setFlags(long flags) {
        this.flags = flags;
    }

    /**
     * Sets the screenname to send in this header. This value is supposed to be
     * the screenname of the user sending the header, but in practice it is
     * simply ignored by the official clients (as it should be). Note that by
     * sending a different screenname allows one to remotely spoof messages from
     * other users when chatting with someone using <a
     * href="http://gaim.sf.net">Gaim</a>.
     *
     * @param sn a screenname to send in this header
     */
    public synchronized final void setScreenname(String sn) { this.sn = sn; }

    /**
     * Sets the size of this header, in bytes, as read from an incoming stream.
     *
     * @param headerSize the size of this header, in bytes
     */
    private synchronized final void setHeaderSize(int headerSize) {
        this.headerSize = headerSize;
    }

    /**
     * Sets some default values for this header. Calling this method is
     * equivalent to excecuting the following code:
     * <pre>
header.setDcVersion(DirectImHeader.DCVERSION_DEFAULT);
header.setDataLength(0);
header.setEncoding(null);
header.setFlags(0);
header.setScreenname("");
     * </pre>
     * Note that a call to {@link #write} will never throw an
     * <code>IllegalArgumentException</code> if it has not been modified since
     * a call to this method.
     */
    public synchronized final void setDefaults() {
        this.dcVersion = DCVERSION_DEFAULT;
        this.dataLength = 0;
        this.encoding = null;
        this.flags = 0;
        this.sn = "";
    }

    /**
     * Ensures that this direct IM header contains valid values for all fields.
     *
     * @throws IllegalArgumentException if a field has an invalid value
     */
    private synchronized void checkValidity() throws IllegalArgumentException {
        DefensiveTools.checkNull(dcVersion, "dcVersion");
        DefensiveTools.checkRange(dataLength, "dataLength", 0);
        DefensiveTools.checkNull(encoding, "encoding");
        DefensiveTools.checkRange(flags, "flags", 0);
        DefensiveTools.checkNull(sn, "sn");
    }

    /**
     * Writes this header to the given stream. Note that this method will not
     * write any data to the stream if any fields in this header object are
     * invalid. Valid values for individual fields are as follows:
     * <table>
     * <tr><th>Field</th><th>Must be...</th></tr>
     * <tr><td><code>dcVersion</code></td><td>non-<code>null</code></td></tr>
     * <tr><td><code>dataLength</code></td><td>nonnegative (<code>0</code> or
     * greater)</td></tr>
     * <tr><td><code>encoding</code></td><td>non-<code>null</code></td></tr>
     * <tr><td><code>flags</code></td><td>nonnegative (<code>0</code> or
     * greater)</td></tr>
     * <tr><td><code>screenname</code></td><td>non-<code>null</code></td></tr>
     * </table>
     *
     * @param out the stream to which to write
     *
     * @throws IOException if an I/O error occurs
     * @throws IllegalArgumentException if a field is invalid
     */
    public synchronized void write(OutputStream out)
            throws IOException, IllegalArgumentException {
        DefensiveTools.checkNull(out, "out");

        checkValidity();

        ByteArrayOutputStream hout = new ByteArrayOutputStream(76);

        byte[] hdrBytes = BinaryTools.getAsciiBytes(dcVersion);
        hout.write(hdrBytes);
        BinaryTools.writeUShort(hout, 76);

        BinaryTools.writeUShort(hout, 1);
        BinaryTools.writeUShort(hout, 6);
        BinaryTools.writeUShort(hout, 0);

        BinaryTools.writeLong(hout, messageId);
        hout.write(new byte[8]);
        BinaryTools.writeUInt(hout, dataLength);

        if (encoding != null) {
            BinaryTools.writeUShort(hout, encoding.getCharsetCode());
            BinaryTools.writeUShort(hout, encoding.getCharsetSubcode());
        } else {
            BinaryTools.writeUShort(hout, 0);
            BinaryTools.writeUShort(hout, 0);
        }

        BinaryTools.writeUInt(hout, flags);
        BinaryTools.writeUInt(hout, 0);

        ByteBlock snBlock = ByteBlock.wrap(BinaryTools.getAsciiBytes(sn));
        BinaryTools.writeNullPadded(hout, snBlock, 16);

        hout.write(new byte[16]);

        hout.writeTo(out);
    }

    public synchronized String toString() {
        return "DirectIMHeader: " +
                "msgid=" + messageId +
                ", dataLen=" + dataLength +
                ", encoding=" + encoding +
                ", flags=0x" + Long.toHexString(flags) +
                ", sn='" + sn + "'" +
                ", headerSize=" + headerSize;
    }
}
