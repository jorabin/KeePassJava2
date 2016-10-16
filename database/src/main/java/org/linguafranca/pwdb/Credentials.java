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

package org.linguafranca.pwdb;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Supports a contract that yields a key for decryption of databases
 *
 * @author jo
 */
public interface Credentials {

    /**
     * Implementation of no credentials
     */
    class None implements Credentials {
        @Override
        public byte[] getKey() {
            MessageDigest md = null;
            try {
                md = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e);
            }
            return md.digest(new byte[0]);
        }
    }

    byte [] getKey();
}
