/*
 * Copyright 2015 Jo Rabin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.linguafranca.pwdb.kdbx;

import org.linguafranca.pwdb.Credentials;
import org.linguafranca.pwdb.security.*;

import javax.crypto.Mac;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
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
 * <p>It is a factory for encryption and decryption streams. It provides for verification of its own serialization.
 * <p>
 * <p>While KDBX streams are Little-Endian, data is passed to and from this class in standard Java byte order.
 */
@SuppressWarnings("WeakerAccess")
public class KdbxHeader {

    /**
     * The ordinal 0 represents uncompressed and 1 GZip compressed
     */
    @SuppressWarnings("WeakerAccess")
    public enum CompressionFlags {
        NONE, GZIP
    }

    /**
     * The ordinals represent various types of encryption that may
     * be applied to fields within the unencrypted data
     *
     * @see StreamFormat
     * @see KdbxStreamFormat
     */
    @SuppressWarnings("WeakerAccess, unused")
    public enum ProtectedStreamAlgorithm {
        NONE(0), ARC_FOUR(1), SALSA_20(2), CHA_CHA_20(3);

        private int value;

        ProtectedStreamAlgorithm(int value) {
            this.value = value;
        }

        public static ProtectedStreamAlgorithm getAlgorithm(int innerRandomStreamId) {
            for (ProtectedStreamAlgorithm pse: values()) {
                if (pse.value == innerRandomStreamId) {
                    return pse;
                }
            }
            throw new IllegalArgumentException("Inner Random Stream Id " + innerRandomStreamId + "is not known");
        }
    }

    private List<Integer> allowableVersions = new ArrayList<>(Arrays.asList(3, 4));


    /* version of the file */
    private int version;

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
    private ProtectedStreamAlgorithm protectedStreamAlgorithm;

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

    /**
     * Construct a default KDBX header
     */
    public KdbxHeader() {
        this(3);
    }

    public KdbxHeader(int version) {
        SecureRandom random = new SecureRandom();

        this.version = version;
        cipherUuid = Aes.getInstance().getCipherUuid();
        compressionFlags = CompressionFlags.GZIP;
        masterSeed = random.generateSeed(32);
        transformSeed = random.generateSeed(32);
        transformRounds = 6000;
        encryptionIv = random.generateSeed(16);
        innerRandomStreamKey = random.generateSeed(32);
        streamStartBytes = new byte[32];
        protectedStreamAlgorithm = ProtectedStreamAlgorithm.SALSA_20;

        kdfParameters = Aes.createKdfParameters();
    }

    /**
     * Compute the Hmac Key Digest
     * KdbxFile.cs Computekeys
     *
     * @param credentials the credentials
     * @return the digest
     */
    public byte[] getHmacKey(Credentials credentials) {
        // Compute the Hmac Key Digest
        // KdbxFile.cs Computekeys
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

    // Alternative implementation of above using bouncy castle
    /*
        HMac hmac = new HMac(new SHA256Digest());
        hmac.init(new KeyParameter(hmacKey64));
        hmac.update(kdbxHeader.getHeaderBytes(), 0, kdbxHeader.getHeaderBytes().length);
        byte[] computedHmacSha256 = new byte[32];
        hmac.doFinal(computedHmacSha256, 0);
     */

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

    public StreamEncryptor getInnerStreamEncryptor() {
        return getVersion() == 4 ?
                new StreamEncryptor.ChaCha20(getInnerRandomStreamKey()) :
                new StreamEncryptor.Salsa20(getInnerRandomStreamKey());
    }

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
        KeyDerivationFunction kdf = Encryption.Kdf.getKdf(kdfParameters.mustGet("$UUID").asUuid());
        return kdf.getTransformedKey(digest, kdfParameters);
    }

    /**
     * Create an unencrypted outputstream using the supplied digest and this header
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
        return Aes.getInstance().getEncryptedOutputStream(outputStream, finalKeyDigest, getEncryptionIv());
    }

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

    public ProtectedStreamAlgorithm getProtectedStreamAlgorithm() {
        return protectedStreamAlgorithm;
    }

    public byte[] getHeaderHash() {
        return headerHash;
    }

    public int getVersion() {
        return version;
    }

    public StreamEncryptor getStreamEncryptor() {
        switch (getProtectedStreamAlgorithm()) {
            case NONE: {
                throw new IllegalStateException("Inner stream encoding of NONE");
            }
            case ARC_FOUR: {
                throw new UnsupportedOperationException("Arc Four inner stream not supported");
            }
            case SALSA_20: {
                return new StreamEncryptor.Salsa20(this.innerRandomStreamKey);
            }
            case CHA_CHA_20: {
                return new StreamEncryptor.ChaCha20(this.innerRandomStreamKey);
            }
        }
        throw new IllegalStateException("Inner stream encoding unsupported");
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
        this.protectedStreamAlgorithm = ProtectedStreamAlgorithm.getAlgorithm(innerRandomStreamId);
    }

    public void setHeaderHash(byte[] headerHash) {
        this.headerHash = headerHash;
    }

    public void setCipherUuid(byte[] uuid) {
        ByteBuffer b = ByteBuffer.wrap(uuid);
        UUID incoming = new UUID(b.getLong(), b.getLong(8));
        if (!incoming.equals(Aes.getInstance().getCipherUuid()) && !incoming.equals(ChaCha.getInstance().getCipherUuid())) {
            throw new IllegalStateException("Unknown Cipher UUID " + incoming.toString());
        }
        this.cipherUuid = incoming;
    }

    public void setVersion(int version) {
        if (!allowableVersions.contains(version)) {
            throw new IllegalStateException("File version must be in " + allowableVersions.toString());
        }
        this.version = version;
    }

    /**
     * V4 add Key Definition Function Parameters
     */
    public void setKdfParameters(VariantDictionary kdfParameters) {
        this.kdfParameters = kdfParameters;
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
}
