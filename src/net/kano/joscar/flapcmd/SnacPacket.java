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
 *  File created by keith @ Feb 17, 2003
 *
 */

package net.kano.joscar.flapcmd;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.LiveWritable;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Represents a single "packet" or SNAC command sent over a FLAP connection.
 */
public final class SnacPacket implements LiveWritable {
    /**
     * A <code>SnacCommand</code> to use in writing the SNAC data to a stream.
     */
    private final SnacCommand snacCommand;

    /** This command's SNAC family code. */
    private final int family;
    /** This command's SNAC subtype. */
    private final int command;
    /** This command's request ID. */
    private final long reqid;
    /** The first SNAC flag of this command. */
    private final short flag1;
    /** The second SNAC flag of this command. */
    private final short flag2;
    /** The SNAC data block, if read from an input stream. */
    private final ByteBlock block;

    /**
     * Returns <code>true</code> if the given block of data represents a valid
     * SNAC packet. This only tests the format, and not the actual content, of
     * the data block; it will not test for a valid SNAC family/subtype pair
     * or anything of that sort.
     *
     * @param data the data block possibly containing a SNAC packet
     * @return whether or not the given data block represents a valid SNAC
     *         packet
     */
    static boolean isValidSnacPacket(ByteBlock data) {
        return data.getLength() >= 10;
    }

    /**
     * Generates a new SNAC packet object from the given data block, or
     * <code>null</code> if no valid packet could be read.
     *
     * @param flapData a block of data containing a SNAC packet
     * @return a SNAC packet object read from the given data, or
     *         <code>null</code> if no valid packet could be read
     * @throws IllegalArgumentException if the given data block does not contain
     *         a valid SNAC packet
     */
    static SnacPacket readSnacPacket(ByteBlock flapData) {
        if (!isValidSnacPacket(flapData)) return null;

        int family = BinaryTools.getUShort(flapData, 0);
        int command = BinaryTools.getUShort(flapData, 2);
        short flag1 = BinaryTools.getUByte(flapData, 4);
        short flag2 = BinaryTools.getUByte(flapData, 5);
        long reqid = BinaryTools.getUInt(flapData, 6);
        ByteBlock block = flapData.subBlock(10);

        return new SnacPacket(family, command, reqid, flag1, flag2, block);
    }

    /**
     * Creates a SNAC packet object with the given properties.
     *
     * @param family the packet's SNAC family code
     * @param command the packet's SNAC family command subtype
     * @param reqid the packet's request ID
     * @param flag1 the packet's first SNAC flag byte
     * @param flag2 the packet's second SNAC flag byte
     * @param data the SNAC data block
     */
    public SnacPacket(int family, int command, long reqid, short flag1,
            short flag2, ByteBlock data) {
        DefensiveTools.checkRange(family, "family", 0);
        DefensiveTools.checkRange(command, "command", 0);
        DefensiveTools.checkRange(reqid, "reqid", 0);
        DefensiveTools.checkRange(flag1, "flag1", 0);
        DefensiveTools.checkRange(flag2, "flag2", 0);
        DefensiveTools.checkNull(data, "data");

        this.snacCommand = null;
        this.family = family;
        this.command = command;
        this.reqid = reqid;
        this.flag1 = flag1;
        this.flag2 = flag2;
        this.block = data;
    }

    /**
     * Creates a SNAC packet object with the given request ID and the properties
     * of the given SNAC command. The given SNAC command will be used to write
     * the SNAC data block upon a call to <code>write</code>.
     *
     * @param reqid the request ID of this SNAC packet
     * @param command the SNAC command upon which this packet is based
     */
    SnacPacket(long reqid, SnacCommand command) {
        DefensiveTools.checkRange(reqid, "reqid", 0);
        DefensiveTools.checkNull(command, "command");

        this.snacCommand = command;
        this.family = command.getFamily();
        this.command = command.getCommand();
        this.reqid = reqid;
        this.flag1 = command.getFlag1();
        this.flag2 = command.getFlag2();
        this.block = null;
    }

    /**
     * Returns this packet's SNAC family code.
     *
     * @return this packet's SNAC family
     */
    public final int getFamily() {
        return family;
    }

    /**
     * Returns this packet's SNAC command code ("subtype").
     *
     * @return this packet's SNAC command subtype
     */
    public final int getCommand() {
        return command;
    }

    /**
     * Returns this packet's SNAC request ID.
     *
     * @return this packet's SNAC request ID
     */
    public final long getReqid() {
        return reqid;
    }

    /**
     * Returns this packet's first SNAC flag byte.
     *
     * @return this packet's first SNAC flag byte
     */
    public final short getFlag1() {
        return flag1;
    }

    /**
     * Returns this packet's second SNAC flag byte.
     *
     * @return this packet's second SNAC flag byte
     */
    public final short getFlag2() {
        return flag2;
    }

    /**
     * Returns this packet's SNAC data block, or <code>null</code> if this
     * packet was not read from an incoming stream.
     *
     * @return this packet's SNAC data block, if read from an incoming stream
     */
    public final ByteBlock getData() {
        return block;
    }

    public void write(OutputStream out) throws IOException {
        BinaryTools.writeUShort(out, family);
        BinaryTools.writeUShort(out, command);
        BinaryTools.writeUByte(out, flag1);
        BinaryTools.writeUByte(out, flag2);
        BinaryTools.writeUInt(out, reqid);
        if (snacCommand != null) snacCommand.writeData(out);
        else if (block != null) block.write(out);
    }

    public String toString() {
        return "SnacPacket type 0x" + Integer.toHexString(family)
                + "/0x" + Integer.toHexString(command)
                + (flag1 == 0 ? "" : ", flag1=0x" + Integer.toHexString(flag1))
                + (flag2 == 0 ? "" : ", flag2=0x" + Integer.toHexString(flag2))
                + ": " + (block == null ? null : block.getLength() + " bytes")
                + " (id=" + reqid + ")";
    }
}
