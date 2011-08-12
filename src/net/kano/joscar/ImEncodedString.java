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
 *  File created by keith @ Apr 25, 2003
 *
 */

package net.kano.joscar;

/**
 * A class representing a string encoded using "IM encoding," or the method of
 * unicode encoding used when sending instant messages.
 *
 * @see net.kano.joscar.ImEncodingParams
 */
public final class ImEncodedString {
    /** The encoding used to encode this string. */
    private final ImEncodingParams encoding;
    /** The encoded string. */
    private final byte[] bytes;
    /** The string encoded using IM encoding. */
    private final String string;

    /**
     * Creates a new <code>String</code> from the given block of binary data
     * and the given encoding.
     *
     * @param encoding an object representing the IM encoding parameters to be
     *        used in decoding the given block of data into a string
     * @param block a block of data containing a string encoded with the given
     *        IM encoding parameters
     * @return a <code>String</code> decoded from the given block with the given
     *         encoding
     */
    public static String readImEncodedString(ImEncodingParams encoding,
            ByteBlock block) {
        DefensiveTools.checkNull(encoding, "encoding");
        DefensiveTools.checkNull(block, "block");

        return OscarTools.getString(block, encoding.toCharsetName());
    }

    /**
     * Returns an object representing the given string encoded with IM encoding
     * as well as the encoding parameters used.
     *
     * @param string the string to encode
     * @return an object containing the IM encoding parameters used to encode
     *         the given string as well as the encoded string itself
     */
    public static ImEncodedString encodeString(String string) {
        DefensiveTools.checkNull(string, "string");

        return new ImEncodedString(string);
    }

    /**
     * Creates a new <code>ImEncodedString</code> by encoding the given string.
     *
     * @param string the string to encode
     */
    private ImEncodedString(String string) {
        EncodedStringInfo encInfo = MinimalEncoder.encodeMinimally(string);

        this.bytes = encInfo.getData();
        this.string = string;
        this.encoding = encInfo.getImEncoding();
    }

    /**
     * Returns the IM encoding parameters used to encode the associated string.
     *
     * @return the IM encoding parameters used to encode this string
     */
    public final ImEncodingParams getEncoding() { return encoding; }

    /**
     * Returns the raw bytes of the encoded string.
     *
     * @return the raw bytes of the associated string
     */
    public final byte[] getBytes() {
        return (byte[]) bytes.clone();
    }

    /**
     * Returns the string represented by this object.
     *
     * @return this object's associated string
     */
    public final String getString() { return string; }
}
