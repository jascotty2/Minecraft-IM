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
 *  File created by keith @ Feb 23, 2003
 *
 */

package net.kano.joscar.snaccmd.loc;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.flapcmd.SnacPacket;
import net.kano.joscar.snaccmd.DirInfo;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A SNAC command containing a user's directory information. Normally sent in
 * response to a {@link GetDirInfoCmd}.
 *
 * @snac.src server
 * @snac.cmd 0x02 0x0c
 *
 * @see GetDirInfoCmd
 */
public class DirInfoCmd extends LocCommand {
    /**
     * A result code indicating that the requested directory information exists.
      */
    public static final int CODE_SUCCESS = 0x01;
    /**
     * A result code indicating that the requested directory information was not
     * found.
     */
    public static final int CODE_ERROR = 0x02;

    /** A result code. */
    private final int code;
    /** The directory information block returned, if any. */
    private final DirInfo dirInfo;

    /**
     * Generates a new directory information response from the given incoming
     * SNAC packet.
     *
     * @param packet an incoming directory information response packet
     */
    protected DirInfoCmd(SnacPacket packet) {
        super(CMD_DIR_INFO);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

        code = BinaryTools.getUShort(snacData, 0);
        
        int tlvCount = BinaryTools.getUShort(snacData, 2);

        ByteBlock infoBlock = snacData.subBlock(4);

        dirInfo = DirInfo.readDirInfo(infoBlock, tlvCount);
    }

    /**
     * Creates a new outgoing directory information command with the given
     * directory information block, if any. The <code>code</code> field is
     * set to {@link #CODE_SUCCESS} if <code>dirInfo</code> is
     * non-<code>null</code> and to {@link #CODE_ERROR} if it is
     * <code>null</code>.
     *
     * @param dirInfo a directory information block, or <code>null</code> for
     *        none
     */
    public DirInfoCmd(DirInfo dirInfo) {
        this(dirInfo != null ? CODE_SUCCESS : CODE_ERROR, dirInfo);
    }

    /**
     * Creates a new outgoing directory information command with the given
     * result code and directory information block, if any.
     *
     * @param code a result code, like {@link #CODE_SUCCESS}
     * @param dirInfo a directory information block, or <code>null</code> for
     *        none
     */
    public DirInfoCmd(int code, DirInfo dirInfo) {
        super(CMD_DIR_INFO);

        DefensiveTools.checkRange(code, "code", 0);

        this.code = code;
        this.dirInfo = dirInfo;
    }

    /**
     * Returns the result code sent of this directory information response.
     * Normally one of {@link #CODE_SUCCESS} and {@link #CODE_ERROR}.
     *
     * @return the directory information response code
     */
    public final int getResultCode() {
        return code;
    }

    /**
     * Returns the directory information block included in this response,
     * or <code>null</code> if none was sent. This value is normally
     * <code>null</code> if the {@linkplain #getResultCode result code} is
     * {@link #CODE_ERROR}.
     *
     * @return the directory information contained in this response
     */
    public final DirInfo getDirInfo() {
        return dirInfo;
    }

    public void writeData(OutputStream out) throws IOException {
        BinaryTools.writeUShort(out, code);

        if (dirInfo != null) dirInfo.write(out);
    }

    public String toString() {
        return "DirInfoCmd: code=" + code + ", dirinfo=<" + dirInfo + ">";
    }
}
