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

import net.kano.joscar.DefensiveTools;
import net.kano.joscar.MiscTools;
import net.kano.joscar.snaccmd.ssi.SsiItem;
import net.kano.joscar.tlv.ImmutableTlvChain;
import net.kano.joscar.tlv.TlvChain;
import net.kano.joscar.tlv.TlvTools;

/**
 * A base class for the two item types that only contain a name and reside in
 * the root group (group <code>0x0000</code>). These are {@link PermitItem} and
 * {@link DenyItem}.
 */
public abstract class SimpleNamedItem extends AbstractItemObj {
    /** The parent ID to use in simple named items. */
    private static final int PARENTID_DEFAULT = 0;

    /** The user's screenname. */
    private final String sn;
    /** The item ID. */
    private final int id;

    /**
     * Creates a new simple named item object generated from the data in the
     * given SSI item block.
     *
     * @param item a simple named SSI item
     */
    protected SimpleNamedItem(SsiItem item) {
        this(item.getName(), item.getId(),
                TlvTools.readChain(item.getData()));
    }

    /**
     * Creates a new simple named item object with the same properties as the
     * given item.
     *
     * @param other a simple named item object
     */
    protected SimpleNamedItem(SimpleNamedItem other) {
        this(other.sn, other.id, other.copyExtraTlvs());
    }

    /**
     * Creates a new simple named item object with the given screenname and the
     * given item ID.
     *
     * @param sn the screenname for this item
     * @param id this item's SSI item ID
     */
    protected SimpleNamedItem(String sn, int id) {
        this(sn, id, null);
    }

    /**
     * Creates a new simple named item object with the given screenname and the
     * given item ID.
     *
     * @param sn the screenname for this item
     * @param id this item's SSI item ID
     * @param extraTlvs a list of extra TLV's to store in this item
     */
    protected SimpleNamedItem(String sn, int id, TlvChain extraTlvs) {
        super(extraTlvs);

        DefensiveTools.checkNull(sn, "sn");
        DefensiveTools.checkRange(id, "id", 0);

        this.sn = sn;
        this.id = id;
    }

    /**
     * Returns this item's screenname.
     *
     * @return this item's screenname
     */
    public final String getScreenname() { return sn; }

    /**
     * Returns the SSI item ID of this simple named item object.
     *
     * @return this item's SSI item ID
     */
    public final int getId() { return id; }

    /**
     * Returns the SSI item type of this item. This should normally return one
     * of the {@linkplain SsiItem#TYPE_BUDDY <code>SsiItem.TYPE_<i>*</i></code>
     * constants}.
     *
     * @return the SSI item type of this item
     */
    protected abstract int getItemType();

    public SsiItem toSsiItem() {
        return generateItem(sn, PARENTID_DEFAULT, id, getItemType(), null);
    }

    public String toString() {
        return MiscTools.getClassName(this) + " for " + sn + " (id=0x"
                + Integer.toHexString(id) + ")";
    }
}
