/*
 * Copyright (c) 2025. Jo Rabin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.linguafranca.pwdb.format;

import org.linguafranca.pwdb.Credentials;
import org.linguafranca.pwdb.StreamConfiguration;
import org.linguafranca.pwdb.security.*;

import javax.crypto.Mac;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.linguafranca.pwdb.security.Encryption.getSha256MessageDigestInstance;

/**
 * This class represents the header portion of a KeePass KDBX file or stream. The header is received in
 * plain text and describes the encryption and compression of the remainder of the file.
 * <p>
 * In V4 the header became Outer Header and Inner Header. The class stores the configuration
 * contents of both and binary attachments.
 * <p>
 * It is a factory for encryption and decryption streams. It provides for verification of its own serialization.
 * <p>
 * While KDBX streams are Little-Endian, data is passed to and from this class in standard Java byte order.
 */
@SuppressWarnings("WeakerAccess")
public class KdbxHeader implements StreamConfiguration {
    /**
     * The ordinal 0 represents uncompressed and 1 GZip compressed
     */
    @SuppressWarnings("WeakerAccess")
    public enum CompressionFlags {
        NONE, GZIP
    }

    private final List<Integer> allowableVersions = new ArrayList<>(Arrays.asList(3, 4));


    /* major version of the file - most significant 2 bytes of the file version, e.g. 3 in 0x00030001 */
    private int version;
    /* minor version of the file - least significant 2 bytes of the file version, e.g. 1 in 0x00030001 */
    private int minorVersion;
    /* for version 4, whether the minor version is chosen when writing, as the lowest that holds the content */
    private boolean minorVersionAutomatic;

    protected UUID cipherUuid;
    private byte[] masterSeed;
    private byte[] encryptionIv;

    /* whether the data is compressed */
    private CompressionFlags compressionFlags;

    /* V3 fields */
    private byte[] transformSeed;
    private long transformRounds;

    /* header (V3) inner header (v4) */
    private byte[] innerRandomStreamKey;
    private Encryption.ProtectedStreamAlgorithm protectedStreamAlgorithm;
    private CipherAlgorithm cipherAlgorithm;
    private KeyDerivationFunction keyDerivationFunction;

    /* these bytes appear in cipher text immediately following the header (V3) */
    private byte[] streamStartBytes;

    /* dictionaries in V4 */
    private VariantDictionary kdfParameters;
    // TODO implement V4 custom data
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private VariantDictionary customData;

    /*
    * binaries in V4
    * first byte, if set to 1 indicates "protected" remainder is the payload
    */
    List<byte[]> binaries = new ArrayList<>();

    /* V3 not transmitted as part of the header, used in the XML payload, so calculated
     * on transmission or receipt */
    private byte[] headerHash;

    /* the bytes that compose the outer header, required for V4 to calculate the HMac */
    private byte[] headerBytes;

