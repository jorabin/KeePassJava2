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

package org.linguafranca.pwdb.kdbx;

import org.linguafranca.pwdb.*;
import org.linguafranca.pwdb.format.KdbxCredentials;
import org.linguafranca.pwdb.format.KdbxHeader;
import org.linguafranca.pwdb.format.KdbxStreamFormat;
import org.linguafranca.pwdb.kdb.KdbCredentials;
import org.linguafranca.pwdb.kdb.KdbDatabase;
import org.linguafranca.pwdb.kdbx.jackson.KdbxDatabase;
import org.linguafranca.pwdb.security.Encryption;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.linguafranca.pwdb.Entry.STANDARD_PROPERTY_NAME.*;
import static org.linguafranca.util.TestUtil.getTestPrintStream;

/**
 * Examples for QuickStart
 *
 * @author jo
 */
@SuppressWarnings("WeakerAccess")
public abstract class QuickStart {

    public static final String TEST_OUTPUT_DIR = "testOutput/";
    static PrintStream printStream = getTestPrintStream();

    public void canonicalQuickStart() throws IOException {

        // V3 create a database, no need to specify what kind (e.g. JacksonDatabase)
        // V3 Database no longer need to be generified
        Database database = new KdbxDatabase();
        // V3 the property value strategy is set to PropertyValue.Strategy.Default(), anyway, but the Default property
        // value strategy being to protect field "Password" and no other, all of which are stored as byte[]
        database.setPropertyValueStrategy(new PropertyValue.Strategy.Default());
        // V3 Group is not generified. create a group
        // V3 add a new group to another group with the name supplied
        Group main = database.getRootGroup().addGroup("Main");
        // V3 fluent building of Entry (using PropertyValueStrategy as set in database to determine protection)
        main.addEntry()
                // V3 addProperty short-cut for setPropertyValue
                // V3 abbreviated static import of standard field names
                .addProperty(TITLE, "first entry")
                .addProperty(USER_NAME, "Tom")
                .addProperty(PASSWORD, "123".getBytes())
                .addProperty(URL, "http://localhost:8080/")
            .addEntry("2nd Entry") // v3 addEntry on Entry adds Entry to parent Group if exists
                .addProperty(USER_NAME, "Alice")
                .addProperty(PASSWORD, "123".getBytes());

        // find entries just added, in V3 the findEntries return value is just List<Entry>
        List<Entry> entries = database.findEntries("entry");

        // V3 KdbxCreds now called KdbxCredentials
        KdbxCredentials credentials = new KdbxCredentials("123".getBytes());
        try (OutputStream outputStream = Files.newOutputStream(Path.of(TEST_OUTPUT_DIR, "test.kdbx"))) {
            database.save(credentials, outputStream);
        }
    }

    /**
     * Illustrates the fluent API for creating a database, as well as the various ways of setting property values.
     */
    public void fluentExample() {
        Database database = new KdbxDatabase();
        database.getRootGroup().addGroup("Group 1")
                .addEntry("as property values determined by the database property value strategy (preferred)")
                    .addProperty("prop1", "value1".getBytes())
                    .addProperty("prop2", "value2")
                    .addProperty("prop3", "value3")
                .addEntry("as non default storage type (according to PropertyValueStrategy)  property values")
                    .setPropertyValue("prop1", database.getPropertyValueStrategy().newProtected().of("value1"))
                    .setPropertyValue("prop2", database.getPropertyValueStrategy().newUnprotected().of("value2"))
                .addEntry("as explicit storage type property values")
                    .setPropertyValue("prop1", new PropertyValue.BytesStore("value1".getBytes()))
                    .setPropertyValue("prop2", new PropertyValue.SealedStore("value2"))
                .addEntry("as strings")
                    .setProperty("prop1", "value1")
                    .setProperty("prop2", "value2")
                    .setProperty("prop3", "value3");

        // find entries just added, in V3 the findEntries return value is just List<Entry>
        List<Entry> entries = database.findEntries("prop");
    }

