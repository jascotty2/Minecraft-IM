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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Provides a read-only interface to an underlying block of data. This class
 * is useful for conserving memory (and CPU time copying memory) when many
 * objects need to access different parts of a byte array.
 * <br>
 * For example, when
 * a FLAP packet is read from a stream, a <code>ByteBlock</code> is created
 * containing the FLAP data (bytes 6 through n-6-1, where n is the FLAP data
 * length). If this is a channel-2 FLAP, then a <code>SnacPacket</code> is
 * created, which creates a <code>ByteBlock</code> representing the SNAC data,
 * or bytes 10 through n-10-1 of the FLAP data. Then, if this command is an
 * ICBM (like an IM or chat message), an <code>AbstractIcbmCommand</code>
 * subclass creates another <code>ByteBlock</code> to represent the
 * "channel-specific data," and in most cases the channel data are read as
 * a <code>TlvChain</code>, which splits that <code>ByteBlock</code> up into,
 * in the case of IM, up to five separate <code>TLV</code>'s, each containing
 * a ByteBlock that represents some range of data within that
 * <code>TlvChain</code>.
 * <br>
 * <br>
 * Now obviously it would be inefficient to be copying the majority of each IM
 * packet's data at least four times. On the other hand, it would be tedious to
 * pass an offset into the array and a length to every single method that needed
 * some part of every packet that came in. Thus, <code>ByteBlock</code> was
 * created, providing the convenience of a single object to represent data
 * with the efficiency of accessing a single underlying <code>byte[]</code>,
 * <i>and</i> with the added security of being immutable (that is, read-only).
 */
public final class ByteBlock implements Writable, Serializable {
    /**
     * A <code>ByteBlock</code> with a length of 0.
     */
    public static final ByteBlock EMPTY_BLOCK = new ByteBlock();

    /**
     * Returns a ByteBlock logically equivalent to the given byte array.
     * Calling this method is equivalent to calling {@link
     * #wrap(byte[], int, int) ByteBlock.wrap(bytes, 0,
     * bytes.length)}.
     * <br>
     * <br>
     * Note that while the <code>ByteBlock</code> class is described as
     * "immutable" because there are no public methods that can modify the
     * object's state, there is still the possibility that anyone with a
     * reference to the given <code>byte[]</code> will still be able to modify
     * its contents. Thus, <code>ByteBlock</code> is, in a sense, only as
     * immutable as you make it.
     *
     * @param bytes the data to "wrap" in the <code>ByteBlock</code>
     * @return a <code>ByteBlock</code> backed by the given array
     * @throws IllegalArgumentException if the given byte array is
     *         <code>null</code>
     *
     * @see #wrap(byte[], int, int)
     */
    public static ByteBlock wrap(byte[] bytes) throws IllegalArgumentException {
        return new ByteBlock(bytes, 0, bytes.length);
    }

    /**
     * Creates a <code>ByteBlock</code> that is simply a wrapper around the
     * data in the given array after the given index. Thus,
     * <code>ByteBlock.wrap(bytes, 50).get(0) == bytes[50]</code>, and
     * <code>ByteBlock.wrap(bytes, 50).getLength() == (bytes.length -
     * 50)</code>.
     * <br>
     * <br>
     * Calling this method is equivalent to calling {@link
     * #wrap(byte[], int, int) ByteBlock.wrap(bytes, offset, bytes.length
     * - offset)}.
     * <br>
     * <br>
     * Note that while the <code>ByteBlock</code> class is described as
     * "immutable" because there are no public methods that can modify the
     * object's state, there is still the possibility that anyone with a
     * reference to the given <code>byte[]</code> will still be able to modify
     * its contents. Thus, <code>ByteBlock</code> is, in a sense, only as
     * immutable as you make it.
     *
     * @param bytes the data that the returned <code>ByteBlock</code> will
     *        contain
     * @param offset the starting index of the data to be held in the returned
     *        <code>ByteBlock</code>
     * @return a <code>ByteBlock</code> backed by the given array after the
     *         given index
     * @throws IllegalArgumentException if the given array is <code>null</code>
     * @throws IndexOutOfBoundsException if the given offset is less than zero
     *         or greater than the given array's length
     *
     * @see #wrap(byte[], int, int)
     */
    public static ByteBlock wrap(byte[] bytes, int offset)
            throws IllegalArgumentException, IndexOutOfBoundsException {
        return new ByteBlock(bytes, offset, bytes.length - offset);
    }

