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
 *  File created by keith @ Feb 18, 2003
 *
 */

package net.kano.joscar.tlv;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.LiveWritable;
import net.kano.joscar.Writable;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * Represents a "Type-Length-Value" block, a very popular data structure in the
 * OSCAR protocol. Fun trivia: as of this writing, this class is referenced 267
 * times throughout joscar. A TLV simply contains a two-byte unsigned "type" and
 * a "value" whose binary length is less than 65536 bytes. This class is
 * immutable.
 */
public final class Tlv implements Writable {
    /** This TLV's type. */
    private final int type;
    /** This TLV's value. */
    private ByteBlock data;
    /** The original size of this TLV, as read from a stream. */
    private final int totalSize;
    /** An object used to write the TLV data to a stream. */
    private final Writable writer;

    /**
     * Returns a new TLV of the given type containing the given string encoded
     * to bytes with the US-ASCII encoder. In other words, creates a TLV
     * containing the given ASCII string.
     *
     * @param type the type of the TLV to be returned
     * @param string the ASCII string to use as the TLV's data
     * @return a TLV of the given type containing the given ASCII string as its
     *         data block
     */
    public static Tlv getStringInstance(int type, String string) {
        return new Tlv(type, ByteBlock.wrap(BinaryTools.getAsciiBytes(string)));
    }

    /**
     * Returns a new TLV of the given type containing the given value as a
     * two-byte unsigned integer as its data block.
     *
     * @param type the type of the TLV to be returned
     * @param number the value of the TLV, to be encoded as a two-byte unsigned
     *        integer
     * @return a TLV of the given type with the given value as its data block
     */
    public static Tlv getUShortInstance(int type, int number) {
        return new Tlv(type, ByteBlock.wrap(BinaryTools.getUShort(number)));
    }
    /**
     * Returns a new TLV of the given type containing the given value as a
     * four-byte unsigned integer as its data block.
     *
     * @param type the type of the TLV to be returned
     * @param number the value of the TLV, to be encoded as a four-byte unsigned
     *        integer
     * @return a TLV of the given type with the given value as its data block
     */

    public static Tlv getUIntInstance(int type, long number) {
        return new Tlv(type, ByteBlock.wrap(BinaryTools.getUInt(number)));
    }

    /**
     * Returns <code>true</code> if there is a valid TLV at the beginning of the
     * given data block. This only checks for the length of the block, and in no
     * way performs validation on its contents.
     *
     * @param tlvBytes the bytes supposedly containing a TLV at the beginning
     * @return whether or not a valid TLV exists at the beginning of the given
     *         block of data
     */
    public static boolean isValidTLV(ByteBlock tlvBytes) {
        if (tlvBytes.getLength() < 4) return false;

        int length = BinaryTools.getUShort(tlvBytes, 2);

        return length <= tlvBytes.getLength() - 4;
    }

    /**
     * Creates a TLV of the given type with no data block. Equivalent to calling
     * {@link #Tlv(int, Writable) new Tlv(type, ByteBlock.EMPTY_BLOCK)}.
     *
     * @param type the type of this TLV
     */
    public Tlv(int type) {
        this(type, ByteBlock.EMPTY_BLOCK);
    }

    /**
     * Creates a TLV of the given type with the given data block.
     *
     * @param type the type of this TLV
     * @param data this TLV's data block
     */
    public Tlv(int type, Writable data) {
        DefensiveTools.checkRange(type, "type", 0);
        DefensiveTools.checkNull(data, "data");

        this.type = type;
        this.writer = data;
        this.data = (data instanceof ByteBlock ? (ByteBlock) data : null);
        this.totalSize = -1;
    }

    /**
     * Creates a new TLV from the beginning of the given data block. The total
     * number of bytes read can be returned by calling
     * <code>getTotalSize</code>.
     *
     * @param tlvBytes a block of data containing a TLV at the beginning
     * @throws IllegalArgumentException if the beginning of the given byte block
     *         does not contain a valid TLV at the beginning
     */
    public Tlv(ByteBlock tlvBytes) throws IllegalArgumentException {
        DefensiveTools.checkNull(tlvBytes, "tlvBytes");

        int blocklen = tlvBytes.getLength();
        if (blocklen < 4) {
            throw new IllegalArgumentException("data not long enough to be a " +
                    "TLV (length=" + blocklen + ")");
        }

        int length = BinaryTools.getUShort(tlvBytes, 2);
        if (length > blocklen - 4) {
            throw new IllegalArgumentException("data (length=" +
                    blocklen + ") not long enough to fulfill " +
                    "header-specified length (" + length + ")");
        }

        type = BinaryTools.getUShort(tlvBytes, 0);
        data = tlvBytes.subBlock(4, length);
        writer = data;
        totalSize = length + 4;
    }

