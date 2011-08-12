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

package net.kano.joscar.rvproto.ft;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.ImEncodedString;
import net.kano.joscar.ImEncodingParams;
import net.kano.joscar.LiveWritable;
import net.kano.joscar.rvcmd.SegmentedFilename;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A data structure used to transfer information over a file transfer
 * connection.
 */
public final class FileTransferHeader implements LiveWritable {
    /** The file transfer protocol version used by WinAIM. */
    public static final String FTVERSION_DEFAULT = "OFT2";

    /** A header type indicating that a header is a "sending file" header. */
    public static final int HEADERTYPE_SENDHEADER = 0x0101;
    /**
     * A header typing indicating that a header is an "acknowledgement" header.
     */
    public static final int HEADERTYPE_ACK = 0x0202;
    /**
     * A header type indicating that a header is a "transfer finished" header.
     */
    public static final int HEADERTYPE_RECEIVED = 0x0204;

    /**
     * A header type indicating that the header is requesting that a file be
     * "resumed" from a point in the file up to which the receiver has already
     * received data.
     */
    public static final int HEADERTYPE_RESUME = 0x0205;
    /**
     * A header type indicating that the sender is about to send ("resume") the
     * file being resumed.
     */
    public static final int HEADERTYPE_RESUME_SENDHEADER = 0x0106;
    /**
     * A header type used to indicate that the sender can begin sending the
     * resumed file.
     */
    public static final int HEADERTYPE_RESUME_ACK = 0x0207;

    /**
     * A header type used in Get File indicating that a file list is about to be
     * sent.
     */
    public static final int HEADERTYPE_FILELIST_SENDLIST = 0x1108;
    /**
     * A header typed used in Get File to acknowledge that a file list is about
     * to be sent.
     */
    public static final int HEADERTYPE_FILELIST_ACK = 0x1209;
    /**
     * A header type used in Get File to indicate that a file list was received
     * successfully.
     */
    public static final int HEADERTYPE_FILELIST_RECEIVED = 0x120b;
    /** A header type used in Get File to request a file. */
    public static final int HEADERTYPE_FILELIST_REQFILE = 0x120c;
    /**
     * A header type used in Get File to request a directory listing for a
     * specific directory.
      */
    public static final int HEADERTYPE_FILELIST_REQDIR = 0x120e;

    /** An encryption code indicating that no encryption is being used. */
    public static final int ENCRYPTION_NONE = 0x0000;
    /** An encryption code indicating that no compression is being used. */
    public static final int COMPRESSION_NONE = 0x0000;

    /** A client ID string used by WinAIM 5.1 and earlier. */
    public static final String CLIENTID_OLD = "OFT_Windows ICBMFT V1.1 32";
    /** A client ID string used by WinAIM 5.2 beta. */
    public static final String CLIENTID_DEFAULT = "Cool FileXfer";

    /**
     * The value always sent by WinAIM for the <code>listNameOffset</code>
     * field.
     *
     * @see #setListNameOffset(int)
     */
    public static final int LISTNAMEOFFSET_DEFAULT = 28;
    /**
     * The value always sent by WinAIM for the <code>listSizeOffset</code>
     * field.
     *
     * @see #setListSizeOffset(int)
     */
    public static final int LISTSIZEOFFSET_DEFAULT = 17;

    /** A flag that is always sent by WinAIM. */
    public static final int FLAG_DEFAULT = 0x20;
    /** A flag sent in a {@link #HEADERTYPE_RECEIVED} header. */
    public static final int FLAG_DONE = 0x01;
    /** A flag sent in a {@link #HEADERTYPE_FILELIST_SENDLIST} header. */
    public static final int FLAG_FILELIST = 0x10;

    /** The "dummy" block sent by WinAIM. */
    public static final ByteBlock DUMMY_DEFAULT = ByteBlock.wrap(new byte[69]);
    /** The Mac file information block sent by WinAIM. */
    public static final ByteBlock MACFILEINFO_DEFAULT
            = ByteBlock.wrap(new byte[16]);

