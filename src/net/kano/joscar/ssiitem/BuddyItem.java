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
import net.kano.joscar.snaccmd.ssi.SsiItem;
import net.kano.joscar.tlv.*;

/**
 * An SSI item object representing a buddy on the user's buddy list. A buddy
 * item contains a set of buddy alert flags (for the different types of alerts),
 * an alert sound filename, a buddy comment, and (though WinAIM does not yet
 * support it) an "alias" or "display name" for the buddy.
 * <br>
 * <br>
 * Note that this class is only used to store data and that <b>changes to this
 * object are not reflected on the server</b> without sending the changes to the
 * server with a {@link net.kano.joscar.snaccmd.ssi.ModifyItemsCmd
 * ModifyItemsCmd}.
 */
public class BuddyItem extends AbstractItemObj {
    /**
     * An alert action flag indicating that a window should be popped up when
     * the buddy alert is activated.
     */
    public static final int MASK_ACTION_POPUP = 0x01;
    /**
     * An alert action flag indicating that a sound should be played when the
     * buddy alert is activated. The sound file is specified in {@link
     * #getAlertSound getAlertSound}.
     */
    public static final int MASK_ACTION_PLAY_SOUND = 0x02;

    /**
     * An alert flag indicating that the buddy's alert should be activated when
     * he or she signs on.
     */
    public static final int MASK_WHEN_ONLINE = 0x01;
    /**
     * An alert flag indicating that the buddy's alert should be activated when
     * he or she comes back from being idle.
     */
    public static final int MASK_WHEN_UNIDLE = 0x02;
    /**
     * An alert flag indicating that the buddy's alert should be activated when
     * he or she comes back from being away.
     */
    public static final int MASK_WHEN_UNAWAY = 0x04;

    /** A TLV type containing the user's "alias," or "display name." */
    private static final int TYPE_ALIAS = 0x0131;
    /** A TLV type containing the user's "buddy comment." */
    private static final int TYPE_COMMENT = 0x013c;
    /**
     * A TLV type containing the filename of a sound to play when an alert for
     * this buddy is activated.
     */
    private static final int TYPE_ALERT_SOUND = 0x013e;
    /** A TLV type containing a set of buddy alert flags. */
    private static final int TYPE_ALERT_FLAGS = 0x13d;

    /** The buddy's screenname. */
    private final String sn;
    /** The ID of the parent group of this buddy. */
    private final int groupid;
    /** The ID of this buddy in its parent group. */
    private final int id;

    /** The buddy's "alias." */
    private String alias;
    /** The buddy's buddy comment. */
    private String comment;

    /** A bit mask for what to do when an alert is activated. */
    private int alertActionMask;
    /** A bit mask for when to activate a buddy alert for this buddy. */
    private int alertWhenMask;
    /** A sound to play when an alert is activated. */
    private String alertSound;

    /**
     * Creates a new buddy item object from the given SSI item.
     *
     * @param item a "buddy" (type {@link SsiItem#TYPE_BUDDY}) SSI item
     */
    public BuddyItem(SsiItem item) {
        DefensiveTools.checkNull(item, "item");

        sn = item.getName();

        groupid = item.getParentId();
        id = item.getId();

        TlvChain chain = TlvTools.readChain(item.getData());

        alias = chain.getString(TYPE_ALIAS);
        comment = chain.getString(TYPE_COMMENT);
        alertSound = chain.getString(TYPE_ALERT_SOUND);

        Tlv alertTlv = chain.getLastTlv(TYPE_ALERT_FLAGS);

        if (alertTlv != null) {
            ByteBlock alertMaskData = alertTlv.getData();

            alertActionMask = BinaryTools.getUByte(alertMaskData, 0);
            alertWhenMask = BinaryTools.getUByte(alertMaskData, 1);

            if (alertActionMask == -1) alertActionMask = 0;
            if (alertWhenMask == -1) alertWhenMask = 0;
        } else {
            alertActionMask = 0;
            alertWhenMask = 0;
        }

        MutableTlvChain extraTlvs = TlvTools.getMutableCopy(chain);

        extraTlvs.removeTlvs(new int[] {
            TYPE_ALIAS, TYPE_COMMENT, TYPE_ALERT_SOUND, TYPE_ALERT_FLAGS
        });

        addExtraTlvs(extraTlvs);
    }

