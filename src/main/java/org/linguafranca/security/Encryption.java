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

package org.linguafranca.security;

import org.jetbrains.annotations.NotNull;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.*;

/**
 * A number of utilities whose partial purpose is to wrap
 * Checked Exceptions into unchecked {@link
 * IllegalStateException}s to allow for more readable code.
 *
 * <p>There is no point in cluttering up calling classes with
 * checked exception handling for repeated calls that
 * have previously succeeded.
 *
 * @author jo
 */
public class Encryption {

    /**
     * Gets a Cipher instance
     *
     * @param type a string representing the type
     * @return an instance
     */
    @NotNull
    public static Cipher getCipherInstance(String type) {
        try {
            return Cipher.getInstance(type);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("NoSuchAlgorithm: " + e.getMessage());
        } catch (NoSuchPaddingException e) {
            throw new IllegalStateException("NoSuchPadding: " + e.getMessage());
        }
    }

    /**
     * Gets a digest for a UTF-8 encoded string
     *
     * @param string the string
     * @return a digest as a byte array
     */
    public static byte[] getDigest(String string) {
        return getDigest(string, "UTF-8");
    }

    /**
     * Gets a digest for a string
     *
     * @param string the string
     * @param encoding the encoding of the String
     * @return a digest as a byte array
     */
    public static byte[] getDigest(String string, String encoding) {
        if (string == null || string.length() == 0)
            throw new IllegalArgumentException("Password cannot be null or empty");

        if (encoding == null || encoding.length() == 0)
            throw new IllegalArgumentException("Encoding cannot be null or empty");

        MessageDigest md = getMessageDigestInstance();

        try {
            byte[] bytes = string.getBytes(encoding);
            md.update(bytes, 0, bytes.length);
            return md.digest();
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(encoding + " is not supported");
        }
    }

    /**
     * Gets a SHA-256 message digest instance
     *
     * @return A MessageDigest
     */
    public static MessageDigest getMessageDigestInstance() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not supported");
        }
    }

    /**
     * Gets a digest for a key transformed according to the parameters.
     *
     * @param key a key
     * @param masterSeed the master seed
     * @param transformSeed the transform seed
     * @param transformRounds the number of transform rounds
     * @return a digest
     */
    public static byte[] getFinalKeyDigest(byte[] key, byte[] masterSeed, byte[] transformSeed, long transformRounds) {

        MessageDigest md = getMessageDigestInstance();
        Cipher cipher = getCipherInstance("AES/ECB/NoPadding");

        try {
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(transformSeed, "AES"));
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("Invalid password", e);
        }

        // Encrypt the key transformRounds times
        byte[] inputKey = new byte[key.length];
        System.arraycopy(key, 0, inputKey, 0, inputKey.length);
        byte[] outputKey = new byte[inputKey.length];
        for (long rounds = 0; rounds < transformRounds; rounds++) {
            try {
                cipher.update(inputKey, 0, inputKey.length, outputKey, 0);
                System.arraycopy(outputKey, 0, inputKey, 0, outputKey.length);
            } catch (ShortBufferException e) {
                throw new IllegalStateException(e.getMessage());
            }
        }

        byte[] transformedPassword = md.digest(outputKey);

        md.update(masterSeed);
        return md.digest(transformedPassword);
    }

    /**
     * Gets and initializes a Cipher instance using AES/CBC/PKCS5Padding
     *
     * @param mode {@link javax.crypto.Cipher.ENCRYPT_MODE} {@link javax.crypto.Cipher.DECRYPT_MODE} etc
     * @param finalKey a finalKey
     * @param encryptionIv an encryptionIv
     * @return initialized Cipher
     */
    @SuppressWarnings("JavadocReference")
    public static Cipher initCipher(int mode, byte[] finalKey, byte[] encryptionIv) {
        return initCipher(Encryption.getCipherInstance("AES/CBC/PKCS5Padding"), mode, finalKey, encryptionIv);
    }

    /**
     * Gets and initializes a Cipher instance using specified Cipher
     *
     * @param cipher a Cipher instance
     * @param mode {@link javax.crypto.Cipher.ENCRYPT_MODE} {@link javax.crypto.Cipher.DECRYPT_MODE} etc
     * @param finalKey a finalKey
     * @param encryptionIv an encryptionIv
     * @return initialized Cipher
     */
    @SuppressWarnings("JavadocReference")
    public static Cipher initCipher(Cipher cipher, int mode, byte[] finalKey, byte[] encryptionIv) {
        Key aesKey = new SecretKeySpec(finalKey, "AES");
        IvParameterSpec iv = new IvParameterSpec(encryptionIv);
        try {
            cipher.init(mode, aesKey, iv);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new IllegalStateException(e.getMessage());
        }
        return cipher;
    }
}
