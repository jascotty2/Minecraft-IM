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

package net.kano.joscar.snaccmd.loc;

import net.kano.joscar.flapcmd.SnacCommand;
import net.kano.joscar.snaccmd.conn.SnacFamilyInfo;

/**
 * A base class for the commands in the "location" <code>0x02</code> SNAC
 * family.
 */
public abstract class LocCommand extends SnacCommand {
    /** The SNAC family code for the location family. */
    public static final int FAMILY_LOC = 0x0002;

    /** A set of SNAC family information for this family. */
    public static final SnacFamilyInfo FAMILY_INFO
            = new SnacFamilyInfo(FAMILY_LOC, 0x0001, 0x0110, 0x0739);

    /** A command subtype for requesting location-related rights. */
    public static final int CMD_RIGHTS_REQ = 0x0002;
    /** A command subtype containing location-related "rights." */
    public static final int CMD_RIGHTS_RESP = 0x0003;
    /** A command subtype for setting one's "info." */
    public static final int CMD_SET_INFO = 0x0004;
    /** A command subtype formerly used for getting another user's "info." */
    public static final int CMD_OLD_GET_INFO = 0x0005;
    /** A command subtype containing a user's "info." */
    public static final int CMD_USER_INFO = 0x0006;
    /**
     * A command subtype for requesting another user's directory information.
     */
    public static final int CMD_GET_DIR = 0x000b;
    /** A command subtype containing a user's directory information. */
    public static final int CMD_DIR_INFO = 0x000c;
    /** A command subtype for setting one's chat interests. */
    public static final int CMD_SET_INTERESTS = 0x000f;
    /**
     * A command subtype for acknowledging that one's chat interests were set.
     */
    public static final int CMD_INTEREST_ACK = 0x0010;
    /** A command subtype for setting your directory information. */
    public static final int CMD_SET_DIR = 0x0009;
    /**
     * A command subtype for acknowledging the setting of one's directory
     * information.
     */
    public static final int CMD_SET_DIR_ACK = 0x000a;
    /** A command subtype used to request information about a user. */
    public static final int CMD_NEW_GET_INFO = 0x0015;

    /**
     * Creates a new command in the location family.
     *
     * @param command the SNAC command subtype
     */
    protected LocCommand(int command) {
        super(FAMILY_LOC, command);
    }
}
