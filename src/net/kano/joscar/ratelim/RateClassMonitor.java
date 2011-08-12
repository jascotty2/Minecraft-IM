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
 *  File created by keith @ Jun 5, 2003
 *
 */

package net.kano.joscar.ratelim;

import net.kano.joscar.DefensiveTools;
import net.kano.joscar.snaccmd.conn.RateChange;
import net.kano.joscar.snaccmd.conn.RateClassInfo;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Monitors rate information for a single rate class.
 */
public class RateClassMonitor {
    /** A logger for rate-related logging. */
    private static final Logger logger
            = Logger.getLogger("net.kano.joscar.ratelim");

    /** The rate monitor that acts as this monitor's parent. */
    private final RateMonitor rateMonitor;
    /** Rate information for the rate class that this monitor is monitoring. */
    private RateClassInfo rateInfo;
    /** The time at which the last command was sent in this class. */
    private long last = -1;
    /** The current "running average" for this class. */
    private long runningAvg;
    /** Whether or not this rate class is limited. */
    private boolean limited = false;
    /**
     * This class's error margin, or <code>-1</code> to fall through to the
     * parent rate monitor's error margin.
     */
    private int errorMargin = -1;

    /**
     * Creates a new rate class monitor with the given parent rate monitor and
     * rate class information.
     *
     * @param rateMonitor this rate class monitor's "parent" rate monitor
     * @param rateInfo information about the rate class that this monitor should
     *        monitor
     */
    RateClassMonitor(RateMonitor rateMonitor, RateClassInfo rateInfo) {
        this.rateMonitor = rateMonitor;
        this.rateInfo = rateInfo;
        this.runningAvg = rateInfo.getMax();
    }

