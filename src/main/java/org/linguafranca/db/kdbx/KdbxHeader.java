package org.linguafranca.db.kdbx;

import org.linguafranca.security.Encryption;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.UUID;

/**
 * This class represents the header portion of a Keepass KDBX file or stream. The header is received in
 * plain text and describes the encryption and compression of the remainder of the file.
 * <p/>
 * When transferred, the values of the header are represented in little-endian order. The values
 * stored in this class are big-endian i.e. they need to be converted before or after use (e.g.
 * by using a {@link com.google.common.io.LittleEndianDataInputStream} or
 * {@link com.google.common.io.LittleEndianDataOutputStream}).
 * <p/>
 * @author jo
 */
public class KdbxHeader {

    /**
     * The numeric 0 represents uncompressed and 1 GZip compressed
     */
    public enum CompressionFlags {
        NONE, GZIP
    }

    /**
     * The ordinals represent various types of encryption that may be applied to fields
     */
    public enum CrsAlgorithm {
        NONE, ARC_FOUR, SALSA_20
    }

    /**
     * This UUID denotes that AES Cipher is in use. No other values are known.
     */
    public static final UUID AES_CIPHER = UUID.fromString("31C1F2E6-BF71-4350-BE58-05216AFC5AFF");

    /* the cipher in use */
    private UUID cipher;
    /* whether the data is compressed */
    private CompressionFlags compressionFlags;
    private byte [] masterSeed;
    private byte[] transformSeed;
    private long transformRounds;
    private byte[] encryptionIv;
    private byte[] protectedStreamKey;
    /* these bytes appear in cipher text immediately following the header */
    private byte[] streamStartBytes;
    private CrsAlgorithm crsAlgorithm;
    /* not transmitted as part of the header, used in the XML payload, so calculated
     * on transmission or receipt */
    private byte[] headerHash;

    /**
     * Construct a default KDBX header
     */
    public KdbxHeader() {
        SecureRandom random = new SecureRandom();
        cipher = AES_CIPHER;
        compressionFlags = CompressionFlags.GZIP;
        masterSeed = random.generateSeed(32);
        transformSeed = random.generateSeed(32);
        transformRounds = 6000;
        encryptionIv = random.generateSeed(16);
        protectedStreamKey = random.generateSeed(32);
        streamStartBytes = new byte[32];
        crsAlgorithm = CrsAlgorithm.SALSA_20;
    }

    /**
     * Get a final key digest according to the settings of this KDBX file
     * @param digest the key digest
     * @return a digest
     */
    public byte[] getFinalKeyDigest(byte[] digest) {
        return Encryption.getFinalKeyDigest(digest, getMasterSeed(), getTransformSeed(), getTransformRounds());
    }

    /**
     * Create a decrypted stream supplied digest and encrypted input stream using the data contained
     * in this instance.
     * @param digest the key digest
     * @param inputStream the encrypted input stream
     * @return a decrypted stream
     * @throws IOException
     */
    public InputStream createDecryptedStream(byte[] digest, InputStream inputStream) throws IOException {
        return createDecryptedStream(digest, this, inputStream);
    }

    /**
     * Create a decrypted stream using the supplied password and encrypted input stream using the data contained
     * in the passed KdbxHeader instance
     * @param digest the key digest
     * @param header a kdbx header
     * @param inputStream the encrypted input stream
     * @return a decrypted stream
     * @throws IOException
     */
    public static InputStream createDecryptedStream(byte[] digest, KdbxHeader header, InputStream inputStream) throws IOException {
        Cipher cipher = Encryption.initCipher(Cipher.DECRYPT_MODE, header.getFinalKeyDigest(digest), header.getEncryptionIv());
        return new CipherInputStream(inputStream, cipher);
    }

    /**
     * Create an encrypted stream using the supplied password and unencrypted output stream using the data contained
     * in this instance
     * @param digest the key digest
     * @param outputStream the plain text output stream
     * @return a decrypted stream
     * @throws IOException
     */
    public OutputStream createEncryptedStream(byte[] digest, OutputStream outputStream) throws IOException {
        return createEncryptedStream(digest, this, outputStream);
    }

    /**
     * Create an encrypted stream using the supplied password and unencrypted output stream using the data contained
     * in the passed KdbxHeader instance
     * @param digest the key digest
     * @param header a kdbx header
     * @param outputStream the plain text output stream
     * @return a decrypted stream
     * @throws IOException
     */
    public static OutputStream createEncryptedStream(byte[] digest, KdbxHeader header, OutputStream outputStream) throws IOException {
        Cipher cipher = Encryption.initCipher(Cipher.ENCRYPT_MODE, header.getFinalKeyDigest(digest), header.getEncryptionIv());
        return new CipherOutputStream(outputStream, cipher);
    }

    public UUID getCipher() {
        return cipher;
    }

    public CompressionFlags getCompressionFlags() {
        return compressionFlags;
    }

    public byte[] getMasterSeed() {
        return masterSeed;
    }

    public byte[] getTransformSeed() {
        return transformSeed;
    }

    public long getTransformRounds() {
        return transformRounds;
    }

    public byte[] getEncryptionIv() {
        return encryptionIv;
    }

    public byte[] getProtectedStreamKey() {
        return protectedStreamKey;
    }

    public byte[] getStreamStartBytes() {
        return streamStartBytes;
    }

    public CrsAlgorithm getCrsAlgorithm() {
        return crsAlgorithm;
    }

    public byte[] getHeaderHash() {
        return headerHash;
    }

    public void setCipher(byte[] uuid) {
        ByteBuffer b = ByteBuffer.wrap(uuid);
        UUID incoming = new UUID(b.getLong(), b.getLong(8));
        if (!incoming.equals(AES_CIPHER)) {
            throw new IllegalStateException("Unknown Cipher UUID " + incoming.toString());
        }
        this.cipher = incoming;
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

    public void setProtectedStreamKey(byte[] protectedStreamKey) {
        this.protectedStreamKey = protectedStreamKey;
    }

    public void setStreamStartBytes(byte[] streamStartBytes) {
        this.streamStartBytes = streamStartBytes;
    }

    public void setInnerRandomStreamId(int innerRandomStreamId) {
        this.crsAlgorithm = CrsAlgorithm.values()[innerRandomStreamId];
    }

    public void setHeaderHash(byte[] headerHash) {
        this.headerHash = headerHash;
    }
}
