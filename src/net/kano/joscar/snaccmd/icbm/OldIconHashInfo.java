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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

/**
 * A data structure containing a set of information about a buddy icon. This is
 * called "old" icon hash data because of the new buddy icon system (the
 * {@linkplain net.kano.joscar.snaccmd.icon <code>0x10</code> service}).
 * <br>
 * <br>
 * Note that this data structure is stored in <i>two separate formats</i> in the
 * OSCAR protocol. The first is when "advertising" one's buddy icon in an
 * IM and the second is when sending one's icon to another user in a rendezvous
 * packet. Accordingly, are two pairs of <code>read<i>*</i></code> and
 * <code>write<i>*</i></code> methods for reading from and writing to these two
 * formats.
 */
public final class OldIconHashInfo {
    /**
     * Returns an icon sum of all of the data in the given stream. Data are read
     * from until the end of the given stream has bene reached.
     *
     * @param in a stream containing a buddy icon
     * @return an "old icon sum" of the data in the given stream
     *
     * @throws IOException if an I/O exception occurs
     */
    public static int computeIconSum(InputStream in) throws IOException {
        long sum = 0;

        int lastByte = -1;
        for (;;) {
            int a = in.read();
            if (a == -1) {
                break;
            }
            int b = in.read();
            if (b == -1) {
                lastByte = a;
                break;
            }

            sum += ((b & 0xff) << 8) + (a & 0xff);
        }

        if (lastByte != -1) {
            sum += lastByte;
        }

        sum = ((sum & 0xffff0000L) >> 16) + (sum & 0x0000ffffL);

        return (int) sum;
    }

    /**
     * Returns an "old icon sum" from the given buddy icon data.
     *
     * @param data the raw buddy icon data
     * @return an icon sum suitable for use in an <code>OldIconHashData</code>
     */
    public static int computeIconSum(ByteBlock data) {
        DefensiveTools.checkNull(data, "data");

        try {
            return computeIconSum(ByteBlock.createInputStream(data));
        } catch (IOException impossible) { return -1; }
    }

    /**
     * Returns an icon hash data block read from the beginning of the given
     * block of binary data. Returns <code>null</code> if the given block does
     * not contain a valid icon hash data block. Note that this method reads the
     * data using the format normally used in sending one's icon hash in an
     * IM.
     *
     * @param block the block of data from which to read
     * @return an icon hash data block read from the given block, or
     *         <code>null</code> if none could be read
     */
    public static OldIconHashInfo readIconHashFromImTlvData(ByteBlock block) {
        DefensiveTools.checkNull(block, "block");

        if (block.getLength() < 12) return null;

        long size = BinaryTools.getUInt(block, 0);
        int sum = BinaryTools.getUShort(block, 6);
        long stamp = BinaryTools.getUInt(block, 8);

        return new OldIconHashInfo(size, sum, stamp, 12);
    }

    /**
     * Returns an icon hash data block read from the beginning of the given
     * block of binary data. Returns <code>null</code> if the given block does
     * not contain a valid icon hash data block. Note that this method reads the
     * data using the format normally used in sending one's icon hash along with
     * one's icon in a {@link net.kano.joscar.rvcmd.icon.SendBuddyIconRvCmd}.
     *
     * @param block the block of data from which to read
     * @return an icon hash data block read from the given block, or
     *         <code>null</code> if none could be read
     */
    public static OldIconHashInfo readIconHashFromRvData(ByteBlock block) {
        DefensiveTools.checkNull(block, "block");

        if (block.getLength() < 12) return null;

        int sum = BinaryTools.getUShort(block, 2);
        long size = BinaryTools.getUInt(block, 4);
        long stamp = BinaryTools.getUInt(block, 8);

        return new OldIconHashInfo(size, sum, stamp, 12);
    }

    /** The file size of the icon. */
    private final long size;
    /** The icon's "icon sum." */
    private final int sum;
    /** The icon's timestamp. */
    private final long timestamp;
    /** The total size of this block, as read from an incoming stream. */
    private final int hashBlockSize;

    /**
     * Creates an old icon hash block from the contents, size, and
     * last-modified time of the given file.
     *
     * @param file a buddy icon file
     *
     * @throws FileNotFoundException if the given file does not exist
     * @throws IOException if an I/O error occurs (i.e., if the file cannot be
     *         read)
     */
    public OldIconHashInfo(File file)
            throws FileNotFoundException, IOException {
        DefensiveTools.checkNull(file, "file");

        size = file.length();
        timestamp = file.lastModified();

        FileInputStream fin = new FileInputStream(file);
        sum = computeIconSum(fin);
        fin.close();

        hashBlockSize = -1;
    }

