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
import static org.linguafranca.util.TestUtil.getTestPrintStream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;

import org.junit.Test;
import org.linguafranca.pwdb.checks.SaveAndReloadChecks;
import org.linguafranca.pwdb.kdbx.KdbxCreds;
import org.linguafranca.pwdb.kdbx.KdbxHeader;

import org.linguafranca.pwdb.kdbx.jackson.JacksonDatabase;
import org.linguafranca.pwdb.kdbx.jackson.JacksonEntry;
import org.linguafranca.pwdb.kdbx.jackson.JacksonGroup;
import org.linguafranca.pwdb.kdbx.jackson.JacksonIcon;


public class JacksonSaveAndReloadTest extends SaveAndReloadChecks<JacksonDatabase, JacksonGroup, JacksonEntry>{
 
    @Override
    public JacksonDatabase getDatabase() {
        try {
            return new JacksonDatabase();
        } catch(Exception e) {
            return null;
        }
        
    }

    @Override
    public JacksonDatabase getDatabase(String name, Credentials credentials) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(name);
        return JacksonDatabase.load(credentials, inputStream);
    }

    @Override
    public void saveDatabase(JacksonDatabase database, Credentials credentials, OutputStream outputStream) throws IOException {
        database.save(credentials, outputStream);
    }

    @Override
    public JacksonDatabase loadDatabase(Credentials credentials, InputStream inputStream) throws IOException {
        return JacksonDatabase.load(credentials, inputStream);
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
