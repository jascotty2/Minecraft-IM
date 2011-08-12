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
 *  File created by keith @ Mar 3, 2003
 *
 */

package net.kano.joscar.snaccmd.ssi;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.LiveWritable;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * A data structure used to store various types of "items" on the server. These
 * items can be buddies (with comments and buddy alert data), groups, blocked
 * buddies, and other settings. See the {@linkplain net.kano.joscar.ssiitem the
 * <code>ssiitem</code> package} for more logical implementations of specific
 * SSI item types.
 *
 * @see net.kano.joscar.ssiitem
 */
public class SsiItem implements LiveWritable, Serializable {
    /**
     * An SSI item type for a buddy.
     *
     * @see net.kano.joscar.ssiitem.BuddyItem
     */
    public static final int TYPE_BUDDY = 0x0000;
    /**
     * An SSI item type for a buddy group.
     *
     * @see net.kano.joscar.ssiitem.RootItem
     * @see net.kano.joscar.ssiitem.GroupItem
     */
    public static final int TYPE_GROUP = 0x0001;
    /**
     * An SSI item type for an "allowed" user, or "permit."
     *
     * @see net.kano.joscar.ssiitem.PermitItem
     */
    public static final int TYPE_PERMIT = 0x0002;
    /**
     * An SSI item type for a "blocked" user, or "deny."
     *
     * @see net.kano.joscar.ssiitem.DenyItem
     */
    public static final int TYPE_DENY = 0x0003;
    /**
     * An SSI item type for various privacy-related settings.
     *
     * @see net.kano.joscar.ssiitem.PrivacyItem
     */
    public static final int TYPE_PRIVACY = 0x0004;
    /**
     * An SSI item type for various "visiblity-related" settings.
     *
     * @see net.kano.joscar.ssiitem.VisibilityItem
     */
    public static final int TYPE_VISIBILITY = 0x0005;
    /**
     * An SSI item type for a {@linkplain net.kano.joscar.snaccmd.ExtraInfoData
     * buddy icon hash}.
     *
     * @see net.kano.joscar.ssiitem.IconItem
      */
    public static final int TYPE_ICON_INFO = 0x0014;

    /**
     * Generates a new SSI item from the given block of binary data, or
     * <code>null</code> if no valid item could be read.
     *
     * @param block a block of data containing an SSI item
     * @return a new SSI item object read from the given block of data, or
     *         <code>null</code> if none could be read
     */
    protected static SsiItem readSsiItem(ByteBlock block) {
        DefensiveTools.checkNull(block, "block");

        if (block.getLength() < 10) return null;

        int nameLen = BinaryTools.getUShort(block, 0);
        if (block.getLength() < 2 + nameLen) return null;

        ByteBlock nameBlock = block.subBlock(2, nameLen);
        String name = BinaryTools.getAsciiString(nameBlock);

        ByteBlock rest = block.subBlock(2 + nameLen);
        if (rest.getLength() < 8) return null;

        int groupid = BinaryTools.getUShort(rest, 0);
        int buddyid = BinaryTools.getUShort(rest, 2);
        int type = BinaryTools.getUShort(rest, 4);

        int datalen = BinaryTools.getUShort(rest, 6);
        if (rest.getLength() < 1 + datalen) return null;

        ByteBlock data = rest.subBlock(8, datalen);

        int size = data.getOffset() + data.getLength() - block.getOffset();

        return new SsiItem(name, groupid, buddyid, type, data, size);
    }

    /** The item name. */
    private final String name;
    /** The parent ID of the item. */
    private final int parentid;
    /** The subID"" of the item. */
    private final int id;
    /** The item type. */
    private final int type;
    /** The item data block. */
    private final ByteBlock data;
    /** The total size of this object, as read from an incoming data block. */
    private transient final int totalSize;

    /**
     * Creates a new SSI item with the given properties.
     *
     * @param name the name of this item
     * @param parentid the "parent ID" of this item
     * @param id the ID of this item in its parent
     * @param type the type of the item, like {@link #TYPE_ICON_INFO}
     * @param data a type-specific data block for this item
     * @param totalSize the total size of this item, as read from an incoming
     *        block of binary data
     */
    private SsiItem(String name, int parentid, int id, int type,
            ByteBlock data, int totalSize) {
        DefensiveTools.checkNull(name, "name");
        DefensiveTools.checkRange(parentid, "parentid", 0);
        DefensiveTools.checkRange(id, "id", 0);
        DefensiveTools.checkRange(type, "type", 0);
        DefensiveTools.checkRange(totalSize, "totalSize", -1);

        this.name = name;
        this.parentid = parentid;
        this.id = id;
        this.type = type;
        this.data = data;
        this.totalSize = totalSize;
    }

    /**
     * Creates a new SSI item with no name or type-specific data and with the
     * given parent ID, sub ID, and item type.
     *
     * @param parentid the "parent ID" of this item
     * @param id the ID of this item in its parent
     * @param type the type of this item, like {@link #TYPE_BUDDY}
     */
    public SsiItem(int parentid, int id, int type) {
        this("", parentid, id, type, null);
    }

    /**
     * Creates a new SSI item with the given properties.
     *
     * @param name the name of this item
     * @param parentid the "parent ID" of this item
     * @param id the ID of this item in its parent
     * @param type the type of this item, like {@link #TYPE_GROUP}
     * @param data a block of type-specific data, or <code>null</code> (or an
     *        empty block) for none
     */
    public SsiItem(String name, int parentid, int id, int type,
            ByteBlock data) {
        this(name, parentid, id, type, data, -1);
    }

    /**
     * Returns the name of this item.
     *
     * @return the name of this item
     */
    public final String getName() {
        return name;
    }

    /**
     * Returns the "parent ID" of this item.
     *
     * @return this item's "parent ID"
     */
    public final int getParentId() {
        return parentid;
    }

    /**
     * Returns the ID or "sub ID" of this item. This value is only unique within
     * its {@linkplain #getParentId parent}.
     *
     * @return this item's "sub ID" in its parent
     */
    public final int getId() {
        return id;
    }

    /**
     * Returns this item's type. Normally one of the {@linkplain #TYPE_BUDDY
     * <code>TYPE_<i>*</i></code> constants} defined in this class.
     *
     * @return this item's SSI item type
     */
    public final int getItemType() {
        return type;
    }

    /**
     * Returns the type-specific data stored in this item.
     *
     * @return this item's type-specific data block
     */
    public final ByteBlock getData() {
        return data;
    }

    /**
     * Returns the total size, in bytes, of this object, if read from an
     * incoming block of binary data. Note that this will be <code>-1</code> if
     * this item was not read from an incoming data block.
     *
     * @return the total size, in bytes, of this object, as read from an
     *         incoming block of data
     */
    public int getTotalSize() {
        return totalSize;
    }

    public void write(OutputStream out) throws IOException {
        byte[] namebytes = BinaryTools.getAsciiBytes(name);
        BinaryTools.writeUShort(out, namebytes.length);
        out.write(namebytes);

        BinaryTools.writeUShort(out, parentid);
        BinaryTools.writeUShort(out, id);
        BinaryTools.writeUShort(out, type);

        // here we are nice and let data be null
        int len = data == null ? 0 : data.getLength();
        BinaryTools.writeUShort(out, len);
        if (data != null) data.write(out);
    }

    public String toString() {
        return "SsiItem '" + name + "', type=0x" + Integer.toHexString(type)
                + ", parentid=0x" + Integer.toHexString(parentid) + ", id=0x"
                + Integer.toHexString(id);
    }
}
