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

package org.linguafranca.pwdb.test;

import com.google.common.io.ByteStreams;
import org.junit.jupiter.api.Test;
import org.linguafranca.pwdb.Credentials;
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.Entry;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;


public interface BinaryPropertiesTest {

    void newDatabase();
    Database getDatabase();
    Database createDatabase();
    void saveDatabase(Database database, Credentials credentials, OutputStream outputStream) throws IOException;
    Database loadDatabase(Credentials credentials, InputStream inputStream) throws IOException;
    Credentials getCredentials(byte[] credentials);

    /**
     * Verify that the database correctly reports that its supports binary attachments - override for
     * databases that don't
     */
    @Test
    default void checkSupported() {
        assertTrue(getDatabase().supportsBinaryProperties());
    }

    /**
     * Retrieve and verify attachment "letter J"
     */
    @Test
    default void getBinaryProperty() throws Exception {
        Entry entry = getDatabase().findEntries("Test attachment").get(0);
        byte[] letterJ = entry.getBinaryProperty("letter J.jpeg");
        InputStream testFile = getClass().getClassLoader().getResourceAsStream("letter J.jpeg");
        assert testFile != null;
        byte[] original = ByteStreams.toByteArray(testFile);
        assertArrayEquals(original, letterJ);
    }

    /**
     * Retrieve and verify attachment "letter L"
     */
    @Test
    default void getAnotherBinaryProperty() throws Exception {
        Entry entry = getDatabase().findEntries("Test 2 attachment").get(0);
        byte[] letterL = entry.getBinaryProperty("letter L.jpeg");
        InputStream testFile = getClass().getClassLoader().getResourceAsStream("letter L.jpeg");
        assert testFile != null;
        byte[] original = ByteStreams.toByteArray(testFile);
        assertArrayEquals(original, letterL);
    }

    /**
     * Add the Letter L to the entry containing letter J
     */
    @Test
    default void setBinaryProperty() throws Exception {
        InputStream testFile = getClass().getClassLoader().getResourceAsStream("letter L.jpeg");
        assert testFile != null;
        byte[] original = ByteStreams.toByteArray(testFile);
        Entry entry = getDatabase().findEntries("Test attachment").get(0);
        entry.setBinaryProperty("letter L.jpeg", original);
        byte[] letterL = entry.getBinaryProperty("letter L.jpeg");
        assertArrayEquals(original, letterL);
        assertArrayEquals(new String[]{"letter J.jpeg", "letter L.jpeg"}, entry.getBinaryPropertyNames().toArray());
        assertTrue(getDatabase().isDirty());
    }

    /**
     * Verify that the entries have the right attachments
     */
    @Test
    default void getBinaryPropertyNames() {
        Entry entry = getDatabase().findEntries("Test attachment").get(0);
        assertArrayEquals(new String[]{"letter J.jpeg"}, entry.getBinaryPropertyNames().toArray());

        entry = getDatabase().findEntries("Test 2 attachment").get(0);
        assertArrayEquals(new String[]{"letter J.jpeg", "letter L.jpeg"}, entry.getBinaryPropertyNames().toArray());
    }

    /**
     * Verify that the binary properties can be added and removed correctly
     */
    @Test
    default void checkAddChangeRemoveBinaryProperty() {
        byte[] test = new byte[]{0, 1, 2, 3};
        byte[] test2 = new byte[]{3, 2, 1, 0};
        Entry entry = getDatabase().findEntries("Test attachment").get(0);
        assertEquals(1, entry.getBinaryPropertyNames().size());
        entry.setBinaryProperty("test", test);
        assertArrayEquals(test, entry.getBinaryProperty("test"));
        entry.setBinaryProperty("test", test2);
        assertArrayEquals(test2, entry.getBinaryProperty("test"));
        // true that property was removed
        assertTrue(entry.removeBinaryProperty("test"));
        // false that same property was removed
        assertFalse(entry.removeBinaryProperty("test"));
        // false that non-existent was removed
        assertFalse(entry.removeBinaryProperty("test-test"));
        // same number of properties as we started with
        assertEquals(1, entry.getBinaryPropertyNames().size());
    }

    /**
     * Checks that a database with binary properties saves and reloads correctly
     */
    @Test
    default void saveAndReloadCheck() throws IOException {
        Path file = Files.createTempFile("test8", "tmp");
        saveDatabase(getDatabase(), getCredentials("123".getBytes()), Files.newOutputStream(file));

        Database db = loadDatabase(getCredentials("123".getBytes()), Files.newInputStream(file));
        Entry newEntry = db.findEntries("Test attachment").get(0);
        assertArrayEquals(new String[]{"letter J.jpeg"}, newEntry.getBinaryPropertyNames().toArray());
        Entry oldEntry = getDatabase().findEntries("Test attachment").get(0);
        assertArrayEquals(oldEntry.getBinaryProperty("letter J.jpeg"), newEntry.getBinaryProperty("letter J.jpeg"));

        newEntry = db.findEntries("Test 2 attachment").get(0);
        assertArrayEquals(new String[]{"letter J.jpeg", "letter L.jpeg"}, newEntry.getBinaryPropertyNames().toArray());
        oldEntry = getDatabase().findEntries("Test 2 attachment").get(0);
        assertArrayEquals(oldEntry.getBinaryProperty("letter J.jpeg"), newEntry.getBinaryProperty("letter J.jpeg"));
        assertArrayEquals(oldEntry.getBinaryProperty("letter L.jpeg"), newEntry.getBinaryProperty("letter L.jpeg"));

    }

    /**
     * Checks that a new database can add binary properties saves and reloads correctly
     */
    @Test
    default void createAndSaveCheck() throws IOException {
        Path file = Files.createTempFile("keepass", "tmp");
        Database database1 = createDatabase();
        Entry entry = database1.newEntry("Test attachment");
        database1.getRootGroup().addEntry(entry);
        InputStream testFile = getClass().getClassLoader().getResourceAsStream("letter J.jpeg");
        assert testFile != null;
        byte[] letterJ = ByteStreams.toByteArray(testFile);
        entry.setBinaryProperty("letter J.jpeg", letterJ);
        saveDatabase(database1, getCredentials("123".getBytes()), Files.newOutputStream(file));

        Database db = loadDatabase(getCredentials("123".getBytes()), Files.newInputStream(file));
        Entry entry1 = db.findEntries("Test attachment").get(0);
        // just one property
        assertArrayEquals(new String[]{"letter J.jpeg"}, entry1.getBinaryPropertyNames().toArray());
        // content is correct
        assertArrayEquals(letterJ, entry1.getBinaryProperty("letter J.jpeg"));
    }
}
