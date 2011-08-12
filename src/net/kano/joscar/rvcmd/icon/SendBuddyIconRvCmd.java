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

package net.kano.joscar.rvcmd.icon;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.LiveWritable;
import net.kano.joscar.rvcmd.AbstractRequestRvCmd;
import net.kano.joscar.snaccmd.CapabilityBlock;
import net.kano.joscar.snaccmd.icbm.OldIconHashInfo;
import net.kano.joscar.snaccmd.icbm.RecvRvIcbm;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A rendezvous command used to send one's "buddy icon" to another user. Note
 * that while this method of distributing one's icon should be supported for
 * backwards compability, it is becoming obsolete with the coming of the
 * {@linkplain net.kano.joscar.snaccmd.icon buddy icon service}.
 * <br>
 * <br>
 * Note that while it may seem odd, one's buddy icon is sent in its entirety
 * over the SNAC connection in an (this) rendezvous command.
 */
public class SendBuddyIconRvCmd extends AbstractRequestRvCmd {
    /** An "icon ID string" used by WinAIM by default. */
    public static final String ICONIDSTRING_DEFAULT = "AVT1picture.id";

    /** A block of "old icon hash data." */
    private final OldIconHashInfo hash;
    /** The buddy icon itself. */
    private final ByteBlock iconData;
    /** An object to be used in writing the icon to an outgoing stream. */
    private final LiveWritable iconWriter;
    /** A string that somehow identifies the icon being sent. */
    private final String iconId;

    /**
     * Creates a new send-buddy-icon command from the given incoming
     * send-buddy-icon RV ICBM.
     *
     * @param icbm an incoming send-buddy-icon RV ICBM command
     */
    public SendBuddyIconRvCmd(RecvRvIcbm icbm) {
        super(icbm);

        iconWriter = null;

        ByteBlock serviceData = getServiceData();

        OldIconHashInfo hash = null;
        ByteBlock iconData = null;
        String iconId = null;
        if (serviceData != null) {
            hash = OldIconHashInfo.readIconHashFromRvData(serviceData);
            if (hash != null) {
                int hashLen = hash.getRvDataFormatLength();
                int iconSize = (int) hash.getIconSize();

                if (serviceData.getLength() >= hashLen + iconSize) {
                    iconData = serviceData.subBlock(hashLen, iconSize);

                    ByteBlock iconIdBlock = serviceData.subBlock(hashLen
                            + iconSize);
                    if (iconIdBlock.getLength() > 0) {
                        // we only want an icon ID string if it's not empty
                        iconId = BinaryTools.getAsciiString(iconIdBlock);
                    }
                }
            }
        }

        this.hash = hash;
        this.iconData = iconData;
        this.iconId = iconId;
    }

    /**
     * Creates a new outgoing buddy icon send command with the given icon hash
     * and the given icon data writer. The given icon data writer will be used
     * (via its <code>write</code> method) to write the buddy icon to the OSCAR
     * connection upon sending this command. The "ID string" of the icon will
     * be set to {@link #ICONIDSTRING_DEFAULT}.
     * <br>
     * <br>
     * Using this constructor is equivalent to using {@link
     * #SendBuddyIconRvCmd(OldIconHashInfo, LiveWritable, String) new
     * SendBuddyIconRvCmd(hash, iconDataWriter, ICONIDSTRING_DEFAULT)}.
     *
     * @param hash an object representing a "hash" of the icon being sent
     * @param iconDataWriter an object containing the raw buddy icon data
     *
     * @see net.kano.joscar.FileWritable
     */
    public SendBuddyIconRvCmd(OldIconHashInfo hash,
            LiveWritable iconDataWriter) {
        this(hash, iconDataWriter, ICONIDSTRING_DEFAULT);
    }

    /**
     * Creates a new outgoing buddy icon send command with the given icon hash
     * icon data writer, and "ID string." The given icon data writer will be
     * used (via its <code>write</code> method) to write the buddy icon to the
     * OSCAR connection upon sending this command.
     *
     * @param hash an object representing a "hash" of the icon being sent
     * @param iconDataWriter an object containing the raw buddy icon data
     * @param iconIdString an "ID string" for this icon, like {@link
     *        #ICONIDSTRING_DEFAULT}
     */
    public SendBuddyIconRvCmd(OldIconHashInfo hash, LiveWritable iconDataWriter,
            String iconIdString) {
        super(CapabilityBlock.BLOCK_ICON);

        DefensiveTools.checkNull(hash, "hash");
        DefensiveTools.checkNull(iconDataWriter, "iconDataWriter");

        this.hash = hash;
        this.iconData = null;
        this.iconWriter = iconDataWriter;
        this.iconId = iconIdString;
    }

    /**
     * Returns the "old-format icon hash block" sent in this command. This block
     * normally describes the associated buddy icon. Note that this method will
     * return <code>null</code> if no icon hash block is present in this
     * command.
     *
     * @return an icon hash block describing the icon being sent, or
     *         <code>null</code> if none is present
     */
    public final OldIconHashInfo getIconHash() { return hash; }

    /**
     * Returns the block of buddy icon data sent in this command. Note that this
     * value will be <code>null</code> if this is an outgoing command.
     *
     * @return the raw buddy icon data sent in this command, or
     *         <code>null</code> if none was sent or if this is an outgoing
     *         command
     */
    public final ByteBlock getIconData() { return iconData; }

    /**
     * Returns the "icon ID string" sent with the associated icon, if any. This
     * value does not seem to affect anything in WinAIM or iChat. This will
     * normally be a short string such as <code>"AVT1picture.id"</code>.
     *
     * @return the associated icon's "ID string," or <code>null</code> if none
     *         was sent
     */
    public final String getIconIdString() { return iconId; }

    protected void writeRvTlvs(OutputStream out) throws IOException { }

    protected void writeServiceData(OutputStream out) throws IOException {
        if (hash != null) {
            hash.writeToRvData(out);

            if (iconData != null || iconWriter != null) {
                if (iconWriter != null) iconWriter.write(out);
                else iconData.write(out);

                if (iconId != null) {
                    out.write(BinaryTools.getAsciiBytes(iconId));
                }
            }
        }
    }

    public String toString() {
        return "SendBuddyIconRvCmd: hash=" + hash + ", icon="
                + (iconData == null ? -1 : iconData.getLength()) + " bytes (id="
                + iconId + ")";
    }
}
