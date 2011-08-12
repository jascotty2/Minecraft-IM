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

import net.kano.joscar.flapcmd.SnacPacket;

/**
 * A SNAC command sent in response to an {@link AcctModCmd}.
 *
 * @snac.src server
 * @snac.cmd 0x07 0x05
 *
 * @see AcctModCmd
 */
public class AcctModAck extends AcctResponse {
    /**
     * An error code indicating that the user attempted to reformat his or her
     * screenname in a way that made it a different screenname.
     * @see net.kano.joscar.OscarTools#normalize
     */
    public static final int ERRORCODE_DIFFERENT_SN = 0x0001;
    /**
     * An error code indicating that the newly formatted screenname ends with a
     * space and is thus an invalid screenname.
     */
    public static final int ERRORCODE_SN_ENDS_WITH_SPACE = 0x0006;
    /**
     * An error code indicating that the newly formatted screenname is too long
     * (try removing some spaces).
     */
    public static final int ERRORCODE_SN_TOO_LONG = 0x000b;

    /**
     * An error code indicating that the registered email address cannot be
     * changed while another address change is pending authorization.
     */
    public static final int ERRORCODE_CANT_CHANGE_TWICE = 0x001d;
    /**
     * An error code indicating that the attempted change in this user's
     * registered email address would result in that email address having
     * too many screennames registered under it.
     */
    public static final int ERRORCODE_EMAIL_HAS_TOO_MANY_SCREENNAMES = 0x0021;
    /**
     * An error code indicating that the new registered email address is not
     * a valid email address.
     */
    public static final int ERRORCODE_INVALID_EMAIL = 0x0023;

    /**
     * Creates an account modification response object from the given incoming
     * SNAC packet.
     *
     * @param packet the account modification response SNAC packet
     */
    protected AcctModAck(SnacPacket packet) {
        super(CMD_MOD_ACK, packet);
    }

    /**
     * Creates an outgoing account modification response command with the given
     * properties.
     *
     * @param type a result type (normally {@link #TYPE_DEFAULT})
     * @param result a result code (like {@link #RESULT_SUCCESS})
     * @param sn a screen name, or <code>null</code> for none
     * @param email an email address, or <code>null</code> for none
     * @param errorCode an error code, or <code>-1</code> for none
     * @param errorUrl an error URL, or <code>-1</code> for none
     */
    public AcctModAck(int type, int result, String sn, String email,
            int errorCode, String errorUrl) {
        super(CMD_MOD_ACK, type, result, sn, email, errorCode, errorUrl);
    }

    /**
     * Creates an outgoing account modification response command with a type of
     * {@link #TYPE_DEFAULT} and a result code of {@link #RESULT_ERROR}.
     *
     * @param errorCode an error code, like {@link
     *        #ERRORCODE_EMAIL_HAS_TOO_MANY_SCREENNAMES}
     * @param errorURL an error URL, or <code>null</code> for none
     */
    public AcctModAck(int errorCode, String errorURL) {
        super(CMD_MOD_ACK, errorCode, errorURL);
    }
}
