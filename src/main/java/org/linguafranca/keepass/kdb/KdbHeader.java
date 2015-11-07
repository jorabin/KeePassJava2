package org.linguafranca.keepass.kdb;

import org.linguafranca.keepass.encryption.Encryption;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class stores the encryption details of a KDB file and provides a method to create
 * a decrypted inputStream from an encrypted one.
 *
 * @author jo
 */
@SuppressWarnings("unused")
public class KdbHeader {

    // flags for possible encryption of the KDB stream
    public static final int FLAG_SHA2 = 1;
    public static final int FLAG_RIJNDAEL = 2;
    public static final int FLAG_ARCFOUR = 4;
    public static final int FLAG_TWOFISH = 8;

    private int flags;
    private int version;
    private byte[] masterSeed;
    private byte[] encryptionIv;
    private long groupCount;
    private long entryCount;
    private byte[] contentHash;
    private byte[] transformSeed;
    private long transformRounds;

    /**
     * Create a decrypted stream from an encrypted one
     *
     * @param password credentials
     * @param inputStream an encrypted stream
     * @return a decrypted stream
     * @throws IOException
     */
    public InputStream createDecryptedInputStream(String password, InputStream inputStream) throws IOException {
        Cipher cipher;
        if ((flags & FLAG_RIJNDAEL) != 0) {
            cipher = Encryption.getCipherInstance("AES/CBC/PKCS5Padding");
        } else if ((flags & FLAG_TWOFISH) != 0) {
            cipher = Encryption.getCipherInstance("TWOFISH/CBC/PKCS7PADDING");
        } else {
            throw new IllegalStateException("Encryption algorithm is not supported");
        }

        byte[] finalKeyDigest = Encryption.getFinalKeyDigest(password.getBytes(), masterSeed, transformSeed, transformRounds);
        cipher = Encryption.initCipher(cipher, Cipher.DECRYPT_MODE, finalKeyDigest, encryptionIv);
        return new CipherInputStream(inputStream, cipher);
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public byte[] getMasterSeed() {
        return masterSeed;
    }

    public void setMasterSeed(byte[] masterSeed) {
        this.masterSeed = masterSeed;
    }

    public byte[] getEncryptionIv() {
        return encryptionIv;
    }

    public void setEncryptionIv(byte[] encryptionIv) {
        this.encryptionIv = encryptionIv;
    }

    public long getGroupCount() {
        return groupCount;
    }

    public void setGroupCount(long groupCount) {
        this.groupCount = groupCount;
    }

    public long getEntryCount() {
        return entryCount;
    }

    public void setEntryCount(long entryCount) {
        this.entryCount = entryCount;
    }

    public byte[] getContentHash() {
        return contentHash;
    }

    public void setContentHash(byte[] contentHash) {
        this.contentHash = contentHash;
    }

    public byte[] getTransformSeed() {
        return transformSeed;
    }

    public void setTransformSeed(byte[] transformSeed) {
        this.transformSeed = transformSeed;
    }

    public long getTransformRounds() {
        return transformRounds;
    }

    public void setTransformRounds(long transformRounds) {
        this.transformRounds = transformRounds;
    }
}
