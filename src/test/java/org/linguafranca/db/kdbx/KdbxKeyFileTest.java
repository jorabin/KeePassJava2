package org.linguafranca.db.kdbx;

import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.*;

/**
 * @author jo
 */
public class KdbxKeyFileTest {

    @Test
    public void testLoad() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("KeyFileDatabase.key");
        byte[] key = KdbxKeyFile.load(inputStream);
        assertNotNull(key);
        assertEquals(32, key.length);
    }
}