    /**
     * Creates a buddy item object with the same properties as the given object.
     *
     * @param other a buddy item object to copy
     */
    public BuddyItem(BuddyItem other) {
        this(other.sn, other.groupid, other.id, other.alias, other.comment,
                other.alertWhenMask, other.alertActionMask, other.alertSound,
                other.copyExtraTlvs());
    }

    /**
     * Creates a new buddy item with the given screenname, group ID, and buddy
     * ID. All other fields are set to <code>null</code> or <code>0</code>
     * depending on the field's type.
     *
     * @param sn the buddy's screenname
     * @param groupid the ID of the group in which this buddy resides
     * @param id the buddy's buddy ID
     */
    public BuddyItem(String sn, int groupid, int id) {
        this(sn, groupid, id, null, null, 0, 0, null);
    }

     /**
     * Creates a new buddy item object with the given properties. All fields
     * other than <code>sn</code>, <code>groupid</code>, and <code>id</code>
     * can be either <code>0</code> or <code>null</code> (depending on type) to
     * indicate that the given field should not be sent.
     *
     * @param sn the buddy's screenname
     * @param groupid the ID of the group in which this buddy resides
     * @param id the buddy's buddy ID
     * @param alias an "alias" or "display name" for this buddy (only supported
     *        by joscar and gaim)
     * @param comment a "buddy comment" for this buddy
     * @param alertWhenMask a set of bit flags indicating when a buddy alert
     *        should be activated (see the {@link
      *       #MASK_WHEN_ONLINE MASK_WHEN_<i>*</i>} constants)
     * @param alertActionMask a set of bit flags indicating what should happen
      *       when a buddy alert is activated (see the {@link
      *       #MASK_ACTION_POPUP MASK_ACTION_<i>*</i>} constants)
     * @param alertSound the name of a sound file to play when an alert is
      *       activated (normally stored without a full path or an extension,
      *       like "moo")
     */
    public BuddyItem(String sn, int groupid, int id, String alias,
            String comment, int alertWhenMask, int alertActionMask,
            String alertSound) {
        this(sn, groupid, id, alias, comment, alertWhenMask, alertActionMask,
                alertSound, null);
    }

    /**
     * Creates a new buddy item object with the given properties. All fields
     * other than <code>sn</code>, <code>groupid</code>, and <code>id</code>
     * can be either <code>0</code> or <code>null</code> (depending on type) to
     * indicate that the given field should not be sent.
     *
     * @param sn the buddy's screenname
     * @param groupid the ID of the group in which this buddy resides
     * @param id the buddy's buddy ID
     * @param alias an "alias" or "display name" for this buddy (only supported
     *        by joscar and gaim)
     * @param comment a "buddy comment" for this buddy
     * @param alertWhenMask a set of bit flags indicating when a buddy alert
     *        should be activated (see the {@link
     *       #MASK_WHEN_ONLINE MASK_WHEN_<i>*</i>} constants)
     * @param alertActionMask a set of bit flags indicating what should happen
     *       when a buddy alert is activated (see the {@link
     *       #MASK_ACTION_POPUP MASK_ACTION_<i>*</i>} constants)
     * @param alertSound the name of a sound file to play when an alert is
     *       activated (normally stored without a full path or an extension,
     *       like "moo")
     * @param extraTlvs a set of extra TLV's to store in this item
     */
    public BuddyItem(String sn, int groupid, int id, String alias,
            String comment, int alertWhenMask, int alertActionMask,
            String alertSound, TlvChain extraTlvs) {
        super(extraTlvs);

        DefensiveTools.checkNull(sn, "sn");
        DefensiveTools.checkRange(groupid, "groupid", 0);
        DefensiveTools.checkRange(id, "id", 0);
        DefensiveTools.checkRange(alertWhenMask, "alertWhenMask", 0);
        DefensiveTools.checkRange(alertActionMask, "alertActionMask", 0);

        this.sn = sn;
        this.groupid = groupid;
        this.id = id;
        this.alias = alias;
        this.comment = comment;
        this.alertActionMask = alertActionMask;
        this.alertWhenMask = alertWhenMask;
        this.alertSound = alertSound;
    }

