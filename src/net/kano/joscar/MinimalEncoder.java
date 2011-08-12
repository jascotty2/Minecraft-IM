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
 *  File created by keith @ Mar 1, 2003
 *
 */

package net.kano.joscar;

import java.io.UnsupportedEncodingException;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Provides a means of encoding a set of strings in the "smallest" encoding
 * possible. This is useful when a set of data needs to have each member encoded
 * using the same charset, as in <code>DirInfo</code>.
 */
public final class MinimalEncoder {
    /** US-ASCII encoding. */
    public static final String ENCODING_ASCII = "us-ascii";
    /** ISO-8859-1 encoding. */
    public static final String ENCODING_ISO   = "iso-8859-1";
    /** UTF-16BE encoding. */
    public static final String ENCODING_UTF16 = "utf-16be";
    /** UTF-8 encoding. */
    public static final String ENCODING_UTF8  = "utf-8";

    /**
     * One of a set of charset objects cached used to increase performance of
     * <code>MinimalEncoder</code>.
     */
    private static final Charset ASCII7;
    /**
     * One of a set of charset objects cached used to increase performance of
     * <code>MinimalEncoder</code>.
     */
    private static final Charset ISO88591;
    /**
     * One of a set of charset objects cached used to increase performance of
     * <code>MinimalEncoder</code>.
     */
    private static final Charset UTF16;

    /**
     * A list of valid charsets to use to encode strings within
     * <code>MinimalEncoder</code>, in order of increasing complexity (and thus
     * the order in which they will be tried).
     */
    private static final List validCharsets
            = Collections.unmodifiableList(Arrays.asList(new Object[] {
                ENCODING_ASCII, ENCODING_ISO, ENCODING_UTF16, ENCODING_UTF8
            }));

    static {
        Charset ascii = null;
        try {
            ascii = Charset.forName("US-ASCII");
        } catch (UnsupportedCharsetException ohwell) { }
        ASCII7 = ascii;

        Charset iso = null;
        try {
            iso = Charset.forName("ISO-8859-1");
        } catch (UnsupportedCharsetException ohwell) { }
        ISO88591 = iso;

        Charset utf = null;
        try {
            utf = Charset.forName("UTF-16BE");
        } catch (UnsupportedCharsetException ohwell) { }
        UTF16 = utf;
    }

    /**
     * Encodes a string minimally. Simply a shortcut for <code>new
     * MinimalEncoder().{@linkplain #encode encode}(str)</code>.
     *
     * @param str the string to minimally encode
     * @return an object describing the encoded string and the charset used to
     *         encode it
     */
    public static EncodedStringInfo encodeMinimally(String str) {
        return new MinimalEncoder().encode(str);
    }

    /**
     * The currently minimal charset that can be used to encode. Starts at
     * the "lowest" charset, the first index of <code>validCharsets</code>.
     */
    private String charset = (String) validCharsets.get(0);

    /**
     * Stores the last charset used, since most of the time the same charset
     * will be necessary for subsequent strings.
     */
    private String lastCharset = null;

    /**
     * Stores the last encoder used, since most of the time the same encoder
     * will be necessary for subsequent strings.
     */
    private CharsetEncoder lastEncoder = null;

    /**
     * Returns the given charset name if the given charset can encode the given
     * string, and <code>null</code> otherwise.
     *
     * @param str the string to test for encodability
     * @param charset the charset to use to test encodability of the given
     *        string
     * @param name the charset name to return if the given charset can encode
     *        the given string
     * @return the given charset name if the given charset can encode the given
     *         string; <code>null</code> otherwise
     */
    private static String getCharset(String str, Charset charset,
            String name) {
        if (charset != null && charset.newEncoder().canEncode(str)) return name;
        else return null;
    }

    /**
     * Returns the name of the minimum charset that can encode the given string.
     *
     * @param str the string to encode
     * @return the name of the minimum charset that can encode the given string
     */
    private static String getMinimalCharset(String str) {
        String encoder = getCharset(str, ASCII7, ENCODING_ASCII);
        if (encoder != null) return encoder;

        encoder = getCharset(str, ISO88591, ENCODING_ISO);
        if (encoder != null) return encoder;

        encoder = ENCODING_UTF16;

        return encoder;
    }

    /**
     * Updates the current minimum charset to accommodate the given string. For
     * example, if the current minimum charset were {@link #ENCODING_ASCII},
     * after calling <code>update(</code><i>hebrew text</i><code>)</code> the
     * minimum charset would probably be {@link #ENCODING_UTF16}.
     *
     * @param str the string to accommodate
     */
    public synchronized final void update(String str) {
        String encoderType = getMinimalCharset(str);

        if (validCharsets.indexOf(encoderType)
                > validCharsets.indexOf(charset)) {
            charset = encoderType;
        }
    }

    /**
     * Simply a utility method for calling <code>update</code> on an entire
     * array of strings.
     *
     * @param strings the strings to accommodate
     */
    public synchronized final void updateAll(String[] strings) {
        for (int i = 0; i < strings.length; i++) {
            update(strings[i]);
        }
    }

    /**
     * Returns the current minimal charset that can be used to encode all
     * strings that have been passed to <code>update</code> thus far. Will
     * <i>always</i> be one of {@link #ENCODING_ASCII}, {@link #ENCODING_ISO},
     * {@link #ENCODING_UTF16}, or in very rare cases (namely, a VM without
     * UTF-16BE encoding) {@link #ENCODING_UTF8}.
     *
     * @return the current minimal charset
     */
    public synchronized final String getCharset() {
        return charset;
    }

    /**
     * Encodes the given string using the minimal encoding computed from
     * previous calls to <code>update</code>. See {@link #getCharset()} for
     * details on possible values of the returned
     * <code>EncodedStringInfo</code>'s charset field.
     *
     * @param str the string to encode
     * @return an object describing the encoded string and the charset with
     *         which it was encoded
     */
    public synchronized final EncodedStringInfo encode(String str) {
        update(str);

        CharsetEncoder encoder;
        if (lastCharset == charset && lastEncoder != null) {
            encoder = lastEncoder;
        } else {
            if (charset == ENCODING_ASCII) encoder = ASCII7.newEncoder();
            else if (charset == ENCODING_ISO) encoder = ISO88591.newEncoder();
            else encoder = UTF16.newEncoder();

            lastCharset = charset;
            lastEncoder = encoder;
        }

        try {
            return new EncodedStringInfo(charset,
                    encoder.encode(CharBuffer.wrap(str)).array());
        } catch (CharacterCodingException uhoh) {
            try {
                return new EncodedStringInfo(ENCODING_UTF8,
                        str.getBytes(ENCODING_UTF8));
            } catch (UnsupportedEncodingException impossibler) { return null; }
        }
    }
}
