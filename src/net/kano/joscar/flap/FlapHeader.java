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

package net.kano.joscar.flap;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents the first six bytes of a FLAP packet, the FLAP "header," which
 * contains a sequence number, channel, and data length.
 */
final class FlapHeader {
    /**
     * The first byte of every FLAP command, as defined by the protocol.
     */
    private static final int PARITY_BYTE = 0x2a;

    /**
     * The sequence number of this FLAP header.
     */
    private final int seqnum;

    /**
     * The channel this FLAP header was read on.
     */
    private final int channel;

    /**
     * The length of the data to follow this header.
     */
    private final int length;

    /**
     * Reads a FLAP header from the given input stream, blocking until either
     * a full header has been read, at least the first byte of an invalid FLAP
     * header has been read, the end of the given stream has been reached, or
     * another I/O error occurs. If the end of the stream is reached and no
     * exception has been thrown, <code>null</code> is returned.
     *
     * @param in the stream from which to read the FLAP header
     * @return a FLAP header read from the given stream, or <code>null</code> if
     *         the end of the stream was reached with no errors
     *
     * @throws IOException if an I/O error occurs
     * @throws InvalidFlapHeaderException if an invalid FLAP header is received
     *         from the given stream
     */
    public static FlapHeader readFLAPHeader(InputStream in)
            throws InvalidFlapHeaderException, IOException {
        DefensiveTools.checkNull(in, "in");

        final byte[] header = new byte[6];
        int pos = 0;
        boolean paritied = false;

        while (pos < header.length) {
            final int count = in.read(header, pos, header.length - pos);

            if (count == -1) {
                // the connection died, or we got an EOF or something.
                return null;
            }

            pos += count;

            if (!paritied && pos >= 1) {
                paritied = true;
                if (header[0] != PARITY_BYTE) {
                    throw new InvalidFlapHeaderException("first byte of FLAP " +
                            "header must be 0x"
                            + Integer.toHexString(PARITY_BYTE) + ", was 0x"
                            + Integer.toHexString(header[0]));
                }
            }
        }

        return new FlapHeader(ByteBlock.wrap(header));
    }

    /**
     * Creates a new <code>FlapHeader</code> from the given block of six bytes.
     *
     * @param bytes the byte block from which to read the FLAP header
     * @throws IllegalArgumentException if the length of the given block is not
     *         six or if the block does not contain a valid FLAP header
     */
    public FlapHeader(ByteBlock bytes) throws IllegalArgumentException {
        DefensiveTools.checkNull(bytes, "bytes");
        
        if (bytes.getLength() != 6) {
            throw new IllegalArgumentException("FLAP header length ("
                    + bytes.getLength() + ") must be 6");
        }
        if (bytes.get(0) != PARITY_BYTE) {
            throw new IllegalArgumentException("FLAP command must begin " +
                    "with 0x2a (started with 0x"
                    + Integer.toHexString(((int) bytes.get(0)) & 0xff)
                    + "): "
                    + BinaryTools.describeData(bytes.subBlock(0, 6))
                    + " (data: " + (bytes.getLength() - 6) + " bytes)");
        }

        channel = BinaryTools.getUByte(bytes, 1);
        seqnum = BinaryTools.getUShort(bytes, 2);
        length = BinaryTools.getUShort(bytes, 4);
    }

    /**
     * Returns the sequence number of this FLAP header.
     * @return the sequence number in this FLAP header
     */
    public final int getSeqnum() {
        return seqnum;
    }

    /**
     * Returns the FLAP channel on which this FLAP header was received.
     * @return the FLAP channel on which this FLAP header was received
     */
    public final int getChannel() {
        return channel;
    }

    /**
     * Returns the data length value contained in this FLAP header.
     * @return the prescribed length of the FLAP data to follow this header
     */
    public final int getDataLength() {
        return length;
    }

    public String toString() {
        return "FlapHeader: " +
                "seqnum=" + seqnum +
                ", channel=" + channel +
                ", length=" + length;
    }
}
