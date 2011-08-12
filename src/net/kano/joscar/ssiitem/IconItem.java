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

package net.kano.joscar.ssiitem;

import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.snaccmd.ExtraInfoData;
import net.kano.joscar.snaccmd.ssi.SsiItem;
import net.kano.joscar.tlv.*;

/**
 * An SSI item object holding information about a buddy icon. Note that
 * {@linkplain net.kano.joscar.snaccmd.ssi.ModifyItemsCmd modifying} (or
 * {@linkplain net.kano.joscar.snaccmd.ssi.CreateItemsCmd creating}) an icon
 * object sets that icon as the user's current icon (given that you {@linkplain
 * net.kano.joscar.snaccmd.icon.UploadIconCmd upload} it when {@linkplain
 * net.kano.joscar.snaccmd.conn.ExtraInfoAck asked}).
 * <br>
 * <br>
 * Note that this class is only used to store data and that <b>changes to this
 * object are not reflected on the server</b> without sending the changes to the
 * server with a {@link net.kano.joscar.snaccmd.ssi.ModifyItemsCmd
 * ModifyItemsCmd}.
 */
public class IconItem extends AbstractItemObj {
    /** A default name for a buddy icon item. */
    public static final String NAME_DEFAULT = "1";
    /**
     * A default "alias" for a buddy icon item. This is just an empty string, as
     * that is what WinAIM uses.
     */
    public static final String ALIAS_DEFAULT = "";

    /** The ID of the group in which buddy icon items reside. */
    private static final int GROUPID_DEFAULT = 0x0000;

    /** A TLV type containing an icon hash. */
    private static final int TYPE_ICON_HASH = 0x00d5;
    /**
     * A TLV type containing an alias or something. I'm not really sure what
     * it's supposed to be, as it's always empty when WinAIM uses it.
     */
    private static final int TYPE_ALIAS = 0x0131;

    /**
     * The name of this icon. Normally a number in the form of an ASCII string.
     */
    private final String name;
    /** The ID of this icon item. */
    private final int id;
    /** The icon hash stored in this item. */
    private ExtraInfoData iconInfo;
    /**
     * Some sort of alias for this icon, maybe. Currently always an empty string
     * in WinAIM.
     */
    private String alias;

    /**
     * Creates a new buddy icon item object from the data in the given SSI item.
     *
     * @param item a buddy icon SSI item
     */
    public IconItem(SsiItem item) {
        DefensiveTools.checkNull(item, "item");

        name = item.getName();
        id = item.getId();

        TlvChain chain = TlvTools.readChain(item.getData());

        Tlv iconTlv = chain.getLastTlv(TYPE_ICON_HASH);

        if (iconTlv != null) {
            ByteBlock block = iconTlv.getData();

            iconInfo = ExtraInfoData.readExtraInfoData(block);
        } else {
            iconInfo = null;
        }

        alias = chain.getString(TYPE_ALIAS);

        MutableTlvChain extraTlvs = TlvTools.getMutableCopy(chain);

        extraTlvs.removeTlvs(new int[] { TYPE_ICON_HASH, TYPE_ALIAS });

        addExtraTlvs(extraTlvs);
    }

    /**
     * Creates a new buddy icon item object with the same properties as the
     * given icon item object.
     *
     * @param other an icon item object to copy
     */
    public IconItem(IconItem other) {
        this(other.name, other.id, other.iconInfo, other.alias,
                other.copyExtraTlvs());
    }

    /**
     * Creates a new buddy icon item object with the given icon name (try
     * {@link #NAME_DEFAULT}), unique icon item ID number, and icon hash block.
     * Note that the icon hash block can be <code>null</code> to not store a
     * hash at all in this item. The item will be created with an "alias" of
     * {@link #ALIAS_DEFAULT}, which is an empty string.
     *
     * @param name the "name" of this icon, normally a positive number in ASCII
     *        text format (like <code>"2"</code> or {@link #NAME_DEFAULT})
     * @param id an ID number for this item
     * @param iconInfo a block of icon hash information, or <code>null</code> to not
     *        store an icon hash block in this item
     */
    public IconItem(String name, int id, ExtraInfoData iconInfo) {
        this(name, id, iconInfo, ALIAS_DEFAULT, null);
    }