    /**
     * Creates a <code>ByteBlock</code> that is simply a wrapper around the
     * specified length of data in the given array after the given index. Thus,
     * <code>ByteBlock.wrap(bytes, 50, 3).get(0) == bytes[50]</code>, and
     * <code>ByteBlock.wrap(bytes, 50, 3).getLength() == 3</code>.
     * <br>
     * <br>
     * Note that while the <code>ByteBlock</code> class is described as
     * "immutable" because there are no public methods that can modify the
     * object's state, there is still the possibility that anyone with a
     * reference to the given <code>byte[]</code> will still be able to modify
     * its contents. Thus, <code>ByteBlock</code> is, in a sense, only as
     * immutable as you make it.
     *
     * @param bytes the data that the returned <code>ByteBlock</code> will
     *        contain
     * @param offset the starting index of the data to be held in the returned
     *        <code>ByteBlock</code>
     * @param len the number of bytes after <code>index</code> to hold in the
     *        returned <code>ByteBlock</code>
     * @return a <code>ByteBlock</code> backed by the given number of bytes
     *         after the given index
     * @throws IllegalArgumentException if the given byte array is
     *         <code>null</code>
     * @throws IndexOutOfBoundsException if the given offset is less than zero
     *         or greater than the length of the given byte array, or if the
     *         given length is less than zero or greater than the length of
     *         the given array minus the given offset (that is, if <code>offset
     *         + len > bytes.length</code>)
     */
    public static ByteBlock wrap(byte[] bytes, int offset, int len)
            throws IllegalArgumentException, IndexOutOfBoundsException {
        return new ByteBlock(bytes, offset, len);
    }

    /**
     * Creates a <code>ByteBlock</code> by writing the given
     * <code>LiveWritable</code> to a byte array, then {@linkplain
     * #wrap(byte[]) wrapping} that array in a <code>ByteBlock</code>.
     * <br>
     * <br>
     * Note that if <code>writable</code> is already a <code>ByteBlock</code>,
     * no data will be copied but instead a block backed by the same array and
     * with the same start index and data length will be created and returned.
     *
     * @param writable the object to wrap in a <code>ByteBlock</code>
     * @return a <code>ByteBlock</code> containing the data written by
     *         <code>writable.write(<i>[OutputStream]</i>)</code>
     * @throws ArrayIndexOutOfBoundsException if
     *         <code>writable.getWritableLength</code> returns a number larger
     *         than can be stored in an array (<code>n >= {@link
     *         Integer#MAX_VALUE}</code>)
     */
    public static ByteBlock createByteBlock(LiveWritable writable)
            throws ArrayIndexOutOfBoundsException {
        byte[] bytes;
        int offset;
        int len;

        if (writable instanceof ByteBlock) {
            ByteBlock block = (ByteBlock) writable;

            bytes = block.bytes;
            offset = block.offset;
            len = block.len;

        } else {
            ByteArrayOutputStream out;

            if (writable instanceof Writable) {
                long writableLength = ((Writable) writable).getWritableLength();

                if (writableLength > Integer.MAX_VALUE) {
                    throw new ArrayIndexOutOfBoundsException("writable length "
                            + "of " + writable + " is " + writableLength
                            + ", must be <= Integer.MAX_VALUE ("
                            + Integer.MAX_VALUE + ")");
                }

                out = new ByteArrayOutputStream((int) writableLength);
            } else {
                out = new ByteArrayOutputStream();
            }

            try { writable.write(out); } catch (IOException impossible) { }

            bytes = out.toByteArray();

            offset = 0;
            len = bytes.length;
        }

        return wrap(bytes, offset, len);
    }

