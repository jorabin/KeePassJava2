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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Group;
import org.linguafranca.pwdb.Icon;

import static org.junit.jupiter.api.Assertions.*;

class BasicDatabaseTest {

    private BasicDatabase database;

    @BeforeEach
    void setUp() {
        database = new BasicDatabase();
    }

    @Test
    void testGetRootGroup() {
        Group root = database.getRootGroup();
        assertNotNull(root);
        assertEquals("Root", root.getName());
    }

    @Test
    void testNewGroup() {
        Group group = database.newGroup();
        assertNotNull(group);
    }

    @Test
    void testNewEntry() {
        Entry entry = database.newEntry();
        assertNotNull(entry);
    }

    @Test
    void testNewIcon() {
        Icon icon = database.newIcon();
        assertNotNull(icon);
    }

    @Test
    void testNewIconWithInteger() {
        Icon icon = database.newIcon(1);
        assertNotNull(icon);
    }

    @Test
    void testIsRecycleBinEnabled() {
        assertFalse(database.isRecycleBinEnabled());
    }

    @Test
    void testEnableRecycleBin() {
        assertThrows(UnsupportedOperationException.class, () -> database.enableRecycleBin(true));
    }

    @Test
    void testGetRecycleBin() {
        assertNull(database.getRecycleBin());
    }

    @Test
    void testGetName() {
        assertNull(database.getName());
    }

    @Test
    void testGetSetDescription() {
        assertNull(database.getDescription());
        database.setName("Test Database");
        assertEquals("Test Database", database.getName());
    }

    @Test
    void testSetDescription() {
        database.setDescription("Test Description");
        assertEquals("Test Description", database.getDescription());
    }
}
