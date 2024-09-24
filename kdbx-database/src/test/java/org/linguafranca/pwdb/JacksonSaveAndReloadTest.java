/*
 * Copyright 2023 Giuseppe Valente
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

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.linguafranca.pwdb.checks.SaveAndReloadChecks;
import org.linguafranca.pwdb.format.KdbxCreds;
import org.linguafranca.pwdb.format.KdbxHeader;

import org.linguafranca.pwdb.kdbx.jackson.KdbxDatabase;


public class JacksonSaveAndReloadTest extends SaveAndReloadChecks{
 
    @Override
    public KdbxDatabase getDatabase() {
        try {
            return new KdbxDatabase();
        } catch(Exception e) {
            return null;
        }
        
    }

    @Override
    public KdbxDatabase getDatabase(String name, Credentials credentials) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(name);
        return KdbxDatabase.load(credentials, inputStream);
    }

    @Override
    public void saveDatabase(Database database, Credentials credentials, OutputStream outputStream) throws IOException {
        database.save(credentials, outputStream);
    }

    @Override
    public KdbxDatabase loadDatabase(Credentials credentials, InputStream inputStream) throws IOException {
        return KdbxDatabase.load(credentials, inputStream);
    }


    @Override
    public boolean verifyStreamFormat(StreamFormat s1, StreamFormat s2) {
        KdbxHeader h1 = (KdbxHeader) s1.getStreamConfiguration();
        KdbxHeader h2 = (KdbxHeader) s1.getStreamConfiguration();
        return (h1.getVersion() == h2.getVersion() &&
                h1.getProtectedStreamAlgorithm().equals(h2.getProtectedStreamAlgorithm()) &&
                h1.getKeyDerivationFunction().equals(h2.getKeyDerivationFunction()) &&
                h1.getCipherAlgorithm().equals(h2.getCipherAlgorithm()));
    }
    
  
    @Override
    public Credentials getCreds(byte[] creds) {
        return new KdbxCreds(creds);
    }

}
