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
 *  File created by keith @ Feb 28, 2003
 *
 */

package net.kano.joscar.snaccmd.search;

import net.kano.joscar.flapcmd.SnacCommand;
import net.kano.joscar.flapcmd.SnacPacket;
import net.kano.joscar.snac.CmdType;
import net.kano.joscar.snac.SnacCmdFactory;

/**
 * A SNAC command factory for the client-bound commands provided in this
 * package, appropriate for use by an AIM client.
 */
public class ClientSearchCmdFactory implements SnacCmdFactory {
    /** The SNAC command types supported by this factory. */
    private static final CmdType[] SUPPORTED_TYPES = new CmdType[] {
        new CmdType(SearchCommand.FAMILY_SEARCH, SearchCommand.CMD_INTERESTS),
        new CmdType(SearchCommand.FAMILY_SEARCH, SearchCommand.CMD_RESULTS),
    };

    public CmdType[] getSupportedTypes() {
        return (CmdType[]) SUPPORTED_TYPES.clone();
    }

    public SnacCommand genSnacCommand(SnacPacket packet) {
        if (packet.getFamily() != SearchCommand.FAMILY_SEARCH) return null;

        int command = packet.getCommand();

        if (command == SearchCommand.CMD_INTERESTS) {
            return new InterestListCmd(packet);
        } else if (command == SearchCommand.CMD_RESULTS) {
            return new SearchResultsCmd(packet);
        } else {
            return null;
        }
    }
}
