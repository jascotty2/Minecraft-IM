/*
 *  Copyright (c) 2003, The Joust Project
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
 *  File created by keith @ Jun 14, 2003
 *
 */

package net.kano.joscar;

/**
 * Represents a sequence of numbers within a given range and allows for
 * automatically-wrapping sequential traversal of such a sequence. This class
 * has an internal value for the "current" value in the sequence, which is
 * advanced with each call to {@link #next}.
 * <br>
 * <br>
 * <i>Example usage:</i>
 * <pre>
class CommandSender {
    // command ID's must be between 1 and 100 and each
    // must be greater than the last, unless wrapping
    // back to 1 from 100
    SeqNum cmdIdSeq = new SeqNum(1, 100);

    void sendCmd(Command cmd) {
        long cmdId = cmdIdSeq.next();
        System.out.println("Command #" + cmdId + ": "
                + cmd);
        reallySendCmd(cmd);
    }

    ...
}
 * </pre>
 */
public class SeqNum {
    /** The minimum value for a number in this sequence. */
    private final long min;
    /** The maximum value for a number in this sequence. */
    private final long max;
    /** The last-generated value in this sequence. */
    private long last;

    /**
     * Creates a new sequence with the given minimum and maximum values. Note
     * that the given maximum value is an actual maximum value; that is, a
     * value returned by <code>next</code> could be that value.
     *
     * @param min the smallest value allowed in this sequence
     * @param max the largest value allowed in this sequence
     */
    public SeqNum(long min, long max) {
        this(min, max, min);
    }

    /**
     * Creates a new sequence with the given minimum and maximum values. Note
     * that the given maximum value is an actual maximum value; that is, a
     * value returned by <code>next</code> could be that value.
     *
     * @param min the smallest value allowed in this sequence
     * @param max the largest value allowed in this sequence
     * @param current an initial value for the current value in this sequence
     *
     * @throws IllegalArgumentException if the given initial value does not lie
     *         within the given range
     */
    public SeqNum(long min, long max, long current) {
        DefensiveTools.checkRange(current, "current", min, max);

        this.min = min;
        this.max = max;
        this.last = current;
    }

    /**
     * Returns the minimum value of an element of this sequence.
     *
     * @return this sequence's inclusive lower bound
     */
    public final long getMin() { return min; }

    /**
     * Returns the maximum value of an element of this sequence.
     *
     * @return this sequence's inclusive upper bound
     */
    public final long getMax() { return max; }

    /**
     * Returns the last value returned by {@link #next}, or the initial value if
     * <code>next()</code> has not been called.
     *
     * @return the last value returned by <code>next()</code>
     */
    public synchronized final long getLast() { return last; }

    /**
     * Returns the next element of this sequence. This method advances this
     * class's "current" value of the sequence.
     *
     * @return the next element of this sequence, wrapping if necessary
     */
    public synchronized long next() {
        if (last == max) last = min;
        else last++;

        return last;
    }
}
