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

import net.kano.joscar.ByteBlock;
import net.kano.joscar.snaccmd.icbm.RecvRvIcbm;
import net.kano.joscar.tlv.Tlv;
import net.kano.joscar.tlv.TlvChain;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;

/**
 * A rendezvous command used to accept a Trillian Encryption session. This
 * command appears to send back a Diffie-Hellman public value (<code>y</code>),
 * but as of this writing it is still unclear how the values fit together.
 */
public class TrillianCryptAcceptRvCmd extends AbstractTrillianCryptRvCmd {
    /** A TLV type containing a Diffie-Hellman public value. */
    private static final int TYPE_PUBLICVALUE = 0x03eb;

    /** The Diffie-Hellman public value. */
    private final BigInteger publicValue;

    /**
     * Creates a new Trillian Encryption session acceptance command from the
     * given incoming session acceptance RV ICBM.
     *
     * @param icbm an incoming Trillian Encryption session acceptance RV ICBM
     *        command
     */
    public TrillianCryptAcceptRvCmd(RecvRvIcbm icbm) {
        super(icbm);

        TlvChain chain = getExtraTlvs();

        Tlv codeTlv = chain.getLastTlv(TYPE_PUBLICVALUE);

        BigInteger publicValue = null;
        if (codeTlv != null) {
            publicValue = getBigIntFromHexBlock(codeTlv.getData());
        }
        this.publicValue = publicValue;
    }

    /**
     * Creates a new outgoing Trillian Encryption session acceptance command
     * with the given Diffie-Hellman public value.
     *
     * @param publicValue a Diffie-Hellman public value
     */
    public TrillianCryptAcceptRvCmd(BigInteger publicValue) {
        super(CMDTYPE_ACCEPT);

        this.publicValue = publicValue;
    }

    /**
     * Returns the Diffie-Hellman public value sent in this command.
     *
     * @return this command's Diffie-Hellman public value, or <code>null</code>
     *         if none was sent
     */
    public final BigInteger getPublicValue() { return publicValue; }

    protected void writeExtraTlvs(OutputStream out) throws IOException {
        if (publicValue != null) {
            byte[] hexBlock = getBigIntHexBlock(publicValue);
            new Tlv(TYPE_PUBLICVALUE, ByteBlock.wrap(hexBlock)).write(out);
        }
    }

    public String toString() {
        return "TrillianEncryptAcceptRvCmd: code=" + publicValue;
    }
}
