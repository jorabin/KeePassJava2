package org.linguafranca.pwdb.checks;

import org.junit.Test;
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Group;
import org.linguafranca.pwdb.Icon;

import static org.junit.Assert.*;

/**
 * @author jo
 */
public abstract class RecycleBinChecks  <D extends Database<D,G,E,I>, G extends Group<D,G,E,I>, E extends Entry<D,G,E,I>, I extends Icon>  {

    protected Database<D,G,E,I> database;

    @Test
    public void recycleBinEntries() {
        database.enableRecycleBin(false);
        assertFalse(database.isRecycleBinEnabled());
        assertNull(database.getRecycleBin());
        database.enableRecycleBin(true);
        assertTrue(database.isRecycleBinEnabled());
        G recycleBin = database.getRecycleBin();
        assertNotNull(recycleBin);

        E entry = database.newEntry();
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
    public void recycleBinGroups() {
        database.enableRecycleBin(true);
        assertTrue(database.isRecycleBinEnabled());
        G recycleBin = database.getRecycleBin();
        assertNotNull(recycleBin);

        G group= database.newGroup();
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
