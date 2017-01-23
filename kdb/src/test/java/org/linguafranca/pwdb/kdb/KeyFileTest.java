package org.linguafranca.pwdb.kdb;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

/**
 * @author jo
 */
public class KeyFileTest {

    @Test
    public void openKdbWithKeyFile() throws IOException {
        InputStream key = getClass().getClassLoader().getResourceAsStream("kdb.key");
        KdbCredentials creds = new KdbCredentials.KeyFile("123".getBytes(), key);
        InputStream is = getClass().getClassLoader().getResourceAsStream("kdbwithkey.kdb");
        KdbDatabase db = KdbDatabase.load(creds, is);
        assertEquals(1, db.getRootGroup().getGroupsCount());
        assertEquals("General", db.getRootGroup().getGroups().get(0).getName());
    }
}