    /**
     * Updates the rate information for this monitor's associated rate class.
     *
     * @param changeCode the rate change code sent by the server with the given
     *        rate information, or <code>-1</code> if none was sent
     * @param rateInfo the rate information that was sent
     */
    synchronized void updateRateInfo(int changeCode, RateClassInfo rateInfo) {
        DefensiveTools.checkNull(rateInfo, "rateInfo");

        if (rateInfo.getRateClass() != this.rateInfo.getRateClass()) {
            throw new IllegalArgumentException("updated rate information is " +
                    "not the same class as the previous rate information for " +
                    "this rate class monitor");
        }

        if (logger.isLoggable(Level.FINER)) {
            logger.finer("Rate monitor for class " + rateInfo.getRateClass()
                    + " thinks rate average is " + runningAvg + "ms; server "
                    + "thinks it is " + rateInfo.getCurrentAvg() + "ms");
        }

        this.rateInfo = rateInfo;
        // I'm not sure if this min call is necessary, or correct, but I know
        // sometimes the server will give you really crazy values (in the range
        // of several minutes) for an average. but that is only on the initial
        // rate class packet. either way I think your average can never
        // correctly be greater than the max.
//        if (rateInfo.getCurrentAvg() < runningAvg) {
//            runningAvg = Math.min(rateInfo.getCurrentAvg(), rateInfo.getMax());
//        }
        runningAvg = Math.min(rateInfo.getMax(), runningAvg);

        if (changeCode == RateChange.CODE_LIMITED) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Rate class " + this.rateInfo.getRateClass()
                        + ") is now rate-limited!");
            }
            setLimited(true);

        } else if (changeCode == RateChange.CODE_LIMIT_CLEARED) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Rate class " + this.rateInfo.getRateClass()
                        + ") is no longer rate-limited, according to server");
            }
            setLimited(false);
        }
    }

    /**
     * Updates this monitor's associated rate class's current rate with the
     * given send time.
     *
     * @param sentTime the time at which a command in the associated rate class
     *        was sent, in milliseconds since the unix epoch
     */
    synchronized void updateRate(long sentTime) {
        if (last != -1) {
            assert sentTime >= last;
            runningAvg = computeCurrentAvg(sentTime);
        }
        last = sentTime;
    }

    /**
     * Returns the rate information associated with this monitor's associated
     * rate class.
     *
     * @return this monitor's associated rate class's rate information
     */
    public synchronized final RateClassInfo getRateInfo() { return rateInfo; }

    /**
     * Ensures that this monitor's limited status ({@link #limited}) is
     * accurate.
     */
    private synchronized void updateLimitedStatus() {
        if (limited) {
            long avg = computeCurrentAvg();
            if (avg > rateInfo.getClearAvg() + getErrorMargin()) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("We think that rate class "
                            + rateInfo.getRateClass() + " is not limited "
                            + "anymore (avg is " + avg + ")");
                }
                setLimited(false);
            }
        }
    }

    /**
     * Returns this monitor's error margin. If this monitor's error margin is
     * set to <code>-1</code>, the error margin of this monitor's parent
     * <code>RateMonitor</code> will be returned.
     *
     * @return this monitor's error margin
     */
    public synchronized final int getErrorMargin() {
        if (errorMargin == -1) return rateMonitor.getErrorMargin();
        else return errorMargin;
    }

    /**
     * Returns this monitor's locally set error margin. This value defaults to
     * <code>-1</code>, which indicates that the error margin should be
     * "inherited" from this rate class monitor's parent
     * <code>RateMonitor</code>.
     *
     * @return this monitor's locally set error margin, or <code>-1</code> if
     *         this monitor's error margin is currently inherited from its
     *         parent rate monitor
     */
    public synchronized final int getLocalErrorMargin() { return errorMargin; }

    /**
     * Sets this monitor's error margin. Note that if the given margin is
     * <code>-1</code> this monitor's error margin will be "inherited" from this
     * monitor's parent <code>RateMonitor</code>.
     *
     * @param errorMargin an error margin value
     */
    public synchronized final void setErrorMargin(int errorMargin) {
        DefensiveTools.checkRange(errorMargin, "errorMargin", -1);

        this.errorMargin = errorMargin;
    }

    /**
     * Computes a new rate average given the time at which a command was sent.
     * Note that this method does <i>not</i> modify the current running average;
     * it merely computes a new one and returns it.
     *
     * @param sentTime the time at which the command was sent
     * @return a new average computed from the given send time and the current
     *         running average
     */
    private synchronized long computeCurrentAvg(long sentTime) {
        long diff = sentTime - last;
        long winSize = rateInfo.getWindowSize();
        long max = rateInfo.getMax();
        return Math.min(max, (runningAvg * (winSize - 1) + diff) / winSize);
    }

    /**
     * Computes "the current rate average," what the average would be if a
     * command were sent at the time this method was invoked.
     *
     * @return the "current rate average"
     *
     * @see #computeCurrentAvg(long)
     */
    private synchronized long computeCurrentAvg() {
        return computeCurrentAvg(System.currentTimeMillis());
    }

    /**
     * Sets whether this rate monitor's associated rate class is currently
     * rate-limited. This method will notify any listeners if the value has
     * changed.
     *
     * @param limited whether or not this monitor's rate class is currently rate
     *        limited
     */
    private synchronized void setLimited(boolean limited) {
        if (limited != this.limited) {
            this.limited = limited;

            rateMonitor.fireLimitedEvent(this, this.limited);
        }
    }

    /**
     * Returns whether this rate monitor's associated rate class is currently
     * rate-limited.
     *
     * @return whether this rate monitor's associated rate class is currently
     *         rate-limited
     */
    public synchronized final boolean isLimited() {
        updateLimitedStatus();

        return limited;
    }

