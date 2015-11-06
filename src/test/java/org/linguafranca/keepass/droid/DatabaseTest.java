package org.linguafranca.keepass.droid;

import org.junit.Before;
import org.junit.Test;
import org.linguafranca.keepass.db.Entry;
import org.linguafranca.keepass.db.Group;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Jo
 */
public class DatabaseTest {

    org.linguafranca.keepass.db.Database database;

    @Before
    public void setup(){
        database = new DatabaseWrapper();
    }

    @Test
    public void testEmptyDatabase() {
        assertTrue (database.getRootGroup().getName().equals("root"));
        assertTrue (database.getRootGroup().getEntries().size() == 0);
        assertTrue (database.getRootGroup().getGroups().size() == 0);
    }

    @Test
    public void testAddGroup() {
        Group g1 = database.getRootGroup().addGroup(new GroupWrapper("group1"));
        assertTrue (database.getRootGroup().getGroups().size() == 1);
        assertTrue (g1.getName().equals("group1"));
        assertTrue (g1.getGroups().size() == 0);
        assertTrue (g1.getEntries().size() == 0);
        assertTrue ("root is not the parent of its child", g1.getParent().equals(database.getRootGroup()));

        // show that the list of groups is a copy
        database.getRootGroup().getGroups().clear();
        assertTrue(database.getRootGroup().getGroups().size() == 1);
        assertTrue (g1.getEntries().size() == 0);
    }

    @Test
    public void testDeleteGroup () {
        Group g1 = database.getRootGroup().addGroup(new GroupWrapper("group1"));
        List<Group> l1 = database.getRootGroup().findGroups("group1");
        assertTrue(l1.size() == 1);
        Group g2 = l1.get(0);
        assertTrue (g2.equals(g1));
        Group g3 = database.getRootGroup().removeGroup(g2);
        assertTrue (g3.equals(g1));
        assertTrue (g1.getParent() == null);
        assertTrue(database.getRootGroup().getGroups().size() == 0);
        assertTrue(database.getRootGroup().findGroups("group1").size() == 0);
    }

    @Test
    public void testAddRemoveEntry() {
        org.linguafranca.keepass.db.Entry e1 = database.getRootGroup().addEntry(new EntryWrapper("entry1"));
        List<org.linguafranca.keepass.db.Entry> l1 = database.getRootGroup().findEntries("entry1");
        assertTrue(l1.size() == 1);
        Entry e12 = database.getRootGroup().addEntry(new EntryWrapper("entry12"));
        List<org.linguafranca.keepass.db.Entry> l2 = database.getRootGroup().findEntries("entry1");
        assertTrue(l2.size() == 2);
        assertFalse(l2.get(0).equals(l2.get(1)));
        // list is a copy
        l2.clear();
        assertTrue(database.getRootGroup().findEntries("entry1").size() == 2);
        // removed is same as inserted
        org.linguafranca.keepass.db.Entry e12b = database.getRootGroup().removeEntry(e12);
        assertTrue(e12b.equals(e12));
        // has been unhooked from parent
        assertTrue(e12.getParent() == null);
        assertTrue(database.getRootGroup().findEntries("entry1").size() == 1);
    }

    @Test
    public void testSetFields () {
        org.linguafranca.keepass.db.Entry e1 = new EntryWrapper("Entry 1");
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

        org.linguafranca.keepass.db.Icon ic1 = new IconWrapper(27);
        e1.setIcon(ic1);
        assertTrue(e1.getIcon().equals(ic1));


        org.linguafranca.keepass.db.Entry e2 = new EntryWrapper("Entry 2");
        e2.setNotes("How much is that doggy in the window?");
    }

}