    /**
     * Reads a file transfer header from the given stream. Note that this
     * method will return <code>null</code> if no valid header can be read or
     * if the end of the stream is reached before a valid header can be read.
     * Also note that this method will block until a complete header is read
     * or until one of the situations mentioned above occurs.
     *
     * @param in the stream from which to read the header
     * @return a file transfer header read from the given stream, or
     *         <code>null</code> if none could be read
     *
     * @throws IOException if an I/O error occurs
     */
    public static FileTransferHeader readHeader(InputStream in)
            throws IOException {
        DefensiveTools.checkNull(in, "in");

        // first we read the mini-header which contains a file transfer version
        // and the length of the whole header
        byte[] header = new byte[6];
        for (int i = 0; i < header.length;) {
            int count = in.read(header, i, header.length - i);

            if (count == -1) return null;

            i += count;
        }

        ByteBlock ftVerBlock = ByteBlock.wrap(header, 0, 4);
        String ftversion = BinaryTools.getAsciiString(ftVerBlock);

        int headerLen = BinaryTools.getUShort(ByteBlock.wrap(header), 4);

        if (headerLen < 6) return null;

        // then we read the full header by reading the rest of the bytes
        // whose length was given in the mini-header
        byte[] bigheader = new byte[headerLen - 6];
        for (int i = 0; i < bigheader.length;) {
            int count = in.read(bigheader, i, bigheader.length - i);

            if (count == -1) return null;

            i += count;
        }

        // I think now is a good time to create the header object.
        FileTransferHeader fsh = new FileTransferHeader();
        fsh.setFtVersion(ftversion);
        fsh.setHeaderSize(headerLen);

        ByteBlock block = ByteBlock.wrap(bigheader);
        fsh.setHeaderType(BinaryTools.getUShort(block, 0));
        fsh.setIcbmMessageId(BinaryTools.getLong(block, 2));
        fsh.setEncryption(BinaryTools.getUShort(block, 10));
        fsh.setCompression(BinaryTools.getUShort(block, 12));

        fsh.setFileCount(BinaryTools.getUShort(block, 14));
        fsh.setFilesLeft(BinaryTools.getUShort(block, 16));
        fsh.setPartCount(BinaryTools.getUShort(block, 18));
        fsh.setPartsLeft(BinaryTools.getUShort(block, 20));

        fsh.setTotalFileSize(BinaryTools.getUInt(block, 22));
        fsh.setFileSize(BinaryTools.getUInt(block, 26));
        fsh.setLastmod(BinaryTools.getUInt(block, 30));
        fsh.setChecksum(BinaryTools.getUInt(block, 34));

        fsh.setResForkReceivedChecksum(BinaryTools.getUInt(block, 38));
        fsh.setResForkSize(BinaryTools.getUInt(block, 42));
        fsh.setCreated(BinaryTools.getUInt(block, 46));
        fsh.setResForkChecksum(BinaryTools.getUInt(block, 50));

        fsh.setBytesReceived(BinaryTools.getUInt(block, 54));
        fsh.setReceivedChecksum(BinaryTools.getUInt(block, 58));

        // the client ID block is stored as 32 bytes of ASCII text padded to the
        // right with nulls.
        ByteBlock clientidBlock = block.subBlock(62, 32);
        fsh.setClientid(BinaryTools.getNullPadded(clientidBlock).getString());

        ByteBlock rest = block.subBlock(94);
        fsh.setFlags(BinaryTools.getUByte(rest, 0));
        fsh.setListNameOffset(BinaryTools.getUByte(rest, 1));
        fsh.setListSizeOffset(BinaryTools.getUByte(rest, 2));

        fsh.setDummyBlock(rest.subBlock(3, 69));
        fsh.setMacFileInfo(rest.subBlock(72, 16));

        int charset = BinaryTools.getUShort(rest, 88);
        int charsubset = BinaryTools.getUShort(rest, 90);

        ByteBlock filenameBlock = rest.subBlock(92);

        ImEncodingParams encoding = new ImEncodingParams(charset, charsubset);
        String ftFilename = ImEncodedString.readImEncodedString(
                encoding, filenameBlock);
        int firstNull = ftFilename.indexOf('\0');
        int lastNull = ftFilename.lastIndexOf('\0');
        if (firstNull != lastNull) {
            // there might be embedded null unicode characters, so we scan back
            // from the last one for the first non-null character
            for (int i = lastNull - 1; i >= 0; i--) {
                if (ftFilename.charAt(i) != '\0') {
                    firstNull = i + 1;
                    break;
                }
            }
        }
        if (firstNull != -1) ftFilename = ftFilename.substring(0, firstNull);
        SegmentedFilename segmented = SegmentedFilename.fromFTFilename(
                ftFilename);
        for (int i = 0; i < ftFilename.length(); i++) {
            char ch = ftFilename.charAt(i);
//            System.out.println("char " + ch + " = " + (int) ch);
        }

        fsh.setFilename(segmented);

        return fsh;
    }

