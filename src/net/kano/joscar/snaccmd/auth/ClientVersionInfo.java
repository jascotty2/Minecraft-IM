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
 *  File created by keith @ Apr 7, 2003
 *
 */

package net.kano.joscar.snaccmd.auth;

import net.kano.joscar.DefensiveTools;
import net.kano.joscar.LiveWritable;
import net.kano.joscar.tlv.Tlv;
import net.kano.joscar.tlv.TlvChain;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A data structure used to transmit information about the client software used
 * to connect to OSCAR. This consists of a "client string" and a set of numbers
 * used in specifying the exact version of the sofware. Note that currently AOL
 * seems to block client versions other than its own official clients.
 */
public final class ClientVersionInfo implements LiveWritable {
    /** A TLV containing the user's client version string. */
    private static final int TYPE_VERSION_STRING = 0x0003;
    /** A TLV type containing the user's client's major version number. */
    private static final int TYPE_MAJOR = 0x0017;
    /** A TLV type containing the user's client's minor version number. */
    private static final int TYPE_MINOR = 0x0018;
    /** A TLV type containing the user's client's "point" version number. */
    private static final int TYPE_POINT = 0x0019;
    /** A TLV type containing the user's client's build number. */
    private static final int TYPE_BUILD = 0x001a;
    /** A TLV type containing the user's client's "distribution code." */
    private static final int TYPE_DISTCODE = 0x0014;

    /** A version string describing the client. */
    private final String versionString;
    /** The client's major version. */
    private final int major;
    /** The client's minor version. */
    private final int minor;
    /** The client's point version. */
    private final int point;
    /** The client's build number. */
    private final int build;
    /** The client's "distribution code." */
    private final int distCode;

    /**
     * Reads a client version information block from the given TLV chain.
     *
     * @param chain a TLV chain containing client version TLV's
     * @return a client version information object read from the given TLV
     *         chain
     */
    static ClientVersionInfo readClientVersionInfo(TlvChain chain) {
        DefensiveTools.checkNull(chain, "chain");

        String verString = chain.getString(TYPE_VERSION_STRING);
        int major = chain.getUShort(TYPE_MAJOR);
        int minor = chain.getUShort(TYPE_MINOR);
        int point = chain.getUShort(TYPE_POINT);
        int build = chain.getUShort(TYPE_BUILD);
        int distCode = chain.getUShort(TYPE_DISTCODE);

        return new ClientVersionInfo(verString, major, minor, point, build,
                distCode);
    }

    /**
     * Creates a new client version information object with the given
     * properties.
     *
     * @param versionString a "client version string," like <code>"AOL Instant
     *        Messenger, version 5.1.3036/WIN32"</code>
     * @param major a "major version," like <code>5</code> in the above example
     * @param minor a "minor version," like <code>1</code> in the above example
     * @param point a "point version"; WinAIM 5.1 sends <code>0</code>
     * @param build a "build number," <code>3036</code> in the above example
     * @param distCode a "distribution code," whose meaning is unknown at the
     *        time of this writing
     */
    public ClientVersionInfo(String versionString, int major, int minor,
            int point, int build, int distCode) {
        DefensiveTools.checkRange(major, "major", -1);
        DefensiveTools.checkRange(minor, "minor", -1);
        DefensiveTools.checkRange(point, "point", -1);
        DefensiveTools.checkRange(build, "build", -1);
        DefensiveTools.checkRange(distCode, "distCode", -1);

        this.versionString = versionString;
        this.major = major;
        this.minor = minor;
        this.point = point;
        this.build = build;
        this.distCode = distCode;
    }

    /**
     * Returns the "client version string." This will be <code>null</code> if
     * this value was not sent.
     *
     * @return the client's client version string
     */
    public String getVersionString() { return versionString; }

    /**
     * Returns the client's "major version." This is generally the first number
     * in a dotted version number, like <code>5</code> in
     * <code>"5.1.3036"</code>. This will be <code>-1</code> if this value was
     * not sent.
     *
     * @return the client's "major version" number
     */
    public int getMajor() { return major; }

    /**
     * Returns the client's "minor version." This is generally the second number
     * in a dotted version number, like <code>1</code> in
     * <code>"5.1.3036"</code>. This will be <code>-1</code> if this value was
     * not sent.
     *
     * @return the client's "major version" number
     */
    public int getMinor() { return minor; }

    /**
     * Returns the client's "point version." In WinAIM this is always zero; it
     * will be <code>-1</code> if this value was not sent.
     *
     * @return the client's "point version" number
     */
    public int getPoint() { return point; }

    /**
     * Returns the client's build number. This will be <code>-1</code> if this
     * value was not sent.
     *
     * @return the client's build number
     */
    public int getBuild() { return build; }

    /**
     * Returns the client's "distribution code." As of this writing, the
     * significance of the distribution code is unknown.
     *
     * @return the client's "distribution code"
     */
    public int getDistCode() { return distCode; }

    public void write(OutputStream out) throws IOException {
        if (versionString != null) {
            Tlv.getStringInstance(TYPE_VERSION_STRING, versionString).write(out);
        }
        if (major != -1) Tlv.getUShortInstance(TYPE_MAJOR, major).write(out);
        if (minor != -1) Tlv.getUShortInstance(TYPE_MINOR, minor).write(out);
        if (point != -1) Tlv.getUShortInstance(TYPE_POINT, point).write(out);
        if (build != -1) Tlv.getUShortInstance(TYPE_BUILD, build).write(out);
    }

    public String toString() {
        return "ClientVersionInfo: " +
                ", versionString='" + versionString + "'" +
                ", major=" + major +
                ", minor=" + minor +
                ", point=" + point +
                ", build=" + build +
                ", distCode=" + distCode;
    }
}
