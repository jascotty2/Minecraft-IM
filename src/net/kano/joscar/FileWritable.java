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
 *  File created by keith @ Apr 1, 2003
 *
 */

package net.kano.joscar;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Provides an efficient means of using the contents of a file as a
 * <code>LiveWritable</code>.
 * 
 * @see LiveWritable
 */
public class FileWritable implements LiveWritable {
    /**
     * The file from which to read.
     */
    private final String filename;

    /**
     * Creates a new Writable that will write the contents of the given file
     * on command.
     *
     * @param filename the file whose contents should be written
     */
    public FileWritable(String filename) {
        this.filename = filename;
    }

    /**
     * Returns the file to be written by this object.
     *
     * @return the file that this object represents
     */
    public final String getFilename() { return filename; }

    /**
     * Writes the contents of this object's file (<code>getFile()</code>) to
     * the given stream.
     *
     * @param out the stream to which to write the file's contents
     * @throws IOException if an I/O error occurs
     */
    public final void write(OutputStream out) throws IOException {
        File file = new File(filename);
        long len = file.length();

        FileInputStream in = new FileInputStream(file);

        try {
            FileChannel inch = in.getChannel();
            WritableByteChannel outch = Channels.newChannel(out);

            inch.transferTo(0, len, outch);
        } finally {
            in.close();
        }
    }
}