    /** A file transfer protocol version string. */
    private String ftVersion = null;
    /** A header type code. */
    private int headerType = -1;
    /** An ICBM message ID associated with this connection. */
    private long icbmMessageId = 0;
    /** An encryption method code. */
    private int encryption = -1;
    /** A compression method code. */
    private int compression = -1;
    /**
     * The number of files in the associated file list, or the number of files
     * being transferred.
     */
    private int fileCount = -1;
    /** The number of files that remain to be sent. */
    private int filesLeft = -1;
    /** The number of "parts" in the associated file list. */
    private int partCount = -1;
    /** The number of "parts" that remain to be sent. */
    private int partsLeft = -1;
    /** The total number of bytes in all of the files being transferred. */
    private long totalFileSize = -1;
    /** The size of the file being transferred. */
    private long fileSize = -1;
    /** The last modification date of the file being transferred. */
    private long lastmod = -1;
    /** A checksum for the file being transferred. */
    private long checksum = -1;
    /** A checksum for the received resource fork parts. */
    private long resForkReceivedChecksum = -1;
    /** The total number of bytes in the resource fork. */
    private long resForkSize = -1;
    /** The creation time of the file being sent. */
    private long created = -1;
    /** A checksum for the file's "resource fork" (only on MacOS). */
    private long resForkChecksum = -1;
    /** The number of bytes received in the transfer of the last file. */
    private long bytesReceived = -1;
    /** A checksum for the bytes received. */
    private long receivedChecksum = -1;
    /** A client ID string. */
    private String clientid = null;
    /** A set of header flags. */
    private int flags = -1;
    /** Some sort of offset. */
    private int listNameOffset = -1;
    /** Some sort of offset. */
    private int listSizeOffset = -1;
    /** A block that is always full of null bytes. */
    private ByteBlock dummyBlock = null;
    /** A block of MacOS file information. */
    private ByteBlock macFileInfo = null;
    /** A filename. */
    private SegmentedFilename filename = null;
    /** The size of this header, as read from an incoming block of data. */
    private int headerSize = -1;

    /**
     * Creates a new file transfer header with all values initialized to either
     * <code>null</code> or <code>-1</code>, depending on type.
     *
     * @see #setDefaults()
     */
    public FileTransferHeader() { }

    /**
     * Creates a new file transfer header with the same properties as the given
     * header.
     *
     * @param other a header to copy
     */
    public FileTransferHeader(FileTransferHeader other) {
        DefensiveTools.checkNull(other, "other");

        ftVersion = other.ftVersion;
        headerType = other.headerType;
        icbmMessageId = other.icbmMessageId;
        encryption = other.encryption;
        compression = other.compression;
        fileCount = other.fileCount;
        filesLeft = other.filesLeft;
        partCount = other.partCount;
        partsLeft = other.partsLeft;
        totalFileSize = other.totalFileSize;
        fileSize = other.fileSize;
        lastmod = other.lastmod;
        checksum = other.checksum;
        resForkReceivedChecksum = other.resForkReceivedChecksum;
        resForkSize = other.resForkSize;
        created = other.created;
        resForkChecksum = other.resForkChecksum;
        bytesReceived = other.bytesReceived;
        receivedChecksum = other.receivedChecksum;
        clientid = other.clientid;
        flags = other.flags;
        listNameOffset = other.listNameOffset;
        listSizeOffset = other.listSizeOffset;
        dummyBlock = other.dummyBlock;
        macFileInfo = other.macFileInfo;
        filename = other.filename;
    }

    /**
     * Returns the file transfer protocol version string contained in this
     * header. This will normally be {@link #FTVERSION_DEFAULT}.
     *
     * @return the file transfer protocol version string contained in this
     *         header
     */
    public synchronized String getFtVersion() { return ftVersion; }

    /**
     * Returns a code describing which type of header this is. This will
     * normally be one of {@linkplain #HEADERTYPE_SENDHEADER the
     * <code>HEADERTYPE_<i>*</i></code> constants} defined in this class.
     *
     * @return this header's header type code
     */
    public synchronized int getHeaderType() { return headerType; }

    /**
     * Returns an ICBM message ID associated with this file transfer header.
     * This value is used to confirm the identity of the person on the other
     * end of the connection; for a file transfer, this value should be the
     * ICBM message ID of the {@link
     * net.kano.joscar.rvcmd.sendfile.FileSendAcceptRvCmd} sent to accept the
     * transfer. Otherwise, this value will normally be <code>0</code>.
     *
     * @return the ICBM message ID sent in this command
     */
    public synchronized long getIcbmMessageId() { return icbmMessageId; }

    /**
     * Returns a code describing the encryption being used to transfer a file.
     * This will normally be {@link #ENCRYPTION_NONE} (to indicate that no
     * encryption is taking place).
     *
     * @return this header's encryption code
     */
    public synchronized int getEncryption() { return encryption; }

    /**
     * Returns a code describing the compression being used to transfer a file.
     * This will normally be {@link #COMPRESSION_NONE} (to indicate that the
     * transfer is not compressed).
     *
     * @return this header's compression code
     */
    public synchronized int getCompression() { return compression; }

    /**
     * Returns the total number of files being transferred, or in the case of
     * transferring a file list, the total number of files in the following
     * file list.
     *
     * @return the total number of files being transferred or listed
     */
    public synchronized int getFileCount() { return fileCount; }

