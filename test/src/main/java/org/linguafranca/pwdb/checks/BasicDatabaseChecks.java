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

package org.linguafranca.pwdb.checks;

import org.junit.Assert;
import org.junit.Test;
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Group;
import org.linguafranca.pwdb.Icon;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Jo
 */
public abstract class BasicDatabaseChecks <G extends Group<G,E>, E extends Entry<G,E>> {

    protected Database<G, E> database;

    public abstract Database<G,E> createDatabase() throws IOException;

    public BasicDatabaseChecks() throws IOException {
        this.database = createDatabase();
    }

    @Test
    public void testEmptyDatabase() {
        assertEquals("Root", database.getRootGroup().getName());
        assertEquals(0, database.getRootGroup().getEntries().size());
        assertEquals(0, database.getRootGroup().getGroups().size());
    }

    @Test
    public void testAddGroup() {
        G g1 = database.getRootGroup().addGroup(database.newGroup("group1"));
        assertEquals(1, database.getRootGroup().getGroups().size());
        assertEquals("group1", g1.getName());
        assertEquals(0, g1.getGroups().size());
        assertEquals(0, g1.getEntries().size());
        assertEquals("root is not the parent of its child", g1.getParent(), database.getRootGroup());

        G g2 = database.newGroup();
        assertEquals("", g2.getName());
        Assert.assertNotNull(g2.getUuid());
        assertEquals(0, g2.getIcon().getIndex());
        assertEquals(0, g2.getGroups().size());
        assertEquals(0, g2.getEntries().size());

        // show that the list of groups is a copy
        database.getRootGroup().getGroups().clear();
        assertEquals(1, database.getRootGroup().getGroups().size());
        assertEquals(0, g1.getEntries().size());
    }

    @Test
    public void testDeleteGroup () {
        Group<G,E> g1 = database.getRootGroup().addGroup(database.newGroup("group1"));
        List<? extends G> l1 = database.getRootGroup().findGroups("group1");
        assertEquals(1, l1.size());
        G g2 = l1.get(0);
        assertEquals(g2, g1);
        G g3 = database.getRootGroup().removeGroup(g2);
        assertEquals(g3, g1);
        assertNull(g1.getParent());
        assertEquals(0, database.getRootGroup().getGroups().size());
        assertEquals(0, database.getRootGroup().findGroups("group1").size());
    }

    @Test
    public void testAddRemoveEntry() {
        E e1 = database.getRootGroup().addEntry(database.newEntry());
        e1.setTitle("entry1");
        List<? extends E> l1 = database.findEntries("entry1");
        assertEquals(1, l1.size());

        E e12 = database.getRootGroup().addEntry(database.newEntry("entry12"));
        List<? extends E> l2 = database.findEntries("entry1");
        assertEquals(2, l2.size());

        // show that the entries are different
        assertNotEquals(l2.get(0), l2.get(1));

        // show that the list is a copy
        l2.clear();
        assertEquals(2, database.findEntries("entry1").size());

        // show that we get an equivalent entry when we remove to when we inserted
        E e12b = database.getRootGroup().removeEntry(e12);
        assertEquals(e12b, e12);
        // has been unhooked from parent
        assertNull(e12.getParent());
        assertEquals(1, database.findEntries("entry1").size());
    }

