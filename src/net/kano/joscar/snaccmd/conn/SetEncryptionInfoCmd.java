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
 *  File created by keith @ Aug 5, 2003
 *
 */

package net.kano.joscar.snaccmd.conn;

import net.kano.joscar.flapcmd.SnacPacket;
import net.kano.joscar.snaccmd.ExtraInfoBlock;

/**
 * A SNAC command used for setting some sort of security information. As of this
 * writing, the significance of this command is unknown. This command is
 * normally responded-to with a {@link EncryptionInfoAck} which contains the
 * same extra info blocks sent in this command.
 * <br>
 * <br>
 * When sent by the official AIM clients, this command normally contains two
 * extra info blocks, the first of type {@link
 * ExtraInfoBlock#TYPE_CERTINFO_HASHA} and the second of type {@link
 * ExtraInfoBlock#TYPE_CERTINFO_HASHB}. They normally contain {@link
 * net.kano.joscar.snaccmd.CertificateInfo#HASHA_DEFAULT} and {@link
 * net.kano.joscar.snaccmd.CertificateInfo#HASHB_DEFAULT}, respectively.
 *
 * @snac.src client
 * @snac.cmd 0x01 0x22
 *
 * @see EncryptionInfoAck
 * @see ExtraInfoBlock
 * @see net.kano.joscar.snaccmd.CertificateInfo
 */
public class SetEncryptionInfoCmd extends AbstractExtraInfoCmd {
    /**
     * Creates a new set-encryption-info command from the given incoming
     * set-encryption-info SNAC packet.
     *
     * @param packet an incoming set-encryption-info SNAC packet
     */
    protected SetEncryptionInfoCmd(SnacPacket packet) {
        super(CMD_SETENCINFO, packet);
    }

    /**
     * Creates a new outgoing set-encryption-info command with the given list of
     * extra info blocks. Note that neither <code>blocks</code> nor any of its
     * elements can be <code>null</code>.
     *
     * @param blocks the list of extra info blocks to send in this command
     */
    public SetEncryptionInfoCmd(ExtraInfoBlock[] blocks) {
        super(CMD_SETENCINFO, blocks);
    }
}
