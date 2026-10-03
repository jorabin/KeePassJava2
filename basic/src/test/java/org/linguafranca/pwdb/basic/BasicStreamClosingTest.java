/*
 * Copyright (c) 2026. Jo Rabin
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

package org.linguafranca.pwdb.basic;

import org.junit.jupiter.api.Test;
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.format.KdbxCredentials;
import org.linguafranca.pwdb.format.KdbxHeader;
import org.linguafranca.pwdb.format.KdbxSerializer;
import org.linguafranca.pwdb.format.KdbxStreamFormat;
import org.linguafranca.util.CloseTracking;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Issue 109: write and read leave the caller's stream open, the deprecated save and load close it
 */
public class BasicStreamClosingTest {

    private static final KdbxCredentials CREDENTIALS = new KdbxCredentials("123".getBytes());

    private static BasicDatabase createDatabase() {
        BasicDatabase database = new BasicDatabase();
        database.getRootGroup().addEntry("Test entry");
        return database;
    }

    private static void assertContents(Database database) {
        assertEquals(1, database.findEntries("Test entry").size());
    }

    private static Database readKdbx(byte[] bytes) throws IOException {
        KdbxHeader header = new KdbxHeader();
        try (InputStream inputStream = KdbxSerializer.createUnencryptedInputStream(CREDENTIALS, header,
                new CloseTracking.InputStream(bytes))) {
            return new BasicDatabaseSerializer.Xml(header.getInnerStreamEncryptor()).read(inputStream);
        }
    }

    @Test
    public void writeKdbxLeavesStreamOpen() throws IOException {
        CloseTracking.OutputStream outputStream = new CloseTracking.OutputStream();
        createDatabase().write(new KdbxStreamFormat(), CREDENTIALS, outputStream);
        assertFalse(outputStream.isClosed());
        assertContents(readKdbx(outputStream.toByteArray()));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void saveKdbxClosesStream() throws IOException {
        CloseTracking.OutputStream outputStream = new CloseTracking.OutputStream();
        createDatabase().save(new KdbxStreamFormat(), CREDENTIALS, outputStream);
        assertTrue(outputStream.isClosed());
        assertContents(readKdbx(outputStream.toByteArray()));
    }

    @Test
    public void serializerWriteAndReadLeaveStreamOpen() throws IOException {
        BasicDatabaseSerializer serializer = new BasicDatabaseSerializer.Xml();
        CloseTracking.OutputStream outputStream = new CloseTracking.OutputStream();
        serializer.write(createDatabase(), outputStream);
        assertFalse(outputStream.isClosed());

        CloseTracking.InputStream inputStream = new CloseTracking.InputStream(outputStream.toByteArray());
        assertContents(serializer.read(inputStream));
        assertFalse(inputStream.isClosed());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void serializerSaveAndLoadCloseStream() throws IOException {
        BasicDatabaseSerializer serializer = new BasicDatabaseSerializer.Xml();
        CloseTracking.OutputStream outputStream = new CloseTracking.OutputStream();
        serializer.save(createDatabase(), outputStream);
        assertTrue(outputStream.isClosed());

        CloseTracking.InputStream inputStream = new CloseTracking.InputStream(outputStream.toByteArray());
        assertContents(serializer.load(inputStream));
        assertTrue(inputStream.isClosed());
    }
}
