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
 *  File created by keith @ Feb 27, 2003
 *
 */

package net.kano.joscar;



/**
 * A simple structure containing a block of data and the name of the charset
 * that was used to encode that block of data from a text string.
 */
public final class EncodedStringInfo {
    /**
     * The charset used to encode a string to <code>data</code>.
     */
    private final String charset;

    /**
     * A block of data encoded with the associated charset.
     */
    private final byte[] data;

    /**
     * Creates a new <code>EncodedStringInfo</code> with the given properties.
     *
     * @param charset the character set used to encode the given data
     * @param data the data encoded by the given character set
     */
    public EncodedStringInfo(String charset, byte[] data) {
        DefensiveTools.checkNull(charset, "charset");
        DefensiveTools.checkNull(data, "data");

        this.charset = charset;
        this.data = (byte[]) data.clone();
    }

    /**
     * Returns the name of the charset that encoded the associated data from
     * a <code>String</code>.
     *
     * @return the name of the charset that encoded the associated data block
     */
    public final String getCharset() {
        return charset;
    }

    /**
     * Returns an <code>ImEncoding</code> object that describes the charset
     * used to encode the associated string.
     *
     * @return an <code>ImEncoding</code> describing the charset that encoded
     *         the string associated with this <code>EncodedStringInfo</code>
     *
     * @see net.kano.joscar.ImEncodedString
     */
    public final ImEncodingParams getImEncoding() {
        int charsetCode;
        int charsetSubcode;
        if (charset == MinimalEncoder.ENCODING_ASCII) {
            charsetCode = ImEncodingParams.CHARSET_ASCII;
        } else if (charset == MinimalEncoder.ENCODING_ISO) {
            charsetCode = ImEncodingParams.CHARSET_ISO;
        } else if (charset == MinimalEncoder.ENCODING_UTF16) {
            charsetCode = ImEncodingParams.CHARSET_UTF16;
        } else {
            // this shouldn't ever really happen, but it's nice to have
            // something in case it does.
            charsetCode = ImEncodingParams.CHARSET_ASCII;
        }

        // this is always the same value
        charsetSubcode = ImEncodingParams.CHARSUBSET_DEFAULT;

        return new ImEncodingParams(charsetCode, charsetSubcode);
    }

    /**
     * The data block encoded from a <code>String</code> by the associated
     * charset.
     *
     * @return the data block encoded by the associated charset
     */
    public final byte[] getData() {
        return (byte[]) data.clone();
    }
}
