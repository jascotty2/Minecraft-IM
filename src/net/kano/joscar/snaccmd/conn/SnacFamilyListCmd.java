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
import net.kano.joscar.MiscTools;
import net.kano.joscar.flapcmd.SnacPacket;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A base class for the two SNAC commands that contain a list of SNAC families.
 * These commands are {@link PauseAck} and {@link ServerReadyCmd}.
 */
public abstract class SnacFamilyListCmd extends ConnCommand {
    /** The SNAC families contained in this command. */
    private final int[] snacFamilies;

    /**
     * Generates a new snac family list command of the given SNAC command
     * subtype and the given incoming SNAC packet.
     *
     * @param command the SNAC command subtype of this command
     * @param packet the incoming SNAC-family-list-based packet
     */
    protected SnacFamilyListCmd(int command, SnacPacket packet) {
        super(command);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

        snacFamilies = new int[snacData.getLength() / 2];
        for (int i = 0; i * 2 < snacData.getLength() - 1; i++) {
            snacFamilies[i] = BinaryTools.getUShort(snacData, i * 2);
        }
    }

    /**
     * Creates a new outgoing SNAC-family-list-based command with the given
     * SNAC command subtype and the given SNAC family list.
     *
     * @param command the SNAC command subtype of this command
     * @param snacFamilies the SNAC families contained in this command
     */
    protected SnacFamilyListCmd(int command, int[] snacFamilies) {
        super(command);

        this.snacFamilies = (int[]) (snacFamilies == null
                ? null
                : snacFamilies.clone());
    }

    /**
     * Returns the SNAC families contained in this command.
     *
     * @return this command's associated SNAC families
     */
    public final int[] getSnacFamilies() {
        return (int[]) (snacFamilies == null ? null : snacFamilies.clone());
    }

    public void writeData(OutputStream out) throws IOException {
        if (snacFamilies != null) {
            for (int i = 0; i < snacFamilies.length; i++) {
                BinaryTools.writeUShort(out, snacFamilies[i]);
            }
        }
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(MiscTools.getClassName(this) + ": snac families: ");
        if (snacFamilies != null) {
            for (int i = 0; i < snacFamilies.length; i++) {
                if (i != 0) buffer.append(", ");
                buffer.append(Integer.toHexString(snacFamilies[i]));
            }
        }

        return buffer.toString();
    }
}