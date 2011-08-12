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
 *  File created by keith @ Apr 22, 2003
 *
 */

package net.kano.joscar.tlv;

import net.kano.joscar.Writable;

/**
 * Provides an interface for a TLV chain whose contents can be modified.
 */
public interface MutableTlvChain extends TlvChain, Writable {
    /**
     * Adds the given TLV to this chain.
     *
     * @param tlv the TLV to add
     */
    void addTlv(Tlv tlv);

    /**
     * Removes all TLV's of the given type from the chain, and inserts the given
     * TLV at the index of the first TLV removed, or at the end of the chain if
     * no TLV's of the same type were found.
     *
     * @param tlv the TLV to replace its "siblings" of the same TLV type
     */
    void replaceTlv(Tlv tlv);

    /**
     * Removes the given TLV from the chain, if it is present.
     *
     * @param tlv the TLV to remove
     */
    void removeTlv(Tlv tlv);

    /**
     * Removes all TLV's in this chain of the given TLV type.
     *
     * @param type the type of TLV of which to remove all instances
     */
    void removeTlvs(int type);

    /**
     * Removes all TLV's in this chain having any of the given types.
     *
     * @param types the TLV types of which to remove all instances
     */
    void removeTlvs(int[] types);

    /**
     * Adds all TLV's in the given chain to the end of this chain (preserving
     * order).
     *
     * @param other the chain whose TLV's will be appended to this chain
     */
    void addAll(TlvChain other);

    /**
     * Deletes all TLV's currently in this chain having the same type as any of
     * the TLV's in the other chain, and replaces them with their counterparts
     * in the given chain. Behavior is undefined if the given chain contains two
     * or more TLV's of the same type. Any TLV's in the given chain without
     * counterparts in this chain will be appended to the end of this chain.
     *
     * @param other the TLV whose TLV's will replace and/or add to TLV's in this
     *        chain
     */
    void replaceAll(TlvChain other);
}
