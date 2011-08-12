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

package net.kano.joscar.snac;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;

/**
 * A SNAC preprocessor to filter out some recent strangeness in the protocol.
 * If a certain SSI (family <code>0x13</code>) SNAC family version is sent, the
 * server begins certain packets with a TLV block containing that family
 * version. Don't ask me why. Either way, I think the AOL developers might
 * have some sympathy for third-party developers, because a <code>0x80</code>
 * bit mask is applied to the SNAC's first "flag" byte when this TLV block has
 * been prepended to a packet. This makes filtering it out very easy. 
 */
public final class FamilyVersionPreprocessor implements SnacPreprocessor {
    /**
     * Filters the TLV block mentioned {@linkplain FamilyVersionPreprocessor
     * above} out of the given SNAC packet.
     *
     * @param packet the packet to filter
     */
    public final void process(final MutableSnacPacket packet) {
        if ((packet.getFlag1() & 0x80) != 0) {
            // yay.
            ByteBlock data = packet.getData();

            final int len = BinaryTools.getUShort(data, 0);
            ByteBlock rest = data.subBlock(2 + len);

            packet.setData(rest);
        }
    }
}
