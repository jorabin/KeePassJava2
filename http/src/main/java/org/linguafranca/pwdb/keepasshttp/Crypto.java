package org.linguafranca.pwdb.keepasshttp;

import org.linguafranca.pwdb.kdbx.Helpers;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.engines.AESFastEngine;
import org.spongycastle.crypto.modes.CBCBlockCipher;
import org.spongycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.params.ParametersWithIV;

import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Cryptography for KeePassHttp emulator
 */
public class Crypto {

    private byte[] binaryKey;
    public byte[] getKey() {
        return binaryKey;
    }
    public void setKey(byte[] binaryKey) {
        this.binaryKey = binaryKey;
    }

    public enum CMode {
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

    /** TODO this can be simplified using the helpers below (it's what they are for) */
    public boolean verify(Message.Verifiable verifiable) {
        if (getKey() == null || verifiable.Verifier == null || verifiable.Nonce == null) {
            return false;
        }
        byte[] verifier = Helpers.decodeBase64Content(verifiable.Verifier.getBytes(), false);
        if (verifier.length == 0) {
            return false;
        }
        byte[] iv = Helpers.decodeBase64Content(verifiable.Nonce.getBytes(), false);
        if (iv.length == 0) {
            return false;
        }

        PaddedBufferedBlockCipher cipher = getCipher(Crypto.CMode.DECRYPT, iv);

        byte[] output = new byte[cipher.getOutputSize(verifier.length)];
        int outputlen = cipher.processBytes(verifier, 0, verifier.length, output, 0);

        try {
            cipher.doFinal(output, outputlen);
            byte[] comparison = new byte[output.length];
            System.arraycopy(verifiable.Nonce.getBytes(),0,comparison,0,verifiable.Nonce.length());
            return Arrays.equals(output, comparison);
        } catch (InvalidCipherTextException e) {
            return false;
        }
    }

    public void makeVerifiable(Message.Response response) {
        // we don't have a key? we can't do anything
        if (getKey() == null) {
            return;
        }

        // The nonce is base64 encoded version of an iv
        // The verifier is the base64 encoded encrypted version of the nonce
        byte[] iv = new SecureRandom().generateSeed(16);
        response.Nonce = Helpers.encodeBase64Content(iv, false);
        response.Verifier = encryptToBase64(response.Nonce, iv);
/*
        PaddedBufferedBlockCipher cipher = getCipher(Crypto.CMode.ENCRYPT, iv);
        response.Verifier = CryptoTransform(response.Nonce, false, true, cipher);
*/

        if (response.Entries != null) {
            for (Message.ResponseEntry entry : response.Entries) {
                entry.Login = encryptToBase64(entry.Login, iv);
                entry.Uuid = encryptToBase64(entry.Uuid, iv);
                entry.Name = encryptToBase64(entry.Name, iv);
                entry.Password = encryptToBase64(entry.Password, iv);
            }
        }
    }


    public PaddedBufferedBlockCipher getCipher(CMode mode, byte[] iv) {
        PaddedBufferedBlockCipher result = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESFastEngine()));
        result.init(mode.getEncrypt(), new ParametersWithIV(new KeyParameter(getKey()), iv));
        return result;
    }

    public String decryptFromBase64(String input, byte[] iv){
        return CryptoTransform(input, true, false, getCipher(CMode.DECRYPT, iv));
    }

    public String encryptToBase64(String input, byte[] iv){
        return CryptoTransform(input, false, true, getCipher(CMode.ENCRYPT, iv));
    }

    public static String CryptoTransform(String input, boolean base64in, boolean base64out, PaddedBufferedBlockCipher cipher) {
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
