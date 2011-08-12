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
 *  File created by keith @ Mar 2, 2003
 *
 */

package net.kano.joscar.snaccmd;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.Writable;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents an "extra information block," as I call it, which contains a
 * single result code and an <code>ExtraInfoData</code>. This structure is used
 * in various SNAC commands related to buddy icons and iChat availabilty
 * messages.
 */
public final class ExtraInfoBlock implements Writable {
    /**
     * An extra info block type indicating that it contains buddy icon
     * information.
     */
    public static final int TYPE_ICONHASH = 0x0001;

    /**
     * An extra info block type indicating that it contains iChat availability
     * message information.
     */
    public static final int TYPE_AVAILMSG = 0x0002;

    /**
     * An extra info block type indicating that the block contains AIM
     * Expression information.
     */
    public static final int TYPE_AIMEXPINFO = 0x0080;

    /**
     * An extra info block type indicating that the block contains a
     * security-related MD5 hash whose significance is unknown at the time of
     * this writing.
     */
    public static final int TYPE_CERTINFO_HASHA = 0x0402;
    /**
     * An extra info block type indicating that the block contains a
     * security-related MD5 hash whose significance is unknown at the time of
     * this writing.
     */
    public static final int TYPE_CERTINFO_HASHB = 0x0403;

    /** The type of data contained in this extra info block. */
    private final int type;

    /** The data contained in this extra info block. */
    private final ExtraInfoData extraData;

    /**
     * The total size of this structure, if read from a data block with
     * <code>readExtraInfoBlock</code>.
     */
    private final int totalSize;

    /**
     * Reads a series of <code>ExtraInfoBlock</code>s from the given block. The
     * total number of bytes read is the sum of calling
     * <code>getTotalSize</code> on each element of the returned array.
     *
     * @param block the block containing zero or more extra info blocks
     * @return a list of zero or more extra info block objects read from the
     *         given data block
     */
    public static ExtraInfoBlock[] readExtraInfoBlocks(ByteBlock block) {
        DefensiveTools.checkNull(block, "block");

        List infos = new LinkedList();

        for (;;) {
            ExtraInfoBlock info = readExtraInfoBlock(block);
            if (info == null) break;

            infos.add(info);

            block = block.subBlock(info.getTotalSize());
        }

        return (ExtraInfoBlock[]) infos.toArray(new ExtraInfoBlock[0]);
    }

    /**
     * Reads an extra info block from the given data block.
     *
     * @param block the data block from which to read
     * @return an extra info block object read from the given data block, or
     *         <code>null</code> if no valid object could be read
     */
    public static ExtraInfoBlock readExtraInfoBlock(ByteBlock block) {
        DefensiveTools.checkNull(block, "block");

        if (block.getLength() < 2) return null;

        int code = BinaryTools.getUShort(block, 0);

        ByteBlock hashBlock = block.subBlock(2);
        ExtraInfoData data = ExtraInfoData.readExtraInfoData(hashBlock);

        int size = 2;
        if (data != null) size += data.getTotalSize();

        return new ExtraInfoBlock(code, data, size);
    }

    /**
     * Creates a new extra info block object with the given properties.
     *
     * @param code the type code associated with this extra info block object,
     *        like {@link #TYPE_AVAILMSG}
     * @param data the data in this extra info block
     * @param totalSize the total size of this object, if read from a block
     *        of data
     */
    protected ExtraInfoBlock(int code, ExtraInfoData data, int totalSize) {
        DefensiveTools.checkRange(code, "code", 0);
        DefensiveTools.checkRange(totalSize, "totalSize", -1);

        this.type = code;
        this.extraData = data;
        this.totalSize = totalSize;
    }

    /**
     * Creates an extra info block object with the given type code and data.
     *
     * @param type the type of data being stored in this extra info block
     * @param extraData a block of extra info data
     */
    public ExtraInfoBlock(int type, ExtraInfoData extraData) {
        this(type, extraData, -1);
    }

    /**
     * Returns the type code for this extra info block. The returned value will
     * normally be one of {@link #TYPE_ICONHASH} and {@link #TYPE_AVAILMSG}.
     *
     * @return a type code
     */
    public final int getType() {
        return type;
    }

    /**
     * The data embedded in this extra info block.
     *
     * @return this object's associated extra data block
     */
    public final ExtraInfoData getExtraData() {
        return extraData;
    }

    /**
     * Returns the total size, in bytes, of this object. Will be <code>-1</code>
     * if this object was not read using <code>readExtraInfoBlock</code> or
     * <code>readExtraInfos</code>.
     *
     * @return the total size, in bytes, of this object, if read from a data
     *         block
     */
    public final int getTotalSize() {
        return totalSize;
    }

    public long getWritableLength() {
        return 2 + (extraData == null ? 0 : extraData.getWritableLength());
    }

    public void write(OutputStream out) throws IOException {
        BinaryTools.writeUShort(out, type);
        if (extraData != null) extraData.write(out);
    }

    public String toString() {
        return "ExtraInfoBlock: type=0x" + Long.toHexString(type)
                + ", extraData=<" + extraData + ">";
    }
}
