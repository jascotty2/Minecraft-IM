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
 *  File created by keith @ Feb 22, 2003
 *
 */

package net.kano.joscar.snaccmd;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.LiveWritable;
import net.kano.joscar.OscarTools;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A data structure containing a screen name and warning level.
 */
public class MiniUserInfo implements LiveWritable {
    /** The user's screen name. */
    private final String sn;
    /** The user's warning level. */
    private final int warningLevel;
    /**
     * The total size of this structure, as read from a block of binary data.
     */
    private final int totalSize;

    /**
     * Returns a mini user info object generated from the given block of data,
     * or <code>null</code> if no valid user info block exists in the given
     * block.
     *
     * @param block a block of bytes containing a mini user info block
     * @return a mini user info object read from the given byte block
     */
    public static MiniUserInfo readUserInfo(ByteBlock block) {
        DefensiveTools.checkNull(block, "block");

        if (block.getLength() == 0) return null;

        int length = BinaryTools.getUByte(block, 0);

        if (block.getLength() < length + 1) return null;

        String sn = BinaryTools.getAsciiString(block.subBlock(1, length));

        int size = 1 + length;
        int warningLevel = -1;
        if (block.getLength() >= 1 + length + 2) {
            warningLevel = BinaryTools.getUShort(block, 1 + length  + 2);
            size += 2;
        }

        return new MiniUserInfo(sn, warningLevel, size);
    }

    /**
     * Creates a new miniature user info block with the given properties.
     *
     * @param sn the user's screenname
     * @param warningLevel the user's warning level, or <code>-1</code> if the
     *        warning level was not or is not to be sent
     * @param totalSize the total size of this structure, as read from a block
     *        of binary data
     */
    protected MiniUserInfo(String sn, int warningLevel, int totalSize) {
        DefensiveTools.checkNull(sn, "sn");
        DefensiveTools.checkRange(warningLevel, "warningLevel", 0);
        DefensiveTools.checkRange(totalSize, "totalSize", -1);

        this.sn = sn;
        this.warningLevel = warningLevel;
        this.totalSize = totalSize;
    }

    /**
     * Creates a new mini user info block with the given properties.
     *
     * @param sn the user's screenname
     * @param warningLevel the user's warning level
     */
    public MiniUserInfo(String sn, int warningLevel) {
        this(sn, warningLevel, -1);
    }

    /**
     * Returns this user info block's screenname.
     *
     * @return the user's screenname
     */
    public final String getScreenname() {
        return sn;
    }

    /**
     * Returns this user info block's warning level.
     *
     * @return the user's warning level
     */
    public final int getWarningLevel() {
        return warningLevel;
    }

    /**
     * Returns the total size, in bytes, of this object, or <code>-1</code> if
     * this object was not read using <code>readUserInfo</code>.
     *
     * @return the total size of this object, in bytes
     */
    public final int getTotalSize() {
        return totalSize;
    }

    public void write(OutputStream out) throws IOException {
        OscarTools.writeScreenname(out, sn);

        BinaryTools.writeUShort(out, warningLevel);
    }

    public String toString() {
        return "MiniUserInfo: " + sn + " (" + warningLevel + "%)";
    }
}