    /**
     * Creates a <code>ByteBlock</code> by concatenating the output of the given
     * list of <code>LiveWritable</code>s.
     *
     * @param writables the list of <code>LiveWritable</code>s to concatenate
     * @return a <code>ByteBlock</code> containing the data output from each
     *         of the given <code>LiveWritable</code>s, in the given order
     *
     * @throws ArrayIndexOutOfBoundsException if the total size is greater than
     *         the maximum size of an array (<code>n >= {@link
     *         Integer#MAX_VALUE}</code>)
     */
    public static ByteBlock createByteBlock(LiveWritable[] writables)
            throws ArrayIndexOutOfBoundsException {
        if (writables.length == 0) return EMPTY_BLOCK;
        else if (writables.length == 1) return createByteBlock(writables[0]);

        long ttlSize = 0;
        boolean good = true;
        for (int i = 0; i < writables.length; i++) {
            if (!(writables[i] instanceof Writable)) {
                good = false;
                break;
            }

            long len = ((Writable) writables[i]).getWritableLength();
            ttlSize += len;
        }

        if (ttlSize > Integer.MAX_VALUE) {
            throw new ArrayIndexOutOfBoundsException("sum of writable length "
                    + "of writables is " + ttlSize + ", must be <= " +
                    "Integer.MAX_VALUE (" + Integer.MAX_VALUE + ")");
        }

        ByteArrayOutputStream out;
        if (good) out = new ByteArrayOutputStream((int) ttlSize);
        else out = new ByteArrayOutputStream();

        try {
            for (int i = 0; i < writables.length; i++) {
                writables[i].write(out);
            }
        } catch (IOException impossible) { }

        return wrap(out.toByteArray());
    }

    /**
     * Creates a <code>String</code> from the data in the given block, using
     * the given charset for encoding. This is implemented for performance
     * reasons so that the data need not be copied to a second array to be
     * converted to a <code>String</code>.
     *
     * @param block the block of data to convert to a <code>String</code>
     * @param charset the charset with which to decode the given data
     * @return a <code>String</code> decoded with the given charset from the
     *         given block of data
     * @throws UnsupportedEncodingException if the given encoding is not
     *         supported by the VM
     *
     * @see String#String(byte[], int, int, String)
     */
    public static String createString(ByteBlock block, String charset)
        throws UnsupportedEncodingException {
        return new String(block.bytes, block.offset, block.len, charset);
    }

    /**
     * Returns an <code>InputStream</code> that simply reads from the given byte
     * block. Semantics of the returned <code>InputStream</code> are those of
     * <code>ByteArrayOutputStream</code>.
     *
     * @param data the block for which a stream should be created
     * @return an input stream using the given byte block as a backing buffer
     */
    public static InputStream createInputStream(ByteBlock data) {
        return new ByteArrayInputStream(data.bytes, data.offset, data.len);
    }

    /**
     * The array backing this <code>ByteBlock</code>.
     */
    private final byte[] bytes;

    /**
     * The index in the backing array that represents the first index of this
     * block.
     */
    private final int offset;

    /**
     * The number of bytes represented by this block.
     */
    private final int len;

    /**
     * A hash code generated from this byte block, or <code>0</code> if no code
     * has yet been generated. (This value is generated lazily.)
     */
    private int hashCode = 0;

    /**
     * Creates an empty <code>ByteBlock</code>.
     */
    private ByteBlock() {
        bytes = new byte[0];
        offset = 0;
        len = 0;
    }

    /**
     * Creates a new <code>ByteBlock</code> with the given backing array,
     * offset into that array, and data length.
     *
     * @param bytes the backing array for this block
     * @param offset the offset into that array that represents the first index
     *        of this block
     * @param len the length of the data represented by this block
     *
     * @throws IllegalArgumentException if the given byte array is
     *         <code>null</code>
     * @throws IndexOutOfBoundsException if <code>offset</code> or
     *         <code>len</code> are negative or if <code>offset + len >
     *         bytes.length</code>
     */
    private ByteBlock(byte[] bytes, int offset, int len)
            throws IllegalArgumentException, IndexOutOfBoundsException {
        DefensiveTools.checkNull(bytes, "bytes");
        
        if (offset < 0) {
            throw new IndexOutOfBoundsException("offset (" + offset + ") < 0");
        }
        if (len < 0) {
            throw new IndexOutOfBoundsException("length (" + len + ") < 0");
        }
        if (len + offset > bytes.length) {
            throw new IndexOutOfBoundsException("len + offset ("
                    + (len + offset) + ") > data length ("
                    + bytes.length + ")");
        }

        this.bytes = bytes;
        this.offset = offset;
        this.len = len;
    }

