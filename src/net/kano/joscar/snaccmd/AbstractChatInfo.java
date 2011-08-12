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
 *  File created by keith @ Feb 26, 2003
 *
 */

package net.kano.joscar.snaccmd;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.MiscTools;
import net.kano.joscar.snaccmd.chat.ChatMsg;
import net.kano.joscar.tlv.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

/**
 * Provides a base class for "chat information," which can be sent in two very
 * similar forms. The first form is {@linkplain ExchangeInfo chat exchange
 * information} and the second is {@linkplain FullRoomInfo chat room
 * information}.
 */
public abstract class AbstractChatInfo {

    /**
     * A value indicating that this block is a "short" information block. At the
     * time of this writing I am not sure which fields a "short" chat
     * information block has.
     */
    public static final int TYPE_SHORT = 1;
    /**
     * A value indicating that this block is a "full" information block. At the
     * time of this writing I am not sure which fields a "full" chat information
     * block has.
     */
    public static final int TYPE_FULL = 2;

    /**
     * A value indicating that this block consists of information about an
     * "instance" of a chat room.
     */
    public static final int TYPE_INSTANCE_INFO = 4;

    /**
     * At the time of this writing, I am unsure of what this value means.
     */
    public static final int TYPE_NAV_SHORT_DESC = 8;

    /**
     * At the time of this writing, I am unsure of what this value means.
     */
    public static final int TYPE_NAV_INSTANCE_INFO = 16;

    /**
     * A value indicating that creating rooms is not allowed on the given
     * exchange.
     */
    public static final int PERM_CANNOT_CREATE_ANYTHING = 0;

    /**
     * A value indicating that creating rooms is allowed on the given exchange.
     */
    public static final int PERM_CAN_CREATE_ROOM = 1;

    /**
     * A value indicating that creating new exchanges is allowed.
     */
    public static final int PERM_CAN_CREATE_EXCHANGE = 2;

    /**
     * At the time of this writing, I am unsure of what this value means.
     */
    public static final int MASK_WARNABLE = 0x01;

    /**
     * At the time of this writing, I am unsure of what this value means.
     */
    public static final int MASK_NAV_ONLY = 0x02;

    /**
     * At the time of this writing, I am unsure of what this value means.
     */
    public static final int MASK_INSTANCING_ALLOWED = 0x04;

    /**
     * A mask indicating that one is allowed to view the member list of a chat
     * room without first joining it.
     */
    public static final int MASK_CAN_PEEK = 0x08;

    /**
     * A TLV type for the TLV containing various bit "flags" related to the
     * associated room or exchange.
     */
    private static final int TYPE_FLAGS = 0x00c9;

    /**
     * A TLV type containing the creation date of a room (or exchange?).
     */
    private static final int TYPE_CREATION_DATE = 0x00ca;

    /**
     * A TLV type containing the maximum message length of a room (or
     * exchange?).
     */
    private static final int TYPE_MAX_MSG_LEN = 0x00d1;

    /**
     * A TLV type containing the maximum occupancy of a room.
     */
    private static final int TYPE_MAX_OCCUPANCY = 0x00d2;

    /**
     * A TLV type containing the name of a chat room.
     */
    private static final int TYPE_NAME = 0x00d3;

    /**
     * A TLV type containing "creation permissions" for a given exchange.
     */
    private static final int TYPE_CREATE_PERMS = 0x00d5;

    /**
     * A TLV type containing a charset.
     */
    private static final int TYPE_CHARSET_1 = 0x00d6;

    /**
     * A TLV type containing a language code.
     */
    private static final int TYPE_LANG_1 = 0x00d7;

    /**
     * A TLV type containing another charset.
     */
    private static final int TYPE_CHARSET_2 = 0x00d8;

    /**
     * A TLV type containing another language code.
     */
    private static final int TYPE_LANG_2 = 0x00d9;

    /**
     * A TLV type containing a value whose significance is currently unknown.
     */
    private static final int TYPE_SOMETHING = 0x00da;

    /** A TLV type containing a content type string. */
    private static final int TYPE_CONTENT_TYPE = 0x00db;
    /**
     * This chat information object's "flags," such as
     * <code>MASK_CAN_PEEK</code>.
     */
    private int flags;

    /**
     * The creation date of this chat room or exchange.
     */
    private Date creation;

