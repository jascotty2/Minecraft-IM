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
import net.kano.joscar.LiveWritable;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A data structure containing information about a "chat interest." A quick
 * example of a list of interest information follows (note that these are not
 * actual chat topics on AOL's AIM servers; I just made them up for an example).
 * <br>
 * <br>
 * Consider the given list of interests sent in an {@link InterestListCmd}.
 * <table border="1" cellspacing="0">
 * <tr> <th>Type</th> <th>Parent ID</th> <th>Name</th> </tr>
 * <tr> <td><code>TYPE_CHILD</code></td> <td> 34 </td> <td>Cats</td> </tr>
 * <tr> <td><code>TYPE_CHILD</code></td> <td> 34 </td> <td>Fish</td> </tr>
 * <tr> <td><code>TYPE_PARENT</code></td> <td> 34 </td> <td>Pets</td> </tr>
 * <tr> <td><code>TYPE_PARENT</code></td> <td> 7 </td> <td>Food</td> </tr>
 * <tr> <td><code>TYPE_CHILD</code></td> <td> 34 </td> <td>Birds</td> </tr>
 * <tr> <td><code>TYPE_CHILD</code></td> <td> 0 </td> <td>General Chat</td> </tr>
 * <tr> <td><code>TYPE_CHILD</code></td> <td> 7 </td> <td>Pizza</td> </tr>
 * <tr> <td><code>TYPE_CHILD</code></td> <td> 0 </td> <td>AIM Help</td> </tr>
 * </table>
 * <br>
 * <br>
 * The preceding list of chat interest information blocks should be rendered
 * to the user as such:
 *
 * <ul>
 * <li> Pets
 * <ul>
 * <li> Cats </li>
 * <li> Fish </li>
 * <li> Birds </li>
 * </ul>
 * </li>
 * <li> Food
 * <ul>
 * <li> Pizza </li>
 * </ul>
 * </li>
 * <li> General Chat </li>
 * <li> AIM Help </li>
 * </ul>
 *
 * Note that even though "Pets" is a "parent" interest, it is still a valid
 * chat interest (meaning one can list "Pets" as one of his or her chat
 * interests just like with, say, "Cats").
 * <br>
 * <br>
 * Also note that, by design, the sent "tree" of interests can be only one level
 * deep; interests cannot be children of other children (for instance, it is
 * impossible to have a "Pepperoni" interest as a sub-interest of "Pizza" in the
 * above example because "Pizza" is already a sub-interest).
 */
public class InterestInfo implements LiveWritable {
    /** An interest type indicating that the interest has sub-interests. */
    public static final int TYPE_PARENT = 0x01;
    /** An interest type indicating that the interest has no sub-interests. */
    public static final int TYPE_CHILD = 0x02;

    /**
     * Generates a new chat interest information block from the given block of
     * binary data. Will return <code>null</code> if no valid interest
     * information block can be read.
     *
     * @param block a block of binary data containing a chat interest
     *        information block
     * @return an interest information block read from the given block of binary
     *         data, or <code>null</code> if none could be read
     */
    protected static InterestInfo readInterestInfo(ByteBlock block) {
        DefensiveTools.checkNull(block, "block");

        if (block.getLength() < 4) return null;

        int type = BinaryTools.getUByte(block, 0);
        int parent = BinaryTools.getUByte(block, 1);

        int namelen = BinaryTools.getUShort(block, 2);
        if (block.getLength() < namelen + 4) return null;

        ByteBlock nameBlock = block.subBlock(4, namelen);
        String name = BinaryTools.getAsciiString(nameBlock);

        int size = 4 + namelen;

        return new InterestInfo(type, parent, name, size);
    }

    /** Whether this interest is a "parent" or a "child." */
    private final int type;
    /** This interest's "parent" interest. */
    private final int parent;
    /** The name of this interest. */
    private final String name;
    /** The total size of this object, as read from a block of binary data. */
    private final int totalSize;

    /**
     * Creates a new chat interest information block with the given properties.
     *
     * @param type the type of chat interest, like {@link #TYPE_PARENT}
     * @param parent the parent ID of this chat interest: the ID of this
     *        interest if the type is {@link #TYPE_PARENT}, or the ID of this
     *        interest's parent if the type is {@link #TYPE_CHILD}
     * @param name the name of this interest, like <code>"Travel"</code>
     * @param totalSize the total size of this object, as read from a block of
     *        binary data
     */
    protected InterestInfo(int type, int parent, String name, int totalSize) {
        DefensiveTools.checkRange(type, "type", 0);
        DefensiveTools.checkRange(parent, "parent", 0);
        DefensiveTools.checkNull(name, "name");
        DefensiveTools.checkRange(totalSize, "totalSize", -1);

        this.type = type;
        this.parent = parent;
        this.name = name;
        this.totalSize = totalSize;
    }

    /**
     * Creates a new chat interest information block with the given properties.
     *
     * @param type the type of chat interest, like {@link #TYPE_PARENT}
     * @param parent the parent ID of this chat interest: the ID of this
     *        interest if the type is {@link #TYPE_PARENT}, or the ID of this
     *        interest's parent if the type is {@link #TYPE_CHILD}
     * @param name the name of this interest, like <code>"Travel"</code>
     */
    public InterestInfo(int type, int parent, String name) {
        this(type, parent, name, -1);
    }

    /**
     * Returns the "interest type" of this chat interest. Normally either
     * {@link #TYPE_CHILD} or {@link #TYPE_PARENT}. Note that this value
     * dictates the meaning of the {@linkplain #getParentId parent ID}.
     *
     * @return the type of this chat interest, like {@link #TYPE_CHILD}
     */
    public final int getType() {
        return type;
    }

    /**
     * Returns the "parent ID" of this chat interest. Normally, if the
     * {@linkplain #getType type} is {@link #TYPE_PARENT}, this value represents
     * a unique ID for this chat interest. If the type is {@link #TYPE_CHILD},
     * this value represents the unique ID of a "parent" interest under which
     * this interest should be listed.
     *
     * @return this chat interest's "parent ID"
     */
    public final int getParentId() {
        return parent;
    }

    /**
     * Returns the name of this chat interest, like "Travel."
     *
     * @return this chat interest's name
     */
    public final String getName() {
        return name;
    }

    /**
     * Returns the total size of this chat interest information block, as read
     * from a block of binary data. Note that this will be <code>-1</code> if
     * this block was not created from an incoming block of data.
     *
     * @return the total size, in bytes, of this chat interest block, as read
     *         from an incoming data block
     */
    public int getTotalSize() {
        return totalSize;
    }

    public void write(OutputStream out) throws IOException {
        BinaryTools.writeUByte(out, type);
        BinaryTools.writeUByte(out, parent);

        byte[] namebytes = BinaryTools.getAsciiBytes(name);
        BinaryTools.writeUShort(out, namebytes.length);
        out.write(namebytes);
    }

    public String toString() {
        return "InterestInfo: type=" + type + ", parent=" + parent + ": "
                + name;
    }
}
