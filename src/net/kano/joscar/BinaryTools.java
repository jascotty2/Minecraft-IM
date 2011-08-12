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
 *  File created by keith @ Feb 14, 2003
 *
 */

package net.kano.joscar;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.UnknownHostException;

/**
 * Provides a set of methods for converting binary data sent over an OSCAR
 * connection to various logical structures.
 *<br>
 * <br>
 * Note that for all types such that <code>get<i>[type]</i></code> and
 * <code>write<i>[type]</i></code> exist, the two methods are inverses:
 * calling <code>getLong</code> on a block written by <codE>writeLong</code>
 * will return the original value written.
 * <br>
 * <br>
 * The methods in this class attempt to be very robust: in the cases of the
 * four-, two-, and one-byte unsigned integers (as in {@link #writeUShort}),
 * if the given value is too large to fit into that size a block, it will be
 * wrapped (for example, <code>writeUShort(out, 65537)</code> would be written
 * to the stream as if <code>writeUShort(out, 1)</code> had been called). See
 * {@link #UINT_MAX}, {@link #USHORT_MAX}, and {@link #UBYTE_MAX} for the
 * maximum values such methods can take without wrapping. Note that those values
 * are <b>not</b> appropriate for the right-hand side of of a modulo operation
 * (like <code>65537 % BinaryTools.USHORT_MAX</code>), as these are
 * <b>maxima</b>. (An appropriate operation, if one were necessary, would be
 * <code>65537 % (BinaryTools.USHORT_MAX + 1)</code>.)
 * <br>
 * <br>
 * Another area in which the methods attempt to be robust is that except for
 * {@link #getLong(ByteBlock, int) getLong}, none of these methods declare any
 * exceptions. If a <code>get<i>[type]</i>(ByteBlock, int)</code> method is
 * passed a byte block that is too small, it simply returns <code>-1</code>
 * (this is okay because these methods only return unsigned values).
 * <br>
 * <br>
 * Lastly, it is important to note that all numbers transferred over OSCAR in
 * <i>unsigned</i> format. Why do <code>getLong</code> and
 * <code>writeLong</code> exist, you may ask, when they work with <i>signed</i>
 * <code>long</code>s? Let me start by saying that nowhere in the OSCAR protocol
 * is an eight-byte integer used. Why do these methods exist at all?, you may
 * now ask. The answer is simple: IM and Rendezvous ID's are eight bytes,
 * and are effectively represented as Java's <code>long</code>. Whether these
 * values are read as signed or unsigned matters not, as the <code>long</code>
 * is only an internal representation just as a <code>ByteBlock</code> is an
 * internal representation of a block of bytes. I hope that explanation was
 * clear enough.
 *
 * @see MiscTools
 * @see net.kano.joscar.OscarTools
 */
public final class BinaryTools {
    /**
     * Represents the largest value a four-byte unsigned integer can have. All
     * numbers sent over OSCAR are unsigned.
     */
    public static final long UINT_MAX = 4294967295L;

    /**
     * Represents the largest value a two-byte unsigned integer can have. All
     * numbers sent over OSCAR are unsigned.
     */
    public static final int USHORT_MAX = 65535;

    /**
     * Represents the largest value a one-byte unsigned integer can have. All
     * numbers sent over OSCAR are unsigned.
     */
    public static final short UBYTE_MAX = 255;

    /**
     * This is never called, and ensures an instance of <code>BinaryTools</code>
     * cannot exist.
     */
    private BinaryTools() { }

    /**
     * Returns the <i>signed</i> <code>long</code> stored in the given data
     * block. The returned <code>long</code> represents the first eight bytes
     * after index <code>pos</code> of <code>data</code>; that is, the byte at
     * index <code>pos</code> will be the first byte read, and index
     * <code>pos+7</code> the last.
     *
     * @param data the block of data to read from
     * @param pos the starting index of <code>data</code> to read from
     * @return a signed <code>long</code> representing the value stored in the
     *         first eight bytes of the given data block after the given
     *         position
     * @throws ArrayIndexOutOfBoundsException if there are fewer than eight
     *         bytes in <code>data</code>
     *
     * @see #writeLong
     * @see #getLong(long)
     */
    public static long getLong(final ByteBlock data, int pos)
            throws ArrayIndexOutOfBoundsException {
        long num = 0;

        for (int i = pos, end = i + 8; i < end; i++) {
            final int offset = (end - i - 1) * 8;
            num |= (((long) data.get(i)) & 0xffL) << offset;
        }

        return num;
    }

