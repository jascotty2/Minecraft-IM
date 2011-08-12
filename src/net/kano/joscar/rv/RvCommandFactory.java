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
 *  File created by keith @ Apr 24, 2003
 *
 */

package net.kano.joscar.rv;

import net.kano.joscar.snaccmd.CapabilityBlock;
import net.kano.joscar.snaccmd.icbm.RecvRvIcbm;
import net.kano.joscar.snaccmd.icbm.RvCommand;

/**
 * An interface for producing <code>RvCommand</code>s from incoming rendezvous
 * ICBM commands.
 */
public interface RvCommandFactory {
    /**
     * Returns the types of RV commands that this factory may be able to
     * generate in <code>genRvCommand</code>. Note that a capability block's
     * presence in the returned array does not mean that a call to {@link
     * #genRvCommand} must return a non-<code>null</code> value; it simply means
     * that this factory can be used to handle commands of that type.
     * <br>
     * <br>
     * Note that if this method returns <code>null</code>, it will be used to
     * by the <code>RvProcessor</code> to which it is added to handle all types
     * of commands do not otherwise have an associated factory. See {@link
     * RvProcessor#registerRvCmdFactory(CapabilityBlock, RvCommandFactory)} for
     * details.
     *
     * @return the capabilities (RV types) that this factory may be able to
     *         convert, or <code>null</code> if it can handle all types of
     *         rendezvous commands
     */
    CapabilityBlock[] getSupportedCapabilities();

    /**
     * Attempts to generate a <code>RvCommand</code> from the data in the given
     * <code>RecvRvIcbm</code>. Note that this method can return
     * <code>null</code> if an RV command cannot be generated for any reason
     * (such as if the given command is in an invalid format
     *
     * @param rvIcbm the incoming rendezvous ICBM command from which a
     *        <code>RvCommand</code> should be generated
     * @return a <code>RvCommand</code> generated from the given incoming RV
     *         ICBM, or <code>null</code> if none could be generated
     */
    RvCommand genRvCommand(RecvRvIcbm rvIcbm);
}
