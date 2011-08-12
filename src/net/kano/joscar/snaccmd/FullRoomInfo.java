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
import net.kano.joscar.LiveWritable;
import net.kano.joscar.snaccmd.chat.ChatMsg;
import net.kano.joscar.tlv.Tlv;
import net.kano.joscar.tlv.TlvChain;
import net.kano.joscar.tlv.TlvTools;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

/**
 * A data structure containing details about an OSCAR chat room. These details
 * include the {@linkplain ExchangeInfo exchange} on which the room resides,
 * and possibly a "cookie" to be passed to a {@linkplain
 * net.kano.joscar.snaccmd.conn.ServiceRequest service request} to join the
 * room.
 */
public final class FullRoomInfo
        extends AbstractChatInfo implements LiveWritable {
    /**
     * Represents the "last instance" of a given room. It is recommended to use
     * this as the "instance" field when joining a room; as of this writing,
     * I am unsure of what happens if you do not. If someone wants to test this
     * for me I'd be grateful :)
     */
    public static final int INSTANCE_LAST = 0xffff;

    /**
     * Reads a room information block from the given data block.
     *
     * @param block the data block from which to read the room information
     * @return a room information block read from the given data block
     */
    public static FullRoomInfo readRoomInfo(ByteBlock block) {
        return new FullRoomInfo(block);
    }
    /**
     * A TLV type containing the room name.
     */
    private static final int TYPE_ROOM_NAME = 0x006a;

    /** A default value for the "code" sent in full room information blocks. */
    private static final int CODE_DEFAULT = 0x0000;

    /**
     * A <code>FullRoomInfo</code> is just a <code>MiniRoomInfo</code> with some
     * extra fields, so we store some stuff in a mini room info object.
     */
    private MiniRoomInfo mini = null;

    /**
     * The chat exchange on which this room exists.
     */
    private final int exchange;

    /**
     * A message cookie that can be passed to a service redirect request.
     */
    private final String cookie;

    /**
     * The "instance number" of this chat room. As of this writing I'm unsure
     * as to this value's significance, aside from the fact that it should be
     * {@link #INSTANCE_LAST}
     */
    private final int instance;

    /**
     * The type of chat room information block, like <code>TYPE_SHORT</code>.
     */
    private final int type;

    /** Some sort of code contained in this room information block. */
    private final int code;

    /**
     * The name of the room.
     */
    private final String roomName;

    /**
     * Generates a room info object from the given block.
     *
     * @param block the block of data containing a room info object
     */
    protected FullRoomInfo(ByteBlock block) {
        DefensiveTools.checkNull(block, "block");

        mini = MiniRoomInfo.readMiniRoomInfo(block);

        exchange = mini.getExchange();
        cookie = mini.getCookie();
        instance = mini.getInstance();

        ByteBlock rest = block.subBlock(mini.getTotalSize());

        type = BinaryTools.getUByte(rest, 0);
        code = BinaryTools.getUShort(rest, 1);

        ByteBlock roomBlock = rest.subBlock(3);

        TlvChain chain = TlvTools.readChain(roomBlock);

        // TLV userlistTlv = chain.getLastTlv(TYPE_MEMBERS);

        roomName = chain.getString(TYPE_ROOM_NAME);

        readBaseInfo(chain);
    }

    /**
     * Creates a chat room information object with the given properties. The
     * associated charset is set to <code>"us-ascii"</code> and the language
     * is set to the JVM's current default language. Using this constructor is
     * equivalent to using {@link #FullRoomInfo(int, String, String, String) new
     * FullRoomInfo(exchange, name, "us-ascii",
     * Locale.getDefault().getLanguage())}.
     *
     * @param exchange the exchange on which the chat room resides
     * @param name the name of the chat room
     */
    public FullRoomInfo(int exchange, String name) {
        this(exchange, name, "us-ascii", Locale.getDefault().getLanguage());
    }

    /**
     * Creates a chat room information object with the given properties. This
     * constructor is useful for passing to {@link
     * net.kano.joscar.snaccmd.rooms.JoinRoomCmd}s.
     *
     * @param exchange the exchange on which the chat room resides
     * @param name the name of the chat room
     * @param charset1 the charset associated with the given room
     * @param language1 the language associated with the given room, normally
     *        a two-letter language code like "en" (for English)
     */
    public FullRoomInfo(int exchange, String name, String charset1,
            String language1) {
        this(exchange, name, charset1, language1, null);
    }

    /**
     * Creates a chat room information object with the given properties. This
     * constructor is useful for passing to {@link
     * net.kano.joscar.snaccmd.rooms.JoinRoomCmd}s.
     *
     * @param exchange the exchange on which the chat room resides
     * @param name the name of the chat room
     * @param charset1 the charset associated with the given room
     * @param language1 the language associated with the given room, normally
     *        a two-letter language code like "en" (for English)
     * @param contentType a content type string, like {@link ChatMsg#CONTENTTYPE_DEFAULT}
     */
    public FullRoomInfo(int exchange, String name, String charset1,
            String language1, String contentType) {
        super(name, charset1, language1, contentType);

        DefensiveTools.checkRange(exchange, "exchange", 0);

        this.exchange = exchange;
        this.cookie = "create";
        this.instance = INSTANCE_LAST;
        this.type = TYPE_SHORT;
        this.code = CODE_DEFAULT;
        this.roomName = null;
    }

    /**
     * Returns the number of the exchange on which this room resides.
     *
     * @return this room's associated exchange number
     */
    public final int getExchange() {
        return exchange;
    }

    /**
     * The {@linkplain net.kano.joscar.snaccmd.conn.ServiceRequest service
     * request} cookie associated with this room. Only sent when a full room
     * info object is a part of a response to a {@link
     * net.kano.joscar.snaccmd.rooms.JoinRoomCmd}.
     *
     * @return a cookie to use when joining this room
     */
    public final String getCookie() {
        return cookie;
    }

    /**
     * Returns the "instance" of the chat room. As of this writing I am unsure
     * of the significance of this value.
     *
     * @return the chat room's "instance" value
     */
    public final int getInstance() {
        return instance;
    }

    /**
     * Returns the type of room information block that this object represents.
     * Is normally one of {@link #TYPE_SHORT}, {@link #TYPE_FULL}, {@link
     * #TYPE_NAV_INSTANCE_INFO}, {@link #TYPE_NAV_SHORT_DESC}, and {@link
     * #TYPE_INSTANCE_INFO}.
     *
     * @return the type of room information block that this object represents
     */
    public final int getType() {
        return type;
    }
    /**
     * The full name of the chat room.
     *
     * @return the chat room's name
     */
    public final String getRoomName() {
        return roomName;
    }

    public void write(OutputStream out) throws IOException {
        if (mini == null) mini = new MiniRoomInfo(this);
        mini.write(out);

        BinaryTools.writeUByte(out, type);
        BinaryTools.writeUShort(out, code);

        writeBaseInfo(out);
        if (roomName != null) {
            Tlv.getStringInstance(TYPE_ROOM_NAME, roomName).write(out);
        }
    }

    public String toString() {
        return "FullRoomInfo: " +
                "exchange=" + exchange +
                ", cookie=" + cookie +
                ", instance=" + instance +
                ", type=" + type +
                ", roomname=" + roomName +
                " - " + super.toString();
    }
}
