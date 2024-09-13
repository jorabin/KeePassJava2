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

package org.linguafranca.pwdb.kdbx;

import org.junit.BeforeClass;
import org.linguafranca.pwdb.*;
import org.linguafranca.pwdb.format.KdbxCreds;
import org.linguafranca.pwdb.format.KdbxHeader;
import org.linguafranca.pwdb.format.KdbxStreamFormat;
import org.linguafranca.pwdb.kdb.KdbCredentials;
import org.linguafranca.pwdb.kdb.KdbDatabase;
import org.linguafranca.pwdb.security.Encryption;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.linguafranca.util.TestUtil.getTestPrintStream;

/**
 * Examples for QuickStart
 *
 * @author jo
 */
@SuppressWarnings("WeakerAccess")
public abstract class QuickStart {

    static PrintStream printStream = getTestPrintStream();


    public abstract Database getDatabase();
    public abstract Database loadDatabase(Credentials creds, InputStream inputStream);

    @BeforeClass
    public static void ensureOutputDir() throws IOException {
        Files.createDirectories(Paths.get("testOutput"));
    }

    /**
     * Load KDBX
     */
    public void loadKdbx() throws IOException {
        // get an input stream
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test123.kdbx")) {
            // password credentials
            Credentials credentials = new KdbxCreds("123".getBytes());
            // Jaxb implementation seems a lot faster than the DOM implementation
            Database database = loadDatabase(credentials, inputStream);
            // visit all groups and entries and list them to console
            database.visit(new Visitor.Print(printStream));
        }
    }

    /**
     * Save KDBX
     */

    private Entry entryFactory(Database database, String s, int e) {
        return database.newEntry(String.format("Group %s Entry %d", s, e));
    }

    public void saveKdbx() throws IOException {
        // create an empty database
        Database database = getDatabase();

        // add some groups and entries
        for (int g = 0; g < 5; g++) {
            Group group = database.getRootGroup().addGroup(database.newGroup(Integer.toString(g)));
            for (int e = 0; e <= g; e++) {
                // entry factory is a local helper to populate an entry
                group.addEntry(entryFactory(database, Integer.toString(g), e));
            }
        }

        // save to a file with password "123"
        try (FileOutputStream outputStream = new FileOutputStream("testOutput/test.kdbx")) {
            database.save(new KdbxCreds("123".getBytes()), outputStream);
        }
    }

    /**
     * Splice - add a group from one database to a parent from another (or copy from the same db)
     */
    public void splice(Group newParent, Group groupToSplice) {
        Group addedGroup = newParent.addGroup(newParent.getDatabase().newGroup(groupToSplice));
        addedGroup.copy(groupToSplice);
    }

    /**
     * Group by title
     */
    public Group groupByTitle(Database database) {
        List<? extends Entry> entries = database.findEntries(entry -> entry.getProperty(Entry.STANDARD_PROPERTY_NAME_TITLE).toLowerCase().contains("findme!"));
        // create a new group using DB factory method
        Group newParent = database.newGroup("Found entries");
        // iterate over the found entries
        for (Entry entry : entries) {
            // copy the entry using DB factory so that it remains where it was found as well as being in new group
            Entry copy = database.newEntry(entry);
            // add new entry to new group
            newParent.addEntry(copy);
        }
        return newParent;
    }

    /**
     * Load KDB and save as KDBX
     */
    public void loadKdb() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test123.kdb");
        // password credentials
        Credentials credentials = new KdbCredentials.Password("123".getBytes());
        // load KdbDatabase
        KdbDatabase database = KdbDatabase.load(credentials, inputStream);
        // visit all groups and entries and list them to console

        // create a KDBX (database
        Database kdbxDatabase = getDatabase();
        kdbxDatabase.setName("New Database");
        kdbxDatabase.setDescription("Migration of KDB Database to KDBX Database");
        // deep copy from group (not including source group, KDB database has simulated root)
        kdbxDatabase.getRootGroup().copy(database.getRootGroup());
        // save it
        try (FileOutputStream f = new FileOutputStream("testOutput/migration.kdbx")) {
            kdbxDatabase.save(new KdbxCreds("123".getBytes()), f);
        }
    }

    /**
     * Load KDBX V3 save as KDBX V4
     */
    public void loadKdbx3SaveKdbx4(String resourceName, byte[] password, OutputStream v4OutputStream) throws IOException {
        KdbxDatabase database;
        // password credentials
        KdbxCreds credentials = new KdbxCreds(password);
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            // load KdbDatabase
            database = KdbxDatabase.load(credentials, inputStream);
        }

        // create a KDBX (database
        Database kdbxDatabase = getDatabase();
        kdbxDatabase.setName("New Database");
        kdbxDatabase.setDescription("Migration of KDBX 3 Database to KDBX 4 Database");
        // deep copy from group (not including source group, KDB database has simulated root)
        kdbxDatabase.getRootGroup().copy(database.getRootGroup());

        // choose a stream format - V4 Kdbx and choose some algorithms
        KdbxHeader kdbxHeader = new KdbxHeader(KdbxHeader.KdbxHeaderOpts.V4_AES_ARGON_CHA_CHA);
        KdbxStreamFormat formatV4 = new KdbxStreamFormat(kdbxHeader);
        // change algorithm from those originally selected
        kdbxHeader.setCipherAlgorithm(Encryption.Cipher.CHA_CHA_20);
        kdbxHeader.setKeyDerivationFunction(Encryption.KeyDerivationFunction.ARGON2);
        kdbxHeader.setProtectedStreamAlgorithm(Encryption.ProtectedStreamAlgorithm.CHA_CHA_20);

        // save it with format options
        kdbxDatabase.save(formatV4, credentials, v4OutputStream);
    }


    /**
     * Load KDBX V3 save as KDBX V4
     */
    public void loadKdbx4SaveKdbx3(String resourceName, byte[] password, OutputStream v3OutputStream) throws IOException {
        KdbxDatabase database;
        // password credentials
        KdbxCreds credentials = new KdbxCreds(password);
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            // load KdbDatabase
            database = KdbxDatabase.load(credentials, inputStream);
        }

        // create a KDBX (database)
        Database kdbxDatabase = getDatabase();
        kdbxDatabase.setName("New Database");
        kdbxDatabase.setDescription("Migration of KDBX 4 Database to KDBX 3 Database");
        // deep copy from group (not including source group
        kdbxDatabase.getRootGroup().copy(database.getRootGroup());

        // choose a stream format - V3
        KdbxHeader kdbxHeader = new KdbxHeader(KdbxHeader.KdbxHeaderOpts.V3_AES_SALSA_20);
        KdbxStreamFormat formatV3 = new KdbxStreamFormat(kdbxHeader);

        // save it with format options
        kdbxDatabase.save(formatV3, credentials, v3OutputStream);
    }
}
