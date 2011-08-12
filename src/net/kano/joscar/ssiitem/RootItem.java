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
 * An SSI item object representing the "root group," or a "meta-group" that
 * contains all of the groups (and all other non-buddy items, implicitly). This
 * group has a group ID of <code>0</code>. You may ask why a group containing
 * all groups is necessary when one could just iterate through the SSI items
 * of type {@link SsiItem#TYPE_GROUP} to get a list of groups. The answer is
 * <i>ordering</i>: one can move a group up or down on his or her buddy list,
 * but this does not change the group item itself; rather, its position in this
 * root item's group list is changed.
 * <br>
 * <br>
 * Note that this class is only used to store data and that <b>changes to this
 * object are not reflected on the server</b> without sending the changes to the
 * server with a {@link net.kano.joscar.snaccmd.ssi.ModifyItemsCmd
 * ModifyItemsCmd}.
 */
public class RootItem extends AbstractItemObj {
    /** The item name used for the root item. */
    private static final String NAME_DEFAULT = "";
    /** The parent ID used for the root item. */
    private static final int PARENTID_DEFAULT = 0x0000;
    /** The ID used for the root item. */
    private static final int ID_DEFAULT = 0x0000;

    /** A TLV type containing the list of groups. */
    private static final int TYPE_GROUPIDS = 0x00c8;

    /** A list of the ID's of the groups in the buddy list. */
    private int[] groupids;

    /**
     * Creates a new root group item object generated from the data in the given
     * SSI item.
     *
     * @param item a root group SSI item
     */
    public RootItem(SsiItem item) {
        DefensiveTools.checkNull(item, "item");

        TlvChain chain = TlvTools.readChain(item.getData());

        Tlv groupTlv = chain.getLastTlv(TYPE_GROUPIDS);

        if (groupTlv != null) {
            ByteBlock groupBlock = groupTlv.getData();

            groupids = new int[groupBlock.getLength() / 2];

            for (int i = 0; i < groupids.length; i++) {
                groupids[i] = BinaryTools.getUShort(groupBlock, i*2);
            }
        } else {
            groupids = null;
        }

        MutableTlvChain extraTlvs = TlvTools.getMutableCopy(chain);

        extraTlvs.removeTlvs(new int[] { TYPE_GROUPIDS });

        addExtraTlvs(extraTlvs);
    }

    /**
     * Creates a new root group item object with the same properties as the
     * given item object.
     *
     * @param other a root group item object to copy
     */
    public RootItem(RootItem other) {
        this(other.groupids == null ? null : (int[]) other.groupids.clone(),
                other.copyExtraTlvs());
    }

    /**
     * Creates a new root group item object without any master group list.
     */
    public RootItem() {
        this(null, null);
    }

    /**
     * Creates a new root group item object with the given list of ID's of
     * groups on the buddy list.
     *
     * @param groupids a list of the group ID's of the groups on the buddy list
     */
    public RootItem(int[] groupids) {
        this(groupids, null);
    }

    /**
     * Creates a new root group item object with the given list of ID's of
     * groups on the buddy list.
     *
     * @param groupids a list of the group ID's of the groups on the buddy list
     * @param extraTlvs a list of extra TLV's to store in this item
     */
    public RootItem(int[] groupids, TlvChain extraTlvs) {
        super(extraTlvs);

        this.groupids = (int[]) (groupids == null ? null : groupids.clone());
    }

    /**
     * Returns a list of the group ID's of the groups on the buddy list, or
     * <code>null</code> if this item does not contain a master group list
     * field. The list of groups is in the order in which the groups should be
     * displayed to the user.
     *
     * @return the list of groups stored in this root group item, or
     *         <code>null</code> if this item contains no master group list
     *         field
     */
    public synchronized final int[] getGroupids() {
        return (int[]) groupids.clone();
    }

    /**
     * Sets the list of groups on the buddy list.
     *
     * @param groupids a list of the group ID's of the groups on the buddy list,
     *        in the order they should appear to the user
     */
    public synchronized final void setGroupids(int[] groupids) {
        this.groupids = (groupids == null ? null : (int[]) groupids.clone());
    }

    public synchronized SsiItem toSsiItem() {
        MutableTlvChain chain = TlvTools.createMutableChain();

        if (groupids != null) {
            ByteArrayOutputStream out
                = new ByteArrayOutputStream(groupids.length * 2);

            try {
                for (int i = 0; i < groupids.length; i++) {
                    BinaryTools.writeUShort(out, groupids[i]);
                }
            } catch (IOException impossible) { }

            ByteBlock tlvData = ByteBlock.wrap(out.toByteArray());

            chain.addTlv(new Tlv(TYPE_GROUPIDS, tlvData));
        }

        return generateItem(NAME_DEFAULT, PARENTID_DEFAULT, ID_DEFAULT,
                SsiItem.TYPE_GROUP, chain);
    }

    public synchronized String toString() {
        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < groupids.length; i++) {
            buffer.append("0x");
            buffer.append(Integer.toHexString(groupids[i]));
            buffer.append(", ");
        }

        return "RootItem with groupids: " + buffer;
    }
}
