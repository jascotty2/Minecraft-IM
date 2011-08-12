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
 *  File created by keith @ Feb 22, 2003
 *
 */

package net.kano.joscar.snaccmd.conn;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.flapcmd.SnacPacket;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A SNAC command sent when rate limiting information for a rate class changes.
 *
 * @snac.src server
 * @snac.cmd 0x01 0x0a
 */
public class RateChange extends ConnCommand {
    /** A rate change code indicating that rates changed for some reason. */
    public static final int CODE_RATES_CHANGED = 1;
    /**
     * A rate change code indicating that the user is in the "warning zone."
     * WinAIM indicates this state by placing the "rate bar" in the yellow
     * region.
     */
    public static final int CODE_WARNING = 2;
    /**
     * A rate change code indicating that the user has been rate-limited and
     * that one or more previously sent commands were ignored. All future
     * commands in this rate class will be ignored until the receipt of a
     * {@linkplain #CODE_LIMIT_CLEARED limit-cleared} change command.
     */
    public static final int CODE_LIMITED = 3;
    /**
     * A rate change code indicating that the user is no longer being
     * rate-limited.
     */
    public static final int CODE_LIMIT_CLEARED = 4;

    /** A code indicating the type of change that occurred. */
    private final int code;
    /** Information about the rate class that changed. */
    private final RateClassInfo rateInfo;

    /**
     * Generates a new rate change command from the given incoming SNAC packet.
     *
     * @param packet an incoming rate change packet
     */
    protected RateChange(SnacPacket packet) {
        super(CMD_RATE_CHG);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

        code = BinaryTools.getUShort(snacData, 0);

        ByteBlock rateBlock = snacData.subBlock(2);

        rateInfo = RateClassInfo.readRateClassInfo(rateBlock);
    }

    /**
     * Creates a new outgoing rate change command of the given type and with the
     * given new rate class information.
     *
     * @param code a change code, like {@link #CODE_LIMITED}
     * @param rateInfo the new rate class information for the class changed
     */
    public RateChange(int code, RateClassInfo rateInfo) {
        super(CMD_RATE_CHG);

        DefensiveTools.checkRange(code, "code", 0);

        this.rateInfo = rateInfo;
        this.code = code;
    }

    /**
     * Returns a code indicating the type of change that occurred. This will
     * normally be one of {@link #CODE_RATES_CHANGED}, {@link #CODE_WARNING},
     * {@link #CODE_LIMITED}, and {@link #CODE_LIMIT_CLEARED}.
     *
     * @return a code indicating the type of rate change that occurred
     */
    public final int getChangeCode() {
        return code;
    }

    /**
     * The updated rate information for the rate class that changed.
     *
     * @return the changed rate class
     */
    public final RateClassInfo getRateInfo() {
        return rateInfo;
    }

    public void writeData(OutputStream out) throws IOException {
        BinaryTools.writeUShort(out, code);

        if (rateInfo != null) rateInfo.write(out);
    }

    public String toString() {
        return "RateChange (code=" + code + "): " + rateInfo;
    }
}