    /**
     * Load KDBX
     */
    public void loadKdbx() throws IOException {
        // get an input stream
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test123.kdbx")) {
            // password credentials
            Credentials credentials = new KdbxCredentials("123".getBytes());
            // Jaxb implementation seems a lot faster than the DOM implementation
            Database database = KdbxDatabase.load(credentials, inputStream);
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
        Database database = new KdbxDatabase();

        // add some groups and entries
        for (int g = 0; g < 5; g++) {
            Group group = database.getRootGroup().addGroup(database.newGroup(Integer.toString(g)));
            for (int e = 0; e <= g; e++) {
                // entry factory is a local helper to populate an entry
                group.addEntry(entryFactory(database, Integer.toString(g), e));
            }
        }

        // save to a file with password "123"
        try (FileOutputStream outputStream = new FileOutputStream(TEST_OUTPUT_DIR + "test.kdbx")) {
            database.save(new KdbxCredentials("123".getBytes()), outputStream);
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
        List<Entry> entries =
                database.findEntries(entry -> entry.getProperty(Entry.STANDARD_PROPERTY_NAME_TITLE).toLowerCase().contains("findme!"));
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
    public void loadKdbSaveKdbx() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test123.kdb");
        // password credentials
        Credentials credentials = new KdbCredentials.Password("123".getBytes());
        // load KdbDatabase
        KdbDatabase database = KdbDatabase.load(credentials, inputStream);
        // visit all groups and entries and list them to console

        // create a KDBX (database
        Database kdbxDatabase = new KdbxDatabase();
        kdbxDatabase.setName("New Database");
        kdbxDatabase.setDescription("Migration of KDB Database to KDBX Database");
        // deep copy from group (not including source group, KDB database has simulated root)
        kdbxDatabase.getRootGroup().copy(database.getRootGroup());
        // save it
        try (FileOutputStream f = new FileOutputStream(TEST_OUTPUT_DIR + "migration.kdbx")) {
            kdbxDatabase.save(new KdbxCredentials("123".getBytes()), f);
        }
    }

    /**
     * Load KDBX V3 save as KDBX V4
     */
    public void loadKdbx3SaveKdbx4(String v3ResourceName, byte[] password, Path v4Filename) throws IOException {
        KdbxDatabase database;
        // password credentials
        KdbxCredentials credentials = new KdbxCredentials(password);
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(v3ResourceName)) {
            // load KdbDatabase
            database = KdbxDatabase.load(credentials, inputStream);
        }

        // create a KDBX (database
        Database kdbxDatabase = new KdbxDatabase();
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
        try (OutputStream v4OutputStream = Files.newOutputStream(v4Filename)) {
            kdbxDatabase.save(formatV4, credentials, v4OutputStream);
        }
    }


    /**
     * Load KDBX V3 save as KDBX V4
     */
    public void loadKdbx4SaveKdbx3(String resourceName, byte[] password, Path v3Filename) throws IOException {
        KdbxDatabase database;
        // password credentials
        KdbxCredentials credentials = new KdbxCredentials(password);
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            // load KdbDatabase
            database = KdbxDatabase.load(credentials, inputStream);
        }

        // create a KDBX (database)
        Database kdbxDatabase = new KdbxDatabase();
        kdbxDatabase.setName("New Database");
        kdbxDatabase.setDescription("Migration of KDBX 4 Database to KDBX 3 Database");
        // deep copy from group (not including source group
        kdbxDatabase.getRootGroup().copy(database.getRootGroup());

        // choose a stream format - V3
        KdbxHeader kdbxHeader = new KdbxHeader(KdbxHeader.KdbxHeaderOpts.V3_AES_SALSA_20);
        KdbxStreamFormat formatV3 = new KdbxStreamFormat(kdbxHeader);

        // save it with format options
        try (OutputStream v3OutputStream = Files.newOutputStream(v3Filename)) {
            kdbxDatabase.save(formatV3, credentials, v3OutputStream);
        }
    }
}
