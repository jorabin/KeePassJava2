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
import org.linguafranca.pwdb.Visitor;
import org.linguafranca.pwdb.kdbx.KdbxCreds;

import java.io.InputStream;

import static org.junit.Assert.*;

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
    public void emptyDb() throws Exception {
        SimpleDatabase database = new SimpleDatabase();
        System.out.println(database.getDescription());
    }

}