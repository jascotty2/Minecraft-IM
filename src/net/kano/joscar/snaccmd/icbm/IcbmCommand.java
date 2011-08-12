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
 *  File created by keith @ Mar 6, 2003
 *
 */

package net.kano.joscar.snaccmd.icbm;

import net.kano.joscar.flapcmd.SnacCommand;
import net.kano.joscar.snaccmd.conn.SnacFamilyInfo;

/**
 * A base class for all SNAC commands in the ICBM <code>0x04</code> family.
 */
public abstract class IcbmCommand extends SnacCommand {
    /** The SNAC family for ICBM's. */
    public static final int FAMILY_ICBM = 0x0004;

    /** A set of SNAC family information for the ICBM family. */
    public static final SnacFamilyInfo FAMILY_INFO
            = new SnacFamilyInfo(FAMILY_ICBM, 0x0001, 0x0110, 0x739);

    /** A command type for requesting ICBM parameter information. */
    public static final int CMD_PARAM_INFO_REQ = 0x0004;
    /** A command type for sending ICBM parameter information to the client. */
    public static final int CMD_PARAM_INFO = 0x0005;
    /** A command type for sending ICBM parameter information to the server. */
    public static final int CMD_SET_PARAM_INFO = 0x0002;
    /** A command type for sending ICBM's to the client. */
    public static final int CMD_ICBM = 0x0007;
    /** A command type for sending ICBM's to another user (to the server). */
    public static final int CMD_SEND_ICBM = 0x0006;
    /** A command type for sending a typing notification to another user. */
    public static final int CMD_SEND_TYPING = 0x0014;
    /** A command type for sending a typing notification to the client. */
    public static final int CMD_RECV_TYPING = CMD_SEND_TYPING;
    /** A command type for warning another user. */
    public static final int CMD_WARN = 0x0008;
    /** A command type for informing the client of messages it missed. */
    public static final int CMD_MISSED = 0x000a;
    /** A command type indicating that a rendezvous failed .*/
    public static final int CMD_RV_RESPONSE = 0x000b;
    /** A command type for acknowledging an IM's sending. */
    public static final int CMD_MSG_ACK = 0x000c;

    /**
     * Creates a new SNAC command in the ICBM family with the given command
     * subtype.
     *
     * @param command the SNAC command subtype of this command
     */
    protected IcbmCommand(int command) {
        super(FAMILY_ICBM, command);
    }
}