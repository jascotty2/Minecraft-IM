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
 *  File created by keith @ Feb 21, 2003
 *
 */

package net.kano.joscar;

/**
 * A very simple but very common data structure containing a string and the
 * total size of the structure that held that string (including the string
 * itself).
 */
public final class StringBlock {
    /** The string. */
    private final String string;
    /** The length of this object. */
    private final int totalSize;

    /**
     * Creates a new <code>StringBlock</code> object with the given
     * properties.
     *
     * @param string the string read
     * @param totalSize the size of this structure, as read from a block of
     *        binary data
     */
    public StringBlock(String string, int totalSize) {
        DefensiveTools.checkNull(string, "string");
        DefensiveTools.checkRange(totalSize, "totalSize", 0);

        this.string = string;
        this.totalSize = totalSize;
    }

    /**
     * Returns the string read from binary data to create this structure.
     *
     * @return this object's string value
     */
    public final String getString() {
        return string;
    }

    /**
     * Returns the total size of this object, as read from a block of binary
     * data. This is most commonly equivalent to <code>getString().length() +
     * 1</code>, but that is not always the case.
     *
     * @return the total size of this structure: how many bytes were read to
     *         read this string
     */
    public final int getTotalSize() {
        return totalSize;
    }
}
