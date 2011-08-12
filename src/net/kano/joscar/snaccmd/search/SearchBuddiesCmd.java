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

import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.flapcmd.SnacPacket;
import net.kano.joscar.snaccmd.DirInfo;
import net.kano.joscar.tlv.ImmutableTlvChain;
import net.kano.joscar.tlv.Tlv;
import net.kano.joscar.tlv.TlvChain;
import net.kano.joscar.tlv.TlvTools;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A SNAC command used to search for buddies by directory information, email
 * address, or chat interests. Normally responded-to with a {@link
 * SearchResultsCmd}.
 *
 * @snac.src client
 * @snac.cmd 0x0f 0x02
 *
 * @see SearchResultsCmd
 */
public class SearchBuddiesCmd extends SearchCommand {
    /** A search type indicating a search based on directory information. */
    public static final int TYPE_BY_DIRINFO = 0x0000;
    /**
     * A search type indicating a search based on email address or a chat
     * interests.
     */
    public static final int TYPE_BY_EMAIL_OR_INTEREST = 0x0001;

    /** A TLV type containing the search type. */
    private static final int TYPE_SEARCH_TYPE = 0x000a;
    /** A TLV type containing an email address. */
    private static final int TYPE_EMAIL = 0x0005;
    /** A TLV type containing a chat interests. */
    private static final int TYPE_INTEREST = 0x000b;

    /** The type of search being performed. */
    private final int type;
    /** An email address. */
    private final String email;
    /** A chat interest. */
    private final String interest;
    /** A set of directory information to use in searching. */
    private final DirInfo dirInfo;

    /**
     * Generates a new buddy search command from the given incoming SNAC
     * packet.
     *
     * @param packet an incoming buddy search packet
     */
    protected SearchBuddiesCmd(SnacPacket packet) {
        super(CMD_SEARCH);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

        TlvChain chain = TlvTools.readChain(snacData);

        type = chain.getUShort(TYPE_SEARCH_TYPE);
        email = chain.getString(TYPE_EMAIL);
        interest = chain.getString(TYPE_INTEREST);
        dirInfo = DirInfo.readDirInfo(chain);
    }

    /**
     * Creates a new outgoing buddy search command for buddies with the given
     * email address.
     *
     * @param email the email address to search for
     * @return a new outgoing buddy search command for buddies with the given
     *         email address
     */
    public static SearchBuddiesCmd createSearchByEmailCmd(String email) {
        return new SearchBuddiesCmd(TYPE_BY_EMAIL_OR_INTEREST, email, null,
                null);
    }

    /**
     * Creates a new outgoing buddy search command for buddies with the given
     * chat interest.
     *
     * @param interest the name of the chat interest to search for, like
     *        "Travel"
     * @return a new outgoing buddy search command for buddies with the given
     *         chat interest
     */
    public static SearchBuddiesCmd createSearchByInterestCmd(String interest) {
        return new SearchBuddiesCmd(TYPE_BY_EMAIL_OR_INTEREST, null, interest,
                null);
    }

    /**
     * Creates a new outgoing buddy search command for buddies matching the
     * given (non-<code>null</code>) fields in the given block of directory
     * information. Note that as few as one or as many as all of the fields of
     * the given directory information block can be filled in; these will be
     * the only ones used in the search.
     *
     * @param dirInfo the directory information whose matches should be found
     * @return a new outgoing buddy search command for buddies matching the
     *         given set of directory information
     */
    public static SearchBuddiesCmd createSearchByDirInfoCmd(DirInfo dirInfo) {
        return new SearchBuddiesCmd(TYPE_BY_DIRINFO, null, null,
                dirInfo);
    }

    /**
     * Creates a new outgoing buddy search command with the given properties.
     * Note that, normally, only <i>one</i> of <code>email</code>,
     * <code>interest</code>, and <code>dirInfo</code> should be
     * <i>non-<code>null</code></i>, as AOL's servers only allow you to perform
     * a search based on one of the three fields. See {@link
     * #createSearchByEmailCmd}, {@link #createSearchByInterestCmd}, and {@link
     * #createSearchByDirInfoCmd} for more intuitive factory methods.
     *
     * @param type the type of search to perform, like {@link #TYPE_BY_DIRINFO}
     * @param email an email address to search for, or <code>null</code> for
     *        none
     * @param interest a chat interest to search for, or <code>null</code> for
     *        none
     * @param dirInfo a set of directory information to search for, or
     *        <code>null</code> for none
     */
    public SearchBuddiesCmd(int type, String email, String interest,
            DirInfo dirInfo) {
        super(CMD_SEARCH);

        DefensiveTools.checkRange(type, "type", 0);

        this.type = type;
        this.email = email;
        this.interest = interest;
        this.dirInfo = dirInfo;
    }

    /**
     * Returns the type of search being performed. Normally one of {@link
     * #TYPE_BY_DIRINFO} and {@link #TYPE_BY_EMAIL_OR_INTEREST}.
     *
     * @return the type of search being performed
     */
    public final int getSearchType() {
        return type;
    }

    /**
     * Returns the email address being searched for, if any.
     *
     * @return the email address being searched for
     */
    public final String getEmail() {
        return email;
    }

    /**
     * Returns the chat interest being searched for, if any.
     *
     * @return the chat interest being searched for
     */
    public final String getInterest() {
        return interest;
    }

    /**
     * Returns the set of directory information being searched for, if any.
     *
     * @return the set of directory information being searched for
     */
    public final DirInfo getDirInfo() {
        return dirInfo;
    }

    public void writeData(OutputStream out) throws IOException {
        Tlv.getUShortInstance(TYPE_SEARCH_TYPE, type).write(out);
        if (email != null) {
            Tlv.getStringInstance(TYPE_EMAIL, email).write(out);
        }
        if (interest != null) {
            Tlv.getStringInstance(TYPE_INTEREST, interest).write(out);
        }
        if (dirInfo != null) {
            dirInfo.write(out);
        }
    }

    public String toString() {
        return "SearchBuddiesCmd, type=" + type + ", email=" + email
                + ", interest=" + interest + ", dirinfo=<" + dirInfo + ">";
    }
}