    // Make static and try to use SHA1PRNG per
    // https://stackoverflow.com/questions/137212/how-to-deal-with-a-slow-securerandom-generator
    // and comment on issue #12. If you don't like this then you can of course set this
    // to something else
    public static SecureRandom random;
    static {
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            random = new SecureRandom();
        }
    }
    /**
     * Provides for choice of version number and crypto options for V3 and V4
     */
    interface KdbxHeaderOptions {
        int getVersion();
        int getMinorVersion();
        CipherAlgorithm getCipherAlgorithm();
        KeyDerivationFunction getKeyDerivationFunction();
        Encryption.ProtectedStreamAlgorithm getProtectedStreamAlgorithm();
    }

    /**
     * Default values for crypto options
     */
    public enum KdbxHeaderOpts implements KdbxHeaderOptions{
        /** KDBX 3.1 */
        V3_AES_SALSA_20(3, 1, Encryption.Cipher.AES, Encryption.KeyDerivationFunction.AES, Encryption.ProtectedStreamAlgorithm.SALSA_20),
        /** KDBX 4.0 */
        V4_AES_ARGON_CHA_CHA (4, 0, Encryption.Cipher.AES, Encryption.KeyDerivationFunction.ARGON2, Encryption.ProtectedStreamAlgorithm.CHA_CHA_20),
        /** KDBX 4.1 */
        V4_1_AES_ARGON_CHA_CHA (4, 1, Encryption.Cipher.AES, Encryption.KeyDerivationFunction.ARGON2, Encryption.ProtectedStreamAlgorithm.CHA_CHA_20);

        //<editor-fold desc="Fields, Getters and Setters for this class">
        final int version;
        final int minorVersion;
        final CipherAlgorithm algorithm;
        final KeyDerivationFunction kdf;
        final Encryption.ProtectedStreamAlgorithm protectedStreamAlgorithm;


        KdbxHeaderOpts(int version, int minorVersion, Encryption.Cipher cipher, Encryption.KeyDerivationFunction kdf, Encryption.ProtectedStreamAlgorithm protectedStreamAlgorithm) {
            this.version = version;
            this.minorVersion = minorVersion;
            this.algorithm = cipher;
            this.kdf = kdf;
            this.protectedStreamAlgorithm = protectedStreamAlgorithm;
        }

        @Override
        public int getVersion() {
            return version;
        }

        @Override
        public int getMinorVersion() {
            return minorVersion;
        }

        @Override
        public CipherAlgorithm getCipherAlgorithm() {
            return algorithm;
        }

        @Override
        public KeyDerivationFunction getKeyDerivationFunction() {
            return kdf;
        }

        @Override
        public Encryption.ProtectedStreamAlgorithm getProtectedStreamAlgorithm() {
            return protectedStreamAlgorithm;
        }
        //</editor-fold>
    }

    /**
     * Construct a default version 3 KDBX header
     */
    public KdbxHeader(){
        this(KdbxHeaderOpts.V3_AES_SALSA_20);
    }

    /**
     * Construct a default KDBX header for the major version given: 3.1 with AES/AES/SALSA_20, or 4 with
     * AES/ARGON2/CHA_CHA_20, written as 4.1 if the content needs it and otherwise as 4.0, as KeePass does
     * @see #isMinorVersionAutomatic()
     */
    public KdbxHeader(int version) {
        this(version ==3 ? KdbxHeaderOpts.V3_AES_SALSA_20 : KdbxHeaderOpts.V4_AES_ARGON_CHA_CHA);
        this.minorVersionAutomatic = version != 3;
    }


    public KdbxHeader(KdbxHeaderOptions opts) {
        this.version = opts.getVersion();
        this.minorVersion = opts.getMinorVersion();
        setCipherAlgorithm(opts.getCipherAlgorithm());
        setKeyDerivationFunction(opts.getKeyDerivationFunction());
        setProtectedStreamAlgorithm(opts.getProtectedStreamAlgorithm());
        cipherUuid = opts.getCipherAlgorithm().getCipherUuid();

        compressionFlags = CompressionFlags.GZIP;
        masterSeed = random.generateSeed(32);
        transformSeed = random.generateSeed(32);
        transformRounds = 6000;
        encryptionIv = random.generateSeed(16);
        innerRandomStreamKey = random.generateSeed(32);
        streamStartBytes = new byte[32];
    }

    /**
     * Compute the Hmac Key Digest
     * from "KdbxFile.cs Computekeys"
     *
     * @param credentials the credentials
     * @return the digest
     */
    public byte[] getHmacKey(Credentials credentials) {
        MessageDigest md = Encryption.getSha512MessageDigestInstance();
        md.update(getMasterSeed());
        md.update(getTransformedKeyDigest(credentials.getKey()));
        return md.digest(new byte[]{1});
    }

    /**
     * Verify the header Hmac
     *
     * @param key   the transformed Hmac Key for the header
     * @param bytes the bytes to compare to verify
     */
    public void verifyHeaderHmac(byte[] key, byte[] bytes) {
        Mac mac = Encryption.getHMacSha256Instance(key);
        byte[] computedHmacSha256 = mac.doFinal(getHeaderBytes());
        if (!Arrays.equals(computedHmacSha256, bytes)) {
            throw new IllegalStateException("Header HMAC does not match");
        }
    }

    /**
     * Create a decrypted input stream using supplied digest and this header
     * apply decryption to the passed encrypted input stream
     *
     * @param digest      the key digest
     * @param inputStream the encrypted input stream
     * @return a decrypted stream
     */
    public InputStream createDecryptedStream(byte[] digest, InputStream inputStream) {
        // return digest of master seed and hash
        MessageDigest md = getSha256MessageDigestInstance();
        md.update(masterSeed);
        byte[] finalKeyDigest = md.digest(getTransformedKeyDigest(digest));
        CipherAlgorithm ca = Encryption.Cipher.getCipherAlgorithm(cipherUuid);
        return ca.getDecryptedInputStream(inputStream, finalKeyDigest, encryptionIv);
    }

