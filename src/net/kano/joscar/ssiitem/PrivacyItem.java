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

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.snaccmd.FullUserInfo;
import net.kano.joscar.snaccmd.ssi.SsiItem;
import net.kano.joscar.tlv.*;

/**
 * An SSI item object containing various privacy-related settings.
 * <br>
 * <br>
 * Note that this class is only used to store data and that <b>changes to this
 * object are not reflected on the server</b> without sending the changes to the
 * server with a {@link net.kano.joscar.snaccmd.ssi.ModifyItemsCmd
 * ModifyItemsCmd}.
 */
public class PrivacyItem extends AbstractItemObj {
    /**
     * A privacy mode under which the {@linkplain DenyItem block list} is
     * ignored and thus no users are blocked.
     */
    public static final int MODE_ALLOW_ALL = 0x01;
    /** A privacy mode under which all users are blocked. */
    public static final int MODE_BLOCK_ALL = 0x02;
    /**
     * A privacy mode under which all users not on the {@linkplain PermitItem
     * "allow list"} are blocked.
     */
    public static final int MODE_ALLOW_PERMITS = 0x03;
    /**
     * A privacy mode under which only users on the {@linkplain DenyItem block
     * list} are blocked.
     */
    public static final int MODE_BLOCK_DENIES = 0x04;
    /**
     * A privacy mode under which all users not on one's buddy list are blocked.
     */
    public static final int MODE_ALLOW_BUDDIES = 0x05;

    /**
     * A visibility flag indicating that the fact that the user is or is not
     * using a wireless/mobile device (cell phone) should be hidden from other
     * users.
     */
    public static final long VISMASK_HIDE_WIRELESS = 0x00000002L;

    /** The item name used for privacy items. */
    private static final String NAME_DEFAULT = "";
    /** The parent group ID used for privacy items. */
    private static final int PARENTID_DEFAULT = 0x0000;

    /** A TLV type containing the user's "privacy mode." */
    private static final int TYPE_PRIVACY_MODE = 0x00ca;
    /** A TLV type containing the user's "visible class mask." */
    private static final int TYPE_CLASS_MASK = 0x00cb;
    /** A TLV type containing the user's "visibility mask." */
    private static final int TYPE_VISIBILE_MASK = 0x00cc;

    /** The privacy item ID. */
    private final int id;
    /** The user's "privacy mode. */
    private int privacyMode;
    /** The user's "visible class mask." */
    private long classMask;
    /** The user's "visibility mask. */
    private long visibleMask;

    /**
     * Creates a new privacy settings item object generated from the given
     * SSI item.
     *
     * @param item a privacy settings SSI item
     */
    public PrivacyItem(SsiItem item) {
        DefensiveTools.checkNull(item, "item");

        TlvChain chain = TlvTools.readChain(item.getData());

        id = item.getId();

        Tlv typeTlv = chain.getLastTlv(TYPE_PRIVACY_MODE);
        if (typeTlv != null) {
            privacyMode = BinaryTools.getUByte(typeTlv.getData(), 0);
        } else {
            privacyMode = -1;
        }

        Tlv classMaskTlv = chain.getLastTlv(TYPE_CLASS_MASK);
        if (classMaskTlv != null) {
            classMask = BinaryTools.getUInt(classMaskTlv.getData(), 0);
        } else {
            classMask = -1;
        }

        Tlv visibileMaskTlv = chain.getLastTlv(TYPE_VISIBILE_MASK);
        if (visibileMaskTlv != null) {
            visibleMask = BinaryTools.getUInt(visibileMaskTlv.getData(), 0);
        } else {
            visibleMask = -1;
        }

        MutableTlvChain extraTlvs = TlvTools.getMutableCopy(chain);

        extraTlvs.removeTlvs(new int[] {
            TYPE_PRIVACY_MODE, TYPE_CLASS_MASK, TYPE_VISIBILE_MASK
        });

        addExtraTlvs(extraTlvs);
    }

    /**
     * Creates a new privacy settings item object with the same properties as
     * the given object.
     *
     * @param other a privacy settings item object to copy
     */
    public PrivacyItem(PrivacyItem other) {
        this(other.id, other.privacyMode, other.classMask,
                other.visibleMask, other.copyExtraTlvs());
    }

