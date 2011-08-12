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
 * An SSI item object representing a user who has been "blocked," or "denied."
 * <br>
 * <br>
 * Note that this class is only used to store data and that <b>changes to this
 * object are not reflected on the server</b> without sending the changes to the
 * server with a {@link net.kano.joscar.snaccmd.ssi.ModifyItemsCmd
 * ModifyItemsCmd}.
 */
public class DenyItem extends SimpleNamedItem {
    /**
     * Creates a new blocked user item object generated from the given SSI item.
     *
     * @param item a new deny item object generated from the data in the given
     *        SSI item
     */
    public DenyItem(SsiItem item) {
        super(item);
    }

    /**
     * Creates a new blocked user item object with the same properties as the
     * given deny item.
     *
     * @param other a deny item to copy
     */
    public DenyItem(DenyItem other) {
        super(other);
    }

    /**
     * Creates a new blocked user item object representing the given user and
     * with the given blocked user ID. Note that this ID is <i>not</i> related
     * to the user's {@linkplain BuddyItem#getId buddy ID}; the given ID is a
     * unique ID for this deny item.
     *
     * @param sn the screenname of the user to block
     * @param id a unique item ID for this deny item
     */
    public DenyItem(String sn, int id) {
        super(sn, id);
    }


    /**
     * Creates a new blocked user item object representing the given user and
     * with the given blocked user ID and with the given set of additional TLV's
     * to store in this item. Note that this ID is <i>not</i> related to the
     * user's {@linkplain BuddyItem#getId buddy ID}; the given ID is a unique
     * ID for this deny item.
     *
     * @param sn the screenname of the user to block
     * @param id a unique item ID for this deny item
     * @param extraTlvs a list of extra TLV's to store in this item
     */
    public DenyItem(String sn, int id, TlvChain extraTlvs) {
        super(sn, id, extraTlvs);
    }

    protected final int getItemType() { return SsiItem.TYPE_DENY; }
}
