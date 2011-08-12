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
 *  File created by keith @ Feb 19, 2003
 *
 */

package net.kano.joscar.snac;

import net.kano.joscar.DefensiveTools;
import net.kano.joscar.flapcmd.SnacCommand;

/**
 * Represents a single SNAC command type, or a group of all SNAC command types
 * in a single family, or all SNAC commands. In practice, provides a means of
 * listening for a specific set of commands on a SNAC connection.
 */
public final class CmdType {
    /**
     * A family type or command type that indicates all families or all command
     * types within a family, depending on whether it is used as a family type
     * or command type, respectively.
     */
    public static final int ALL = -1;

    /**
     * A <code>CmdType</code> that represents all possible command types.
     * Equivalent to a new <code>CmdType</code> instantiated using <code>new
     * CmdType(CmdType.ALL, CmdType.ALL)</code>. In fact, that's what it is.
     */
    public static final CmdType CMDTYPE_ALL = new CmdType(ALL, ALL);

    /**
     * Returns a <code>CmdType</code> that represents the command type of the
     * given SNAC command object.
     *
     * @param command a SNAC command
     * @return a command type object representing the type of the given SNAC
     *         command
     */
    public static CmdType ofCmd(SnacCommand command) {
        DefensiveTools.checkNull(command, "command");

        return new CmdType(command.getFamily(), command.getCommand());
    }

    /**
     * The SNAC family of this command type, or <code>ALL</code>.
     */
    private final int family;

    /**
     * The SNAC command type ("subtype") of this command type, or
     * <code>ALL</code>.
     */
    private final int command;

    /**
     * Creates a <code>CmdType</code> matching all commands in the given family.
     * Using this constructor is the equivalent to using <code>CmdType(family,
     * CmdType.ALL)</code>. Note that <code>family</code> can be
     * <code>CmdType.ALL</code>, but it is recommended to simply use
     * <code>CmdType.CMDTYPE_ALL</code> instead of creating a new instance.
     *
     * @param family the SNAC family for this <code>CmdType</code>, or
     *        <code>CmdType.ALL</code>
     */
    public CmdType(int family) {
        this(family, ALL);
    }

    /**
     * Creates a <code>CmdType</code> matching the given command in the given
     * family. <code>command</code> can be <code>CmdType.ALL</code>, in which
     * case this object will match all commands in the given family.
     * <code>family</code> can also be <code>CmdType.ALL</code>, if and only if
     * <code>command</code> is <code>CmdType.ALL</code> as well, in which case
     * this object will match all possible commands.
     *
     * @param family the family of the commands to represent, or
     *        <code>CmdType.ALL</code>
     * @param command the command type ("subtype") of the command to represent,
     *        or <code>CmdType.ALL</code>
     *
     * @throws IllegalArgumentException if <code>family</code> is
     *         <code>CmdType.ALL</code> and <code>command</code> is not, or
     *         if either <code>family</code> or <code>command</code> are not
     *         <code>CmdType.ALL</code> or positive numbers
     */
    public CmdType(int family, int command) throws IllegalArgumentException {
        if (family == ALL && command != ALL) {
            throw new IllegalArgumentException("if family is CmdType.ALL (-1),"
                    + ", command type must be as well (instead it is "
                    + command + ")");
        }
        if (family != ALL && family < 0) {
            throw new IllegalArgumentException("family must be CmdType.ALL or "
                    + "a positive number (was " + family + ")");
        }
        if (command != ALL && command < 0) {
            throw new IllegalArgumentException("command type must be " +
                    "CmdType.ALL or a positive number");
        }

        this.family = family;
        this.command = command;
    }

    /**
     * Returns the SNAC family of the command(s) represented by this
     * <code>CmdType</code>, or <code>CmdType.ALL</code> if all commands in all
     * families are represented.
     *
     * @return the SNAC family of the command(s) represented by this object,
     *         or <code>CmdType.ALL</code>
     */
    public final int getFamily() {
        return family;
    }

    /**
     * Returns the SNAC command type ("subtype") of the command represented by
     * this <code>CmdType</code>, or <code>CmdType.ALL</code> if this object
     * represents every command in a given family. Also, if
     * <code>getFamily()</code> returns <code>CmdType.ALL</code>, this method
     * will return <code>CmdType.ALL</code> as well, indicating that all
     * commands in all families are represented.
     *
     * @return the SNAC command type of the command represented, or
     *         <code>Cmd.ALL</code>
     */
    public final int getCommand() {
        return command;
    }

    /**
     * Returns <code>true</code> if these two objects represent the same set of
     * SNAC commands; <code>false</code> otherwise.
     *
     * @param o the other <code>CmdType</code> to compare to
     * @return whether this and the given object represent the exact same set of
     *         SNAC commands
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CmdType)) return false;

        final CmdType other = (CmdType) o;

        return family == other.family && command == other.command;
    }

    /**
     * Returns a hash code relatively unique to this set of SNAC commands.
     *
     * @return a hash code
     */
    public int hashCode() {
        return family << 16 ^ command;
    }

    public String toString() {
        if (family == ALL) {
            return "CmdType: all commands";
        } else if (command == ALL) {
            return "CmdType: all commands in family 0x"
                    + Integer.toHexString(family);
        } else {
            return "CmdType: 0x" + Integer.toHexString(family)
                    + "/0x" + Integer.toHexString(command);
        }
    }
}
