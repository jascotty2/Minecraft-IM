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
 *  File created by keith @ Feb 18, 2003
 *
 */

package net.kano.joscar.snaccmd.auth;

import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.MiscTools;
import net.kano.joscar.flapcmd.SnacPacket;
import net.kano.joscar.snaccmd.acct.AcctModCmd;
import net.kano.joscar.tlv.Tlv;
import net.kano.joscar.tlv.TlvChain;
import net.kano.joscar.tlv.TlvTools;

import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Pattern;

/**
 * A SNAC command sent in response to an {@link AuthRequest}. This is the last
 * step of the authorization process.
 * <br><br>
 * AIM's login process uses many error codes for the same thing. Each of the
 * error codes recognized by AIM 5.2 is present in this class as {@link
 * #ERROR_ACCOUNT_DELETED ERROR_*} constants. Codes with the same meaning are
 * given the same name, followed by a letter. To interpret these error codes
 * correctly, a client should handle each <i>ERROR_*_[A-Z]</i> code as the same
 * error code.
 *
 * @snac.src server
 * @snac.cmd 0x17 0x03
 *
 * @see AuthRequest
 */
public class AuthResponse extends AuthCommand {
    /**
     * An error code indicating that the screenname and/or password provided is
     * not valid.
     */
    public static final int ERROR_INVALID_SN_OR_PASS_A = 1;
    /**
     * See {@link #ERROR_INVALID_SN_OR_PASS_A} for a description of this code.
     */
    public static final int ERROR_INVALID_SN_OR_PASS_B = 4;
    /**
     * An error code indicating that the given password is wrong. AIM 5.2
     * suggests that in this case, the user delete his or her "stored password"
     * and re-enter it on the signon screen.
     */
    public static final int ERROR_BAD_PASSWORD = 5;
    /**
     * An error code indicating that the format of the <code>AuthRequest</code>
     * SNAC was invalid. AIM 5.2 says "internal error."
     */
    public static final int ERROR_BAD_INPUT = 6;
    /**
     * An error code indicating that the user's account has been deleted.
     */
    public static final int ERROR_ACCOUNT_DELETED = 8;
    /**
     * An error code indicating that the user's signing on has been blocked for
     * some reason.
     */
    public static final int ERROR_SIGNON_BLOCKED = 17;
    /** An error code indicating that AIM is currently unavailable. */
    public static final int ERROR_TEMP_UNAVAILABLE_A = 12;
    /** See {@link #ERROR_TEMP_UNAVAILABLE_A} for a description of this code. */
    public static final int ERROR_TEMP_UNAVAILABLE_B = 13;
    /** See {@link #ERROR_TEMP_UNAVAILABLE_A} for a description of this code. */
    public static final int ERROR_TEMP_UNAVAILABLE_C = 19;
    /** See {@link #ERROR_TEMP_UNAVAILABLE_A} for a description of this code. */
    public static final int ERROR_TEMP_UNAVAILABLE_D = 20;
    /** See {@link #ERROR_TEMP_UNAVAILABLE_A} for a description of this code. */
    public static final int ERROR_TEMP_UNAVAILABLE_E = 21;
    /** See {@link #ERROR_TEMP_UNAVAILABLE_A} for a description of this code. */
    public static final int ERROR_TEMP_UNAVAILABLE_F = 26;
    /** See {@link #ERROR_TEMP_UNAVAILABLE_A} for a description of this code. */
    public static final int ERROR_TEMP_UNAVAILABLE_G = 31;
    /**
     * An error code indicating that the user has been reconnecting too
     * frequently, and should try again.
     */
    public static final int ERROR_CONNECTING_TOO_MUCH_A = 24;
    /**
     * See {@link #ERROR_CONNECTING_TOO_MUCH_A} for a description of this code.
     */
    public static final int ERROR_CONNECTING_TOO_MUCH_B = 28;
    /**
     * An error code indicating that the client software is too old to connect
     * to AIM anymore.
     */
    public static final int ERROR_CLIENT_TOO_OLD = 27;
    /**
     * An error code that does not show any dialog in AIM 5.2. Instead, it
     * brings the user back to the signon screen without warning.
     */
    public static final int ERROR_SOMETHING_FUNNY = 28;
    /**
     * An error code indicating that the user entered an invalid SecurID. This
     * code normally only applies to AIM administrators/moderators.
     */
    public static final int ERROR_INVALID_SECURID = 32;
    /**
     * An error code indicating that the user is under 13 years of age, and thus
     * cannot use AIM.
     */
    public static final int ERROR_UNDER_13 = 34;

