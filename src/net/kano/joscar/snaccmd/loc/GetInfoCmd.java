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
 *  File created by keith @ Aug 17, 2003
 *
 */

package net.kano.joscar.snaccmd.loc;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.OscarTools;
import net.kano.joscar.StringBlock;
import net.kano.joscar.flapcmd.SnacPacket;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A SNAC command used by newer clients to request information about a buddy.
 * This command is generally recommended over {@link OldGetInfoCmd} because it
 * is normally not as heavily rate limited and because it allows one to retrieve
 * a buddy's away message and user profile in a single command. This command is
 * also the only known way to retrieve a buddy's {@linkplain
 * net.kano.joscar.snaccmd.InfoData#getCertificateInfo() security information}.
 *
 * @snac.src client
 * @snac.cmd 0x02 0x15
 */
public class GetInfoCmd extends LocCommand {
    /**
     * A flag indicating that the user's user profile ("info") is being
     * requested.
     */
    public static final long FLAG_INFO = 0x00000001;
    /** A flag indicating that the user's away message is being requested. */
    public static final long FLAG_AWAYMSG = 0x00000002;
    /**
     * A flag indicating that the user's {@linkplain
     * net.kano.joscar.snaccmd.InfoData#getCertificateInfo() security
     * information} is being requested.
     */
    public static final long FLAG_CERT = 0x00000008;

    /**
     * A set of flags describing what type(s) of information are being
     * requested.
     */
    private final long flags;
    /** The screenname of the user whose information is being requested. */
    private final String sn;

    /**
     * Creates a new get-user-info command from the given incoming get-user-info
     * SNAC packet.
     *
     * @param packet an incoming get-user-info SNAC packet
     */
    protected GetInfoCmd(SnacPacket packet) {
        super(CMD_NEW_GET_INFO);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock data = packet.getData();

        flags = BinaryTools.getUInt(data, 0);

        ByteBlock snData = data.subBlock(4);
        StringBlock snBlock = OscarTools.readScreenname(snData);

        if (snBlock == null) {
            sn = null;
        } else {
            sn = snBlock.getString();
        }
    }

    /**
     * Creates a new outgoing get-user-info command with the given flags for the
     * given screenname. {@linkplain #FLAG_AWAYMSG Flags} can be combined using
     * bitwise operations, as follows:
     * <pre>
void getInfoAndAwayMessage(String sn) {
    send(new GetInfoCmd(GetInfoCmd.FLAG_AWAYMSG
           | GetInfoCmd.FLAG_INFO, sn));
}
void getAwayMessage(String sn) {
    send(new GetInfoCmd(GetInfoCmd.FLAG_AWAYMSG, sn));
}
void getSecurityInfo(String sn) {
    send(new GetInfoCmd(GetInfoCmd.FLAG_CERT, sn));
}
</pre>
     *
     * @param flags a set of bit flags, normally a bitwise combination of the
     *        {@link #FLAG_AWAYMSG FLAG_*} constants defined in this class
     * @param sn the screenname of the user whose information is being requested
     */
    public GetInfoCmd(long flags, String sn) {
        super(CMD_NEW_GET_INFO);

        DefensiveTools.checkRange(flags, "flags", -1);
        DefensiveTools.checkNull(sn, "sn");

        this.flags = flags;
        this.sn = sn;
    }

    /**
     * Returns the request type flags sent in this command. This will normally
     * be a bitwise combination of the {@link #FLAG_AWAYMSG FLAGS_*} constants
     * defined in this class. To check for the presence of a certain flag, one
     * could use code such as the following:
     * <pre>
if ((getInfoCmd.getFlags()
        & GetInfoCmd.FLAG_AWAYMSG) != 0) {
    System.out.println("user requested away message for "
            + getInfoCmd.getScreenname());
}
</pre>
     *
     * @return the request type flags sent in this command
     */
    public final long getFlags() { return flags; }

    /**
     * Returns the screenname of the user whose information is being requested.
     *
     * @return the screenname of the user whose information is being requested
     */
    public final String getScreenname() { return sn; }

    public void writeData(OutputStream out) throws IOException {
        if (flags != -1) {
            BinaryTools.writeUInt(out, flags);
            if (sn != null) OscarTools.writeScreenname(out, sn);
        }
    }

    public String toString() {
        return "GetInfoCmd: flags=" + flags + ", sn=" + sn;
    }
}
