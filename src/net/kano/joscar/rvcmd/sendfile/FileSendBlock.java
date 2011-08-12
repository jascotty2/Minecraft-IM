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
 *  File created by keith @ Apr 24, 2003
 *
 */

package net.kano.joscar.rvcmd.sendfile;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.LiveWritable;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A data structure containing information about a file or directory of files
 * being sent in a {@linkplain FileSendReqRvCmd file transfer request}.
 */
public class FileSendBlock implements LiveWritable {
    /** A code indicating that a single file is being sent. */
    public static final int SENDTYPE_SINGLEFILE = 0x01;
    /**
     * A code indicating that all of the files in a directory are being sent.
     */
    public static final int SENDTYPE_DIR = 0x02;

    /**
     * A code indicating whether a file or a directory full of files is being
     * sent.
     */
    private final int sendType;
    /** The number of files being sent. */
    private final int fileCount;
    /** The total size of all of the files. */
    private final long totalFileSize;
    /** The filename of the file or directory being sent. */
    private final String filename;

    /**
     * Creates a new file send block object from the given incoming block of
     * binary data.
     *
     * @param block a block of binary data containing a file send block
     * @return a file send block object read from the given block of data
     */
    public static FileSendBlock readFileSendBlock(ByteBlock block) {
        DefensiveTools.checkNull(block, "block");

        int type = BinaryTools.getUShort(block, 0);
        int count = BinaryTools.getUShort(block, 2);
        long size = BinaryTools.getUInt(block, 4);

        ByteBlock filenameBlock = block.subBlock(8);
        int firstNull;
        for (firstNull = 0; firstNull < filenameBlock.getLength();
             firstNull++) {
            if (filenameBlock.get(firstNull) == 0) break;
        }

        String name = null;
        name = BinaryTools.getAsciiString(filenameBlock.subBlock(0,
                firstNull));

        return new FileSendBlock(type, name, count, size);
    }

    /**
     * Creates a file send block describing a single file with the given size.
     * That is, creates a file send block with the given properties, a type of
     * {@link #SENDTYPE_SINGLEFILE} and a file count of <code>1</code>.
     * <br>
     * <br>
     * Using this constructor is eqiuvalent to using {@link #FileSendBlock(int,
     * String, int, long) new FileSendBlock(SENDTYPE_SINGLEFILE, filename, 1,
     * size)}.
     *
     * @param filename the name of the file being sent
     * @param size the size of the file being sent
     */
    public FileSendBlock(String filename, long size) {
        this(SENDTYPE_SINGLEFILE, filename, 1, size);
    }

    /**
     * Creates a new outgoing file send block with the given properties.
     * <br>
     * <br>
     * Note that when sending all files in the directory <code>dirName</code>,
     * WinAIM sends <code>"dirName\*"</code> as the value of
     * <code>filename</code>.
     *
     * @param sendType a "send type" code, like {@link #SENDTYPE_SINGLEFILE}
     * @param filename the name of the file or directory being sent
     * @param fileCount the number of files being sent (not including the base
     *        directory itself, if sending an entire directory)
     * @param totalFileSize the total cumulative file size of all of the files
     *        being sent
     */
    public FileSendBlock(int sendType, String filename, int fileCount,
            long totalFileSize) {
        DefensiveTools.checkRange(sendType, "sendType", 0);
        DefensiveTools.checkRange(fileCount, "fileCount", 0);
        DefensiveTools.checkRange(totalFileSize, "totalFileSize", 0);
        DefensiveTools.checkNull(filename, "filename");

        this.sendType = sendType;
        this.fileCount = fileCount;
        this.totalFileSize = totalFileSize;
        this.filename = filename;
    }

    /**
     * Returns the "send type" code for this transfer. This will normally be
     * either {@link #SENDTYPE_SINGLEFILE} or {@link #SENDTYPE_DIR}.
     *
     * @return this block's "send type" code
     */
    public final int getSendType() { return sendType; }

    /**
     * Returns the total number of files being sent.
     *
     * @return the total number of files being sent
     */
    public final int getFileCount() { return fileCount; }

    /**
     * Returns the total cumulative file size, in bytes, of all files being
     * sent.
     *
     * @return the total file size of all files being sent
     */
    public final long getTotalFileSize() { return totalFileSize; }

    /**
     * Returns the name of the file being sent. Note that when sending an
     * entire directory of files, WinAIM sends something resembling
     * <code>"directoryName\*"</code> as the filename, indicating that the
     * transferred files should be placed in a new directory called
     * <code>directoryName</code>.
     *
     * @return the name of the file being sent
     */
    public final String getFilename() { return filename; }

    public void write(OutputStream out) throws IOException {
        BinaryTools.writeUShort(out, sendType);
        BinaryTools.writeUShort(out, fileCount);
        BinaryTools.writeUInt(out, totalFileSize);
        out.write(BinaryTools.getAsciiBytes(filename));

        // we write 46 nulls here. fun fun.
        out.write(new byte[46]);
    }

    public String toString() {
        return "FileSendBlock: type=" + sendType + (
                fileCount > 1
                ? ", " + fileCount + " files under " + filename
                : ": " + filename
                ) + ": "
                + totalFileSize + " bytes total";
    }
}
