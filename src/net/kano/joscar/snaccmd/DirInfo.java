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

package net.kano.joscar.snaccmd;

import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.LiveWritable;
import net.kano.joscar.MinimalEncoder;
import net.kano.joscar.tlv.ImmutableTlvChain;
import net.kano.joscar.tlv.Tlv;
import net.kano.joscar.tlv.TlvChain;
import net.kano.joscar.tlv.TlvTools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Represents a set of "directory information," used for searching for buddies
 * by various fields.
 */
public final class DirInfo implements LiveWritable {
    /**
     * Reads a directory information entry from the given block. Equivalent to
     * calling {@link #readDirInfo(net.kano.joscar.tlv.TlvChain)
     * readDirInfo(TlvChain.readChain(block))}. The total number of bytes read
     * can be retrieved by calling <code>getTotalSize</code> on the returned
     * object.
     *
     * @param block the data block containing directory information
     * @return a directory information object generated from the data in the
     *         given block
     */
    public static DirInfo readDirInfo(ByteBlock block) {
        return readDirInfo(TlvTools.readChain(block));
    }

    /**
     * Reads a directory information block from the given block, only reading
     * the given number of TLV's from the block. (Directory info blocks consist
     * of a series of TLV's.) Using this method is equivalent to calling
     * {@link #readDirInfo(net.kano.joscar.tlv.TlvChain) readDirInfo(TlvChain.readChain(block,
     * maxTlvs))}. The total number of bytes read can be retrieved by calling
     * <code>getTotalSize</code> on the returned object.
     *
     * @param block the block of data from which to read
     * @param maxTlvs the maximum number of TLV's to read from the block
     * @return a directory information object
     */
    public static DirInfo readDirInfo(ByteBlock block, int maxTlvs) {
        return readDirInfo(TlvTools.readChain(block, maxTlvs));
    }

    /**
     * Generates a <code>DirInfo</code> object from the TLV's in the given
     * chain. (A directory info block consists solely of a series of TLV's.)
     * Note that if no TLV's are present in the given chain, <code>null</code>
     * is returned.
     *
     * @param chain the TLV chain containing directory information TLV's
     * @return a directory information object generated from the TLV's in the
     *         given chain
     */
    public static DirInfo readDirInfo(TlvChain chain) {
        DefensiveTools.checkNull(chain, "chain");

        if (chain.getTlvCount() == 0) return null;

        String charset = chain.getString(TYPE_CHARSET);

        String sn = chain.getString(TYPE_SN, charset);
        String email = chain.getString(TYPE_EMAIL, charset);

        String first = chain.getString(TYPE_FIRSTNAME, charset);
        String middle = chain.getString(TYPE_MIDDLENAME, charset);
        String last = chain.getString(TYPE_LASTNAME, charset);
        String nickname = chain.getString(TYPE_NICKNAME, charset);
        String maiden = chain.getString(TYPE_MAIDENNAME, charset);

        String address = chain.getString(TYPE_ADDRESS, charset);
        String city = chain.getString(TYPE_CITY, charset);
        String state = chain.getString(TYPE_STATE, charset);
        String zip = chain.getString(TYPE_ZIP, charset);
        String country = chain.getString(TYPE_COUNTRY, charset);

        String language = chain.getString(TYPE_LANGUAGE, charset);

        return new DirInfo(sn, email, first, middle, last, maiden, nickname,
                address, city, state, zip, country, language,
                chain.getTotalSize());
    }

    /**
     * A TLV type containing the charset of the strings contained in this block.
     */
    private static final int TYPE_CHARSET = 0x0018;

    /**
     * A TLV type containing the screenname holding this directory info.
     */
    private static final int TYPE_SN = 0x0009;

    /**
     * A TLV type containing an email address.
     */
    private static final int TYPE_EMAIL = 0x0005;

    /**
     * A TLV type containing a first name.
     */
    private static final int TYPE_FIRSTNAME = 0x0001;

