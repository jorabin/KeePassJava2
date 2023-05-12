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

package org.linguafranca.pwdb.kdbx.simple;

import org.junit.Test;
import org.linguafranca.pwdb.StreamFormat;
import org.linguafranca.pwdb.checks.SaveAndReloadChecks;
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.kdbx.KdbxCreds;
import org.linguafranca.pwdb.Credentials;
import org.linguafranca.pwdb.kdbx.KdbxHeader;
import org.linguafranca.pwdb.kdbx.KdbxSerializer;

import java.io.*;

import static org.junit.Assert.assertTrue;
import static org.linguafranca.util.TestUtil.getTestPrintStream;

/**
 * @author jo
 */
public class SimpleSaveAndReloadTest extends SaveAndReloadChecks {

    static PrintStream printStream = getTestPrintStream();

    @Override
    public Database getDatabase() {
        return new SimpleDatabase();
    }

    @Override
    public Database getDatabase(String s, Credentials credentials) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(s);
        try {
            return SimpleDatabase.load(credentials, inputStream);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public void saveDatabase(Database database, Credentials credentials, OutputStream outputStream) throws IOException {
        database.save(credentials, outputStream);
    }

    @Override
    public Database loadDatabase(Credentials credentials, InputStream inputStream) throws IOException {
        try {
            return SimpleDatabase.load(credentials, inputStream);
        } catch (Exception e) {
            throw new IOException(e);
        }
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


    // check that boolean comes out in upper case - Simple Converters don't work on attributes
    // so this is done in the output transformer
    @Test
    public void uppercaseBooleanTest() throws IOException {
        SimpleDatabase s = new SimpleDatabase();
        SimpleEntry e = s.newEntry();
        e.setPassword("12345");
        s.getRootGroup().addEntry(e);
        File file = File.createTempFile("kdbx", "kdbx");
        s.save(new KdbxCreds("123".getBytes()), new FileOutputStream(file));
        InputStream inputStream = new FileInputStream(file);
        Credentials credentials = new KdbxCreds("123".getBytes());
        InputStream decryptedInputStream = KdbxSerializer.createUnencryptedInputStream(credentials, new KdbxHeader(), inputStream);
        BufferedReader br = new BufferedReader(new InputStreamReader(decryptedInputStream));
        boolean foundValue = false;
        while (br.ready()) {
            String string = br.readLine();
            if (string.trim().startsWith("<Value Protected=")) {
                assertTrue(string.contains("True"));
                foundValue = true;
            }
            printStream.println(string);
        }
        assertTrue(foundValue);
    }


    @Override
    public Credentials getCreds(byte[] creds) {
        return new KdbxCreds(creds);
    }
}
