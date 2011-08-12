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

import net.kano.joscar.Writable;

import java.util.Iterator;

/**
 * Represents a "chain," or block, or sequence, of {@link Tlv}s.
 */
public interface TlvChain extends Writable {
    /**
     * Returns <code>true</code> if this TLV chain contains any TLV's of the
     * given TLV type.
     *
     * @param type a TLV type
     * @return whether or not this chain contains one or more TLV's of the given
     *         type
     */
    boolean hasTlv(int type);

    /**
     * Returns an array of all TLV's in this chain, in order.
     *
     * @return all of this chain's TLV's
     */
    Tlv[] getTlvs();

    /**
     * Returns an iterator over the TLV's in this TLV chain.
     *
     * @return an iterator traversing over the TLV's in this chain
     */
    Iterator iterator();

    /**
     * Returns the number of TLV's in this chain.
     *
     * @return the number of TLV's in this chain
     */
    int getTlvCount();

    /**
     * Returns the first TLV in this chain with the given type, or
     * <code>null</code> of TLV of the given type is present.
     *
     * @param type the type of TLV whose first match will be returned
     * @return the first TLV in this chain with the given type, or
     *         <code>null</code> if none was found
     */
    Tlv getFirstTlv(int type);

    /**
     * Returns the last TLV in this chain with the given type, or
     * <code>null</code> of TLV of the given type is present.
     *
     * @param type the type of TLV whose last match will be returned
     * @return the last TLV in this chain with the given type, or
     *         <code>null</code> if none was found
     */
    Tlv getLastTlv(int type);

    /**
     * Returns an array containing all TLV's in this chain with the given TLV
     * type, with original order preserved. Note that if there are no matches
     * this will return a zero-length array and not <code>null</code>.
     *
     * @param type the type of TLV whose matching TLV's will be returned
     * @return a list of the TLV's in this chain with the given type
     */
    Tlv[] getTlvs(int type);

    /**
     * Returns the ASCII string contained in the <i>last</i> TLV in this chain
     * with the given type, or <code>null</code> if no TLV with the given type
     * is present in this chain. Equivalent to <code>chain.hasTlv(type) ?
     * chain.getLastTlv(type).getDataAsString() : null</code>.
     *
     * @param type the type of TLV whose ASCII string value will be returned
     * @return the ASCII string stored in the value of the last TLV in this
     *         chain that has the given TLV type
     * @see #getLastTlv
     * @see Tlv#getDataAsString
     */
    String getString(int type);

    /**
     * Returns the string contained in the <i>last</i> TLV in this chain
     * with the given type, decoded with the given charset, or <code>null</code>
     * if no TLV with the given type is present in this chain. Note that if the
     * given charset is not found in this JVM, a valid charset will be derived
     * (like converting "unicode-2.0" to "UTF-16BE") or "US-ASCII" will be used.
     *
     * @param type the type of TLV whose string value will be returned
     * @param charset the charset with which the string will be decoded, or
     *        <code>null</code> to decode as US-ASCII
     * @return the ASCII string stored in the value of the last TLV in this
     *         chain that has the given TLV type
     *
     * @see #getLastTlv
     */
    String getString(int type, String charset);

    /**
     * Returns an unsigned two-byte integer read from the value of the
     * <i>last</i> TLV of the given type in this chain, or <code>-1</code> if
     * either no TLV of the given type is present in this chain or if the data
     * block for the TLV contains fewer than two bytes.
     *
     * @param type the type of the TLV whose value will be returned
     * @return the two-byte integer value stored in the last TLV of the given
     *         type, or <code>-1</code> if none is present
     * @see #getLastTlv
     * @see Tlv#getDataAsUShort
     */
    int getUShort(int type);

    /**
     * Returns an unsigned four-byte integer read from the value of the
     * <i>last</i> TLV of the given type in this chain, or <code>-1</code> if
     * either no TLV of the given type is present in this chain or if the data
     * block for the TLV contains fewer than two bytes.
     *
     * @param type the type of the TLV whose value will be returned
     * @return the four-byte integer value stored in the last TLV of the given
     *         type, or <code>-1</code> if none is present
     * @see #getLastTlv
     * @see Tlv#getDataAsUInt
     */
    long getUInt(int type);

    /**
     * Returns the total size, in bytes, of this chain, as read from an incoming
     * stream. Will be <code>-1</code> if this chain was not read from a stream.
     *
     * @return the total size, in bytes, of this chain
     */
    int getTotalSize();
}
