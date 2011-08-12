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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * An SSI item object representing a "buddy group," or a group of buddies. These
 * are just your normal buddy list groups.
 * <br>
 * <br>
 * Note that this class is only used to store data and that <b>changes to this
 * object are not reflected on the server</b> without sending the changes to the
 * server with a {@link net.kano.joscar.snaccmd.ssi.ModifyItemsCmd
 * ModifyItemsCmd}.
 */
public class GroupItem extends AbstractItemObj {
    /** The ID used for group items. */
    private static final int ID_DEFAULT = 0x0000;

    /** A TLV type containing the list of buddies in a group. */
    private static final int TYPE_BUDDIES = 0x00c8;

    /** This group's name. */
    protected final String name;
    /** This group's group ID. */
    protected final int id;
    /** The ID's of the buddies in this group. */
    protected int[] buddies;

    /**
     * Creates a new buddy group item object from the data in the given SSI
     * item.
     *
     * @param item the buddy group SSI item
     */
    public GroupItem(SsiItem item) {
        DefensiveTools.checkNull(item, "item");

        name = item.getName();

        id = item.getParentId();

        TlvChain chain = TlvTools.readChain(item.getData());

        Tlv buddiesTlv = chain.getLastTlv(TYPE_BUDDIES);

        if (buddiesTlv != null) {
            ByteBlock buddyBlock = buddiesTlv.getData();

            buddies = new int[buddyBlock.getLength() / 2];

            for (int i = 0; i < buddies.length; i++) {
                buddies[i] = BinaryTools.getUShort(buddyBlock, i*2);
            }
        } else {
            buddies = null;
        }

        MutableTlvChain extraTlvs = TlvTools.getMutableCopy(chain);

        extraTlvs.removeTlvs(new int[] { TYPE_BUDDIES });

        addExtraTlvs(extraTlvs);
    }

    /**
     * Creates a new buddy group item object with the same properties as the
     * given object.
     *
     * @param other a buddy group item object to copy
     */
    public GroupItem(GroupItem other) {
        this(other.name, other.id,
                other.buddies == null ? null : other.buddies.clone(),
                other.copyExtraTlvs());
    }

    /**
     * Creates a new buddy group item object with the given group name and the
     * given group ID. The group is created with no buddies.
     *
     * @param name the name of this group, like "Family"
     * @param id a unique group ID for this group
     */
    public GroupItem(String name, int id) {
        this(name, id, null, null);
    }

    /**
     * Creates a new buddy group item object with the given group name, group
     * ID, and list of "child" buddy ID's.
     *
     * @param name the name of this group, like "Family"
     * @param id a unique group ID for this group
     * @param buddies a list of the ID's of buddies in this group
     */
    public GroupItem(String name, int id, int[] buddies) {
        this(name, id, buddies, null);
    }

    /**
     * Creates a new buddy group item object with the given group name, group
     * ID, and list of "child" buddy ID's.
     *
     * @param name the name of this group, like "Family"
     * @param id a unique group ID for this group
     * @param buddies a list of the ID's of buddies in this group
     * @param extraTlvs a list of extra TLV's to store in this item
     */
    public GroupItem(String name, int id, int[] buddies, TlvChain extraTlvs) {
        super(extraTlvs);

        DefensiveTools.checkNull(name, "name");
        DefensiveTools.checkRange(id, "id", 0);

        this.name = name;
        this.id = id;
        this.buddies = buddies == null ? null : buddies.clone();
    }

    /**
     * Returns the name of this group, like "Family."
     *
     * @return this group's name
     */
    public final String getGroupName() { return name; }

    /**
     * Returns the group ID of this group. This ID is unique in the set of
     * group ID's in this user's server-stored information block.
     *
     * @return this group's group ID
     */
    public final int getId() { return id; }

    /**
     * Returns a list of the ID's of the buddies in this group. Note that this
     * will be <code>null</code> if this item has no child buddy field.
     *
     * @return a list of the ID's of the buddies in this group, or
     *         <code>null</code> if this group item does not contain a child
     *         buddy field
     */
    public synchronized final int[] getBuddies() {
        return buddies == null ? null : buddies.clone();
    }

    /**
     * Sets the buddies in this group. The given list should contain the
     * {@linkplain BuddyItem#getId buddy ID's} of the buddies in this group.
     * This can be <code>null</code> to not store a child buddy list in this
     * item at all.
     *
     * @param buddies a list of the ID's of the buddies in this group, or
     *        <code>null</code> to erase this group's child buddy list
     */
    public synchronized final void setBuddies(int[] buddies) {
        this.buddies = buddies;
    }

    public synchronized SsiItem toSsiItem() {
        MutableTlvChain chain = TlvTools.createMutableChain();

        if (buddies != null) {
            ByteArrayOutputStream out
                    = new ByteArrayOutputStream(buddies.length * 2);

            try {
                for (int i = 0; i < buddies.length; i++) {
                    BinaryTools.writeUShort(out, buddies[i]);
                }
            } catch (IOException impossible) { }

            ByteBlock tlvData = ByteBlock.wrap(out.toByteArray());
            chain.addTlv(new Tlv(TYPE_BUDDIES, tlvData));
        }

        return generateItem(name, id, ID_DEFAULT, SsiItem.TYPE_GROUP,
                chain);
    }

	@Override
    public synchronized String toString() {
        String buddyStr;
        if (buddies != null) {
            StringBuilder buffer = new StringBuilder();
            for (int i = 0; i < buddies.length; i++) {
                buffer.append("0x");
                buffer.append(Integer.toHexString(buddies[i]));
                buffer.append(", ");
            }
            buddyStr = buffer.toString();
        } else {
            buddyStr = "none";
        }
        return "GroupItem for " + name + ", groupid=0x"
                + Integer.toHexString(id) + ", buddies: " + buddyStr;
    }
}
