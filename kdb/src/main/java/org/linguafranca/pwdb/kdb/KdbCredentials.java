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

import com.google.common.io.ByteStreams;
import org.linguafranca.pwdb.Credentials;
import org.linguafranca.pwdb.security.Encryption;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.io.InputStream;
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
    class Password implements KdbCredentials {

        private final byte [] key;

        public Password(byte[] password) {
            MessageDigest md = Encryption.getMessageDigestInstance();
            this.key = md.digest(password);
        }

        @Override
        public byte[] getKey() {
            return key;
        }
    }

    /**
     * Key file credentials
     */
    class KeyFile implements KdbCredentials {

        private final byte[] key;

        public KeyFile(byte[] password, InputStream inputStream) {
            MessageDigest md = Encryption.getMessageDigestInstance();
            byte[] pwKey = md.digest(password);
            md.update(pwKey);

            try {
                byte [] keyFileData = ByteStreams.toByteArray(inputStream);
                if (keyFileData.length == 64) {
                    keyFileData = Hex.decode(keyFileData);
                }
                key = md.digest(keyFileData);
            } catch (IOException e) {
                throw new IllegalStateException("Could not read key file", e);
            }
        }

        @Override
        public byte[] getKey() {
            return this.key;
        }

    }
}
