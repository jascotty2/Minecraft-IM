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

import net.kano.joscar.DefensiveTools;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides an interface for grouping a set of SNAC command factories into a
 * list. I can't quite remember why I didn't just use a <code>List</code> or
 * something. :) See {@link DefaultSnacCmdFactoryList} for a simpler way to
 * use <code>SnacCmdFactoryList</code>s.
 */
public abstract class SnacCmdFactoryList {
    /**
     * The map of factories from <code>CmdType</code>s.
     */
    private Map factories = new HashMap();

    /**
      * Registers the given command factory for the given command type.
      * The factory will be added such that <code>getFactory(type) ==
      * factory</code>.
      * <br>
      * <br>
      * Note that the given factory must include the given command type in the
      * list returned by its {@link SnacCmdFactory#getSupportedTypes
      * getSupportedTypes} method.
      *
      * @param type the command type to register
      * @param factory the factory to be registered with the given command type
      */
    protected synchronized final void register(CmdType type,
            SnacCmdFactory factory) {
        DefensiveTools.checkNull(type, "type");

        if (!Arrays.asList(factory.getSupportedTypes()).contains(type)) return;

        factories.put(type, factory);
    }

    /**
     * Registers the given factory for all command types provided by its
     *  <code>getSupportedTypes</code> method.
     *
     * @param factory the factory to register
     */
    protected synchronized final void registerAll(
            SnacCmdFactory factory) {
        DefensiveTools.checkNull(factory, "factory");

        CmdType[] types = factory.getSupportedTypes();

        for (int i = 0; i < types.length; i++) {
            factories.put(types[i], factory);
        }
    }

    /**
     * Unregisters the given factory for the given SNAC command type. No change
     * will take place if the given factory is not registered for the given
     * type.
     *
     * @param type the command type for which the given factory should be
     *        unregistered
     * @param factory the factory to unregister for the given type
     */
    protected synchronized final void unregister(CmdType type,
            SnacCmdFactory factory) {
        DefensiveTools.checkNull(type, "type");

        SnacCmdFactory other = (SnacCmdFactory) factories.get(type);

        if (other == factory) factories.remove(type);
    }

    /**
     * Unregisters the given factory for all types for which it is currently
     * registered.
     *
     * @param factory the factory to completely unregister
     */
    protected synchronized final void unregisterAll(SnacCmdFactory factory) {
        DefensiveTools.checkNull(factory, "factory");

        Collection c = factories.values();

        // remove each instance of this factory
        while (c.remove(factory));
    }

    /**
     * Unregisters all SNAC factories from all registered command types.
     * Effectively resets this factory list.
     */
    protected synchronized final void unregisterAll() {
        factories.clear();
    }

    /**
     * Returns an the <code>SnacCmdFactory</code> registered for the given
     * <code>CmdType</code>. Note that this only returns exact matches, so
     * <code>getFactory(new CmdType(CmdType.ALL, CmdType.ALL))</code> will
     * return <code>null</code> if no factory has been registered specifically
     * for that command type, even though, according to the {@linkplain CmdType
     * <code>CmdType</code> specification}, <code>CmdType</code> <i>matches</i>
     * all possible command types.
     *
     * @param type the command type whose associated command factory will be
     *        returned
     * @return the command factory associated with the given command type, or
     *         <code>null</code> if none exists
     */
    public synchronized final SnacCmdFactory getFactory(CmdType type) {
        DefensiveTools.checkNull(type, "type");

        return (SnacCmdFactory) factories.get(type);
    }
}
