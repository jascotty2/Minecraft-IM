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

import net.kano.joscar.flapcmd.SnacCommand;
import net.kano.joscar.snaccmd.conn.SnacFamilyInfo;

/**
 * A base class for SNAC commands in the "buddy icon" <code>0x10</code> SNAC
 * family.
 */
public abstract class IconCommand extends SnacCommand {
    /** The SNAC family code for the icon family. */
    public static final int FAMILY_ICON = 0x0010;

    /** A set of SNAC family information for this family. */
    public static final SnacFamilyInfo FAMILY_INFO
            = new SnacFamilyInfo(FAMILY_ICON, 0x0001, 0x0010, 0x0739);

    /** A command subtype for requesting one's icon. */
    public static final int CMD_ICON_REQ = 0x0004;
    /** A command subtype for receiving a buddy icon. */
    public static final int CMD_ICON_DATA = 0x0005;
    /** A command subtype for uploading your buddy icon. */
    public static final int CMD_UPLOAD_ICON = 0x0002;
    /** A command subtype for an uploaded icon acknowledgement. */
    public static final int CMD_UPLOAD_ACK = 0x0003;

    /**
     * Creates a new SNAC command in the buddy icon family.
     *
     * @param command the SNAC command subtype
     */
    protected IconCommand(int command) {
        super(FAMILY_ICON, command);
    }
}
