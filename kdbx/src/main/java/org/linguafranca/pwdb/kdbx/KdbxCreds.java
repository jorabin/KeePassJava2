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
public class KdbxCreds implements Credentials {

    private final byte[] key;

    /**
     * Constructor for password with  KDBX Keyfile
     * @param password Master Password (<code>new byte[0]</code> if empty, not none)
     * @param inputStream inputstream of the keyfile
     */
    public KdbxCreds(@NotNull byte[] password, @NotNull InputStream inputStream) {
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
    public KdbxCreds(@NotNull InputStream inputStream) {
        MessageDigest md = Encryption.getMessageDigestInstance();
        byte[] keyFileData = KdbxKeyFile.load(inputStream);
        if (keyFileData == null) {
            throw new IllegalStateException("Could not read key file");
        }
        this.key = md.digest(keyFileData);
    }


    public KdbxCreds(@NotNull byte[] password) {
        MessageDigest md = Encryption.getMessageDigestInstance();
        byte[] digest = md.digest(password);
        key = md.digest(digest);
    }

    @Override
    public byte[] getKey() {
        return key;
    }
}
