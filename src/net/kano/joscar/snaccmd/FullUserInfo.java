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
 *  File created by keith @ Feb 21, 2003
 *
 */

package net.kano.joscar.snaccmd;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.LiveWritable;
import net.kano.joscar.OscarTools;
import net.kano.joscar.StringBlock;
import net.kano.joscar.tlv.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;

/**
 * A very widely used structure that represents a single screen name and various
 * flags and values associated with it. Such values include warning level,
 * idle time, and away status.
 */
public class FullUserInfo implements LiveWritable {
    /**
     * A user mask indicating that a user never replied to his confirmation
     * email, and may in fact not actually own the email address to which the
     * screenname is registered.
     */
    public static final int MASK_UNCONFIRMED = 0x0001;

    /**
     * A user mask indicating that a user is an administrator.
     */
    public static final int MASK_ADMIN = 0x0002;

    /**
     * A user mask indicating that a user is using AOL and not AIM.
     */
    public static final int MASK_AOL = 0x0004;

    /**
     * A user mask indicating that a user is using AIM for free (as opposed to
     * using AOL, as with <code>MASK_AOL</code>).
     */
    public static final int MASK_FREE = 0x0010;

    /**
     * A user mask indicating that a user is using a mobile phone to chat. Note
     * that not every user on a mobile phone will have this flag, as it can be
     * {@linkplain net.kano.joscar.ssiitem.PrivacyItem#VISMASK_HIDE_WIRELESS
     * disabled}.
     */
    public static final int MASK_WIRELESS = 0x0080;

    /**
     * A user mask indicating that a user is an ActiveBuddy robot.
     */
    public static final int MASK_AB = 0x0400;

    /**
     * A user mask indicating that a user is away.
     */
    public static final int MASK_AWAY = 0x0020;

    /**
     * A user mask representing all users. Mainly for use in {@link
     * net.kano.joscar.ssiitem.PrivacyItem}.
      */
    public static final long MASK_ALL = 0xffffffffL;

