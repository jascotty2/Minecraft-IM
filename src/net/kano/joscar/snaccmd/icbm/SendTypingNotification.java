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
 *  File created by keith @ Mar 28, 2003
 *
 */

package net.kano.joscar.snaccmd.icbm;

import net.kano.joscar.flapcmd.SnacPacket;

/**
 * A SNAC command used to send another user an indication of the user's "typing
 * status" -- that is, information about whether the user is currently typing a
 * message.
 *
 * @snac.src client
 * @snac.cmd 0x04 0x14
 *
 * @see RecvTypingNotification
 */
public class SendTypingNotification extends TypingCmd {
    /**
     * Generates a new send-typing-notification command from the given
     * incoming SNAC packet.
     *
     * @param packet an incoming send-typing-notification packet
     */
    protected SendTypingNotification(SnacPacket packet) {
        super(CMD_SEND_TYPING, packet);
    }

    /**
     * Creates a new outgoing typing notification command to the given user
     * and with the given typing state. The values of <code>nulls</code> and
     * <code>code</code> are their defaults, {@link #NULLS_DEFAULT} and
     * {@link #CODE_DEFAULT}, respectively. Using this constructor is equivalent
     * to using {@link #SendTypingNotification(long, int, String, int) new
     * SendTypingNotification(SendTypingNotification.NULLS_DEFAULT,
     * SendTypingNotification.CODE_DEFAULT, sn, typingState)}.
     *
     * @param sn the screenname to which the user is typing
     * @param typingState a typing state, like {@link #STATE_PAUSED}
     */
    public SendTypingNotification(String sn, int typingState) {
        super(CMD_SEND_TYPING, sn, typingState);
    }

    /**
     * Creates a new outgoing typing notification command with the given
     * properties.
     *
     * @param nulls the value for the first eight bytes of the typing
     *        notification command (currently WinAIM sends {@link
     *        #NULLS_DEFAULT} (<code>0</code>))
     * @param code some sort of typing notification code (currently WinAIM sends
     *        {@link #CODE_DEFAULT})
     * @param sn the screenname to which the user is typing
     * @param typingState a typing state, like {@link #STATE_TYPING}
     */
    public SendTypingNotification(long nulls, int code, String sn,
            int typingState) {
        super(CMD_SEND_TYPING, nulls, code, sn, typingState);
    }
}
