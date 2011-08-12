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
 *  File created by keith @ Apr 28, 2003
 *
 */

package net.kano.joscar.rvcmd.getfile;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.rvcmd.AbstractRequestRvCmd;
import net.kano.joscar.rvcmd.RvConnectionInfo;
import net.kano.joscar.snaccmd.CapabilityBlock;
import net.kano.joscar.snaccmd.icbm.RecvRvIcbm;
import net.kano.joscar.tlv.Tlv;
import net.kano.joscar.tlv.TlvChain;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A rendezvous command used to request browsing another user's files via a
 * "Get File" connection. This command is also used to "redirect" a Get File
 * connection to a new TCP "location" should a connection attempt as per the
 * initial request's connection information block fail.
 * <br>
 * <br>
 * Note that several fields of this command are not yet understood:
 * <ul>
 * <li> The value of the request's "code," which is always <code>0x0012</code>
 * ({@link #CODE_DEFAULT}) in WinAIM </li>
 * <li> The value of the "protocol version," which is <code>0x0002</code>
 * ({@link #PROTOVERSION_DEFAULT}) in WinAIM 5.2 and <code>0x0001</code> ({@link
 * #PROTOVERSION_OLD}) in WinAIM 5.1 and previous </li>
 * <li> The value of the "extra data block", which is one null byte ({@link
 * #EXTRABLOCK_DEFAULT}) in AIM 5.2 and four null bytes in AIM 5.1 and
 * previous</li>
 * <li> The value of the "charset name," which seems to be ignored completely
 * by WinAIM (WinAIM sends <code>"us-ascii"</code> ({@link
 * #CHARSET_DEFAULT})) </li>
 * </ul>
 */
public class GetFileReqRvCmd extends AbstractRequestRvCmd {
    /*
    AIM 5.1:      00 12 00 01 00 00 00 00 00 00 00 00
    AIM 5.2 beta: 00 12 00 02 00 00 00 01 00
    */

    /**
     * A charset value used in Get File requests by WinAIM regardless of file
     * content or platform.
     */
    public static final String CHARSET_DEFAULT = "us-ascii";

    /** The "code" value always used by WinAIM. */
    public static final int CODE_DEFAULT = 0x0012;

    /** The "protocol version" used by WinAIM 5.2 beta. */
    public static final int PROTOVERSION_DEFAULT = 0x0002;
    /** The "protocol version" used by WinAIM 5.1.* and older. */
    public static final int PROTOVERSION_OLD = 0x0001;

    /**
     * A flag used to indicate that the user's entire shared file list should
     * not be listed recursively on connect but rather only a single directory,
     * with subdirectories being listed dynamically as they are viewed by the
     * user. This feature was added in WinAIM 5.2 beta to improve performance
     * when sharing many files and should be used whenever possible.
     */
    public static final long FLAG_EXPAND_DYNAMIC = 0x00000001L;

    /**
     * An "extra data block" used by WinAIM 5.2. As of this writing, this
     * block's significance is unknown.
     */
    public static final ByteBlock EXTRABLOCK_DEFAULT
            = ByteBlock.wrap(new byte[] { 0x00 });

    /**
     * A TLV type containing the name of a charset (which appears not to be used
     * anywhere).
     */
    private static final int TYPE_CHARSET = 0x2712;

    /** A connection information block sent in this request. */
    private final RvConnectionInfo connInfo;
    /** The charset name sent in this block. */
    private final String charset;
    /** The value of some sort of code sent in this request. */
    private final int code;
    /** The value of the protocol version sent in this request. */
    private final int protoVersion;
    /** A set of bit flags sent in this request. */
    private final long flags;
    /** A block of "extra data" sent in this request. */
    private final ByteBlock extraBlock;

    /**
     * Creates a new Get File session request from the given incoming Get File
     * request RV ICBM.
     *
     * @param icbm an incoming Get File request RV ICBM
     */
    public GetFileReqRvCmd(RecvRvIcbm icbm) {
        super(icbm);

        TlvChain chain = getRvTlvs();

        connInfo = RvConnectionInfo.readConnectionInfo(chain);

        charset = chain.getString(TYPE_CHARSET);

        ByteBlock block = getServiceData();

        if (block == null) {
            code = -1;
            protoVersion = -1;
            flags = -1;
            extraBlock = null;
        } else {
            code = BinaryTools.getUShort(block, 0);
            protoVersion = BinaryTools.getUShort(block, 2);
            flags = BinaryTools.getUInt(block, 4);

            if (block.getLength() > 8) {
                extraBlock = ByteBlock.wrap(block.subBlock(8).toByteArray());
            } else {
                extraBlock = null;
            }
        }
    }

    /**
     * Creates a new outgoing initial Get File session request with the given
     * connection information block and a default set of other properties.
     * <br>
     * <br>
     * Using this constructor is equivalent to using {@link
     * #GetFileReqRvCmd(int, RvConnectionInfo, String, int, int, long,
     * ByteBlock) new GetFileReqRvCmd(REQTYPE_INITIALREQUEST, connInfo,
     * CHARSET_DEFAULT, CODE_DEFAULT, PROTOVERSION_DEFAULT,
     * FLAG_EXPAND_DYNAMIC, EXTRABLOCK_DEFAULT)}.
     *
     * @param connInfo a block of connection information to send in this command
     */
    public GetFileReqRvCmd(RvConnectionInfo connInfo) {
        this(connInfo, FLAG_EXPAND_DYNAMIC);
    }

    /**
     * Creates a new outgoing initial Get File session request with the given
     * connection information block and set of flags as well as a default set of
     * other properties.
     * <br>
     * <br>
     * Using this constructor is equivalent to using {@link
     * #GetFileReqRvCmd(int, RvConnectionInfo, String, int, int, long,
     * ByteBlock) new GetFileReqRvCmd(REQTYPE_INITIALREQUEST, connInfo,
     * CHARSET_DEFAULT, CODE_DEFAULT, PROTOVERSION_DEFAULT, flags,
     * EXTRABLOCK_DEFAULT)}.
     *
     * @param connInfo a block of connection information to send in this command
     * @param flags a set of flags, like {@link #FLAG_EXPAND_DYNAMIC}, or
     *        <code>0</code> (for none)
     */
    public GetFileReqRvCmd(RvConnectionInfo connInfo, long flags) {
        this(REQTYPE_INITIALREQUEST, connInfo, CHARSET_DEFAULT, CODE_DEFAULT,
                PROTOVERSION_DEFAULT, flags, EXTRABLOCK_DEFAULT);
    }

    /**
     * Creates a new outgoing Get File session request / redirect with the given
     * request type and the given connection information block.
     * <br>
     * <br>
     * Using this constructor is equivalent to using {@link
     * #GetFileReqRvCmd(int, RvConnectionInfo, String, int, int, long,
     * ByteBlock) new GetFileReqRvCmd(requestType, connInfo,
     * CHARSET_DEFAULT, CODE_DEFAULT, PROTOVERSION_DEFAULT, FLAG_EXPAND_DYNAMIC,
     * EXTRABLOCK_DEFAULT)}.
     *
     * @param requestType a request type, like {@link #REQTYPE_REDIRECT}
     * @param connInfo a block of connection information to send in this command
     */
    public GetFileReqRvCmd(int requestType, RvConnectionInfo connInfo) {
        this(requestType, connInfo, CHARSET_DEFAULT, CODE_DEFAULT,
                PROTOVERSION_DEFAULT, FLAG_EXPAND_DYNAMIC, EXTRABLOCK_DEFAULT);
    }

    /**
     * Creates a new outgoing Get File session request / redirect (depending on
     * <code>requestType</code>) with the given properties.
     *
     * @param requestType a request type, like {@link #REQTYPE_INITIALREQUEST}
     * @param connInfo a block of connection information to use for the
     *        associated connection
     * @param charset the name of a charset (this value appears to be ignored
     *        by WinAIM); normally always {@link #CHARSET_DEFAULT}
     * @param code some sort of "code" value; normally {@link #CODE_DEFAULT}
     * @param protoVersion a "protocol version" value, like {@link
     *        #PROTOVERSION_DEFAULT}
     * @param flags a set of bit flags to send in this request, like {@link
     *        #FLAG_EXPAND_DYNAMIC} or <code>0</code> (for none)
     * @param extraBlock an "extra" block of data; normally {@link
     *        #EXTRABLOCK_DEFAULT}
     */
    public GetFileReqRvCmd(int requestType, RvConnectionInfo connInfo,
            String charset, int code, int protoVersion, long flags,
            ByteBlock extraBlock) {
        super(CapabilityBlock.BLOCK_FILE_GET, requestType);

        DefensiveTools.checkRange(code, "code", -1);
        DefensiveTools.checkRange(protoVersion, "protoVersion", -1);
        DefensiveTools.checkRange(flags, "flags", -1);

        this.connInfo = connInfo;
        this.charset = charset;
        this.code = code;
        this.protoVersion = protoVersion;
        this.flags = flags;
        this.extraBlock = extraBlock;
    }

    /**
     * Returns the connection information block sent in this
     * request/redirection.
     *
     * @return this command's connection information block
     */
    public final RvConnectionInfo getConnInfo() { return connInfo; }

    /**
     * Returns the charset name sent in this request. Note that this value
     * appears to be ignored by the official clients; WinAIM always sends
     * {@link #CHARSET_DEFAULT} (<code>"us-ascii"</code>). See {@linkplain
     * GetFileReqRvCmd above} for details.
     *
     * @return the charset name sent in this request, or <code>null</code> if
     *         none was sent
     */
    public final String getCharset() { return charset; }

    /**
     * Returns the "code" value sent in this request. See {@linkplain
     * GetFileReqRvCmd above} for details.
     *
     * @return this request's "code" value
     */
    public final int getCode() { return code; }

    /**
     * Returns the "protocol version" value sent in this command. See
     * {@linkplain GetFileReqRvCmd above} for details.
     *
     * @return this command's "protocol version" value
     */
    public final int getProtoVersion() { return protoVersion; }

    /**
     * Returns the bit flags sent in this request. This will normally be a
     * bitwise combination of "flags" like {@link #FLAG_EXPAND_DYNAMIC}. For
     * those unfamiliar with bitwise flags, one could test for a particular flag
     * with code such as the following:
     * <pre>
if ((getFileReq.getFlags() & GetFileReqRvCmd.FLAG_EXPAND_DYNAMIC) != 0) {
     System.out.println("Client supports dynamic file listing!");
}
     * </pre>
     *
     * @return this request's associated bit flags
     */
    public final long getFlags() { return flags; }

    /**
     * Returns this request's "extra data block" value. See {@linkplain
     * GetFileReqRvCmd above} for details.
     *
     * @return this request's "extra data block"
     */
    public final ByteBlock getExtraBlock() { return extraBlock; }

    protected void writeRvTlvs(OutputStream out) throws IOException {
        if (connInfo != null) {
            connInfo.write(out);
        }
        if (charset != null) {
            Tlv.getStringInstance(TYPE_CHARSET, charset).write(out);
        }
    }

    protected boolean hasServiceData() {
        return true;
    }

    protected void writeServiceData(OutputStream out) throws IOException {
        if (code != -1 && protoVersion != -1 && flags != -1) {
            BinaryTools.writeUShort(out, code);
            BinaryTools.writeUShort(out, protoVersion);
            BinaryTools.writeUInt(out, flags);
            if (extraBlock != null) extraBlock.write(out);
        }
    }

    public String toString() {
        return "GetFileReqRvCmd: connInfo=<" + connInfo + ">, code=0x"
                + Integer.toHexString(code) + ", proto=0x"
                + Integer.toHexString(protoVersion) + ", flags=0x"
                + Long.toHexString(flags) + ", extraBlock="
                + (extraBlock == null ? null
                : BinaryTools.describeData(extraBlock));
    }
}
