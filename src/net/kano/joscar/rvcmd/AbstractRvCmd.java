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
 *  File created by Keith @ 4:16:31 AM
 *
 */

package net.kano.joscar.rvcmd;

import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.snaccmd.CapabilityBlock;
import net.kano.joscar.snaccmd.icbm.RecvRvIcbm;
import net.kano.joscar.snaccmd.icbm.RvCommand;
import net.kano.joscar.tlv.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A base class for the basic rendezvous format used by all known AIM clients.
 * Provides a simpler way to read and write the RV data as a TLV chain as well
 * as for reading and writing the common "service data block." Also provides a
 * means of writing "header TLV's" before the RV-type-specific
 */
public abstract class AbstractRvCmd extends RvCommand {
    /** A TLV type containing a "service data" block. */
    private static final int TYPE_SERVICE_DATA = 0x2711;

    /** This RV command's service data block. */
    private final ByteBlock serviceData;
    /** The TLV's contained in the rendezvous data block. */
    private final MutableTlvChain rvTlvs;

    /**
     * Creates a new RV command from the given incoming rendezvous ICBM command.
     *
     * @param icbm an incoming rendezvous ICBM command
     */
    protected AbstractRvCmd(RecvRvIcbm icbm) {
        super(icbm);

        ByteBlock block = icbm.getRvData();

        if (block != null) {
            TlvChain chain = TlvTools.readChain(block);

            Tlv serviceDataTlv = chain.getLastTlv(TYPE_SERVICE_DATA);
            if (serviceDataTlv == null) serviceData = null;
            else serviceData = serviceDataTlv.getData();

            MutableTlvChain extras = TlvTools.getMutableCopy(chain);
            extras.removeTlvs(new int[] {
                TYPE_SERVICE_DATA
            });

            rvTlvs = extras;
        } else {
            serviceData = null;
            rvTlvs = TlvTools.createMutableChain();
        }
    }

    /**
     * Creates a new outgoing RV command with the given properties.
     *
     * @param rvStatus the rendezvous status code for this command
     * @param cap the capability block ("rendezvous type") of this RV command
     */
    protected AbstractRvCmd(int rvStatus, CapabilityBlock cap) {
        super(rvStatus, cap);

        serviceData = null;
        rvTlvs = null;
    }

    /**
     * Returns the "service data" block contained in this command, if any.
     * "Service data" is the data in the type <code>0x2711</code> rendezvous
     * TLV. Note that this will be <code>null</code> if this command was not
     * read from an incoming <code>RecvRvIcbm</code>. This method will also
     * return <code>null</code> if this command was read in from an incoming
     * RV ICBM but contained no service data block.
     *
     * @return this RV command's "service data block," if any
     */
    protected final ByteBlock getServiceData() { return serviceData; }

    /**
     * Returns the TLV's contained in this rendezvous command's RV data block.
     * This value will be <code>null</code> if this rendezvous command was not
     * read from an incoming <code>RecvRvIcbm</code> but was instead created
     * manually. This command will not otherwise return <code>null</code> for
     * reasons of ease of use. Note that The returned TLV chain may not contain
     * all of the original TLV's that were received in the incoming RV ICBM; for
     * example, the returned chain will not contain TLV's of type
     * <code>0x2711</code> as that value is extracted and can be retrieved with
     * a call to {@link #getServiceData}.
     *
     * @return this RV command's RV TLV chain, if any
     */
    protected final TlvChain getRvTlvs() { return rvTlvs; }

    /**
     * Returns a mutable version of the chain of TLV's contained in this RV
     * command's RV data block. Subclasses with access to this chain are
     * permitted to modify it, though modifying the chain after initial creation
     * is not recommended. Note that this value will be <code>null</code> if
     * and only if {@link #getRvTlvs} returns <code>null</code>; see that
     * method's documentation for details.
     *
     * @return a mutable version of the TLV chain that contains the rendezvous
     *         TLV's in this RV command
     *
     * @see #getRvTlvs
     */
    final MutableTlvChain getMutableTlvs() { return rvTlvs; }

    public final void writeRvData(OutputStream out) throws IOException {
        DefensiveTools.checkNull(out, "out");

        writeHeaderRvTlvs(out);
        writeRvTlvs(out);

        if (hasServiceData()) {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            writeServiceData(bout);

            ByteBlock serviceBlock = ByteBlock.wrap(bout.toByteArray());

            new Tlv(TYPE_SERVICE_DATA, serviceBlock).write(out);
        }
    }

    /**
     * Writes this RV command's "header TLV's" to the given stream. This method
     * will always be called prior to any call to {@link #writeRvTlvs}; it
     * provides a means for subclasses to write a set of TLV's that are present
     * in all subclasses.
     *
     * @param out the stream to which to write
     *
     * @throws IOException if an I/O error occurs
     */
    protected abstract void writeHeaderRvTlvs(OutputStream out)
            throws IOException;

    /**
     * Writes this RV command's list of rendezvous TLV's to the given stream.
     *
     * @param out the stream to which to write
     *
     * @throws IOException if an I/O error occurs
     */
    protected abstract void writeRvTlvs(OutputStream out) throws IOException;

    /**
     * Returns whether this RV command's TLV list should contain a "service data
     * block" TLV. The return value of this method must be consistent throughout
     * this object's existence. Note that if this method returns
     * <code>false</code> for an object, <code>writeServiceData</code> will
     * never be called on that object. The default implementation of this method
     * returns <code>true</code>.
     *
     * @return whether or not this RV command has a "service data block"
     *
     * @see #writeServiceData
     */
    protected boolean hasServiceData() { return true; }

    /**
     * Writes this RV command's "service data block." A service data block's
     * format changes from rendezvous to rendezvous, but generally provides
     * information specific to the type of rendezvous being sent. For example,
     * in a file transfer request, the file's name and size (among other fields)
     * are sent in the service block. This method will never be called if
     * {@link #hasServiceData} returns <code>false</code>. The default
     * implementation of this method does not write any data to the stream.
     *
     * @param out the stream to which to write
     *
     * @throws IOException if an I/O error occurs
     */
    protected void writeServiceData(OutputStream out) throws IOException { }
}