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

package org.linguafranca.pwdb;

import org.junit.Test;
import org.linguafranca.pwdb.kdbx.dom.DomDatabaseWrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Jo
 */
public class BasicDatabaseChecks {

    Database database;

    public BasicDatabaseChecks(Database database) {
        this.database = database;
    }

    @Test
    public void testEmptyDatabase() {
        assertTrue (database.getRootGroup().getName().equals("Root"));
        assertTrue (database.getRootGroup().getEntries().size() == 0);
        assertTrue (database.getRootGroup().getGroups().size() == 0);
    }

    @Test
    public void testAddGroup() {
        Group g1 = database.getRootGroup().addGroup(database.newGroup("group1"));
        assertTrue (database.getRootGroup().getGroups().size() == 1);
        assertTrue (g1.getName().equals("group1"));
        assertTrue (g1.getGroups().size() == 0);
        assertTrue (g1.getEntries().size() == 0);
        assertTrue("root is not the parent of its child", g1.getParent().equals(database.getRootGroup()));

        Group g2 = database.newGroup();
        assertEquals("", g2.getName());
        assertNotNull(g2.getUuid());
        assertEquals(0, g2.getGroups().size());
        assertEquals(0, g2.getEntries().size());

        // show that the list of groups is a copy
        database.getRootGroup().getGroups().clear();
        assertTrue(database.getRootGroup().getGroups().size() == 1);
        assertTrue (g1.getEntries().size() == 0);
    }

    @Test
    public void testDeleteGroup () {
        Group g1 = database.getRootGroup().addGroup(database.newGroup("group1"));
        List<Group> l1 = database.getRootGroup().findGroups("group1");
        assertTrue(l1.size() == 1);
        Group g2 = l1.get(0);
        assertTrue (g2.equals(g1));
        Group g3 = database.getRootGroup().removeGroup(g2);
        assertTrue (g3.equals(g1));
        assertTrue(g1.getParent() == null);
        assertTrue(database.getRootGroup().getGroups().size() == 0);
        assertTrue(database.getRootGroup().findGroups("group1").size() == 0);
    }

    @Test
    public void testAddRemoveEntry() {
        Entry e1 = database.getRootGroup().addEntry(database.newEntry());
        e1.setTitle("entry1");
        List<Entry> l1 = database.findEntries("entry1");
        assertTrue(l1.size() == 1);

        Entry e12 = database.getRootGroup().addEntry(database.newEntry("entry12"));
        List<Entry> l2 = database.findEntries("entry1");
        assertTrue(l2.size() == 2);

        // show that the entries are different
        assertFalse(l2.get(0).equals(l2.get(1)));

        // show that the list is a copy
        l2.clear();
        assertTrue(database.findEntries("entry1").size() == 2);

        // show that we get an equivalent entry when we remove to when we inserted
        Entry e12b = database.getRootGroup().removeEntry(e12);
        assertTrue(e12b.equals(e12));
        // has been unhooked from parent
        assertTrue(e12.getParent() == null);
        assertTrue(database.findEntries("entry1").size() == 1);
    }

    @Test
    public void testSetFields () {
        Entry e1 = database.newEntry("Entry 1");
        e1.setNotes("this looks a little like Entry 2");
        assertTrue(e1.getNotes().equals("this looks a little like Entry 2"));
        e1.setUsername("jake@window.com");
        assertTrue(e1.getUsername().equals("jake@window.com"));
        e1.setPassword("supercalifragelisticexpialidocious");
        assertTrue(e1.getPassword().equals("supercalifragelisticexpialidocious"));
        e1.setUrl("http://window.com");
        assertTrue(e1.getUrl().equals("http://window.com"));


        assertTrue(e1.match("2"));
        assertTrue(e1.matchTitle("1"));
        assertFalse(e1.matchTitle("doggy"));

        Icon ic1 = database.newIcon(27);
        e1.setIcon(ic1);
        assertTrue(e1.getIcon().equals(ic1));

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
            assertArrayEquals(e1.getPropertyNames().toArray(), Entry.STANDARD_PROPERTY_NAMES.toArray());
        }

        e1.setNotes("How much is that doggy in the window?");
        assertEquals("How much is that doggy in the window?", e1.getNotes());
        e1.setTitle("Entry 2");
        assertEquals("Entry 2", e1.getTitle());
        assertEquals("Entry 2", e1.getPath());
    }

    @Test
    public void testNewEntry() {
        Entry e2 = database.newEntry();
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
    public void testCopy() throws IOException {
        Entry entry1 = database.newEntry();
        entry1.setTitle("Entry");
        entry1.setUsername("Username");
        entry1.setPassword("Password");
        entry1.setUrl("http://dont.follow.me");
        entry1.setNotes("Notes");
        entry1.setIcon(database.newIcon(2));

        // create a new Database
        Database database2 = new DomDatabaseWrapper();
        // create a new Entry in new Database
        Entry entry2 = database2.newEntry(entry1);

        assertEquals(entry1.getTitle(), entry2.getTitle());
        assertEquals(entry1.getUsername(), entry2.getUsername());
        assertEquals(entry1.getPassword(), entry2.getPassword());
        assertEquals(entry1.getUrl(), entry2.getUrl());
        assertEquals(entry1.getNotes(), entry2.getNotes());
        assertEquals(entry1.getIcon(), entry2.getIcon());
        assertNotEquals(entry1.getUuid(), entry2.getUuid());

        Group group1 = database.newGroup();
        group1.setName("Group");
        group1.setIcon(database.newIcon(3));

        Group group2 = database2.newGroup(group1);
        assertEquals(group1.getName(), group2.getName());
        assertEquals(group1.getIcon(), group2.getIcon());
        assertNotEquals(group1.getUuid(), group2.getUuid());
    }
}
