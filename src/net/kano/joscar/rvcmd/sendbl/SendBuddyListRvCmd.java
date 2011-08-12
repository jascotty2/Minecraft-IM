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
 *  File created by keith @ Apr 27, 2003
 *
 */

package net.kano.joscar.rvcmd.sendbl;

import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.rvcmd.AbstractRequestRvCmd;
import net.kano.joscar.snaccmd.CapabilityBlock;
import net.kano.joscar.snaccmd.icbm.RecvRvIcbm;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * A rendezvous command used in sending portions of one's buddy list to another
 * user.
 * <br>
 * <br>
 * <b>Important note for implementing Send Buddy List:</b><br>
 * The official Windows AIM client (WinAIM, as I call it) <i>always</i> sends a
 * {@link net.kano.joscar.snaccmd.icbm.RvResponse} with a code of {@link
 * net.kano.joscar.snaccmd.icbm.RvResponse#CODE_NOT_ACCEPTING} in response to a
 * Send Buddy List command (this class). If this <code>RvResponse</code> is not
 * sent in response to an incoming <code>SendBuddyListRvCmd</code> and the
 * sender is using WinAIM, he or she will not be able to send any more Send
 * Buddy List commands to your client until he or she restarts AIM. I do not
 * know why this happens, but this behavior should be duplicated for maximum
 * compatibility with other users.
 */
public class SendBuddyListRvCmd extends AbstractRequestRvCmd {
    /** The buddy groups being sent. */
    private final SendBuddyListGroup[] groups;

    /**
     * Creates a new Send Buddy List command from the given incoming Send Buddy
     * List RV ICBM.
     *
     * @param icbm an incoming Send Buddy List RV ICBM
     */
    public SendBuddyListRvCmd(RecvRvIcbm icbm) {
        super(icbm);

        ByteBlock serviceData = getServiceData();

        if (serviceData == null) groups = null;
        else groups = SendBuddyListGroup.readBuddyListGroups(serviceData);
    }

    /**
     * Creates a new Send Buddy List command with the given list of buddy
     * groups.
     *
     * @param groups the list of buddy groups
     */
    public SendBuddyListRvCmd(SendBuddyListGroup[] groups) {
        super(CapabilityBlock.BLOCK_SENDBUDDYLIST);

        DefensiveTools.checkNull(groups, "groups");

        this.groups = (SendBuddyListGroup[]) groups.clone();

        DefensiveTools.checkNullElements(this.groups, "groups");
    }

    /**
     * Returns a list of the buddy groups contained in this command. Note that
     * this method will never return <code>null</code>; if no groups were sent,
     * the returned array will simply be empty.
     *
     * @return a list of the buddy groups sent in this command
     */
    public final SendBuddyListGroup[] getGroups() {
        return (SendBuddyListGroup[]) groups.clone();
    }

    protected void writeServiceData(OutputStream out) throws IOException {
        for (int i = 0; i < groups.length; i++) {
            groups[i].write(out);
        }
    }

    protected void writeRvTlvs(OutputStream out) throws IOException { }

    public String toString() {
        return "SendBuddyListRvCmd: " + Arrays.asList(groups);
    }
}