    /**
     * Reads a user info block from the given data block, or returns
     * <code>null</code> if no valid user info block is present in the given
     * data block.
     *
     * @param block the block from which to read user info
     * @return a user info object read from the given data block
     */
    public static FullUserInfo readUserInfo(ByteBlock block) {
        DefensiveTools.checkNull(block, "block");

        int start = block.getOffset();

        StringBlock snInfo = OscarTools.readScreenname(block);

        if (snInfo == null) return null;

        String sn = snInfo.getString();
        int snLength = snInfo.getTotalSize();

        if (block.getLength() < snLength + 2) {
            return new FullUserInfo(sn, snLength);
        }

        block = block.subBlock(snLength);

        int warningLevel = BinaryTools.getUShort(block, 0);

        block = block.subBlock(2);

        if (block.getLength() < 2) {
            return new FullUserInfo(sn, warningLevel, snLength + 2);
        }

        int tlvCount = BinaryTools.getUShort(block, 0);
        block = block.subBlock(2);
        TlvChain chain = TlvTools.readChain(block, tlvCount);

        // read the TLV's we know about
        Tlv userFlagTlv = chain.getLastTlv(TYPE_USER_FLAG);
        Tlv accountCreationTlv = chain.getLastTlv(TYPE_ACCT_CREATED);
        Tlv onSinceTlv = chain.getLastTlv(TYPE_ON_SINCE);
        Tlv idleTimeTlv = chain.getLastTlv(TYPE_IDLE_MINS);
        Tlv memberSinceTlv = chain.getLastTlv(TYPE_MEMBER_SINCE);
        Tlv capTlv = chain.getLastTlv(TYPE_CAPS);
        Tlv sessionLengthAIM = chain.getLastTlv(TYPE_SESS_LEN_AIM);
        Tlv sessionLengthAOL = chain.getLastTlv(TYPE_SESS_LEN_AOL);
        Tlv extraInfoTlv = chain.getLastTlv(TYPE_EXTRA_INFO);
        Tlv certHashTlv = chain.getLastTlv(TYPE_CERT_HASH);
        Tlv shortCapTlv = chain.getLastTlv(TYPE_SHORT_CAPS);

        MutableTlvChain extras = TlvTools.getMutableCopy(chain);
        extras.removeTlvs(new int[] {
            TYPE_USER_FLAG, TYPE_ACCT_CREATED, TYPE_ON_SINCE, TYPE_IDLE_MINS,
            TYPE_MEMBER_SINCE, TYPE_CAPS, TYPE_SESS_LEN_AIM, TYPE_SESS_LEN_AOL,
            TYPE_EXTRA_INFO, TYPE_CERT_HASH, TYPE_SHORT_CAPS
        });

        Boolean away = null;
        int flags = -1;
        // check for the user flag TLV
        if (userFlagTlv != null) {
            // read the flags from the TLV
            flags = userFlagTlv.getDataAsUShort();
            away = Boolean.valueOf((flags & MASK_AWAY) != 0);
        }

        Date accountCreated = null;
        if (accountCreationTlv != null) {
            // the time is a long as seconds since the unix epoch, and the Date
            // constructor takes milliseconds, so we multiply it by 1000.
            accountCreated = new Date(accountCreationTlv.getDataAsUInt()
                    * 1000);
        }

        Date memberSince = null;
        if (memberSinceTlv != null) {
            memberSince = new Date(memberSinceTlv.getDataAsUInt() * 1000);
        }

        long sessLengthAIM = -1;
        if (sessionLengthAIM != null) {
            // once again convert seconds to milliseconds and subtract from the
            // current time
            sessLengthAIM = sessionLengthAIM.getDataAsUInt() * 1000;
        }

        long sessLengthAOL = -1;
        if (sessionLengthAOL != null) {
            sessLengthAOL = sessionLengthAOL.getDataAsUInt() * 1000;
        }

        Date onSince = null;
        if (onSinceTlv != null) {
            // the "on since" is an absolute time, so just set the date to that
            onSince = new Date(onSinceTlv.getDataAsUInt() * 1000);
        }

        // the idle time is sent to us in minutes. yay.
        int idleMins = -1;
        if (idleTimeTlv != null) {
            idleMins = idleTimeTlv.getDataAsUShort();
        }

        // the capabilities!!!
        CapabilityBlock[] capabilityBlocks = null;
        if (capTlv != null) {
            // and create a new CapabilityBlock from each one.
            capabilityBlocks = CapabilityBlock.getCapabilityBlocks(
                    capTlv.getData());
        }

        ExtraInfoBlock[] extraInfos = null;
        if (extraInfoTlv != null) {
            ByteBlock extraBlocks = extraInfoTlv.getData();
            extraInfos = ExtraInfoBlock.readExtraInfoBlocks(extraBlocks);
        }

        ByteBlock certHash = null;
        if (certHashTlv != null) {
            certHash = certHashTlv.getData();
        }

        ShortCapabilityBlock[] shortCaps = null;
        if (shortCapTlv != null) {
            ByteBlock shortCapData = shortCapTlv.getData();
            shortCaps = ShortCapabilityBlock.readShortCaps(shortCapData);
        }

        block = block.subBlock(chain.getTotalSize());

        // and store this for use by fun things.
        int totalSize = block.getOffset() - start;

        ImmutableTlvChain extrasImmutable = TlvTools.getImmutableCopy(extras);

        return new FullUserInfo(sn, warningLevel, flags, accountCreated,
                memberSince, sessLengthAIM, sessLengthAOL, onSince, idleMins,
                capabilityBlocks, away, extraInfos, certHash, shortCaps,
                extrasImmutable, totalSize);
    }

    /**
     * A TLV type containing the user masks.
     */
    private static final int TYPE_USER_FLAG = 0x0001;

    /**
     * A TLV type containing the date at which a user's account was created.
     */
    private static final int TYPE_ACCT_CREATED = 0x0002;

