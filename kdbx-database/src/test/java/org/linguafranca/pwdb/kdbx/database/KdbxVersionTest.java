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
import org.linguafranca.pwdb.format.KdbxStreamFormat;
import org.linguafranca.pwdb.kdbx.jackson.KdbxDatabase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * New databases are written as KDBX 4.1, and databases that are read keep their version, minor version
 * included, when they are written
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

    @Test
    public void newDatabaseIsKdbx41() throws IOException {
        KdbxDatabase database = new KdbxDatabase();
        KdbxHeader header = ((KdbxStreamFormat) database.getStreamFormat()).getStreamConfiguration();
        assertEquals(4, header.getVersion());
        assertEquals(1, header.getMinorVersion());

        byte[] bytes = write(database);
        assertEquals(0x00040001, fileVersion(bytes));

        KdbxDatabase reread = KdbxDatabase.read(CREDENTIALS, new ByteArrayInputStream(bytes));
        assertEquals(1, ((KdbxStreamFormat) reread.getStreamFormat()).getStreamConfiguration().getMinorVersion());
    }

    @Test
    public void writeUsesTheFormatThatIsSet() throws IOException {
        KdbxDatabase database = new KdbxDatabase();
        database.setStreamFormat(new KdbxStreamFormat(new KdbxHeader(KdbxHeader.KdbxHeaderOpts.V4_AES_ARGON_CHA_CHA)));
        assertEquals(0x00040000, fileVersion(write(database)));
        database.setStreamFormat(new KdbxStreamFormat(new KdbxHeader(3)));
        assertEquals(0x00030001, fileVersion(write(database)));
        assertThrows(IllegalArgumentException.class, () -> database.setStreamFormat(null));
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