    /**
     * Creates a new privacy settings item object with the given properties and
     * a visible class mask of {@link FullUserInfo#MASK_ALL
     * FullUserInfo.MASK_ALL}.
     *
     * @param id the unique privacy item ID for this item
     * @param mode the "privacy mode," like {@link #MODE_ALLOW_BUDDIES}
     * @param visibleMask a "visibility mask," like {@link
     *        #VISMASK_HIDE_WIRELESS}
     */
    public PrivacyItem(int id, int mode, long visibleMask) {
        this(id, mode, FullUserInfo.MASK_ALL, visibleMask);
    }

    /**
     * Creates a new privacy settings item object with the given properties.
     *
     * @param id the unique privacy item ID for this item
     * @param mode the "privacy mode," like {@link #MODE_ALLOW_BUDDIES}
     * @param classMask a "class mask," like <code>{@link
     *        FullUserInfo#MASK_FREE FullUserInfo.MASK_FREE} | {@link
     *        FullUserInfo#MASK_WIRELESS FullUserInfo.MASK_WIRELESS}</code> or
     *        (preferably) {@link FullUserInfo#MASK_ALL FullUserInfo.MASK_ALL}
     * @param visibleMask a "visibility mask," like {@link
     *        #VISMASK_HIDE_WIRELESS}
     */
    public PrivacyItem(int id, int mode, long classMask, long visibleMask) {
        this(id, mode, classMask, visibleMask, null);
    }

    /**
     * Creates a new privacy settings item object with the given properties.
     *
     * @param id the unique privacy item ID for this item
     * @param mode the "privacy mode," like {@link #MODE_ALLOW_BUDDIES}
     * @param classMask a "class mask," like <code>{@link
     *        FullUserInfo#MASK_FREE FullUserInfo.MASK_FREE} | {@link
     *        FullUserInfo#MASK_WIRELESS FullUserInfo.MASK_WIRELESS}</code> or
     *        (preferably) {@link FullUserInfo#MASK_ALL FullUserInfo.MASK_ALL}
     * @param visibleMask a "visibility mask," like {@link
     *        #VISMASK_HIDE_WIRELESS}
     * @param extraTlvs a list of extra TLV's to store in this item
     */
    public PrivacyItem(int id, int mode, long classMask, long visibleMask,
            TlvChain extraTlvs) {
        super(extraTlvs);

        DefensiveTools.checkRange(id, "id", 0);
        DefensiveTools.checkRange(mode, "mode", -1);
        DefensiveTools.checkRange(classMask, "classMask", -1);
        DefensiveTools.checkRange(visibleMask, "visibleMask", -1);

        this.id = id;
        this.privacyMode = mode;
        this.classMask = classMask;
        this.visibleMask = visibleMask;
    }

    /**
     * Returns the item ID of this privacy settings item object.
     *
     * @return this privacy settings item object's item ID
     */
    public final int getId() { return id; }

    /**
     * Returns this privacy settings object's (and, consequently, the user's)
     * privacy mode. Normally one of {@link #MODE_ALLOW_ALL}, {@link
     * #MODE_BLOCK_ALL}, {@link #MODE_ALLOW_PERMITS}, {@link
     * #MODE_BLOCK_DENIES}, and {@link #MODE_ALLOW_BUDDIES}. This value will
     * be <code>-1</code> if no privacy mode code is stored in this item.
     *
     * @return the user's privacy mode, or <code>-1</code> if none is stored
     *         in this item
     */
    public synchronized final int getPrivacyMode() { return privacyMode; }

    /**
     * Returns this object's "visible class mask." Normally a bitwise
     * combination of any of the {@linkplain FullUserInfo#MASK_UNCONFIRMED
     * <code>FullUserInfo.MASK_<i>*</i></code> constants}.
     * <br>
     * <br>
     * To check for a specific class mask, you can use code resembling the
     * following:
     * <pre>
if ((privacyItem.getClassMask() & FullUserInfo.MASK_WIRELESS) != 0) {
    System.out.println("We are visible to wireless AIM users");
}
     * </pre>
     * <b>NOTE that this value might be <code>-1</code></b>, meaning that
     * <i>any</i> bit mask will match. <b>Always check for <code>-1</code>
     * before performing bitwise operations on this value.</b>
     * <br>
     * <br>
     * A "visible class mask" describes, essentially, which users are blocked.
     * WinAIM always sets this to allow all user classes ({@link
     * FullUserInfo#MASK_ALL FullUserInfo.MASK_ALL}), but if you really wanted
     * to you could set yourself to be only visible to, for example, users who
     * were either away or using AIM on a cell phone (using <code>{@link
     * FullUserInfo#MASK_AWAY FullUserInfo.MASK_AWAY} | {@link
     * FullUserInfo#MASK_WIRELESS FullUserInfo.MASK_WIRELESS}).
     *
     * @return the user's "visible class mask," or <code>-1</code> if no class
     *         mask is stored in this item
     */
    public synchronized final long getClassMask() { return classMask; }

