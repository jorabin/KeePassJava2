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

import org.junit.Test;
import org.linguafranca.pwdb.Credentials;
import org.linguafranca.pwdb.kdbx.dom.DomDatabaseWrapper;
import org.linguafranca.pwdb.kdbx.dom.DomEntryWrapper;
import org.linguafranca.pwdb.kdbx.dom.DomGroupWrapper;
import org.linguafranca.pwdb.kdbx.dom.DomIconWrapper;
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
public class SimpleQuickStartTest extends QuickStart<DomDatabaseWrapper, DomGroupWrapper, DomEntryWrapper, DomIconWrapper> {


    @Override
    public DomDatabaseWrapper getDatabase() {
        return new DomDatabaseWrapper();
    }

    @Override
    public DomDatabaseWrapper loadDatabase(Credentials credentials, InputStream inputStream){
        try {
            return DomDatabaseWrapper.load(credentials, inputStream);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
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

        loadKdbx3SaveKdbx4("test123.kdbx","123".getBytes(), Files.newOutputStream(path));

        // load newly created V4 database
        DomDatabaseWrapper db = DomDatabaseWrapper.load(new KdbxCreds("123".getBytes()), Files.newInputStream(path));
        KdbxStreamFormat streamFormat = (KdbxStreamFormat) db.getStreamFormat();
        assertEquals(4, streamFormat.getStreamConfiguration().getVersion());
        assertEquals(Encryption.Cipher.CHA_CHA_20, streamFormat.getStreamConfiguration().getCipherAlgorithm());
        assertEquals(Encryption.KeyDerivationFunction.ARGON2, streamFormat.getStreamConfiguration().getKeyDerivationFunction());
        assertEquals(Encryption.ProtectedStreamAlgorithm.CHA_CHA_20, streamFormat.getStreamConfiguration().getProtectedStreamAlgorithm());
    }
    @Test
    public void loadSave2() throws IOException {
        Path path = Paths.get("testOutput/CHACHA-AES-CHACHA.kdbx");

        loadKdbx4SaveKdbx3("V4-ChaCha20-Argon2-Attachment.kdbx","123".getBytes(), Files.newOutputStream(path));

        // load newly created V4 database
        DomDatabaseWrapper db = DomDatabaseWrapper.load(new KdbxCreds("123".getBytes()), Files.newInputStream(path));
        KdbxStreamFormat streamFormat = (KdbxStreamFormat) db.getStreamFormat();
        assertEquals(3, streamFormat.getStreamConfiguration().getVersion());
        assertEquals(Encryption.Cipher.AES, streamFormat.getStreamConfiguration().getCipherAlgorithm());
        assertEquals(Encryption.KeyDerivationFunction.AES, streamFormat.getStreamConfiguration().getKeyDerivationFunction());
        assertEquals(Encryption.ProtectedStreamAlgorithm.SALSA_20, streamFormat.getStreamConfiguration().getProtectedStreamAlgorithm());
    }
}
