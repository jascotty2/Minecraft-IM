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
 *  File created by keith @ Apr 26, 2003
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
 * A rendezvous command used to initiate a Trillian Secure IM session. This
 * command appears to contain a Diffie-Hellman modulus and public value, but as
 * of this writing, it is unclear what exactly these values mean and how exactly
 * to use/create/send messages over a Secure IM connection. It appears, however,
 * that Trillian always uses a generator (<code>g</code>) value of
 * <code>5</code>. (For fun, try sending a Trillian client a modulus of
 * <code>5</code> in this command.)
 *
 * @see net.kano.joscar.rvcmd.trillcrypt
 */
public class TrillianCryptReqRvCmd extends AbstractTrillianCryptRvCmd {
    /** A TLV type containing a Diffie-Hellman modulus (<code>p</code>). */
    private static final int TYPE_MODULUS = 0x3e9;
    /** A TLV type containing a Diffie-Hellman public value (<code>y</code>). */
    private static final int TYPE_PUBLICVALUE = 0x3ea;

    /** The Diffie-Hellman modulus. */
    private final BigInteger modulus;
    /** The Diffie-Hellman public value. */
    private final BigInteger publicValue;

    /**
     * Creatse a new Trillian Encryption session request from the given incoming
     * Trillian Encryption session request RV ICBM.
     *
     * @param icbm an incoming Trillian Encryption session request RV ICBM
     *        command
     */
    public TrillianCryptReqRvCmd(RecvRvIcbm icbm) {
        super(icbm);

        TlvChain chain = getExtraTlvs();

        Tlv pTlv = chain.getLastTlv(TYPE_MODULUS);

        if (pTlv != null) modulus = getBigIntFromHexBlock(pTlv.getData());
        else modulus = null;

        Tlv gTlv = chain.getLastTlv(TYPE_PUBLICVALUE);

        if (gTlv != null) publicValue = getBigIntFromHexBlock(gTlv.getData());
        else publicValue = null;
    }

    /**
     * Creates a new outgoing Trillian Encryption session request with the given
     * Diffie-Hellman modulus and public value.
     *
     * @param modulus the Diffie-Hellman modulus
     * @param publicValue the Diffie-Hellman public value
     */
    public TrillianCryptReqRvCmd(BigInteger modulus, BigInteger publicValue) {
        super(CMDTYPE_REQUEST);

        this.modulus = modulus;
        this.publicValue = publicValue;
    }

    /**
     * Returns the Diffie-Hellman modulus sent in this command.
     *
     * @return this command's Diffie-Hellman modulus, or <code>null</code> if
     *         none was sent
     */
    public final BigInteger getModulus() { return modulus; }

    /**
     * Returns the Diffie-Hellman public value sent in this command.
     *
     * @return this command's Diffie-Hellman public value, or <code>null</code>
     *         if none was sent
     */
    public final BigInteger getPublicValue() { return publicValue; }

    protected void writeExtraTlvs(OutputStream out) throws IOException {
        if (modulus != null) {
            ByteBlock modBlock = ByteBlock.wrap(getBigIntHexBlock(modulus));
            new Tlv(TYPE_MODULUS, modBlock).write(out);
        }
        if (publicValue != null) {
            ByteBlock pubBlock = ByteBlock.wrap(getBigIntHexBlock(publicValue));
            new Tlv(TYPE_PUBLICVALUE, pubBlock).write(out);
        }
    }

    public String toString() {
        return "TrillianEncryptReqRvCmd: modulus=" + modulus + ", public value="
                + publicValue;
    }
}
