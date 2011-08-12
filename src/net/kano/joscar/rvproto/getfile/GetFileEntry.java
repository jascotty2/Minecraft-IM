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
 *  File created by Keith @ 5:23:34 AM
 *
 */

package net.kano.joscar.rvproto.getfile;

import net.kano.joscar.DefensiveTools;
import net.kano.joscar.LiveWritable;
import net.kano.joscar.rvcmd.SegmentedFilename;
import net.kano.joscar.tlv.ImmutableTlvChain;
import net.kano.joscar.tlv.Tlv;
import net.kano.joscar.tlv.TlvChain;
import net.kano.joscar.tlv.TlvTools;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

/**
 * A data structure representing a single file or directory in a "Get File"
 * directory listing.
 */
public class GetFileEntry implements LiveWritable {
    /** A flag indicating that a file entry is a directory. */
    public static final long FLAG_DIR = 0x0001;

    /** A TLV type containing the file's last modification date. */
    private static final int TYPE_LASTMOD = 0x0101;
    /** A TLV type containing the file's size, in bytes. */
    private static final int TYPE_FILESIZE = 0x0303;
    /** A TLV type containing the file name. */
    private static final int TYPE_FILENAME = 0x0404;
    /** A TLV type containing a set of flags. */
    private static final int TYPE_FLAGS = 0x0900;
    /** The TLV type of the last TLV in an entry. */
    private static final int TYPE_SENTINEL = 0x0909;

    /**
     * Reads a Get File directory list entry from the given list of TLV's. Note
     * that the total number of TLV's read can be obtained by calling the
     * returned <code>GetFileEntry</code>'s {@link #getTotalTlvCount()} method.
     *
     * @param tlvs a list of TLV's containing a Get File directory list entry
     * @param offset the index of the first TLV in the given array from which
     *        the file entry TLV's should be read
     * @return a Get File directory list entry read from the given list of TLV's
     */
    public static final GetFileEntry readEntry(Tlv[] tlvs, int offset) {
        DefensiveTools.checkNull(tlvs, "tlvs");
        DefensiveTools.checkRange(offset, "offset", 0, tlvs.length - 1);

        boolean gotLastmod = false;
        int lastTlv = -1;
        for (int i = offset; i < tlvs.length; i++) {
            int type = tlvs[i].getType();
            if (type == TYPE_LASTMOD) {
                if (!gotLastmod) {
                    gotLastmod = true;
                } else {
                    lastTlv = i - 1;
                    break;
                }
            } else if (type == TYPE_SENTINEL) {
                lastTlv = i;
                break;
            }
        }

        int totalTlvCount;
        if (lastTlv == -1) totalTlvCount = tlvs.length - offset;
        else totalTlvCount = lastTlv - offset + 1;

        if (totalTlvCount == 0) return null;

        TlvChain chain = TlvTools.createChain(tlvs, offset,
                totalTlvCount);

        SegmentedFilename filename = null;
        String ftFilenameStr = chain.getString(TYPE_FILENAME);
        if (ftFilenameStr != null) {
            filename = SegmentedFilename.fromFTFilename(ftFilenameStr);
        }

        Tlv lastmodTlv = chain.getFirstTlv(TYPE_LASTMOD);
        long lastmod = -1;
        if (lastmodTlv != null) {
            lastmod = lastmodTlv.getDataAsUInt();
        }

        Tlv filesizeTlv = chain.getFirstTlv(TYPE_FILESIZE);
        long filesize = -1;
        if (filesizeTlv != null) {
            filesize = filesizeTlv.getDataAsUInt();
        }

        Tlv flagsTlv = chain.getFirstTlv(TYPE_FLAGS);
        long flags = 0;
        if (flagsTlv != null) {
            flags = flagsTlv.getDataAsUInt();
            if (flags == -1) flags = 0;
        }

        return new GetFileEntry(filename, filesize, lastmod, flags,
                totalTlvCount);
    }

    /** The name of the represented file. */
    private final SegmentedFilename filename;
    /** The size of the represented file. */
    private final long filesize;
    /** The last-modification date of the represented file. */
    private final long lastmod;
    /** A set of flags describing the represented file. */
    private final long flags;
    /**
     * The total number of TLV's read in constructing this entry, if this entry
     * was read from an incoming list of TLV's.
     */
    private final int totalTlvCount;

    /**
     * Creates a new Get File directory list entry with the given properties.
     *
     * @param filename the name of the file
     * @param filesize the size of the file
     * @param lastmod the last modification date of the file, in seconds since
     *        the unix epoch
     * @param flags a set of bit flags describing this file, like {@link
     *        #FLAG_DIR} or <code>0</code>
     * @param totalTlvCount the total number of TLV's read in constructing this
     *        object
     */
    private GetFileEntry(SegmentedFilename filename, long filesize,
            long lastmod, long flags, int totalTlvCount) {
        this.filename = filename;
        this.filesize = filesize;
        this.lastmod = lastmod;
        this.flags = flags;
        this.totalTlvCount = totalTlvCount;
    }

