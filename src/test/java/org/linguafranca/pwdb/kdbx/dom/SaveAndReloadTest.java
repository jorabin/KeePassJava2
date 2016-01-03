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

package org.linguafranca.pwdb.kdbx.dom;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.linguafranca.pwdb.Visitor;
import org.linguafranca.pwdb.kdbx.StreamFormat;
import org.linguafranca.security.Credentials;
import org.linguafranca.pwdb.kdbx.KdbxCredentials;
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Group;

import java.io.*;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * @author jo
 */
public class SaveAndReloadTest {


    @Test
    public void saveAndReloadTest() throws IOException {
        DomDatabaseWrapper output = createNewDatabase();

        File temp = File.createTempFile("temp", "temp");
        FileOutputStream fos = new FileOutputStream(temp);
        output.save(new KdbxCredentials.Password("123".getBytes()), fos);

        FileInputStream fis = new FileInputStream(temp);
        DomDatabaseWrapper input = DomDatabaseWrapper.load(new KdbxCredentials.Password("123".getBytes()), fis);

        input.save(new StreamFormat.None(), new Credentials.None(), System.out);

        for (Integer g = 0; g< 5; g++){
            Group group = input.getRootGroup().getGroups().get(g);
            assertEquals(g.toString(), group.getName());
            assertEquals(g + 1, group.getEntries().size());
            for (int e = 0; e <= g; e++) {
                Entry entry = group.getEntries().get(e);
                assertEquals(g + "-" + e, entry.getTitle());
                assertEquals(g + " - un - " + e, entry.getUsername());
                assertEquals(g + "- p -" + e, entry.getPassword());
                assertEquals(g + "- url - " + e, entry.getUrl());
                assertEquals(g + "- n - " + e, entry.getNotes());
            }
        }

    }

    private DomDatabaseWrapper createNewDatabase() throws IOException {
        DomDatabaseWrapper database = new DomDatabaseWrapper();

        for (Integer g = 0; g < 5; g++){
            Group group = database.getRootGroup().addGroup(database.newGroup(g.toString()));
            for (int e = 0; e <= g; e++) {
                group.addEntry(entryFactory(database, g.toString(), e));
            }
        }

        return database;
    }

    private Entry entryFactory(Database database, String g, int e) {
        Entry result = database.newEntry();
        result.setTitle(g + "-" + e);
        result.setUsername(g + " - un - " + e);
        result.setPassword(g + "- p -" + e);
        result.setUrl(g + "- url - " + e);
        result.setNotes(g + "- n - " + e);
        return result;
    }

    /**
     * Outputs the database to a file - we can try to read it in other versions of the program. Run "manually".
     *
     * @throws IOException
     */
    @Test @Ignore
    public void saveNewDatabase () throws IOException {
        DomDatabaseWrapper database = createNewDatabase();

        FileOutputStream outputStream = new FileOutputStream("compatibility.kdbx");
        database.save(new KdbxCredentials.Password("123".getBytes()), outputStream);
    }

    /**
     * Doesn't do anything other than output the database using default PrintVisitor
     * @throws IOException
     */
    @Test
    public void inspectNewDatabase () throws IOException {
        DomDatabaseWrapper database = createNewDatabase();

        database.visit(new Visitor.Print());
    }

    // create a new database for messing around with
    // the assertions here somewhat duplicate those in BasicDatabaseChecks
    @Test
    public void testNewDatabase() throws IOException {
        DomDatabaseWrapper database = new DomDatabaseWrapper();
        Group root = database.getRootGroup();
        assertTrue(root.isRootGroup());
        assertEquals(0, root.getGroups().size());
        assertEquals(0, root.getEntries().size());

        assertTrue(database.shouldProtect("Password"));
        assertFalse(database.shouldProtect("Title"));
        assertFalse(database.shouldProtect("Bogus"));

        assertEquals("New Database", database.getName());
        database.setName("Modified Database");
        assertEquals("Modified Database", database.getName());

        assertEquals("Empty Database", database.getDescription());
        database.setDescription("Test Database");
        assertEquals("Test Database", database.getDescription());

        Group group1 = database.newGroup("Group 1");
        UUID newGroupUUID = group1.getUuid();

        root.addGroup(group1);
        assertEquals("Group 1", group1.getName());
        assertFalse(group1.isRootGroup());
        assertTrue(root.isRootGroup());

        assertEquals(1, root.getGroups().size());
        assertEquals(newGroupUUID, root.getGroups().get(0).getUuid());

        group1.setParent(root);
        root.addGroup(group1);

        root.removeGroup(group1);
        assertTrue(group1.getParent() == null);
        assertEquals(0, root.getGroups().size());
        root.addGroup(group1);
        assertEquals(1, root.getGroups().size());
        assertEquals(newGroupUUID, root.getGroups().get(0).getUuid());

        try {
            root.setParent(group1);
            fail("Cannot add root group to another group");
        } catch (Exception ignored) {
        }

        Group group2 = database.newGroup();
        group2.setName("Group 2");
        group1.addGroup(group2);
        assertEquals(1, group1.getGroups().size());
        assertEquals(1, root.getGroups().size());

        root.addGroup(group2);
        assertEquals(0, group1.getGroups().size());
        assertEquals(2, root.getGroups().size());

        Entry entry1 = database.newEntry();
        entry1.setTitle("A new entry");
        assertEquals("A new entry", entry1.getTitle());
        entry1.setUsername("user name");
        assertEquals("user name", entry1.getUsername());
        entry1.setProperty("random", "new");
        assertEquals("new", entry1.getProperty("random"));
        entry1.setProperty("random", "old");
        assertEquals("old", entry1.getProperty("random"));


        group2.addEntry(entry1);

        assertEquals(1, group2.getEntries().size());
        entry1.setPassword("pass");
        assertEquals("pass", entry1.getPassword());

        Entry entry2 = database.newEntry(entry1);
        entry2.setPassword("pass2");
        assertEquals("pass2", entry2.getPassword());
        group2.addEntry(entry2);

        assertEquals(2, group2.getEntries().size());
        root.removeGroup(group1);
        root.removeGroup(group2);

        assertEquals(0, root.getGroups().size());
        Assert.assertEquals(0, database.findEntries("").size());
    }
}
