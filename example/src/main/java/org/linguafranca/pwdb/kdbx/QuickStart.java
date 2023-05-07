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
import org.linguafranca.pwdb.kdb.KdbCredentials;
import org.linguafranca.pwdb.kdb.KdbDatabase;
import org.linguafranca.pwdb.kdbx.dom.DomDatabaseWrapper;
import org.linguafranca.pwdb.security.Aes;
import org.linguafranca.pwdb.security.Encryption;
import org.linguafranca.pwdb.security.KeyDerivationFunction;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Examples for QuickStart
 *
 * @author jo
 */
@SuppressWarnings("WeakerAccess")
public abstract class QuickStart<D extends Database<D, G, E, I>, G extends Group<D, G, E, I>, E extends Entry<D, G, E, I>, I extends Icon> {

    public abstract D getDatabase();
    public abstract D loadDatabase(Credentials creds, InputStream inputStream);

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
            D database = loadDatabase(credentials, inputStream);
            // visit all groups and entries and list them to console
            database.visit(new Visitor.Print());
        }
    }

    /**
     * Save KDBX
     */

    private E entryFactory(D database, String s, int e) {
        return database.newEntry(String.format("Group %s Entry %d", s, e));
    }

    public void saveKdbx() throws IOException {
        // create an empty database
        D database = getDatabase();

        // add some groups and entries
        for (Integer g = 0; g < 5; g++) {
            G group = database.getRootGroup().addGroup(database.newGroup(g.toString()));
            for (int e = 0; e <= g; e++) {
                // entry factory is a local helper to populate an entry
                group.addEntry(entryFactory(database, g.toString(), e));
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
    public void splice(G newParent, Group<?, ?, ?, ?> groupToSplice) {
        G addedGroup = newParent.addGroup(newParent.getDatabase().newGroup(groupToSplice));
        addedGroup.copy(groupToSplice);
    }

    /**
     * Group by title
     */
    public G groupByTitle(D database) {
        List<? extends E> entries = database.findEntries(new Entry.Matcher() {
            @Override
            public boolean matches(Entry entry) {
                return entry.getProperty(Entry.STANDARD_PROPERTY_NAME_TITLE).toLowerCase().contains("findme!");
            }
        });
        // create a new group using DB factory method
        G newParent = database.newGroup("Found entries");
        // iterate over the found entries
        for (E entry : entries) {
            // copy the entry using DB factory so it remains where it was found as well as being in new group
            E copy = database.newEntry(entry);
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
        D kdbxDatabase = getDatabase();
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
     * Load KDBX V3 save as KDBX V4 - then load again and save with different configuration
     */
    public void loadKdbx3SaveKdbx4() throws IOException {
        DomDatabaseWrapper database;
        // password credentials
        KdbxCreds credentials = new KdbxCreds("123".getBytes());

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test123.kdbx")) {
            // load KdbDatabase
            database = DomDatabaseWrapper.load(credentials, inputStream);
        }
        // visit all groups and entries and list them to console
        database.visit(new Visitor.Print());

        // create a KDBX (database
        D kdbxDatabase = getDatabase();
        kdbxDatabase.setName("New Database");
        kdbxDatabase.setDescription("Migration of KDB Database to KDBX Database");
        // deep copy from group (not including source group, KDB database has simulated root)
        kdbxDatabase.getRootGroup().copy(database.getRootGroup());

        // choose a stream format - V4 Kdbx and choose some algorithms
        KdbxStreamFormat formatV4 = new KdbxStreamFormat(new KdbxHeader(KdbxHeader.KdbxHeaderOpts.V4_AES_ARGON_CHA_CHA));
        KdbxHeader kdbxHeader = formatV4.getStreamConfiguration();
        // change algos from those originally selected
        kdbxHeader.setCipherAlgorithm(Encryption.Cipher.CHA_CHA_20);
        kdbxHeader.setKeyDerivationFunction(Encryption.Kdf.AES);
        kdbxHeader.setProtectedStreamAlgorithm(Encryption.ProtectedStreamAlgorithm.CHA_CHA_20);

        // save it with format options
        try (FileOutputStream f = new FileOutputStream("testOutput/CHACHA-AES-CHACHA.kdbx")) {
            kdbxDatabase.save(formatV4, credentials, f);
        }

        // doesn't matter what we create it will be overwritten
        KdbxStreamFormat kdbxStreamFormat = new KdbxStreamFormat();
        // load it again
        try (FileInputStream f = new FileInputStream("testOutput/CHACHA-AES-CHACHA.kdbx")) {
            DomDatabaseWrapper.load(kdbxStreamFormat, credentials, f);
        }

        assertEquals("CHA_CHA_20", kdbxStreamFormat.getStreamConfiguration().getCipherAlgorithm().getName());
        assertEquals("AES", kdbxStreamFormat.getStreamConfiguration().getKeyDerivationFunction().getName());
        assertEquals("CHA_CHA_20", kdbxStreamFormat.getStreamConfiguration().getProtectedStreamAlgorithm().name());

        try (FileOutputStream f = new FileOutputStream("testOutput/CHACHA-AES-CHACHA-2.kdbx")) {
            kdbxDatabase.save(kdbxStreamFormat, credentials, f);
        }

        // doesn't matter what we create it will be overwritten
        KdbxStreamFormat kdbxStreamFormat2 = new KdbxStreamFormat();
        // load it again
        try (FileInputStream f = new FileInputStream("testOutput/CHACHA-AES-CHACHA-2.kdbx")) {
            DomDatabaseWrapper.load(kdbxStreamFormat2, credentials, f);
        }
        // still CHA-CHA_20 etc.
        assertEquals("CHA_CHA_20", kdbxStreamFormat2.getStreamConfiguration().getCipherAlgorithm().getName());
        assertEquals("AES", kdbxStreamFormat.getStreamConfiguration().getKeyDerivationFunction().getName());
        assertEquals("CHA_CHA_20", kdbxStreamFormat.getStreamConfiguration().getProtectedStreamAlgorithm().name());
    }


    public KdbxHeader loadKdbxHeader(String filename) throws IOException {
        KdbxHeader kdbxHeader = new KdbxHeader();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filename)) {
            try {
                // load KdbDatabase with no credentials, in V4 this will fail as HMac256 won't be correct
                // but the header will still be populated
                KdbxSerializer.createUnencryptedInputStream(new Credentials.None(), kdbxHeader, inputStream);
            } catch (IllegalStateException ignored) {
            }
            return kdbxHeader;
        }
    }

    public void listKdbxHeaderProperties(KdbxHeader kdbxHeader, PrintWriter printWriter) {
        printWriter.format("Version: %d\n", kdbxHeader.getVersion());
        printWriter.format("Cipher Algorithm: %s\n", kdbxHeader.getCipherAlgorithm().getName());
        // AES is the only KDF in V3
        KeyDerivationFunction kdf = kdbxHeader.getVersion() == 3 ? Aes.getInstance() : kdbxHeader.getKeyDerivationFunction();
        printWriter.format("Key Derivation Function: %s\n", kdf.getName());
        printWriter.format("Inner Stream Algorithm: %s\n", kdbxHeader.getProtectedStreamAlgorithm().name());
        printWriter.flush();
    }
    /**
     * List Database Encryption Characteristics
     */
    public void listKdbxHeaderParams () throws IOException {
        PrintWriter writer = new PrintWriter(System.out);
        listKdbxHeaderProperties(loadKdbxHeader("V4-AES-AES.kdbx"), writer);
        listKdbxHeaderProperties(loadKdbxHeader("V4-AES-Argon2.kdbx"), writer);
        listKdbxHeaderProperties(loadKdbxHeader("V4-ChaCha20-AES.kdbx"), writer);
        listKdbxHeaderProperties(loadKdbxHeader("V4-ChaCha20-Argon2-Attachment.kdbx"), writer);
    }

}
