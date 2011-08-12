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
 *  File created by keith @ Aug 25, 2003
 *
 */

package net.kano.joscar.snaccmd;

import net.kano.joscar.BinaryTools;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.LiveWritable;
import net.kano.joscar.tlv.Tlv;
import net.kano.joscar.tlv.TlvChain;
import net.kano.joscar.tlv.TlvTools;

import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Contains information about a user's security certificates. This structure
 * contains the certificates themselves as well as MD5 hashes whose significance
 * is unknown at the time of this writing.
 *
 * @see FullUserInfo#getCertInfoHash()
 * @see InfoData#getCertificateInfo()
 */
public class CertificateInfo implements LiveWritable {
    /** A default value for a "code" sent in certificate information blocks. */
    public static final int CODE_DEFAULT = 1;

    /** The MD5 hash of an empty string of bytes. */
    private static final ByteBlock HASH_DEFAULT = ByteBlock.wrap(new byte[] {
        (byte) 0xd4, 0x1d, (byte) 0x8c, (byte) 0xd9, (byte) 0x8f, 0x00,
        (byte) 0xb2, 0x04, (byte) 0xe9, (byte) 0x80, 0x09, (byte) 0x98,
        (byte) 0xec, (byte) 0xf8, 0x42, 0x7e });

    /**
     * The value used by the official AIM clients for the first MD5 hash sent in
     * certificate information blocks. Note that at the time of this writing,
     * this value is the same as {@link #HASHB_DEFAULT}.
     * <br>
     * <br>
     * At the time of this writing, this value is the MD5 hash of an empty
     * string of bytes.
     */
    public static final ByteBlock HASHA_DEFAULT = HASH_DEFAULT;
    /**
     * The value used by the official AIM clients for the second MD5 hash sent
     * in certificate information blocks. Note that at the time of this writing,
     * this value is the same as {@link #HASHA_DEFAULT}.
     * <br>
     * <br>
     * At the time of this writing, this value is the MD5 hash of an empty
     * string of bytes. 
     */
    public static final ByteBlock HASHB_DEFAULT = HASHA_DEFAULT;

    /**
     * Computes the MD5 hash of the given certificate information block. This is
     * used to identify whether one's copy of a user's certificate information
     * is up to date (see {@link FullUserInfo#getCertInfoHash()}).
     *
     * @param certInfo the certificate information block whose hash should be
     *        computed
     * @return the MD5 hash of the given certificate information block
     */
    public static byte[] getCertInfoHash(CertificateInfo certInfo) {
        DefensiveTools.checkNull(certInfo, "certInfo");

        ByteBlock data = ByteBlock.createByteBlock(certInfo);

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException impossible) { return null; }

        byte[] hash = digest.digest(data.toByteArray());