    /**
     * Returns the byte in this block at the given index.
     * @param index the index of the byte to return
     * @return the byte at the given index of this block
     * @throws IndexOutOfBoundsException if the given index is less than zero
     *         or greater than this block's length (<code>getLength()</code>)
     */
    public final byte get(int index) throws IndexOutOfBoundsException {
        if (index < 0) {
            throw new IndexOutOfBoundsException("index (" + index
                    + ") must be >= 0");
        }
        if (index >= len) {
            throw new IndexOutOfBoundsException("index (" + index
                    + ") must be less than length (" + len + ")");
        }
        return bytes[offset + index];
    }

    /**
     * Returns the length of this byte block.
     * @return the length of this block
     */
    public int getLength() {
        return len;
    }

    /**
     * Provided in order to satisfy the requirements of <code>Writable</code>;
     * returns the length of this byte block.
     * @return the length of this byte block
     */
    public long getWritableLength() {
        return getLength();
    }

    /**
     * Writes the contents of this <code>ByteBlock</code> to the given stream.
     *
     * @param stream the stream to which to write this block
     * @throws IOException if an I/O error occurs
     */
    public void write(OutputStream stream) throws IOException {
        DefensiveTools.checkNull(stream, "stream");

        stream.write(bytes, offset, len);
    }

    /**
     * Allocates a new byte array containing a copy of the contents of this byte
     * block.
     * @return a newly allocated byte array containing a copy of this byte block
     */
    public byte[] toByteArray() {
        if (offset == 0 && len == bytes.length) {
            // this is slightly faster
            return (byte[]) bytes.clone();
        } else {
            byte[] array = new byte[len];
            System.arraycopy(bytes, offset, array, 0, len);
            return array;
        }
    }

    /**
     * Returns a new <code>ByteBlock</code> containing all bytes in this block
     * from <code>offset</code> to the end of this block. Calling this method
     * is the equivalent of calling {@link #subBlock(int, int)
     * block.subBlock(offset.getLength() - offset)}.
     * <br>
     * <br>
     * The new block will exist such that <code>block.subBlock(5).get(0) ==
     * block.get(5)</code>, and <code>block.subBlock(5).getLength() ==
     * (block.getLength() - 5)</code>.
     * <br>
     * <br>
     * Note that this method does <i>not</i> modify this block, but merely
     * creates a new one with the same backing array and a new offset and
     * length.
     *
     * @param offset the first index of the data to hold in the new
     *        <code>ByteBlock</code>
     * @return a new <code>ByteBlock</code> holding all of the bytes in this
     *         block starting at the given offset
     *
     * @throws IndexOutOfBoundsException
     */
    public ByteBlock subBlock(int offset) throws IndexOutOfBoundsException {
        return subBlock(offset, len - offset);
    }

    /**
     * Returns a new <code>ByteBlock</code> containing the first
     * <code>len</code> bytes in this block starting at index
     * <code>offset</code>.
     * <br>
     * <br>
     * The new block will exist such that <code>block.subBlock(5, 2).get(0) ==
     * block.get(5)</code>, and <code>block.subBlock(5, 2).getLength() ==
     * 2</code>.
     * <br>
     * <br>
     * Note that no copying of data is done during this process, so feel free
     * to use this method freely in a free manner.
     *
     * @param offset the first index of the data to hold in the new
     *        <code>ByteBlock</code>
     * @param len the number of bytes that the new <code>ByteBlock</code> shall
     *        hold
     * @return a new <code>ByteBlock</code> containing the specified number of
     *         bytes in this block starting at the specified index
     * @throws IndexOutOfBoundsException if the specified offset or length is
     *         negative or if <code>offset + length > getLength()</code>
     */
    public ByteBlock subBlock(int offset, int len)
            throws IndexOutOfBoundsException {
        if (offset < 0) {
            throw new IndexOutOfBoundsException("offset (" + offset + ") < 0");
        }
        if (len < 0) {
            throw new IndexOutOfBoundsException("len (" + len + ") < 0");
        }
        if (len + offset > this.len) {
            throw new IndexOutOfBoundsException("length + offset ("
                    + (len + offset) + ") > length (" + this.len + ")");
        }

        return wrap(bytes, this.offset + offset, len);
    }

