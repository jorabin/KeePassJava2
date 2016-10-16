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
import org.linguafranca.pwdb.Credentials;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

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
        database.visit(new Visitor.Print());

        // create a KDBX (database
        D kdbxDatabse = getDatabase();
        kdbxDatabse.setName("New Database");
        kdbxDatabse.setDescription("Migration of KDB Database to KDBX Database");
        // deep copy from group (not including source group, KDB database has simulated root)
        kdbxDatabse.getRootGroup().copy(database.getRootGroup());
        // save it
        try (FileOutputStream f = new FileOutputStream("testOutput/migration.kdbx")) {
            kdbxDatabse.save(new KdbxCreds("123".getBytes()), f);
        }
    }
}
