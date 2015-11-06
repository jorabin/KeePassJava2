package org.linguafranca.keepass.kdb;

import org.junit.Test;
import org.linguafranca.keepass.db.Database;

import java.io.InputStream;

import static org.junit.Assert.*;

/**
 * @author jo
 */
public class KdbSerializerTest {

    @Test
    public void testCreateKdbDatabase() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test.kdb");
        KdbDatabase database = KdbSerializer.createKdbDatabase("123", new KdbHeader(), inputStream);
        database.visit(new Database.PrintVisitor());
    }
}