    /**
     * Returns the number of files (of the {@linkplain #getFileCount total
     * number}) that remain to be sent.
     *
     * @return the number of files that remain to be sent
     */
    public synchronized int getFilesLeft() { return filesLeft; }

    /**
     * Returns the total number of file "parts" being sent. This will normally
     * be <code>1</code> for most files and <code>2</code> for files on MacOS
     * that have <i>resource forks</i>.
     *
     * @return the total number of file parts being sent
     */
    public synchronized int getPartCount() { return partCount; }

    /**
     * Returns the number of "parts" (of the {@linkplain #getPartCount total
     * number} that remain to be sent.
     *
     * @return the number of file parts that remain to be sent
     */
    public synchronized int getPartsLeft() { return partsLeft; }

    /**
     * Returns the total file size of all files being sent.
     *
     * @return the total size, in bytes, of all files being sent
     */
    public synchronized long getTotalFileSize() { return totalFileSize; }

    /**
     * Returns the size of the file currently being sent.
     *
     * @return the size of the file currently being sent
     */
    public synchronized long getFileSize() { return fileSize; }

    /**
     * Returns the last modification date of the file being sent, in seconds
     * since the unix epoch.
     *
     * @return the last modification date of the file being sent
     */
    public synchronized long getLastmod() { return lastmod; }

    /**
     * Returns a {@linkplain FileTransferChecksum checksum} of the file being
     * transferred.
     *
     * @return a checksum of the file being transferred
     *
     * @see FileTransferChecksum
     */
    public synchronized long getChecksum() { return checksum; }

    /**
     * Returns a {@linkplain FileTransferChecksum checksum} of the received
     * MacOS resource fork data. Note that in most cases this value will be
     * <code>0</code>.
     *
     * @return a checksum of the received MacOS resource fork data
     *
     * @see FileTransferChecksum
     */
    public synchronized long getResForkReceivedChecksum() {
        return resForkReceivedChecksum;
    }

    /**
     * Returns the size of the MacOS resource fork of the file being
     * transferred. Note that in most cases this will be <code>0</code>.
     *
     * @return the size of the MacOS resource fork of the file being transferred
     */
    public synchronized long getResForkSize() { return resForkSize; }

    /**
     * Returns the creation date of the file being transferred, in seconds since
     * the unix epoch. Note that WinAIM always sends <code>0</code> for this
     * value.
     *
     * @return the creation date of the file being transferred
     */
    public synchronized long getCreated() { return created; }

    /**
     * Returns a {@linkplain FileTransferChecksum checksum} of the MacOS
     * resource fork for the file being transferred.
     *
     * @return a checksum of the MacOS resource fork for the file being
     *         transferred
     */
    public synchronized long getResForkChecksum() { return resForkChecksum; }

    /**
     * Returns the number of bytes received for the file being transferred.
     *
     * @return the number of bytes received for the file being transferred
     */
    public synchronized long getBytesReceived() { return bytesReceived; }

    /**
     * Returns a {@linkplain FileTransferChecksum checksum} of the data
     * received for the current file.
     *
     * @return a checksum of the data received for the currently transferring
     *         file
     */
    public synchronized long getReceivedChecksum() { return receivedChecksum; }

    /**
     * Returns the file transfer client ID string sent in this header. This will
     * normally be a string describing the client, such as {@link
     * #CLIENTID_DEFAULT} or {@link #CLIENTID_OLD}.
     *
     * @return the file transfer client ID string
     */
    public synchronized String getClientid() { return clientid; }

    /**
     * Returns the set of bit flags sent in this header. This will normally
     * be a combination of {@linkplain #FLAG_DEFAULT the
     * <code>FLAG_<i>*</i></code> constants} defined in this class.
     *
     * @return the set of bit flags sent in this header
     */
    public synchronized int getFlags() { return flags; }

    /**
     * Returns the "list name offset" sent in this header. As of this writing,
     * the significance of this value is unknown; it will normally be either
     * <code>0</code> or {@link #LISTNAMEOFFSET_DEFAULT}.
     *
     * @return the "list name offset" sent in this header
     */
    public synchronized int getListNameOffset() { return listNameOffset; }

    /**
     * Returns the "list size offset" sent in this header. As of this writing,
     * the significance of this value is unknown; it will normally be either
     * <code>0</code> or {@link #LISTSIZEOFFSET_DEFAULT}.
     *
     * @return the "list size offset" sent in this header
     */
    public synchronized int getListSizeOffset() { return listSizeOffset; }

    /**
     * Returns the value of the (sigh) 69-byte "dummy block" sent in this
     * command. This block is normally full of null bytes (<code>0x00</code>).
     *
     * @return this header's "dummy block"
     */
    public synchronized ByteBlock getDummyBlock() { return dummyBlock; }

