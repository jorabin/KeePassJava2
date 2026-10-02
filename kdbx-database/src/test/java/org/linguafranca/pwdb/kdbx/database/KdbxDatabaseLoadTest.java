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
package org.linguafranca.pwdb.kdbx.database;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.Test;
import org.linguafranca.pwdb.Credentials;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.PropertyValue;
import org.linguafranca.pwdb.StreamFormat;
import org.linguafranca.pwdb.Visitor;
import org.linguafranca.pwdb.format.KdbxCredentials;
import org.linguafranca.pwdb.kdbx.jackson.KdbxDatabase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.linguafranca.util.TestUtil.getTestPrintStream;

public class KdbxDatabaseLoadTest {
    
    static PrintStream printStream = getTestPrintStream();


    @Test
    public void loadXml() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("ExampleDatabase.xml");
        KdbxDatabase database = KdbxDatabase.loadXml(inputStream);
        database.visit(new Visitor.Print(printStream));
        PropertyValue password = database.findEntries("Sample Entry #2").get(0).getPropertyValue(Entry.STANDARD_PROPERTY_NAME_PASSWORD);
        assertTrue(password.isProtected());
        assertEquals("12345", password.getValue().toString());
    }

    @Test
    public void loadXmlWithEncryptedProtectedValues() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("xml/V4-AES-AES.xml");
        KdbxDatabase database = KdbxDatabase.loadXml(inputStream);
        database.visit(new Visitor.Print(printStream));
    }

    @Test
    public void loadStreamFormatNone() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("V4-AES-Argon2.kdbx");
        KdbxDatabase database = KdbxDatabase.load(new KdbxCredentials("123".getBytes()), inputStream);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        database.save(new StreamFormat.None(), new Credentials.None(), outputStream);

        KdbxDatabase xmlDatabase = KdbxDatabase.load(new StreamFormat.None(), new Credentials.None(),
                new ByteArrayInputStream(outputStream.toByteArray()));
        PropertyValue password = xmlDatabase.findEntries("Sample Entry #2").get(0).getPropertyValue(Entry.STANDARD_PROPERTY_NAME_PASSWORD);
        assertTrue(password.isProtected());
        assertEquals("12345", password.getValue().toString());
    }

    @Test
    public void loadKdbx() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test123.kdbx");
        KdbxDatabase database = KdbxDatabase.load(new KdbxCredentials("123".getBytes()), inputStream);
        database.visit(new Visitor.Print(printStream));
    }

    @Test
    public void loadKdbxV4() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("V4-AES-Argon2.kdbx");
        KdbxDatabase database = KdbxDatabase.load(new KdbxCredentials("123".getBytes()), inputStream);
        database.visit(new Visitor.Print(printStream));
        // test what happens to dates in V4
        database.visit(new Visitor.Default(){
            @Override
            public void visit(Entry entry) {
                printStream.println(entry.getCreationTime());
            }
        });
    }

    @Test
    public void emptyDb() throws Exception {
        KdbxDatabase database = new KdbxDatabase();
        printStream.println(database.getDescription());
    }

    @Test
    public void dbWithDeleted() throws Exception {
         InputStream inputStream = getClass().getClassLoader().getResourceAsStream("testDeleted.kdbx");
         KdbxDatabase database = KdbxDatabase.load(new KdbxCredentials("123".getBytes()), inputStream);
         database.visit(new Visitor.Print(printStream));
     }

}
