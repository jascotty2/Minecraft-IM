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
 *  File created by keith @ Apr 21, 2003
 *
 */

package net.kano.joscar.tlv;

import net.kano.joscar.ByteBlock;

import java.util.*;

/**
 * An immutable TLV chain, a TLV chain that cannot be modified after its
 * creation. 
 */
public final class ImmutableTlvChain extends AbstractTlvChain {
    /** A list of the TLV's in this chain, in order. */
    private final List tlvList = new LinkedList();
    /**
     * A map from TLV type codes to <code>List</code>s of the TLV's in this
     * chain with that type.
     */
    private final Map tlvMap = new HashMap();

    /**
     * Creates a new immutable TLV chain with the given number of TLV's starting
     * from the given offset of the given TLV array.
     *
     * @param tlvs the list of TLV's
     * @param offset the index of the first TLV that this chain should contain
     * @param len the number of TLV's to include in this chain
     */
    ImmutableTlvChain(Tlv[] tlvs, int offset, int len) {
        List list = Arrays.asList(tlvs).subList(offset, offset + len);
        for (Iterator it = list.iterator(); it.hasNext();) {
            addTlvImpl((Tlv) it.next());
        }
    }

    /**
     * Reads a TLV chain from the given block of TLV's, stopping after reading
     * the number of TLV's specified by <code>maxTlvs</code>. If
     * <code>maxTlvs</code> is <code>-1</code>, all possible TLV's are read. The
     * total number of bytes read can be read by calling
     * <code>getTotalSize</code>.
     *
     * @param block block the data block containing zero or more TLV's
     * @param maxTlvs the maximum number of TLV's to read, or <code>-1</code> to
     *        read all possible TLV's in the given block
     */
    ImmutableTlvChain(ByteBlock block, int maxTlvs) {
        initFromBlock(block, maxTlvs);
    }

    /**
     * Creates a TLV chain containing the same TLV's as the given chain, in the
     * same order.
     *
     * @param other a TLV chain to copy
     */
    ImmutableTlvChain(TlvChain other) {
        copy(other);
    }

    protected final List getTlvList() { return tlvList; }

    protected final Map getTlvMap() { return tlvMap; }
}
