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

package net.kano.joscar.snaccmd.conn;

import net.kano.joscar.flapcmd.SnacCommand;

/**
 * A base class for commands in the "service" or "basic connection"
 * <code>0x01</code> family.
 */
public abstract class ConnCommand extends SnacCommand {
    /** The family code of this SNAC family. */
    public static final int FAMILY_CONN = 0x0001;

    /** A set of SNAC family information for this family. */
    public static final SnacFamilyInfo FAMILY_INFO
            = new SnacFamilyInfo(FAMILY_CONN, 0x0003, 0x0110, 0x0739);

    /** A command subtype for sending the client's SNAC family versions. */
    public static final int CMD_CLIENT_VERS = 0x0017;
    /** A command subtype for requesting rate limiting information. */
    public static final int CMD_RATE_REQ = 0x0006;
    /** A command subtype for acknowledging previously sent rate information. */
    public static final int CMD_RATE_ACK = 0x0008;
    /** A command subtype for requesting information about oneself. */
    public static final int CMD_MY_INFO_REQ = 0x000e;
    /** A command subtype for when the client is ready to "go online." */
    public static final int CMD_CLIENT_READY = 0x0002;
    /** A command subtype for requesting a new SNAC service. */
    public static final int CMD_SERVICE_REQ = 0x0004;
    /** A command subtype for acknowledging a "server pause." */
    public static final int CMD_PAUSE_ACK = 0x000c;
    /** A command subtype for setting one's idle time. */
    public static final int CMD_SET_IDLE = 0x0011;
    /** A command subtype for setting one's "extra info blocks." */
    public static final int CMD_SETEXTRAINFO = 0x001e;

    /** A command subtype for setting one's security information. */
    public static final int CMD_SETENCINFO = 0x0022;

    /**
     * A command subtype for when the server is ready for the client to begin
     * logging in.
     */
    public static final int CMD_SERVER_READY = 0x0003;
    /**
     * A command subtype for sending the client the server's SNAC family
     * versions.
     */
    public static final int CMD_SERV_VERS = 0x0018;
    /** A command subtype for sending the client rate limiting information. */
    public static final int CMD_RATE_INFO = 0x0007;
    /** A command subtype for sending the client his or her user information. */
    public static final int CMD_YOUR_INFO = 0x000f;
    /** A command subtype for informing the client that he was warned. */
    public static final int CMD_WARNED = 0x0010;
    /**
     * A command subtype for informing the client of a new version of his
     * client software.
     */
    public static final int CMD_UPDATE = 0x0013;
    /**
     * A command subtype for indicating to a client that rate limiting values
     * changed.
     */
    public static final int CMD_RATE_CHG = 0x000a;
    /** A command subtype for doing nothing at all. */
    public static final int CMD_NOOP = 0x0016;
    /**
     * A command subtype for redirecting a client to a new server for a certain
     * SNAC service.
     */
    public static final int CMD_SERVICE_REDIR = 0x0005;
    /** A command subtype for telling a client to "pause." */
    public static final int CMD_PAUSE = 0x000b;
    /** A command subtype for telling a client to resume from pausing. */
    public static final int CMD_RESUME = 0x000d;
    /**
     * A command subtype for telling a client to "migrate" to another server.
     */
    public static final int CMD_MIGRATE_PLS = 0x0012;
    /** A command subtype for telling the client what his buddy icon is. */
    public static final int CMD_EXTRA_ACK = 0x0021;
    /**
     * A command subtype acknowledging the setting of one's security
     * information.
     */
    public static final int CMD_ENCINFOACK = 0x0023;

    /**
     * Creates a new SNAC command in this family.
     *
     * @param command the SNAC command subtype
     */
    protected ConnCommand(int command) {
        super(FAMILY_CONN, command);
    }
}
