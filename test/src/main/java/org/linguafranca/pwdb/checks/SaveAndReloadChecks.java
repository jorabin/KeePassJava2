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

package org.linguafranca.pwdb.checks;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.linguafranca.pwdb.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.linguafranca.util.TestUtil.getTestPrintStream;

/**
 * @author jo
 */
public abstract class SaveAndReloadChecks <D extends Database<D, G, E, I>, G extends Group<D,G,E,I>, E extends Entry<D,G,E,I>, I extends Icon>{

    static PrintStream printStream = getTestPrintStream();

    public abstract D getDatabase();
    public abstract D getDatabase(String name, Credentials credentials) throws IOException;

    public abstract void saveDatabase(D database, Credentials credentials, OutputStream outputStream) throws IOException;
    public abstract D loadDatabase(Credentials credentials, InputStream inputStream) throws IOException;
    public abstract Credentials getCreds(byte[] creds);
    public abstract boolean verifyStreamFormat (StreamFormat<?> s1, StreamFormat<?> s2);

    @BeforeClass
    public static void ensureOutputDir() throws IOException {
        Files.createDirectories(Paths.get("testOutput"));
    }

    /**
     * Test verifies that entries contain the same content on reload as they did on save,
     * and also verifies that saving doesn't alter the contents
     */
    @Test
    public void saveAndReloadTest() throws IOException {

        long now = System.currentTimeMillis();

        // create database with known content
        D output = createNewDatabase();
        verifyContents(output);
        //output.save(new StreamFormat.None(), new Credentials.None(), printStream);

        FileOutputStream fos = new FileOutputStream("testOutput/test1.kdbx");
        saveDatabase(output, getCreds("123".getBytes()), fos);
        Assert.assertFalse(output.isDirty());
        fos.flush();
        fos.close();
        // make sure that saving didn't mess up content
        verifyContents(output);
        //output.save(new StreamFormat.None(), new Credentials.None(), printStream);


        FileInputStream fis = new FileInputStream("testOutput/test1.kdbx");
        D input = loadDatabase(getCreds("123".getBytes()), fis);
        verifyContents(input);
        //input.save(new StreamFormat.None(),  new Credentials.None(), printStream);
        printStream.format("Test took %d millis", System.currentTimeMillis() - now);
    }

    @Test
    public void saveAndReloadTestPasswordBytes() throws IOException {

        long now = System.currentTimeMillis();

        // create database with known content
        D output = createNewDatabase();
        verifyContentsPasswordBytes(output);
        //output.save(new StreamFormat.None(), new Credentials.None(), printStream);

        FileOutputStream fos = new FileOutputStream("testOutput/test1.kdbx");
        saveDatabase(output, getCreds("123".getBytes()), fos);
        Assert.assertFalse(output.isDirty());
        fos.flush();
        fos.close();
        // make sure that saving didn't mess up content
        verifyContentsPasswordBytes(output);
        //output.save(new StreamFormat.None(), new Credentials.None(), printStream);


        FileInputStream fis = new FileInputStream("testOutput/test1.kdbx");
        D input = loadDatabase(getCreds("123".getBytes()), fis);
        verifyContentsPasswordBytes(input);
        //input.save(new StreamFormat.None(),  new Credentials.None(), printStream);
        printStream.format("Test took %d millis", System.currentTimeMillis() - now);
    }

    /**
     * Test verifies that attachments are saved and reloaded correctly
     */
    @Test
    public void saveAndReloadTest2() throws IOException {
        D attachment = getDatabase("Attachment.kdbx", getCreds("123".getBytes()));

        E entry = attachment.findEntries("Test attachment").get(0);
        assertArrayEquals(new String[] {"letter J.jpeg"}, entry.getBinaryPropertyNames().toArray());

        E entry2 = attachment.findEntries("Test 2 attachment").get(0);
        assertArrayEquals(new String[] {"letter J.jpeg", "letter L.jpeg"}, entry2.getBinaryPropertyNames().toArray());

        byte[] content = entry2.getBinaryProperty("letter L.jpeg");
        entry.setBinaryProperty("letter L.jpeg", content);
        assertArrayEquals(new String[] {"letter J.jpeg", "letter L.jpeg"}, entry.getBinaryPropertyNames().toArray());

        FileOutputStream fos = new FileOutputStream("testOutput/test2.kdbx");
        saveDatabase(attachment, getCreds("123".getBytes()), fos);
        fos.flush();
        fos.close();

        FileInputStream fis = new FileInputStream("testOutput/test2.kdbx");
        D input = loadDatabase(getCreds("123".getBytes()), fis);

        entry = input.findEntries("Test attachment").get(0);
        assertArrayEquals(new String[] {"letter J.jpeg", "letter L.jpeg"}, entry.getBinaryPropertyNames().toArray());

        //saveDatabase(input, new StreamFormat.None(), new Credentials.None(), printStream);
    }