    /**
     * A TLV type containing the date at which a user signed on.
     */
    private static final int TYPE_ON_SINCE = 0x0003;

    /**
     * A TLV type containing the number of minutes a user has been idle.
     */
    private static final int TYPE_IDLE_MINS = 0x0004;

    /**
     * A TLV type containing the date at which a user created his or her
     * account. At the time of this writing I am not sure of the difference
     * between this field and the <code>TYPE_ACCT_CREATED</code> field, though
     * neither are ever sent anymore.
     */
    private static final int TYPE_MEMBER_SINCE = 0x0005;

    /**
     * A TLV type containing capability blocks.
     */
    private static final int TYPE_CAPS = 0x000d;

    /**
     * A TLV type containing the number of seconds that an AIM user has been
     * online. This excludes AOL users.
     */
    private static final int TYPE_SESS_LEN_AIM = 0x000f;

    /**
     * A TLV type containing the number of seconds that an AOL user has been
     * online.
     */
    private static final int TYPE_SESS_LEN_AOL = 0x0010;

    /** A TLV containing a series of <code>ExtraInfoBlock</code> structures. */
    private static final int TYPE_EXTRA_INFO = 0x001d;

    /**
     * A TLV type containing an MD5 hash of the user's certificate information
     * block.
     */
    private static final int TYPE_CERT_HASH = 0x001b;

    /** A TLV type containing a list of "short capability blocks. */
    private static final int TYPE_SHORT_CAPS = 0x19;

    /** The screenname of this user. */
    private final String sn;

    /**
     * The total size of this structure, if read from a block of binary data.
     * Will be <code>-1</code> otherwise.
     */
    private final int totalSize;

    /**
     * The warning level of this user, as a "percentage." This value should be
     * <code>0</code>-<code>100</code>, or <code>-1</code> if this field is not
     * present.
     */
    private final int warningLevel;

    /** This user's user flags (like <code>MASK_FREE</code>). */
    private final int flags;

    /**
     * The date at which this user created his or her account, or
     * <code>null</code> if this field was not sent.
     */
    private final Date accountCreated;

    /**
     * Another date at which this user created his or her account, or
     * <code>null</code> if this field was not sent.
     */
    private final Date memberSince;

    /**
     * How long this AIM user has been online during this session, or
     * <code>null</code> if this field was not sent.
     */
    private final long sessionLengthAIM;

    /**
     * How long this AOL user has been online during this session, or
     * <code>null</code> if this field was not sent.
     */
    private final long sessionLengthAOL;

    /**
     * The date at which this user signed on, or <code>null</code> if this field
     * was not sent.
     */
    private final Date onSince;

    /**
     * The number of minutes this user has been idle, or <code>-1</code> if this
     * field was not sent.
     */
    private final int idleMins;

    /**
     * The capability blocks advertised by this user, or <code>null</code> if
     * this field was not sent.
     */
    private final CapabilityBlock[] capabilityBlocks;

    /**
     * Whether this user is away or not; <code>null</code> if this field is not
     * sent.
     */
    private final Boolean away;

    /**
     * A set of extra information blocks advertised by this user, or
     * <code>null</code> if this field was not sent.
     */
    private final ExtraInfoBlock[] extraInfos;

    /** An MD5 hash of the user's certificate information. */
    private final ByteBlock certInfoHash;

    /** A list of "short capability blocks." */
    private final ShortCapabilityBlock[] shortCaps;

    /** A set of extra TLV's that were not explicitly parsed into fields. */
    private final ImmutableTlvChain extraTlvs;

    /**
     * Creates a user info block containing only the given screenname.
     *
     * @param sn the screenname of this user info block
     * @param totalSize the total size of the block, as read from a data block
     */
    private FullUserInfo(String sn, int totalSize) {
        this(sn, -1, totalSize);
    }

    /**
     * Creates a user info block with the given properties.
     *
     * @param sn the screenname of this user info block
     * @param warningLevel the warning level of the given user
     * @param totalSize the total size of this block, as read from a data block
     */
    private FullUserInfo(String sn, int warningLevel, int totalSize) {
        this(sn, warningLevel, -1, null, null, -1, -1, null, -1, null, null,
                null, null, null, null, totalSize);
    }

