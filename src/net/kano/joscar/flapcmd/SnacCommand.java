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

import net.kano.joscar.DefensiveTools;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Represents a single SNAC command that can write a single SNAC packet
 * to a stream. <code>SnacCommand</code> is currently the only means of sending
 * a SNAC command over a FLAP connection in joscar.
 */
public abstract class SnacCommand {
    /**
     * The default value for both the first and the second flags of a SNAC
     * command.
     */
    public static final short SNACFLAG_DEFAULT = 0;

    /**
     * The command family of this SNAC command.
     */
    private final int family;

    /**
     * The command subtype of this SNAC command.
     */
    private final int command;

    /**
     * The first "flag" byte of this command.
     */
    private final short flag1;

    /**
     * The second "flag" byte of this command.
     */
    private final short flag2;

    /**
     * Creates a new <code>SnacCommand</code> with the given properties and both
     * flags set to {@link #SNACFLAG_DEFAULT}. Using this constructor is equivalent
     * to using {@link #SnacCommand(int, int, short, short) new
     * SnacCommand(family, command, SnacCommand.SNACFLAG_DEFAULT,
     * SnacCommand.SNACFLAG_DEFAULT)}.
     *
     * @param family the SNAC command family of this command
     * @param command the SNAC command subtype of this command
     */
    protected SnacCommand(int family, int command) {
        this(family, command, SNACFLAG_DEFAULT, SNACFLAG_DEFAULT);
    }

    /**
     * Creates a new <code>SnacCommand</code> with the given properties.
     *
     * @param family the SNAC command family of this command
     * @param command the SNAC command subtype of this command
     * @param flag1 the first flag byte of this command
     * @param flag2 the second flag byte of this command
     */
    protected SnacCommand(int family, int command, short flag1, short flag2) {
        DefensiveTools.checkRange(family, "family", 0);
        DefensiveTools.checkRange(command, "command", 0);
        DefensiveTools.checkRange(flag1, "flag1", 0);
        DefensiveTools.checkRange(flag2, "flag2", 0);

        this.family = family;
        this.command = command;
        this.flag1 = flag1;
        this.flag2 = flag2;
    }

    /**
     * Returns the SNAC command family of this command.
     *
     * @return this command's SNAC command family
     */
    public final int getFamily() {
        return family;
    }

    /**
     * Returns the SNAC command subtype of this command.
     *
     * @return this command's SNAC command ID ("subtype")
     */
    public final int getCommand() {
        return command;
    }

    /**
     * Returns the first "flag byte" of this SNAC command.
     *
     * @return this SNAC command's first "flag byte"
     */
    public final short getFlag1() {
        return flag1;
    }

    /**
     * Returns the second "flag byte" of this SNAC command.
     *
     * @return this SNAC command's second "flag byte"
     */
    public final short getFlag2() {
        return flag2;
    }

    /**
     * Writes this command's SNAC data block to the given stream. The SNAC data
     * block is the data after the first ten bytes of a SNAC packet.
     *
     * @param out the stream to which to write the SNAC data
     * @throws IOException if an I/O error occurs
     */
    public abstract void writeData(OutputStream out) throws IOException;
}
