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
import org.linguafranca.pwdb.StreamFormat;
import org.linguafranca.pwdb.format.KdbxCredentials;
import org.linguafranca.pwdb.format.KdbxHeader;
import org.linguafranca.pwdb.format.KdbxStreamFormat;
import org.linguafranca.pwdb.kdbx.jackson.KdbxDatabase;
import org.linguafranca.util.CloseTracking;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Issue 109: write and read leave the caller's stream open, the deprecated save and load close it
 */
public class KdbxStreamClosingTest {

    private static final Credentials CREDENTIALS = new KdbxCredentials("123".getBytes());

    private static KdbxDatabase createDatabase() {
        KdbxDatabase database = new KdbxDatabase();
        database.getRootGroup().addEntry(database.newEntry("Test entry"));
        return database;
    }

    private static void assertContents(KdbxDatabase database) {
        assertEquals(1, database.findEntries("Test entry").size());
    }

    @Test
    public void writeAndReadKdbxV3LeaveStreamOpen() throws IOException {
        writeAndReadKdbxLeaveStreamOpen(3);
    }

    @Test
    public void writeAndReadKdbxV4LeaveStreamOpen() throws IOException {
        writeAndReadKdbxLeaveStreamOpen(4);
    }

    private static void writeAndReadKdbxLeaveStreamOpen(int version) throws IOException {
        CloseTracking.OutputStream outputStream = new CloseTracking.OutputStream();
        createDatabase().write(new KdbxStreamFormat(new KdbxHeader(version)), CREDENTIALS, outputStream);
        assertFalse(outputStream.isClosed());

        CloseTracking.InputStream inputStream = new CloseTracking.InputStream(outputStream.toByteArray());
        KdbxDatabase database = KdbxDatabase.read(CREDENTIALS, inputStream);
        assertFalse(inputStream.isClosed());
        assertContents(database);
        assertEquals(version, ((KdbxStreamFormat) database.getStreamFormat()).getStreamConfiguration().getVersion());
    }

    @Test
    public void saveAndLoadKdbxV3CloseStream() throws IOException {
        saveAndLoadKdbxCloseStream(3);
    }

    @Test
    public void saveAndLoadKdbxV4CloseStream() throws IOException {
        saveAndLoadKdbxCloseStream(4);
    }

    @SuppressWarnings("deprecation")
    private static void saveAndLoadKdbxCloseStream(int version) throws IOException {
        CloseTracking.OutputStream outputStream = new CloseTracking.OutputStream();
        createDatabase().save(new KdbxStreamFormat(new KdbxHeader(version)), CREDENTIALS, outputStream);
        assertTrue(outputStream.isClosed());

        CloseTracking.InputStream inputStream = new CloseTracking.InputStream(outputStream.toByteArray());
        assertContents(KdbxDatabase.load(CREDENTIALS, inputStream));
        assertTrue(inputStream.isClosed());
    }

    @Test
    public void writeWithDefaultFormatLeavesStreamOpen() throws IOException {
        CloseTracking.OutputStream outputStream = new CloseTracking.OutputStream();
        createDatabase().write(CREDENTIALS, outputStream);
        assertFalse(outputStream.isClosed());
        assertContents(KdbxDatabase.read(CREDENTIALS, new CloseTracking.InputStream(outputStream.toByteArray())));
    }

    @Test
    public void writeAndReadXmlLeaveStreamOpen() throws IOException {
        CloseTracking.OutputStream outputStream = new CloseTracking.OutputStream();
        createDatabase().write(new StreamFormat.None(), new Credentials.None(), outputStream);
        assertFalse(outputStream.isClosed());
        // the caller can carry on writing
        outputStream.write("<!-- end -->".getBytes(StandardCharsets.UTF_8));
        String xml = outputStream.toString(StandardCharsets.UTF_8);
        assertTrue(xml.matches("(?s).*</KeePassFile>\\s*<!-- end -->"), xml.substring(Math.max(0, xml.length() - 40)));

        CloseTracking.InputStream inputStream = new CloseTracking.InputStream(outputStream.toByteArray());
        assertContents(KdbxDatabase.read(new StreamFormat.None(), new Credentials.None(), inputStream));
        assertFalse(inputStream.isClosed());

        inputStream = new CloseTracking.InputStream(outputStream.toByteArray());
        assertContents(KdbxDatabase.readXml(inputStream));
        assertFalse(inputStream.isClosed());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void saveAndLoadXmlCloseStream() throws Exception {
        CloseTracking.OutputStream outputStream = new CloseTracking.OutputStream();
        createDatabase().save(new StreamFormat.None(), new Credentials.None(), outputStream);
        assertTrue(outputStream.isClosed());

        CloseTracking.InputStream inputStream = new CloseTracking.InputStream(outputStream.toByteArray());
        assertContents(KdbxDatabase.load(new StreamFormat.None(), new Credentials.None(), inputStream));
        assertTrue(inputStream.isClosed());

        inputStream = new CloseTracking.InputStream(outputStream.toByteArray());
        assertContents(KdbxDatabase.loadXml(inputStream));
        assertTrue(inputStream.isClosed());
    }
}