    /**
     * A TLV type containing a middle name.
     */
    private static final int TYPE_MIDDLENAME = 0x0003;

    /**
     * A TLV type containing a last name.
     */
    private static final int TYPE_LASTNAME = 0x0002;

    /**
     * A TLV type containing a maiden name.
     */
    private static final int TYPE_MAIDENNAME = 0x0004;

    /**
     * A TLV type containing a nickname.
     */
    private static final int TYPE_NICKNAME = 0x000c;

    /**
     * A TLV type containing a street address.
     */
    private static final int TYPE_ADDRESS = 0x0021;

    /**
     * A TLV type containing a city of residence.
     */
    private static final int TYPE_CITY = 0x0008;

    /**
     * A TLV type containing a state of residence.
     */
    private static final int TYPE_STATE = 0x0007;

    /**
     * A TLV type containing a zip code.
     */
    private static final int TYPE_ZIP = 0x000d;

    /**
     * A TLV type containing a two-letter country code.
     */
    private static final int TYPE_COUNTRY = 0x0006;

    /**
     * A TLV type containing a two-letter language code.
     */
    private static final int TYPE_LANGUAGE = 0x000f;

    /**
     * The screenname holding this directory info.
     */
    private final String sn;

    /** An email address. */
    private final String email;

    /** A first name. */
    private final String first;

    /** A middle name. */
    private final String middle;

    /** A last name. */
    private final String last;

    /** A maiden name. */
    private final String maiden;

    /** A nickname. */
    private final String nickname;

    /** A street address. */
    private final String address;

    /** A city name. */
    private final String city;

    /** A state name or abbreviation. */
    private final String state;

    /** A zip code. */
    private final String zip;

    /** A two-letter country code. */
    private final String country;

    /** A two-letter language code. */
    private final String language;

    /**
     * The total size, in bytes, that this structure took up when read using
     * any form of <code>readDirInfo</code>.
     */
    private final int totalSize;

    /**
     * A byte block holding the contents of this directory info block. Created
     * lazily upon a call to <code>write</code>.
     */
    private ByteBlock block = null;

    /**
     * The number of TLV's that have been or are to be written by
     * <code>write</code>.
     */
    private int tlvCount = -1;

    /**
     * Creates a new directory info object with the given properties. Any of
     * these fields can be <code>null</code> to avoid using the given field or
     * to indicate that the given field was not included in the directory info
     * block.
     *
     * @param sn the screenname having this directory information
     * @param email the email address of this user
     * @param first this user's first name
     * @param middle the user's middle name
     * @param last the user's last name
     * @param maiden the user's maiden name
     * @param nickname the user's nickname
     * @param address the user's street address
     * @param city the user's city of residence
     * @param state the user's state of residence, in any format ("NY" and "New
     *        York" are both valid)
     * @param zip the user's zip code
     * @param country the user's country of residence, as a two-letter country
     *        code
     * @param language the user's language, as a two-letter code such as "en"
     *        (for English)
     * @param totalSize the total size of the directory information block, as
     *        read from some sort of stream or data block
     */
    protected DirInfo(String sn, String email, String first, String middle,
            String last, String maiden, String nickname, String address,
            String city, String state, String zip, String country,
            String language, int totalSize) {

        DefensiveTools.checkRange(totalSize, "totalSize", -1);

        this.sn = sn;
        this.email = email;
        this.first = first;
        this.middle = middle;
        this.last = last;
        this.maiden = maiden;
        this.nickname = nickname;
        this.address = address;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.country = country;
        this.language = language;
        this.totalSize = totalSize;
    }

