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

package net.kano.joscar.snaccmd.rooms;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.flapcmd.SnacPacket;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A SNAC command sent to request information about a particular chat
 * "exchange." Normally responded-to with a {@link RoomResponse}.
 *
 * @snac.src client
 * @snac.cmd 0x0d 0x03
 *
 * @see RoomResponse
 */
public class ExchangeInfoReq extends RoomCommand {
    /** The exchange whose information is being requested. */
    private final int exchange;

    /**
     * Generates a new exchange information request from the given incoming SNAC
     * packet.
     *
     * @param packet an incoming exchange information request packet
     */
    protected ExchangeInfoReq(SnacPacket packet) {
        super(CMD_EXCH_INFO_REQ);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

        exchange = BinaryTools.getUShort(snacData, 0);
    }

    /**
     * Creates a new exchange information request for the given exchange.
     *
     * @param exchange the exchange whose information is being requested
     */
    public ExchangeInfoReq(int exchange) {
        super(CMD_EXCH_INFO_REQ);

        DefensiveTools.checkRange(exchange, "exchange", 0);

        this.exchange = exchange;
    }

    /**
     * Returns the exchange whose information is being requested.
     *
     * @return the exchange whose information is being requested
     */
    public final int getExchange() {
        return exchange;
    }

    public void writeData(OutputStream out) throws IOException {
        if (exchange != -1) BinaryTools.writeUShort(out, exchange);
    }

    public String toString() {
        return "ExchangeInfoReq for exchange #" + exchange;
    }
}
