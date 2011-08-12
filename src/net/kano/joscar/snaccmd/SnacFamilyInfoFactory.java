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

package net.kano.joscar.snaccmd;

import net.kano.joscar.DefensiveTools;
import net.kano.joscar.snaccmd.acct.AcctCommand;
import net.kano.joscar.snaccmd.auth.AuthCommand;
import net.kano.joscar.snaccmd.buddy.BuddyCommand;
import net.kano.joscar.snaccmd.chat.ChatCommand;
import net.kano.joscar.snaccmd.conn.ConnCommand;
import net.kano.joscar.snaccmd.conn.SnacFamilyInfo;
import net.kano.joscar.snaccmd.icbm.IcbmCommand;
import net.kano.joscar.snaccmd.icon.IconCommand;
import net.kano.joscar.snaccmd.invite.InviteCommand;
import net.kano.joscar.snaccmd.loc.LocCommand;
import net.kano.joscar.snaccmd.popup.PopupCommand;
import net.kano.joscar.snaccmd.rooms.RoomCommand;
import net.kano.joscar.snaccmd.search.SearchCommand;
import net.kano.joscar.snaccmd.ssi.SsiCommand;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Provides a means of generating a list of <code>SnacFamilyInfo</code> values
 * that are compatible with the default implementations of
 * <code>SnacCommand</code>s in joscar. Note that this only provides a default
 * set of <code>SnacFamilyInfo</code>s and you are free to send whatever list
 * of <code>SnacFamilyInfo</code>s you please in {@link
 * net.kano.joscar.snaccmd.conn.ClientVersionsCmd}s and {@link
 * net.kano.joscar.snaccmd.conn.ClientReadyCmd}s; however, with different SNAC
 * family versions often comes changes to the protocol, changes that the default
 * <code>SnacCommand</code> implementations provided by joscar may not be able
 * to handle.
 */
public final class SnacFamilyInfoFactory {
    /**
     * As the only constructor, this guarantees an instance of this class can
     * never be created.
     */
    private SnacFamilyInfoFactory() { }

    /** A map of SNAC family codes to their version informations. */
    private static final Map families = new HashMap();

    /*
    From WinAIM 5.2 beta or so:
    BOS:
        0x0001, 0x0003, 0x0010, 0x0629
        0x0002, 0x0001, 0x0110, 0x0629
        0x0003, 0x0001, 0x0110, 0x0629
        0x0004, 0x0001, 0x0110, 0x0629
        0x0006, 0x0001, 0x0110, 0x0629
        0x0008, 0x0001, 0x0104, 0x0001
        0x0009, 0x0001, 0x0110, 0x0629
        0x000a, 0x0001, 0x0110, 0x0629
        0x000b, 0x0001, 0x0104, 0x0001
        0x0013, 0x0003, 0x0110, 0x0629

    Buddy Icon server:
        0x0010, 0x0001, 0x0010, 0x0629

    Buddy search server:
        0x000f, 0x0001, 0x0010, 0x0629
    */

    static {
        List infos = Arrays.asList(new SnacFamilyInfo[] {
            AuthCommand.FAMILY_INFO,
            ConnCommand.FAMILY_INFO,
            LocCommand.FAMILY_INFO,
            BuddyCommand.FAMILY_INFO,
            PopupCommand.FAMILY_INFO,
            AcctCommand.FAMILY_INFO,
            RoomCommand.FAMILY_INFO,
            ChatCommand.FAMILY_INFO,
            InviteCommand.FAMILY_INFO,
            SearchCommand.FAMILY_INFO,
            IconCommand.FAMILY_INFO,
            SsiCommand.FAMILY_INFO,
            IcbmCommand.FAMILY_INFO,
        });

        for (Iterator it = infos.iterator(); it.hasNext();) {
            SnacFamilyInfo sfi = (SnacFamilyInfo) it.next();
            families.put(new Integer(sfi.getFamily()), sfi);
        }
    }

    /**
     * Returns a list of SNAC family version information objects given a list
     * of supported families. See {@linkplain SnacFamilyInfoFactory above} for
     * details. For each SNAC family code in the given list of supported
     * families which is also supported by joscar, a single
     * <code>SnacFamilyInfo</code> is returned, in the order that the supported
     * family list was given.
     * <br>
     * <br>
     * A typical use of this method would be as follows:
     * <pre>
void handleServerReadyCmd(ServerReadyCmd serverReadyCmd) {
    int[] families = serverReadyCmd.getSnacFamilies();
    SnacFamilyInfo[] familyInfos = SnacFamilyInfoFactory.getDefaultFamilyInfos(
            families);
    ClientVersionsCmd out = new ClientVersionsCmd(familyInfos);
     // ... send command ...
}
     * </pre>
     *
     * @param supportedFamilies a list of supported families for which family
     *        information objects should be returned
     * @return a list of SNAC family info objects corresponding to supported
     *         SNAC families in the given list of families
     */
    public static SnacFamilyInfo[] getDefaultFamilyInfos(
            int[] supportedFamilies) {
        DefensiveTools.checkNull(supportedFamilies, "supportedFamilies");

        List list = new LinkedList();

        for (int i = 0; i < supportedFamilies.length; i++) {
            SnacFamilyInfo info = getFamily(supportedFamilies[i]);

            if (info != null) list.add(info);
        }

        return (SnacFamilyInfo[]) list.toArray(new SnacFamilyInfo[0]);
    }

    /**
     * Returns an appropriate <code>SnacFamilyInfo</code> for the given SNAC
     * family, or <code>null</code> if none exists.
     *
     * @param family the SNAC family whose family info is being queried
     * @return a <code>SnacFamilyInfo</code> for the given family
     */
    private static SnacFamilyInfo getFamily(int family) {
        return (SnacFamilyInfo) families.get(new Integer(family));
    }
}
