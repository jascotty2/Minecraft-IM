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
 *  File created by keith @ Apr 27, 2003
 *
 */

package net.kano.joscar.rvcmd.trillcrypt;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.snaccmd.icbm.RecvRvIcbm;
import net.kano.joscar.tlv.Tlv;
import net.kano.joscar.tlv.TlvChain;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A rendezvous command used to send an encrypted message over a Trillian
 * Secure IM connection.
 */
public class TrillianCryptMsgRvCmd extends AbstractTrillianCryptRvCmd {
    /** A TLV type containing the encrypted message. */
    private static final int TYPE_MSG = 0x3f2;

    /** The encrypted message. */
    private final ByteBlock encryptedMsg;

    /**
     * Creates a new Trillian Encrypted message command from the given incoming
     * encrypted message RV ICBM.
     *
     * @param icbm an incoming Trillian Encrypted message RV ICBM command
     */
    public TrillianCryptMsgRvCmd(RecvRvIcbm icbm) {
        super(icbm);

        TlvChain chain = getExtraTlvs();

        Tlv encMsgTlv = chain.getLastTlv(TYPE_MSG);
        if (encMsgTlv == null) encryptedMsg = null;
        else encryptedMsg = encMsgTlv.getData();
    }

    /**
     * Creates a new outgoing Trillian Encrypted message command with the given
     * encrypted message data.
     *
     * @param encryptedMsg the encrypted message data
     */
    public TrillianCryptMsgRvCmd(ByteBlock encryptedMsg) {
        super(CMDTYPE_MESSAGE);

        this.encryptedMsg = encryptedMsg;
    }

    /**
     * Returns the encrypted message block sent in this command.
     *
     * @return this command's encrypted message block, or <code>null</code> if
     *         none was sent
     */
    public final ByteBlock getEncryptedMsg() { return encryptedMsg; }

    protected void writeExtraTlvs(OutputStream out) throws IOException {
        if (encryptedMsg != null) new Tlv(TYPE_MSG, encryptedMsg).write(out);
    }

    public String toString() {
        return "TrillianEncryptMsgRvCmd: msg="
                + BinaryTools.describeData(encryptedMsg);
    }
}