    /**
     * Creates a new directory info object with the given properties and no
     * associated screenname. This constructor should be used to create
     * directory info blocks to be sent to the server by a client, as no
     * screenname field is used in such a case. Any of these fields can be
     * <code>null</code> to avoid sending the given field.
     *
     * @param email the email address of this user
     * @param first this user's first name
     * @param middle the user's middle name
     * @param last the user's last name
     * @param maiden the user's maiden name
     * @param nickname the user's nickname
     * @param address the user's street address
     * @param city the user's city of residence
     * @param state the user's state of residence, in any format ("NY" and "New
     *        York" are both valid)
     * @param zip the user's zip code
     * @param country the user's country of residence, as a two-letter country
     *        code
     * @param language the user's language, as a two-letter code such as "en"
     *        (for English)
     */
    public DirInfo(String email, String first, String middle,
            String last, String maiden, String nickname, String address,
            String city, String state, String zip, String country,
            String language) {
        this(null, email, first, middle, last, maiden, nickname, address, city,
                state, zip, country, language, -1);
    }
    /**
     * Creates a new directory info object with the given properties. Any of
     * these fields can be <code>null</code> to avoid sending the given field.
     *
     * @param sn the screenname holding this directory information
     * @param email the email address of this user
     * @param first this user's first name
     * @param middle the user's middle name
     * @param last the user's last name
     * @param maiden the user's maiden name
     * @param nickname the user's nickname
     * @param address the user's street address
     * @param city the user's city of residence
     * @param state the user's state of residence, in any format ("NY" and "New
     *        York" are both valid)
     * @param zip the user's zip code
     * @param country the user's country of residence, as a two-letter country
     *        code
     * @param language the user's language, as a two-letter code such as "en"
     *        (for English)
     */
    public DirInfo(String sn, String email, String first, String middle,
            String last, String maiden, String nickname, String address,
            String city, String state, String zip, String country,
            String language) {
        this(sn, email, first, middle, last, maiden, nickname, address, city,
                state, zip, country, language, -1);
    }

    /**
     * Returns the screenname holding this directory information.
     *
     * @return this directory information's associated screenname
     */
    public final String getScreenname() {
        return sn;
    }

    /**
     * Returns the email address field of this directory info block.
     *
     * @return this directory info block's associated email address
     */
    public final String getEmail() {
        return email;
    }

    /**
     * Returns the first-name field of this directory info block.
     *
     * @return this directory info block's associated first name
     */
    public final String getFirstname() {
        return first;
    }

    /**
     * Returns the middle-name field of this directory info block.
     *
     * @return this directory info block's associated middle name
     */
    public final String getMiddlename() {
        return middle;
    }

    /**
     * Returns the last-name field of this directory info block.
     *
     * @return this directory info block's associated last name
     */
    public final String getLastname() {
        return last;
    }

    /**
     * Returns the maiden-name field of this directory info block.
     *
     * @return this directory info block's associated maiden name
     */
    public final String getMaiden() {
        return maiden;
    }

    /**
     * Returns the nickname field of this directory info block.
     *
     * @return this directory info block's associated nickname
     */
    public final String getNickname() {
        return nickname;
    }

    /**
     * Returns the street-address field of this directory info block.
     *
     * @return this directory info block's associated street address
     */
    public final String getStreetAddress() {
        return address;
    }

    /**
     * Returns the city of residence field of this directory info block.
     *
     * @return this directory info block's associated city name
     */
    public final String getCity() {
        return city;
    }

    /**
     * Returns the state field of this directory info block. This may be in
     * "NY" format or "New York" format, or actually any other format, as WinAIM
     * allows the user to simply type it in.
     *
     * @return this directory info block's associated state of residence
     */
    public final String getState() {
        return state;
    }

    /**
     * Returns the zip-code field of this directory info block.
     *
     * @return this directory info block's associated zip code
     */
    public final String getZip() {
        return zip;
    }

    /**
     * Returns the country-code field of this directory info block. This will
     * be a two-letter abbreviation like "US" or "CA".
     *
     * @return this directory info block's associated country code
     */
    public final String getCountryCode() {
        return country;
    }

    /**
     * Returns the language-code field of this directory info block. This will
     * be in a format like "en" (representing English).
     *
     * @return this directory info block's associated language code
     */
    public final String getLanguageCode() {
        return language;
    }