    /** A TLV type containing the user's screenname. */
    private static final int TYPE_SN = 0x0001;
    /** A TLV type containing the server and port to connect to for BOS. */
    private static final int TYPE_SERVER = 0x0005;
    /**
     * A TLV type containing the cookie to provide to the BOS server upon
     * connecting.
     */
    private static final int TYPE_COOKIE = 0x0006;
    /** A TLV type containing the user's registered email address. */
    private static final int TYPE_EMAIL = 0x0011;
    /** A TLV type containing an error code. */
    private static final int TYPE_ERRCODE = 0x0008;
    /** A TLV type containing a URL that explains the error code, if sent. */
    private static final int TYPE_ERRURL = 0x0004;
    /** A TLV type containing a "registration status' code. */
    private static final int TYPE_REGSTATUS = 0x0013;

    /** The user's screenname. */
    private final String sn;
    /** The BOS server to connect to next. */
    private final String server;
    /** The port of the BOS server on which to connect. */
    private final int port;
    /** The cookie to provide to the BOS server upon connecting. */
    private final ByteBlock cookie;
    /** The user's "registration status" code. */
    private final int regstatus;

    /** The user's registered email address. */
    private final String email;

    /** An error code. */
    private final int errorCode;
    /** A URL providing more information on the error code, if sent. */
    private final String errorUrl;

    /**
     * Generates an authorization response command from the given incoming SNAC
     * packet.
     *
     * @param packet the authorization response SNAC packet
     */
    protected AuthResponse(SnacPacket packet) {
        super(CMD_AUTH_RESP);

        DefensiveTools.checkNull(packet, "packet");

        TlvChain chain = TlvTools.readChain(packet.getData());

        sn = chain.getString(TYPE_SN);

        // this is in the format "129.3.20.1:5190"
        String serverport = chain.getString(TYPE_SERVER);
        String tserver = null;
        int tport = -1;
        if (serverport != null && serverport.indexOf(':') != -1) {
            serverport = serverport.trim();
            int colon = serverport.indexOf(':');

            String portString = serverport.substring(colon + 1);
            try {
                tport = Integer.parseInt(portString);
                tserver = serverport.substring(0, colon);
            } catch (NumberFormatException ignored) { }
        }

        server = tserver;
        port = tport;

        cookie = chain.hasTlv(TYPE_COOKIE)
                ? chain.getLastTlv(TYPE_COOKIE).getData() : null;

        email = chain.getString(TYPE_EMAIL);

        regstatus = chain.getUShort(TYPE_REGSTATUS);

        errorCode = chain.getUShort(TYPE_ERRCODE);
        errorUrl = chain.getString(TYPE_ERRURL);
    }

    /**
     * Creates an outgoing authorization response command with the given
     * properties and no error code or error URL.
     *
     * @param sn the user's screen name
     * @param server the BOS server to which the user should connect
     * @param port the port on which the user should connect
     * @param cookie a login cookie that the user should provide to the BOS
     *        server upon connecting
     * @param regStatus the user's registration status code
     * @param email the user's registered email address
     */
    public AuthResponse(String sn, String server, int port, ByteBlock cookie,
            int regStatus, String email) {
        this(sn, server, port, cookie, regStatus, email, -1, null);
    }

    /**
     * Creates an outgoing authorization response command with the given error
     * code and URL and no other properties.
     *
     * @param errorCode an error code, like {@link #ERROR_ACCOUNT_DELETED}
     * @param errorUrl a URL explaining the given error code, or
     *        <code>null</code> for none
     */
    public AuthResponse(int errorCode, String errorUrl) {
        this(null, null, -1, null, -1, null, errorCode, errorUrl);
    }

    /**
     * Creates an outgoing authorization response command with the given
     * properties.
     *
     * @param sn the user's screen name, or <code>null</code> for none
     * @param server the BOS server to which the user should connect, or
     *        <code>null</code> for none
     * @param port the port on which the user should connect, or <code>-1</code>
     *        for none
     * @param cookie a login cookie that the user should provide to the BOS
     *        server upon connecting, or <code>null</code> for none
     * @param regStatus the user's registration status code, or <code>-1</code>
     *        for none
     * @param email the user's registered email address, or <code>null</code>
     *        for none
     * @param errorCode an error code, or <code>-1</code> for none
     * @param errorUrl a URL explaining the given error code, or
     *        <code>null</code> for none
     */
    public AuthResponse(String sn, String server, int port, ByteBlock cookie,
            int regStatus, String email, int errorCode, String errorUrl) {
        super(CMD_AUTH_RESP);

        DefensiveTools.checkRange(port, "port", -1);
        DefensiveTools.checkRange(regStatus, "regStatus", -1);
        DefensiveTools.checkRange(errorCode, "errorCode", -1);

        this.sn = sn;
        this.server = server;
        this.port = port;
        this.cookie = cookie;
        this.regstatus = regStatus;
        this.email = email;
        this.errorCode = errorCode;
        this.errorUrl = errorUrl;
    }

