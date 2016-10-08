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

package org.linguafranca.pwdb.kdb;

import org.linguafranca.security.Credentials;
import org.linguafranca.security.Encryption;

import java.security.MessageDigest;

/**
 * Has inner classes representing credentials appropriate to KDB files
 *
 * @author jo
 */
public interface KdbCredentials extends Credentials {

    /**
     * Password only credentials
     */
    public static class Password implements KdbCredentials {
        private byte [] key;

        public Password(byte[] password) {
            MessageDigest md = Encryption.getMessageDigestInstance();
            this.key = md.digest(password);
        }

        @Override
        public byte[] getKey() {
            return key;
        }
    }
}