    /**
     * Returns the total number of bytes this directory information object
     * used when read from a byte block. Will be <code>-1</code> if this info
     * block was not read using any of the <code>readDirInfo</code> forms.
     *
     * @return the total size, in bytes, of this object, if read from a block
     *         of data
     */
    public final int getTotalSize() {
        return totalSize;
    }

    /**
     * Writes the given string to the given output stream in a TLV of the given
     * type, encoding using the given minimal encoder.
     *
     * @param out the stream to which to write
     * @param type the type of TLV to write
     * @param str the string to write
     * @param encoder the minimal encoder to use to encode the given string to
     *        bytes
     * @return <code>true</code> if a TLV was written to the stream;
     *         false otherwise
     * @throws IOException if an I/O error occurs
     */
    private static boolean writeString(OutputStream out, int type, String str,
            MinimalEncoder encoder)
            throws IOException {
        if (str != null) {
            new Tlv(type, ByteBlock.wrap(encoder.encode(str).getData()))
                    .write(out);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Writes this directory object to the given stream, presumably to a
     * SNAC packet data block of sorts.
     *
     * @param out the stream to write to
     * @throws IOException if an I/O exception occurs
     */
    private void writeTo(OutputStream out) throws IOException {
        int count = 0;

        MinimalEncoder menc = new MinimalEncoder();

        menc.updateAll(new String[] {
            sn, email, first, middle, last, maiden, nickname, address, city,
            state, zip, country, language
        });

        Tlv.getStringInstance(TYPE_CHARSET, menc.getCharset()).write(out);
        count++;

        if (writeString(out, TYPE_SN, sn, menc)) count++;
        if (writeString(out, TYPE_EMAIL, email, menc)) count++;

        if (writeString(out, TYPE_FIRSTNAME, first, menc)) count++;
        if (writeString(out, TYPE_MIDDLENAME, middle, menc)) count++;
        if (writeString(out, TYPE_LASTNAME, last, menc)) count++;
        if (writeString(out, TYPE_MAIDENNAME, maiden, menc)) count++;
        if (writeString(out, TYPE_NICKNAME, nickname, menc)) count++;

        if (writeString(out, TYPE_ADDRESS, address, menc)) count++;
        if (writeString(out, TYPE_CITY, city, menc)) count++;
        if (writeString(out, TYPE_STATE, state, menc)) count++;
        if (writeString(out, TYPE_ZIP, zip, menc)) count++;
        if (writeString(out, TYPE_COUNTRY, country, menc)) count++;

        if (writeString(out, TYPE_LANGUAGE, language, menc)) count++;

        tlvCount = count;
    }

    /**
     * Makes sure <code>block</code> is not <code>null</code>, storing a binary
     * representation of this object in the variable if necessary.
     */
    private void ensureBlockExists() {
        if (block != null && tlvCount != -1) return;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            writeTo(out);
        } catch (IOException impossible) { }

        block = ByteBlock.wrap(out.toByteArray());
    }

    /**
     * Returns the total number of TLV's that will be or have been written using
     * <code>write</code>.
     *
     * @return the total number of TLV's that will be or have been written by
     *         invoking <code>write</code>
     */
    public final int getTlvCount() {
        if (tlvCount == -1) ensureBlockExists();

        return tlvCount;
    }

    public void write(OutputStream out) throws IOException {
        if (block == null) writeTo(out);
        else block.write(out);
    }

    public String toString() {
        return "DirInfo for " + sn + " (email=" + email + "): " +
                "first='" + first + "'" +
                ", middle='" + middle + "'" +
                ", last='" + last + "'" +
                ", maiden='" + maiden + "'" +
                ", nickname='" + nickname + "'" +
                ", address='" + address + "'" +
                ", city='" + city + "'" +
                ", state='" + state + "'" +
                ", zip='" + zip + "'" +
                ", country='" + country + "'" +
                ", language='" + language + "'";
    }

}
