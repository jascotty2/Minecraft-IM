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
 *  File created by keith @ Mar 30, 2003
 *
 */

package net.kano.joscar.ssiitem;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.snaccmd.ssi.SsiItem;
import net.kano.joscar.tlv.*;

/**
 * An SSI item object representing a set of "visibility settings." The
 * difference between the types of data stored in this type of item and those
 * stored in {@link PrivacyItem} is very fine; in fact, in this author's
 * opinion, they should be combined.
 * <br>
 * <br>
 * Note that this class is only used to store data and that <b>changes to this
 * object are not reflected on the server</b> without sending the changes to the
 * server with a {@link net.kano.joscar.snaccmd.ssi.ModifyItemsCmd
 * ModifyItemsCmd}.
 */
public class VisibilityItem extends AbstractItemObj {
    /**
     * A visibility settings flag indicating that the user's idle time should be
     * visible to other AIM users.
     */
    public static final long MASK_SHOW_IDLE_TIME = 0x00000400L;
    /**
     * A visibility settings flag indicating that the user's {@linkplain
     * net.kano.joscar.snaccmd.icbm.SendTypingNotification typing status}
     * should be visible to other users.
     *
     * @see net.kano.joscar.snaccmd.icbm.SendTypingNotification
     */
    public static final long MASK_SHOW_TYPING =    0x00400000L;

    /** A TLV type containing a bitwise set of visibility flags. */
    private static final int TYPE_VIS_MASK = 0x00c9;

    /** The SSI item name to use for visibility items. */
    private static final String NAME_DEFAULT = "";
    /** The parent ID to use for visibility items. */
    private static final int GROUPID_DEFAULT = 0x0000;

    /** The item ID of this item. */
    private final int id;
    /** The set of visibility flags. */
    private long flags;

    /**
     * Creates a new visibility settings item object generated from from the
     * data in the given SSI item block.
     *
     * @param item a visibility settings SSI item
     */
    public VisibilityItem(SsiItem item) {
        DefensiveTools.checkNull(item, "item");

        id = item.getId();

        TlvChain chain = TlvTools.readChain(item.getData());

        Tlv visMaskTlv = chain.getLastTlv(TYPE_VIS_MASK);
        if (visMaskTlv != null) {
            flags = visMaskTlv.getDataAsUInt();
            if (flags == -1) flags = 0;
        }
        else {
            flags = 0;
        }

        MutableTlvChain extras = TlvTools.getMutableCopy(chain);
        extras.removeTlvs(new int[] { TYPE_VIS_MASK });
        addExtraTlvs(extras);
    }

    /**
     * Creates a new visibility settings item object with the same properties
     * as the given item object.
     *
     * @param other a visibility settings item object to copy
     */
    public VisibilityItem(VisibilityItem other) {
        this(other.id, other.flags, other.copyExtraTlvs());
    }

    /**
     * Creates a new visibility settings item object with the given SSI item ID
     * and the given set of visibility flags.
     *
     * @param id the SSI item ID for this item
     * @param flags the set of visibility flags, like <code>{@link
     *        #MASK_SHOW_IDLE_TIME} | {@link #MASK_SHOW_TYPING}</code>
     */
    public VisibilityItem(int id, long flags) {
        this(id, flags, null);
    }

    /**
     * Creates a new visibility settings item object with the given SSI item ID
     * and the given set of visibility flags.
     *
     * @param id the SSI item ID for this item
     * @param flags the set of visibility flags, like <code>{@link
     *        #MASK_SHOW_IDLE_TIME} | {@link #MASK_SHOW_TYPING}</code>
     * @param extraTlvs a list of extra TLV's to store in this item
     */
    public VisibilityItem(int id, long flags, TlvChain extraTlvs) {
        super(extraTlvs);

        DefensiveTools.checkRange(id, "id", 0);
        DefensiveTools.checkRange(flags, "flags", 0);

        this.id = id;
        this.flags = flags;
    }

    /**
     * Returns the SSI item ID of this visibility settings item.
     *
     * @return this item's SSI item ID
     */
    public final int getId() { return id; }

    /**
     * Returns the set of visibility flags. Normally a bitwise combination of
     * any of {@link #MASK_SHOW_IDLE_TIME} and {@link #MASK_SHOW_TYPING}. To
     * check for a specific flag, you could use code resembling the following:
     * <pre>
if ((visItem.getVisFlags() & VisibilityItem.MASK_SHOW_TYPING) {
    System.out.println("We should send typing notifications to buddies!");
}
     * </pre>
     *
     * @return the set of visibility flags stored in this item
     */
    public synchronized final long getVisFlags() { return flags; }

    /**
     * Sets the user's visibility flags. See {@link #getVisFlags} for details
     * on this value's meaning.
     *
     * @param flags this item's new visibility flags, like <code>{@link
     *        #MASK_SHOW_IDLE_TIME} | {@link #MASK_SHOW_TYPING}</code>
     */
    public synchronized final void setVisFlags(long flags) {
        this.flags = flags;
    }

    public synchronized SsiItem toSsiItem() {
        MutableTlvChain chain = TlvTools.createMutableChain();

        if (flags != 0) {
            byte[] flagBytes = BinaryTools.getUInt(flags);
            ByteBlock flagsBlock = ByteBlock.wrap(flagBytes);
            chain.addTlv(new Tlv(TYPE_VIS_MASK, flagsBlock));
        }

        return generateItem(NAME_DEFAULT, GROUPID_DEFAULT, id,
                SsiItem.TYPE_VISIBILITY, chain);
    }

    public synchronized String toString() {
        return "VisibilityItem: id=0x" + Integer.toHexString(id) + ", flags=0x"
                + Long.toHexString(flags);
    }
}
