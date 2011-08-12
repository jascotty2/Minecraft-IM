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

package net.kano.joscar.tlv;

import net.kano.joscar.DefensiveTools;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A default implementation of a (thread-safe) mutable TLV chain.
 */
public class DefaultMutableTlvChain
        extends AbstractTlvChain implements MutableTlvChain {
    /** A list of the TLV's in this chain, in order. */
    private final List tlvList = new LinkedList();
    /**
     * A map from TLV type codes to <code>List</code>s of the TLV's in this
     * chain with that type.
     */
    private final Map tlvMap = new HashMap();

    /**
     * Creates an empty TLV chain.
     */
    protected DefaultMutableTlvChain() { }

    /**
     * Creates a TLV chain containing the same TLV's as the given chain.
     *
     * @param other a TLV chain to copy
     *
     * @see #addAll
     */
    protected DefaultMutableTlvChain(TlvChain other) {
        copy(other);
    }

    public synchronized final void addTlv(Tlv tlv) {
        addTlvImpl(tlv);
    }

    public synchronized final void replaceTlv(Tlv tlv) {
        DefensiveTools.checkNull(tlv, "tlv");

        int typeCode = tlv.getType();
        Integer type = new Integer(typeCode);
        List tlvs = (List) getTlvMap().get(type);

        int insertAt = -1;
        if (tlvs == null) {
            tlvs = new LinkedList();
            getTlvMap().put(type, tlvs);
        } else if (!tlvs.isEmpty()) {
            // find the first instance of a tlv of this type
            int i = 0;
            for (Iterator it = getTlvList().iterator(); it.hasNext(); i++) {
                Tlv next = (Tlv) it.next();
                if (next.getType() == typeCode) {
                    // we found one!
                    if (insertAt == -1) insertAt = i;
                    it.remove();
                }
            }

            tlvs.clear();
        }
        if (insertAt == -1) insertAt = getTlvList().size();

        tlvs.add(tlv);
        getTlvList().add(insertAt, tlv);
    }

    public synchronized final void removeTlv(Tlv tlv) {
        DefensiveTools.checkNull(tlv, "tlv");

        int typeCode = tlv.getType();
        Integer type = new Integer(typeCode);
        List tlvs = (List) getTlvMap().get(type);

        if (tlvs != null) while (tlvs.remove(tlv));
        while (getTlvList().remove(tlv));
    }

    public synchronized final void removeTlvs(int type) {
        DefensiveTools.checkRange(type, "type", 0);

        Integer typeKey = new Integer(type);
        List tlvs = (List) getTlvMap().remove(typeKey);

        if (tlvs != null) getTlvList().removeAll(tlvs);
    }

    public synchronized final void removeTlvs(int[] types) {
        DefensiveTools.checkNull(types, "types");

        types = (int[]) types.clone();

        for (int i = 0; i < types.length; i++) {
            DefensiveTools.checkRange(types[i], "types[] elements", 0);
        }

        for (int i = 0; i < types.length; i++) {
            removeTlvs(types[i]);
        }
    }

    public synchronized final void addAll(TlvChain other) {
        DefensiveTools.checkNull(other, "other");

        List tlvs;
        if (other instanceof AbstractTlvChain) {
            tlvs = ((AbstractTlvChain) other).getTlvList();
        } else {
            tlvs = Arrays.asList(other.getTlvs());
        }
        for (Iterator it = tlvs.iterator(); it.hasNext();) {
            Tlv tlv = (Tlv) it.next();

            addTlvImpl(tlv);
        }
    }

    public synchronized final void replaceAll(TlvChain other) {
        DefensiveTools.checkNull(other, "other");

        List tlvs;
        if (other instanceof AbstractTlvChain) {
            tlvs = ((AbstractTlvChain) other).getTlvList();
        } else {
            tlvs = Arrays.asList(other.getTlvs());
        }
        for (Iterator it = tlvs.iterator(); it.hasNext();) {
            Tlv tlv = (Tlv) it.next();

            replaceTlv(tlv);
        }
    }

    protected synchronized List getTlvList() { return tlvList; }

    protected synchronized Map getTlvMap() { return tlvMap; }
}
