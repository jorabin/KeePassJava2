package org.linguafranca.keepass.dom;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import org.junit.Test;
import org.linguafranca.keepass.db.Credentials;
import org.linguafranca.keepass.db.Entry;
import org.linguafranca.keepass.db.Formatter;
import org.linguafranca.keepass.db.Group;
import org.linguafranca.keepass.kdbx.KdbxFormatter;

import java.io.*;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @author jo
 */
public class DomDatabaseWrapperTest {

    @Test
    public void saveNewDatabase () throws IOException {
        DomDatabaseWrapper database = createNewDatabase();

        FileOutputStream outputStream = new FileOutputStream("test.kdbx");
        database.save(new KdbxFormatter(), new Credentials.Password("123"), outputStream);
    }

    @Test
    public void inspectNewDatabase () throws IOException {
        DomDatabaseWrapper database = createNewDatabase();

        ByteOutputStream outputStream = new ByteOutputStream();
        database.save(new Formatter.NoOp(), new Credentials.NoOp(), outputStream);
        System.out.println(outputStream.toString());
    }

    @Test
    public void inspectExistingDatabase () throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test123.kdbx");
        DomDatabaseWrapper database = new DomDatabaseWrapper(new KdbxFormatter(), new Credentials.Password("123"), inputStream);

        ByteOutputStream outputStream = new ByteOutputStream();
        database.save(new Formatter.NoOp(), new Credentials.NoOp(), outputStream);
        System.out.println(outputStream.toString());
    }

    @Test
    public void inspectExistingDatabase2() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test1234.kdbx");
        DomDatabaseWrapper database = new DomDatabaseWrapper(new KdbxFormatter(), new Credentials.Password("1234"), inputStream);

        ByteOutputStream outputStream = new ByteOutputStream();
        database.save(new Formatter.NoOp(), new Credentials.NoOp(), outputStream);
        System.out.println(outputStream.toString());
    }

    @Test
    public void testExistingDatabase () throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test123.kdbx");
        DomDatabaseWrapper database = new DomDatabaseWrapper(new KdbxFormatter(), new Credentials.Password("123"), inputStream);
        List<Entry> entries = database.findEntries(new Entry.Matcher() {
            @Override
            public boolean matches(Entry entry) {
                return entry.getTitle().equals("hello world");
            }});

        assertEquals(1, entries.size());
        assertEquals("pass", entries.get(0).getPassword());
        ByteOutputStream outputStream = new ByteOutputStream();
        database.save(new Formatter.NoOp(), new Credentials.NoOp(), outputStream);
        System.out.println(outputStream.toString());
    }

    private DomDatabaseWrapper createNewDatabase() throws IOException {
        DomDatabaseWrapper database = new DomDatabaseWrapper();
        Group root = database.getRootGroup();
        assertTrue(root.isRootGroup());
        assertEquals(0, root.getGroups().size());
        assertEquals(0, root.getEntries().size());

        assertTrue(database.isProtected("Password"));
        assertFalse(database.isProtected("Title"));
        assertFalse(database.isProtected("Bogus"));

        assertEquals("New Database", database.getName());
        database.setName("Modified Database");
        assertEquals("Modified Database", database.getName());

        assertEquals("Empty Database", database.getDescription());
        database.setDescription("Test Database");
        assertEquals("Test Database", database.getDescription());

        Group newGroup = database.newGroup();
        UUID newGroupUUID = newGroup.getUuid();

        root.addGroup(newGroup);
        assertEquals("New Group", newGroup.getName());
        assertFalse(newGroup.isRootGroup());
        assertTrue(root.isRootGroup());

        assertEquals(1, root.getGroups().size());
        assertEquals(newGroupUUID, root.getGroups().get(0).getUuid());

        newGroup.setParent(root);
        root.addGroup(newGroup);

        root.removeGroup(newGroup);
        assertTrue(newGroup.getParent() == null);
        assertEquals(0, root.getGroups().size());
        root.addGroup(newGroup);
        assertEquals(1, root.getGroups().size());
        assertEquals(newGroupUUID, root.getGroups().get(0).getUuid());

        try {
            root.setParent(newGroup);
            fail("Cannot add root group to another group");
        } catch (Exception ignored) {}

        Group group2 = database.newGroup();
        group2.setName("Group 2");
        newGroup.addGroup(group2);
        assertEquals(1, newGroup.getGroups().size());
        assertEquals(1, root.getGroups().size());

        root.addGroup(group2);
        assertEquals(0, newGroup.getGroups().size());
        assertEquals(2, root.getGroups().size());

        Entry entry1 = database.newEntry();
        entry1.setTitle("A new entry");
        assertEquals("A new entry", entry1.getTitle());
        entry1.setUsername("user name");
        assertEquals("user name", entry1.getUsername());
        entry1.setProperty("random", "new");
        assertEquals("new", entry1.getProperty("random"));
        entry1.setProperty("random", "old");
        assertEquals("old", entry1.getProperty("random"));


        group2.addEntry(entry1);

        assertEquals(1, group2.getEntries().size());

        entry1.setPassword("pass");
        assertEquals("pass", entry1.getPassword());
        return database;
    }

}