    /**
     * Returns the Mac file information block sent in this command. When
     * receiving files from Windows or other non-Macintosh OS users, this value
     * is normally {@link #MACFILEINFO_DEFAULT} (a block of sixteen null bytes).
     * When coming from a Macintosh user, however, this block is a copy of the
     * file's <a href=
     * "http://developer.apple.com/techpubs/macosx/Carbon/Files/FinderInterface/Finder_Interface/finder_interface/data_type_8.html"
     * title="Apple's description of the FInfo file information
     * block"><code>FInfo</code> block</a>. For more information on reading and
     * writing Macintosh file attributes in Java, see <a
     * href="http://www.tolstoy.com/samizdat/jconfig.html" title="The JConfig
     * homepage">JConfig</a>, an "extension of the core Java API's" for various
     * platforms.
     *
     * @return the Mac file information block sent in this command
     */
    public synchronized ByteBlock getMacFileInfo() { return macFileInfo; }

    /**
     * Returns an object representing the filename sent in this header.
     *
     * @return the filename sent in this header
     */
    public synchronized SegmentedFilename getFilename() { return filename; }

    /**
     * Returns the size, in bytes, of this header, or <code>-1</code> if this
     * header was not read from an incoming block of binary data.
     *
     * @return the size of this header, in bytes, or <code>-1</code> if this
     *         header was not rea
     */
    public synchronized int getHeaderSize() { return headerSize; }

    /**
     * Sets the file transfer protocol version string for this header. This
     * value should normally be {@link #FTVERSION_DEFAULT}.
     *
     * @param ftVersion a new file transfer protocol version string
     */
    public synchronized void setFtVersion(String ftVersion) {
        this.ftVersion = ftVersion;
    }

    /**
     * Sets this header's header type. This should normally be one of
     * {@linkplain #HEADERTYPE_SENDHEADER the <code>HEADERTYPE_<i>*</i></code>
     * constants} defined in this class.
     *
     * @param headerType a new header type code
     */
    public synchronized void setHeaderType(int headerType) {
        this.headerType = headerType;
    }

    /**
     * Sets this header's ICBM message ID. See {@link #getIcbmMessageId} for
     * details on what this value means.
     *
     * @param icbmMessageId a new ICBM message ID
     */
    public synchronized void setIcbmMessageId(long icbmMessageId) {
        this.icbmMessageId = icbmMessageId;
    }

    /**
     * Sets this header's encryption mode to the given value. This value should
     * normally be {@link #ENCRYPTION_NONE}.
     *
     * @param encryption a new encryption method code
     */
    public synchronized void setEncryption(int encryption) {
        this.encryption = encryption;
    }

    /**
     * Sets this header's encryption mode to the given value. This value should
     * normally be {@link #COMPRESSION_NONE}.
     *
     * @param compression a new compression method code
     */
    public synchronized void setCompression(int compression) {
        this.compression = compression;
    }

    /**
     * Sets the total file count for this header. See {@link #getFileCount()}
     * for details on what this value means.
     *
     * @param fileCount a new total file count value for this header
     */
    public synchronized void setFileCount(int fileCount) {
        this.fileCount = fileCount;
    }

    /**
     * Sets this header's "number of files remaining to be sent" value.
     *
     * @param filesLeft the number of files (of the total) remaining to be sent
     */
    public synchronized void setFilesLeft(int filesLeft) {
        this.filesLeft = filesLeft;
    }

    /**
     * Sets the total file part count for this header. See {@link
     * #getPartCount()} for details on what this value means.
     *
     * @param partCount a new total "file part" count for this header
     */
    public synchronized void setPartCount(int partCount) {
        this.partCount = partCount;
    }

    /**
     * Sets this header's "number of parts remaining to be sent" value.
     *
     * @param partsLeft the number of parts (of the total) remaining to be sent
     */
    public synchronized void setPartsLeft(int partsLeft) {
        this.partsLeft = partsLeft;
    }

    /**
     * Sets the total cumulative size of all files being transferred.
     *
     * @param totalFileSize a new total file size value for this header
     */
    public synchronized void setTotalFileSize(long totalFileSize) {
        this.totalFileSize = totalFileSize;
    }

    /**
     * Sets this header's value for the size of the file currently being
     * transferred.
     *
     * @param fileSize the size of the file currently being transferred
     */
    public synchronized void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * Sets the last modification date of the file being transferred, in
     * seconds since the unix epoch, or <code>0</code> for none.
     *
     * @param lastmod the last modification date of the file being transferred
     */
    public synchronized void setLastmod(long lastmod) {
        this.lastmod = lastmod;
    }