/*
    //TODO: implement, document getCurrentAvg
    public final long getCurrentAvg() {
        return computeCurrentAvg();
    }
*/

    /**
     * Returns what the rate average was when the last command in the associated
     * rate class was sent.
     *
     * @return the rate average at the time of the last command send
     */
    public synchronized final long getLastRateAvg() {
        return runningAvg;
    }

    /**
     * Returns what the rate average <i>would</i> be if a command were sent at
     * the current time.
     *
     * @return the potential rate average
     */
    public final long getPotentialAvg() {
        return getPotentialAvg(System.currentTimeMillis());
    }

    /**
     * Returns what the rate average <i>would</i> be if a command were sent at
     * the given time.
     *
     * @param time the time at which a hypothetical command would be sent, in
     *        milliseconds since the unix epoch
     * @return the potential rate average
     */
    public final long getPotentialAvg(long time) {
        return computeCurrentAvg(time);
    }

    /**
     * Returns how long one "should" wait before sending a command in this
     * monitor's associated rate class (to avoid being rate limited). This
     * algorithm attempts to stay above the rate limit (or the clear limit, if
     * {@linkplain #isLimited currently rate limited}) plus the {@linkplain
     * #getErrorMargin error margin}. Note that this method will never return
     * a value less than zero.
     *
     * @return how long one should wait before sending a command in the
     *         associated rate class
     */
    public synchronized final long getOptimalWaitTime() {
        return getTimeUntil(getMinSafeAvg() + getErrorMargin());
    }

    /**
     * Returns the average above which this monitor's associated rate class's
     * average must stay to avoid being rate-limited. This method ignores the
     * {@linkplain #getErrorMargin error margin}.
     *
     * @return the minimum average that this monitor's associated rate class
     *         must stay above to avoid rate limiting
     */
    private synchronized long getMinSafeAvg() {
        if (isLimited()) return rateInfo.getClearAvg();
        else return rateInfo.getLimitedAvg();
    }

    /**
     * Returns how long one must wait before sending a command in this monitor's
     * associated rate class to keep the current average above the given
     * average. This method ignores the {@linkplain #getErrorMargin error
     * margin}; it returns exactly how long one must wait for the rate average
     * to be equal to or above the given average. Note that this method will
     * never return a value less than zero.
     *
     * @param minAvg the "target" average
     * @return how long one must wait before sending a command to stay above the
     *         given average
     */
    public synchronized final long getTimeUntil(long minAvg) {
        if (last == -1) return 0;

        long winSize = rateInfo.getWindowSize();
        long sinceLast = System.currentTimeMillis() - last;

        long minLastDiff = (winSize * minAvg) - (runningAvg  * (winSize - 1));
        long toWait = minLastDiff - sinceLast + 1;

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Class " + rateInfo.getRateClass()
                    + " should be waiting " + toWait + "ms (avg is "
                    + computeCurrentAvg() + "ms)");
        }

        return Math.max(toWait, 0);
    }

    /**
     * Returns the number of commands in this rate class that <i>could</i> be
     * sent immediately, without being rate limited.
     *
     * @return the number of commands in this rate class that could be sent
     *         immediately without being rate limited
     */
    public synchronized final int getPossibleCmdCount() {
        return getPossibleCmdCount(runningAvg);
    }

    /**
     * Returns the <i>maximum</i> number of commands in this rate class that
     * could ever be sent at the same time without being rate-limited. This
     * method is similar to {@link #getPossibleCmdCount()} but differs in that
     * this method essentially returns the upper limit for the return value
     * of <code>getPossibleCmdCount()</code>: it returns the maximum number of
     * commands that could <i>ever</i> be sent at once; that is, what
     * <code>getPossibleCmdCount()</code> <i>would</i> return if the current
     * rate average were at its maximum.
     *
     * @return the maximum number of commands that could be sent in this rate
     *         class simultaneously without being rate limited
     */
    public synchronized final int getMaxCmdCount() {
        return getPossibleCmdCount(rateInfo.getMax());
    }

    /**
     * Returns the number of commands that could be sent in this monitor's
     * associated rate class without being rate-limited.
     *
     * @param currentAvg the starting average (normally the current average)
     * @return the number of commands that could be sent in this monitor's
     *         associated rate class without being rate-limited
     */
    private synchronized int getPossibleCmdCount(long currentAvg) {
        long diff = System.currentTimeMillis() - last;
        long winSize = rateInfo.getWindowSize();
        long limited = getMinSafeAvg() + getErrorMargin();
        long avg = currentAvg;
        int count = 0;

        while (avg > limited) {
            avg = (diff + avg * (winSize - 1)) / winSize;

            // after the first iteration we set diff to 0, since the difference
            // will be zero
            diff = 0;

            count++;
        }

        // this means the loop never iterated, so no commands can be sent
        if (count == 0) return 0;

        // the loop iterates once past the maximum, so we decrement the counter
        // to get the number of commands that can be sent
        return count - 1;
    }
}
