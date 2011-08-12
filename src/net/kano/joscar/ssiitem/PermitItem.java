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
 *  File created by keith @ Mar 27, 2003
 *
 */

package net.kano.joscar.ssiitem;

import net.kano.joscar.snaccmd.ssi.SsiItem;
import net.kano.joscar.tlv.TlvChain;

/**
 * An SSI item object representing a user on one's "allow" (or "permit" list.
 * The allow list is an infrequently used privacy feature that allows one to
 * block everyone <i>but</i> those on this list.
 * <br>
 * <br>
 * Note that this class is only used to store data and that <b>changes to this
 * object are not reflected on the server</b> without sending the changes to the
 * server with a {@link net.kano.joscar.snaccmd.ssi.ModifyItemsCmd
 * ModifyItemsCmd}.
 */
public class PermitItem extends SimpleNamedItem {
    /**
     * Creates a new allowed user item object generated from the data in the
     * given SSI item.
     *
     * @param item a "permit" SSI item
     */
    public PermitItem(SsiItem item) {
        super(item);
    }

    /**
     * Creates a new allowed user item object with the same properties as the
     * given object.
     *
     * @param other a permit item object to copy
     */
    public PermitItem(PermitItem other) {
        super(other);
    }

    /**
     * Creates a new allowed user item object for the given user and with the
     * given permit item ID. Note that the given ID is in no way related to the
     * {@linkplain BuddyItem#getId buddy ID} of the user.
     *
     * @param sn the screenname of the user being "allowed"
     * @param id an ID number for this permit item
     */
    public PermitItem(String sn, int id) {
        super(sn, id);
    }

    /**
     * Creates a new allowed user item object for the given properties.. Note
     * that the given ID is in no way related to the {@linkplain
     * BuddyItem#getId buddy ID} of the user.
     *
     * @param sn the screenname of the user being "allowed"
     * @param id an ID number for this permit item
     * @param extraTlvs a list of extra TLV's to store in this permit item
     */
    public PermitItem(String sn, int id, TlvChain extraTlvs) {
        super(sn, id, extraTlvs);
    }

    protected int getItemType() {
        return SsiItem.TYPE_PERMIT;
    }
}
