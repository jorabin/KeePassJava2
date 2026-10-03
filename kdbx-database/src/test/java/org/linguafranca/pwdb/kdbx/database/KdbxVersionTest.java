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

package org.linguafranca.pwdb.kdbx.database;

import org.junit.jupiter.api.Test;
import org.linguafranca.pwdb.Credentials;
import org.linguafranca.pwdb.format.KdbxCredentials;
import org.linguafranca.pwdb.format.KdbxHeader;
import org.linguafranca.pwdb.format.KdbxSerializer;
import org.linguafranca.pwdb.format.KdbxStreamFormat;
import org.linguafranca.pwdb.kdbx.jackson.KdbxDatabase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * KDBX 4 databases are written as 4.1 only if they use 4.1 features, unless a version is set; databases
 * that are read keep their version, minor version included; and content a version can't hold is left out
 * when writing it, but kept in the database
 */
public class KdbxVersionTest {

    private static final Credentials CREDENTIALS = new KdbxCredentials("123".getBytes());

    /** The file version, which follows the two signature words */
    private static int fileVersion(byte[] bytes) {
        return ByteBuffer.wrap(bytes, 8, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    private static byte[] write(KdbxDatabase database) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        database.write(CREDENTIALS, outputStream);
        return outputStream.toByteArray();
    }

    private static String xml(byte[] bytes) throws IOException {
        try (InputStream inputStream = KdbxSerializer.createUnencryptedInputStream(CREDENTIALS, new KdbxHeader(),
                new ByteArrayInputStream(bytes))) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private KdbxDatabase read(String resource) throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(inputStream, resource);
            return KdbxDatabase.read(CREDENTIALS, inputStream);
        }
    }

    @Test
    public void newDatabaseWithout41FeaturesIsKdbx40() throws IOException {
        KdbxDatabase database = new KdbxDatabase();
        KdbxHeader header = ((KdbxStreamFormat) database.getStreamFormat()).getStreamConfiguration();
        assertEquals(4, header.getVersion());
        assertTrue(header.isMinorVersionAutomatic());

        byte[] bytes = write(database);
        assertEquals(0x00040000, fileVersion(bytes));

        KdbxDatabase reread = KdbxDatabase.read(CREDENTIALS, new ByteArrayInputStream(bytes));
        KdbxHeader rereadHeader = ((KdbxStreamFormat) reread.getStreamFormat()).getStreamConfiguration();
        assertEquals(0, rereadHeader.getMinorVersion());
        assertFalse(rereadHeader.isMinorVersionAutomatic());
    }

    @Test
    public void versionSetIsWritten() throws IOException {
        KdbxDatabase database = new KdbxDatabase();
        database.setStreamFormat(new KdbxStreamFormat(new KdbxHeader(KdbxHeader.KdbxHeaderOpts.V4_1_AES_ARGON_CHA_CHA)));
        assertEquals(0x00040001, fileVersion(write(database)));
        database.setStreamFormat(new KdbxStreamFormat(new KdbxHeader(KdbxHeader.KdbxHeaderOpts.V4_AES_ARGON_CHA_CHA)));
        assertEquals(0x00040000, fileVersion(write(database)));
        database.setStreamFormat(new KdbxStreamFormat(new KdbxHeader(3)));
        assertEquals(0x00030001, fileVersion(write(database)));
        assertThrows(IllegalArgumentException.class, () -> database.setStreamFormat(null));
    }

    @Test
    public void with41FeaturesAutomaticIsKdbx41() throws IOException {
        KdbxDatabase database = read("Database-4.1-123.kdbx");
        database.setStreamFormat(new KdbxStreamFormat(new KdbxHeader(4)));
        byte[] bytes = write(database);
        assertEquals(0x00040001, fileVersion(bytes));
        assertTrue(xml(bytes).contains("<PreviousParentGroup>"));
    }

    @Test
    public void writing40LeavesOut41Features() throws IOException {
        KdbxDatabase database = read("Database-4.1-123.kdbx");
        database.setStreamFormat(new KdbxStreamFormat(new KdbxHeader(KdbxHeader.KdbxHeaderOpts.V4_AES_ARGON_CHA_CHA)));
        byte[] bytes = write(database);
        assertEquals(0x00040000, fileVersion(bytes));
        assertFalse(xml(bytes).contains("<PreviousParentGroup>"));

        // the database still has them
        database.setStreamFormat(new KdbxStreamFormat(new KdbxHeader(4)));
        assertTrue(xml(write(database)).contains("<PreviousParentGroup>"));
    }

    @Test
    public void writing31LeavesOut4Features() throws IOException {
        KdbxDatabase database = read("Database-4.1-123.kdbx");
        database.setStreamFormat(new KdbxStreamFormat(new KdbxHeader(3)));
        byte[] bytes = write(database);
        assertEquals(0x00030001, fileVersion(bytes));
        String xml = xml(bytes);
        assertFalse(xml.contains("<PreviousParentGroup>"));
        assertFalse(xml.contains("<SettingsChanged>"));
        // CustomData only in Meta
        assertTrue(xml.indexOf("<CustomData") == xml.lastIndexOf("<CustomData"));
        // and it reads back
        KdbxDatabase.read(CREDENTIALS, new ByteArrayInputStream(bytes));
    }

    @Test
    public void versionIsKeptKdbx31() throws IOException {
        checkVersionKept("test123.kdbx", 0x00030001);
    }

    @Test
    public void versionIsKeptKdbx40() throws IOException {
        checkVersionKept("V4-AES-Argon2.kdbx", 0x00040000);
    }

    @Test
    public void versionIsKeptKdbx41() throws IOException {
        checkVersionKept("Database-4.1-123.kdbx", 0x00040001);
    }

    private void checkVersionKept(String resource, int expectedVersion) throws IOException {
        byte[] original;
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(inputStream, resource);
            original = inputStream.readAllBytes();
        }
        assertEquals(expectedVersion, fileVersion(original), resource);

        KdbxDatabase database = KdbxDatabase.read(CREDENTIALS, new ByteArrayInputStream(original));
        assertEquals(expectedVersion, fileVersion(write(database)), resource);
    }
}