    /**
     * Returns this buddy's screenname.
     *
     * @return this buddy's screenname
     */
    public final String getScreenname() { return sn; }

    /**
     * Returns the ID of the group in which this buddy resides.
     *
     * @return the ID of this buddy's parent group
     */
    public final int getGroupId() { return groupid; }

    /**
     * Returns the ID of this buddy in its parent group.
     *
     * @return this buddy's "buddy ID"
     */
    public final int getId() { return id; }


    /**
     * Returns this buddy's "alias" or "display name." Currently this feature
     * is only supported by joscar and gaim, but it's conceivable that WinAIM
     * will begin supporting it in the future.
     *
     * @return this buddy's "alias" or "display name," or <code>null</code> if
     *         this buddy has no alias
     */
    public synchronized final String getAlias() { return alias; }

    /**
     * Returns this buddy's "buddy comment." A buddy comment is a string of text
     * normally edited by the user to store some brief information about the
     * buddy. WinAIM puts a limit of 84 characters on this value, but there is
     * no hard (server-side) limit. Note that this value is stored as ASCII
     * text.
     *
     * @return this buddy's "buddy comment," or <code>null</code> if this buddy
     *         has no buddy comment
     */
    public synchronized final String getBuddyComment() { return comment; }

    /**
     * Returns a bit mask describing what should happen when a buddy alert is
     * activated for this buddy. Normally a combination of any of {@link
     * #MASK_ACTION_POPUP} and {@link #MASK_ACTION_PLAY_SOUND}. One can test
     * for a given value using code resembling the following:
     * <pre>
if ((buddyItem.getAlertActionMask() & BuddyItem.MASK_ACTION_POPUP) != 0) {
    // popup alert box
}
     * </pre>
     *
     * @return a set of bit flags describing what should happen when this
     *         buddy's buddy alert is activated
     */
    public synchronized final int getAlertActionMask() {
        return alertActionMask;
    }

    /**
     * Returns a bit mask describing when a buddy alert for this user should be
     * activated. Normally a combination of any of {@link #MASK_WHEN_ONLINE},
     * {@link #MASK_WHEN_UNAWAY}, and {@link #MASK_WHEN_UNIDLE}. One can test
     * for a given value using code resembling the following:
     * <pre>
if ((buddyItem.getAlertWhenMask() & BuddyItem.MASK_WHEN_ONLINE) != 0) {
    System.out.println("An alert should be triggered when "
            + buddyItem.getScreenname() + " signs on!");
}
     * </pre>
     *
     * @return a set of bit flags describing when a buddy alert for this user
     *         should be activated
     */
    public synchronized final int getAlertWhenMask() { return alertWhenMask; }

    /**
     * Returns the name of a sound file that should be played when this buddy's
     * alert is activated. The sound should only be played if this buddy's
     * {@linkplain #getAlertActionMask alert action mask} contains {@link
     * #MASK_ACTION_PLAY_SOUND}. The filename is normally stored without a full
     * path or file extension, like <code>"moo"</code> to represent
     * <code>C:\Program Files\AIM95\Sounds\moo.wav</code>.
     *
     * @return the name of a sound file that should be played when this buddy's
     *         alert is activated, or <code>null</code> if none is stored for
     *         this buddy
     *
     * @see #getAlertActionMask
     */
    public synchronized final String getAlertSound() { return alertSound; }

