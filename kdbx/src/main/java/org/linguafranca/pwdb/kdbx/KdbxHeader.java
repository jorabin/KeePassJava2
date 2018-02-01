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

import org.linguafranca.pwdb.security.*;

import java.io.IOException;
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
 *
 * <p>It is a factory for encryption and decryption streams and contains a hash of its own serialization.
 *
 * <p>While KDBX streams are Little-Endian, data is passed to and from this class in standard Java byte order.
 *
 * @author jo
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
        NONE, ARC_FOUR, SALSA_20, CHA_CHA_20
    }

    /**
     * UUIDs of Ciphers for encryption
     */
    public static final UUID AES_CIPHER = UUID.fromString("31C1F2E6-BF71-4350-BE58-05216AFC5AFF");
    public static final UUID CHACHA_CIPHER = UUID.fromString("d6038a2b-8b6f-4cb5-a524-339a31dbb59a");

    private List<Integer> allowableVersions = new ArrayList<>(Arrays.asList(3,4));


    /* version of the file */
    private int version;

    protected UUID cipherUuid;
    private byte [] masterSeed;
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
    private VariantDictionary kdfparameters;
    private VariantDictionary customData;

    /* not transmitted as part of the header, used in the XML payload, so calculated
     * on transmission or receipt */
    private byte[] headerHash;

    private byte[] headerBytes;

    /**
     * Construct a default KDBX header
     */
    public KdbxHeader() {
        SecureRandom random = new SecureRandom();
        cipherUuid = AES_CIPHER;
        compressionFlags = CompressionFlags.GZIP;
        masterSeed = random.generateSeed(32);
        transformSeed = random.generateSeed(32);
        transformRounds = 6000;
        encryptionIv = random.generateSeed(16);
        innerRandomStreamKey = random.generateSeed(32);
        streamStartBytes = new byte[32];
        protectedStreamAlgorithm = ProtectedStreamAlgorithm.SALSA_20;
        version = 3;
    }

    /**
     * Create a decrypted input stream using supplied digest and this header
     * apply decryption to the passed encrypted input stream
     *
     * @param digest the key digest
     * @param inputStream the encrypted input stream
     * @return a decrypted stream
     * @throws IOException if something bad happens
     */
    public InputStream createDecryptedStream(byte[] digest, InputStream inputStream) throws IOException {
        // return digest of master seed and hash
        MessageDigest md = getSha256MessageDigestInstance();
        md.update(masterSeed);
        byte[] finalKeyDigest = md.digest(getTransformedKeyDigest(digest));

        if (AES_CIPHER.equals(cipherUuid)) {
            return Encryption.getDecryptedInputStream(inputStream, Aes.getCipher(), finalKeyDigest, getEncryptionIv());
        } else if (CHACHA_CIPHER.equals(cipherUuid)) {
            return Encryption.getDecryptedInputStream(inputStream, ChaCha.getCipher(), finalKeyDigest, getEncryptionIv());
        }
        throw new UnsupportedOperationException("Unknown encryption cipher " + cipherUuid);
    }

    public StreamEncryptor getInnerStreamEncryptor () {
        return getVersion() == 4 ?
                new StreamEncryptor.ChaCha20(getInnerRandomStreamKey()) :
                new StreamEncryptor.Salsa20(getInnerRandomStreamKey());
    }

    public byte[] getTransformedKeyDigest(byte[] digest) {
        byte[] transformedKeyDigest;

        UUID kdf = null;
        if (kdfparameters != null) {
            kdf = kdfparameters.get("$UUID").asUuid();
        }
        // v3 doesn't have a kdf therefore AES
        if (kdf == null || Aes.KDF.equals(kdf)){
            transformedKeyDigest = Aes.getTransformedKey(digest, getTransformSeed(), getTransformRounds());
        } else if (Argon.argon2_kdf.equals(kdf)) {
            transformedKeyDigest = Argon.getTransformedKey(digest, kdfparameters);
        } else {
            throw new UnsupportedOperationException("Unknown transform KDF " + kdf);
        }
        return transformedKeyDigest;
    }

    /**
     * Create an unencrypted outputstream using the supplied digest and this header
     * and use the supplied output stream to write encrypted data.
     * @param digest the key digest
     * @param outputStream the output stream which is the destination for encrypted data
     * @return an output stream to write unencrypted data to
     * @throws IOException  if something bad happens
     */
    public OutputStream createEncryptedStream(byte[] digest, OutputStream outputStream) throws IOException {
        // return digest of master seed and hash
        MessageDigest md = getSha256MessageDigestInstance();
        md.update(masterSeed);
        byte[] finalKeyDigest = md.digest(getTransformedKeyDigest(digest));
        return Encryption.getEncryptedOutputStream(outputStream, finalKeyDigest, getEncryptionIv());
    }

    public byte[] getTransformSeed() {
        if (version < 4) {
            return transformSeed;
        }
        return kdfparameters.get(Aes.KdfKeys.ParamSeed).asByteArray();
    }

    public long getTransformRounds() {
        if (version < 4) {
            return transformRounds;
        }
        return kdfparameters.get(Aes.KdfKeys.ParamRounds).asLong();
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
            case NONE: {throw new IllegalStateException("Inner stream encoding of NONE");}
            case ARC_FOUR: {throw new UnsupportedOperationException("Arc Four inner stream not supported");}
            case SALSA_20: {return new StreamEncryptor.Salsa20(this.innerRandomStreamKey);}
            case CHA_CHA_20: {return new StreamEncryptor.ChaCha20(this.innerRandomStreamKey);}
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
        this.protectedStreamAlgorithm = ProtectedStreamAlgorithm.values()[innerRandomStreamId];
    }

    public void setHeaderHash(byte[] headerHash) {
        this.headerHash = headerHash;
    }

    public void setCipherUuid(byte[] uuid) {
        ByteBuffer b = ByteBuffer.wrap(uuid);
        UUID incoming = new UUID(b.getLong(), b.getLong(8));
        if (!incoming.equals(AES_CIPHER) && !incoming.equals(CHACHA_CIPHER)) {
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

    public void setKdfparameters(VariantDictionary kdfparameters) {
        this.kdfparameters = kdfparameters;
    }

    public void setCustomData(VariantDictionary customData) {
        this.customData = customData;
    }

    public void addBinary(byte[] bytes) {

    }
    public byte[] getHeaderBytes() {
        return headerBytes;
    }

    public void setHeaderBytes(byte[] headerBytes) {
        byte [] copy = new byte[headerBytes.length];
        System.arraycopy(headerBytes, 0, copy, 0, headerBytes.length);
        this.headerBytes = copy;
    }
}