    /**
     * Sets the {@linkplain FileTransferChecksum checksum} of the file currently
     * being transferred.
     *
     * @param checksum the checksum of the file currently being transferred
     */
    public synchronized void setChecksum(long checksum) {
        this.checksum = checksum;
    }

    /**
     * Sets the value of the checksum of the received portion of the MacOS
     * "resource fork" being sent.
     *
     * @param resForkReceivedChecksum the checksum of the received portion of
     *        the MacOS resource fork being sent
     */
    public synchronized void setResForkReceivedChecksum(
            long resForkReceivedChecksum) {
        this.resForkReceivedChecksum = resForkReceivedChecksum;
    }

    /**
     * Sets the size of the MacOS "resource fork" being sent.
     *
     * @param resForkSize the size, in bytes, of the MacOS resource fork being
     *        sent
     */
    public synchronized void setResForkSize(long resForkSize) {
        this.resForkSize = resForkSize;
    }

    /**
     * Sets the creation date of the file being sent, or <code>0</code> for
     * none. Note that WinAIM does not send the creation date of files being
     * transferred.
     *
     * @param created the creation date of the file being sent, in seconds since
     *        the unix epoch
     */
    public synchronized void setCreated(long created) {
        this.created = created;
    }

    /**
     * Sets the value of the {@linkplain FileTransferChecksum checksum} of the
     * MacOS "resource fork" being transferred.
     *
     * @param resForkChecksum the value of the checksum of the MacOS resource
     *        fork being transferred
     */
    public synchronized void setResForkChecksum(long resForkChecksum) {
        this.resForkChecksum = resForkChecksum;
    }

    /**
     * Sets the number of bytes received in the current transfer.
     *
     * @param bytesReceived the number of bytes received in the current transfer
     */
    public synchronized void setBytesReceived(long bytesReceived) {
        this.bytesReceived = bytesReceived;
    }

    /**
     * Sets the value of the {@linkplain FileTransferChecksum checksum} of the
     * data received.
     *
     * @param receivedChecksum the checksum of the data received in the current
     *        transfer
     */
    public synchronized void setReceivedChecksum(long receivedChecksum) {
        this.receivedChecksum = receivedChecksum;
    }

    /**
     * Sets the file transfer client ID string for this header. (WinAIM sends
     * values like {@link #CLIENTID_DEFAULT} and {@link #CLIENTID_OLD}.) Note
     * that the given string will be truncated to 32 characters as per the
     * protocol spec.
     *
     * @param clientid the file transfer client ID string for this header
     */
    public synchronized void setClientid(String clientid) {
        this.clientid = clientid;
    }

    /**
     * Sets this header's bit flags. The given value should normally be a
     * bitwise combination of {@linkplain #FLAG_DEFAULT the
     * <code>FLAG_<i>*</i></code> constants} defined in this class.
     *
     * @param flags a set of bit flags for this header
     */
    public synchronized void setFlags(int flags) { this.flags = flags; }

    /**
     * Sets the value of the "list name offset" in this header. As of this
     * writing, the significance of this value is unknown. Recent versions of
     * WinAIM always use {@link #LISTNAMEOFFSET_DEFAULT} for this value.
     *
     * @param listNameOffset a new "list name offset" value
     */
    public synchronized void setListNameOffset(int listNameOffset) {
        this.listNameOffset = listNameOffset;
    }

    /**
     * Sets the value of the "list size offset" in this header. As of this
     * writing, the significance of this value is unknown. Recent versions of
     * WinAIM always use {@link #LISTSIZEOFFSET_DEFAULT} for this value.
     *
     * @param listSizeOffset a new "list size offset" value
     */
    public synchronized void setListSizeOffset(int listSizeOffset) {
        this.listSizeOffset = listSizeOffset;
    }

    /**
     * Sets this header's "dummy block" value. Note that this value will be
     * truncated to (sigh) 69 bytes as per the protocol spec. Note that in all
     * clients this value is {@link #DUMMY_DEFAULT} (69 null bytes).
     *
     * @param dummyBlock a new "dummy block" value
     */
    public synchronized void setDummyBlock(ByteBlock dummyBlock) {
        this.dummyBlock = dummyBlock;
    }

    /**
     * Sets this header's Mac file information block. See {@link
     * #getMacFileInfo()} for details on what this value means.
     *
     * @param macFileInfo a new Mac file information block value
     */
    public synchronized void setMacFileInfo(ByteBlock macFileInfo) {
        this.macFileInfo = macFileInfo;
    }

    /**
     * Sets this header's filename value.
     *
     * @param filename the name of the file being transferred
     */
    public synchronized void setFilename(SegmentedFilename filename) {
        this.filename = filename;
    }