    /**
     * Returns the offset into the backing array that represents the first index
     * of this block. Useful only for comparing two blocks that use the
     * <i>same</i> backing array. For example, if <code>b = a.subBlock(50,
     * 2).subBlock(20)</code>, then <code>b.getOffset() - a.getOffset() ==
     * 70</code>.
     *
     * @return the offset into the backing array that represents the first index
     *         of this block
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Writes this object out, writing only the portion of the array being
     * represented and writing the offset into it as 0.
     *
     * @param out the stream to which to write this object
     * @throws IOException if an I/O exception occurs
     */
    private void writeObject(ObjectOutputStream out)
            throws IOException {
        // hack the object output stream so we can keep our fields final
        ObjectOutputStream.PutField fields = out.putFields();

        // when it is read back in, it will read only the portion of the backing
        // array that we wrote out, so the offset into it should be zero
        fields.put("offset", 0);
        fields.put("len", len);

        // only write the bytes that this block represents -- not the entire
        // backing array, *unless* this block represents the entire backing
        // array.
        byte[] array = (offset == 0 && len == bytes.length)
                    ? bytes
                    : toByteArray();
        fields.put("bytes", array);

        out.writeFields();
    }

    /*
    private void writeObject(ObjectOutputStream out)
            throws IOException {
        out.writeInt(len);
        out.write(bytes, offset, len);
    }
    
    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        offset = 0;
        len = in.readInt();
        bytes = new byte[len];
        in.read(bytes, 0, len);
    }
*/

    /**
     * Copies the data in this block to the given array at the given position.
     *
     * @param dest the array to which to copy this block
     * @param destOffset the offset into the given array at which to begin
     *        copying this block
     */
    public void copyTo(byte[] dest, int destOffset) {
        System.arraycopy(bytes, offset, dest, destOffset, len);
    }


    /**
     * Returns <code>true</code> if this and the given object represent the same
     * data, byte for byte; <code>false</code> otherwise.
     *
     * @param o another <code>ByteBlock</code> to compare this block to
     * @return <code>true</code> if this block represents the same data as the
     *         given block
     */
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ByteBlock)) return false;

        final ByteBlock other = (ByteBlock) o;
        if (len != other.len) return false;

        // if a hash code has been computed, we can test that first
        int thiscode = hashCode;
        int othercode = other.hashCode;

        if ((thiscode != 0 && othercode != 0) && thiscode != othercode) {
            return false;
        }

        // if we know the two blocks represent all the data in their backing
        // arrays, we can try to use this, which is probably faster.
        if (offset == 0 && other.offset == 0 && len == bytes.length) {
            return Arrays.equals(bytes, other.bytes);
        }

        // oh well. we tried.
        for (int i = 0; i < len; i++) {
            if (get(i) != other.get(i)) return false;
        }

        return true;
    }

    public final int hashCode() {
        int current = hashCode;
        if (current != 0) return current;
        int code = 0;
        int delta = Math.max(1, len/128);
        int lim = offset + len;
        for (int i = offset; i < lim; i += delta) {
            code = code * 29 + bytes[i];
        }

        // make sure it's not 0, which is a special code meaning that the
        // hashcode hasn't been computed yet
        if (code == 0) code = 1;

        hashCode = code;

        return code;
    }

    public String toString() {
        return BinaryTools.describeData(this);
    }
}
