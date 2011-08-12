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
import net.kano.joscar.OscarTools;
import net.kano.joscar.StringBlock;
import net.kano.joscar.flapcmd.SnacPacket;
import net.kano.joscar.snaccmd.ExtraInfoBlock;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A SNAC command sent to request another user's buddy icon. Normally
 * responded-to with an {@link IconDataCmd}.
 *
 * @snac.src client
 * @snac.cmd 0x10 0x04
 *
 * @see IconDataCmd
 */
public class IconRequest extends IconCommand {
    /**
     * A default value for the "icon request code" sent in an
     * <code>IconRequest</code>.
     */
    public static final int CODE_DEFAULT = 0x0001;

    /** The screenname whose icon is being requested. */
    private final String sn;
    /** Some sort of request code. */
    private final int code;
    /** The icon data whose corresponding icon is being requested. */
    private final ExtraInfoBlock iconInfo;

    /**
     * Generates a new icon request command from the given incoming SNAC packet.
     *
     * @param packet an incoming icon request packet
     */
    protected IconRequest(SnacPacket packet) {
        super(CMD_ICON_REQ);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

        StringBlock snInfo = OscarTools.readScreenname(snacData);

        sn = snInfo.getString();

        ByteBlock rest = snacData.subBlock(snInfo.getTotalSize());

        code = BinaryTools.getUByte(rest, 0);

        ByteBlock iconBlock = rest.subBlock(1);

        iconInfo = ExtraInfoBlock.readExtraInfoBlock(iconBlock);
    }

    /**
     * Creates a new outgoing (server-bound) icon request for the icon
     * corresponding to the given icon information block from the given user.
     * The <code>code</code> field is set to {@link #CODE_DEFAULT}.
     *
     * @param sn the screenname of the user whose buddy icon is being requested
     * @param iconInfo the icon information block whose corresponding icon is
     *        being requested
     */
    public IconRequest(String sn, ExtraInfoBlock iconInfo) {
        this(sn, CODE_DEFAULT, iconInfo);
    }

    /**
     * Creates a new outgoing (server-bound) icon request for the icon
     * corresponding to the given icon information block from the given user.
     *
     * @param sn the screenname of the user whose buddy icon is being requested
     * @param code some sort of code; WinAIM sends {@link #CODE_DEFAULT}
     * @param iconInfo the icon information block whose corresponding icon is
     *        being requested
     */
    public IconRequest(String sn, int code, ExtraInfoBlock iconInfo) {
        super(CMD_ICON_REQ);

        DefensiveTools.checkNull(sn, "sn");
        DefensiveTools.checkRange(code, "code", 0);
        DefensiveTools.checkNull(iconInfo, "iconInfo");

        this.sn = sn;
        this.code = code;
        this.iconInfo = iconInfo;
    }

    /**
     * Returns the screenname of the user whose icon is being requested.
     *
     * @return the screenname of the user whose icon is being requested
     */
    public final String getScreenname() {
        return sn;
    }

    /**
     * Returns some sort of code included in this request. Normally {@link
     * #CODE_DEFAULT}.
     *
     * @return the icon request code
     */
    public final int getCode() {
        return code;
    }

    /**
     * Returns the icon information block corresponding to the icon being
     * requested.
     *
     * @return an icon information block describing the icon being requested
     */
    public final ExtraInfoBlock getIconInfo() {
        return iconInfo;
    }

    public void writeData(OutputStream out) throws IOException {
        OscarTools.writeScreenname(out, sn);
        BinaryTools.writeUByte(out, code);
        iconInfo.write(out);
    }

    public String toString() {
        return "IconRequest for " + sn + " (code=" + code + "): iconInfo=<"
                + iconInfo + ">";
    }
}
