/*
 *  Copyright (c) 2003, The Joust Project
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
 *  File created by keith @ Sep 23, 2003
 *
 */

package net.kano.joscar.snaccmd;

import net.kano.joscar.ByteBlock;
import net.kano.joscar.BinaryTools;
import net.kano.joscar.Writable;
import net.kano.joscar.DefensiveTools;

import java.util.LinkedList;
import java.util.List;
import java.io.OutputStream;
import java.io.IOException;

/**
 * A data structure used to hold {@link ExtraInfoBlock}s and some sort of code.
 * As of this writing, this structure is only used in {@link
 * net.kano.joscar.snaccmd.conn.ExtraInfoAck} commands, and any advantages to
 * sending <code>ExtraInfoBlock</code>s directly is unknown.
 *
 * @see net.kano.joscar.snaccmd.conn.ExtraInfoAck
 */
public class ExtraInfoBlockHolder implements Writable {
    /** The code value sent by default by the AIM server. */
    public static final int CODE_DEFAULT = 0x07;

    /**
     * Reads an <code>ExtraInfoBlockHolder</code> from the given block of binary
     * data. The total number of bytes read can be obtained by calling the
     * {@link #getTotalSize()} method of the returned object. Note that if no
     * valid extra info block holder object can be read, this method will return
     * <code>null</code>.
     *
     * @param block a block of data containing an extra info block holder object
     * @return an extra info block holder object read from the given block of
     *         binary data, or <code>null</code> if none could be read
     */
    public static final ExtraInfoBlockHolder readBlockHolder(ByteBlock block) {
        DefensiveTools.checkNull(block, "block");

        int origOffset = block.getOffset();

        ExtraInfoBlock first = ExtraInfoBlock.readExtraInfoBlock(block);
        if (first == null) return null;

        block = block.subBlock(first.getTotalSize());
        if (block.getLength() < 1) return null;
        int code = BinaryTools.getUByte(block, 0);

        block = block.subBlock(1);
        ExtraInfoBlock second = ExtraInfoBlock.readExtraInfoBlock(block);
        if (second == null) return null;

        int size = (block.getOffset() + second.getTotalSize()) - origOffset;
        return new ExtraInfoBlockHolder(first, code, second, size);
    }

    /**
     * Reads a series of <code>ExtraInfoBlockHolder</code>s from the given block
     * of binary data. Note that this method will never return
     * <code>null</code>; if no extra info block holder objects can be read from
     * the given block of data, an empty array is returned. The total number of
     * bytes read can be obtained by adding the results of calling {@link
     * #getTotalSize()} on each of the elements of the returned array.
     *
     * @param block a block of data containing a sequence of zero or more
     *        <code>ExtraInfoBlock</code>s
     * @return a list of the <code>ExtraInfoBlock</code> objects read from the
     *         given block of binary data
     */
    public static final ExtraInfoBlockHolder[] readBlockHolders(
            ByteBlock block) {
        List list = new LinkedList();
        for (;;) {
            ExtraInfoBlockHolder bh = readBlockHolder(block);
            if (bh == null) break;
            list.add(bh);
            block = block.subBlock(bh.getTotalSize());
        }

        return (ExtraInfoBlockHolder[])
                list.toArray(new ExtraInfoBlockHolder[0]);
    }

    /** The first extra info block contained in this block holder. */
    private final ExtraInfoBlock first;
    /** The code contained in this block holder. */
    private final int code;
    /** The second extra info block contained in this block holder. */
    private final ExtraInfoBlock second;
    /**
     * The total number of bytes read to create this object, if read from an
     * incoming block of binary data.
     */
    private final int totalSize;

    /**
     * Creates a new <code>ExtraInfoBlockHolder</code> with the given
     * properties.
     *
     * @param first the first <code>ExtraInfoBlock</code> to hold in this block
     *        holder
     * @param code the numeric code to hold in this block holder
     * @param second the second <code>ExtraInfoBlock</code> to hold in this
     *        block holder
     * @param totalSize the total number of bytes read to create this object,
     *        if read from an incoming block of binary data
     */
    protected ExtraInfoBlockHolder(ExtraInfoBlock first, int code,
            ExtraInfoBlock second, int totalSize) {
        DefensiveTools.checkRange(code, "code", -1);
        DefensiveTools.checkRange(totalSize, "totalSize", -1);

        this.first = first;
        this.code = code;
        this.second = second;
        this.totalSize = totalSize;
    }

    /**
     * Creates an extra info block holder whose blocks are both set to the given
     * block and with a code value of {@link #CODE_DEFAULT}. As of this writing,
     * this is the only format used.
     *
     * @param both the extra info block to use as both the first and second
     *        blocks
     */
    public ExtraInfoBlockHolder(ExtraInfoBlock both) {
        this(both, CODE_DEFAULT, both);
    }

    /**
     * Creates a new <code>ExtraInfoBlockHolder</code> with the given
     * properties.
     *
     * @param first the first <code>ExtraInfoBlock</code> to hold in this block
     *        holder
     * @param code the numeric code to hold in this block holder, like {@link
     *        #CODE_DEFAULT}
     * @param second the second <code>ExtraInfoBlock</code> to hold in this
     *        block holder
     */
    public ExtraInfoBlockHolder(ExtraInfoBlock first, int code,
            ExtraInfoBlock second) {
        this(first, code, second, -1);
    }

    /**
     * Returns the first extra info block contained in this extra info block
     * holder.
     *
     * @return the first extra info block
     */
    public final ExtraInfoBlock getFirstBlock() { return first; }

    /**
     * The numeric code contained in this extra info block holder. Note that
     * this is normally {@link #CODE_DEFAULT}.
     *
     * @return the numeric code contained in this extra info block holder
     */
    public final int getCode() { return code; }

    /**
     * Returns the second extra info block contained in this extra info block
     * holder.
     *
     * @return the second extra info block
     */
    public final ExtraInfoBlock getSecondBlock() { return second; }

    /**
     * Returns the number of bytes read to create this object, if read from an
     * incoming block of binary data with {@link #readBlockHolder(ByteBlock)
     * readBlockHolder}.
     *
     * @return the total number of bytes read to create this object
     */
    public final int getTotalSize() { return totalSize; }

    public long getWritableLength() {
        return (first == null ? 0 : first.getWritableLength())
                + 1
                + (second == null ? 0 : second.getWritableLength());
    }

    public void write(OutputStream out) throws IOException {
        if (first != null) first.write(out);
        BinaryTools.writeUByte(out, code);
        if (second != null) second.write(out);
    }

    public String toString() {
        return "ExtraInfoBlockHolder: code=" + code
                + ", first=" + first 
                + ", second=" + second;
    }
}
