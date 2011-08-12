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
 *  File created by keith @ Aug 11, 2003
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
 * Short capability blocks are means of storing a capability block in two bytes
 * instead of sixteen. Almost all of the official sixteen-byte AIM capability
 * blocks contain fourteen of the same bytes. That is, most of AIM's capability
 * blocks are of the form:
 * <pre>
09 46 <i>?? ??</i> 4c 7f 11 d1
82 22 44 45 53 54 00 00
</pre>
 * A short capability block only stores the values of the two bytes that vary
 * between most of AIM's capabilities.
 * <br>
 * <br>
 * One may wish to note that capabilities that do not fit the given
 * form are normally still stored as plain old {@link CapabilityBlock}s.
 */
public class ShortCapabilityBlock implements Writable {
    /**
     * Reads a sequence of short capability blocks from the given block of
     * binary data. Note that this method will never return <code>null</code>;
     * if no short capability blocks are present in the given block of data,
     * an empty array will be returned.
     *
     * @param block a block of data containing a sequence of zero or more short
     *        capability blocks
     * @return an array of short capability block objects, read from the given
     *         block of binary data
     */
    public static ShortCapabilityBlock[] readShortCaps(ByteBlock block) {
        DefensiveTools.checkNull(block, "block");

        List caps = new LinkedList();

        for (int i = 0; i < block.getLength(); i += 2) {
            caps.add(new ShortCapabilityBlock(block.subBlock(i, 2)));
        }

        return (ShortCapabilityBlock[])
                caps.toArray(new ShortCapabilityBlock[0]);
    }

    /**
     * Returns whether the given long capability block fits the form of the
     * "family" of capability blocks that can be represented as short capability
     * blocks.
     *
     * @param cap the capability block to check
     * @return whether or not the given long capability block could be
     *         represented as a short capability block
     */
    public static boolean couldBeShortBlock(CapabilityBlock cap) {
        DefensiveTools.checkNull(cap, "cap");

        ByteBlock block = cap.getBlock();
        return block.subBlock(0, 2).equals(BYTEBLOCK_DEFAULT.subBlock(0, 2))
                && block.subBlock(4).equals(BYTEBLOCK_DEFAULT.subBlock(4));
    }

    /**
     * Returns a short capability block that represents the given long
     * capability block.
     *
     * @param cap the capability block to convert to a short capability block
     * @return a short capability block that represents the given long
     *         capability block
     *
     * @throws IllegalArgumentException if the given capability block cannot
     *         be represented as a short capability block
     */
    public static ShortCapabilityBlock getShortBlock(CapabilityBlock cap)
            throws IllegalArgumentException {
        DefensiveTools.checkNull(cap, "cap");

        if (!couldBeShortBlock(cap)) {
            throw new IllegalArgumentException("Capability block '" + cap
                    + "' cannot be converted to a short capability block");
        }

        return new ShortCapabilityBlock(cap.getBlock().subBlock(2, 2));
    }

    /**
     * The "template" for converting short capability blocks to long capability
     * blocks.
     */
    private static final byte[] BLOCK_DEFAULT = new byte[] {
            0x09, 0x46, 0x00, 0x00, 0x4c, 0x7f, 0x11, (byte) 0xd1,
            (byte) 0x82, 0x22, 0x44, 0x45, 0x53, 0x54, 0x00, 0x00};
    /**
     * The "template" for converting short capability blocks to long capability
     * blocks, in <code>ByteBlock</code> form.
     */
    private static final ByteBlock BYTEBLOCK_DEFAULT
            = ByteBlock.wrap(BLOCK_DEFAULT);

    /** The two bytes of short capability block data. */
    private final ByteBlock data;

    /**
     * Creates a new short capability block object with the given two-byte block
     * of data.
     *
     * @param data the two-byte block of data that this short capability block
     *        should contain
     *
     * @throws IllegalArgumentException if the given block of data contains more
     *         or fewer than two bytes 
     */
    public ShortCapabilityBlock(ByteBlock data)
            throws IllegalArgumentException {
        DefensiveTools.checkNull(data, "data");

        if (data.getLength() != 2) {
            throw new IllegalArgumentException("short capability data block "
                    + "must contain only two bytes (it has "
                    + data.getLength() + ")");
        }

        this.data = data;
    }

    /**
     * Returns the two bytes of data contained in this short capability block.
     *
     * @return this short capability block's (two-byte) data block
     */
    public final ByteBlock getData() { return data; }

    /**
     * Returns the <code>CapabilityBlock</code> that this short capability block
     * represents.
     *
     * @return a <code>CapabilityBlock</code> equivalent to this short
     *         capability block
     */
    public final CapabilityBlock toCapabilityBlock() {
        byte[] block = (byte[]) BLOCK_DEFAULT.clone();
        System.arraycopy(data.toByteArray(), 0, block, 2, 2);

        return new CapabilityBlock(ByteBlock.wrap(block));
    }

    public long getWritableLength() { return 2; }

    public void write(OutputStream out) throws IOException {
        data.write(out);
    }

    /**
     * Returns whether this object is equal to the given object. Note that this
     * method returns <code>false</code> for <i>all</i> objects which are not
     * <code>ShortCapabilityBlock</code>s - even <code>CapabilityBlock</code>s.
     *
     * @param obj the object to compare for equality
     * @return whether or not this object is equal to the given object
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof ShortCapabilityBlock)) return false;
        if (this == obj) return true;

        ShortCapabilityBlock scb = (ShortCapabilityBlock) obj;

        return scb.data.equals(data);
    }

    public int hashCode() {
        return BinaryTools.getUShort(data, 0);
    }

    public String toString() {
        return "ShortCapabilityBlock: " + BinaryTools.describeData(data);
    }
}
