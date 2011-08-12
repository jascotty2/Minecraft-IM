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
 *  File created by keith @ Mar 2, 2003
 *
 */

package net.kano.joscar.snaccmd.icon;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.flapcmd.SnacPacket;
import net.kano.joscar.snaccmd.ExtraInfoBlock;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A SNAC command sent to acknowledge that a buddy icon has successfully been
 * uploaded. Normally sent in response to a {@link UploadIconCmd}.
 *
 * @snac.src server
 * @snac.cmd 0x10 0x03
 *
 * @see UploadIconCmd
 */
public class UploadIconAck extends IconCommand {
    /** A default value for the acknowledgement code sent in this command. */
    public static final int CODE_DEFAULT = 0x0000;

    /** The acknowledgement code. */
    private final int code;
    /** An icon information block corresponding to the uploaded icon. */
    private final ExtraInfoBlock iconInfo;

    /**
     * Generates an icon upload acknowledgement command from the given incoming
     * SNAC packet.
     *
     * @param packet an incoming icon upload acknowledgement packet
     */
    protected UploadIconAck(SnacPacket packet) {
        super(CMD_UPLOAD_ACK);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

        code = BinaryTools.getUByte(snacData, 0);

        ByteBlock iconBlock = snacData.subBlock(1);

        iconInfo = ExtraInfoBlock.readExtraInfoBlock(iconBlock);
    }

    /**
     * Creates a new outgoing icon upload acknowledgement command with the
     * given icon information block and a <code>code</code> of {@link
     * #CODE_DEFAULT}. Using this constructor is equivalent to using {@link
     * #UploadIconAck(int, ExtraInfoBlock) new
     * UploadIconAck(UploadIconAck.CODE_DEFAULT, iconInfo)}.
     *
     * @param iconInfo an icon information block corresponding to the uploaded
     *        icon
     */
    public UploadIconAck(ExtraInfoBlock iconInfo) {
        this(CODE_DEFAULT, iconInfo);
    }

    /**
     * Creates a new outgoing icon upload acknowledgement command with the given
     * icon information block and the given code.
     *
     * @param code some sort of code, normally {@link #CODE_DEFAULT}
     * @param iconInfo an icon information block corresponding to the uploaded
     *        icon
     */
    public UploadIconAck(int code, ExtraInfoBlock iconInfo) {
        super(CMD_UPLOAD_ACK);

        DefensiveTools.checkRange(code, "code", 0);

        this.code = code;
        this.iconInfo = iconInfo;
    }

    /**
     * Returns the acknowledgement code sent in this command. Normally {@link
     * #CODE_DEFAULT}.
     *
     * @return some sort of acknowledgement code
     */
    public final int getCode() {
        return code;
    }

    /**
     * Returns the icon information block corresponding to the uploaded icon.
     *
     * @return the icon information block for the uploaded icon
     */
    public final ExtraInfoBlock getIconInfo() {
        return iconInfo;
    }

    public void writeData(OutputStream out) throws IOException {
        BinaryTools.writeUByte(out, code);
        if (iconInfo != null) iconInfo.write(out);
    }

    public String toString() {
        return "UploadIconAck: code=" + code + ", iconInfo=<" + iconInfo + ">";
    }
}
