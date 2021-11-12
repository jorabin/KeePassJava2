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

package org.linguafranca.pwdb.kdbx.simple;

import org.junit.Test;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Group;
import org.linguafranca.pwdb.Visitor;
import org.linguafranca.pwdb.kdbx.KdbxCreds;

import java.io.InputStream;

/**
 * @author jo
 */
public class SimpleDatabaseLoadTest {

    @Test
    public void loadXml() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("ExampleDatabase.xml");
        SimpleDatabase database = SimpleDatabase.loadXml(inputStream);
        database.visit(new Visitor.Print());
    }
    @Test
    public void loadKdbx() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test123.kdbx");
        SimpleDatabase database = SimpleDatabase.load(new KdbxCreds("123".getBytes()), inputStream);
        database.visit(new Visitor.Print());
    }
    @Test
    public void loadKdbxV4() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("V4-AES-Argon2.kdbx");
        SimpleDatabase database = SimpleDatabase.load(new KdbxCreds("123".getBytes()), inputStream);
        database.visit(new Visitor.Print());
        // test what happens to dates in V4
        database.visit(new Visitor.Default(){
            @Override
            public void visit(Entry entry) {
                System.out.println(entry.getCreationTime());
            }
        });
    }

    @Test
    public void emptyDb() throws Exception {
        SimpleDatabase database = new SimpleDatabase();
        System.out.println(database.getDescription());
    }

    @Test
    public void dbWithDeleted() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("testDeleted.kdbx");
        SimpleDatabase database = SimpleDatabase.load(new KdbxCreds("123".getBytes()), inputStream);
        database.visit(new Visitor.Print());
    }

}