    String [] testFiles = {"V4-AES-AES.kdbx",
            "V4-AES-Argon2.kdbx",
            "V4-ChaCha20-AES.kdbx",
            "V4-ChaCha20-Argon2-Attachment.kdbx"};

    /***
     * Test verifies that database is saved with same encryption that it was loaded with
     */
    @Test
    public void saveAndReloadTest3() throws IOException {
        for (String resource: testFiles) {
            D database = getDatabase(resource, this.getCreds("123".getBytes()));
            StreamFormat<?> format1 = database.getStreamFormat();

            database.save(getCreds("123".getBytes()), Files.newOutputStream(Paths.get("testOutput/test3.kdbx")));

            FileInputStream fis = new FileInputStream("testOutput/test3.kdbx");
            D input = loadDatabase(getCreds("123".getBytes()), fis);
            StreamFormat<?> format2 = input.getStreamFormat();
            assertTrue(verifyStreamFormat(format1, format2));
        }
    }

    private D createNewDatabase() throws IOException {
        D database = getDatabase();

        for (@SuppressWarnings("WrapperTypeMayBePrimitive") Integer g = 0; g < 5; g++){
            G group = database.getRootGroup().addGroup(database.newGroup(g.toString()));
            for (int e = 0; e <= g; e++) {
                group.addEntry(entryFactory(database, g.toString(), e));
            }
        }

        return database;
    }

    private E entryFactory(D database, String g, int e) {
        E result = database.newEntry();
        result.setTitle(g + "-" + e);
        result.setUsername(g + " - un - " + e);
        result.setPassword(g + "- p -" + e);
        result.setUrl(g + "- url - " + e);
        result.setNotes(g + "- n - " + e);
        return result;
    }

    private void verifyContents(D database) {
        for (Integer g = 0; g< 5; g++){
            G group = database.getRootGroup().getGroups().get(g);
            assertEquals(g.toString(), group.getName());
            assertEquals(g + 1, group.getEntries().size());
            assertEquals(g+1, group.getEntriesCount());
            assertEquals(database.getRootGroup(), group.getParent());
            for (int e = 0; e <= g; e++) {
                E entry = group.getEntries().get(e);
                assertEquals(g + "-" + e, entry.getTitle());
                assertEquals(g + " - un - " + e, entry.getUsername());
                assertEquals(g + "- p -" + e, entry.getPassword());
                assertEquals(g + "- url - " + e, entry.getUrl());
                assertEquals(g + "- n - " + e, entry.getNotes());
                assertEquals(group, entry.getParent());
            }
        }
    }

    private void verifyContentsPasswordBytes(D database) {
        for (Integer g = 0; g< 5; g++){
            G group = database.getRootGroup().getGroups().get(g);
            assertEquals(g.toString(), group.getName());
            assertEquals(g + 1, group.getEntries().size());
            assertEquals(g+1, group.getEntriesCount());
            assertEquals(database.getRootGroup(), group.getParent());
            for (int e = 0; e <= g; e++) {
                E entry = group.getEntries().get(e);
                assertEquals(g + "-" + e, entry.getTitle());
                assertEquals(g + " - un - " + e, entry.getUsername());
                assertEquals(g + "- p -" + e, new String(entry.getPasswordAsBytes()));
                assertEquals(g + "- url - " + e, entry.getUrl());
                assertEquals(g + "- n - " + e, entry.getNotes());
                assertEquals(group, entry.getParent());
            }
        }
    }

    /**
     * Outputs the database to a file - we can try to read it in other versions of the program. Run "manually".
     *
     * @throws IOException when naughty
     */
    @Test @Ignore
    public void saveNewDatabase () throws IOException {
        D database = createNewDatabase();

        FileOutputStream outputStream = new FileOutputStream("compatibility.kdbx");
        saveDatabase(database, getCreds("123".getBytes()), outputStream);
    }

    /**
     * Doesn't do anything other than output the database using default PrintVisitor
     * @throws IOException when naughty
     */
    @Test @Ignore
    public void inspectNewDatabase () throws IOException {
        D database = createNewDatabase();

        database.visit(new Visitor.Print(printStream));
    }

