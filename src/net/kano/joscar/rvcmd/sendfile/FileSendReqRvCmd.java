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

import net.kano.joscar.ByteBlock;
import net.kano.joscar.rvcmd.AbstractRequestRvCmd;
import net.kano.joscar.rvcmd.InvitationMessage;
import net.kano.joscar.rvcmd.RvConnectionInfo;
import net.kano.joscar.snaccmd.CapabilityBlock;
import net.kano.joscar.snaccmd.icbm.RecvRvIcbm;
import net.kano.joscar.tlv.TlvChain;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A rendezvous command used to attempt to send one or more files to another
 * user.
 */
public class FileSendReqRvCmd extends AbstractRequestRvCmd {
    /** The "invitation message" for this request. */
    private final InvitationMessage invMessage;
    /**
     * The connection information block describing the connection to be made.
     */
    private final RvConnectionInfo connInfo;
    /** An object describing the file or files being sent. */
    private final FileSendBlock fileSendBlock;

    /**
     * Creates a new file send request from the given incoming file send request
     * RV ICBM.
     *
     * @param icbm an incoming file send request RV ICBM
     */
    public FileSendReqRvCmd(RecvRvIcbm icbm) {
        super(icbm);

        TlvChain chain = getRvTlvs();

        invMessage = InvitationMessage.readInvitationMessage(chain);

        ByteBlock sendData = getServiceData();
        fileSendBlock = (sendData == null
                ? null
                : FileSendBlock.readFileSendBlock(sendData));

        connInfo = RvConnectionInfo.readConnectionInfo(chain);
    }

    /**
     * Creates a new outgoing initial file send request with the given message,
     * connection information, and file transfer information block. As an
     * initial request, the created request's request type will be {@link
     * #REQTYPE_INITIALREQUEST}.
     * <br>
     * <br>
     * Using this constructor is equivalent to using {@link
     * #FileSendReqRvCmd(int, InvitationMessage, RvConnectionInfo,
     * FileSendBlock) new FileSendReqRvCmd(REQTYPE_INITIALREQUEST, message,
     * connInfo, file)}.
     *
     * @param message an "invitation message," a message displayed to the user
     *        upon receiving this request, or <code>null</code> to not include
     *        an invitation message
     * @param connInfo a connection information block describing the connection
     *        to be made, or <code>null</code> to not specify connection
     *        information
     * @param fileInfo an object describing the file or files being sent
     */
    public FileSendReqRvCmd(InvitationMessage message,
            RvConnectionInfo connInfo, FileSendBlock fileInfo) {
        this(REQTYPE_INITIALREQUEST, message, connInfo, fileInfo);
    }

    /**
     * Creates a new outgoing file send connection redirection command with the
     * given connection information.
     * <br>
     * <br>
     * Using this constructor is equivalent to using {@link
     * #FileSendReqRvCmd(int, InvitationMessage, RvConnectionInfo,
     * FileSendBlock) new FileSendReqRvCmd(REQTYPE_REDIRECT, null, connInfo,
     * null)}.
     *
     * @param connInfo a block of connection information describing the
     *        connection to which a file send connection has been redirected
     */
    public FileSendReqRvCmd(RvConnectionInfo connInfo) {
        this(REQTYPE_REDIRECT, null, connInfo, null);
    }

    /**
     * Creates a new outgoing file send request command with the given
     * properties.
     *
     * @param requestType a request type, like {@link #REQTYPE_INITIALREQUEST}
     * @param message an "invitation message," a message displayed to the user
     *        upon receiving this request, or <code>null</code> to not include
     *        an invitation message
     * @param connInfo a connection information block describing the connection
     *        to be made, or <code>null</code> to not specify connection
     *        information
     * @param fileInfo an object describing the file or files being sent
     */
    public FileSendReqRvCmd(int requestType, InvitationMessage message,
            RvConnectionInfo connInfo, FileSendBlock fileInfo) {
        super(CapabilityBlock.BLOCK_FILE_SEND, requestType);

        this.connInfo = connInfo;
        this.fileSendBlock = fileInfo;
        this.invMessage = message;
    }

    /**
     * Returns the invitation message sent in this command, or <code>null</code>
     * if none was sent.
     *
     * @return this request's "invitation message"
     */
    public final InvitationMessage getMessage() { return invMessage; }

    /**
     * Returns the connection information block sent in this request.
     *
     * @return the connection information block sent in this request
     */
    public final RvConnectionInfo getConnInfo() { return connInfo; }

    /**
     * Returns an object containing information about the file or files being
     * sent, or <code>null</code> if no such information was sent.
     *
     * @return an object containing information about the file or files being
     *         transferred
     */
    public final FileSendBlock getFileSendBlock() { return fileSendBlock; }

    public void writeRvTlvs(OutputStream out) throws IOException {
        if (invMessage != null) invMessage.write(out);
        if (connInfo != null) connInfo.write(out);
    }

    protected boolean hasServiceData() { return fileSendBlock != null; }

    protected void writeServiceData(OutputStream out) throws IOException {
        if (fileSendBlock != null) fileSendBlock.write(out);
    }

    public String toString() {
        return "FileSendReqRvCmd: " +
                "reqType=" + getRequestType() +
                ", message='" + invMessage + "'" +
                ", connInfo=" + connInfo +
                ", fileSendBlock=" + fileSendBlock;
    }
}
