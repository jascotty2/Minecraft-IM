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
 *  File created by keith @ Feb 28, 2003
 *
 */

package net.kano.joscar.snaccmd.search;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.flapcmd.SnacPacket;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A SNAC command containing a "tree" of valid "chat interests." Normally sent
 * in response to a {@link InterestListReq}. See {@link InterestInfo} for
 * details on how to interpret the data sent in this command.
 *
 * @snac.src server
 * @snac.cmd 0x0f 0x05
 *
 * @see InterestListReq
 * @see InterestInfo
 */
public class InterestListCmd extends SearchCommand {
    /** A result code indicating that a interest list request was successful. */
    public static final int CODE_SUCCESS = 0x0001;
    /**
     * A result code indicating that a chat interest list is currently
     * unavailable.
     */
    public static final int CODE_UNAVAILABLE = 0x000b;

    /** A result code. */
    private final int code;
    /** A list of interests sent in this command. */
    private final InterestInfo[] interests;

    /**
     * Generates a new interest list command from the given incoming SNAC
     * packet.
     *
     * @param packet an incoming interest list packet
     */
    protected InterestListCmd(SnacPacket packet) {
        super(CMD_INTERESTS);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

        code = BinaryTools.getUShort(snacData, 0);
        int interestCount = BinaryTools.getUShort(snacData, 2);

        if (snacData.getLength() > 4) {
            ByteBlock block = snacData.subBlock(4);

            List interestList = new ArrayList();
            for (int i = 0; i < interestCount; i++) {
                InterestInfo interest = InterestInfo.readInterestInfo(block);
                if (interest == null) break;

                interestList.add(interest);

                block = block.subBlock(interest.getTotalSize());
            }

            interests = (InterestInfo[]) interestList.toArray(
                    new InterestInfo[0]);
        } else {
            interests = null;
        }
    }

    /**
     * Creates a new outgoing interest list command with no interest list and
     * with the given result code.
     *
     * @param code a result code, like {@link #CODE_UNAVAILABLE}
     */
    public InterestListCmd(int code) {
        this(code, null);
    }

    /**
     * Creates a new outgoing interest list command with the given interest
     * list. The result code will be {@link #CODE_SUCCESS} if
     * <code>interests</code> is not <code>null</code> (even if
     * <code>interests.length</code> is <code>0</code>!), and {@link
     * #CODE_UNAVAILABLE} if <code>interests</code> is <code>null</code>.
     *
     * @param interests a list of interests
     */
    public InterestListCmd(InterestInfo[] interests) {
        this(interests != null ? CODE_SUCCESS : CODE_UNAVAILABLE, interests);
    }

    /**
     * Creates a new outgoing interest list command with the given result code
     * and the given interest list.
     *
     * @param code a result code, like {@link #CODE_UNAVAILABLE}
     * @param interests a list of interest, or <code>null</code> for none
     */
    public InterestListCmd(int code, InterestInfo[] interests) {
        super(CMD_INTERESTS);

        DefensiveTools.checkRange(code, "code", 0);

        this.code = code;
        this.interests = (InterestInfo[]) (interests == null
                ? null
                : interests.clone());
    }

    /**
     * Returns the result code sent in this command. Normally either {@link
     * #CODE_SUCCESS} or {@link #CODE_UNAVAILABLE}.
     *
     * @return the result code associated with this response
     */
    public int getResultCode() { return code; }

    /**
     * Returns the interest list sent in this command, or <code>null</code> if
     * none was sent. See {@link InterestInfo} for details on how to interpret
     * this list.
     *
     * @return the list of chat interests sent in this command
     */
    public final InterestInfo[] getInterests() {
        return (InterestInfo[]) (interests == null ? null : interests.clone());
    }

    public void writeData(OutputStream out) throws IOException {
        BinaryTools.writeUShort(out, code);
        if (interests != null) {
            BinaryTools.writeUShort(out, interests.length);
            for (int i = 0; i < interests.length; i++) {
                interests[i].write(out);
            }
        }
    }

    public String toString() {
        return "InterestListCmd: code=" + code + ", " + (interests == null
                ? -1 : interests.length) + " interests";
    }
}
