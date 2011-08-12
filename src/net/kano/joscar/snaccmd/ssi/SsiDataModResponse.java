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
 *  File created by keith @ Mar 3, 2003
 *
 */

package net.kano.joscar.snaccmd.ssi;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.flapcmd.SnacPacket;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A SNAC command used to acknowledge the modification of the user's
 * server-stored data. Normally sent in response to {@link CreateItemsCmd},
 * {@link ModifyItemsCmd}, and {@link DeleteItemsCmd}.
 *
 * @snac.src server
 * @snac.cmd 0x13 0x0e
 *
 * @see CreateItemsCmd
 * @see ModifyItemsCmd
 * @see DeleteItemsCmd
 */
public class SsiDataModResponse extends SsiCommand {
    /**
     * A result code indicating that the requested change was made successfully.
     */
    public static final int RESULT_SUCCESS = 0x0000;
    /**
     * A result code indicating that one or more of the items requested to be
     * modified or deleted does not exist and thus cannot be modified or
     * deleted.
      */
    public static final int RESULT_NO_SUCH_ITEM = 0x0002;
    /**
     * A result code indicating that the client attempted to create a second
     * {@linkplain net.kano.joscar.ssiitem.RootItem group list}.
     */
    public static final int RESULT_CANT_ADD_ANOTHER_ROOT_GROUP = 0x0003;
    /**
     * A result code indicating that one or more of the items requested to be
     * created cannot be because an item with the same ID already exists.
     */
    public static final int RESULT_ID_TAKEN = 0x000a;
    /**
     * A result code indicating that one or more of the requested items cannot
     * be created because the {@linkplain SsiRightsCmd maximum number of items}
     * of that type has been reached.
     */
    public static final int RESULT_MAX_ITEMS = 0x000c;
    /**
     * A result code indicating that ICQ users cannot be added to an AIM buddy
     * list.
     */
    public static final int RESULT_NO_ICQ = 0x000d;

    /** The result code. */
    private final int result;

    /**
     * Generates a new SSI data modification response command from the
     * given incoming SNAC packet.
     *
     * @param packet an incoming SSI data modification response packet
     */
    protected SsiDataModResponse(SnacPacket packet) {
        super(CMD_MOD_ACK);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

        result = BinaryTools.getUShort(snacData, 0);
    }

    /**
     * Creates a new outgoing SSI modification response with the given result
     * code.
     *
     * @param result a result code, like {@link #RESULT_SUCCESS}
     */
    public SsiDataModResponse(int result) {
        super(CMD_MOD_ACK);

        DefensiveTools.checkRange(result, "result", 0);

        this.result = result;
    }

    /**
     * Returns the result code associated with this SSI modification response.
     * Normally one of the {@linkplain #RESULT_SUCCESS
     * <code>RESULT_<i>*</i></code> constants}.
     *
     * @return the result code sent in this SSI modification response
     */
    public final int getResult() {
        return result;
    }

    public void writeData(OutputStream out) throws IOException {
        BinaryTools.writeUShort(out, result);
    }

    public String toString() {
        return "SsiDataModAck: result=0x" + Integer.toHexString(result);
    }
}