    /**
     * Returns this TLV's type. This will be an integer ranging inclusively
     * from <code>0</code> to <code>BinaryTools.USHORT_MAX</code>.
     *
     * @return this TLV's type
     */
    public final int getType() {
        return type;
    }

    /**
     * Returns this TLV's data block. Note that if this TLV was {@linkplain
     * #Tlv(int, Writable) created with a <code>Writable</code>} that is not a
     * <code>ByteBlock</code>, a byte block is generated from that
     * <code>Writable</code>, which may be <i>a computationally expensive
     * operation</i>.
     *
     * @return this TLV's data block
     */
    public synchronized final ByteBlock getData() {
        if (data == null) data = ByteBlock.createByteBlock(writer);

        return data;
    }

    /**
     * Returns the object that will be used to write the TLV data to a stream.
     * May be a <code>ByteBlock</code>.
     *
     * @return the object that will be used to write the TLV data block to
     *         a stream with {@link #write}
     */
    public final LiveWritable getDataWriter() {
        return writer;
    }

    /**
     * Returns this TLV's data block decoded into an US-ASCII string. In other
     * words, reads the TLV's data block as if it were an ASCII string.
     *
     * @return an ASCII string read from this TLV's data block
     * @see BinaryTools#getAsciiString
     */
    public final String getDataAsString() {
        return BinaryTools.getAsciiString(data);
    }

    /**
     * Returns an unsigned integer read from the first two bytes of this TLV's
     * data block, or <code>-1</code> if fewer than two bytes exist in the
     * block.
     *
     * @return an unsigned two-byte integer read from this TLV's data block,
     *         or <code>-1</code> if none exists
     * @see BinaryTools#getUShort(ByteBlock, int)
     */
    public final int getDataAsUShort() {
        return BinaryTools.getUShort(data, 0);
    }

    /**
     * Returns an unsigned integer read from the first four bytes of this TLV's
     * data block, or <code>-1</code> if fewer than four bytes exist in the
     * block.
     *
     * @return an unsigned four-byte integer read from this TLV's data block,
     *         or <code>-1</code> if none exists
     * @see BinaryTools#getUInt(ByteBlock, int)
     */
    public final long getDataAsUInt() {
        return BinaryTools.getUInt(data, 0);
    }

    /**
     * Returns the total size of this object, as read from an input stream. Will
     * be <code>-1</code> if this TLV was not read from a stream but instead
     * constructed manually.
     *
     * @return the total size, in bytes, of this object, or <code>-1</code> if
     *         this object wasn't read from an incoming stream
     */
    public final int getTotalSize() {
        return totalSize;
    }

    public long getWritableLength() {
        return 4 + data.getLength();
    }

    public void write(OutputStream out) throws IOException {
        BinaryTools.writeUShort(out, type);
        BinaryTools.writeUShort(out, (int) writer.getWritableLength());
        writer.write(out);
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("TLV: type=0x");
        buffer.append(Integer.toHexString(type));
        if (data == null) {
            buffer.append(" (no data block)");
        } else {
            int len = data.getLength();
            buffer.append(", length=" + len);

            if (len > 0) {
                CharsetDecoder ascii = Charset.forName("US-ASCII").newDecoder();

                CharBuffer chars = null;
                try {
                    chars = ascii.decode(ByteBuffer.wrap(data.toByteArray()));
                } catch (CharacterCodingException e) { }

                boolean alternatevalue = false;
                if (chars != null) {
                    buffer.append(", ascii value=\"" + chars.toString() + "\"");
                    alternatevalue = true;
                }
                if (len == 2) {
                    buffer.append(", ushort value=" + getDataAsUShort());
                    alternatevalue = true;
                }
                if (len == 4) {
                    buffer.append(", uint value=" + getDataAsUInt());
                    alternatevalue = true;
                }
                if (TlvTools.isCompleteTlvChain(data)) {
                    buffer.append(", tlvchain value="
                            + TlvTools.readChain(data));
                    alternatevalue = true;
                }

                if (!alternatevalue) buffer.append(" - hex: ");
                else buffer.append(": ");
                if (false && len > 30) {
                    ByteBlock sub = data.subBlock(0, 30);
                    buffer.append(BinaryTools.describeData(sub) + "...");
                } else {
                    buffer.append(BinaryTools.describeData(data));
                }
            }
        }

        return buffer.toString();
    }
}
