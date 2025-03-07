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

import org.bouncycastle.crypto.StreamCipher;
import org.bouncycastle.crypto.engines.ChaCha7539Engine;
import org.bouncycastle.crypto.io.CipherInputStream;
import org.bouncycastle.crypto.io.CipherOutputStream;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * ChaCha20 may be used for the inner stream as well as for the underlying database encryption scheme
 * <p>
 * A singleton
 */
public class ChaCha implements CipherAlgorithm {

    private static final UUID CHA_CHA_20_CIPHER = UUID.fromString("d6038a2b-8b6f-4cb5-a524-339a31dbb59a");

    private  static final String name = "CHA_CHA_20";

    // hide constructor to enforce singleton
    private ChaCha(){}
    private static final ChaCha instance = new ChaCha();

    public static ChaCha getInstance() {
        return instance;
    }

    @Override
    public UUID getCipherUuid() {
        return CHA_CHA_20_CIPHER;
    }

    @Override
    public String getName(){
        return name;
    }

    @Override
    public InputStream getDecryptedInputStream(InputStream encryptedInputStream, byte[] key, byte[] iv) {
        final ParametersWithIV keyAndIV = new ParametersWithIV(new KeyParameter(key), iv);
        StreamCipher cipher = new ChaCha7539Engine();
        cipher.init(false, keyAndIV);
        return new CipherInputStream(encryptedInputStream, cipher);
    }

    @Override
    public OutputStream getEncryptedOutputStream(OutputStream decryptedOutputStream, byte[] key, byte[] iv) {
        final ParametersWithIV keyAndIV = new ParametersWithIV(new KeyParameter(key), iv);
        StreamCipher cipher = new ChaCha7539Engine();
        cipher.init(true, keyAndIV);
        return new CipherOutputStream(decryptedOutputStream, cipher);
    }
}
