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

package org.linguafranca.checks;

import com.google.common.io.ByteStreams;
import org.junit.Assert;
import org.junit.Test;
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.kdbx.StreamFormat;
import org.linguafranca.security.Credentials;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.Assert.*;
import static org.junit.Assert.assertArrayEquals;

/**
 * @author jo
 */
public abstract class EntryChecks {
    public Database database;
    @SuppressWarnings("unused")
    public abstract void saveDatabase(Database database, StreamFormat streamFormat, Credentials credentials, OutputStream outputStream) throws IOException;

    @Test
    public void getBinaryProperty() throws Exception {
        Entry entry = database.findEntries("Test attachment").get(0);
        byte [] letterJ = entry.getBinaryProperty("letter J.jpeg");
        InputStream testfile = getClass().getClassLoader().getResourceAsStream("letter J.jpeg");
        byte [] original = ByteStreams.toByteArray(testfile);
        assertArrayEquals(original, letterJ);
    }

    @Test
    public void getAnotherBinaryProperty() throws Exception {
        Entry entry = database.findEntries("Test 2 attachment").get(0);
        byte [] letterL = entry.getBinaryProperty("letter L.jpeg");
        InputStream testfile = getClass().getClassLoader().getResourceAsStream("letter L.jpeg");
        byte [] original = ByteStreams.toByteArray(testfile);
        assertArrayEquals(original, letterL);
    }

    @Test
    public void setBinaryProperty() throws Exception {
        InputStream testfile = getClass().getClassLoader().getResourceAsStream("letter L.jpeg");
        byte [] original = ByteStreams.toByteArray(testfile);
        Entry entry = database.findEntries("Test attachment").get(0);
        entry.setBinaryProperty("letter L.jpeg", original);
        byte [] letterL = entry.getBinaryProperty("letter L.jpeg");
        assertArrayEquals(original, letterL);
        assertArrayEquals(new String[] {"letter J.jpeg", "letter L.jpeg"}, entry.getBinaryPropertyNames().toArray());
        assertTrue(database.isDirty());
    }

    @Test
    public void getBinaryPropertyNames() throws Exception {
        Entry entry = database.findEntries("Test attachment").get(0);
        assertArrayEquals(new String[] {"letter J.jpeg"}, entry.getBinaryPropertyNames().toArray());

        entry = database.findEntries("Test 2 attachment").get(0);
        assertArrayEquals(new String[] {"letter J.jpeg", "letter L.jpeg"}, entry.getBinaryPropertyNames().toArray());
    }
}
