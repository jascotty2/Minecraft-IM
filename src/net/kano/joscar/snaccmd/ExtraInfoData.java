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
 *  File created by keith @ Mar 1, 2003
 *
 */

package net.kano.joscar.snaccmd;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.Writable;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A data structure used to hold data in {@link ExtraInfoBlock}s, containing
 * a set of flags and a block of data.
 * <br>
 * <br>
 * A few notes on flags:
 * <ul>
 * <li> When an extra info block received from the server is an {@linkplain
 * ExtraInfoBlock#TYPE_ICONHASH icon hash block} (like in a
 * {@link FullUserInfo#getExtraInfoBlocks() FullUserInfo}):
 * <ul>
 * <li> If the flags include the {@link #FLAG_HASH_PRESENT} bit, the data field
 * is a buddy icon hash (in MD5 format). If the <code>FLAG_HASH_PRESENT</code>
 * bit is off,that user may not have an icon, and the data field will (normally)
 * be {@link #HASH_SPECIAL}. The significance of <code>HASH_SPECIAL</code> is
 * unknown at the time of this writing. One should note that if the flags
 * include the <code>FLAG_HASH_PRESENT</code> bit, the data block may still be
 * <code>HASH_SPECIAL</code>. </li>
 * <li> When received in an {@link net.kano.joscar.snaccmd.conn.ExtraInfoAck},
 * the flags {@link #FLAG_UPLOAD_ICON} or {@link #FLAG_ALREADY_HAVE_ICON} may be
 * present. If <code>FLAG_UPLOAD_ICON</code> is present, the client should
 * upload the user's buddy icon to the buddy icon server (with an {@link
 * net.kano.joscar.snaccmd.icon.UploadIconCmd}. If
 * <code>FLAG_ALREADY_HAVE_ICON</code> is present, the server has your buddy
 * icon cached and no further action is necessary. Normally, the
 * <code>FLAG_ALREADY_HAVE_ICON</code> flag is accompanied by a
 * <code>FLAG_HASH_PRESENT</code> flag; the <code>FLAG_UPLOAD_ICON</code> flag
 * normally is the only flag set (if, of course, it is set). </li>
 * </ul>
 * </li>
 * <li> When sending icon data to the server, like in a {@link
 * net.kano.joscar.ssiitem.IconItem}:
 * <ul>
 * <li> When setting a buddy icon, <code>FLAG_HASH_PRESENT</code> should be set
 * and the data block should be an MD5 hash of the icon. </li>
 * <li> When clearing (removing) a buddy icon, no flags should be set ({@link
 * #FLAG_DEFAULT}) and the data block should be <code>#HASH_SPECIAL</code>.
 * </ul>
 * </li>
 * <li> When setting an {@linkplain ExtraInfoBlock#TYPE_AVAILMSG iChat
 * availability message}, the flag {@link #FLAG_AVAILMSG_PRESENT} should be
 * set. The data should be as follows:
 * <ul>
 * <li> An unsigned two-byte value indicating the length of the message
 * data </li>
 * <li> The message data: the message string encoded as UTF-8 </li>
 * <li> Two zero bytes </li>
 * </ul>
 *  </li>
 * <li> To "unset" or clear an iChat availability message, one should simply set
 * an empty (zero-length) availability message. </li>
 * </ul>
 *
 * Note that, as used above, a "flag" means a single bit, and a flag being
 * "present," "on"," or "included" means that the bit is "on." For example, to
 * see if an extra info data's <code>FLAG_HASH_PRESENT</code> flag is on, one
 * could use code such as the following:
 * <pre>
if ((extraInfoData.getFlags()
        & ExtraInfoData.FLAG_HASH_PRESENT) != 0) {
    System.out.println("this extra info data block "
            + "contains a buddy icon MD5 hash!");
}
 * </pre>
 */
public final class ExtraInfoData implements Writable {
    /** A flagset with no flags on (<code>0x00</code>). */
    public static final int FLAG_DEFAULT = 0x00;

    /**
     * A flag indicating that a hash (for example, an MD5 hash of the user's
     * buddy icon) is present. See {@linkplain ExtraInfoData above} for details.
     */
    public static final int FLAG_HASH_PRESENT = 0x01;

    /**
     * A flag indicating that the client should upload its buddy icon. See
     * {@linkplain ExtraInfoData above} for details.
     * */
    public static final int FLAG_UPLOAD_ICON = 0x40;

    /**
     * A flag indicating that the server already has a copy of your buddy icon.
     * This flag is normally accompanied by {@link #FLAG_HASH_PRESENT}. See
     * {@linkplain ExtraInfoData above} for details.
     * */
    public static final int FLAG_ALREADY_HAVE_ICON = 0x80;

    /**
     * A flag indicating that an iChat availability message is present. See
     * {@linkplain ExtraInfoData above} for details.
     * */
    public static final int FLAG_AVAILMSG_PRESENT = 0x04;

    /**
     * A "special" icon hash which appears to indicate that no icon has been
     * set. See {@linkplain ExtraInfoData above} for details.
     */
    public static final ByteBlock HASH_SPECIAL = ByteBlock.wrap(
            new byte[] { 0x02, 0x01, (byte) 0xd2, 0x04, 0x72 });

    /**
     * Reads an extra info data block from the given block of binary data. The
     * total number of bytes read can be retrieved by calling
     * <code>getTotalSize</code> on the returned object. Note that this will
     * return <code>null</code> if no valid extra info data block can be read.
     *
     * @param block the block containing an extra info data block
     * @return an extra info data block object read from the given data block,
     *         or <code>null</code> if no valid block could be read
     */
    public static ExtraInfoData readExtraInfoData(ByteBlock block) {
        DefensiveTools.checkNull(block, "block");

        if (block.getLength() < 2) return null;

        int code = BinaryTools.getUByte(block, 0);
        int len = BinaryTools.getUByte(block, 1);

        if (block.getLength() < len + 2) return null;

        ByteBlock hash = block.subBlock(2, len);

        return new ExtraInfoData(code, hash, 2 + len);
    }

    /**
     * The code associated with this hash info block.
     */
    private final int flags;

    /** The data stored in this object. */
    private final ByteBlock data;

    /**
     * The total size of this block, in bytes, as read from a block of binary
     * data.
     */
    private final int totalSize;

    /**
     * Creates a new extra info data object with the given properties. One
     * should note that the block of data must contain no more than {@link
     * BinaryTools#UBYTE_MAX} (<code>255</code>) bytes.
     *
     * @param code the extra info data's code
     * @param data the data stored in this extra info data block
     * @param totalSize the total size of this object, as read from a block of
     *        binary data
     *
     * @throws IllegalArgumentException if <code>data.length >
     *         BinaryTools.UBYTE_MAX</code>
     */
    private ExtraInfoData(int code, ByteBlock data, int totalSize)
            throws IllegalArgumentException {
        DefensiveTools.checkRange(code, "code", 0);
        DefensiveTools.checkRange(totalSize, "totalSize", -1);

        DefensiveTools.checkRange(data.getLength(), "data length", 0,
                BinaryTools.UBYTE_MAX);

        this.flags = code;
        this.data = data;
        this.totalSize = totalSize;
    }

    /**
     * Creates a new extra info block object with the given flags and given data
     * block. One should note that the block of data must contain no more than
     * {@link BinaryTools#UBYTE_MAX} (<code>255</code>) bytes.
     *
     * @param flags a set of bit flags, like {@link #FLAG_AVAILMSG_PRESENT}
     * @param data a block of data to store in this extra info data block
     *
     * @throws IllegalArgumentException if <code>data.length >
     *         BinaryTools.UBYTE_MAX</code>
     */
    public ExtraInfoData(int flags, ByteBlock data)
            throws IllegalArgumentException {
        this(flags, data, -1);
    }

    /**
     * Returns the flags stored in this extra info data object. The returned
     * value will normally be a bitwise combination of the {@link
     * #FLAG_HASH_PRESENT FLAG_*} constants defined in this class. See
     * {@linkplain ExtraInfoData above} for details on how to check for specific
     * flags.
     *
     * @return a set of bit flags associated with this extra info data block
     */
    public final int getFlags() { return flags; }

    /**
     * Returns the data stored in this extra info data block. The type of the
     * returned data may be inferred from this object's {@linkplain #getFlags()
     * flags}.
     *
     * @return the data contained in this extra info data block
     */
    public final ByteBlock getData() { return data; }

    /**
     * Returns the size, in bytes, of this object, as read from a block of
     * binary data. Will be <code>-1</code> if this object was not read from a
     * data block with <code>readExtraInfoData</code>.
     *
     * @return the total size of this object, in bytes
     */
    public final int getTotalSize() { return totalSize; }

    public long getWritableLength() {
        return 2 + (data == null ? 0 : data.getWritableLength());
    }

    public void write(OutputStream out) throws IOException {
        BinaryTools.writeUByte(out, flags);
        if (data != null) {
            BinaryTools.writeUByte(out, data.getLength());
            data.write(out);
        }
    }

    public String toString() {
        return "ExtraInfoData: flags=0x" + Integer.toHexString(flags)
                + ", data=" + BinaryTools.describeData(data);
    }
}
