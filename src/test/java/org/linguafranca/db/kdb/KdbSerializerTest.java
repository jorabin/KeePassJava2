package org.linguafranca.db.kdb;

import org.junit.Test;
import org.linguafranca.db.Database;
import org.linguafranca.db.Visitor;

import java.io.InputStream;

/**
 * @author jo
 */
public class KdbSerializerTest {

    @Test
    public void testCreateKdbDatabase() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test123.kdb");
        Database database = KdbDatabase.load(new KdbCredentials.Password("123".getBytes()), inputStream);
        database.visit(new Visitor.Print());
    }
}