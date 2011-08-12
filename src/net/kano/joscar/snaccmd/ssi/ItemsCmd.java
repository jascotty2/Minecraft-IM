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

import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.MiscTools;
import net.kano.joscar.flapcmd.SnacPacket;
import net.kano.joscar.ssiitem.AbstractItemObj;
import net.kano.joscar.ssiitem.SsiItemObj;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * A base class for the three item-based commands in this package. These
 * commands are {@link CreateItemsCmd}, {@link ModifyItemsCmd}, {@link
 * DeleteItemsCmd}.
 *
 * @see CreateItemsCmd
 * @see ModifyItemsCmd
 * @see DeleteItemsCmd
 */
public abstract class ItemsCmd extends SsiCommand {
    /** The items sent in this command. */
    private final SsiItem[] items;

    /**
     * Generates a new item-based command from the given incoming SNAC packet.
     *
     * @param command the SNAC command subtype for this command
     * @param packet an incoming item-based command packet
     */
    protected ItemsCmd(int command, SnacPacket packet) {
        super(command);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock block = packet.getData();

        List itemList = new LinkedList();

        for (;;) {
            SsiItem item = SsiItem.readSsiItem(block);
            if (item == null) break;

            itemList.add(item);

            block = block.subBlock(item.getTotalSize());
        }

        items = (SsiItem[]) itemList.toArray(new SsiItem[0]);
    }

    /**
     * Creates a new item-based command with a list of <code>SsiItem</code>s
     * {@linkplain AbstractItemObj#toSsiItem generated} from the given list
     * of item objects.
     *
     * @param command the SNAC command subtype of this command
     * @param itemObjs a list of SSI item objects to use in generating
     *        <code>SsiItem</code>s
     */
    protected ItemsCmd(int command, SsiItemObj[] itemObjs) {
        this(command, AbstractItemObj.generateSsiItems(itemObjs));
    }

    /**
     * Creates a new outgoing item-based command with the given list of items.
     *
     * @param command the SNAC command subtype for this command
     * @param items the list of items to send in this commnad
     */
    protected ItemsCmd(int command, SsiItem[] items) {
        super(command);

        DefensiveTools.checkNull(items, "items");

        this.items = (SsiItem[]) items.clone();
    }

    /**
     * Returns the list of server-stored "items" contained in this command.
     *
     * @return the list of "items" sent in this command
     */
    public final SsiItem[] getItems() {
        return (SsiItem[]) items.clone();
    }

    public void writeData(OutputStream out) throws IOException {
        for (int i = 0; i < items.length; i++) {
            items[i].write(out);
        }
    }

    public String toString() {
        return MiscTools.getClassName(this) + ": " + items.length + " items: "
                + Arrays.asList(items);
    }
}
