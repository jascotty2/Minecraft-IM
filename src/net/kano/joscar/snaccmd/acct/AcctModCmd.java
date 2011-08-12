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
 *  File created by keith @ Feb 24, 2003
 *
 */

package net.kano.joscar.snaccmd.acct;

import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.flapcmd.SnacPacket;
import net.kano.joscar.tlv.ImmutableTlvChain;
import net.kano.joscar.tlv.Tlv;
import net.kano.joscar.tlv.TlvChain;
import net.kano.joscar.tlv.TlvTools;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A SNAC command used to modify the formatting of one's screenname or to change
 * one's registered email address.
 *
 * @snac.src client
 * @snac.cmd 0x07 0x04
 *
 * @see AcctModAck
 */
public class AcctModCmd extends AcctCommand {
    /**
     * A registration status visibility code indicating that users who know
     * one's email address can find out "Nothing about me."
     */
    public static final int REGSTATUS_NONE = 0x0001;
    /**
     * A registration status visibility code indicating that users who know
     * one's email address can find out "Only that I have an account."
     */
    public static final int REGSTATUS_PARTIAL = 0x0002;
    /**
     * A registration status visibility code indicating that users who know
     * one's email address can find out "My screenname."
     */
    public static final int REGSTATUS_FULL = 0x0003;

    /** A command type indicating that the screen name is being reformatted. */
    private static final int TYPE_SN = 0x0001;
    /**
     * A command type indicating that the registered email address is being
     * changed.
     */
    private static final int TYPE_EMAIL = 0x0011;
    /** A TLV type containing a registration status visibility code. */
    private static final int TYPE_REGSTATUS = 0x0013;

    /** The new screenname. */
    private final String sn;
    /** The new email address. */
    private final String email;
    /** A registration visibility status code. */
    private final int regstatus;

    /**
     * Generates an account modification command object from the given incoming
     * SNAC packet.
     *
     * @param packet the account modification command packet
     */
    protected AcctModCmd(SnacPacket packet) {
        super(CMD_ACCT_MOD);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

        TlvChain chain = TlvTools.readChain(snacData);

        sn = chain.getString(TYPE_SN);
        email = chain.getString(TYPE_EMAIL);
        regstatus = chain.getUShort(TYPE_REGSTATUS);
    }

    /**
     * Creates an outgoing account modification command that sets the user's
     * registration visibility status to the given value.
     *
     * @param regstatus the new registration visibility code, like {@link
     *        #REGSTATUS_PARTIAL}
     */
    public AcctModCmd(int regstatus) {
        this(null, null, regstatus);
    }

    /**
     * Creates an outgoing account modification command that sets the user's
     * screenname format and/or email address to the given values. Note that
     * either of the parameters can be <code>null</code>, indicating that that
     * value should not be modified.
     *
     * @param sn the new screenname format
     * @param email the new email address
     */
    public AcctModCmd(String sn, String email) {
        this(sn, email, -1);
    }

    /**
     * Creates an outgoing account modification command that sets the given
     * screenname and/or registered email address. Note that any of
     * <code>sn</code>, <code>email</code>, and <code>regstatus</code> can be
     * <code>null</code> or <code>-1</code> to indicate that the given field
     * should not be modified.
     *
     * @param sn a newly formatted screenname, or <code>null</code> to indicate
     *        that this field should not be modified
     * @param email a new registered email address for this screenname, or
     *        <code>null</code> to indicate that this field should not be
     *        modified
     * @param regstatus a new registration visibility status code, like {@link
     *        #REGSTATUS_FULL}, or <code>-1</code> to indicate that this field
              should not be modified
     */
    public AcctModCmd(String sn, String email, int regstatus) {
        super(CMD_ACCT_MOD);

        DefensiveTools.checkRange(regstatus, "regstatus", -1);

        this.sn = sn;
        this.email = email;
        this.regstatus = regstatus;
    }

    /**
     * Returns the new screen name format requested in this command, or
     * <code>null</code> if that field was not sent.
     *
     * @return this command's requested screen name format field
     */
    public final String getScreenname() { return sn; }

    /**
     * Returns the new registered email address requested by this command, or
     * <code>null</code> if that field was not sent.
     *
     * @return this command's requested new registered email address field
     */
    public final String getEmail() { return email; }

    /**
     * Returns the registration visibility status code stored in this account
     * modification command, or <code>-1</code> if this value is not to be
     * modified.
     *
     * @return the registration visibility status code stored in this command
     */
    public final int getRegVisStatus() { return regstatus; }

    public void writeData(OutputStream out) throws IOException {
        if (sn != null) {
            Tlv.getStringInstance(TYPE_SN, sn).write(out);
        }
        if (email != null) {
            Tlv.getStringInstance(TYPE_EMAIL, email).write(out);
        }
        if (regstatus != -1) {
            Tlv.getUShortInstance(TYPE_REGSTATUS, regstatus).write(out);
        }
    }

    public String toString() {
        return "AccountModCmd: sn=" + sn + ", email=" + email
                + ", regstatus=" + regstatus;
    }
}
