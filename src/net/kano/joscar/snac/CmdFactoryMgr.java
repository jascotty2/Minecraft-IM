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
 *  File created by keith @ Feb 19, 2003
 *
 */

package net.kano.joscar.snac;

/**
 * Provides a central means of producing <code>SnacCommand</code>s from
 * <code>SnacPacket</code>s by merging a "default factory list" with the
 * factories registered by the user of SNAC processor in a logical manner.
 * <br>
 * <br>
 * This class effectively handles several different "levels" of SNAC factories,
 * digging deeper and deeper until an appropriate factory for a given command
 * type is found. When attempting to find a SNAC command factory for a given
 * command type, the following takes place:
 * 
 * <ol>
 * <li> If a command factory has been registered for this exact command type,
 * that factory is returned </li>
 *
 * <li> Otherwise, if a command factory has been registered for the entire
 * family (using a command type of <code>CmdType.CMDTYPE_ALL</code>), that factory
 * is returned </li>
 *
 * <li> Otherwise, if a command factory has been registered for all command
 * types (using family <i>and</i> command types of
 * <code>CmdType.CMDTYPE_ALL</code>), that factory is returned </li>
 *
 * <li> Otherwise, a similar three-step process occurs using the <i>default
 * factory list</i> specified by <code>setDefaultFactoryList</code> </li>
 * </ol>
 * For more details on how this class is used, see {@link ClientSnacProcessor}.
 */
public final class CmdFactoryMgr extends SnacCmdFactoryList {
    /**
     * The "default factory list," or a list of factories used when no
     * user-registered factories match a given command type.
     */
    private SnacCmdFactoryList defaultFactories;

    /**
     * Creates a new command factory manager with no default factories.
     *
     * @see #setDefaultFactoryList
     */
    CmdFactoryMgr() { }

    /**
     * Sets the default factory list for this factory manager. See {@linkplain
     * CmdFactoryMgr above} for details.
     *
     * @param list the new default factory list
     */
    public synchronized final void setDefaultFactoryList(
            SnacCmdFactoryList list) {
        this.defaultFactories = list;
    }

    /**
     * Returns this command factory manager's the default SNAC command factory
     * list.
     *
     * @return this object's default factory list
     */
    public synchronized final SnacCmdFactoryList getDefaultFactoryList() {
        return defaultFactories;
    }

    /**
     * Searches several levels of factory lists to come up with an appropriate
     * command factory for the given SNAC command type. See {@linkplain
     * CmdFactoryMgr above} for details.
     *
     * @param type the command type for which a factory must be found
     * @return an appropriate command factory for the given type
     */
    synchronized final SnacCmdFactory findFactory(CmdType type) {
        // see if there's a usert r factory for this command
        SnacCmdFactory factory = getFactory(type);

        if (factory != null) return factory;

        // see if there's a user factory for this family
        CmdType familyCmd = new CmdType(type.getFamily());
        factory = getFactory(familyCmd);

        if (factory != null) return factory;

        if (defaultFactories != null) {
            // see if there's a default factory for this command
            factory = defaultFactories.getFactory(type);

            if (factory != null) return factory;

            // and if there's a default factory for this family
            factory = defaultFactories.getFactory(familyCmd);
        }

        if (factory != null) return factory;

        // and then if there's a catchall factory set by the user
        factory = getFactory(CmdType.CMDTYPE_ALL);

        if (factory != null) return factory;

        if (defaultFactories != null) {
            // and then if there's a default catchall factory
            factory = defaultFactories.getFactory(CmdType.CMDTYPE_ALL);
        }

        // it doesn't matter if it's null anymore, man. it just doesn't matter.
        return factory;
    }
}