    /**
     * Sets this buddy's "alias" or "display name."
     *
     * @param alias this buddy's new "alias" or "display name," or
     *        <code>null</code> to erase this buddy's alias
     */
    public synchronized final void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * Sets this buddy's "buddy comment."
     *
     * @param comment this buddy's new "buddy comment," or <code>null</code> to
     *        erase this buddy's comment
     */
    public synchronized final void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Sets this buddy's "alert action mask." Normally a combination of any of
     * {@link #MASK_ACTION_POPUP} and {@link #MASK_ACTION_PLAY_SOUND}.
     *
     * @param alertActionMask a new "alert action mask" for this buddy
     */
    public synchronized final void setAlertActionMask(int alertActionMask) {
        this.alertActionMask = alertActionMask;
    }

    /**
     * Sets this buddy's "alert criteria mask." Normally a combination of any
     * of {@link #MASK_WHEN_ONLINE}, {@link #MASK_WHEN_UNAWAY}, and {@link
     * #MASK_WHEN_UNIDLE}.
     *
     * @param alertWhenMask a new "alert criteria mask" for this buddy
     */
    public synchronized final void setAlertWhenMask(int alertWhenMask) {
        this.alertWhenMask = alertWhenMask;
    }

    /**
     * Sets the "alert sound" filename for this buddy. This is normally stored
     * without full path or file extension, like <code>"moo"</code> to represent
     * <code>C:\Program Files\AIM95\Sounds\moo.wav</code>.
     *
     * @param alertSound the buddy's "alert sound filename," or
     *        <code>null</code> to erase any alert sound file currently stored
     *        in this item
     */
    public synchronized final void setAlertSound(String alertSound) {
        this.alertSound = alertSound;
    }


    public synchronized SsiItem toSsiItem() {
        MutableTlvChain chain = TlvTools.createMutableChain();

        if (alias != null) {
            chain.addTlv(Tlv.getStringInstance(TYPE_ALIAS, alias));
        }
        if (comment != null) {
            chain.addTlv(Tlv.getStringInstance(TYPE_COMMENT, comment));
        }
        if (alertActionMask != 0 || alertWhenMask != 0) {
            // this is the most elegant statement I've ever written.
            ByteBlock block = ByteBlock.wrap(new byte[] {
                BinaryTools.getUByte(alertActionMask)[0],
                BinaryTools.getUByte(alertWhenMask)[0]
            });
            chain.addTlv(new Tlv(TYPE_ALERT_FLAGS, block));
        }
        if (alertSound != null) {
            chain.addTlv(Tlv.getStringInstance(TYPE_ALERT_SOUND, alertSound));
        }

        return generateItem(sn, groupid, id, SsiItem.TYPE_BUDDY, chain);
    }

	@Override
    public synchronized String toString() {
        boolean popupAlert = (alertActionMask & MASK_ACTION_POPUP) != 0;
        boolean playSound = (alertActionMask & MASK_ACTION_PLAY_SOUND) != 0;

        boolean alertOnSignon = (alertWhenMask & MASK_WHEN_ONLINE) != 0;
        boolean alertOnUnidle = (alertWhenMask & MASK_WHEN_UNIDLE) != 0;
        boolean alertOnBack = (alertWhenMask & MASK_WHEN_UNAWAY) != 0;

        return "BuddyItem for " + sn + " (buddy 0x" + Integer.toHexString(id)
                + " in group 0x" + Integer.toHexString(groupid) + "): alias="
                + alias + ", comment=\"" + comment + "\", alerts: "
                + (popupAlert ? "[popup alert] " : "")
                + (playSound ? "[play " + alertSound + "] " : "")
                + (alertOnSignon ? "[on signon] " : "")
                + (alertOnUnidle ? "[on unidle] " : "")
                + (alertOnBack ? "[on unaway] " : "");
    }
}
