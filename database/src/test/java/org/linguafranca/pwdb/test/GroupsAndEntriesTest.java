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

package org.linguafranca.pwdb.test;/*
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

import org.junit.jupiter.api.Test;
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Group;
import org.linguafranca.pwdb.Icon;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Jo
 */
public interface GroupsAndEntriesTest {
    void newDatabase();
    Database getDatabase();
    Database createDatabase();
    
    @Test
    default void testEmptyDatabase() {
        assertEquals("Root", getDatabase().getRootGroup().getName());
        assertEquals(0, getDatabase().getRootGroup().getEntries().size());
        assertEquals(0, getDatabase().getRootGroup().getGroups().size());
    }

    @Test
    default void testAddGroup() {
        Group g1 = getDatabase().getRootGroup().addGroup(getDatabase().newGroup("group1"));
        assertEquals(1, getDatabase().getRootGroup().getGroups().size());
        assertEquals("group1", g1.getName());
        assertEquals(0, g1.getGroups().size());
        assertEquals(0, g1.getEntries().size());
        assertEquals(g1.getParent(), getDatabase().getRootGroup(), "root is not the parent of its child");

        Group g2 = getDatabase().newGroup();
        assertEquals("", g2.getName());
        assertNotNull(g2.getUuid());
        assertEquals(0, g2.getIcon().getIndex());
        assertEquals(0, g2.getGroups().size());
        assertEquals(0, g2.getEntries().size());

        // show that the list of groups is a copy
        getDatabase().getRootGroup().getGroups().clear();
        assertEquals(1, getDatabase().getRootGroup().getGroups().size());
        assertEquals(0, g1.getEntries().size());
    }

    @Test
    default void testDeleteGroupDetail () {
        Group g1 = getDatabase().getRootGroup().addGroup(getDatabase().newGroup("group1"));
        List<? extends Group> l1 = getDatabase().getRootGroup().findGroups("group1");
        assertEquals(1, l1.size());
        Group g2 = l1.get(0);
        assertEquals(g2, g1);
        Group g3 = getDatabase().getRootGroup().removeGroup(g2);
        assertEquals(g3, g1);
        assertNull(g1.getParent());
        assertEquals(0, getDatabase().getRootGroup().getGroups().size());
        assertEquals(0, getDatabase().getRootGroup().findGroups("group1").size());
    }

    @Test
    default void testAddRemoveEntry() {
        Entry e1 = getDatabase().getRootGroup().addEntry(getDatabase().newEntry());
        e1.setTitle("entry1");
        List<? extends Entry> l1 = getDatabase().findEntries("entry1");
        assertEquals(1, l1.size());

        Entry e12 = getDatabase().getRootGroup().addEntry(getDatabase().newEntry("entry12"));
        List<? extends Entry> l2 = getDatabase().findEntries("entry1");
        assertEquals(2, l2.size());

        // show that the entries are different
        assertNotEquals(l2.get(0), l2.get(1));

        // show that the list is a copy
        l2.clear();
        assertEquals(2, getDatabase().findEntries("entry1").size());

        // show that we get an equivalent entry when we remove to when we inserted
        Entry e12b = getDatabase().getRootGroup().removeEntry(e12);
        assertEquals(e12b, e12);
        // has been unhooked from parent
        assertNull(e12.getParent());
        assertEquals(1, getDatabase().findEntries("entry1").size());
    }

    @Test
    default void testSetFields () {
        Entry e1 = getDatabase().newEntry("Entry 1");
        e1.setNotes("this looks a little like Entry 2");
        assertEquals("this looks a little like Entry 2", e1.getNotes());
        e1.setUsername("jake@window.com");
        assertEquals("jake@window.com", e1.getUsername());
        e1.setPassword("supercalifragelisticexpialidocious");
        assertEquals("supercalifragelisticexpialidocious", e1.getPassword());
        e1.setUrl("https://window.com");
        assertEquals("https://window.com", e1.getUrl());


        assertTrue(e1.match("2"));
        assertTrue(e1.matchTitle("1"));
        assertFalse(e1.matchTitle("doggy"));

        Icon ic1 = getDatabase().newIcon(27);
        e1.setIcon(ic1);
        assertEquals(e1.getIcon(), ic1);

        // databases have to support setting of standard properties
        e1.setProperty(Entry.STANDARD_PROPERTY_NAME_TITLE, "A title");
        assertEquals("A title", e1.getTitle());
        e1.setProperty(Entry.STANDARD_PROPERTY_NAME_USER_NAME, "username");
        assertEquals("username", e1.getUsername());
        e1.setProperty(Entry.STANDARD_PROPERTY_NAME_NOTES, "notes");
        assertEquals("notes", e1.getNotes());
        e1.setProperty(Entry.STANDARD_PROPERTY_NAME_PASSWORD, "password");
        assertEquals("password", e1.getPassword());
        e1.setProperty(Entry.STANDARD_PROPERTY_NAME_URL, "url");
        assertEquals("url", e1.getUrl());

        try {
            e1.setProperty("silly", "hello");
            assertEquals("hello", e1.getProperty("silly"));
            List<String> properties = new ArrayList<>(Entry.STANDARD_PROPERTY_NAMES);
            properties.add("silly");
            // remove all properties to show that getProperties returns all the values we want
            properties.removeAll(e1.getPropertyNames());
            assertEquals(0, properties.size());
        } catch (UnsupportedOperationException e) {
            // databases don't have to support arbitrary properties
            assertFalse(getDatabase().supportsNonStandardPropertyNames());
            assertArrayEquals(e1.getPropertyNames().toArray(), Entry.STANDARD_PROPERTY_NAMES.toArray());
        }

        e1.setNotes("How much is that doggy in the window?");
        assertEquals("How much is that doggy in the window?", e1.getNotes());
        e1.setTitle("Entry 2");
        assertEquals("Entry 2", e1.getTitle());
        assertEquals("Entry 2", e1.getPath());
    }

