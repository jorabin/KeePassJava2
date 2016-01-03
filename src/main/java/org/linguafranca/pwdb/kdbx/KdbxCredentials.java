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

import org.linguafranca.security.Encryption;
import org.linguafranca.security.Credentials;

import java.io.InputStream;
import java.security.MessageDigest;

/**
 * The class holds subclasses of {@link Credentials} appropriate to KDBX files.
 *
 * @author jo
 */
public interface KdbxCredentials extends Credentials {

    /**
     * Class for KDBX key file with password credentials
     */
    public static class KeyFile implements KdbxCredentials {

        private final byte[] key;

        public KeyFile(byte[] password, InputStream inputStream) {
            MessageDigest md = Encryption.getMessageDigestInstance();
            byte[] pwKey = md.digest(password);

            byte[] keyFileData = KdbxKeyFile.load(inputStream);
            if (keyFileData == null) {
                throw new IllegalStateException("Could not read key file");
            }
            md.update(pwKey);
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
    public static class Password implements KdbxCredentials {

        private final byte[] key;

        public Password(byte[] password) {
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
