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

package org.linguafranca.pwdb.kdb;

import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.Visitor;
import org.linguafranca.pwdb.test.DatabaseTestBase;
import org.linguafranca.pwdb.test.GroupsAndEntriesTest;
import org.junit.jupiter.api.Test;
import org.linguafranca.pwdb.test.Test123Test;
import org.linguafranca.util.CloseTracking;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.linguafranca.util.TestUtil.getTestPrintStream;

/**
 * @author Jo
 */
public class KdbDatabaseTest
        extends
            DatabaseTestBase
        implements
            GroupsAndEntriesTest,
            Test123Test {

    KdbDatabaseTest() {
        super(KdbDatabase::new, KdbDatabase::read, Database::write, KdbCredentials.Password::new);
        newDatabase();
    }

    @Override
    public boolean getSkipDateCheck() {
        return true;
    }

    @Override
    public String getFileName(){
        return "test123.kdb";
    }

    @Test
    public void supportedFunctionalityTest(){
        assertFalse(getDatabase().supportsBinaryProperties());
        assertFalse(getDatabase().supportsNonStandardPropertyNames());
        assertFalse(getDatabase().supportsRecycleBin());
        assertFalse(getDatabase().supportsPropertyValueStrategy());
    }

    @Test
    public void testCreateKdbDatabase() throws Exception {
        Database database;
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test123.kdb")) {
            database = KdbDatabase.read(new KdbCredentials.Password("123".getBytes()), inputStream);
        }
        database.visit(new Visitor.Print(getTestPrintStream()));
    }

    /**
     * Issue 109: read leaves the caller's stream open, the deprecated load closes it
     */
    @Test
    @SuppressWarnings("deprecation")
    public void readLeavesStreamOpenLoadCloses() throws IOException {
        byte[] bytes;
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test123.kdb")) {
            assert inputStream != null;
            bytes = inputStream.readAllBytes();
        }
        CloseTracking.InputStream inputStream = new CloseTracking.InputStream(bytes);
        KdbDatabase database = KdbDatabase.read(new KdbCredentials.Password("123".getBytes()), inputStream);
        assertFalse(inputStream.isClosed());
        assertFalse(database.getRootGroup().getGroups().isEmpty());

        inputStream = new CloseTracking.InputStream(bytes);
        KdbDatabase.load(new KdbCredentials.Password("123".getBytes()), inputStream);
        assertTrue(inputStream.isClosed());
    }

    @Test
    public void openKdbWithKeyFile() throws IOException {
        InputStream key = getClass().getClassLoader().getResourceAsStream("kdb.key");
        KdbCredentials creds = new KdbCredentials.KeyFile("123".getBytes(), key);
        KdbDatabase db;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("kdbwithkey.kdb")) {
            db = KdbDatabase.read(creds, is);
        }
        assertEquals(1, db.getRootGroup().getGroupsCount());
        assertEquals("General", db.getRootGroup().getGroups().get(0).getName());
    }
}
