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
 *  File created by keith @ Mar 6, 2003
 *
 */
package net.kano.joscar.snaccmd;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.Writable;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Represents a single "capability" that a client may have. Such capabilities
 * include being able to invited to chat rooms, hold Direct IM sessions, and
 * receive files. Capabilities are represented as unique sixteen-byte blocks
 * that are sent to the server upon connecting; these blocks are visible to
 * anyone who can view your user info, and clients can use them to determine
 * which operations are possible (such as whether the user can send a file to
 * a given other user).
 * <br>
 * <br>
 * A standard set of these blocks is used and recognized by WinAIM, gaim, and
 * iChat, among other OSCAR-based AIM clients; these blocks are defined as
 * constants in this class. Other blocks can be easily created and advertised
 * in order to, say, create your own inter-client feature such as encryption
 * (which Trillian does with its {@linkplain #BLOCK_TRILLIANCRYPT "Trillian
 * encryption" block}).
 */
public final class CapabilityBlock implements Writable {
    /**
     * A capability block used to indicate that a client is able to receive
     * invitations to chat rooms. Note that this capability need not be
     * advertised in order to create or join rooms or to invite others to
     * chat rooms.
     */
    public static final CapabilityBlock BLOCK_CHAT =
            new CapabilityBlock(new byte[]{
                0x74, (byte) 0x8f, 0x24, 0x20, 0x62, (byte) 0x87, 0x11,
                (byte) 0xd1, (byte) 0x82, 0x22, 0x44, 0x45, 0x53, 0x54,
                0x00, 0x00});

    /**
     * A capability block used to indicate that a client is able to receive
     * voice chat invitations.
     */
    public static final CapabilityBlock BLOCK_VOICE =
            new CapabilityBlock(new byte[]{
                0x09, 0x46, 0x13, 0x41, 0x4c, 0x7f, 0x11, (byte) 0xd1,
                (byte) 0x82, 0x22, 0x44, 0x45, 0x53, 0x54, 0x00, 0x00});

    /**
     * A capability block used to indicate that a client can be sent files.
     */
    public static final CapabilityBlock BLOCK_FILE_SEND =
            new CapabilityBlock(new byte[]{
                0x09, 0x46, 0x13, 0x43, 0x4c, 0x7f, 0x11, (byte) 0xd1,
                (byte) 0x82, 0x22, 0x44, 0x45, 0x53, 0x54, 0x00, 0x00});

    /**
     * A capability block used to indicate that a client can receive a Direct
     * IM invitation.
     */
    public static final CapabilityBlock BLOCK_DIRECTIM =
            new CapabilityBlock(new byte[]{
                0x09, 0x46, 0x13, 0x45, 0x4c, 0x7f, 0x11, (byte) 0xd1,
                (byte) 0x82, 0x22, 0x44, 0x45, 0x53, 0x54, 0x00, 0x00});

    /**
     * A capability block used to indicate that a client can receive buddy
     * icons. Note that this system, however, is becoming obsolete with the
     * recent inception of the {@linkplain net.kano.joscar.snaccmd.icon icon
     * service}.
     */
    public static final CapabilityBlock BLOCK_ICON =
            new CapabilityBlock(new byte[]{
                0x09, 0x46, 0x13, 0x46, 0x4c, 0x7f, 0x11, (byte) 0xd1,
                (byte) 0x82, 0x22, 0x44, 0x45, 0x53, 0x54, 0x00, 0x00});

    /**
     * A capability block used to indicate that a client can receive requests
     * to list files he or she is sharing.
     */
    public static final CapabilityBlock BLOCK_FILE_GET =
            new CapabilityBlock(new byte[]{
                0x09, 0x46, 0x13, 0x48, 0x4c, 0x7f, 0x11, (byte) 0xd1,
                (byte) 0x82, 0x22, 0x44, 0x45, 0x53, 0x54, 0x00, 0x00});

    /**
     * The first of two blocks used to indicate that a client can receive
     * invitations to play games. This block and {@link #BLOCK_GAMES2} are
     * treated as if they are the same block by WinAIM and other clients.
     */
    public static final CapabilityBlock BLOCK_GAMES =
            new CapabilityBlock(new byte[]{
                0x09, 0x46, 0x13, 0x4a, 0x4c, 0x7f, 0x11, (byte) 0xd1,
                (byte) 0x82, 0x22, 0x44, 0x45, 0x53, 0x54, 0x00, 0x00});
    /**
     * The second of two blocks used to indicate that a client can receive
     * invitations to play games. This block and {@link #BLOCK_GAMES} are
     * treated as if they are the same block by WinAIM and other clients.
     */
    public static final CapabilityBlock BLOCK_GAMES2 =
            new CapabilityBlock(new byte[]{
                0x09, 0x46, 0x13, 0x4a, 0x4c, 0x7f, 0x11, (byte) 0xd1,
                0x22, (byte) 0x82, 0x44, 0x45, 0x53, 0x54, 0x00, 0x00});

    /**
     * A capability block used to indicate that a client can receive a
     * copy of another user's buddy list.
     */
    public static final CapabilityBlock BLOCK_SENDBUDDYLIST =
            new CapabilityBlock(new byte[]{
                0x09, 0x46, 0x13, 0x4b, 0x4c, 0x7f, 0x11, (byte) 0xd1,
                (byte) 0x82, 0x22, 0x44, 0x45, 0x53, 0x54, 0x00, 0x00});

    /**
     * A capability block used to indicate that a client can chat using
     * "Trillian encryption."
     */
    public static final CapabilityBlock BLOCK_TRILLIANCRYPT =
            new CapabilityBlock(new byte[]{
                (byte) 0xf2, (byte) 0xe7, (byte) 0xc7, (byte) 0xf4,
                (byte) 0xfe, (byte) 0xad, 0x4d, (byte) 0xfb,
                (byte) 0xb2, 0x35, 0x36, 0x79, (byte) 0x8b,
                (byte) 0xdf, 0x00, 0x00});

    /**
     * A capability block that indicates that a client can chat with ICQ users.
     */
    public static final CapabilityBlock BLOCK_ICQCOMPATIBLE =
            new CapabilityBlock(new byte[] {
                0x09, 0x46, 0x13, 0x4d, 0x4c, 0x7f, 0x11, (byte) 0xd1,
                (byte) 0x82, 0x22, 0x44, 0x45, 0x53, 0x54, 0x00, 0x00});

    /**
     * A capability block indicating that a client allows for using "add-ins"
     * like MS Hearts, NetMeeting, and Quake II with other buddies.
     */
    public static final CapabilityBlock BLOCK_ADDINS =
            new CapabilityBlock(new byte[] {
                0x09, 0x46, 0x13, 0x47, 0x4c, 0x7f, 0x11, (byte) 0xd1,
                (byte) 0x82,0x22, 0x44, 0x45, 0x53, 0x54, 0x00, 0x00});

    /**
     * A capability block whose purpose is unknown at the time of this writing.
     */
    public static final CapabilityBlock BLOCK_SOMETHING =
            new CapabilityBlock(new byte[] {
                0x09, 0x46, 0x01, (byte) 0xff, 0x4c, 0x7f, 0x11, (byte) 0xd1,
                (byte) 0x82, 0x22, 0x44, 0x45, 0x53, 0x54, 0x00, 0x00});

    /**
     * A capability block indicating that the client is capable of parsing
     * "short capability blocks." See {@link ShortCapabilityBlock} as well as
     * {@link FullUserInfo#getShortCapabilityBlocks()} for more information on
     * short capability blocks.
     */
    public static final CapabilityBlock BLOCK_SHORTCAPS =
            new CapabilityBlock(new byte[] {
                0x09, 0x46, 0x00, 0x00, 0x4c, 0x7f, 0x11, (byte) 0xd1,
                (byte) 0x82, 0x22, 0x44, 0x45, 0x53, 0x54, 0x00, 0x00});

    /**
     * A capability block indicating that the user is available for Secure IM
     * and may be available for other secure communcations (secure chat rooms,
     * file transfer, and so on). This capability block causes a padlock icon to
     * be displayed next to one's screenname in the official AIM clients.
     */
    public static final CapabilityBlock BLOCK_ENCRYPTION =
            new CapabilityBlock(new byte[] {
                0x09, 0x46, 0x00, 0x01, 0x4c, 0x7f, 0x11, (byte) 0xd1,
                (byte) 0x82, 0x22, 0x44, 0x45, 0x53, 0x54, 0x00, 0x00});

    /**
     * Converts the given list of capabilities to a block of bytes, suitable for
     * sending in a {@link InfoData} structure.
     *
     * @param capabilities the list of capabilities to convert to a block of
     *        binary data
     * @return a block of binary data containing the binary representations of
     *         the given capabilities
     */
    public static byte[] convertToBytes(CapabilityBlock[] capabilities) {
        byte[] data = new byte[capabilities.length * 16];
        for (int i = 0; i < capabilities.length; i++) {
            capabilities[i].getBlock().copyTo(data, i*16);
        }

        return data;
    }

    /**
     * Extracts a list of capability blocks from the given data block.
     *
     * @param block the data block containing zero or more capability blocks
     * @return a list of capability blocks contained in the given data block
     */
    public static CapabilityBlock[] getCapabilityBlocks(ByteBlock block) {
        CapabilityBlock[] blocks = new CapabilityBlock[block.getLength()/16];

        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = new CapabilityBlock(block.subBlock(0, 16));

            block = block.subBlock(16);
        }

        return blocks;
    }

    /**
     * The unique block of bytes that comprises this capability block.
     */
    private final ByteBlock block;

    /**
     * A relatively unique hash code for this block. Cached because I presume
     * capability blocks will be hash keys rather frequently.
     */
    private final int hashCode;

    /**
     * Creates a capability block from the given sixteen-byte block.
     * @param block the block from which the capability block should be
     *        generated
     */
    private CapabilityBlock(byte[] block) {
        this(ByteBlock.wrap(block));
    }

    /**
     * Creates a new capability block from the given sixteen-byte block.
     *
     * @param block the sixteen-byte block that comprises the capability block
     *        to be created
     * @throws IllegalArgumentException if the given block does not contain
     *         exactly sixteen bytes
     */
    public CapabilityBlock(ByteBlock block) throws IllegalArgumentException {
        DefensiveTools.checkNull(block, "block");

        if (block.getLength() < 16) {
            throw new IllegalArgumentException("invalid capability block: "
                    + "length must be 16 (is " + (block.getLength()) + ")");
        }

        // store the block, copying its contents in case it came in a large
        // packet whose backing array should be thrown away
        this.block = ByteBlock.wrap(block.subBlock(0, 16).toByteArray());

        // and compute a hash code by doing stuff to the bytes. I really have
        // no idea if this is a good hash code, but hey, it doesn't matter too
        // much.
        long longCode = BinaryTools.getLong(block, 0)
                ^ BinaryTools.getLong(block, 8);
        hashCode = (int) ((longCode >> 16) ^ (longCode & 0xff));
    }

    /**
     * Returns the sixteen-byte block that identifies this capability block.
     *
     * @return this capability's byte block
     */
    public final ByteBlock getBlock() { return block; }

    public final long getWritableLength() {
        return 16;
    }

    public void write(OutputStream out) throws IOException {
        block.write(out);
    }

    /**
     * Returns <code>true</code> if this and the given
     * <code>CapabilityBlock</code> represent the same capability. That is,
     * returns whether or not this and the given capability block have the same
     * underlying sixteen-byte data block.
     *
     * @param other the <code>CapabilityBlock</code> to compare to
     * @return whether this capability block represents the same capability as
     *         the given block
     */
    public boolean equals(Object other) {
        if (!(other instanceof CapabilityBlock)) return false;
        if (this == other) return true;

        // return true only if the bytes are equal
        return block.equals(((CapabilityBlock) other).block);
    }

    public int hashCode() { return hashCode; }

    public String toString() {
        String name = "unknown capability block";
        Field[] fields = CapabilityBlock.class.getFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];

            if (!Modifier.isStatic(field.getModifiers())) continue;
            if (!field.getName().startsWith("BLOCK_")) continue;
            try {
                if (field.get(null).equals(this)) {
                    name = field.getName();
                    break;
                }
            } catch (IllegalAccessException e) {
                continue;
            }
        }
        return "CapabilityBlock: " + BinaryTools.describeData(block) + " ("
                + name + ")";
    }
}
