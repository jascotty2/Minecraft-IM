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

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.flapcmd.SnacPacket;
import net.kano.joscar.snaccmd.DirInfo;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A SNAC command containing a list of search results. Normally sent in response
 * to a {@link SearchBuddiesCmd}.
 *
 * @snac.src server
 * @snac.cmd 0x0f 0x03
 *
 * @see SearchBuddiesCmd
 */
public class SearchResultsCmd extends SearchCommand {
    /** The only result code I've ever seen. */
    public static final int CODE_DEFAULT = 0x0005;
    /** The only result subcode I've ever seen. */
    public static final int SUBCODE_DEFAULT = 0x0000;

    /** Some sort of result code. */
    private final int code;
    /** Some sort of result subcode. */
    private final int subCode;
    /** A list of results. */
    private final DirInfo[] results;

    /**
     * Generates a new search result list command from the given incoming SNAC
     * packet.
     *
     * @param packet an incoming search result list packet
     */
    protected SearchResultsCmd(SnacPacket packet) {
        super(CMD_RESULTS);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

        code = BinaryTools.getUShort(snacData, 0);
        subCode = BinaryTools.getUShort(snacData, 2);

        if (snacData.getLength() >= 6) {
            int resultCount = BinaryTools.getUShort(snacData, 4);
            List resultList = new ArrayList();

            ByteBlock block = snacData.subBlock(6);
            for (int i = 0; i < resultCount; i++) {
                int tlvCount = BinaryTools.getUShort(block, 0);

                ByteBlock dirBlock = block.subBlock(2);

                DirInfo dirInfo = DirInfo.readDirInfo(dirBlock, tlvCount);
                if (dirInfo == null) break;

                resultList.add(dirInfo);

                block = block.subBlock(2 + dirInfo.getTotalSize());
            }

            results = (DirInfo[]) resultList.toArray(new DirInfo[0]);
        } else {
            results = null;
        }
    }

    /**
     * Creates a new outgoing search results command with the given list of
     * results. The code and subcode are set to {@link #CODE_DEFAULT} and
     * {@link #SUBCODE_DEFAULT}, respectively. Using this constructor is
     * equivalent to using {@link #SearchResultsCmd(int, int, DirInfo[]) new
     * SearchResultsCmd(SearchResultsCmd.CODE_DEFAULT,
     * SearchResultsCmd.SUBCODE_DEFAULT, results)}.
     *
     * @param results the list of reuslts to send in this command
     */
    public SearchResultsCmd(DirInfo[] results) {
        this(CODE_DEFAULT, SUBCODE_DEFAULT, results);
    }

    /**
     * Creates a new outgoing search results command with the given list of
     * results and the given code and subcode.
     *
     * @param code a result code, normally {@link #CODE_DEFAULT}
     * @param subCode a result subcode, normally {@link #SUBCODE_DEFAULT}
     * @param results a list of results, or <code>null</code> for none
     */
    public SearchResultsCmd(int code, int subCode, DirInfo[] results) {
        super(CMD_RESULTS);

        DefensiveTools.checkRange(code, "code", 0);
        DefensiveTools.checkRange(subCode, "subCode", 0);

        this.code = code;
        this.subCode = subCode;
        this.results = (DirInfo[]) (results == null ? null : results.clone());
    }

    /**
     * Returns the result code sent in this command. Normally {@link
     * #CODE_DEFAULT}.
     *
     * @return the result code associated with these search results
     */
    public final int getResultCode() {
        return code;
    }

    /**
     * Returns the result "subcode" sent in this command. Normally {@link
     * #SUBCODE_DEFAULT}.
     *
     * @return the result "subcode" associated with these search results
     */
    public final int getResultSubCode() {
        return subCode;
    }

    /**
     * Returns the list of results sent in this command, or <code>null</code>
     * if none were sent.
     *
     * @return the search results
     */
    public DirInfo[] getResults() {
        return (DirInfo[]) (results == null ? null : results.clone());
    }

    public void writeData(OutputStream out) throws IOException {
        BinaryTools.writeUShort(out, code);
        BinaryTools.writeUShort(out, subCode);
        if (results != null) {
            BinaryTools.writeUShort(out, results.length);

            for (int i = 0; i < results.length; i++) {
                BinaryTools.writeUShort(out, results[i].getTlvCount());
                results[i].write(out);
            }
        }
    }

    public String toString() {
        return "SearchResultsCmd: " + results.length + " results";
    }
}
