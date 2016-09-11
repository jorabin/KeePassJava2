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

package org.linguafranca.pwdb.example;

import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Group;
import org.linguafranca.pwdb.Visitor;
import org.linguafranca.pwdb.kdb.KdbCredentials;
import org.linguafranca.pwdb.kdb.KdbDatabase;
import org.linguafranca.pwdb.kdbx.KdbxCreds;
import org.linguafranca.pwdb.kdbx.dom.DomDatabaseWrapper;
import org.linguafranca.security.Credentials;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Examples for QuickStart
 *
 * @author jo
 */
public class QuickStart {

/*
### Load KDBX
*/
    public void loadKdbx() throws IOException {
        // get an input stream
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test123.kdbx");
        // password credentials
        Credentials credentials = new KdbxCreds("123".getBytes());
        // open database
        Database database = DomDatabaseWrapper.load(credentials, inputStream);

        // visit all groups and entries and list them to console
        database.visit(new Visitor.Print());
    }

/*
### Save KDBX
*/

    private static Entry entryFactory(DomDatabaseWrapper database, String s, int e) {
        return database.newEntry(String.format("Group %s Entry %d", s, e));
    }

    public void saveKdbx() throws IOException {
        // create an empty database
        DomDatabaseWrapper database = new DomDatabaseWrapper();

        // add some groups and entries
        for (Integer g = 0; g < 5; g++){
            Group group = database.getRootGroup().addGroup(database.newGroup(g.toString()));
            for (int e = 0; e <= g; e++) {
                // entry factory is a local helper to populate an entry
                group.addEntry(entryFactory(database, g.toString(), e));
            }
        }

        // save to a file with password "123"
        FileOutputStream outputStream = new FileOutputStream("test.kdbx");
        database.save(new KdbxCreds("123".getBytes()), outputStream);
    }

/*
### Load KDB
*/
    public void loadKdb() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test.kdb");
        // password credentials
        Credentials credentials = new KdbCredentials.Password("123".getBytes());
        // load KdbDatabase
        Database database = KdbDatabase.load(credentials, inputStream);
        // visit all groups and entries and list them to console
        database.visit(new Visitor.Print());
    }
}