    /**
     * Returns the unsigned integer stored in the given data block. The
     * returned integer is extracted from the first four bytes of the given
     * block, starting at index <code>pos</code>. If there are fewer than four
     * bytes at <code>pos</code>, <code>-1</code> is returned.
     * <br>
     * <br>
     * Note that this is returned
     * as a <code>long</code> because all values of an unsigned four-byte
     * integer cannot be stored in a four-byte signed integer, Java's
     * <code>int</code>.
     *
     * @param data the data block from which to read
     * @param pos the starting index of <code>data</code> to read from
     * @return the value of the unsigned four-byte integer stored in the given
     *         block at the given index, or <code>-1</code> if fewer than four
     *         bytes are present in the block
     *
     * @see #writeUInt
     * @see #getUInt(long)
     */
    public static long getUInt(final ByteBlock data, final int pos) {
        if (data.getLength() - pos < 4) return -1;

        return (((long) data.get(pos) & 0xffL) << 24)
                | (((long) data.get(pos+1) & 0xffL) << 16)
                | (((long) data.get(pos+2) & 0xffL) << 8)
                | ((long) data.get(pos+3) & 0xffL);
    }

    /**
     * Returns an unsigned two-byte integer stored in the given block. The
     * returned integer is extracted from the first two bytes of the given
     * block, starting at index <code>pos</code>. If there are fewer than two
     * bytes at <code>pos</code>, <code>-1</code> is returned.
     * <br>
     * <br>
     * Note that this is returned as an <code>int</code> because all values of
     * an unsigned two-byte integer cannot be stored in a signed short, Java's
     * <code>short</code>.
     *
     * @param data the data block from which to read
     * @param pos the starting index of <code>data</code> to read from
     * @return the value of the two-byte integer stored at the given index of
     *         the given block, or <code>-1</code> if fewer than two bytes exist
     *         at that index
     *
     * @see #writeUShort
     * @see #getUShort(int)
     */
    public static int getUShort(final ByteBlock data, final int pos) {
        if (data.getLength() - pos < 2) return -1;

        return ((data.get(pos) & 0xff) << 8) | (data.get(pos+1) & 0xff);
    }

    /**
     * Returns an unsigned one-byte integer stored in the given block. The
     * returned integer is extracted from the byte of the given block at index
     * <code>pos</code>. If there is no byte at <code>pos</code>,
     * <code>-1</code> is returned.
     * <br>
     * <br>
     * Note that this is returned as a <code>short</code> because all values of
     * an unsigned one-byte integer cannot be stored in a signed byte, Java's
     * <code>byte</code>.
     *
     * @param data the data block to read from
     * @param pos the index in <code>data</code> of the byte to read
     * @return the value of the single-byte integer stored at the given index
     *         of the given block, or <code>-1</code> if there is no byte at
     *         that index (that is, if <code>data.getLength() <= pos</code>)
     *
     * @see #writeUByte
     * @see #getUByte(int)
     */
    public static short getUByte(final ByteBlock data, final int pos) {
        if (data.getLength() - pos < 1) return -1;

        return (short) (data.get(pos) & 0xff);
    }

    /**
     * Returns an IP address stored in the given byte block. The returned IP
     * address is stored in the first four bytes of the given block, starting
     * at <code>pos</code>. If fewer than four bytes exist at the given
     * position, <code>null</code> is returned.
     * <br>
     * <br>
     * This merely duplicates the behavior of {@link
     * Inet4Address#getByAddress(byte[])} (that is, it calls it :) and is only
     * provided for convenience.
     *
     * @param data the data block to read from
     * @param pos the starting index of the IP address block in
     *            <code>data</code>
     * @return the IP address stored in the first four bytes of the given block
     *         starting at the given index, or <code>null</code> if fewer than
     *         four bytes exist at the given index
     *
     * @see java.net.Inet4Address#getByAddress(byte[])
     * @see Inet4Address#getAddress()
     */
    public static Inet4Address getIPFromBytes(final ByteBlock data,
            final int pos) {
        if (data.getLength() - pos < 4) return null;

        // copy the IP bytes over
        final byte[] address = data.subBlock(pos, 4).toByteArray();

        // find the IP
        Inet4Address ip = null;
        try {
            ip = (Inet4Address) Inet4Address.getByAddress(address);
        } catch (UnknownHostException e) {/* won't happen */}

        // return it
        return ip;
    }

