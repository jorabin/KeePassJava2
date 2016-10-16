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
}
