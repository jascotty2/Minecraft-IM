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
 *  File created by keith @ Feb 21, 2003
 *
 */

package net.kano.joscar.snaccmd.conn;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.Writable;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A data structure containing information about a specific SNAC family. SNAC
 * families and the formats of their commands can change over time, so the
 * client and server send "versions" of each family supported so that the server
 * can incorporate new commands and new formats of existing commands without
 * affecting clients which are not (yet) equipped to handle them.
 * <br>
 * <br>
 * All SNAC family information data provided in joscar simply mimics the
 * behavior of AOL's Instant Messenger client for Windows. As a developer you
 * are not expected (and certainly not required) to understand what the fields
 * of this class are for, or what they mean; for the most part, I don't know
 * what they signify either. Once again, they are just copied from WinAIM.
 */
public class SnacFamilyInfo implements Writable {
    /** The SNAC family code. */
    private final int family;
    /** The version of the SNAC family supported. */
    private final int version;
    /** The "tool ID" being used. */
    private final int toolID;
    /** The version of the tool being used. */
    private final int toolVersion;

    /**
     * Returns a SNAC family information block read from the given block of
     * data.
     *
     * @param block a block of data containing SNAC family information
     * @return a SNAC family information block read from the given block of data
     */
    protected static SnacFamilyInfo readSnacFamilyInfo(ByteBlock block) {
        DefensiveTools.checkNull(block, "block");

        int family = BinaryTools.getUShort(block, 0);
        int version = BinaryTools.getUShort(block, 2);
        int toolid = BinaryTools.getUShort(block, 4);
        int toolver = BinaryTools.getUShort(block, 6);

        return new SnacFamilyInfo(family, version, toolid, toolver);
    }

    /**
     * Creates a new SNAC family version information block with the given SNAC
     * family and family version. The tool ID and tool version number of this
     * object will be <code>-1</code>. Using this constructor is equivalent to
     * calling {@link #SnacFamilyInfo(int, int, int, int) new
     * SnacFamilyInfo(family, version, -1, -1)}.
     *
     * @param family the SNAC family
     * @param version the version of the SNAC family supported
     */
    public SnacFamilyInfo(int family, int version) {
        this(family, version, -1, -1);
    }

    /**
     * Creates a new SNAC family information block with the given properties.
     *
     * @param family the SNAC family code
     * @param version the version of the SNAC family supported
     * @param toolID a number representing the "tool" used
     * @param toolVersion the version of the "tool" being used
     */
    public SnacFamilyInfo(int family, int version, int toolID,
            int toolVersion) {
        DefensiveTools.checkRange(family, "family", 0);
        DefensiveTools.checkRange(version, "version", 0);
        DefensiveTools.checkRange(toolID, "toolID", -1);
        DefensiveTools.checkRange(toolVersion, "toolVersion", -1);

        this.family = family;
        this.version = version;
        this.toolID = toolID;
        this.toolVersion = toolVersion;
    }

    /**
     * Returns the code of the SNAC family that this object describes.
     *
     * @return the SNAC family
     */
    public final int getFamily() {
        return family;
    }

    /**
     * Returns the version of the associated SNAC family supported.
     *
     * @return the SNAC family version
     */
    public final int getVersion() {
        return version;
    }

    /**
     * Returns the "tool ID" being used for this SNAC family's operations.
     *
     * @return the SNAC family tool ID
     */
    public final int getToolID() {
        return toolID;
    }

    /**
     * Returns the version of the "tool" being used for this SNAC family's
     * operations.
     *
     * @return the SNAC family tool version
     */
    public final int getToolVersion() {
        return toolVersion;
    }

    public long getWritableLength() {
        return 8;
    }

    public void write(OutputStream out) throws IOException {
        BinaryTools.writeUShort(out, family);
        BinaryTools.writeUShort(out, version);
        if (toolID != -1 && toolVersion != -1) {
            BinaryTools.writeUShort(out, toolID);
            BinaryTools.writeUShort(out, toolVersion);
        }
    }

    public String toString() {
        return "SnacFamilyInfo: " +
                "family=0x" + Integer.toHexString(family) +
                ", version=0x" + Integer.toHexString(version) +
                ", toolID=0x" + Integer.toHexString(toolID) +
                ", toolVersion=0x" + Integer.toHexString(toolVersion);
    }
}
