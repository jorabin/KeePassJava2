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

package org.linguafranca.pwdb.kdbx;

import org.junit.BeforeClass;
import org.junit.Test;
import org.linguafranca.pwdb.Credentials;
import org.linguafranca.pwdb.format.KdbxCreds;
import org.linguafranca.pwdb.format.KdbxStreamFormat;
import org.linguafranca.pwdb.kdbx.jackson.KdbxDatabase;
import org.linguafranca.pwdb.security.Encryption;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

/**
 * Simple illustration of hooking a SAX parser up to process a KDBX file
 * 
 * @author jo
 */
public class SimpleQuickStartTest extends QuickStart {


    @BeforeClass
    public static void ensureOutputDir() throws IOException {
        Files.createDirectories(Paths.get(TEST_OUTPUT_DIR));
    }

    @Test
    public void saveTest() throws IOException {
        super.saveKdbx();
    }

    @Test
    public void loadTest() throws IOException {
        super.loadKdbx();
    }

    @Test
    public void loadKdbSaveVernacular() throws IOException {
        super.loadKdb();
    }

    @Test
    public void loadSave() throws IOException {
        Path path = Paths.get("testOutput/CHACHA-AES-CHACHA.kdbx");

        loadKdbx3SaveKdbx4("test123.kdbx","123".getBytes(), path);

        // load newly created V4 database
        KdbxDatabase db = KdbxDatabase.load(new KdbxCreds("123".getBytes()), Files.newInputStream(path));
        KdbxStreamFormat streamFormat = (KdbxStreamFormat) db.getStreamFormat();
        assertEquals(4, streamFormat.getStreamConfiguration().getVersion());
        assertEquals(Encryption.Cipher.CHA_CHA_20, streamFormat.getStreamConfiguration().getCipherAlgorithm());
        assertEquals(Encryption.KeyDerivationFunction.ARGON2, streamFormat.getStreamConfiguration().getKeyDerivationFunction());
        assertEquals(Encryption.ProtectedStreamAlgorithm.CHA_CHA_20, streamFormat.getStreamConfiguration().getProtectedStreamAlgorithm());
    }
    @Test
    public void loadSave2() throws IOException {
        Path path = Paths.get("testOutput/CHACHA-AES-CHACHA.kdbx");

        loadKdbx4SaveKdbx3("V4-ChaCha20-Argon2-Attachment.kdbx","123".getBytes(), path);

        // load newly created V4 database
        KdbxDatabase db = KdbxDatabase.load(new KdbxCreds("123".getBytes()), Files.newInputStream(path));
        KdbxStreamFormat streamFormat = (KdbxStreamFormat) db.getStreamFormat();
        assertEquals(3, streamFormat.getStreamConfiguration().getVersion());
        assertEquals(Encryption.Cipher.AES, streamFormat.getStreamConfiguration().getCipherAlgorithm());
        assertEquals(Encryption.KeyDerivationFunction.AES, streamFormat.getStreamConfiguration().getKeyDerivationFunction());
        assertEquals(Encryption.ProtectedStreamAlgorithm.SALSA_20, streamFormat.getStreamConfiguration().getProtectedStreamAlgorithm());
    }
}
