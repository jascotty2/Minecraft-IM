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
 *  File created by keith @ Feb 24, 2003
 *
 */

package net.kano.joscar.snaccmd.acct;

import net.kano.joscar.flapcmd.SnacCommand;
import net.kano.joscar.snaccmd.conn.SnacFamilyInfo;

/**
 * A base class for all SNAC commands in the <code>0x07</code> "account
 * administration" family.
 */
public abstract class AcctCommand extends SnacCommand {
    /** This family's SNAC family code. */
    public static final int FAMILY_ACCT = 0x0007;

    /** A set of SNAC family info for this family. */
    public static final SnacFamilyInfo FAMILY_INFO =
            new SnacFamilyInfo(FAMILY_ACCT, 0x0001, 0x0010, 0x0361);

    /** The command subtype for modifying an aspect of one's account. */
    public static final int CMD_ACCT_MOD = 0x0004;
    /**
     * The command subtype for requesting a confirmation email for your account.
     */
    public static final int CMD_CONFIRM = 0x0006;
    /** The command subtype for requesting information about one's account. */
    public static final int CMD_INFO_REQ = 0x0002;

    /**
     * The command subtype for a reply sent by the server after modifying one's
     * account.
     */
    public static final int CMD_MOD_ACK = 0x0005;
    /**
     * The command subtype for a reply sent by the server after requesting a
     * confirmation email.
     */
    public static final int CMD_CONFIRM_ACK = 0x0007;
    /**
     * The command subtype for the server's response to a request for account
     * information.
     */
    public static final int CMD_INFO_RESP = 0x0003;

    /**
     * Creates a new SnacCommand in this family.
     *
      * @param command the SNAC command subtype of this command
     */
    protected AcctCommand(int command) {
        super(FAMILY_ACCT, command);
    }
}
