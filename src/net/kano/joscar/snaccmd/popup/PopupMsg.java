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
 *  File created by keith @ Feb 23, 2003
 *
 */

package net.kano.joscar.snaccmd.popup;

import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.flapcmd.SnacPacket;
import net.kano.joscar.tlv.ImmutableTlvChain;
import net.kano.joscar.tlv.Tlv;
import net.kano.joscar.tlv.TlvChain;
import net.kano.joscar.tlv.TlvTools;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A SNAC command used to pop up a message on the user's screen. I have never
 * seen this command used and thus, the documentation for it is rather vague.
 *
 * @snac.src server
 * @snac.cmd 0x08 0x02
 */
public class PopupMsg extends PopupCommand {
    /** A TLV type containing the popup message. */
    private static final int TYPE_MSG = 0x0001;
    /** A TLV type containing a URL. */
    private static final int TYPE_URL = 0x0002;
    /** A TLV type containing the desired width of the popup window. */
    private static final int TYPE_WIDTH = 0x0003;
    /** A TLV type containing the desired height of the popup window. */
    private static final int TYPE_HEIGHT = 0x0004;
    /** A TLV type containing a delay after which the popup should be shown. */
    private static final int TYPE_DELAY = 0x0005;

    /** The message to display in the popup window. */
    private final String message;
    /** Some sort of URL. */
    private final String url;
    /** The width of the popup window. */
    private final int width;
    /** The height of the popup window. */
    private final int height;
    /** Some sort of delay. */
    private final int delay;

    /**
     * Generates a popup message command from the given incoming SNAC packet.
     *
     * @param packet an incoming popup message packet
     */
    protected PopupMsg(SnacPacket packet) {
        super(CMD_POPUP_MSG);

        DefensiveTools.checkNull(packet, "packet");

        ByteBlock snacData = packet.getData();

        TlvChain chain = TlvTools.readChain(snacData);

        message = chain.getString(TYPE_MSG);
        url = chain.getString(TYPE_URL);
        width = chain.getUShort(TYPE_WIDTH);
        height = chain.getUShort(TYPE_HEIGHT);
        delay = chain.getUShort(TYPE_DELAY);
    }

    /**
     * Creates a new outgoing popup message command with the given properties.
     *
     * @param message the message to pop up, presumably in AOLRTF (HTML) format
     * @param url some sort of URL related to the message
     * @param width the width of the popup window, in pixels
     * @param height the height of the popup window, in pixels
     * @param delay some sort of delay before showing the window
     */
    public PopupMsg(String message, String url, int width, int height,
            int delay) {
        super(CMD_POPUP_MSG);

        this.message = message;
        this.url = url;
        this.width = width;
        this.height = height;
        this.delay = delay;
    }

    /**
     * Returns the message to pop up, presumably in AOLRTF (HTML) format.
     *
     * @return the message to pop up
     */
    public final String getMessage() {
        return message;
    }

    /**
     * Returns some sort of URL related to the popup message.
     *
     * @return the URL associated with this message
     */
    public final String getUrl() {
        return url;
    }

    /**
     * Returns the width, in pixels, of the window to pop up.
     *
     * @return the width, in pixels, of the popup window to be displayed
     */
    public final int getWidth() {
        return width;
    }

    /**
     * Returns the height, in pixels, of the window to pop up.
     *
     * @return the height, in pixels, of the popup window to be displayed
     */
    public final int getHeight() {
        return height;
    }

    /**
     * Returns some sort of delay before popping up the messsage. I do not know
     * what sort of units this value is in, as I have never seen this command
     * used.
     *
     * @return some sort of delay related to this popup message
     */
    public final int getDelay() {
        return delay;
    }

    public void writeData(OutputStream out) throws IOException {
        if (message != null) {
            Tlv.getStringInstance(TYPE_MSG, message).write(out);
        }
        if (url != null) {
            Tlv.getStringInstance(TYPE_URL, url).write(out);
        }
        if (width != -1) {
            Tlv.getUShortInstance(TYPE_WIDTH, width).write(out);
        }
        if (height != -1) {
            Tlv.getUShortInstance(TYPE_HEIGHT, height).write(out);
        }
        if (delay != -1) {
            Tlv.getUShortInstance(TYPE_DELAY, delay).write(out);
        }
    }

    public String toString() {
        return "PopupMsg (" + width + " x " + height + ", delay=" + delay
                + "): " + message + " (" + url + ")";
    }
}
