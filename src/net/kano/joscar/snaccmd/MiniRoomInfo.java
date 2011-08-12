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
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.LiveWritable;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A data structure used to transmit a small amount of information about a
 * chat room. Contains fields for an exchange number, a service redirect cookie,
 * and an instance number.
 */
public final class MiniRoomInfo implements LiveWritable {
    /**
     * Reads a <code>MiniRoomInfo</code> block from the given data block, or
     * <code>null</code> if no valid block can be read.
     *
     * @param block the data block from which to generate the mini room info
     *        object
     * @return a mini room info object read from the given data block, or
     *         <code>null</code> if no valid object can be read
     */
    public static MiniRoomInfo readMiniRoomInfo(ByteBlock block) {
        DefensiveTools.checkNull(block, "block");

        if (block.getLength() < 5) return null;

        int exchange = BinaryTools.getUShort(block, 0);
        int cookielen = BinaryTools.getUByte(block, 2);

        if (block.getLength() < 5 + cookielen) return null;

        ByteBlock cookieBlock = block.subBlock(3, cookielen);
        String cookie = BinaryTools.getAsciiString(cookieBlock);

        ByteBlock rest = block.subBlock(3 + cookielen);
        int instance = BinaryTools.getUShort(rest, 0);

        // this is messy. :/
        int size = (rest.getOffset() + 2) - block.getOffset();

        return new MiniRoomInfo(exchange, cookie, instance, size);
    }

    /** The exchange number on which this room resides. */
    private final int exchange;
    /** The service redirect cookie that can be used to join this room. */
    private final String cookie;
    /** The "instance" number of this room. */
    private final int instance;
    /** The total size of this object, as read from a data block. */
    private final int totalSize;

    /**
     * Creates a room info object with the given properties.
     *
     * @param exchange the number of the exchange on which this room resides
     * @param cookie the service redirect cookie that can be used to join this
     *        room
     * @param instance the "instance number" of this room
     * @param totalSize the total size of this object, as read from a block of
     *        binary data
     */
    protected MiniRoomInfo(int exchange, String cookie, int instance,
            int totalSize) {
        DefensiveTools.checkRange(exchange, "exchange", 0);
        DefensiveTools.checkNull(cookie, "cookie");
        DefensiveTools.checkRange(instance, "instance", 0);
        DefensiveTools.checkRange(totalSize, "totalSize", -1);

        this.exchange = exchange;
        this.cookie = cookie;
        this.instance = instance;
        this.totalSize = totalSize;
    }

    /**
     * Creates a new miniature room information block with the exchange, cookie,
     * and instance properties of the given full room information block.
     *
     * @param roomInfo a full room information block from which to derive this
     *        miniature room information block's properties
     */
    public MiniRoomInfo(FullRoomInfo roomInfo) {
        this.exchange = roomInfo.getExchange();
        this.cookie = roomInfo.getCookie();
        this.instance = roomInfo.getInstance();
        this.totalSize = -1;
    }

    /**
     * Creates a new mini room info object with the given properties.
     *
     * @param exchange the number of the chat exchange on which the associated
     *        room exists
     * @param cookie the service redirect cookie that can be used to join this
     *        room
     * @param instance the "instance number" of this room
     */
    public MiniRoomInfo(int exchange, String cookie, int instance) {
        this(exchange, cookie, instance, -1);
    }

    /**
     * Returns the number of the exchange on which this room resides. See
     * {@link ExchangeInfo} for a brief explanation of exchanges.
     *
     * @return the exchange on which this room exists
     */
    public final int getExchange() {
        return exchange;
    }

    /**
     * Returns the {@linkplain net.kano.joscar.snaccmd.conn.ServiceRequest
     * service redirect} cookie that can be used to join this room.
     *
     * @return the service redirect cookie associated with this room
     */
    public final String getCookie() {
        return cookie;
    }

    /**
     * Returns "instance number" of this room. As of this writing I am unsure of
     * the significance of this number.
     *
     * @return this room's "instance number"
     */
    public final int getInstance() {
        return instance;
    }

    /**
     * Returns the total size of this object, as read from a block of binary
     * data. Will be <code>-1</code> if this object was not read using
     * <code>readMiniRoomInfo</code>.
     *
     * @return the total size, in bytes, of this object, or <code>-1</code> if
     *         it was not read from binary data but instead instantiated
     *         manually
     */
    public final int getTotalSize() {
        return totalSize;
    }

    public void write(OutputStream out) throws IOException {
        BinaryTools.writeUShort(out, exchange);

        byte[] cookieBytes = BinaryTools.getAsciiBytes(cookie);
        BinaryTools.writeUByte(out, cookieBytes.length);
        out.write(cookieBytes);

        BinaryTools.writeUShort(out, instance);
    }

    public String toString() {
        return "MiniRoomInfo: exchange #" + exchange + ", instance #" + instance
                + ", cookie=" + cookie;
    }
}
