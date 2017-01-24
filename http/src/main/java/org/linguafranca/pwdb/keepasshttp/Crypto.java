package org.linguafranca.pwdb.keepasshttp;

import org.linguafranca.pwdb.kdbx.Helpers;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.engines.AESFastEngine;
import org.spongycastle.crypto.modes.CBCBlockCipher;
import org.spongycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.params.ParametersWithIV;

import java.security.SecureRandom;

/**
 * Cryptography for KeePassHttp emulator
 */
class Crypto {

    private byte[] binaryKey;

    Crypto() {
    }

    Crypto(String key) {
        this.binaryKey = Helpers.decodeBase64Content(key.getBytes());
    }

    byte[] getKey() {
        return binaryKey;
    }

    void setKey(byte[] binaryKey) {
        this.binaryKey = binaryKey;
    }

    enum CMode {
        ENCRYPT(true),
        DECRYPT(false);

        private final boolean encrypt;

        CMode(boolean encrypt) {
            this.encrypt = encrypt;
        }

        public boolean getEncrypt() {
            return encrypt;
        }
    }

    /**
     * Return true if the Nonce and the Verifier on a message match
     *
     * @param verifiable a message containing those fields
     */
    boolean verify(Message.Verifiable verifiable) {
        if (getKey() == null || verifiable.Verifier == null || verifiable.Nonce == null ||
                verifiable.Verifier.equals("") || verifiable.Nonce.equals("")) {
            return false;
        }
        // The nonce is base64 encoded version of an iv
        byte[] iv = Helpers.decodeBase64Content(verifiable.Nonce.getBytes(), false);
        // The verifier is the base64 encoded encrypted version of the nonce
        String decrypted = decryptFromBase64(verifiable.Verifier, iv);
        // the decrypted verifier should be the same as the nonce
        return decrypted.equals(verifiable.Nonce);
    }

    boolean verify(Message.Request request) {
        if (!verify((Message.Verifiable) request)) {
            return false;
        }

        byte[] iv = Helpers.decodeBase64Content(request.Nonce.getBytes(), false);
        // decrypt all the fields
        if (request.Login != null) {
            request.Login = decryptFromBase64(request.Login, iv);
        }
        if (request.Password != null) {
            request.Password = decryptFromBase64(request.Password, iv);
        }
        if (request.Url != null) {
            request.Url = decryptFromBase64(request.Url, iv);
        }
        if (request.SubmitUrl != null) {
            request.SubmitUrl = decryptFromBase64(request.SubmitUrl, iv);
        }
        if (request.Uuid != null) {
            request.Uuid = decryptFromBase64(request.Uuid, iv);
        }

        return true;
    }

    /**
     * Add a Nonce and a Verifier to a message to make it verifiable
     *
     * @param response a message to make verifiable
     */
    void makeVerifiable(Message.Response response) {
        // we don't have a key? we can't do anything
        if (getKey() == null) {
            return;
        }

        // The nonce is base64 encoded version of an iv
        byte[] iv = new SecureRandom().generateSeed(16);
        response.Nonce = Helpers.encodeBase64Content(iv, false);
        // The verifier is the base64 encoded encrypted version of the nonce
        response.Verifier = encryptToBase64(response.Nonce, iv);

        // encrypt any entries
        if (response.Entries != null) {
            for (Message.ResponseEntry entry : response.Entries) {
                entry.Login = encryptToBase64(entry.Login, iv);
                entry.Uuid = encryptToBase64(entry.Uuid, iv);
                entry.Name = encryptToBase64(entry.Name, iv);
                entry.Password = encryptToBase64(entry.Password, iv);
            }
        }
    }

    /**
     * Get a cipher
     *
     * @param mode encryption or decryption
     * @param iv   a 16 byte iv
     * @return an initialised Cipher
     */
    PaddedBufferedBlockCipher getCipher(CMode mode, byte[] iv) {
        PaddedBufferedBlockCipher result = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESFastEngine()));
        result.init(mode.getEncrypt(), new ParametersWithIV(new KeyParameter(getKey()), iv));
        return result;
    }

    /**
     * Return an unencrypted non encoded copy of an encrypted base 64 encoded string
     *
     * @param input cipher text
     * @param iv    an iv
     * @return plain text
     */
    String decryptFromBase64(String input, byte[] iv) {
        return CryptoTransform(input, true, false, getCipher(CMode.DECRYPT, iv));
    }

    /**
     * Return an encrypted base 64 encoded copy of plain text string
     *
     * @param input plain text
     * @param iv    an iv
     * @return cipher text
     */
    String encryptToBase64(String input, byte[] iv) {
        return CryptoTransform(input, false, true, getCipher(CMode.ENCRYPT, iv));
    }

    /**
     * Encryption and Decryption Helper
     *
     * @param input     the candidate for transformation
     * @param base64in  true if base 64 encoded
     * @param base64out true if we require base 64 out
     * @param cipher    a Cipher initialised for Encrypt or Decrypt
     * @return the transformed result
     */
    static String CryptoTransform(String input, boolean base64in, boolean base64out, PaddedBufferedBlockCipher cipher) {
        byte[] bytes;
        if (base64in) {
            bytes = Helpers.decodeBase64Content(input.getBytes(), false);
        } else {
            bytes = input.getBytes();
        }

        byte[] output = new byte[cipher.getOutputSize(bytes.length)];
        int outputlen = cipher.processBytes(bytes, 0, bytes.length, output, 0);
        try {
            int len = cipher.doFinal(output, outputlen);
            // padded buffer is required on bas64 i.e. encrypted direction
            if (base64out) {
                return Helpers.encodeBase64Content(output, false);
            }
            // trim to buffer length
            return new String(output, 0, outputlen + len);
        } catch (InvalidCipherTextException e) {
            throw new IllegalStateException(e);
        }
    }
}