    /**
     * Returns the screenname contained in this authorization response, or
     * <code>null</code> if none was sent.
     *
     * @return the user's screenname
     */
    public final String getScreenname() {
        return sn;
    }

    /**
     * Returns the server to which the user should connect next for "basic
     * online service," or <code>null</code> if none was sent.
     *
     * @return the BOS server to connect to
     */
    public final String getServer() {
        return server;
    }

    /**
     * Returns the port on which the user should connect to the given
     * {@linkplain #getServer BOS server}, or <code>-1</code> if none was sent.
     *
     * @return the port of the BOS server to connect to
     */
    public final int getPort() {
        return port;
    }

    /**
     * Returns the login cookie that should be {@linkplain
     * net.kano.joscar.flapcmd.LoginFlapCmd#LoginFlapCmd(long, ByteBlock)
     * provided} to the given {@linkplain #getServer BOS server} upon
     * connecting.
     *
     * @return the login cookie to send to the given BOS server
     */
    public final ByteBlock getCookie() {
        return cookie;
    }

    /**
     * Returns the user's "registration status visibility code," or
     * <code>-1</code> if none was sent. This will normally be one of the
     * {@link net.kano.joscar.snaccmd.acct.AcctModCmd#REGSTATUS_NONE
     * REGSTATUS_*} constants defined in {@link net.kano.joscar.snaccmd.acct.AcctModCmd}.
     *
     * @return the user's registration status visibility code
     */
    public final int getRegstatus() { return regstatus; }

    /**
     * Returns the user's registered email address, or <code>null</code> if none
     * was sent.
     *
     * @return the user's registered email address
     */
    public final String getEmail() {
        return email;
    }

    /**
     * Returns the error code sent in this command, or <code>-1</code> if none
     * was sent.
     *
     * @return this command's error code
     */
    public final int getErrorCode() {
        return errorCode;
    }

    /**
     * Returns the "error URL" associated with the {@linkplain #getErrorCode
     * given error code}, or <code>null</code> if none was sent. Often this URL
     * will be http://www.aol.com, but other times it's a URL that explains
     * an error code in detail.
     *
     * @return this command's "error URL"
     */
    public final String getErrorUrl() {
        return errorUrl;
    }

    public void writeData(OutputStream out) throws IOException {
        if (sn != null) {
            Tlv.getStringInstance(TYPE_SN, sn).write(out);
        }
        if (server != null) {
            String host = server;
            if (port != -1) host = host + ":" + port;

            Tlv.getStringInstance(TYPE_SERVER, host).write(out);
        }
        if (cookie != null) {
            new Tlv(TYPE_COOKIE, cookie).write(out);
        }
        if (errorCode != -1) {
            Tlv.getUShortInstance(TYPE_ERRCODE, errorCode).write(out);
        }
        if (errorUrl != null) {
            Tlv.getStringInstance(TYPE_ERRURL, errorUrl).write(out);
        }
        if (email != null) {
            Tlv.getStringInstance(TYPE_EMAIL, email).write(out);
        }
        if (regstatus != -1) {
            Tlv.getUShortInstance(TYPE_REGSTATUS, regstatus).write(out);
        }
    }

    public String toString() {
        String errorName = null;
        if (errorCode != -1) {
            errorName = MiscTools.findIntField(AuthResponse.class, errorCode,
                    "ERROR_.*");
        }
        String regName = null;
        if (regstatus != -1) {
            regName = MiscTools.findIntField(AcctModCmd.class, regstatus,
                    "REGSTATUS_.*");
        }

        return "AuthResponse: " +
                "sn='" + sn + "'" +
                ", server='" + server + "'" +
                ", port=" + port +
                (regstatus == -1 ? "" : ", regStatus=0x"
                + Integer.toHexString(regstatus)
                + " (" + (regName == null ? "unknown status code" : regName)
                + ")") +
                ", email='" + email + "'" +
                (errorCode == -1 ? "" : ", errorCode=0x"
                + Integer.toHexString(errorCode)
                + " (" + (errorName == null ? "unknown error code" : errorName)
                + ")") +
                (errorUrl == null ? "" : ", errorURL='" + errorUrl + "'");
    }
}