    /**
     * Creates a new icon hash data object with the given properties.
     *
     * @param size the file size of the icon
     * @param sum a {@linkplain #computeIconSum hash} of the icon data
     * @param timestamp the time at which the icon was modified last, in seconds
     *        since the unix epoch
     * @param blockSize the total size of this object, in bytes, as read from an
     *        incoming block of binary data
     */
    protected OldIconHashInfo(long size, int sum, long timestamp,
            int blockSize) {
        DefensiveTools.checkRange(size, "size", 0);
        DefensiveTools.checkRange(sum, "sum", 0);
        DefensiveTools.checkRange(timestamp, "timestamp", 0);
        DefensiveTools.checkRange(blockSize, "blockSize", -1);

        this.size = size;
        this.sum = sum;
        this.timestamp = timestamp;
        this.hashBlockSize = blockSize;
    }

    /**
     * Creates a new icon hash data object with the given properties.
     *
     * @param size the file size of the icon
     * @param sum a {@linkplain #computeIconSum hash} of the icon data
     * @param timestamp the time at which the icon was modified last, in seconds
     *        since the unix epoch
     */
    public OldIconHashInfo(long size, int sum, long timestamp) {
        this(size, sum, timestamp, -1);
    }

    /**
     * Returns the file size of the icon, as sent in this object.
     *
     * @return the icon's size, in bytes
     */
    public final long getIconSize() {
        return size;
    }

    /**
     * Returns an {@linkplain #computeIconSum "icon sum"} of the icon data,
     * as sent in this object.
     *
     * @return the icon's "icon sum"
     */
    public final int getSum() {
        return sum;
    }

    /**
     * Returns the time at which the icon was modified, in seconds since the
     * unix epoch, as sent in this object.
     *
     * @return the time at which this icon was modified
     */
    public final long getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the total size of the data structure from which this icon hash
     * information object was read, or <code>-1</code> if this block was not
     * read from an incoming block of binary data.
     *
     * @return the total size, in bytes, of the data strcture from which this
     *         icon hash information object was read, or <code>-1</code> if this
     *         object was not read from an incoming block of binary data
     */
    public int getHashBlockSize() { return hashBlockSize; }

    /**
     * Writes this icon hash block to the given stream in the format normally
     * used in advertising one's buddy icon in an {@linkplain SendImIcbm IM}.
     *
     * @param out the stream to which to write this block
     *
     * @throws IOException if an I/O error occurs
     */
    public void writeToImTlv(OutputStream out) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream(12);
        BinaryTools.writeUInt(bout, size);
        BinaryTools.writeUShort(bout, 0x0001);
        BinaryTools.writeUShort(bout, sum);
        BinaryTools.writeUInt(bout, timestamp);

        bout.writeTo(out);
    }

    /**
     * Returns the number of bytes that would be or have been written with a
     * call to {@link #writeToImTlv(java.io.OutputStream) writeToImTlv}.
     *
     * @return the length, in bytes, of the icon hash data block format used in
     *         advertising one's buddy icon in an IM
     */
    public int getImTlvFormatLength() {
        return 12;
    }

    /**
     * Writes this icon hash block to the given stream in the format normally
     * used in sending one's buddy icon to a buddy in a {@link
     * net.kano.joscar.rvcmd.icon.SendBuddyIconRvCmd}.
     *
     * @param out the stream to which to write this block
     *
     * @throws IOException if an I/O error occurs
     */
    public void writeToRvData(OutputStream out) throws IOException {
        BinaryTools.writeUShort(out, 0);
        BinaryTools.writeUShort(out, sum);
        BinaryTools.writeUInt(out, size);
        BinaryTools.writeUInt(out, timestamp);
    }

    /**
     * Returns the number of bytes that would be or have been written with a
     * call to {@link #writeToRvData(java.io.OutputStream) writeToRvData}.
     *
     * @return the length, in bytes, of the icon hash data block format used in
     *         sending one's buddy icon in a rendezvous packet
     */
    public int getRvDataFormatLength() {
        return 12;
    }

    public String toString() {
        return "OldIconHashData: size=" + size + " bytes, sum=" + sum
                + ", lastmod=" + new Date(timestamp * 1000);
    }
}
