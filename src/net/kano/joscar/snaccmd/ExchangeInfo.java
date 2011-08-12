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
 *  File created by keith @ Feb 26, 2003
 *
 */

package net.kano.joscar.snaccmd;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.LiveWritable;
import net.kano.joscar.tlv.ImmutableTlvChain;
import net.kano.joscar.tlv.Tlv;
import net.kano.joscar.tlv.TlvChain;
import net.kano.joscar.tlv.TlvTools;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Represents information about a given chat "exchange." Exchanges are,
 * essentially, individual chat services within a single OSCAR chat server;
 * for example, two chat rooms with the same name can exist on two different
 * exchanges. Exchanges are identified by numbers, and generally something like
 * exchanges 4 through 20 are available on AOL's official AIM servers. Exchange
 * #4 is the only exchange on which WinAIM users can create chat rooms or join
 * without being invited; that is, if you're in a chat room in WinAIM, it's
 * probably on exchange #4.
 */
public final class ExchangeInfo extends AbstractChatInfo implements LiveWritable {
    /**
     * Returns an exchange information block read from the given data block, or
     * <code>null</code> if no valid block can be read.
     *
     * @param block the data block containing an exchange info block
     * @return an exchange information object read from the given data block,
     *         or <code>null</code> if none can be read
     */
    public static ExchangeInfo readExchangeInfo(ByteBlock block) {
        if (block.getLength() < 2) return null;

        return new ExchangeInfo(block);
    }

    /**
     * A TLV type containing a URL describing the associated exchange.
     */
    private static final int TYPE_URL = 0x00d4;

    /**
     * The exchange number of the associated exchange.
     */
    private final int number;

    /**
     * A URL at which, presumably, a description of the associated exchange
     * exists.
     */
    private final String url;

    /**
     * Creates a new exchange info block from the given data block.
     *
     * @param block the block of data containing exchange information
     */
    ExchangeInfo(ByteBlock block) {
        number = BinaryTools.getUShort(block, 0);

        ByteBlock tlvBlock = block.subBlock(2);

        TlvChain chain = TlvTools.readChain(tlvBlock);

        readBaseInfo(chain);

        url = chain.getString(TYPE_URL);
    }

    /**
     * Returns this exchange's "exchange number."
     *
     * @return the exchange number of this exchange info object
     */
    public final int getNumber() {
        return number;
    }

    /**
     * Returns a URL that supposedly describes this exchange. This might be
     * a URI that represents the exchange itself or an HTTP URL containing a
     * website describing the exchange. As of this writing I have never seen
     * this field actually sent.
     *
     * @return the URL associated with this
     */
    public final String getUrl() {
        return url;
    }

    public void write(OutputStream out) throws IOException {
        BinaryTools.writeUShort(out, number);

        writeBaseInfo(out);

        if (url != null) Tlv.getStringInstance(TYPE_URL, url).write(out);
    }

    public String toString() {
        return "ExchangeInfo for #" + number +
                ", url='" + url + "'" +
                " - " + super.toString();
    }
}
