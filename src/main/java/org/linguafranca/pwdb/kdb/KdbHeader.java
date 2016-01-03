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

package org.linguafranca.pwdb.kdb;

import org.linguafranca.security.Encryption;

import javax.crypto.Cipher;
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
     * @param key key
     * @param inputStream an encrypted stream
     * @return a decrypted stream
     * @throws IOException
     */
    public InputStream createDecryptedInputStream(byte[] key, InputStream inputStream) throws IOException {
        Cipher cipher;
        if ((flags & FLAG_RIJNDAEL) == 0) {
            throw new IllegalStateException("Encryption algorithm is not supported");
        }

        byte[] finalKeyDigest = Encryption.getFinalKeyDigest(key, masterSeed, transformSeed, transformRounds);
        return Encryption.getDecryptedInputStream(inputStream, finalKeyDigest, encryptionIv);
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
