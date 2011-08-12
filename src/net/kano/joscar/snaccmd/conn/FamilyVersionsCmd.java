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
 *  File created by keith @ Feb 21, 2003
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
 * A base class for the two SNAC-family-version-based commands in this family.
 * These two commands are {@link ClientVersionsCmd} and {@link
 * ServerVersionsCmd}. Note that {@link ClientReadyCmd} involves client
 * versions as well, but sends more details about family versions than the two
 * version-only commands mentioned above.
 */
public abstract class FamilyVersionsCmd extends ConnCommand {
    /** The SNAC family information blocks in this command. */
    private final SnacFamilyInfo[] families;

    /**
     * Creates a new family-version-based command read from the given incoming
     * SNAC packet.
     *
     * @param command the SNAC command subtype of this command
     * @param packet a family-version-based-command SNAC packet
     */
    protected FamilyVersionsCmd(int command, SnacPacket packet) {
        super(command);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

        families = new SnacFamilyInfo[snacData.getLength()/4];

        for (int i = 0; i < families.length; i++) {
            int family = BinaryTools.getUShort(snacData, i*4);
            int version = BinaryTools.getUShort(snacData, i*4+2);
            families[i] = new SnacFamilyInfo(family, version);
        }
    }

    /**
     * Creates a new outgoing family-version-based command with the given SNAC
     * command type and the given list of family version information blocks.
     *
     * @param command the SNAC command subtype of this command
     * @param families a list of SNAC family version information blocks
     */
    protected FamilyVersionsCmd(int command, SnacFamilyInfo[] families) {
        super(command);

        this.families = families == null
                ? null
                : (SnacFamilyInfo[]) families.clone();
    }

    /**
     * Returns the SNAC family information blocks contained in this command.
     *
     * @return this command's list of SNAC family information blocks
     */
    public final SnacFamilyInfo[] getSnacFamilyInfos() {
        return (SnacFamilyInfo[]) families.clone();
    }

    public void writeData(OutputStream out) throws IOException {
        if (families != null) {
            for (int i = 0; i < families.length; i++) {
                BinaryTools.writeUShort(out, families[i].getFamily());
                BinaryTools.writeUShort(out, families[i].getVersion());
            }
        }
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(MiscTools.getClassName(this) + ": family versions: ");

        if (families != null) {
            for (int i = 0; i < families.length; i++) {
                buffer.append(Integer.toHexString(families[i].getFamily()));
                buffer.append(" (v=");
                buffer.append(Integer.toHexString(families[i].getVersion()));
                buffer.append("), ");
            }
        }

        return buffer.toString();
    }
}