    /**
     * Creates a new Get File directory listing entry with the properties of the
     * given file.
     * <br>
     * <br>
     * Using this constructor is equivalent to using {@link
     * #GetFileEntry(SegmentedFilename, File) new
     * GetFileEntry(SegmentedFilename.fromNativeFilename(file.getName()),
     * file)}.
     *
     * @param file the file whose properties should be used in this entry
     */
    public GetFileEntry(File file) {
        this(SegmentedFilename.fromNativeFilename(file.getName()), file);
    }

    /**
     * Creates a new Get File directory listing entry with the properties of the
     * given file and with the given name.
     * <br>
     * <br>
     * Using this constructor is equivalent to using {@link
     * #GetFileEntry(SegmentedFilename, long, long, long) new
     * GetFileEntry(filename, file.lastModified() / 1000,
     * file.isDirectory() ? FLAG_DIR : 0)}.
     *
     * @param filename a filename to use in this entry
     * @param file a file whose properties will be used for this entry
     */
    public GetFileEntry(SegmentedFilename filename, File file) {
        this(filename, file.length(), file.lastModified() / 1000,
                file.isDirectory() ? FLAG_DIR : 0);
    }

    /**
     * Creates a new Get File directory list entry with the given properties.
     * Note that any of this method's arguments may be <code>null</code> or
     * <code>-1</code> (depending on argument type) to indicate that that
     * field should not be present in the created entry.
     *
     * @param filename the name of the file
     * @param filesize the size of the file
     * @param lastmod the last modification date of the file, in seconds since
     *        the unix epoch
     * @param flags a set of bit flags describing this file, like {@link
     *        #FLAG_DIR} or <code>0</code>
     */
    public GetFileEntry(SegmentedFilename filename, long filesize, long lastmod,
            long flags) {
        DefensiveTools.checkRange(lastmod, "lastmod", -1);
        DefensiveTools.checkRange(filesize, "filesize", -1);
        DefensiveTools.checkRange(filesize, "flags", -1);

        if (flags == -1) flags = 0;

        this.lastmod = lastmod;
        this.filesize = filesize;
        this.filename = filename;
        this.flags = flags;
        totalTlvCount = -1;
    }

    /**
     * Returns the filename in this directory listing entry, or
     * <code>null</code> if none is present.
     *
     * @return this entry's filename
     */
    public final SegmentedFilename getFilename() { return filename; }

    /**
     * Returns the file size in this directory listing entry, or <code>-1</code>
     * if none is present.
     *
     * @return this entry's file size
     */
    public final long getFileSize() { return filesize; }

    /**
     * Returns the last modification date in this directory listing entry, in
     * seconds since the unix epoch. Note that this method will return
     * <code>-1</code> if no last modification date was sent.
     *
     * @return this entry's last modification date
     */
    public final long getLastmod() { return lastmod; }

    /**
     * Returns a the set of flags in this directory listing entry. This will
     * normally be either {@link #FLAG_DIR} or <code>0</code>. Note that this
     * value will <i>never</i> be <code>-1</code>; if no flags are sent, this
     * value will simply be <code>0</code>.
     *
     * @return this entry's set of bit flags
     */
    public final long getFlags() { return flags; }

    /**
     * Returns the total number of TLV's read in constructing this entry object.
     * Note that this value will be <code>-1</code> if this object was not read
     * from an incoming block of TLV's (with {@link #readEntry}).
     *
     * @return the total number of TLV's read in constructing this entry object
     */
    public final int getTotalTlvCount() { return totalTlvCount; }

    public void write(OutputStream out) throws IOException {
        if (lastmod != -1) {
            Tlv.getUIntInstance(TYPE_LASTMOD, lastmod).write(out);
        }
        if (filesize != -1) {
            Tlv.getUIntInstance(TYPE_FILESIZE, filesize).write(out);
        }
        Tlv.getUShortInstance(0x0505, 0x0000).write(out);
        if (filename != null) {
            String ftFilename = filename.toFTFilename();
            Tlv.getStringInstance(TYPE_FILENAME, ftFilename).write(out);
        }
        if (flags != 0) {
            Tlv.getUIntInstance(TYPE_FLAGS, flags).write(out);
        }
        new Tlv(TYPE_SENTINEL).write(out);
    }

    public String toString() {
        return "GetFileEntry: file=<" + filename + ">, "
                + ((float) filesize / 1024)
                + " KB, last modified " + new Date(lastmod * 1000)
                + " (flags=" + flags + ")";
    }
}