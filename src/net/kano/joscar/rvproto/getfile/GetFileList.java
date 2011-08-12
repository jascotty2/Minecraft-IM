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
 *  File created by Keith @ 9:14:11 PM
 *
 */

package net.kano.joscar.rvproto.getfile;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.LiveWritable;
import net.kano.joscar.tlv.ImmutableTlvChain;
import net.kano.joscar.tlv.Tlv;
import net.kano.joscar.tlv.TlvChain;
import net.kano.joscar.tlv.TlvTools;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * A data structure representing a "Get File" directory listing. Note that a
 * Get File list may contain only the files and directories in one directory or
 * all files a directory and in all subdirectories, recursively. See {@link
 * net.kano.joscar.rvcmd.getfile.GetFileReqRvCmd#FLAG_EXPAND_DYNAMIC} for
 * details.
 */
public class GetFileList implements LiveWritable {
    /** The Get File directory listing version string used by WinAIM. */
    public static final String GFLISTVERSION_DEFAULT = "Lst1";

    /** The Get File directory listing version string for this list. */
    private final String gfListVersion;
    /** The files on this list. */
    private final GetFileEntry[] files;

    /**
     * Reads a Get File directory list from the given block of binary data. Note
     * that this method will return <code>null</code> if no valid list can be
     * read. The given block should be the entire block of data sent following
     * a {@link
     *net.kano.joscar.rvproto.ft.FileTransferHeader#HEADERTYPE_FILELIST_SENDLIST
     * }
     * header.
     *
     * @param block a block of data containing a Get File directory listing
     * @return a Get File directory listing object read from the given block
     */
    public static GetFileList readGetFileList(ByteBlock block) {
        DefensiveTools.checkNull(block, "block");

        if (block.getLength() < 4) return null;

        String version = BinaryTools.getAsciiString(block.subBlock(0, 4));

        ByteBlock rest = block.subBlock(4);
        TlvChain chain = TlvTools.readChain(rest);

        Tlv[] tlvs = chain.getTlvs();

        List entries = new LinkedList();

        for (int i = 0; i < tlvs.length;) {
            GetFileEntry entry
                    = GetFileEntry.readEntry(tlvs, i);

            if (entry == null) break;

            entries.add(entry);

            i += entry.getTotalTlvCount();
        }

        GetFileEntry[] entryArray
                = (GetFileEntry[]) entries.toArray(new GetFileEntry[0]);
        return new GetFileList(version, entryArray);
    }

    /**
     * Creates a new Get File directory list with the given list of files.
     * <br>
     * <br>
     * Using this constructor is equivalent to using {@link #GetFileList(String,
     * GetFileEntry[]) new GetFileList(GFLISTVERSION_DEFAULT, files}.
     *
     * @param files a list of files to use in this list
     */
    public GetFileList(GetFileEntry[] files) {
        this(GFLISTVERSION_DEFAULT, files);
    }

    /**
     * Creates a new Get File directory listing with the given list version
     * string and the given list of files. Note that the given file list may
     * not contain any <code>null</code> elements.
     *
     * @param gfListVersion a Get File list version string; this should normally
     *        be {@link #GFLISTVERSION_DEFAULT}
     * @param files a list of files to hold in this list
     */
    public GetFileList(String gfListVersion, GetFileEntry[] files) {
        DefensiveTools.checkNull(gfListVersion, "gfListVersion");
        DefensiveTools.checkNull(files, "files");

        this.gfListVersion = gfListVersion;
        this.files = (GetFileEntry[]) files.clone();

        DefensiveTools.checkNullElements(this.files, "files");
    }

    /**
     * Returns the Get File listing version string used in this list. This will
     * normally be {@link #GFLISTVERSION_DEFAULT}.
     *
     * @return the Get Fle listing version string sent in this list
     */
    public final String getGfListVersion() { return gfListVersion; }

    /**
     * Returns an array containing the Get File entries in this list. Note that
     * changes to the returned array will not be reflected in this file list
     * object.
     *
     * @return an array containing the Get File entries in this list
     */
    public final GetFileEntry[] getFileEntries() {
        return (GetFileEntry[]) files.clone();
    }

    public final void write(OutputStream out) throws IOException {
        out.write(BinaryTools.getAsciiBytes(gfListVersion));
        for (int i = 0; i < files.length; i++) {
            files[i].write(out);
        }
    }

    public String toString() {
        return "GetFileList: version=" + gfListVersion + ", files="
                + files.length;
    }
}