    /**
     * Returns this object's "visibility settings mask." Normally a bitwise
     * combination of {@link #VISMASK_HIDE_WIRELESS}. Well, okay, so it can't be
     * a bitwise combination of one value. But in the future other flags could
     * be added, so it's best to use bitwise comparisons on this value.
     * <br>
     * <br>
     * To check for a specific flag, you can use code resembling the following:
     * <pre>
if ((privacyItem.getVisibleMask() & PrivacyItem.VISMASK_HIDE_WIRELESS) != 0) {
    System.out.println("No one knows if we are on a cell phone or not!");
}
     * </pre>
     * <b>NOTE that this value might be <code>-1</code></b>, meaning that
     * <i>any</i> bit mask will match. <b>Always check for <code>-1</code>
     * before performing bitwise operations on this value.</b>
     *
     * @return the user's "visibility settings flags," or <code>-1</code> if no
     *         visibility settings mask is stored in this item
     */
    public synchronized final long getVisibleMask() { return visibleMask; }

    /**
     * Sets this item's privacy mode code. <code>privacyMode</code> should
     * normally be one of {@link #MODE_ALLOW_ALL}, {@link #MODE_BLOCK_ALL},
     * {@link #MODE_ALLOW_PERMITS}, {@link #MODE_BLOCK_DENIES}, and {@link
     * #MODE_ALLOW_BUDDIES}. <code>privacyMode</code> can be <code>-1</code> to
     * erase the privacy mode stored in this item.
     *
     * @param privacyMode a privacy mode code, or <code>-1</code> to erase any
     *        privacy mode stored in this item
     */
    public synchronized final void setPrivacyMode(int privacyMode) {
        this.privacyMode = privacyMode;
    }

    /**
     * Sets the user's "visible class mask." See {@linkplain #getClassMask
     * above} for details on what this means. <code>classMask</code> should
     * normally be a bitwise combination of any of the {@linkplain
     * FullUserInfo#MASK_UNCONFIRMED <code>FullUserInfo.MASK_<i>*</i></code>
     * constants}. <code>classMask</code> can also be <code>-1</code> to erase
     * the visible class mask stored in this item.
     *
     * @param classMask a new "visible class mask" to store in this privacy
     *        settings item, or <code>-1</code> to erase any visible class mask
     *        stored in this item
     */
    public synchronized final void setClassMask(long classMask) {
        this.classMask = classMask;
    }

    /**
     * Sets the user's "visibility flags." See {@linkplain #getVisibleMask
     * above} for details on what this means as well as its normal values.
     *
     * @param visibleMask a new set of "visibility flags" to store in this
     *        privacy settings item
     */
    public synchronized final void setVisibleMask(long visibleMask) {
        this.visibleMask = visibleMask;
    }

    public synchronized SsiItem toSsiItem() {
        MutableTlvChain chain = TlvTools.createMutableChain();

        if (privacyMode != -1) {
            chain.addTlv(new Tlv(TYPE_PRIVACY_MODE,
                    ByteBlock.wrap(BinaryTools.getUByte(privacyMode))));
        }
        if (classMask != -1) {
            chain.addTlv(Tlv.getUIntInstance(TYPE_CLASS_MASK, classMask));
        }
        if (visibleMask != -1) {
            chain.addTlv(Tlv.getUIntInstance(TYPE_VISIBILE_MASK, visibleMask));
        }

        return generateItem(NAME_DEFAULT, PARENTID_DEFAULT, id,
                SsiItem.TYPE_PRIVACY, chain);
    }

    public synchronized String toString() {
        return "PrivacyItem: id=0x" + Integer.toHexString(id)
                + ", mode=" + privacyMode
                + ", classMask=0x" + Long.toHexString(classMask)
                + ", visMask=0x" + Long.toHexString(visibleMask);
    }
}