    /**
     * Creates a new user info block with the given properties. Note that any
     * of these values except for <code>sn</code> can be <code>null</code> or
     * <code>-1</code> (depending on argument type) to indicate that the given
     * field should not be sent in this user info block.
     *
     * @param sn the user's screenname
     * @param warningLevel the user's warning level
     * @param flags the user's "user flags," like <code>MASK_FREE</code>
     * @param accountCreated the date at which this user's account was created
     * @param memberSince the date since which this user has been a member
     * @param sessAIM the amount of time, in seconds this user has been online,
     *        if this user is not an AOL user
     * @param sessAOL the amount of time, in seconds, this user has been online,
     *        if this user is on AOL
     * @param onSince the date at which this user signed on
     * @param idleMins the number of minutes this user has been idle
     * @param capabilityBlocks a list of capability blocks that this user is
     *        advertising
     * @param away whether this user is away
     * @param extraInfos a list of extra information blocks that this user is
     *        advertising
     * @param certHash an MD5 hash of the user's certificate information block
     * @param shortCaps a list of "short capability blocks"
     */
    public FullUserInfo(String sn, int warningLevel, int flags,
            Date accountCreated, Date memberSince, long sessAIM, long sessAOL,
            Date onSince, int idleMins, CapabilityBlock[] capabilityBlocks,
            Boolean away, ExtraInfoBlock[] extraInfos, ByteBlock certHash,
            ShortCapabilityBlock[] shortCaps) {
        this(sn, warningLevel, flags, accountCreated, memberSince, sessAIM,
                sessAOL, onSince, idleMins, capabilityBlocks, away, extraInfos,
                certHash, shortCaps, null);
    }

    /**
     * Creates a new user info block with the given properties. Note that any
     * of these values except for <code>sn</code> can be <code>null</code> or
     * <code>-1</code> (depending on argument type) to indicate that the given
     * field should not be sent in this user info block. This constructor allows
     * you to specify extra user info TLV's to be sent, in the case of a
     * feature added to the protcool after the release of this library.
     *
     * @param sn the user's screenname
     * @param warningLevel the user's warning level
     * @param flags the user's "user flags," like <code>MASK_FREE</code>
     * @param accountCreated the date at which this user's account was created
     * @param memberSince the date since which this user has been a member
     * @param sessAIM the amount of time, in seconds this user has been online,
     *        if this user is not an AOL user
     * @param sessAOL the amount of time, in seconds, this user has been online,
     *        if this user is on AOL
     * @param onSince the date at which this user signed on
     * @param idleMins the number of minutes this user has been idle
     * @param capabilityBlocks a list of capability blocks that this user is
     *        advertising
     * @param away whether this user is away
     * @param extraInfos a list of extra information blocks that this user is
     *        advertising
     * @param certHash an MD5 hash of the user's certificate information block
     * @param shortCaps a list of "short capability blocks"
     * @param extraTlvs a set of extra TLV's to be appended to this user info
     *        block
     */
    public FullUserInfo(String sn, int warningLevel, int flags,
            Date accountCreated, Date memberSince, long sessAIM, long sessAOL,
            Date onSince, int idleMins, CapabilityBlock[] capabilityBlocks,
            Boolean away, ExtraInfoBlock[] extraInfos, ByteBlock certHash,
            ShortCapabilityBlock[] shortCaps, ImmutableTlvChain extraTlvs) {
        this(sn, warningLevel, flags, accountCreated, memberSince, sessAIM,
                sessAOL, onSince, idleMins, capabilityBlocks, away, extraInfos,
                certHash, shortCaps, extraTlvs, -1);
    }

