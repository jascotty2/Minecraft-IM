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
 *  File created by Keith @ 12:05:56 AM
 *
 */

package net.kano.joscar.rvcmd.addins;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.StringBlock;
import net.kano.joscar.rvcmd.AbstractRequestRvCmd;
import net.kano.joscar.rvcmd.InvitationMessage;
import net.kano.joscar.snaccmd.CapabilityBlock;
import net.kano.joscar.snaccmd.icbm.RecvRvIcbm;
import net.kano.joscar.tlv.Tlv;
import net.kano.joscar.tlv.TlvChain;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A rendezvous command used to invite a user to use a specified add-in. For
 * example, AOL Instant Messenger for Windows uses this command invite a user to
 * play MS Hearts and Quake II.
 * <br>
 * <br>
 * An add-in invitation contains several fields, including a URI that describes
 * the add-in (in case the recipient does not have that add-in), a sixteen-byte
 * "UUID" block supposedly unique to that add-in type, an add-in name, and the
 * sender's Windows network name (like <code>\\NETWORKNAME</code>).
 * <br>
 * <br>
 * Note: for information on the format of the URI, see the {@linkplain
 * #getAddinUri <code>getAddinUri</code> documentation}.
 */
public class AddinsReqRvCmd extends AbstractRequestRvCmd {
    /*
    00 00 01 00 05 07 4c 7f 11 d1 82 22 44 45 53 54 00 00 00 09 00 09 4d 53 48
    65 61 72 74 73 00 4c 45 41 4b 2d 54 33 30 00 00 00 00 00

    "UUID" is 07050001-7F4C-D111-8222-444553540000
    */

    /** The "code" value that is always sent by WinAIM. */
    public static final int CODE_DEFAULT = 0;
    /** The "flags" value that is always sent by WinAIM. */
    public static final long FLAGS_DEFAULT = 0;

    /** A TLV type containing the add-in URI. */
    private static final int TYPE_ADDINURI = 0x0007;

    /** Some sort of code sent in an addins reqest. */
    private final int code;
    /**
     * A URI string describing the add-in to which the recipient is being
     * invited.
     */
    private final String addinUri;
    /** A "UUID" block supposedly unique to the requested add-in type. */
    private final ByteBlock uuid;
    /** The name of this add-in. */
    private final String addinName;
    /** The inviter's Windows network computer name. */
    private final String computerName;
    /** A set of flags sent in the addins reqest command. */
    private final long flags;
    /** An invitation message block sent in this invitation. */
    private final InvitationMessage invMessage;

    /**
     * Creates a new addins invitation command from the given incoming addins
     * request RV ICBM.
     *
     * @param icbm an incoming add-in request rendezvous ICBM command
     */
    public AddinsReqRvCmd(RecvRvIcbm icbm) {
        super(icbm);

        TlvChain chain = getRvTlvs();

        addinUri = chain.getString(TYPE_ADDINURI);

        ByteBlock rvData = getServiceData();

        int code = -1;
        ByteBlock uuid = null;
        String addinName = null;
        String computerName = null;
        long flags = -1;

        if (rvData != null && rvData.getLength() >= 2) {
            code = BinaryTools.getUShort(rvData, 0);

            if (rvData.getLength() > 10) {
                // copy the UUID over, since we think it might be used a lot...
                uuid = ByteBlock.wrap(rvData.subBlock(2, 16).toByteArray());

                if (rvData.getLength() >= 22) {
                    int addinNameLen = BinaryTools.getUShort(rvData, 18);
                    int compNameLen = BinaryTools.getUShort(rvData, 20);

                    if (rvData.getLength() >= 22 + addinNameLen + compNameLen) {
                        StringBlock addinNameBlock = BinaryTools.getNullPadded(
                                rvData.subBlock(22));
                        addinName = addinNameBlock.getString();

                        StringBlock compNameBlock
                                = BinaryTools.getNullPadded(rvData.subBlock(
                                        22 + addinNameBlock.getTotalSize()+1));
                        computerName = compNameBlock.getString();

                        flags = BinaryTools.getUInt(rvData,
                                22 + addinNameBlock.getTotalSize()+1
                                + compNameBlock.getTotalSize()+1);
                    }
                }
            }
        }

        this.code = code;
        this.uuid = uuid;
        this.addinName = addinName;
        this.computerName = computerName;
        this.flags = flags;

        invMessage = InvitationMessage.readInvitationMessage(chain);
    }

    /**
     * Creates a new outgoing addins invitation request RV command with the
     * given properties. Note that all values can be either <code>null</code> or
     * <code>-1</code> (depending on the argument's type, obviously) to indicate
     * that that field sholdn't be sent in this invitation command.
     *
     * @param code a "code" for this invitation; normally {@link #CODE_DEFAULT}
     * @param addinUri a URI describing the type of add-in to which the
     *        recipient is being invited (see {@link #getAddinUri} for details)
     * @param uuid a sixteen-byte block of data unique to the associated add-in
     *        type
     * @param addinName the name of the associated add-in, like "MSHearts" or
     *        "QuakeII"
     * @param computerName the Windows network name of the sender, like
     *        <code>COMPUTERNAME</code> in the network filename
     *        <code>\\COMPUTERNAME\FILE.HTML"</code>
     * @param flags a set of flags to send; normally {@link #FLAGS_DEFAULT}
     * @param invMessage a message to send with this invitation
     */
    public AddinsReqRvCmd(int code, String addinUri,
            ByteBlock uuid, String addinName, String computerName, long flags,
            InvitationMessage invMessage) {
        super(CapabilityBlock.BLOCK_ADDINS);

        DefensiveTools.checkRange(code, "code", -1);
        DefensiveTools.checkRange(flags, "flags", -1);

        if (uuid != null && uuid.getLength() != 16) {
            throw new IllegalArgumentException("uuid length ("
                    + uuid.getLength() + ") must be 16 if uuid is non-null");
        }

        this.code = code;
        this.addinUri = addinUri;
        this.uuid = uuid;
        this.addinName = addinName;
        this.computerName = computerName;
        this.flags = flags;
        this.invMessage = invMessage;
    }

    /**
     * Returns a "code" sent in this command. As of this writing, this value is
     * always {@link #CODE_DEFAULT} (<code>0</code>) and its significance is
     * unknown.
     *
     * @return a mysterious "code" send in this command, or <code>-1</code> if
     *         none was sent
     */
    public final int getCode() { return code; }

    /**
     * Returns a URI that describes the associated add-in type, or
     * <code>null</code> if none was sent.
     * <br>
     * <br>
     * Below is a typical URI sent by WinAIM:
     * <pre>
     * aim:AddGame?name=MSHearts&go1st=true&multiplayer=true&url=http://ww
     * w.microsoft.com&cmd=%25m&servercmd=%20&hint=Both%20machines%20must%
     * 20be%20running%20Win95%3CBR%3Eor%20Win98%20and%20be%20on%20the%20sa
     * me%20local%20network.%3CBR%3EWhen%20you%20send%20invite,%20choose%2
     * 0'I%20want%20to%20be%3CBR%3Edealer'%20and%20click%20on%20OK%20*befo
     * re*%20buddy%3CBR%3Eresponds%20(so%20act%20fast).%20*After*%20buddy%
     * 3CBR%3Eresponds,%20press%20F2%20to%20start%20game.
     * </pre>
     *
     * After decoding with the Java API's <code>URLDecoder</code> and then
     * parsing a bit more this becomes:
     * <br>
     * <table>
     * <tr><th>Name</th><th>Value</th></tr>
     * <tr><td><code>name</td></code><td><code>MSHearts</td></code></tr>
     * <tr><td><code>go1st</td></code><td><code>true</td></code></tr>
     * <tr><td><code>multiplayer</td></code><td><code>true</td></code></tr>
     * <tr><td><code>url</td></code><td><code>http://www.microsoft.com</td></code></tr>
     * <tr><td><code>cmd</td></code><td><code>%m</td></code></tr>
     * <tr><td><code>servercmd</td></code><td><code><i>[a single space]</i></td></code></tr>
     * <tr><td><code>hint</td></code><td><code>
     * Both machines must be running Win95&lt;BR&gt;<br>
     * or Win98 and be on the same local network.&lt;BR&gt;<br>
     * When you send invite, choose 'I want to be&lt;BR&gt;<br>
     * dealer' and click on OK *before* buddy&lt;BR&gt;<br>
     * responds (so act fast). *After* buddy&lt;BR&gt;<br>
     * responds, press F2 to start game.
     * </td></code></tr>
     * </table>
     *
     * @return an "addin URI" that describes the associated add-in, or
     *         <code>null</code> if none was sent
     */
    public final String getAddinUri() { return addinUri; }

    /**
     * Returns the add-in unique ID ("UUID") block associated with the
     * associated addin type, or <code>null</code> if none was sent.
     *
     * @return the "UUID" of the associated add-in type
     */
    public final ByteBlock getAddinUuid() { return uuid; }

    /**
     * Returns the name of the associated add-in type. This will be a string
     * like <code>"MSHearts"</code> and <code>"QuakeII"</code> that may be used
     * to uniquely identify an add-in.
     *
     * @return the name of the associated add-in
     */
    public final String getAddinName() { return addinName; }

    /**
     * Returns the Windows network name of the sender. This will be a name like
     * <code>COMPUTERNAME</code> in the network pathname
     * <code>\\COMPUTERNAME\FILE.HTML</code>.
     *
     * @return the sender's Windows network name
     */
    public final String getComputerName() { return computerName; }

    /**
     * Returns a set of "flags" sent in this add-ins invitation. As of this
     * writing, WinAIM always sends {@link #FLAGS_DEFAULT} (<code>0</code>).
     *
     * @return the set of bit flags sent in this command
     */
    public final long getFlags() { return flags; }

    protected void writeRvTlvs(OutputStream out) throws IOException {
        if (addinUri != null) {
            Tlv.getStringInstance(TYPE_ADDINURI, addinUri).write(out);
        }
        if (invMessage != null) {
            invMessage.write(out);
        }
    }

    protected boolean hasServiceData() {
        return true;
    }

    protected void writeServiceData(OutputStream out) throws IOException {
        if (code != -1) {
            BinaryTools.writeUShort(out, code);

            if (uuid != null) {
                uuid.write(out);

                if (addinName != null && computerName != null) {
                    byte[] addinNameBytes
                            = BinaryTools.getAsciiBytes(addinName);
                    byte[] compNameBytes
                            = BinaryTools.getAsciiBytes(computerName);

                    BinaryTools.writeUShort(out, addinNameBytes.length);
                    BinaryTools.writeUShort(out, compNameBytes.length);

                    out.write(addinNameBytes);
                    out.write(0);

                    out.write(compNameBytes);
                    out.write(0);

                    if (flags != -1) {
                        BinaryTools.writeUInt(out, flags);
                    }
                }
            }
        }
    }

    public String toString() {
        return "AddinsReqRvCmd: code=" + code + ", addinName=" + addinName
                + ", computerName=" + computerName + ", flags=0x"
                + Long.toHexString(flags) + ", message=<" + invMessage
                + ">, uri=" + addinUri;
    }
}