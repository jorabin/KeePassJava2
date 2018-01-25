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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Jo
 */
public abstract class BasicDatabaseChecks <D extends Database<D,G,E,I>, G extends Group<D,G,E,I>, E extends Entry<D,G,E,I>, I extends Icon> {

    protected Database<D,G,E,I> database;

    public abstract Database<D,G,E,I> createDatabase() throws IOException;

    public BasicDatabaseChecks() throws IOException {
        this.database = createDatabase();
    }

    @Test
    public void testEmptyDatabase() {
        Assert.assertTrue (database.getRootGroup().getName().equals("Root"));
        Assert.assertTrue (database.getRootGroup().getEntries().size() == 0);
        Assert.assertTrue (database.getRootGroup().getGroups().size() == 0);
    }

    @Test
    public void testAddGroup() {
        Group g1 = database.getRootGroup().addGroup(database.newGroup("group1"));
        Assert.assertTrue (database.getRootGroup().getGroups().size() == 1);
        Assert.assertTrue (g1.getName().equals("group1"));
        Assert.assertTrue (g1.getGroups().size() == 0);
        Assert.assertTrue (g1.getEntries().size() == 0);
        Assert.assertTrue("root is not the parent of its child", g1.getParent().equals(database.getRootGroup()));

        Group g2 = database.newGroup();
        assertEquals("", g2.getName());
        Assert.assertNotNull(g2.getUuid());
        assertEquals(0, g2.getIcon().getIndex());
        assertEquals(0, g2.getGroups().size());
        assertEquals(0, g2.getEntries().size());

        // show that the list of groups is a copy
        database.getRootGroup().getGroups().clear();
        Assert.assertTrue(database.getRootGroup().getGroups().size() == 1);
        Assert.assertTrue (g1.getEntries().size() == 0);
    }

    @Test
    public void testDeleteGroup () {
        Group<D,G,E,I> g1 = database.getRootGroup().addGroup(database.newGroup("group1"));
        List<? extends G> l1 = database.getRootGroup().findGroups("group1");
        Assert.assertTrue(l1.size() == 1);
        G g2 = l1.get(0);
        Assert.assertTrue (g2.equals(g1));
        Group g3 = database.getRootGroup().removeGroup(g2);
        Assert.assertTrue (g3.equals(g1));
        Assert.assertTrue(g1.getParent() == null);
        Assert.assertTrue(database.getRootGroup().getGroups().size() == 0);
        Assert.assertTrue(database.getRootGroup().findGroups("group1").size() == 0);
    }

    @Test
    public void testAddRemoveEntry() {
        E e1 = database.getRootGroup().addEntry(database.newEntry());
        e1.setTitle("entry1");
        List<? extends E> l1 = database.findEntries("entry1");
        Assert.assertTrue(l1.size() == 1);

        E e12 = database.getRootGroup().addEntry(database.newEntry("entry12"));
        List<? extends E> l2 = database.findEntries("entry1");
        Assert.assertTrue(l2.size() == 2);

        // show that the entries are different
        Assert.assertFalse(l2.get(0).equals(l2.get(1)));

        // show that the list is a copy
        l2.clear();
        Assert.assertTrue(database.findEntries("entry1").size() == 2);

        // show that we get an equivalent entry when we remove to when we inserted
        Entry e12b = database.getRootGroup().removeEntry(e12);
        Assert.assertTrue(e12b.equals(e12));
        // has been unhooked from parent
        Assert.assertTrue(e12.getParent() == null);
        Assert.assertTrue(database.findEntries("entry1").size() == 1);
    }

    @Test
    public void testSetFields () {
        E e1 = database.newEntry("Entry 1");
        e1.setNotes("this looks a little like Entry 2");
        Assert.assertTrue(e1.getNotes().equals("this looks a little like Entry 2"));
        e1.setUsername("jake@window.com");
        Assert.assertTrue(e1.getUsername().equals("jake@window.com"));
        e1.setPassword("supercalifragelisticexpialidocious");
        Assert.assertTrue(e1.getPassword().equals("supercalifragelisticexpialidocious"));
        e1.setUrl("http://window.com");
        Assert.assertTrue(e1.getUrl().equals("http://window.com"));


        Assert.assertTrue(e1.match("2"));
        Assert.assertTrue(e1.matchTitle("1"));
        Assert.assertFalse(e1.matchTitle("doggy"));

        I ic1 = database.newIcon(27);
        e1.setIcon(ic1);
        Assert.assertTrue(e1.getIcon().equals(ic1));

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
            Assert.assertEquals(0, properties.size());
        } catch (UnsupportedOperationException e) {
            // databases don't have to support arbitrary properties
            assertTrue(!database.supportsNonStandardPropertyNames());
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
        long before = (new Date().getTime() / 1000L) * 1000L; // round to next lower second
        E entry = database.newEntry();
        long after = (new Date().getTime()/ 1000L) * 1000L; // round to next lower second
        long created = entry.getCreationTime().getTime();
        assertTrue(created >= before && created <= after);
        assertFalse(entry.getExpires());
        assertTrue(entry.getLastAccessTime().getTime() <= created);
        assertTrue(entry.getLastModificationTime().getTime() <= created);

        entry.setExpires(true);
        entry.setExpiryTime(new Date(created));

        assertTrue(entry.getExpires());
        assertEquals(created, entry.getExpiryTime().getTime());


    }

    @Test
    public void checkAddChangeRemoveProperty() {
        // only applies to databases that support arbitrary properties
        E entry = database.newEntry();
        assertEquals(Entry.STANDARD_PROPERTY_NAMES.size(), entry.getPropertyNames().size());
        try {
            entry.setProperty("test", "test1");
        } catch (UnsupportedOperationException e) {
            if (!database.supportsNonStandardPropertyNames()) {
                return;
            }
            fail("Database must report that it doesn't support non standrad properties");
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
        entry1.setUrl("http://dont.follow.me");
        entry1.setNotes("Notes");
        entry1.setIcon(database.newIcon(2));

        // create a new Database
        Database<D,G,E,I> database2 = createDatabase();
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
