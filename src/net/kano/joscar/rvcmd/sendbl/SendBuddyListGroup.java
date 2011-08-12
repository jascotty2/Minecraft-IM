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

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.LiveWritable;
import net.kano.joscar.StringBlock;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * A data structure used in transferring (parts of) one's buddy list to another
 * user.
 */
public class SendBuddyListGroup implements LiveWritable {
    /**
     * Generates an array of <code>SendBuddyListGroup</code>s from the given
     * block of binary data. Note that this method will never return
     * <code>null</code>; if no buddy list groups exist in the given block,
     * an empty array will be returned. Note that the total number of bytes
     * comprising the data represented in the returned array can be computed by
     * simply summing the {@linkplain #getTotalSize() total size} of each of the
     * elements in the returned array.
     *
     * @param block a block of binary data containing zero or more "Send Buddy
     *        List group" structures
     * @return a list of <code>SendBuddyListGroup</code>s representing the
     *         groups in the given block
     */
    public static SendBuddyListGroup[] readBuddyListGroups(ByteBlock block) {
        DefensiveTools.checkNull(block, "block");

        List groups = new LinkedList();
        for (;;) {
            SendBuddyListGroup group = readBuddyListGroup(block);

            if (group == null) break;

            groups.add(group);

            block = block.subBlock(group.getTotalSize());
        }

        return (SendBuddyListGroup[]) groups.toArray(new SendBuddyListGroup[0]);
    }

    /**
     * Reads a single "Send Buddy List group" structure from the given block of
     * binary data. Note that this method will return <code>null</code> if no
     * complete group structure is present in the given block.
     *
     * @param block a block of binary data containing a "Send Buddy List group"
     *        block
     * @return a <code>SendBuddyListGroup</code> read from the given block of
     *         data, or <code>null</code> if none could be read
     */
    public static SendBuddyListGroup readBuddyListGroup(ByteBlock block) {
        DefensiveTools.checkNull(block, "block");

        StringBlock groupName = readString(block);

        if (groupName == null
                || block.getLength() < groupName.getTotalSize() + 2) {
            return null;
        }

        int buddyCount = BinaryTools.getUShort(block, groupName.getTotalSize());

        String[] buddies = new String[buddyCount];
        ByteBlock rest = block.subBlock(groupName.getTotalSize() + 2);

        int size = groupName.getTotalSize() + 2;

        for (int i = 0; i < buddies.length; i++) {
            StringBlock buddyString = readString(rest);

            if (buddyString == null) return null;

            buddies[i] = buddyString.getString();

            rest = rest.subBlock(buddyString.getTotalSize());
            size += buddyString.getTotalSize();
        }

        return new SendBuddyListGroup(groupName.getString(), buddies, size);
    }

    /**
     * Reads an ASCII string preceded by a two-byte length value from the start
     * of the given block of data.
     *
     * @param block a block of data containing an ASCII string preceded by its
     *        length represented as a two-byte unsigned integer
     * @return a <code>StringBlock</code> whose length value is the total
     *         length of the structure (including the length value) and whose
     *         string value is the extracted ASCII string
     */
    private static final StringBlock readString(ByteBlock block) {
        DefensiveTools.checkNull(block, "block");

        if (block.getLength() < 2) return null;

        int len = BinaryTools.getUShort(block, 0);

        if (block.getLength() < len + 2) return null;

        String str = BinaryTools.getAsciiString(block.subBlock(2, len));

        return new StringBlock(str, 2 + len);
    }

    /**
     * Writes an ASCII string preceded by its length written as an unsigned
     * two-byte integer to the given stream.
     *
     * @param out the stream to which to write
     * @param str the ASCII string to write
     *
     * @throws IOException if an I/O error occurs
     */
    private static final void writeString(OutputStream out, String str)
            throws IOException {
        DefensiveTools.checkNull(out, "out");
        DefensiveTools.checkNull(str, "str");

        byte[] bytes = BinaryTools.getAsciiBytes(str);

        BinaryTools.writeUShort(out, bytes.length);
        out.write(bytes);
    }

    /** The name of the group represented. */
    private final String groupName;
    /** A list of the screennames of the buddies in this group. */
    private final String[] buddies;
    /**
     * The total size of this structure, as read from an incoming block of
     * binary data.
     */
    private final int totalSize;

    /**
     * Creates a new Send Buddy List group object with the given group name,
     * the given array of (non-<code>null</code>) screennames, and the given
     * total data structure size.
     *
     * @param groupName the name of the represented group
     * @param buddies a list of the screennames of the buddies in this group
     * @param totalSize the total size of this structure, as read from an
     *        incoming block of binary data, or <code>-1</code> for none
     */
    private SendBuddyListGroup(String groupName, String[] buddies,
            int totalSize) {
        DefensiveTools.checkNull(groupName, "groupName");
        DefensiveTools.checkNull(buddies, "buddies");
        DefensiveTools.checkRange(totalSize, "totalSize", -1);

        this.groupName = groupName;
        this.buddies = (String[]) buddies.clone();
        this.totalSize = totalSize;

        DefensiveTools.checkNullElements(this.buddies, "buddies");
    }

    /**
     * Creates a new Send Buddy List group object with the given group name and
     * the given array of (non-<code>null</code>) screennames.
     *
     * @param groupName the name of the represented group
     * @param buddies a list of the screennames of the buddies in this group
     */
    public SendBuddyListGroup(String groupName, String[] buddies) {
        this(groupName, buddies, -1);
    }

    /**
     * Returns the name of the represented group. This will be a string like
     * <code>"Buddies"</code>.
     *
     * @return the name of the represented group
     */
    public final String getGroupName() { return groupName; }

    /**
     * Returns a list of the screennames of the buddies in this group.
     *
     * @return a list of the screennames of the buddies in this group
     */
    public final String[] getBuddies() {
        return (String[]) buddies.clone();
    }

    /**
     * Returns the total size, in bytes, of this object, as read from an
     * incoming block of binary data. Note that this value will be
     * <code>-1</code> if this object was not read from a block of binary data
     * but was instead created manually.
     *
     * @return the total size, in bytes, of this object, or <code>-1</code> if
     *         this object was not read from an incoming block of data
     */
    public final int getTotalSize() { return totalSize; }

    public void write(OutputStream out) throws IOException {
        writeString(out, groupName);
        BinaryTools.writeUShort(out, buddies.length);
        for (int i = 0; i < buddies.length; i++) {
            writeString(out, buddies[i]);
        }
    }

    public String toString() {
        return "SendBuddyListGroup for group '" + groupName + "': "
                + Arrays.asList(buddies);
    }
}
