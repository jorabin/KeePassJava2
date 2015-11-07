package org.linguafranca.keepass.encryption;

import org.jetbrains.annotations.NotNull;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.*;

/**
 * A number of utilities whose partial purpose is to wrap Checked Exceptions into unchecked {@link
 * IllegalStateException}s to allow for more readable code. There is no point in cluttering up calling classes with
 * checked exception handling for repeated calls that have previously succeeded.
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
     * Gets a digest for a string transformed according to the parameters
     *
     * @param password a UTF-8 encoded string which is the key
     * @param masterSeed the master seed
     * @param transformSeed the transform seed
     * @param transformRounds the number of transform rounds
     * @return a digest
     */
    public static byte[] getFinalKeyDigest(String password, byte[] masterSeed, byte[] transformSeed, long transformRounds) {
        return getFinalKeyDigest(getDigest(password), masterSeed, transformSeed, transformRounds);
    }

    /**
     * Gets a digest for a digest transformed according to the parameters.
     *
     * <p>For kdb files the key is used directly, for KDBX files the key is a digest.
     *
     * @param passwordDigest a key or key digest
     * @param masterSeed the master seed
     * @param transformSeed the transform seed
     * @param transformRounds the number of transform rounds
     * @return a digest
     */
    public static byte[] getFinalKeyDigest(byte[] passwordDigest, byte[] masterSeed, byte[] transformSeed, long transformRounds) {

        MessageDigest md = getMessageDigestInstance();
        md.update(passwordDigest);
        byte[] hashedPassword = md.digest();

        Cipher cipher = getCipherInstance("AES/ECB/NoPadding");

        try {
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(transformSeed, "AES"));
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("Invalid password", e);
        }

        // Encrypt the key transformRounds times
        byte[] inputKey = new byte[hashedPassword.length];
        System.arraycopy(hashedPassword, 0, inputKey, 0, inputKey.length);
        byte[] outputKey = new byte[inputKey.length];
        for (long rounds = 0; rounds < transformRounds; rounds++) {
            try {
                cipher.update(inputKey, 0, inputKey.length, outputKey, 0);
                System.arraycopy(outputKey, 0, inputKey, 0, outputKey.length);
            } catch (ShortBufferException e) {
                throw new IllegalStateException(e.getMessage());
            }
        }

        // md.digest() resets the digest, but just in case get a new one
        md = getMessageDigestInstance();
        md.update(outputKey);
        byte[] transformedPassword = md.digest();

        md = getMessageDigestInstance();
        md.update(masterSeed);
        md.update(transformedPassword);
        return md.digest();
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
