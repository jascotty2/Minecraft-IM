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
 *  File created by keith @ Feb 14, 2003
 *
 */

package net.kano.joscar.flap;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.LiveWritable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Represents a FLAP packet, containing a sequence number, channel, and FLAP
 * data.
 */
public final class FlapPacket implements LiveWritable {
    /**
     * The "login" FLAP channel, channel {@value}.
     */
    public static final int CHANNEL_LOGIN = 0x01;

    /**
     * The "SNAC" FLAP channel, channel {@value}.
     */
    public static final int CHANNEL_SNAC  = 0x02;

    /**
     * The "error" FLAP channel, channel {@value}.
     */
    public static final int CHANNEL_ERROR = 0x03;

    /**
     * The "closing" FLAP channel, channel {@value}.
     */
    public static final int CHANNEL_CLOSE = 0x04;

    /**
     * The maximum length of FLAP data contained in a FLAP packet.
     */
    public static final int MAX_DATA_LEN = 0xffff;

    /**
     * The sequence number of this packet.
     */
    private final int seqnum;

    /**
     * The channel on which this packet resides.
     */
    private final int channel;

    /**
     * The FLAP data block of this FLAP packet, or <code>null</code> if this
     * is a <code>FlapCommand</code>-based packet and its data block has not
     * been written (and stored locally) yet.
     */
    private ByteBlock block = null;

    /**
     * The <code>FlapCommand</code> used to create this FLAP packet, if this
     * is an outgoing packet.
     */
    private final FlapCommand command;

    /**
     * Generates a FLAP packet based on the given header and reading the FLAP
     * data from the given stream.
     *
     * @param header the FLAP header of this packet
     * @param in the stream from which to read the rest of the packet
     * @return a <code>FlapPacket</code> constructed from the given header and
     *         the FLAP data in the given stream
     * @throws IOException if an I/O exception occurs
     */
    static FlapPacket readRestOfFlap(FlapHeader header, InputStream in)
            throws IOException {
        DefensiveTools.checkNull(header, "header");
        DefensiveTools.checkNull(in, "in");

        final byte[] data = new byte[header.getDataLength()];
        int pos = 0;

        while (pos < data.length) {
            int count = in.read(data, pos, data.length - pos);

            if (count == -1) {
                // the connection died!
                return null;
            }

            pos += count;
        }

        return new FlapPacket(header, ByteBlock.wrap(data));
    }

    /**
     * Creates a new <code>FlapPacket</code> from the given FLAP header and the
     * given FLAP data.
     * @param header the FLAP header from which to generate this packet
     * @param data the FLAP data for this packet
     * @throws IllegalArgumentException if the prescribed data length in the
     *         given FLAP header does not match the length of the given FLAP
     *         data block
     */
    FlapPacket(FlapHeader header, ByteBlock data)
            throws IllegalArgumentException {
        DefensiveTools.checkNull(header, "header");
        DefensiveTools.checkNull(data, "data");

        if (header.getDataLength() != data.getLength()) {
            throw new IllegalArgumentException("FLAP data length ("
                    + data.getLength() + ") does not agree with header ("
                    + header.getDataLength() + ")");
        }

        // looks like it's a valid FLAP command. yai.
        channel = header.getChannel();
        seqnum = header.getSeqnum();
        this.block = data;
        this.command = null;
    }

    /**
     * Creates a new FLAP command with the given sequence number and properties
     * of the given FLAP command.
     * @param seqnum the sequence number to use
     * @param command the FLAP command to use to create the packet
     */
    FlapPacket(int seqnum, FlapCommand command) {
        DefensiveTools.checkNull(command, "command");

        this.seqnum = seqnum;
        this.channel = command.getChannel();
        this.block = null;
        this.command = command;
    }

    /**
     * Returns this packet's sequence number.
     * @return the sequence number of this packet
     */
    public final int getSeqnum() {
        return seqnum;
    }

    /**
     * Returns this packet's FLAP channel.
     * @return the FLAP channel on which this packet resides
     */
    public final int getChannel() {
        return channel;
    }

    /**
     * Returns the FLAP data associated with this packet. This may return
     * <code>null</code> if this is an outgoing packet and the FLAP data have
     * not yet been written to a connection, and thus have not yet been stored
     * locally.
     *
     * @return this packet's FLAP data, or <code>null</code> if the data have
     *         not yet been generated
     */
    public synchronized final ByteBlock getData() {
        return block;
    }

    /**
     * Writes this FLAP packet to the given stream, generating FLAP data from
     * the associated <code>FlapCommand</code> if necessary.
     *
     * @param out the stream to which to write
     * @throws FlapDataLengthException if, during FLAP data generation, the FLAP
     *         data length is too large to hold in a FLAP packet
     * @throws IOException if an I/O error occurs
     */
    public synchronized void write(OutputStream out)
            throws FlapDataLengthException, IOException {
        if (block == null) {
            ByteArrayOutputStream tmp = new ByteArrayOutputStream();

            try {
                command.writeData(tmp);
            } catch (IOException impossible) { }

            block = ByteBlock.wrap(tmp.toByteArray());
        }

        int len = (int) block.getWritableLength();

        if (len > MAX_DATA_LEN) {
            // uh oh.
            throw new FlapDataLengthException("data length (" + len + ") must "
                    + "be <= " + MAX_DATA_LEN);
        }

        BinaryTools.writeUByte(out, 0x2a);
        BinaryTools.writeUByte(out, channel);
        BinaryTools.writeUShort(out, seqnum);
        BinaryTools.writeUShort(out, len);

        block.write(out);
    }

    public String toString() {
        return "FlapPacket (channel=" + channel + ", seq=" + seqnum + ")";
    }
}
