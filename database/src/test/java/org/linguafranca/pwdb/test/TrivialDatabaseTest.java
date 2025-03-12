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

package org.linguafranca.pwdb.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Group;
import org.linguafranca.pwdb.Icon;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public interface TrivialDatabaseTest {

    void newDatabase();
    Database getDatabase();

    @BeforeEach
    default void setUp() {
        newDatabase();
    }

    @Test
    default void testGetRootGroup() {
        Group root = getDatabase().getRootGroup();
        assertNotNull(root);
        assertEquals("Root", root.getName());
    }

    @Test
    default void testNewGroup() {
        Group group = getDatabase().newGroup();
        assertNotNull(group);
    }

    @Test
    default void testNewEntry() {
        Entry entry = getDatabase().newEntry();
        assertNotNull(entry);
    }

    @Test
    default void testNewIcon() {
        Icon icon = getDatabase().newIcon();
        assertNotNull(icon);
    }

    @Test
    default void testNewIconWithInteger() {
        Icon icon = getDatabase().newIcon(1);
        assertNotNull(icon);
        assertEquals(1, icon.getIndex());
    }

    @Test
    default void testRecycleBin() {
        if (getDatabase().supportsRecycleBin()) {
            assertTrue(getDatabase().isRecycleBinEnabled());
            Group recycleBin = getDatabase().getRecycleBin();
            assertNotNull(recycleBin);
        } else {
            assertFalse(getDatabase().isRecycleBinEnabled());
            assertThrows(UnsupportedOperationException.class, () -> getDatabase().enableRecycleBin(true));
            assertNull(getDatabase().getRecycleBin());
        }
    }

    @Test
    default void testGetSetName() {
        assertEquals("New Database", getDatabase().getName());
        getDatabase().setName("Test Database");
        assertEquals("Test Database", getDatabase().getName());
    }

    @Test
    default void testGetSetDescription() {
        assertNotNull(getDatabase().getDescription());
        getDatabase().setDescription("Test Description");
        assertEquals("Test Description", getDatabase().getDescription());
    }

    @Test
    default void testDeleteEntry() {
        Entry entry = getDatabase().newEntry();
        getDatabase().getRootGroup().addEntry(entry);
        UUID uuid = entry.getUuid();
        assertTrue(getDatabase().deleteEntry(uuid));
        assertNull(getDatabase().findEntry(uuid));
    }

    @Test
    default void testDeleteGroup() {
        Group group = getDatabase().newGroup();
        getDatabase().getRootGroup().addGroup(group);
        UUID uuid = group.getUuid();
        assertTrue(getDatabase().deleteGroup(uuid));
        assertNull(getDatabase().findGroup(uuid));
    }

    @Test
    default void testSupportsNonStandardPropertyNames() {
        // check standard names are there
        Entry entry = getDatabase().newEntry();
        for (String name : Entry.STANDARD_PROPERTY_NAMES) {
            assertNotNull(entry.getProperty(name));
        }
        // add a random property
        if (getDatabase().supportsNonStandardPropertyNames()) {
            entry.setProperty("random", "you never know");
            assertEquals("you never know", entry.getProperty("random"));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> entry.setProperty("random", "you never know"));
        }
    }

    @Test
    default void testSupportsBinaryProperties() {
        if (getDatabase().supportsBinaryProperties()) {
            Entry entry = getDatabase().newEntry();
            entry.setBinaryProperty("binary", new byte[]{1, 2, 3});
            assertArrayEquals(new byte[]{1, 2, 3}, entry.getBinaryProperty("binary"));
        } else {
            Entry entry = getDatabase().newEntry();
            assertThrows(UnsupportedOperationException.class, () -> entry.setBinaryProperty("binary", new byte[]{1, 2, 3}));
        }
    }
}