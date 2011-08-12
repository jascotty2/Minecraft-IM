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
 *  File created by keith @ Apr 28, 2003
 *
 */

package net.kano.joscar.rvcmd;

import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.EncodedStringInfo;
import net.kano.joscar.LiveWritable;
import net.kano.joscar.MinimalEncoder;
import net.kano.joscar.tlv.Tlv;
import net.kano.joscar.tlv.TlvChain;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

/**
 * A data structure containing an invitation message string and a language in
 * which that message was written. This structure is used in several rendezvous
 * types, including inviting someone to a chat room and sending someone a file.
 */
public class InvitationMessage implements LiveWritable {
    /**
     * Returns an <code>InvitationMessage</code> with properties read from the
     * given TLV chain. Note that this method will never return
     * <code>null</code>, even if no invitation message fields exist in the
     * given TLV chain.
     *
     * @param chain a TLV chain containing invitation message TLV's
     * @return an <code>InvitationMessage</code> read from the given TLV chain
     */
    public static InvitationMessage readInvitationMessage(TlvChain chain) {
        DefensiveTools.checkNull(chain, "chain");

        String charset = chain.getString(TYPE_CHARSET);

        String message = (charset == null
                ? chain.getString(TYPE_MESSAGE)
                : chain.getString(TYPE_MESSAGE, charset));

        String languageStr = chain.getString(TYPE_LANGUAGE);
        Locale language = languageStr == null ? null : new Locale(languageStr);

        return new InvitationMessage(message, language);
    }

    /** A TLV type containing a language code. */
    private static final int TYPE_LANGUAGE = 0x000e;
    /** A TLV type containing a charset name. */
    private static final int TYPE_CHARSET = 0x000d;
    /** A TLV type containing an invitation message. */
    private static final int TYPE_MESSAGE = 0x000c;

    /**
     * An object representing the language in which the invitation message was
     * written.
     */
    private final Locale language;
    /** The invitation message. */
    private final String message;

    /**
     * Creates a new invitation message with the given message body and with
     * the JVM's current language. Using this constructor is equivalent to using
     * {@link #InvitationMessage(String, Locale) new InvitationMessage(message,
     * Locale.getDefault())}.
     *
     * @param message the invitation message text, or <code>null</code> for none
     */
    public InvitationMessage(String message) {
        this(message, Locale.getDefault());
    }

    /**
     * Creates a new invitation message object with the given message body and
     * the language code of the given <code>Locale</code>. Either field can be
     * <code>null</code>, indicating that that field is not present in this
     * invitation message block.
     *
     * @param message the invitation message body
     * @param language a <code>Locale</codE> object whose associated language
     *        represents the language in which the invitation message was
     *        written
     */
    public InvitationMessage(String message, Locale language) {
        this.language = language;
        this.message = message;
    }

    /**
     * Returns a <code>Locale</code> object whose language field represents the
     * language used to write the {@linkplain #getMessage invitation message
     * body}. Note that the returned value will be <code>null</code> if no such
     * field was sent.
     *
     * @return a <code>Locale</code> whose language field represents the
     *         language in which the associated invitation message was written,
     *         or <code>null</code> if no language code was sent in this
     *         invitation message block
     */
    public final Locale getLanguage() { return language; }

    /**
     * Returns the body of the invitation message contained in this invitation
     * message block, or <code>null</code> if none is present.
     *
     * @return the body of the invitation message in this block, or
     *         <code>null</code> if none is present
     */
    public final String getMessage() { return message; }

    public void write(OutputStream out) throws IOException {
        if (language != null) {
            String lang = language.getLanguage();
            Tlv.getStringInstance(TYPE_LANGUAGE, lang).write(out);
        }

        if (message != null) {
            EncodedStringInfo encInfo = MinimalEncoder.encodeMinimally(message);
            String charset = encInfo.getCharset();

            Tlv.getStringInstance(TYPE_CHARSET, charset).write(out);
            new Tlv(TYPE_MESSAGE, ByteBlock.wrap(encInfo.getData())).write(out);
        }
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InvitationMessage)) return false;

        InvitationMessage other = (InvitationMessage) o;

        if (language != null
                ? !language.getLanguage().equals(other.language.getLanguage())
                : other.language != null) return false;
        if (message != null
                ? !message.equals(other.message)
                : other.message != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (language != null ? language.hashCode() : 0);
        result = 29 * result + (message != null ? message.hashCode() : 0);
        return result;
    }

    public String toString() {
        return "InvitationMessage: \"" + message + "\" ("
                + (language == null ? "language=null"
                : "in " + language.getLanguage()) + ")";
    }
}
