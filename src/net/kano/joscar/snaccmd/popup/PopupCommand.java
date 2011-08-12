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
 *  File created by keith @ Feb 23, 2003
 *
 */

package net.kano.joscar.snaccmd.popup;

import net.kano.joscar.flapcmd.SnacCommand;
import net.kano.joscar.snaccmd.conn.SnacFamilyInfo;

/**
 * A base class for all 1 of the commands in the "popup" <code>0x08</code> SNAC
 * family.
 */
public abstract class PopupCommand extends SnacCommand {
    /** The SNAC family code for this family. */
    public static final int FAMILY_POPUP = 0x0008;

    /** A set of SNAC family information about this family. */
    public static final SnacFamilyInfo FAMILY_INFO
            = new SnacFamilyInfo(FAMILY_POPUP, 0x0001, 0x0104, 0x0001);

    /** A command subtype for the popup command. */
    public static final int CMD_POPUP_MSG = 0x0002;

    /**
     * Creates a new SNAC command in the popup family.
     *
     * @param command the SNAC command subtype for this command
     */
    protected PopupCommand(int command) {
        super(FAMILY_POPUP, command);
    }
}
