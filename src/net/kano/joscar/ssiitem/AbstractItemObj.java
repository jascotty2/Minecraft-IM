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
import net.kano.joscar.snaccmd.ssi.SsiItem;
import net.kano.joscar.tlv.DefaultMutableTlvChain;
import net.kano.joscar.tlv.MutableTlvChain;
import net.kano.joscar.tlv.TlvChain;
import net.kano.joscar.tlv.TlvTools;

/**
 * A base class for each of the item object classes provided in this package.
 */
public abstract class AbstractItemObj implements SsiItemObj {
    /**
     * Returns a list of <code>SsiItem</code>s generated from the item objects
     * given in <code>itemObjs</code>.
     *
     * @param itemObjs a list of item objects to use in generating the returned
     *        list of <code>SsiItem</code>s
     * @return a list of <code>SsiItem</code>s generated from the given list of
     *         item objects
     *
     * @see #toSsiItem
     */
    public static SsiItem[] generateSsiItems(SsiItemObj[] itemObjs) {
        DefensiveTools.checkNull(itemObjs, "itemObjs");

        SsiItem[] items = new SsiItem[itemObjs.length];
        for (int i = 0; i < itemObjs.length; i++) {
            items[i] = itemObjs[i].toSsiItem();
        }

        return items;
    }

    /** The "extra TLV's" in this item. This is never <code>null</code>*/
    private final MutableTlvChain extraTlvs = TlvTools.createMutableChain();

    /**
     * Creates a new item object with no extra TLV's. Using this constructor is
     * equivalent to using {@link #AbstractItemObj(TlvChain) new
     * AbstractItemObj(null)}.
     */
    protected AbstractItemObj() {
        this(null);
    }

    /**
     * Creates a new item object with the given set of unprocessed or otherwise
     * unrecognized TLV's in this item's type-specific TLV list.
     *
     * @param extraTlvs the extra TLV's in this item
     */
    protected AbstractItemObj(TlvChain extraTlvs) {
        if (extraTlvs != null) addExtraTlvs(extraTlvs);
    }

    /**
     * Returns a copy of this item's extra TLV's, or <code>null</code> if this
     * item's extra TLV list is <code>null</code>.
     *
     * @return a copy of this item's extra TLV's
     */
    protected final TlvChain copyExtraTlvs() {
        return TlvTools.getMutableCopy(extraTlvs);
    }

    public final MutableTlvChain getExtraTlvs() {
        return extraTlvs;
    }

    /**
     * Adds the given list of TLV's to this item's list of extra TLV's. Using
     * this method is equivalent to using <code>{@link #getExtraTlvs()
     * getExtraTlvs()}.{@link MutableTlvChain#addAll(TlvChain)
     * addAll}(extraTlvs)</code>.
     *
     * @param extraTlvs the list of TLV's to append to this object's list of
     *        extra TLV's
     */
    protected final void addExtraTlvs(TlvChain extraTlvs) {
        this.extraTlvs.addAll(extraTlvs);
    }

    /**
     * Generates a new <code>SsiItem</code> from this item object with the given
     * properties.
     *
     * @param name the name of the item
     * @param parentid the "parent ID" of this item
     * @param subid the "sub ID" of this item in its parent
     * @param type the type of item, like {@link SsiItem#TYPE_PRIVACY}
     * @param customTlvs a list of TLV's to insert into the type-specific data
     *        block of the returned item
     * @return a new SSI item with the given properties
     */
    protected final SsiItem generateItem(String name, int parentid, int subid,
            int type, TlvChain customTlvs) {
        MutableTlvChain chain = TlvTools.getMutableCopy(extraTlvs);
        if (customTlvs != null) chain.replaceAll(customTlvs);

        return new SsiItem(name, parentid, subid, type,
                ByteBlock.createByteBlock(chain));
    }

}