    /**
     * Creates a new user info block with the given properties. Note that any
     * of these values except for <code>sn</code> can be <code>null</code> or
     * <code>-1</code> (depending on argument type) to indicate that the given
     * field should was not included in the received user info block.
     *
     * @param sn the user's screenname
     * @param warningLevel the user's warning level
     * @param flags the user's "user flags," like <code>MASK_FREE</code>
     * @param accountCreated the date at which this user's account was created
     * @param memberSince the date since which this user has been a member
     * @param sessAIM the amount of time, in seconds this user has been online,
     *        if this user is not an AOL user
     * @param sessAOL the amount of time, in seconds, this user has been online,
     *        if this user is on AOL
     * @param onSince the date at which this user signed on
     * @param idleMins the number of minutes this user has been idle
     * @param caps a list of capability blocks that this user is
     *        advertising
     * @param away whether this user is away
     * @param extraInfos a list of extra information blocks that this user is
     *        advertising
     * @param certHash an MD5 hash of the user's certificate information block
     * @param shortCaps a list of "short capability blocks"
     * @param extraTlvs a set of extra TLV's to be appended to this user info
     *        block
     * @param totalSize the total size of this object, as read from a block
     *        of binary data
     */
    private FullUserInfo(String sn, int warningLevel, int flags,
            Date accountCreated, Date memberSince, long sessAIM, long sessAOL,
            Date onSince, int idleMins, CapabilityBlock[] caps,
            Boolean away, ExtraInfoBlock[] extraInfos, ByteBlock certHash,
            ShortCapabilityBlock[] shortCaps, ImmutableTlvChain extraTlvs,
            int totalSize) {

        DefensiveTools.checkNull(sn, "sn");
        DefensiveTools.checkRange(warningLevel, "warningLevel", 0);
        DefensiveTools.checkRange(sessAIM, "sessAIM", -1);
        DefensiveTools.checkRange(sessAOL, "sessAOL", -1);
        DefensiveTools.checkRange(idleMins, "idleMins", -1);
        DefensiveTools.checkRange(totalSize, "totalSize", -1);

        caps = (CapabilityBlock[])
                DefensiveTools.getNonnullArray(caps, "caps");

        extraInfos = (ExtraInfoBlock[])
                DefensiveTools.getNonnullArray(extraInfos, "extraInfos");

        shortCaps = (ShortCapabilityBlock[])
                DefensiveTools.getNonnullArray(shortCaps, "shortCaps");

        this.sn = sn;
        this.warningLevel = warningLevel;
        this.flags = flags;
        this.accountCreated = accountCreated;
        this.memberSince = memberSince;
        this.sessionLengthAIM = sessAIM;
        this.sessionLengthAOL = sessAOL;
        this.onSince = onSince;
        this.idleMins = idleMins;
        this.capabilityBlocks = caps;
        this.away = away;
        this.extraInfos = extraInfos;
        this.certInfoHash = certHash;
        this.shortCaps = shortCaps;
        this.extraTlvs = extraTlvs;
        this.totalSize = totalSize;
    }

    /**
     * Returns the screenname of the user whose info this object represents.
     *
     * @return this user info object's associated screenname
     */
    public final String getScreenname() {
        return sn;
    }

    /**
     * Returns the warning level of this user, as a "percentage," but multiplied
     * by ten. This value normally ranges inclusively from <code>0</code> to
     * <code>999</code> (which represents the percentage range 0% to 99.9%), but
     * will be <code>-1</code> if no warning level was sent.
     *
     * @return the warning level of this user
     */
    public final int getWarningLevel() {
        return warningLevel;
    }

    /**
     * Returns this user's "user mask" flags. This is normally a bitwise
     * combination of {@link #MASK_UNCONFIRMED}, {@link #MASK_FREE}, {@link
     * #MASK_AWAY}, {@link #MASK_AOL}, {@link #MASK_WIRELESS}, {@link
     * #MASK_ADMIN}, and {@link #MASK_AB}. To test for a given flag, use
     * something resembling the following code:
     * <pre>
if ((userInfo.getFlags() & FullUserInfo.MASK_WIRELESS) != 0) {
    System.out.println("User " + userInfo.getScreenname() + " is using a "
            + "wireless device!");
}
     * </pre>
     *
     * Note that this will return <code>-1</code> if this field was not sent.
     *
     * @return this user's "user mask"
     */
    public final int getFlags() {
        return flags;
    }

