/*
 *  Copyright (c) 2003, The Joust Project
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
 *  File created by Keith @ 4:22:26 PM
 *
 */
package net.kano.joscar.tlv;

import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.BinaryTools;

/**
 * Provides a set of utilites for working with TLV chains.
 */
public final class TlvTools {
    /**
     * Ensures that this class is never instantiated.
     */
    private TlvTools() { }

    /**
     * Reads a TLV chain from the given block of TLV's. Calling this method is
     * equivalent to calling {@link #readChain(ByteBlock, int)
     * readChain(block, -1)}. The total number of bytes read can be read by
     * calling the <code>getTotalSize</code> method of the returned
     * <code>TlvChain</code>.
     *
     * @param block the data block containing zero or more TLV's
     * @return a TLV chain object containing TLV's read from the given block of
     *         data
     */
    public static ImmutableTlvChain readChain(ByteBlock block) {
        return readChain(block, -1);
    }

    /**
     * Reads a TLV chain from the given block of TLV's, stopping after reading
     * the number of TLV's specified by <code>maxTlvs</code>. If
     * <code>maxTlvs</code> is <code>-1</code>, all possible TLV's are read. The
     * total number of bytes read can be read by calling the
     * <code>getTotalSize</code> method of the returned <code>TlvChain</code>.
     *
     * @param block block the data block containing zero or more TLV's
     * @param maxTlvs the maximum number of TLV's to read, or <code>-1</code> to
     *        read all possible TLV's in the given block
     * @return an <i>immutable</i> TLV chain object containing TLV's read from
     *         the given block of data
     */
    public static ImmutableTlvChain readChain(ByteBlock block, int maxTlvs) {
        return new ImmutableTlvChain(block, maxTlvs);
    }

    /**
     * Creates a new immutable TLV chain with the given number of TLV's starting
     * from the given offset of the given TLV array. Note that no element of
     * <code>tlvs</code> can be <code>null</code>.
     *
     * @param tlvs the list of (non-<code>null</code>) TLV's
     * @param offset the index of the first TLV that this chain should contain
     * @param len the number of TLV's to include in this chain
     * @return a TLV chain containing the given number of TLV's starting at the
     *         given index of the given array of TLV's
     */
    public static ImmutableTlvChain createChain(Tlv[] tlvs, int offset,
            int len) {
        DefensiveTools.checkNull(tlvs, "tlvs");

        if (offset < 0 || len < 0 || offset + len > tlvs.length) {
            throw new ArrayIndexOutOfBoundsException("offset=" + offset
                    + ", len=" + len + ", tlvs.length=" + tlvs.length);
        }

        DefensiveTools.checkNullElements(tlvs, "tlvs", offset, len);

        return new ImmutableTlvChain(tlvs, offset, len);
    }

    /**
     * Returns a new empty mutable TLV chain.
     *
     * @return a new empty mutable TLV chain
     */
    public static MutableTlvChain createMutableChain() {
        return new DefaultMutableTlvChain();
    }

    /**
     * Returns a copy of the given TLV chain that can be modified.
     * (Modifications to the returned chain will not be reflected in the
     * original chain.)
     *
     * @param other the TLV chain to copy
     * @return a mutable TLV chain containing the same TLV's as the given chain
     */
    public static MutableTlvChain getMutableCopy(TlvChain other) {
        return new DefaultMutableTlvChain(other);
    }

    /**
     * Returns a copy of the given TLV chain that cannot be modified.
     * (Modifications to the original chain will not be reflected in the
     * returned chain.)
     *
     * @param other the TLV chain to copy
     * @return an immutable TLV chain containing the same TLV's as the given
     *         chain
     */
    public static ImmutableTlvChain getImmutableCopy(TlvChain other) {
        return new ImmutableTlvChain(other);
    }

    /**
     * Returns whether the given block might be a valid, complete TLV chain. A
     * complete TLV chain contains no data after the chain.
     *
     * @param block a block of data that might contain a complete TLV chain
     * @return whether or not the given block represents a complete TLV chain
     */
    public static boolean isCompleteTlvChain(ByteBlock block) {
        final int len = block.getLength();

        for (int i = 0; i < len;) {
            if (i + 3 >= len) return false;

            int tlvlen = BinaryTools.getUShort(block, i + 2);
            i += 2 + tlvlen;

            if (i == len) return true;
            if (i > len) return false;
        }

        // we should never get here
        throw new IllegalStateException();
    }
}