    @Test
    public void testSetFields () {
        E e1 = database.newEntry("Entry 1");
        e1.setNotes("this looks a little like Entry 2");
        assertEquals("this looks a little like Entry 2", e1.getNotes());
        e1.setUsername("jake@window.com");
        assertEquals("jake@window.com", e1.getUsername());
        e1.setPassword("supercalifragelisticexpialidocious");
        assertEquals("supercalifragelisticexpialidocious", e1.getPassword());
        e1.setUrl("https://window.com");
        assertEquals("https://window.com", e1.getUrl());


        Assert.assertTrue(e1.match("2"));
        Assert.assertTrue(e1.matchTitle("1"));
        Assert.assertFalse(e1.matchTitle("doggy"));

        Icon ic1 = database.newIcon(27);
        e1.setIcon(ic1);
        assertEquals(e1.getIcon(), ic1);

        // databases have to support setting of standard properties
        e1.setPropertyValue(Entry.STANDARD_PROPERTY_NAME_TITLE, "A title");
        assertEquals("A title", e1.getTitle());
        e1.setPropertyValue(Entry.STANDARD_PROPERTY_NAME_USER_NAME, "username");
        assertEquals("username", e1.getUsername());
        e1.setPropertyValue(Entry.STANDARD_PROPERTY_NAME_NOTES, "notes");
        assertEquals("notes", e1.getNotes());
        e1.setPropertyValue(Entry.STANDARD_PROPERTY_NAME_PASSWORD, "password");
        assertEquals("password", e1.getPassword());
        e1.setPropertyValue(Entry.STANDARD_PROPERTY_NAME_URL, "url");
        assertEquals("url", e1.getUrl());

        try {
            e1.setPropertyValue("silly", "hello");
            assertEquals("hello", e1.getProperty("silly"));
            List<String> properties = new ArrayList<>(Entry.STANDARD_PROPERTY_NAMES);
            properties.add("silly");
            // remove all properties to show that getProperties returns all the values we want
            properties.removeAll(e1.getPropertyNames());
            Assert.assertEquals(0, properties.size());
        } catch (UnsupportedOperationException e) {
            // databases don't have to support arbitrary properties
            assertFalse(database.supportsNonStandardPropertyNames());
            assertArrayEquals(e1.getPropertyNames().toArray(), Entry.STANDARD_PROPERTY_NAMES.toArray());
        }

        e1.setNotes("How much is that doggy in the window?");
        assertEquals("How much is that doggy in the window?", e1.getNotes());
        e1.setTitle("Entry 2");
        assertEquals("Entry 2", e1.getTitle());
        assertEquals("Entry 2", e1.getPath());
    }

    @Test
    public void testTimes() {
        long beforeSecond = Instant.now().toEpochMilli()/1000;
        E entry = database.newEntry();
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
    public void checkAddChangeRemoveProperty() {
        // only applies to databases that support arbitrary properties
        E entry = database.newEntry();
        assertEquals(Entry.STANDARD_PROPERTY_NAMES.size(), entry.getPropertyNames().size());
        try {
            entry.setPropertyValue("test", "test1");
        } catch (UnsupportedOperationException e) {
            if (!database.supportsNonStandardPropertyNames()) {
                return;
            }
            fail("Database must report that it doesn't support non standrad properties");
        }
        assertEquals("test1", entry.getProperty("test"));
        entry.setPropertyValue("test", "test2");
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
    public void testNewEntry() {
        E e2 = database.newEntry();
        Assert.assertNull(e2.getParent());
        assertEquals("", e2.getPassword());
        Assert.assertNotNull(e2.getUuid());
        assertEquals("", e2.getUrl());
        assertEquals("", e2.getNotes());
        assertEquals("", e2.getUsername());
        assertEquals("", e2.getTitle());
        Assert.assertNull(e2.getProperty("silly"));
        List<String> l = e2.getPropertyNames();
        l.removeAll(Entry.STANDARD_PROPERTY_NAMES);
        Assert.assertEquals(0, l.size());
    }

    @Test
    public void testCopy() throws IOException {
        E entry1 = database.newEntry();
        entry1.setTitle("Entry");
        entry1.setUsername("Username");
        entry1.setPassword("Password");
        entry1.setUrl("https://dont.follow.me");
        entry1.setNotes("Notes");
        entry1.setIcon(database.newIcon(2));

        // create a new Database
        Database<G,E> database2 = createDatabase();
        // create a new Entry in new Database
        E entry2 = database2.newEntry(entry1);

        assertEquals(entry1.getTitle(), entry2.getTitle());
        assertEquals(entry1.getUsername(), entry2.getUsername());
        assertEquals(entry1.getPassword(), entry2.getPassword());
        assertEquals(entry1.getUrl(), entry2.getUrl());
        assertEquals(entry1.getNotes(), entry2.getNotes());
        assertEquals(entry1.getIcon(), entry2.getIcon());
        assertNotEquals(entry1.getUuid(), entry2.getUuid());

        G group1 = database.newGroup();
        group1.setName("Group");
        group1.setIcon(database.newIcon(3));

        G group2 = database2.newGroup(group1);
        assertEquals(group1.getName(), group2.getName());
        assertEquals(group1.getIcon(), group2.getIcon());
        assertNotEquals(group1.getUuid(), group2.getUuid());
    }
}
