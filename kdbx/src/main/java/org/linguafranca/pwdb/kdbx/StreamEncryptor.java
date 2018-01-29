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

import org.linguafranca.pwdb.security.Encryption;
import org.spongycastle.crypto.StreamCipher;
import org.spongycastle.crypto.engines.ChaCha7539Engine;
import org.spongycastle.crypto.engines.Salsa20Engine;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.params.ParametersWithIV;
import org.spongycastle.util.encoders.Hex;

import java.security.MessageDigest;

/**
 * KDBX "protected" fields are stream encrypted. They must be decrypted in
 * the same order as they were encrypted.
 *
 * @author jo
 */
public interface StreamEncryptor {
    byte[] getKey();

    byte[] decrypt(byte[] encryptedText);

    byte[] encrypt(byte[] decryptedText);

    class None implements StreamEncryptor {

        @Override
        public byte[] getKey() {
            return new byte[0];
        }

        @Override
        public byte[] decrypt(byte[] encryptedText) {
            return encryptedText;
        }

        @Override
        public byte[] encrypt(byte[] decryptedText) {
            return decryptedText;
        }
    }

    class ChaCha20 extends Default {
        public ChaCha20(byte[] key) {
            super(new ChaCha7539Engine(), key);

            MessageDigest md = Encryption.getSha512MessageDigestInstance();
            md.update(key);
            byte [] digest = md.digest();

            byte [] keyDigest = new byte [32];
            byte [] iv = new byte [12];
            System.arraycopy(digest, 0, keyDigest, 0, keyDigest.length);
            System.arraycopy(digest, 32, iv, 0, iv.length);
            initialize(keyDigest, iv);
        }
    }

    class Salsa20 extends Default {
        private static final byte[] SALSA20_IV = Hex.decode("E830094B97205D2A".getBytes());

        public Salsa20(byte[] key) {
            super(new Salsa20Engine(), key);

            MessageDigest md = Encryption.getSha256MessageDigestInstance();
            initialize(md.digest(key), SALSA20_IV);
        }
    }

    class Default implements StreamEncryptor {

        private final StreamCipher cipher;
        private final byte[] key;


        /**
         * Initializes an engine
         *
         * @param key the key to use
         * @param iv the iv
         */
        public  void initialize(byte[] key, byte [] iv) {
            KeyParameter keyParameter = new KeyParameter(key);
            ParametersWithIV ivParameter = new ParametersWithIV(keyParameter, iv);

            cipher.init(true, ivParameter);
        }

        /**
         * @param cipher the cipher to use
         * @param key the key to use
         */
        public Default(StreamCipher cipher, byte[] key) {
            this.key = key;
            this.cipher = cipher;
        }

        @Override
        public byte[] getKey() {
            return key;
        }

        @Override
        public byte[] decrypt(byte[] encryptedText) {
            byte[] output = new byte[encryptedText.length];
            cipher.processBytes(encryptedText, 0, encryptedText.length, output, 0);
            return output;
        }

        @Override
        public byte[] encrypt(byte[] decryptedText) {
            byte[] output = new byte[decryptedText.length];
            cipher.processBytes(decryptedText, 0, decryptedText.length, output, 0);
            return output;
        }
    }

}
