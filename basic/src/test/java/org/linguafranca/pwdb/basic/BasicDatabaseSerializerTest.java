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

package org.linguafranca.pwdb.basic;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Group;
import org.linguafranca.pwdb.Visitor;
import org.linguafranca.pwdb.security.StreamEncryptor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BasicDatabaseSerializerTest {
    public static final String TEST_OUTPUT_DIR = "testOutput/";
    static BasicDatabase database;

    @BeforeAll
    static void setUp() throws IOException {
        Files.createDirectories(Paths.get(TEST_OUTPUT_DIR));
        database = new BasicDatabase();
        database.setName("My Database");
        database.setDescription("A database of passwords");
        database.getRootGroup().addGroup("Group 1")
                .addEntry("Entry 1")
                    .addProperty(Entry.STANDARD_PROPERTY_NAME.USER_NAME, "John Doe")
                    .addProperty(Entry.STANDARD_PROPERTY_NAME.PASSWORD, "password123".getBytes())
                .addEntry("Entry 2")
                    .addProperty(Entry.STANDARD_PROPERTY_NAME.USER_NAME, "Jane Doe")
                    .addProperty(Entry.STANDARD_PROPERTY_NAME.PASSWORD, "password123".getBytes())
                    .addProperty("non-standard", "non-standard value")
                .addEntry("Entry 3")
                    .setBinaryProperty("gif", new byte[]{1, 2, 3, 4, 5});
    }

    Visitor visitor = new Visitor() {
        @Override
        public void startVisit(Group group) {
            System.out.println("Group: " + group.getName() + " " + group.getUuid());
            assertNotNull(database.findGroup(group.getUuid()));
            assertNotNull(group.getDatabase());
            if (!group.isRootGroup()) {
                assertNotNull(group.getParent());
            }
        }

        @Override
        public void endVisit(Group group) {

        }

        @Override
        public void visit(Entry entry) {
            System.out.println("Entry: " + entry.getTitle() + " " + entry.getUuid());
                for (String propertyName: entry.getPropertyNames()) {
                    System.out.println("  " + propertyName + ": " + entry.getPropertyValue(propertyName).getValueAsString());
                }
            for (String propertyName: entry.getBinaryPropertyNames()) {
                System.out.println("  " + propertyName + ": " + Arrays.toString(entry.getBinaryProperty(propertyName)));
            }
                Entry entry1 = database.findEntry(entry.getUuid());
                assertNotNull(entry1, "Entry not found " + entry.getUuid());
                assertEquals(entry.getPropertyNames().size(), entry1.getPropertyNames().size());
                assertNotNull(entry.getDatabase());
                assertNotNull(entry.getParent());
        }

        @Override
        public boolean isEntriesFirst() {
            return false;
        }
    };

    @Test
    void saveAndLoad() throws IOException {
        new BasicDatabaseSerializer.Xml(new StreamEncryptor.Salsa20(new byte[0])).save(database, new FileOutputStream(TEST_OUTPUT_DIR + "test.xml"));
        BasicDatabase loadedDatabase = new BasicDatabaseSerializer.Xml(new StreamEncryptor.Salsa20(new byte[0])).load(new FileInputStream(TEST_OUTPUT_DIR + "test.xml"));
        loadedDatabase.visit(visitor);
        System.out.println(loadedDatabase.getRootGroup().getDatabase().getName());
    }
}