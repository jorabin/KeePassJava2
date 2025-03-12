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
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Group;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * @author jo
 */
public interface RecycleBinTest {

    void newDatabase();
    Database getDatabase();

    @BeforeEach
    default void makeDatabase() throws Exception {
        newDatabase();
    }

    @Test
    default void recycleBinEntries() {
        assumeTrue(getDatabase().supportsRecycleBin(), "Database does not support recycle bin, skipping test");
        Database database = getDatabase();
        database.enableRecycleBin(false);
        assertFalse(database.isRecycleBinEnabled());
        assertNull(database.getRecycleBin());
        database.enableRecycleBin(true);
        assertTrue(database.isRecycleBinEnabled());
        Group recycleBin = database.getRecycleBin();
        assertNotNull(recycleBin);

        Entry entry = database.newEntry();
        assertEquals(entry, database.getRootGroup().addEntry(entry));
        assertEquals(1, database.getRootGroup().getEntriesCount());

        assertTrue(database.deleteEntry(entry.getUuid()));
        assertEquals(0, database.getRootGroup().getEntriesCount());
        assertEquals(1, recycleBin.getEntriesCount());

        database.enableRecycleBin(false);
        assertFalse(database.isRecycleBinEnabled());

        entry = database.newEntry();
        assertEquals(entry, database.getRootGroup().addEntry(entry));
        assertEquals(1, database.getRootGroup().getEntriesCount());

        assertTrue(database.deleteEntry(entry.getUuid()));
        assertEquals(0, database.getRootGroup().getEntriesCount());
        assertEquals(1, recycleBin.getEntriesCount());
        database.emptyRecycleBin();
        assertEquals(0, recycleBin.getEntriesCount());
    }
    
    @Test
    default void recycleBinGroups() {
        assumeTrue(getDatabase().supportsRecycleBin(), "Database does not support recycle bin, skipping test");
        Database database = getDatabase();
        database.enableRecycleBin(true);
        assertTrue(database.isRecycleBinEnabled());
        Group recycleBin = database.getRecycleBin();
        assertNotNull(recycleBin);

        Group group= database.newGroup();
        assertEquals(group, database.getRootGroup().addGroup(group));
        assertEquals(2, database.getRootGroup().getGroupsCount());

        assertTrue(database.deleteGroup(group.getUuid()));
        assertEquals(1, database.getRootGroup().getGroupsCount());
        assertEquals(1, recycleBin.getGroupsCount());

        database.enableRecycleBin(false);
        assertFalse(database.isRecycleBinEnabled());

        group = database.newGroup();
        assertEquals(group, database.getRootGroup().addGroup(group));
        assertEquals(2, database.getRootGroup().getGroupsCount());

        assertTrue(database.deleteGroup(group.getUuid()));
        assertEquals(1, database.getRootGroup().getGroupsCount());
        assertEquals(1, recycleBin.getGroupsCount());
        database.emptyRecycleBin();
        assertEquals(0, recycleBin.getGroupsCount());
    }

}
