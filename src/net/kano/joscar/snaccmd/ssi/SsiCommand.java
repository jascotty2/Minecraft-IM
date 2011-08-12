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
 *  File created by keith @ Mar 3, 2003
 *
 */

package net.kano.joscar.snaccmd.ssi;

import net.kano.joscar.flapcmd.SnacCommand;
import net.kano.joscar.snaccmd.conn.SnacFamilyInfo;

/**
 * A base class for commands in the server-stored information <code>0x13</code>
 * SNAC family.
 */
public abstract class SsiCommand extends SnacCommand {
    /** The SNAC family code for the SSI family. */
    public static final int FAMILY_SSI = 0x0013;

    /** A set of SNAC family information for this family. */
    public static final SnacFamilyInfo FAMILY_INFO
            = new SnacFamilyInfo(FAMILY_SSI, 0x0003, 0x0110, 0x0739);

    /** A command subtype for requesting SSI-related "rights." */
    public static final int CMD_RIGHTS_REQ = 0x0002;
    /** A command subtype for requesting the user's SSI data. */
    public static final int CMD_DATA_REQ = 0x0004;
    /**
     * A command subtype for requesting the user's SSI data if it has
     * changed.
     */
    public static final int CMD_DATA_CHECK = 0x0005;
    /** A command subtype for "activating" the SSI data. */
    public static final int CMD_ACTIVATE = 0x0007;
    /** A command subtype for creating new server-stored "items." */
    public static final int CMD_CREATE_ITEMS = 0x0008;
    /** A command subtype for modifying existing server-stored "items." */
    public static final int CMD_MODIFY_ITEMS = 0x0009;
    /** A command subtype for deleting server-stored "items." */
    public static final int CMD_DELETE_ITEMS = 0x000a;
    /** A command subtype sometimes sent before changing SSI data. */
    public static final int CMD_PRE_MOD = 0x0011;
    /** A command subtype sometimes sent after changing SSI data. */
    public static final int CMD_POST_MOD = 0x0012;

    /**
     * A command subtype for sending the client a list of SSI-related "rights."
     */
    public static final int CMD_RIGHTS = 0x0003;
    /** A command subtype for sending SSI data to the client. */
    public static final int CMD_SSI_DATA = 0x0006;
    /**
     * A command subtype for telling the user that the SSI data have not changed
     *  since the last time the client saw them.
     */
    public static final int CMD_UNCHANGED = 0x000f;
    /** A command subtype for acknowledging a change to SSI data. */
    public static final int CMD_MOD_ACK = 0x000e;

    /**
     * Creates a new SNAC command in the SSI family.
     *
     * @param command the SNAC command subtype
     */
    protected SsiCommand(int command) {
        super(FAMILY_SSI, command);
    }
}