    // create a new database for messing around with
    // the assertions here somewhat duplicate those in BasicDatabaseChecks
    @Test
    public void testNewDatabase() throws IOException {
        D database = getDatabase();
        G root = database.getRootGroup();
        Assert.assertTrue(root.isRootGroup());
        assertEquals(0, root.getGroups().size());
        assertEquals(0, root.getEntries().size());

        Assert.assertTrue(database.shouldProtect("Password"));
        Assert.assertFalse(database.shouldProtect("Title"));
        Assert.assertFalse(database.shouldProtect("Bogus"));

        assertEquals("New Database", database.getName());
        database.setName("Modified Database");
        assertEquals("Modified Database", database.getName());

        assertEquals("New Database created by KeePassJava2", database.getDescription());
        database.setDescription("Test Database");
        assertEquals("Test Database", database.getDescription());

        G group1 = database.newGroup("Group 1");
        UUID newGroupUUID = group1.getUuid();

        root.addGroup(group1);
        assertEquals("Group 1", group1.getName());
        Assert.assertFalse(group1.isRootGroup());
        Assert.assertTrue(root.isRootGroup());

        assertEquals(1, root.getGroups().size());
        assertEquals(newGroupUUID, root.getGroups().get(0).getUuid());

        group1.setParent(root);
        root.addGroup(group1);

        root.removeGroup(group1);
        assertNull(group1.getParent());
        assertEquals(0, root.getGroups().size());
        root.addGroup(group1);
        assertEquals(1, root.getGroups().size());
        assertEquals(newGroupUUID, root.getGroups().get(0).getUuid());

        try {
            root.setParent(group1);
            Assert.fail("Cannot add root group to another group");
        } catch (Exception ignored) {
        }

        G group2 = database.newGroup();
        group2.setName("Group 2");
        group1.addGroup(group2);
        assertEquals(1, group1.getGroups().size());
        assertEquals(1, root.getGroups().size());

        root.addGroup(group2);
        assertEquals(0, group1.getGroups().size());
        assertEquals(2, root.getGroups().size());

        E entry1 = database.newEntry();
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

        E entry2 = database.newEntry(entry1);
        entry2.setPassword("pass2");
        assertEquals("pass2", entry2.getPassword());
        group2.addEntry(entry2);

        assertEquals(2, group2.getEntries().size());
        root.removeGroup(group1);
        root.removeGroup(group2);

        assertEquals(0, root.getGroups().size());
        assertEquals(0, database.findEntries("").size());
    }

    @Test
    public void testNewDatabasePasswordBytes() throws IOException {
        D database = getDatabase();
        G root = database.getRootGroup();
        Assert.assertTrue(root.isRootGroup());
        assertEquals(0, root.getGroups().size());
        assertEquals(0, root.getEntries().size());

        Assert.assertTrue(database.shouldProtect("Password"));
        Assert.assertFalse(database.shouldProtect("Title"));
        Assert.assertFalse(database.shouldProtect("Bogus"));

        assertEquals("New Database", database.getName());
        database.setName("Modified Database");
        assertEquals("Modified Database", database.getName());

        assertEquals("New Database created by KeePassJava2", database.getDescription());
        database.setDescription("Test Database");
        assertEquals("Test Database", database.getDescription());

        G group1 = database.newGroup("Group 1");
        UUID newGroupUUID = group1.getUuid();

        root.addGroup(group1);
        assertEquals("Group 1", group1.getName());
        Assert.assertFalse(group1.isRootGroup());
        Assert.assertTrue(root.isRootGroup());

        assertEquals(1, root.getGroups().size());
        assertEquals(newGroupUUID, root.getGroups().get(0).getUuid());

        group1.setParent(root);
        root.addGroup(group1);

        root.removeGroup(group1);
        assertNull(group1.getParent());
        assertEquals(0, root.getGroups().size());
        root.addGroup(group1);
        assertEquals(1, root.getGroups().size());
        assertEquals(newGroupUUID, root.getGroups().get(0).getUuid());

        try {
            root.setParent(group1);
            Assert.fail("Cannot add root group to another group");
        } catch (Exception ignored) {
        }

        G group2 = database.newGroup();
        group2.setName("Group 2");
        group1.addGroup(group2);
        assertEquals(1, group1.getGroups().size());
        assertEquals(1, root.getGroups().size());

        root.addGroup(group2);
        assertEquals(0, group1.getGroups().size());
        assertEquals(2, root.getGroups().size());

        E entry1 = database.newEntry();
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
        assertEquals("pass", new String(entry1.getPasswordAsBytes()));

        E entry2 = database.newEntry(entry1);
        entry2.setPassword("pass2");
        assertEquals("pass2", new String(entry2.getPasswordAsBytes()));
        group2.addEntry(entry2);

        assertEquals(2, group2.getEntries().size());
        root.removeGroup(group1);
        root.removeGroup(group2);

        assertEquals(0, root.getGroups().size());
        assertEquals(0, database.findEntries("").size());
    }
}
