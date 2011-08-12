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
 *  File created by keith @ Apr 7, 2003
 *
 */

package net.kano.joscar.snac;

import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.flapcmd.SnacPacket;

/**
 * Provides an interface for modifying the contents of a SNAC packet.
 */
public final class MutableSnacPacket {
    /** This command's SNAC family code. */
    private int family;
    /** This command's SNAC subtype. */
    private int command;
    /** This command's request ID. */
    private long reqid;
    /** The first SNAC flag of this command. */
    private short flag1;
    /** The second SNAC flag of this command. */
    private short flag2;
    /** The SNAC data block, if read from an input stream. */
    private ByteBlock data;
    /** Whether this packet has been changed. */
    private boolean changed = false;

    /**
     * Creates a mutable SNAC packet object with the same properties as the
     * given object.
     *
     * @param packet the SNAC packet whose properties are to be copied into this
     *        object
     */
    public MutableSnacPacket(SnacPacket packet) {
        DefensiveTools.checkNull(packet, "packet");

        family = packet.getFamily();
        command = packet.getCommand();
        reqid = packet.getReqid();
        flag1 = packet.getFlag1();
        flag2 = packet.getFlag2();
        data = packet.getData();
    }

    /**
     * Returns this packet's SNAC family code.
     *
     * @return this packet's SNAC family
     */
    public final int getFamily() { return family; }

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
     * Returns this packet's SNAC data block.
     *
     * @return this packet's SNAC data block
     */
    public final ByteBlock getData() {
        return data;
    }

    /**
     * Sets this packet's SNAC family code.
     *
     * @param family this packet's new SNAC family code
     */
    public void setFamily(int family) {
        DefensiveTools.checkRange(family, "family", 0);

        if (!changed) changed = family != this.family;
        this.family = family;
    }

    /**
     * Sets this packet's SNAC command subtype.
     *
     * @param command this packet's new SNAC command subtype
     */
    public void setCommand(int command) {
        DefensiveTools.checkRange(command, "command", 0);

        if (!changed) changed = command != this.command;
        this.command = command;
    }

    /**
     * Sets this packet's SNAC request ID.
     *
     * @param reqid this packet's new SNAC request ID
     */
    public void setReqid(long reqid) {
        DefensiveTools.checkRange(reqid, "reqid", 0);

        if (!changed) changed = reqid != this.reqid;
        this.reqid = reqid;
    }

    /**
     * Sets this packet's first SNAC flag byte.
     *
     * @param flag1 this packet's new first SNAC flag byte
     */
    public void setFlag1(short flag1) {
        DefensiveTools.checkRange(flag1, "flag1", 0);

        if (!changed) changed = flag1 != this.flag1;
        this.flag1 = flag1;
    }

    /**
     * Sets this packet's second SNAC flag byte.
     *
     * @param flag2 this packet's new second SNAC flag byte
     */
    public void setFlag2(short flag2) {
        DefensiveTools.checkRange(flag2, "flag2", 0);

        if (!changed) changed = flag2 != this.flag2;
        this.flag2 = flag2;
    }

    /**
     * Sets this packet's SNAC data block.
     *
     * @param data this packet's new SNAC data block
     */
    public void setData(ByteBlock data) {
        DefensiveTools.checkNull(data, "data");

        if (!changed) changed = data != this.data;
        this.data = data;
    }

    /**
     * Returns <code>true</code> if this object has been modified since its
     * creation (via one of the <code>set</code> methods).
     *
     * @return whether this object has been modified since its creation
     */
    public boolean isChanged() { return changed; }

    /**
     * Returns a SNAC packet object with the same properties as this object.
     *
     * @return a SNAC packet object representing this object
     */
    public SnacPacket toSnacPacket() {
        return new SnacPacket(family, command, reqid, flag1, flag2, data);
    }

    public String toString() {
        return "MutableSnacPacket type 0x" + Integer.toHexString(family)
                + "/0x" + Integer.toHexString(command)
                + (flag1 == 0 ? "" : ", flag1=0x" + Integer.toHexString(flag1))
                + (flag2 == 0 ? "" : ", flag2=0x" + Integer.toHexString(flag2))
                + ": " + (data == null ? null : data.getLength() + " bytes")
                + " (id=" + reqid + ")";
    }
}
