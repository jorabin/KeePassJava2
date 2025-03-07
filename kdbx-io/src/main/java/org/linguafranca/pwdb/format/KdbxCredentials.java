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

package org.linguafranca.pwdb.format;

import org.jetbrains.annotations.NotNull;
import org.linguafranca.pwdb.Credentials;
import org.linguafranca.pwdb.security.Encryption;

import java.io.InputStream;
import java.security.MessageDigest;

/**
 * The class implements {@link Credentials} for KDBX files.
 *
 * @author jo
 */
public class KdbxCredentials implements Credentials {

    private final byte[] key;

    /**
     * Constructor for password with  KDBX Keyfile
     * @param password Master Password (<code>new byte[0]</code> if empty, not none)
     * @param inputStream inputStream of the keyfile
     */
    public KdbxCredentials(byte @NotNull [] password, @NotNull InputStream inputStream) {
        MessageDigest md = Encryption.getSha256MessageDigestInstance();
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
     * @param inputStream inputStream of the keyfile
     */
    public KdbxCredentials(@NotNull InputStream inputStream) {
        MessageDigest md = Encryption.getSha256MessageDigestInstance();
        byte[] keyFileData = KdbxKeyFile.load(inputStream);
        if (keyFileData == null) {
            throw new IllegalStateException("Could not read key file");
        }
        this.key = md.digest(keyFileData);
    }


    /**
     * Constructor for password only
     * @param password Master Password for database (<code>new byte[0]</code> if empty, not none)
     */
    public KdbxCredentials(byte @NotNull [] password) {
        MessageDigest md = Encryption.getSha256MessageDigestInstance();
        byte[] digest = md.digest(password);
        key = md.digest(digest);
    }

    @Override
    public byte[] getKey() {
        return key;
    }
}