        return hash;
    }

    /**
     * Reads a certificate information block object from the given block of
     * binary data. Note that this method will never return <code>null</code>
     * but will instead return an "empty" certificate information object if no
     * certificate information data is included in the given block of data.
     *
     * @param block a block of data containing a user's certificate information
     * @return a certificate information block object
     */
    public static CertificateInfo readCertInfoBlock(ByteBlock block) {
        DefensiveTools.checkNull(block, "block");

        TlvChain chain = TlvTools.readChain(block);
        /*
        // this doesn't seem to matter
        int code = -1;
        Tlv codeTlv = chain.getLastTlv(TYPE_NUMCERTS);
        if (codeTlv != null) {
            code = codeTlv.getDataAsUShort();
        }
        */

        ByteBlock hashA = null;
        Tlv hashATlv = chain.getLastTlv(TYPE_HASH_A);
        if (hashATlv != null) {
            ByteBlock data = hashATlv.getData();
            ExtraInfoBlock infoBlock = ExtraInfoBlock.readExtraInfoBlock(data);
            if (infoBlock != null) hashA = infoBlock.getExtraData().getData();
        }

        ByteBlock hashB = null;

        Tlv hashBTlv = chain.getLastTlv(TYPE_HASH_B);
        if (hashBTlv != null) {
            ByteBlock data = hashBTlv.getData();
            ExtraInfoBlock infoBlock = ExtraInfoBlock.readExtraInfoBlock(data);
            if (infoBlock != null) hashB = infoBlock.getExtraData().getData();
        }

        ByteBlock encCertData = null;
        Tlv encCertTlv = chain.getLastTlv(TYPE_ENCCERTDATA);
        if (encCertTlv != null) encCertData = encCertTlv.getData();

        ByteBlock signCertData = null;
        Tlv signCertTlv = chain.getLastTlv(TYPE_SIGNCERTDATA);
        if (signCertTlv != null) signCertData = signCertTlv.getData();

        if (signCertData == null) {
            // the encryption cert is probably the common cert (or null)
            return new CertificateInfo(encCertData, hashA, hashB);
        } else {
            return new CertificateInfo(encCertData, signCertData, hashA, hashB);
        }
    }

    /** A TLV type containing the number of certificates in this block. */
    private static final int TYPE_NUMCERTS = 0x0004;
    /**
     * A TLV type containing the single certificate in this block, if only one
     * is present.
     */
    private static final int TYPE_COMMONCERTDATA = 0x0001;
    /** A TLV type containing the user's encryption certificate. */
    private static final int TYPE_ENCCERTDATA = 0x0001;
    /** A TLV type containing the user's signing certificate. */
    private static final int TYPE_SIGNCERTDATA = 0x0002;
    /** A TLV type containing an MD5 hash. */
    private static final int TYPE_HASH_A = 0x0005;
    /** A TLV type containing an MD5 hash. */
    private static final int TYPE_HASH_B = 0x0006;

    /** Whether or not this certificate contains a common certificate. */
    private final boolean common;
    /**
     * The certificate the user uses to sign and encrypt data, if there is only
     * one certificate present in this block.
     */
    private final ByteBlock commonCertData;
    /** The certificate the user uses to encrypt data. */
    private final ByteBlock encCertData;
    /** The certificate the user uses to sign data. */
    private final ByteBlock signCertData;
    /** An MD5 hash. */
    private final ByteBlock hashA;
    /** An MD5 hash. */
    private final ByteBlock hashB;

    /**
     * Creates a new certificate information object with the given certificate
     * used for both signing and encrypting data, and MD5 hashes of {@link
     * #HASHA_DEFAULT} and {@link #HASHB_DEFAULT}.
     * <br>
     * <br>
     * Note that <code>commonCertData</code> can be <code>null</code> to
     * indicate that no certificate should be sent.
     *
     * @param commonCertData the certificate used to sign and encrypt data
     */
    public CertificateInfo(ByteBlock commonCertData) {
        this(commonCertData, HASHA_DEFAULT, HASHB_DEFAULT);
    }

    /**
     * Creates a new certificate information object with the given certificate
     * used for both signing and encrypting data, and the given MD5 hashes.
     * <br>
     * <br>
     * Note that <code>commonCertData</code> can be <code>null</code> to
     * indicate that no certificate should be sent, as can either (or both) of
     * the hashes.
     *
     * @param commonCertData the certificate used to sign and encrypt data
     * @param hashA the first MD5 hash
     * @param hashB the second MD5 hash
     */
    public CertificateInfo(ByteBlock commonCertData, ByteBlock hashA,
            ByteBlock hashB) {
        this.common = true;
        this.commonCertData = commonCertData;
        this.encCertData = null;
        this.signCertData = null;
        this.hashA = hashA;
        this.hashB = hashB;
    }

    /**
     * Creates a new certificate information object with the given encryption
     * and signing certificates, and MD5 hashes of {@link #HASHA_DEFAULT} and
     * {@link #HASHB_DEFAULT}.
     * <br>
     * <br>
     * Note that neither <code>encCertData</code> nor <code>signCertData</code>
     * can be <code>null</code>.
     *
     * @param encCertData the certificate used for encrypting data
     * @param signCertData the certificate used for signing data
     */
    public CertificateInfo(ByteBlock encCertData, ByteBlock signCertData) {
        this(encCertData, signCertData, HASHA_DEFAULT, HASHB_DEFAULT);
    }

    /**
     * Creates a new certificate information object with the given encryption
     * and signing certificates, and the given MD5 hashes. Note that the
     * significance of the MD5 hashes is unknown at the time of this writing;
     * the official clients always send {@link #HASHA_DEFAULT} and
     * {@link #HASHB_DEFAULT}.
     * <br>
     * <br>
     * Note that neither <code>encCertData</code> nor <code>signCertData</code>
     * can be <code>null</code>. Either or both of the hashes, however, can be
     * <code>null</code> to indicate that they should not be sent.
     *
     * @param encCertData the certificate used for encrypting data
     * @param signCertData the certificate used for signing data
     * @param hashA the first MD5 hash
     * @param hashB the second MD5 hash
     */
    public CertificateInfo(ByteBlock encCertData, ByteBlock signCertData,
            ByteBlock hashA, ByteBlock hashB) {
        DefensiveTools.checkNull(encCertData, "encCertData");
        DefensiveTools.checkNull(signCertData, "signCertData");

        this.common = false;
        this.commonCertData = null;
        this.encCertData = encCertData;
        this.signCertData = signCertData;
        this.hashA = hashA;
        this.hashB = hashB;
    }

    /**
     * Returns whether this certificate information block contains a "common
     * certificate," or a certificate used for both encrypting and signing. If
     * <br>
     * <br>
     * If the returned value is <code>true</code>, both {@link
     * #getEncCertData()} and {@link #getSignCertData()} will be
     * <code>null</code>; {@link #getCommonCertData()} will probably be
     * non-<code>null</code> (although the common certificate being
     * non-<code>null</code> cannot be guaranteed).
     * <br>
     * <br>
     * If the returned value is <code>false</code>, {@link #getCommonCertData()}
     * will be <code>null</code>; in most cases, {@link #getEncCertData()} and
     * {@link #getSignCertData()} will be non-<code>null</code> (although, once
     * again, the encryption and signing certificates being
     * non-<code>null</code> cannot be guaranteed).
     *
     * @return whether or not this certificate contains a "common certificate"
     */
    public final boolean isCommon() { return common; }

    /**
     * Returns the "common certificate" stored in this certificate information
     * block, if present. Note that this method will always return
     * <code>null</code> if {@link #isCommon()} is <code>false</code>, and it
     * may return <code>null</code> even if <code>isCommon()</code> is
     * <code>true</code>, if no certificate was sent.
     *
     * @return the "common certificate" stored in this certificate information
     *         block, if any
     *
     * @see #isCommon()
     */
    public final ByteBlock getCommonCertData() { return commonCertData; }

    /**
     * Returns the encryption certificate stored in this certificate information
     * block, if present. Note that this method will always return
     * <code>null</code> if {@link #isCommon()} is <code>true</code>, and it
     * may return <code>null</code> even if <code>isCommon()</code> is
     * <code>false</code>, if no encryption certificate was sent.
     *
     * @return the encryption certificate stored in this certificate information
     *         block, if any
     *
     * @see #isCommon()
     */
    public final ByteBlock getEncCertData() { return encCertData; }

    /**
     * Returns the signing certificate stored in this certificate information
     * block, if present. Note that this method will always return
     * <code>null</code> if {@link #isCommon()} is <code>true</code>, and it
     * may return <code>null</code> even if <code>isCommon()</code> is
     * <code>false</code>, if no signing certificate was sent.
     *
     * @return the signing certificate stored in this certificate information
     *         block, if any
     *
     * @see #isCommon()
     */
    public final ByteBlock getSignCertData() { return signCertData; }

    /**
     * Returns the first MD5 hash contained in this certificate information
     * block, or <code>null</code> if none was sent. At the time of this
     * writing, the significance of this value is unknown, as it seems to always
     * be {@link #HASHA_DEFAULT}.
     *
     * @return the first MD5 hash contained in this certificate information
     *         block
     */
    public final ByteBlock getHashA() { return hashA; }

    /**
     * Returns the second MD5 hash contained in this certificate information
     * block, or <code>null</code> if none was sent. At the time of this
     * writing, the significance of this value is unknown, as it seems to always
     * be {@link #HASHB_DEFAULT}.
     *
     * @return the second MD5 hash contained in this certificate information
     *         block
     */
    public final ByteBlock getHashB() { return hashB; }

    /**
     * Writes the given MD5 hash to the given stream wrapped in the given extra
     * info block type and the given TLV type.
     * @param out the stream to which to write
     * @param tlvType the type of the TLV to write
     * @param extraInfoType the type of the extra info block to write
     * @param hash the MD5 hash itself
     *
     * @throws IOException if an I/O error occurs
     */
    private static void writeHash(OutputStream out, int tlvType,
            int extraInfoType, ByteBlock hash) throws IOException {
        ExtraInfoData data = new ExtraInfoData(
                ExtraInfoData.FLAG_HASH_PRESENT, hash);
        ExtraInfoBlock block = new ExtraInfoBlock(extraInfoType, data);
        new Tlv(tlvType, ByteBlock.createByteBlock(block)).write(out);
    }

    public void write(OutputStream out) throws IOException {
        int numCerts;
        if (common) numCerts = 1;
        else numCerts = 2;
        Tlv.getUShortInstance(TYPE_NUMCERTS, numCerts).write(out);

        if (numCerts == 1) {
            if (commonCertData != null) {
                new Tlv(TYPE_COMMONCERTDATA, commonCertData).write(out);
            }
        } else {
            new Tlv(TYPE_ENCCERTDATA, encCertData).write(out);
            new Tlv(TYPE_SIGNCERTDATA, signCertData).write(out);
        }
        if (hashA != null) {
            writeHash(out, TYPE_HASH_A, ExtraInfoBlock.TYPE_CERTINFO_HASHA,
                    hashA);
        }
        if (hashB != null) {
            writeHash(out, TYPE_HASH_B, ExtraInfoBlock.TYPE_CERTINFO_HASHB,
                    hashB);
        }
    }

    public String toString() {
        return "CertificateInfo: " + (common ? "common cert"
                + (commonCertData == null ? " (null)" : "")
                : "enc cert, signing cert");
    }
}
