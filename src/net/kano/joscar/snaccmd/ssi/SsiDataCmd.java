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
 *  File created by keith @ Mar 3, 2003
 *
 */

package net.kano.joscar.snaccmd.ssi;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.flapcmd.SnacPacket;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * A SNAC command containing the user's server-stored information. Normally
 * sent in respose to either of {@link SsiDataCheck} and {@link SsiDataRequest}.
 * Note that this command is sent <i>multiple times</i>, spreading out the
 * user's SSI items over multiple commands. To check for this, check to see
 * whether {@link #getLastModDate} is <code>0</code>: if it is, there are more
 * <code>SsiDataCmd</code>s to come.
 *
 * @snac.src server
 * @snac.cmd 0x13 0x06
 *
 * @see SsiDataCheck
 * @see SsiDataRequest
 */
public class SsiDataCmd extends SsiCommand {
    /** A default SSI data version; the version number used by WinAIM. */
    public static final int VERSION_DEFAULT = 0x00;

    /** The SSI version being used. */
    private final int version;
    /** The list of items. */
    private final SsiItem[] items;
    /** The last modification date of the SSI data. */
    private final long lastmod;

    /**
     * Generates a new SSI data command from the given incoming SNAC packet.
     *
     * @param packet an incoming SSI data packet
     */
    protected SsiDataCmd(SnacPacket packet) {
        super(CMD_SSI_DATA);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

        version = BinaryTools.getUByte(snacData, 0);

        int itemCount = BinaryTools.getUShort(snacData, 1);

        List<SsiItem> itemList = new LinkedList<SsiItem>();

        ByteBlock block = snacData.subBlock(3);

        for (int i = 0; i < itemCount; i++) {
            SsiItem item = SsiItem.readSsiItem(block);
            if (item == null) break;

            itemList.add(item);

            block = block.subBlock(item.getTotalSize());
        }

        items = itemList.toArray(new SsiItem[0]);

        lastmod = BinaryTools.getUInt(block, 0);
    }

    /**
     * Creates a new outgoing SSI data command with the given properties and
     * an SSI version of {@link #VERSION_DEFAULT}.
     *
     * @param items a list of the user's SSI items
     * @param lastmod the last modification date of the user's SSI data, in
     *        seconds since the unix epoch
     */
    public SsiDataCmd(SsiItem[] items, long lastmod) {
        this(VERSION_DEFAULT, items, lastmod);
    }

    /**
     * Creates a new outgoing SSI data command with the given properties.
     *
     * @param version the SSI version being used, normally {@link
     *        #VERSION_DEFAULT}
     * @param items a list of the user's SSI items
     * @param lastmod the last modification date of the user's SSI data, in
     *        seconds since the unix epoch, or <code>0</code> to indicate that
     *        this is <i>not</i> the last of a series of SSI data packets
     */
    public SsiDataCmd(int version, SsiItem[] items, long lastmod) {
        super(CMD_SSI_DATA);

        DefensiveTools.checkRange(version, "version", 0);
        DefensiveTools.checkNull(items, "items");
        DefensiveTools.checkRange(lastmod, "lastmod", 0);

        this.version = version;
        this.items = items.clone();
        this.lastmod = lastmod;
    }

    /**
     * Returns the SSI version being used. This is normally {@link
     * #VERSION_DEFAULT}.
     *
     * @return the SSI version being used
     */
    public final int getSsiVersion() {
        return version;
    }

    /**
     * Returns the user's SSI items, as sent in this command. Note that this
     * may not be <i>all</i> of the user's SSI items, as this command is
     * sometimes sent multiple times, spreading the user's SSI items over
     * multiple packets. If there are more SSI data commands to follow this
     * one, {@link #getLastModDate} will return <code>0</code>.
     *
     * @return the items in this user's server-stored information
     */
    public final SsiItem[] getItems() {
        return items.clone();
    }

    /**
     * Returns the last modification date of the user's SSI data, or
     * <code>0</code> if more SSI data packets are to follow this one.
     *
     * @return the last modification date of the user's SSI data, in seconds
     *         since the unix epoch
     */
    public final long getLastModDate() {
        return lastmod;
    }

    public void writeData(OutputStream out) throws IOException {
        BinaryTools.writeUByte(out, version);
        BinaryTools.writeUShort(out, items.length);
        for (int i = 0; i < items.length; i++) {
            items[i].write(out);
        }
        BinaryTools.writeUInt(out, lastmod);
    }

	@Override
    public String toString() {
        return "SsiDataCmd (ssi version=" + version + "): " + items.length
                + " items, modified " + new Date(lastmod * 1000);
    }
}