    /**
     * The maximum message length for the given chat room or exchange.
     */
    private int maxMsgLen;

    /**
     * The maximum occupancy of a room.
     */
    private int maxOccupancy;

    /**
     * The name of a chat room.
     */
    private String name;

    /**
     * "Creation permissions," such as <code>CREATE_NOT_ALLOWED</code>.
     */
    private short createPerms;

    /**
     * A charset name.
     */
    private String charset1;

    /**
     * A language code.
     */
    private String language1;

    /**
     * Another charset name.
     */
    private String charset2;

    /**
     * Another language code.
     */
    private String language2;

    /** The content type string contained in this chat information block. */
    private String contentType;

    /** A value whose signficance is unknown as of this writing. */
    private int something;

    /**
     * The TLV chain that will be written upon a call to
     * <code>writeBaseInfo</code>, stored locally in case of a call to
     * <code>getBaseTlvCount</code>.
     */
    private MutableTlvChain tlvChain = null;

    /**
     * Creates an empty chat information object.
     */
    protected AbstractChatInfo() { }

    /**
     * Creates a chat information block with the given properties and a content
     * type of {@link ChatMsg#CONTENTTYPE_DEFAULT}.
     *
     * @param name the name of the chat room
     * @param charset1 the room's associated charset
     * @param language1 the room's associatd language code
     */
    protected AbstractChatInfo(String name, String charset1, String language1) {
        this(name, charset1, language1, ChatMsg.CONTENTTYPE_DEFAULT);
    }

    /**
     * Creates a chat information block with the given properties.
     *
     * @param name the name of the chat room
     * @param charset1 the room's associated charset
     * @param language1 the room's associatd language code
     * @param contentType a content type string, like {@link
     *        ChatMsg#CONTENTTYPE_DEFAULT} or {@link ChatMsg#CONTENTTYPE_SECURE}
     */
    protected AbstractChatInfo(String name, String charset1, String language1,
            String contentType) {
        this(-1, null, -1, -1, name, (short) -1, charset1, language1, null,
                null, contentType);
    }

    /**
     * Creates a chat information block with the given chat room name and no
     * other properties.
     *
     * @param name the name of the chat room
     */
    protected AbstractChatInfo(String name) {
        this(name, null, null);
    }

    /**
     * Creates a chat information block with the given properties.
     *
     * @param flags the block's associated "flags," such as
     *        <code>MASK_CAN_PEEK</code>, or <code>-1</code> for none
     * @param creation the room's creation date, or <code>null</code> for none
     * @param maxMsgLen the maximum message length in a room, or <code>-1</code>
     *        for none
     * @param maxOccupancy the maximum number of members of a room, or
     *        <code>-1</code> for none
     * @param name the name of the room, or <code>null</code> for none
     * @param createPerms the "creation permissions" of the room, such as
     *        <code>PERM_CANNOT_CREATE_ANYTHING</code>, or <code>-1</code> for
     *        none
     * @param charset1 the charset name to place in the first charset block, or
     *        <code>null</code> for none
     * @param language1 the language code (such as "en") to place in the first
     *        language block, or <code>null</code> for none
     * @param charset2 the charset name to place in the second charset block, or
     *        <code>null</code> for none
     * @param language2 the language code (such as "en") to place in the second
     *        language block, or <code>null</code> for none
     * @param contentType the "content type" of the chat room, like {@link
     *        ChatMsg#CONTENTTYPE_DEFAULT} or {@link ChatMsg#CONTENTTYPE_SECURE}
     */
    protected AbstractChatInfo(int flags, Date creation, int maxMsgLen,
            int maxOccupancy, String name, short createPerms, String charset1,
            String language1, String charset2, String language2,
            String contentType) {
        DefensiveTools.checkRange(flags, "flags", -1);
        DefensiveTools.checkRange(maxMsgLen, "maxMsgLen", -1);
        DefensiveTools.checkRange(maxOccupancy, "maxOccupancy", -1);
        DefensiveTools.checkRange(createPerms, "createPerms", -1);

        this.flags = flags;
        this.creation = creation;
        this.maxMsgLen = maxMsgLen;
        this.maxOccupancy = maxOccupancy;
        this.name = name;
        this.createPerms = createPerms;
        this.charset1 = charset1;
        this.language1 = language1;
        this.charset2 = charset2;
        this.language2 = language2;
        this.contentType = contentType;
    }