    /**
     * Creates a new buddy icon item object with the given icon name (try
     * {@link #NAME_DEFAULT}), unique icon item ID number, and icon hash block.
     * Note that the icon hash block can be <code>null</code> to not store a
     * hash at all in this item.
     *
     * @param name the "name" of this icon, normally a positive number in ASCII
     *        text format (like <code>"2"</code> or {@link #NAME_DEFAULT})
     * @param id an ID number for this item
     * @param iconInfo a block of icon hash information, or
     *        <code>null</code> to not store an icon hash block in this item
     * @param alias some sort of "alias" for this icon; WinAIM always uses
     *        {@link #ALIAS_DEFAULT} (an empty string)
     * @param extraTlvs a list of extra TLV's to store in this item
     */
    public IconItem(String name, int id, ExtraInfoData iconInfo, String alias,
            TlvChain extraTlvs) {
        super(extraTlvs);

        DefensiveTools.checkNull(name, "name");
        DefensiveTools.checkRange(id, "id", 0);

        this.name = name;
        this.id = id;
        this.iconInfo = iconInfo;
        this.alias = alias;
    }

    /**
     * Returns this icon item's name. Normally a nonnegative number in ASCII
     * text format (like <code>"0"</code>).
     *
     * @return this icon item's name
     */
    public final String getName() { return name; }

    /**
     * Returns this icon item's unique ID.
     *
     * @return this icon item's ID
     */
    public final int getId() { return id; }

    /**
     * Returns the icon hash stored in this item, or <code>null</code> if none
     * is stored.
     *
     * @return the icon hash block stored in this item, or <code>null</code> if
     *         none is stored in this item
     */
    public synchronized final ExtraInfoData getIconInfo() { return iconInfo; }

    /**
     * Returns this icon item's "alias." As of this writing WinAIM always sets
     * this to an empty string, or {@link #ALIAS_DEFAULT}; thus, I am unsure
     * of the value's significance. This field is called "alias" because it is
     * stored in the same data type as a buddy's {@linkplain BuddyItem#getAlias
     * alias}.
     *
     * @return this icon item's "alias"
     */
    public synchronized final String getAlias() { return alias; }

    /**
     * Sets the icon hash block stored in this icon item. <code>iconInfo</code>
     * can be <code>null</code> to erase this icon item's icon hash.
     *
     * @param iconInfo a new icon hash block for this buddy icon item, or
     *        <code>null</code> to erase this item's stored icon hash (if any)
     */
    public synchronized final void setIconInfo(ExtraInfoData iconInfo) {
        this.iconInfo = iconInfo;
    }

    /**
     * Sets the "alias" for this icon item. As of this writing this is always
     * set to an empty string ({@link #ALIAS_DEFAULT}) by WinAIM. It is called
     * an alias because it is stored as the same data type as a buddy's
     * {@linkplain BuddyItem#getAlias alias}.
     *
     * @param alias a new "alias" for this icon item
     */
    public synchronized final void setAlias(String alias) {
        this.alias = alias;
    }

    public synchronized SsiItem toSsiItem() {
        MutableTlvChain chain = TlvTools.createMutableChain();

        if (iconInfo != null) {
            ByteBlock iconData = ByteBlock.createByteBlock(iconInfo);
            chain.addTlv(new Tlv(TYPE_ICON_HASH, iconData));
        }

        if (alias != null) {
            chain.addTlv(Tlv.getStringInstance(TYPE_ALIAS, alias));
        }

        return generateItem(name, GROUPID_DEFAULT, id, SsiItem.TYPE_ICON_INFO,
                chain);
    }

    public synchronized String toString() {
        return "IconItem: name=" + name + ", id=0x" + Integer.toHexString(id)
                + ", alias='" + alias + "', iconinfo=" + iconInfo;
    }
}
