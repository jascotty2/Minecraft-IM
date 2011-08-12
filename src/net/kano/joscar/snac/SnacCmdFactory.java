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
 *  File created by keith @ Feb 18, 2003
 *
 */

package net.kano.joscar.snac;

import net.kano.joscar.flapcmd.SnacCommand;
import net.kano.joscar.flapcmd.SnacPacket;

/**
 * Provides an interface for generating <code>SnacCommand</code>s from
 * <code>SnacPacket</code>s.
 */
public interface SnacCmdFactory {
    /**
     * Returns a list of the SNAC command types this factory can possibly
     * convert to <code>SnacCommand</code>s. Note that it is not required to
     * be able to convert every SNAC packet that matches the types returned by
     * this method; rather, this just provides a means of filtering out types
     * that can definitely not be handled (by not including them in the returned
     * list).
     * <br>
     * <br>
     * Also note that <b>the command types contained in the list returned must
     * be consistent between calls to this method</b>; that is, an
     * implementation cannot change the supported command type list after this
     * factory has been created.
     *
     * @return a list of command types that can be passed to
     *         <code>genSnacCommand</code>
     */
    CmdType[] getSupportedTypes();

    /**
     * Attempts to convert the given SNAC packet to a <code>SnacCommand</code>.
     * This can return <code>null</code> if no appropriate
     * <code>SnacCommand</code> can be created (for example, if the packet is in
     * an invalid format).
     *
     * @param packet the packet to use for generation of a
     *        <code>SnacCommand</code>
     * @return an appropriate <code>SnacCommand</code> for representing the
     *         given <code>SnacPacket</code>, or <code>null</code> if no such
     *         object can be created
     */
    SnacCommand genSnacCommand(SnacPacket packet);
}
