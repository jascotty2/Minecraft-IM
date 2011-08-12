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
 * A SNAC command containing information about one's account. Normally sent in
 * response to an {@link AcctInfoRequest}.
 *
 * @snac.src server
 * @snac.cmd 0x07 0x03
 *
 * @see AcctInfoRequest
 */
public class AcctInfoCmd extends AcctResponse {
    /**
     * Generates an account info response from the given incoming SNAC packet.
     *
     * @param packet an account information response packet
     */
    protected AcctInfoCmd(SnacPacket packet) {
        super(CMD_INFO_RESP, packet);
    }

    /**
     * Creates an outgoing account information response object with the given
     * properties.
     *
     * @param type a "type code" for this response (normally {@link
     *        #TYPE_DEFAULT})
     * @param result a result code (like {@link #RESULT_SUCCESS})
     * @param sn a screenname, or <code>null</code> for none
     * @param email an email address, or <code>null</code> for none
     * @param errorCode an error code like {@link #ERRORCODE_NO_EMAIL}, or
     *        <code>-1</code> for none
     * @param errorUrl an error URL, or <code>null</code> for none
     */
    public AcctInfoCmd(int type, int result, String sn, String email,
            int errorCode, String errorUrl) {
        super(CMD_INFO_RESP, type, result, sn, email, errorCode, errorUrl);
    }

    /**
     * Creates an outgoing account information response containing nothing but
     * the given error code and URL. The command is created with a type of
     * {@link #TYPE_DEFAULT} and a result code of {@link #RESULT_ERROR}.
     *
     * @param errorCode an error code like {@link #ERRORCODE_NO_EMAIL}, or
     *        <code>-1</code> for none
     * @param errorUrl an error URL, or <code>null</code> for none
     */
    public AcctInfoCmd(int errorCode, String errorUrl) {
        super(CMD_INFO_RESP, errorCode, errorUrl);
    }
}
