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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Interface defining an algorithm for encrypting and decrypting database contents
 */
public interface CipherAlgorithm {
    /**
     * Returns the UUID of this algorithm
     */
    UUID getCipherUuid();

    /**
     * Return the name of this algorithm
     */
    String getName();

    /**
     * Create a decrypted stream from the supplied encrypted one
     *
     * @param encryptedInputStream an encrypted stream
     * @param key                  the decryption key
     * @param iv                   the iv
     * @return an unencrypted stream
     */
    InputStream getDecryptedInputStream(InputStream encryptedInputStream, byte[] key, byte[] iv);

    /**
     * Create an encrypted stream  from the supplied unencrypted one
     *
     * @param decryptedOutputStream an unencrypted stream
     * @param key                   a key
     * @param iv                    an iv
     * @return an encrypted stream
     */
    OutputStream getEncryptedOutputStream(OutputStream decryptedOutputStream, byte[] key, byte[] iv);

}