    /**
     * Sets the size, in bytes, of this header object, as read from an incoming
     * block of binary data.
     *
     * @param headerSize the size, in bytes, of this header object
     */
    private synchronized void setHeaderSize(int headerSize) {
        this.headerSize = headerSize;
    }

    /**
     * Sets all fields in this header (<i>except</i> <code>filename</code> and
     * <code>headerType</code>) to a set of default values. Calling this method
     * is equivalent to executing the following code:
     * <pre>
header.setClientid(FileTransferHeader.CLIENTID_DEFAULT);
header.setCompression(FileTransferHeader.COMPRESSION_NONE);
header.setEncryption(FileTransferHeader.ENCRYPTION_NONE);
header.setIcbmMessageId(0);
header.setDummyBlock(FileTransferHeader.DUMMY_DEFAULT);
header.setFlags(FileTransferHeader.FLAG_DEFAULT);
header.setFtVersion(FileTransferHeader.FTVERSION_DEFAULT);
header.setResForkChecksum(0);
header.setResForkReceivedChecksum(0);
header.setResForkSize(0);
header.setCreated(0);
header.setLastmod(0);
header.setChecksum(0);
header.setListNameOffset(0);
header.setListSizeOffset(0);
header.setMacFileInfo(FileTransferHeader.MACFILEINFO_DEFAULT);
header.setBytesReceived(0);
header.setFileCount(0);
header.setFileSize(0);
header.setFilesLeft(0);
header.setPartCount(0);
header.setPartsLeft(0);
header.setReceivedChecksum(0);
header.setTotalFileSize(0);
     * </pre>
     */
    public synchronized final void setDefaults() {
        this.clientid = CLIENTID_DEFAULT;
        this.compression = COMPRESSION_NONE;
        this.encryption = ENCRYPTION_NONE;
        this.icbmMessageId = 0;
        this.dummyBlock = DUMMY_DEFAULT;
        this.flags = FLAG_DEFAULT;
        this.ftVersion = FTVERSION_DEFAULT;
        this.resForkChecksum = 0;
        this.resForkReceivedChecksum = 0;
        this.resForkSize = 0;
        this.created = 0;
        this.lastmod = 0;
        this.checksum = 0;
        this.listNameOffset = 0;
        this.listSizeOffset = 0;
        this.macFileInfo = MACFILEINFO_DEFAULT;
        this.bytesReceived = 0;
        this.fileCount = 0;
        this.fileSize = 0;
        this.filesLeft = 0;
        this.partCount = 0;
        this.partsLeft = 0;
        this.receivedChecksum = 0;
        this.totalFileSize = 0;
    }

    /**
     * Ensures that all fields in this class are set to valid values.
     *
     * @throws IllegalArgumentException if a field is invalid
     */
    private synchronized void checkValidity() throws IllegalArgumentException {
        DefensiveTools.checkNull(ftVersion, "ftVersion");
        DefensiveTools.checkRange(headerType, "headerType", 0);
        DefensiveTools.checkRange(encryption, "encryption", 0);
        DefensiveTools.checkRange(compression, "compression", 0);
        DefensiveTools.checkRange(fileCount, "fileCount", 0);
        DefensiveTools.checkRange(filesLeft, "filesLeft", 0);
        DefensiveTools.checkRange(partCount, "partCount", 0);
        DefensiveTools.checkRange(partsLeft, "partsLeft", 0);
        DefensiveTools.checkRange(totalFileSize, "totalFileSize", 0);
        DefensiveTools.checkRange(fileSize, "fileSize", 0);
        DefensiveTools.checkRange(lastmod, "lastmod", 0);
        DefensiveTools.checkRange(checksum, "checksum", 0);
        DefensiveTools.checkRange(resForkReceivedChecksum,
                "resForkReceivedChecksum", 0);
        DefensiveTools.checkRange(resForkSize, "resForkSize", 0);
        DefensiveTools.checkRange(created, "created", 0);
        DefensiveTools.checkRange(resForkChecksum, "resForkChecksum", 0);
        DefensiveTools.checkRange(bytesReceived, "bytesReceived", 0);
        DefensiveTools.checkRange(receivedChecksum, "receivedChecksum", 0);
        DefensiveTools.checkNull(clientid, "clientid");
        DefensiveTools.checkRange(flags, "flags", 0);
        DefensiveTools.checkRange(listNameOffset, "listNameOffset", 0);
        DefensiveTools.checkRange(listSizeOffset, "listSizeOffset", 0);
        DefensiveTools.checkNull(dummyBlock, "dummyBlock");
        DefensiveTools.checkNull(macFileInfo, "macFileInfo");
        // whew.
    }