    /**
     * Returns the date at which this user's account was created, or
     * <code>null</code> if this field was not sent. This field does not appear
     * to be sent anymore by normal AIM servers.
     *
     * @return the date at which this user's account was created
     */
    public final Date getAccountCreated() {
        return accountCreated;
    }

    /**
     * Returns the date since which this user has been a member of AIM, or
     * <code>null</code> if this field was not sent. This field does not appear
     * to be sent anymore by normal AIM servers.
     * 
     * @return the date since which this user has been a member of AIM
     */
    public final Date getMemberSince() {
        return memberSince;
    }

    /**
     * Returns the number of seconds for which this user has been online, if
     * this user is an AIM user and not an AOL user. This will be
     * <code>null</code> if this field was not sent.
     *
     * @return this user's "session length," if this user is on AIM and not AOL
     */
    public final long getSessionLengthAIM() {
        return sessionLengthAIM;
    }

    /**
     * Returns the number of seconds for which this user has been online, if
     * this user is an AOL user. This will be <code>null</code> if this field
     * was not sent.
     *
     * @return this user's "session length," if this user is on AOL
     */
    public final long getSessionLengthAOL() {
        return sessionLengthAOL;
    }

    /**
     * Returns the date at which this user began the current session (that is,
     * the current AIM connection). This will be <code>null</code> if this field
     * was not sent.
     *
     * @return the date at which this user logged in
     */
    public final Date getOnSince() {
        return onSince;
    }

    /**
     * Returns the number of minutes for which this user has been idle, or
     * <code>-1</code> if this field was not sent.
     *
     * @return the number of minutes this user has spent idle
     */
    public final int getIdleMins() {
        return idleMins;
    }

    /**
     * Returns the list of "capability blocks" which this user is advertising,
     * or <code>null</code> if this field was not sent. Note that this will
     * return an array of size zero instead of <code>null</code> if this field
     * was sent but contained no capability blocks.
     *
     * @return this user's advertised "capability blocks"
     */
    public final CapabilityBlock[] getCapabilityBlocks() {
        return (capabilityBlocks == null ? null : capabilityBlocks.clone());
    }

    /**
     * Returns whether this user is away, or <code>null</code> if this field
     * was not sent.
     *
     * @return whether this user is currently away
     */
    public final Boolean getAwayStatus() {
        return away;
    }

    /**
     * Returns the list of extra information objects this user is advertising,
     * or <code>null</code> if this field was not sent. Note that this will
     * return a zero-length array if the field was sent but empty.
     *
     * @return a list of advertised extra information blocks
     */
    public final ExtraInfoBlock[] getExtraInfoBlocks() {
        return extraInfos == null ? null : extraInfos.clone();
    }

    /**
     * Returns an MD5 hash of the user's {@linkplain CertificateInfo security
     * information block}, or <code>null</code> if none is contained in this
     * user info object.
     *
     * @return an MD5 hash of the user's certificate information block
     */
    public final ByteBlock getCertInfoHash() { return certInfoHash; }

    /**
     * Returns the list of "short capability blocks" contained in this user info
     * object.
     *
     * @return the list of short capability blocks for the associated user
     */
    public ShortCapabilityBlock[] getShortCapabilityBlocks() {
        return (shortCaps == null)
                ? null
                : shortCaps.clone();
    }

    /**
     * Returns a TLV chain consisting of all TLV's not processed into fields
     * accessible by <code>get*</code> methods of this object. This is useful
     * to aid in processing additional fields AOL may add to the standard user
     * info block after the release of this library.
     *
     * @return a list of TLV's present in the received user info block that were
     *         not processed into fields of this object
     */
    public final ImmutableTlvChain getExtraTlvs() { return extraTlvs; }