    /**
     * Reads <code>AbstractChatInfo</code> fields such as flags, maximum message
     * length, and room name, from the given TLV chain.
     *
     * @param chain the TLV chain from which to read
     */
    protected synchronized final void readBaseInfo(TlvChain chain) {
        DefensiveTools.checkNull(chain, "chain");

        flags = chain.getUShort(TYPE_FLAGS);

        Tlv creationTlv = chain.getLastTlv(TYPE_CREATION_DATE);
        if (creationTlv == null) {
            creation = null;
        } else {
            long date = creationTlv.getDataAsUInt();
            creation = date == -1 || date == 0 ? null : new Date(date * 1000);
        }

        maxMsgLen = chain.getUShort(TYPE_MAX_MSG_LEN);

        maxOccupancy = chain.getUShort(TYPE_MAX_OCCUPANCY);

        name = chain.getString(TYPE_NAME);

        Tlv createPermsTlv = chain.getLastTlv(TYPE_CREATE_PERMS);
        if (createPermsTlv == null) {
            createPerms = -1;
        } else {
            createPerms = BinaryTools.getUByte(createPermsTlv.getData(), 0);
        }

        charset1 = chain.getString(TYPE_CHARSET_1);
        language1 = chain.getString(TYPE_LANG_1);

        charset2 = chain.getString(TYPE_CHARSET_2);
        language2 = chain.getString(TYPE_LANG_2);

        contentType = chain.getString(TYPE_CONTENT_TYPE);

        something = chain.getUShort(TYPE_SOMETHING);
    }

    /**
     * Creates a TLV chain for the writing of this object to an OSCAR connection
     * if one has not already been created.
     */
    private synchronized final void ensureTlvChainExists() {
        if (tlvChain != null) {
            return;
        }

        tlvChain = TlvTools.createMutableChain();

        if (flags != -1) {
            tlvChain.addTlv(Tlv.getUShortInstance(TYPE_FLAGS, flags));
        }
        if (creation != null) {
            tlvChain.addTlv(Tlv.getUIntInstance(TYPE_CREATION_DATE,
                    creation.getTime() / 1000));
        }
        if (maxMsgLen != -1) {
            tlvChain.addTlv(Tlv.getUShortInstance(TYPE_MAX_MSG_LEN, maxMsgLen));
        }
        if (maxOccupancy != -1) {
            tlvChain.addTlv(Tlv.getUShortInstance(TYPE_MAX_OCCUPANCY,
                    maxOccupancy));
        }
        if (name != null) {
            tlvChain.addTlv(Tlv.getStringInstance(TYPE_NAME, name));
        }
        if (createPerms != -1) {
            tlvChain.addTlv(new Tlv(TYPE_CREATE_PERMS,
                    ByteBlock.wrap(BinaryTools.getUByte(createPerms))));
        }
        if (charset1 != null) {
            tlvChain.addTlv(Tlv.getStringInstance(TYPE_CHARSET_1, charset1));
        }
        if (language1 != null) {
            tlvChain.addTlv(Tlv.getStringInstance(TYPE_LANG_1, language1));
        }
        if (charset2 != null) {
            tlvChain.addTlv(Tlv.getStringInstance(TYPE_CHARSET_2, charset2));
        }
        if (language2 != null) {
            tlvChain.addTlv(Tlv.getStringInstance(TYPE_LANG_2, language2));
        }
        if (contentType != null) {
            tlvChain.addTlv(
                    Tlv.getStringInstance(TYPE_CONTENT_TYPE, contentType));
        }
        if (something != -1) {
            tlvChain.addTlv(Tlv.getUShortInstance(TYPE_SOMETHING, something));
        }
    }

    /**
     * Returns the number of TLV's that will be or have been written with a
     * call to {@link #writeBaseInfo writeBaseInfo}.
     *
     * @return the number of TLV's written by <code>writeBaseInfo</code>
     */
    protected synchronized final int getBaseTlvCount() {
        ensureTlvChainExists();

        return tlvChain.getTlvCount();
    }

    /**
     * Writes a chain of TLV's containing the <code>AbstractChatInfo</code>
     * fields, presumably to an OSCAR connection.
     *
     * @param out
     * @throws IOException
     */
    protected synchronized final void writeBaseInfo(OutputStream out)
            throws IOException {
        ensureTlvChainExists();

        tlvChain.write(out);
    }