    /**
     * Writes this header to the given stream. Note that <i>all fields in this
     * header must have valid values</i> upon a call to this method; otherwise,
     * an <code>IllegalArgumentException</code> will be thrown. A valid field
     * has either a nonnegative (that is, <code>0</code> or greater) or
     * non-<code>null</code> value. Only the <code>filename</code> field may be
     * <code>null</code>; such a value indicates that no filename should be
     * sent.
     *
     * @param out the stream to which to write

     * @throws IOException if an I/O error occurs
     * @throws IllegalArgumentException if any field in this header has an
     *         invalid value
     */
    public synchronized void write(OutputStream out)
            throws IOException, IllegalArgumentException {
        DefensiveTools.checkNull(out, "out");

        checkValidity();

        // build the header block
        ByteArrayOutputStream header = new ByteArrayOutputStream(300);
        BinaryTools.writeUShort(header, headerType);
        BinaryTools.writeLong(header, icbmMessageId);
        BinaryTools.writeUShort(header, encryption);
        BinaryTools.writeUShort(header, compression);

        BinaryTools.writeUShort(header, fileCount);
        BinaryTools.writeUShort(header, filesLeft);
        BinaryTools.writeUShort(header, partCount);
        BinaryTools.writeUShort(header, partsLeft);

        BinaryTools.writeUInt(header, totalFileSize);
        BinaryTools.writeUInt(header, fileSize);
        BinaryTools.writeUInt(header, lastmod);
        BinaryTools.writeUInt(header, checksum);

        BinaryTools.writeUInt(header, resForkReceivedChecksum);
        BinaryTools.writeUInt(header, resForkSize);
        BinaryTools.writeUInt(header, created);
        BinaryTools.writeUInt(header, resForkChecksum);

        BinaryTools.writeUInt(header, bytesReceived);
        BinaryTools.writeUInt(header, receivedChecksum);

        // this needs to be 32 bytes...
        ByteBlock clientidBytes
                = ByteBlock.wrap(BinaryTools.getAsciiBytes(clientid));
        BinaryTools.writeNullPadded(header, clientidBytes, 32);

        BinaryTools.writeUByte(header, flags);
        BinaryTools.writeUByte(header, listNameOffset);
        BinaryTools.writeUByte(header, listSizeOffset);

        // this needs to be (sigh) 69 bytes
        BinaryTools.writeNullPadded(header, dummyBlock, 69);

        // this needs to be 16 bytes
        BinaryTools.writeNullPadded(header, macFileInfo, 16);

        // write the segmented filename
        String filenameStr;
        if (filename == null) filenameStr = "";
        else filenameStr = filename.toFTFilename();

        ImEncodedString encInfo = ImEncodedString.encodeString(filenameStr);
        ImEncodingParams encoding = encInfo.getEncoding();

        BinaryTools.writeUShort(header, encoding.getCharsetCode());
        BinaryTools.writeUShort(header, encoding.getCharsetSubcode());

        byte[] fnBytes = encInfo.getBytes();
        header.write(fnBytes);
        // pad this so it's (at least) 63 bytes
        for (int i = fnBytes.length; i < 63; i++) {
            header.write(0);
        }
        // and write a final null
        header.write(0);

        // and write the packet we created above to the stream
        ByteArrayOutputStream fullBuffer
                = new ByteArrayOutputStream(header.size() + 6);
        fullBuffer.write(BinaryTools.getAsciiBytes(ftVersion));
        BinaryTools.writeUShort(fullBuffer, header.size() + 6);
        header.writeTo(fullBuffer);

        // then write that packet to the stream, so it's all in one happy TCP
        // packet
        fullBuffer.writeTo(out);
    }

    public synchronized String toString() {
        return "FileTransferHeader:" +
                "\n ftVersion='" + ftVersion + "'" +
                "\n headerType=0x" + Integer.toHexString(headerType) +
                "\n icbmMessageId=" + icbmMessageId +
                "\n encryption=" + encryption +
                "\n compression=" + compression +
                "\n fileCount=" + fileCount +
                "\n filesLeft=" + filesLeft +
                "\n partCount=" + partCount +
                "\n partsLeft=" + partsLeft +
                "\n totalSize=" + totalFileSize +
                "\n fileSize=" + fileSize +
                "\n lastmod=" + lastmod +
                "\n checksum=" + checksum +
                "\n resForkReceivedChecksum=" + resForkReceivedChecksum +
                "\n resForkSize=" + resForkSize +
                "\n created=" + created +
                "\n resForkChecksum=" + resForkChecksum +
                "\n bytesReceived=" + bytesReceived +
                "\n receivedChecksum=" + receivedChecksum +
                "\n clientid='" + clientid + "'" +
                "\n flags=0x" + Integer.toHexString(flags) +
                "\n listNameOffset=" + listNameOffset +
                "\n listSizeOffset=" + listSizeOffset +
                "\n macFileInfo=" + BinaryTools.describeData(macFileInfo) +
                "\n filename=" + filename;
    }
}