    /**
     * Returns the total number of bytes that were read to generate this user
     * info block. Will be <code>-1</code> if this object was not read from
     * a block of data but instead instantiated manually.
     *
     * @return the total size, in bytes, of this object, as read from a block
     *         of binary data
     */
    public final int getTotalSize() { return totalSize; }

    public void write(OutputStream out) throws IOException {
        OscarTools.writeScreenname(out, sn);

        if (warningLevel == -1) return;

        BinaryTools.writeUByte(out, warningLevel);

        MutableTlvChain chain = TlvTools.createMutableChain();

        if (flags != -1 || away != null) {
            int tflags = this.flags;
            if (away != null) {
                if (away.booleanValue()) {tflags &= MASK_AWAY;} else {
                    tflags
                            &= ~MASK_AWAY;
                }
            }
            chain.addTlv(Tlv.getUShortInstance(TYPE_USER_FLAG, tflags));
        }

        if (accountCreated != null) {
            long unixtime = accountCreated.getTime() / 1000;

            chain.addTlv(Tlv.getUIntInstance(TYPE_ACCT_CREATED, unixtime));
        }

        if (memberSince != null) {
            long unixtime = memberSince.getTime() / 1000;

            chain.addTlv(Tlv.getUIntInstance(TYPE_MEMBER_SINCE, unixtime));
        }

        if (sessionLengthAIM != -1) {
            chain.addTlv(Tlv.getUIntInstance(TYPE_SESS_LEN_AIM,
                    sessionLengthAIM));
        }

        if (sessionLengthAOL != -1) {
            chain.addTlv(Tlv.getUIntInstance(TYPE_SESS_LEN_AOL,
                    sessionLengthAOL));
        }

        if (onSince != null) {
            long unixtime = onSince.getTime() / 1000;

            chain.addTlv(Tlv.getUIntInstance(TYPE_ON_SINCE, unixtime));
        }

        if (idleMins != -1) {
            chain.addTlv(Tlv.getUShortInstance(TYPE_IDLE_MINS, idleMins));
        }

        if (capabilityBlocks != null) {
            chain.addTlv(new Tlv(TYPE_CAPS, ByteBlock.wrap(
                    CapabilityBlock.convertToBytes(capabilityBlocks))));
        }

        if (extraInfos != null) {
            chain.addTlv(new Tlv(TYPE_EXTRA_INFO,
                    ByteBlock.createByteBlock(extraInfos)));
        }

        if (certInfoHash != null) {
            chain.addTlv(new Tlv(TYPE_CERT_HASH, certInfoHash));
        }

        if (extraTlvs != null) chain.addAll(extraTlvs);

        // whew.
        chain.write(out);
    }

	@Override
    public String toString() {
        return "UserInfo for " + sn + 
                (warningLevel != 0 ? (warningLevel/10) + "%"  : "") +
                ", flags=" + flags +

                (accountCreated != null ? ", acctCrtd=" + accountCreated : "") +

                (memberSince != null ? ", memberSince=" + memberSince : "") +

                (sessionLengthAIM != -1
                ? ", sessLen=" + sessionLengthAIM / 60 + "min" : "") +

                (sessionLengthAOL != -1
                ? ", sessLen=" + sessionLengthAOL / 60 + "min" : "") +

                (onSince != null ? ", onSince=" + onSince  : "") +

                (idleMins > 0 ? ", idleMins=" + idleMins : "") +

                (away.booleanValue() ? ", away" : "") +

                (extraInfos != null
                ? ", extraInfos=" + Arrays.asList(extraInfos) : "") +

                (certInfoHash != null ? ", encInfo=" + certInfoHash : "") +

                (extraTlvs != null && extraTlvs.getTlvCount() > 0
                ? ", extraTlvs=" + Arrays.asList(extraTlvs.getTlvs()) : "")

                + (shortCaps != null ? ", shortcaps: " + Arrays.asList(shortCaps) : "")
                + (capabilityBlocks != null ? ", longcaps: " + Arrays.asList(capabilityBlocks) : "");
    }
}