    /**
     * Returns this chat information block's "flags." This will be a bit mask
     * possibly comprised of any of {@link #MASK_CAN_PEEK}, {@link
     * #MASK_INSTANCING_ALLOWED}, {@link #MASK_NAV_ONLY}, and {@link
     * #MASK_WARNABLE}.
     *
     * @return the "flags" bit mask of this chat information block
     */
    public synchronized final int getFlags() {
        return flags;
    }

    /**
     * Returns the date at which the associated chat room or exchange was
     * created.
     *
     * @return the creation date of the associated chat room or exchange
     */
    public synchronized final Date getCreationDate() {
        return creation;
    }

    /**
     * Returns the maximum length of a message sent in the associated chat room
     * or rooms on the associated exchange.
     *
     * @return the associated maximum chat message length
     */
    public synchronized final int getMaxMsgLen() {
        return maxMsgLen;
    }

    /**
     * Returns the maximum number of members of the associated chat room or of
     * rooms on the associated exchange.
     *
     * @return the associated maximum chat room capacity
     */
    public synchronized final int getMaxOccupancy() {
        return maxOccupancy;
    }

    /**
     * Returns the name of the associated chat room.
     *
     * @return the name of the associated chat room
     */
    public synchronized final String getName() {
        return name;
    }

    /**
     * Returns this chat information block's associated "creation permission"
     * value. This should be one of {@link #PERM_CANNOT_CREATE_ANYTHING}, {@link
     * #PERM_CAN_CREATE_ROOM}, and {@link #PERM_CAN_CREATE_EXCHANGE}.
     *
     * @return the creation permissions of the associated exchange
     */
    public synchronized final short getCreatePerms() {
        return createPerms;
    }

    /**
     * Returns the first of the two charset values. Note that normally only one
     * of this and {@link #getCharset2()} is non-<code>null</code>.
     *
     * @return the first charset of this chat information block
     */
    public synchronized final String getCharset1() {
        return charset1;
    }

    /**
     * Returns the first of the two language code values. Note that normally
     * only one of this and {@link #getLanguage2()} is non-<code>null</code>.
     * This is normally a two-character string such as "en" (for English).
     *
     * @return the first language code of this chat information block
     */
    public synchronized final String getLanguage1() {
        return language1;
    }

    /**
     * Returns the second of the two charset values. Note that normally only one
     * of this and {@link #getCharset1()} is non-<code>null</code>.
     *
     * @return the second charset of this chat information block
     */
    public synchronized final String getCharset2() {
        return charset2;
    }

    /**
     * Returns the second of the two language code values. Note that normally
     * only one of this and {@link #getLanguage1()} is non-<code>null</code>.
     * This is normally a two-character string such as "en" (for English).
     *
     * @return the second language code of this chat information block
     */
    public synchronized final String getLanguage2() {
        return language2;
    }

    /**
     * Returns the content type string contained in this chat information block.
     * Note that this will normally be {@link ChatMsg#CONTENTTYPE_DEFAULT} for normal
     * chat rooms and {@link ChatMsg#CONTENTTYPE_SECURE} for secure chat rooms. The
     * returned value may also be <code>null</code> if no content type string
     * was sent in this chat information block.
     *
     * @return the content type string contained in this chat information block
     */
    public synchronized final String getContentType() { return contentType; }

    /**
     * Returns a code of some sort. As of this writing I am unsure of the
     * significance of this value. Note that this value will be <code>-1</code>
     * if no such value was included in this chat information block.
     *
     * @return some sort of code
     */
    public synchronized final int getSomething() { return something; }

    public synchronized String toString() {
        return MiscTools.getClassName(this) + ": "
                + "flags=" + flags
                + ", creation=" + creation
                + ", maxMsgLen=" + maxMsgLen
                + ", maxOccupancy=" + maxOccupancy
                + ", name='" + name + "'"
                + ", createPerms=" + createPerms
                + ", charset1='" + charset1 + "'"
                + ", charset2='" + charset2 + "'"
                + ", language1='" + language1 + "'"
                + ", language2='" + language2 + "'"
                + ", contentType='" + contentType + "'"
                + ", something=" + something;
    }
}
