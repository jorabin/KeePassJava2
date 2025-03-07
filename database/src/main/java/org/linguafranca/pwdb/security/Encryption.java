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

package org.linguafranca.pwdb.security;

import org.linguafranca.pwdb.StreamFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * Contains the key transform functions and cipher algorithms used in other modules.
 * <p>
 * Also, some convenience utilities that hide the checked exceptions that would otherwise need to be checked for
 * when using digests.
 */
public class Encryption {

    /**
     * Gets a SHA-256 message digest instance
     *
     * @return A MessageDigest
     */
    public static MessageDigest getSha256MessageDigestInstance() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not supported");
        }
    }

    /**
     * Gets a SHA-512 message digest instance
     *
     * @return A MessageDigest
     */
    public static MessageDigest getSha512MessageDigestInstance() {
        try {
            return MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-512 is not supported");
        }
    }

    /**
     * Gets an HMacSha256 Mac
     *
     * @param key the key
     * @return the Mac initialised with the key
     */
    public static Mac getHMacSha256Instance(byte[] key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("HmacSHA256 is not supported", e);
        }
    }

    /**
     * From HmacBlockStream.cs GetHmacKey64
     * Calculates the block key for the block number ...
     *
     * @param digest    the HMAC key digest
     * @param transform the transform seed (the block number, 8 bytes)
     * @return a key
     */
    public static byte[] transformHmacKey(byte[] digest, byte[] transform) {
        MessageDigest md = Encryption.getSha512MessageDigestInstance();
        md.update(transform);
        return md.digest(digest);
    }

    /**
     * A list of functions that we can use to transform keys
     * Enum constants forward to underlying implementation.
     */
    public enum KeyDerivationFunction implements org.linguafranca.pwdb.security.KeyDerivationFunction {
        AES(Aes.getInstance()),
        ARGON2(Argon2.getInstance());

        private final org.linguafranca.pwdb.security.KeyDerivationFunction kdf;

        KeyDerivationFunction(org.linguafranca.pwdb.security.KeyDerivationFunction kdf) {
            this.kdf = kdf;
        }

        /**
         * Find a KDF that matches this Uuid
         *
         * @param kdfUuid the Uuid to match
         * @throws IllegalArgumentException if the Uuid is not known
         */
        public static org.linguafranca.pwdb.security.KeyDerivationFunction getKdf(UUID kdfUuid) {
            for (org.linguafranca.pwdb.security.KeyDerivationFunction kdf : values()) {
                if (kdf.getKdfUuid().equals(kdfUuid)) {
                    return kdf;
                }
            }
            throw new IllegalArgumentException("Unknown Cipher UUID");
        }

        @Override
        public UUID getKdfUuid() {
            return kdf.getKdfUuid();
        }

        @Override
        public byte[] getTransformedKey(byte[] key, VariantDictionary transformParams) {
            return kdf.getTransformedKey(key, transformParams);
        }

        @Override
        public VariantDictionary createKdfParameters() {
            return kdf.createKdfParameters();
        }

        @Override
        public String getName(){
            return kdf.getName();
        }
    }

    /**
     * A list of ciphers that we may apply to the database contents.
     * Enum constants forward to underlying implementation.
     */
    public enum Cipher implements CipherAlgorithm {
        CHA_CHA_20(ChaCha.getInstance()),
        AES(Aes.getInstance());

        private final CipherAlgorithm ef;

        /**
         * Find a cipher that matches this Uuid
         *
         * @param cipherUuid the Uuid
         * @return a cipher
         * @throws IllegalArgumentException if the Uuid is not known
         */
        public static CipherAlgorithm getCipherAlgorithm(UUID cipherUuid) {
            for (CipherAlgorithm ca : values()) {
                if (ca.getCipherUuid().equals(cipherUuid)) {
                    return ca;
                }
            }
            throw new IllegalArgumentException("Unknown Cipher UUID");
        }

        Cipher(CipherAlgorithm ef) {
            this.ef = ef;
        }

        @Override
        public UUID getCipherUuid() {
            return ef.getCipherUuid();
        }

        @Override
        public String getName(){
            return this.ef.getName();
        }

        @Override
        public InputStream getDecryptedInputStream(InputStream encryptedInputStream, byte[] key, byte[] iv) {
            return ef.getDecryptedInputStream(encryptedInputStream, key, iv);
        }

        @Override
        public OutputStream getEncryptedOutputStream(OutputStream decryptedOutputStream, byte[] key, byte[] iv) {
            return ef.getEncryptedOutputStream(decryptedOutputStream, key, iv);
        }
    }

    /**
     * The ordinals represent various types of encryption that may
     * be applied to fields within the unencrypted data
     *
     * @see StreamFormat
     */
    @SuppressWarnings("WeakerAccess, unused")
    public enum ProtectedStreamAlgorithm {
        NONE(0), ARC_FOUR(1), SALSA_20(2), CHA_CHA_20(3);

        private final int value;

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

        public static StreamEncryptor getStreamEncryptor(ProtectedStreamAlgorithm psa, byte [] key) {

            switch (psa) {
                case NONE: {
                    throw new IllegalStateException("Inner stream encoding of NONE");
                }
                case ARC_FOUR: {
                    throw new UnsupportedOperationException("Arc Four inner stream not supported");
                }
                case SALSA_20: {
                    return new StreamEncryptor.Salsa20(key);
                }
                case CHA_CHA_20: {
                    return new StreamEncryptor.ChaCha20(key);
                }
            }
            throw new IllegalStateException("Inner stream encoding unsupported");
        }

    }
}
