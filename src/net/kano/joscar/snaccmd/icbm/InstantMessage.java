/*
 *  Copyright (c) 2003, The Joust Project
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
 *  File created by keith @ Aug 25, 2003
 *
 */

package net.kano.joscar.snaccmd.icbm;

import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;

/**
 * Represents a single instant message, either plain or encrypted.
 */
public class InstantMessage {
    /** A default encryption code used by the offiical AIM clients. */
    public static final int ENCRYPTIONCODE_DEFAULT = 0x0001;

    /** Whether this IM is encrypted. */
    private final boolean encrypted;
    /** The message as a string. */
    private final String message;
    /** The encryption code. */
    private final int encryptionCode;
    /** The encrypted data. */
    private final ByteBlock encryptedData;

    /**
     * Creates a new plaintext (unencrypted) instant message object with the
     * given message text.
     *
     * @param message the message body text
     */
    public InstantMessage(String message) {
        DefensiveTools.checkNull(message, "message");

        encrypted = false;

        this.message = message;
        this.encryptionCode = -1;
        this.encryptedData = null;
    }

    /**
     * Creates a new encrypted instant message with the given encrypted data and
     * an encryption code of {@link #ENCRYPTIONCODE_DEFAULT}.
     *
     * @param encryptedData the encrypted message block
     */
    public InstantMessage(ByteBlock encryptedData) {
        this(ENCRYPTIONCODE_DEFAULT, encryptedData);
    }

    /**
     * Creates a new encrypted instant message with the given encryption code
     * and the given encrypted message data block.
     *
     * @param encryptionCode an encryption code, normally {@link
     *        #ENCRYPTIONCODE_DEFAULT}
     * @param encryptedData the encrypted message block
     */
    public InstantMessage(int encryptionCode, ByteBlock encryptedData) {
        DefensiveTools.checkRange(encryptionCode, "encryptionCode", 0);
        DefensiveTools.checkNull(encryptedData, "encryptedData");

        encrypted = true;

        this.message = null;
        this.encryptionCode = encryptionCode;
        this.encryptedData = encryptedData;
    }

    /**
     * Returns whether or not this message is encrypted. If the returned value
     * is <code>true</code>, encrypted message data can be retrieved using
     * {@link #getEncryptedData()}. If the returned value is <code>false</code>,
     * the message body text can be retrieved using {@link #getMessage()}.
     *
     * @return whether or not this message is encrypted
     */
    public final boolean isEncrypted() { return encrypted; }

    /**
     * Returns the text of the message, if unencrypted. This method will always
     * return <code>null</code> if {@link #isEncrypted()} is <code>true</code>;
     * it will never return <code>null</code> otherwise.
     *
     * @return the text of this message, if unencrypted
     */
    public final String getMessage() { return message; }

    /**
     * Returns the encryption code stored in this message, if any. As of this
     * writing, the significance of this value is unknown, but it appears that
     * the official AIM clients always send {@link #ENCRYPTIONCODE_DEFAULT}.
     * Note that this method will always return <code>-1</code> if {@link
     * #isEncrypted()} is <code>false</code>, and may still return
     * <code>-1</code> otherwise (if no encryption code was included in this
     * message).
     *
     * @return the encryption code stored in this message, or <code>-1</code> if
     *         none was sent or if this message is not encrypted
     */
    public final int getEncryptionCode() { return encryptionCode; }

    /**
     * Returns the encrypted message data stored in this message, if encrypted.
     * Note that this method will always return <code>null</code> if {@link
     * #isEncrypted()} is <code>false</code>, and will never return
     * <code>null</code> otherwise.
     *
     * @return the encrypted message data
     */
    public final ByteBlock getEncryptedData() { return encryptedData; }

    public String toString() {
        return "IM: " + (message != null ? message
                : "<encrypted, code=" + encryptionCode + ">");
    }
}
