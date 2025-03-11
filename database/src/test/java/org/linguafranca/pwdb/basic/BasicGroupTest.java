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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Group;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BasicGroupTest {

    private BasicGroup group;

    @BeforeEach
    void setUp() {
        group = new BasicGroup(new BasicDatabase());
    }

    @Test
    void testGetName() {
        group.setName("Test Group");
        assertEquals("Test Group", group.getName());
    }

    @Test
    void testSetName() {
        group.setName("Test Group");
        assertEquals("Test Group", group.getName());
    }

    @Test
    void testGetEntries() {
        Entry entry = new BasicEntry(new BasicDatabase());
        group.addEntry(entry);
        List<Entry> entries = group.getEntries();
        assertTrue(entries.contains(entry));
    }

    @Test
    void testAddEntry() {
        Entry entry = new BasicEntry(new BasicDatabase());
        group.addEntry(entry);
        assertTrue(group.getEntries().contains(entry));
    }

    @Test
    void testRemoveEntry() {
        Entry entry = new BasicEntry(new BasicDatabase());
        group.addEntry(entry);
        group.removeEntry(entry);
        assertFalse(group.getEntries().contains(entry));
    }

    @Test
    void testGetGroups() {
        Group subGroup = new BasicGroup(new BasicDatabase());
        group.addGroup(subGroup);
        List<Group> groups = group.getGroups();
        assertTrue(groups.contains(subGroup));
    }

    @Test
    void testAddGroup() {
        Group subGroup = new BasicGroup(new BasicDatabase());
        group.addGroup(subGroup);
        assertTrue(group.getGroups().contains(subGroup));
    }

    @Test
    void testRemoveGroup() {
        Group subGroup = new BasicGroup(new BasicDatabase());
        group.addGroup(subGroup);
        assertTrue(group.getGroups().contains(subGroup));
        assertEquals(1, group.getGroups().size());
        assertEquals(group, subGroup.getParent());
        group.removeGroup(subGroup);
        assertEquals(0, group.getGroups().size());
        assertNull(subGroup.getParent());
        assertFalse(group.getGroups().contains(subGroup));
    }

    @Test
    void testGetParent() {
        assertNull(group.getParent());
    }

    @Test
    void testGetUuid() {
        UUID uuid = group.getUuid();
        assertNotNull(uuid);
    }

    @Test
    void testGetIcon() {
        assertEquals(0, group.getIcon().getIndex());
    }

    @Test
    void testSetIcon() {
        group.setIcon(new BasicIcon(1));
        assertEquals(1, group.getIcon().getIndex());
    }
}