/*    public StreamEncryptor getInnerStreamEncryptor() {
        return Encryption.ProtectedStreamAlgorithm.getStreamEncryptor(getProtectedStreamAlgorithm(), getInnerRandomStreamKey());
    }*/

    /**
     * Takes the composite credentials and transforms them according to the underlying KDF algorithm.
     * @param digest the credentials digested
     * @return the transformed digest
     */
    public byte[] getTransformedKeyDigest(byte[] digest) {
        // v3 doesn't have a kdf therefore AES
        if (kdfParameters == null) {
            return Aes.getTransformedKey(digest, transformSeed, transformRounds);
        }
        KeyDerivationFunction kdf = Encryption.KeyDerivationFunction.getKdf(kdfParameters.mustGet("$UUID").asUuid());
        return kdf.getTransformedKey(digest, kdfParameters);
    }

    /**
     * Create an unencrypted outputStream using the supplied digest and this header
     * and use the supplied output stream to write encrypted data.
     *
     * @param digest       the key digest
     * @param outputStream the output stream which is the destination for encrypted data
     * @return an output stream to write unencrypted data to
     */
    public OutputStream createEncryptedStream(byte[] digest, OutputStream outputStream) {
        // return digest of master seed and hash
        MessageDigest md = getSha256MessageDigestInstance();
        md.update(masterSeed);
        byte[] finalKeyDigest = md.digest(getTransformedKeyDigest(digest));
        return cipherAlgorithm.getEncryptedOutputStream(outputStream, finalKeyDigest, getEncryptionIv());
    }

    //<editor-fold desc="Getters/ Setters">
    public byte[] getTransformSeed() {
        if (version < 4) {
            return transformSeed;
        }
        return kdfParameters.mustGet(Aes.KdfKeys.ParamSeed).asByteArray();
    }

    public long getTransformRounds() {
        if (version < 4) {
            return transformRounds;
        }
        return kdfParameters.mustGet(Aes.KdfKeys.ParamRounds).asLong();
    }

    public UUID getCipherUuid() {
        return cipherUuid;
    }

    public CompressionFlags getCompressionFlags() {
        return compressionFlags;
    }

    public byte[] getMasterSeed() {
        return masterSeed;
    }

    public byte[] getEncryptionIv() {
        return encryptionIv;
    }

    public byte[] getInnerRandomStreamKey() {
        return innerRandomStreamKey;
    }

    public byte[] getStreamStartBytes() {
        return streamStartBytes;
    }

    public CipherAlgorithm getCipherAlgorithm() {
        return cipherAlgorithm;
    }

    public KeyDerivationFunction getKeyDerivationFunction() {
        return keyDerivationFunction;
    }

    public Encryption.ProtectedStreamAlgorithm getProtectedStreamAlgorithm() {
        return protectedStreamAlgorithm;
    }

    public byte[] getHeaderHash() {
        return headerHash;
    }

    /**
     * @return the major version, 3 or 4
     */
    public int getVersion() {
        return version;
    }

    /**
     * @return the minor version, e.g. 1 for KDBX 4.1
     * @since 3.1.0
     */
    public int getMinorVersion() {
        return minorVersion;
    }

    // V4
    public VariantDictionary getKdfParameters() {
        return kdfParameters;
    }

    public StreamEncryptor getInnerStreamEncryptor() {
        return Encryption.ProtectedStreamAlgorithm.getStreamEncryptor(getProtectedStreamAlgorithm(), this.innerRandomStreamKey);
    }

    public void setCompressionFlags(int flags) {
        this.compressionFlags = CompressionFlags.values()[flags];
    }

    public void setMasterSeed(byte[] masterSeed) {
        this.masterSeed = masterSeed;
    }

    public void setTransformSeed(byte[] transformSeed) {
        this.transformSeed = transformSeed;
    }

    public void setTransformRounds(long transformRounds) {
        this.transformRounds = transformRounds;
    }

    public void setEncryptionIv(byte[] encryptionIv) {
        this.encryptionIv = encryptionIv;
    }

    public void setInnerRandomStreamKey(byte[] key) {
        this.innerRandomStreamKey = key;
    }

    public void setStreamStartBytes(byte[] streamStartBytes) {
        this.streamStartBytes = streamStartBytes;
    }

    public void setInnerRandomStreamId(int innerRandomStreamId) {
        this.protectedStreamAlgorithm = Encryption.ProtectedStreamAlgorithm.getAlgorithm(innerRandomStreamId);
    }

    public void setProtectedStreamAlgorithm(Encryption.ProtectedStreamAlgorithm protectedStreamAlgorithm) {
        this.protectedStreamAlgorithm = protectedStreamAlgorithm;
    }

    public void setKeyDerivationFunction(KeyDerivationFunction keyDerivationFunction) {
        this.keyDerivationFunction = keyDerivationFunction;
        if (version > 3) {
            kdfParameters = this.keyDerivationFunction.createKdfParameters();
        }
    }

    public void setCipherAlgorithm(CipherAlgorithm cipherAlgorithm) {
        this.cipherAlgorithm = cipherAlgorithm;
        setCipherUuid(this.cipherAlgorithm.getCipherUuid());
        if (cipherAlgorithm.getName().equals("CHA_CHA_20")){
            encryptionIv = random.generateSeed(12);
        }
    }
    public void setCipherUuid(byte[] uuid) {
        ByteBuffer b = ByteBuffer.wrap(uuid);
        UUID incoming = new UUID(b.getLong(), b.getLong(8));
        setCipherUuid(incoming);
        this.cipherAlgorithm = Encryption.Cipher.getCipherAlgorithm(incoming);
    }

    public void setCipherUuid(UUID uuid) {
        if (!uuid.equals(Aes.getInstance().getCipherUuid()) && !uuid.equals(ChaCha.getInstance().getCipherUuid())) {
            throw new IllegalStateException("Unknown Cipher UUID " + uuid);
        }
        this.cipherUuid = uuid;
    }

    public void setHeaderHash(byte[] headerHash) {
        this.headerHash = headerHash;
    }

    /**
     * Sets the major version. For 3 the minor version is 1, for 4 it is chosen when writing
     * (see {@link #isMinorVersionAutomatic()})
     * @param version 3 or 4
     */
    public void setVersion(int version) {
        if (!allowableVersions.contains(version)) {
            throw new IllegalStateException("File version must be in " + allowableVersions);
        }
        this.version = version;
        this.minorVersion = version == 3 ? 1 : 0;
        this.minorVersionAutomatic = version != 3;
    }

    /**
     * Sets the minor version, e.g. 0 for KDBX 4.0, which is then written whatever the content
     * @param minorVersion the minor version
     * @since 3.1.0
     */
    public void setMinorVersion(int minorVersion) {
        this.minorVersion = minorVersion;
        this.minorVersionAutomatic = false;
    }

    /**
     * Whether the minor version is chosen when writing, as the lowest that holds the content
     * (4.1 if any 4.1 features are used, otherwise 4.0), as KeePass does. True for headers made with
     * {@link #KdbxHeader(int)} or {@link #setVersion(int)} for version 4; false after
     * {@link #setMinorVersion(int)}, for headers made from {@link KdbxHeaderOpts}, and for headers that
     * were read, so that a database keeps its version.
     * @since 3.1.0
     */
    public boolean isMinorVersionAutomatic() {
        return minorVersionAutomatic;
    }

    /* choose the minor version when writing, leaving it automatic */
    void chooseMinorVersion(int minorVersion) {
        this.minorVersion = minorVersion;
    }

    /**
     * V4 add Key Definition Function Parameters
     */
    public void setKdfParameters(VariantDictionary kdfParameters) {
        this.kdfParameters = kdfParameters;
        this.keyDerivationFunction = Encryption.KeyDerivationFunction.getKdf(kdfParameters.get("$UUID").asUuid());
    }

    /**
     * V4 Add custom data
     */
    public void setCustomData(VariantDictionary customData) {
        this.customData = customData;
    }

    /**
     * V4 add binary from inner header
     */
    public void addBinary(byte[] bytes) {
        binaries.add(bytes);
    }

    public List<byte[]> getBinaries() {
        return binaries;
    }

    /**
     * V4 provide access to the header as bytes for verification
     */
    public byte[] getHeaderBytes() {
        return headerBytes;
    }

    /**
     * V4 provide access to the header as bytes for verification
     */
    public void setHeaderBytes(byte[] headerBytes) {
        byte[] copy = new byte[headerBytes.length];
        System.arraycopy(headerBytes, 0, copy, 0, headerBytes.length);
        this.headerBytes = copy;
    }
    //</editor-fold>
}