    /**
     * Returns a string describing each byte in the given block in generic
     * unsigned hexadecimal dump notation. For example, "00 2f 8a 9b" would be
     * returned if <code>data</code> contained <code>{ 0, 138, -118, -101
     * }</code>.
     *
     * @param data the data block to describe
     * @return a "hex dump" of the given block
     */
    public static String describeData(final ByteBlock data) {
        if (data == null) return null;

        final StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < data.getLength(); i++) {
            final byte b = data.get(i);
            if (i != 0) buffer.append(' ');
            String str = Integer.toHexString(((int) b) & 0xff);
            if (str.length() == 1) buffer.append('0');
            buffer.append(str);
        }

        return buffer.toString();
    }


    /**
     * Writes the given <i>signed</i> <code>long</code> to the given stream.
     * The value is written as an eight-byte <i>signed</i> integer.
     *
     * @param out the stream to write to
     * @param number the value to write to the stream in binary format
     * @throws IOException if an I/O error occurs
     *
     * @see #getLong(long)
     * @see #getLong(ByteBlock, int)
     */
    public static void writeLong(final OutputStream out, final long number)
            throws IOException {
        out.write(getLong(number));
    }


    /**
     * Writes a block of four bytes representing the given unsigned value to
     * the given stream.
     *
     * @param out the stream to write to
     * @param number the value to write to the stream in binary format
     * @throws IOException if an I/O error occurs
     *
     * @see #getUInt(long)
     * @see #getUInt(ByteBlock, int)
     */
    public static void writeUInt(final OutputStream out, final long number)
            throws IOException {
        out.write(getUInt(number));
    }

    /**
     * Writes a block of two bytes representing the given unsigned value to the
     * given stream.
     *
     * @param out the stream to write to
     * @param number the value to write to the stream in binary format
     * @throws IOException if an I/O error occurs
     *
     * @see #getUShort(int)
     * @see #getUShort(ByteBlock, int)
     */
    public static void writeUShort(final OutputStream out, final int number)
            throws IOException {
        out.write(getUShort(number));
    }

    /**
     * Writes a single (unsigned) byte representing the given unsigned value
     * to the given stream.
     *
     * @param out the stream to write to
     * @param number the value to write to the stream in binary format
     * @throws IOException if an I/O error occurs
     *
     * @see #getUByte(int)
     * @see #getUByte(ByteBlock, int)
     */
    public static void writeUByte(final OutputStream out, final int number)
            throws IOException {
        out.write(getUByte(number));
    }

    /**
     * Returns a block of eight bytes representing the given <i>signed</i>
     * <code>long</code> in binary format.
     *
     * @param number the value to be written to the returned array
     * @return an eight-byte block representing the given <i>signed</i> value
     *
     * @see #writeLong
     * @see #getLong(ByteBlock, int)
     */
    public static byte[] getLong(final long number) {
        final byte[] data = new byte[8];

        for (int i = 0; i < 8; i++){
            final int offset = (7 - i) * 8;
            data[i] = (byte) ((number >> offset) & 0xff);
        }

        return data;
    }

    /**
     * Returns a four-byte block representing the given unsigned value in binary
     * format.
     *
     * @param number the value to be written to the returned array
     * @return a four-byte binary representation of the given unsigned value
     *
     * @see #writeUInt
     * @see #getUInt(ByteBlock, int)
     */
    public static byte[] getUInt(final long number) {
        return new byte[] {
            (byte) ((number >> 24) & 0xff),
            (byte) ((number >> 16) & 0xff),
            (byte) ((number >> 8) & 0xff),
            (byte) (number & 0xff)
        };
    }

    /**
     * Returns a two-byte block representing the given unsigned value in binary
     * format.
     *
     * @param number the value to be written to the returned array
     * @return a two-byte binary representation of the given unsigned value
     *
     * @see #writeUShort
     * @see #getUShort(ByteBlock, int)
     */
    public static byte[] getUShort(final int number) {
        return new byte[] {
            (byte) ((number >> 8) & 0xff),
            (byte) (number & 0xff)
        };
    }

    /**
     * Returns a single-byte block representing the given unsigned value in
     * binary format.
     *
     * @param number the value to be written to the returned array
     * @return a one-byte binary representation of the given unsigned value
     *
     * @see #writeUByte
     * @see #getUByte(ByteBlock, int)
     */
    public static byte[] getUByte(final int number) {
        return new byte[] { (byte) (number & 0xff) };
    }


    /**
     * Returns a block of data representing the given US-ASCII string. There
     * is no need to ensure the given string is actually pure US-ASCII, as
     * the charset encoder will encode any string as US-ASCII, replacing
     * non-ASCII characters with '?'.
     * <br>
     * <br>
     * Many values sent over OSCAR connections are implicitly US-ASCII; for
     * those unfamiliar with Unicode and character encodings, writing the
     * returned byte block to an output stream is essentially the equivalent of
     * simply writing a <code>char[]</code> or <code>char*</code> in C/C++ to a
     * stream.
     *
     * @param string the string to be encoded in the returned array
     * @return the given string converted to US-ASCII
     *
     * @see #getAsciiString
     * @see String#getBytes(String)
     */
    public static byte[] getAsciiBytes(String string) {
        try {
            return string.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException impossible) { return null; }
    }

    /**
     * Returns a <code>String</code> representing the given block of ASCII
     * characters.
     * <br>
     * <br>
     * Many values sent over OSCAR connections are implicitly US-ASCII; for
     * those unfamiliar with Unicode and character encodings, calling this
     * method on data read from a stream is essentially the equivalent of
     * reading a <code>char[]</code> or <code>char*</code> in C/C++ from a
     * stream.
     *
     * @param data the block of ASCII characters
     * @return a String containing the (US-ASCII) characters in the given block
     *
     * @see #getAsciiBytes
     * @see String#String(byte[], String)
     */
    public static String getAsciiString(ByteBlock data) {
        try {
            return ByteBlock.createString(data, "US-ASCII");
        } catch (UnsupportedEncodingException impossible) { return null; }
    }

    /**
     * Reads a null-padded ASCII string from the start of the given block. The
     * returned <code>StringBlock</code> contains an ASCII string formed from
     * all of the bytes in the given block before the first null byte
     * (<code>0x00</code>) or the end of the block. Note that the
     * <code>totalSize</code> field of the returned <code>StringBlock</code> is
     * the length of the string <i>without</i> the null byte. This is to avoid
     * confusion when reading strings padded with more than one null byte or
     * those not padded at all (that is, when the block consists only of an
     * ASCII string with no null bytes).
     *
     * @param block a block of data containing an ASCII string followed by zero
     *        or more null (<code>0x00</code>) bytes
     * @return a <code>StringBlock</code> containing the string extracted from
     *         the given block and the size of the string <i>without the null
     *         byte(s)</i> extracted, in bytes
     *
     * @see #getAsciiString
     */
    public static StringBlock getNullPadded(ByteBlock block) {
        int firstNull;
        for (firstNull = 0; firstNull < block.getLength();
             firstNull++) {
            if (block.get(firstNull) == 0) break;
        }
        ByteBlock strBlock = block.subBlock(0, firstNull);

        return new StringBlock(getAsciiString(strBlock), strBlock.getLength());
    }

    /**
     * Writes the given block to the given stream, padded to a given length with
     * null bytes (<code>0x00</code>). This method is guaranteed to write
     * exactly <code>len</code> bytes to the given stream, only writing the
     * first <code>len</code> bytes of the given block (and no null bytes) if
     * <code>block.getLength() > len</code>.
     *
     * @param out a stream to which to write
     * @param block the block to write to the given stream
     * @param len the total number of bytes to write to the given stream
     *
     * @throws IOException if an I/O error occurs
     */
    public static void writeNullPadded(OutputStream out, ByteBlock block,
            int len) throws IOException {
        if (block.getLength() <= len) {
            block.write(out);
            out.write(new byte[len - block.getLength()]);
        } else {
            block.subBlock(0, len).write(out);
        }
    }
}
