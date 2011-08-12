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

/**
 * A SNAC command sent to request the user's server-stored data if and only if
 * it was modified past a certain date. Normally responded-to with either a
 * {@link SsiDataCmd} or a {@link SsiUnchangedCmd}. The purpose of this command
 * in addition to {@link SsiDataRequest} is to avoid downloading the same
 * buddy list at the start of every connection, which both wastes bandwidth and
 * increases the net time it takes to sign on, especially for those with large
 * buddy lists. This command should only be used if the client saves the SSI
 * data to disk or some other non-temporary medium; otherwise one should use
 * {@link SsiDataRequest}.
 *
 * @snac.src client
 * @snac.cmd 0x13 0x05
 *
 * @see SsiDataCmd
 * @see SsiUnchangedCmd
 */
public class SsiDataCheck extends SsiCommand {
    /**
     * The last modification date of the client's version of the SSI data, in
     * seconds since the unix epoch.
     */
    private final long lastmod;
    /** The number of SSI items in the client's version of the SSI data. */
    private final int itemCount;

    /**
     * Generates a new SSI data check command from the given incoming SNAC
     * packet.
     *
     * @param packet an incoming SSI data check packet
     */
    protected SsiDataCheck(SnacPacket packet) {
        super(CMD_DATA_CHECK);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

        lastmod = BinaryTools.getUInt(snacData, 0);
        itemCount = BinaryTools.getUShort(snacData, 4);
    }

    /**
     * Creates a new outgoing SSI data check command with the given last
     * modification date of the client's version of the SSI data and the given
     * number of SSI items currently present in the client's SSI data.
     * <br>
     * <br>
     * For those unfamiliar with unixtime, a fine way to use this command,
     * if the SSI data were stored locally (which is totally optional in itself)
     * in a file called "ssi.dat" (which is a filename I personally wouldn't
     * use outside of this example :):
<pre>
// create a File object for the SSI data file
File ssiDataFile = new File("ssi.dat");

// lastModifed returns milliseconds since unix epoch, so we can just divide by
// 1000 to get seconds
long secondsSinceUnixEpoch = ssiDataFile.lastModified() / 1000;

// count the items. I assume you'll have your own system for determining this
// sort of thing.
int itemCount = someCommandYouMadeToCountTheItems(ssiDataFile);

// now send the command with some send command you made
send(new SsiDataCheck(secondsSinceUnixEpoch, itemCount));
</pre>
     *
     * @param lastmod the last modification date of the client's version of the
     *        SSI data, in seconds since the unix epoch
     * @param itemCount the number of SSI "items" in the local version of the
     *        SSI data
     */
    public SsiDataCheck(long lastmod, int itemCount) {
        super(CMD_DATA_CHECK);

        DefensiveTools.checkRange(lastmod, "lastmod", -1);
        DefensiveTools.checkRange(itemCount, "itemCount", -1);

        this.lastmod = lastmod;
        this.itemCount = itemCount;
    }

    /**
     * Returns the last-modification date of the client's version of the SSI
     * data, in seconds since the unix epoch.
     *
     * @return the last-modification date of the client's version of the SSI
     *         data
     */
    public final long getLastModDate() {
        return lastmod;
    }

    /**
     * Returns the number of SSI "items" contained in the client's version of
     * the SSI data.
     *
     * @return the number of SSI "items" contained in the client's copy of the
     *         SSI data
     */
    public final int getItemCount() {
        return itemCount;
    }

    public void writeData(OutputStream out) throws IOException {
        if (lastmod != -1) {
            BinaryTools.writeUInt(out, lastmod);
            if (itemCount != -1) BinaryTools.writeUShort(out, itemCount);
        }
    }

    public String toString() {
        return "SsiDataCheck: lastmod=" + lastmod + ", " + itemCount + " items";
    }
}
