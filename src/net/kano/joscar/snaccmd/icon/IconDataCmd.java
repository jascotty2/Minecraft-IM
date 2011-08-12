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
 * A SNAC command containing a buddy's icon. Normally sent in response to an
 * {@link IconRequest}.
 *
 * @snac.src server
 * @snac.cmd 0x10 0x05
 *
 * @see IconRequest
 */
public class IconDataCmd extends IconCommand {
    /**
     * The normal value for the extra icon data code of the extra icon
     * information block sent in this command.
     */
    public static final int CODE_DEFAULT = 0x00;

    /** The screenname whose icon this is. */
    private final String sn;
    /** A set of information about the included icon data. */
    private final ExtraInfoBlock iconInfo;
    /** The icon data. */
    private final ByteBlock iconData;

    /**
     * Generates a new icon data command from the given incoming SNAC packet.
     *
     * @param packet an incoming icon data packet
     */
    protected IconDataCmd(SnacPacket packet) {
        super(CMD_ICON_DATA);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

        StringBlock snInfo = OscarTools.readScreenname(snacData);

        sn = snInfo.getString();

        ByteBlock rest = snacData.subBlock(snInfo.getTotalSize());

        iconInfo = ExtraInfoBlock.readExtraInfoBlock(rest);

        rest = rest.subBlock(iconInfo.getTotalSize());

        int iconSize = BinaryTools.getUShort(rest, 0);

        iconData = rest.subBlock(2, iconSize);
    }

    /**
     * Creates a new client-bound outgoing buddy icon data command with the
     * given properties.
     *
     * @param sn the screenname of the user whose buddy icon is contained in
     *        this command
     * @param iconInfo a block of information about the associated icon
     * @param iconData the raw icon data
     */
    public IconDataCmd(String sn, ExtraInfoBlock iconInfo, ByteBlock iconData) {
        super(CMD_ICON_DATA);

        DefensiveTools.checkNull(sn, "sn");

        this.sn = sn;
        this.iconInfo = iconInfo;
        this.iconData = iconData;
    }

    /**
     * Returns the screenname of the user whose buddy icon this command
     * contains.
     *
     * @return the screenname whose icon this is
     */
    public final String getScreenname() {
        return sn;
    }

    /**
     * A block of icon information for this icon.
     *
     * @return a block of icon information for this icon
     */
    public final ExtraInfoBlock getIconInfo() {
        return iconInfo;
    }

    /**
     * The raw buddy icon data.
     *
     * @return the buddy icon data
     */
    public final ByteBlock getIconData() {
        return iconData;
    }

    public void writeData(OutputStream out) throws IOException {
        OscarTools.writeScreenname(out, sn);
        if (iconInfo != null) {
            iconInfo.write(out);
            
            if (iconData != null) {
                BinaryTools.writeUShort(out, iconData.getLength());
                iconData.write(out);
            }
        }
    }

    public String toString() {
        return "IconDataCmd: icon for " + sn  + ": iconInfo=<" + iconInfo
                + ">, icon=" + (iconData == null ? -1 : iconData.getLength())
                + " bytes";
    }
}
