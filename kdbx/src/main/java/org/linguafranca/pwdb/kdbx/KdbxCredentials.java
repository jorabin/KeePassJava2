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

import org.linguafranca.pwdb.Credentials;
import org.linguafranca.pwdb.security.Encryption;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.security.MessageDigest;

/**
 * The class holds subclasses of {@link Credentials} appropriate to KDBX files.
 *
 * @author jo
 * @deprecated use KdbxCreds instead
 */
@Deprecated
@SuppressWarnings("deprecation")
public interface KdbxCredentials extends Credentials {

    /**
     * Class for KDBX key file with password credentials
     */
    class KeyFile implements KdbxCredentials {

        private final byte[] key;

        /**
         * Constructor for password with  KDBX Keyfile
         * @param password Master Password (<code>new byte[0]</code> if empty, not none)
         * @param inputStream inputstream of the keyfile
         */
        public KeyFile(@NotNull byte[] password, @NotNull InputStream inputStream) {
            MessageDigest md = Encryption.getMessageDigestInstance();
            byte[] pwKey = md.digest(password);
            md.update(pwKey);

            byte[] keyFileData = KdbxKeyFile.load(inputStream);
            if (keyFileData == null) {
                throw new IllegalStateException("Could not read key file");
            }
            this.key = md.digest(keyFileData);
        }

        /**
         * Constructor for KDBX Keyfile with no password
         * @param inputStream inputstream of the keyfile
         */
        public KeyFile(@NotNull InputStream inputStream) {
            MessageDigest md = Encryption.getMessageDigestInstance();

            byte[] keyFileData = KdbxKeyFile.load(inputStream);
            if (keyFileData == null) {
                throw new IllegalStateException("Could not read key file");
            }
            this.key = md.digest(keyFileData);
        }

        @Override
        public byte[] getKey() {
            return key;
        }
    }

    /**
     * Class for KDBX password only credentials
     */
    class Password implements KdbxCredentials {

        private final byte[] key;

        public Password(@NotNull byte[] password) {
            MessageDigest md = Encryption.getMessageDigestInstance();
            byte[] digest = md.digest(password);
            key = md.digest(digest);
        }

        @Override
        public byte[] getKey() {
            return key;
        }
    }
}