    @Test
    default void testTimes() {
        long beforeSecond = Instant.now().toEpochMilli()/1000;
        Entry entry = getDatabase().newEntry();
        long afterSecond = Instant.now().toEpochMilli()/1000;
        long createdSecond = entry.getCreationTime().getTime()/1000;

        assertTrue(createdSecond >= beforeSecond && createdSecond <= afterSecond);
        assertFalse(entry.getExpires());
        assertTrue(entry.getLastAccessTime().getTime()/1000 <= createdSecond);
        assertTrue(entry.getLastModificationTime().getTime()/1000 <= createdSecond);

        entry.setExpires(true);
        entry.setExpiryTime(new Date(createdSecond*1000));

        assertTrue(entry.getExpires());
        assertEquals(createdSecond, entry.getExpiryTime().getTime()/1000);


    }

    @Test
    default void checkAddChangeRemoveProperty() {
        // only applies to databases that support arbitrary properties
        Entry entry = getDatabase().newEntry();
        assertEquals(Entry.STANDARD_PROPERTY_NAMES.size(), entry.getPropertyNames().size());
        try {
            entry.setProperty("test", "test1");
        } catch (UnsupportedOperationException e) {
            if (!getDatabase().supportsNonStandardPropertyNames()) {
                return;
            }
            fail("Database must report that it doesn't support non standard properties");
        }
        assertEquals("test1", entry.getProperty("test"));
        entry.setProperty("test", "test2");
        assertEquals("test2", entry.getProperty("test"));
        assertTrue(entry.removeProperty("test"));
        assertFalse(entry.removeProperty("test"));
        assertFalse(entry.removeProperty("test-test"));
        assertEquals(Entry.STANDARD_PROPERTY_NAMES.size(), entry.getPropertyNames().size());
        try {
            entry.removeProperty(Entry.STANDARD_PROPERTY_NAME_USER_NAME);
            fail("Should not be able to remove standard property");
        } catch (IllegalArgumentException ignore) {
            // ignore as expected
        }
    }


    @Test
    default void testNewEntryInit() {
        Entry e2 = getDatabase().newEntry();
        assertNull(e2.getParent());
        assertEquals("", e2.getPassword());
        assertNotNull(e2.getUuid());
        assertEquals("", e2.getUrl());
        assertEquals("", e2.getNotes());
        assertEquals("", e2.getUsername());
        assertEquals("", e2.getTitle());
        assertNull(e2.getProperty("silly"));
        List<String> l = e2.getPropertyNames();
        l.removeAll(Entry.STANDARD_PROPERTY_NAMES);
        assertEquals(0, l.size());
    }

    @Test
    default void testCopy() throws IOException {
        Entry entry1 = getDatabase().newEntry();
        entry1.setTitle("Entry");
        entry1.setUsername("Username");
        entry1.setPassword("Password");
        entry1.setUrl("https://dont.follow.me");
        entry1.setNotes("Notes");
        entry1.setIcon(getDatabase().newIcon(2));

        // create a new Database
        Database database2 = createDatabase();
        // create a new Entry in new Database
        Entry entry2 = database2.newEntry(entry1);

        assertEquals(entry1.getTitle(), entry2.getTitle());
        assertEquals(entry1.getUsername(), entry2.getUsername());
        assertEquals(entry1.getPassword(), entry2.getPassword());
        assertEquals(entry1.getUrl(), entry2.getUrl());
        assertEquals(entry1.getNotes(), entry2.getNotes());
        assertEquals(entry1.getIcon(), entry2.getIcon());
        assertNotEquals(entry1.getUuid(), entry2.getUuid());

        Group group1 = getDatabase().newGroup();
        group1.setName("Group");
        group1.setIcon(getDatabase().newIcon(3));

        Group group2 = database2.newGroup(group1);
        assertEquals(group1.getName(), group2.getName());
        assertEquals(group1.getIcon(), group2.getIcon());
        assertNotEquals(group1.getUuid(), group2.getUuid());
    }
}
