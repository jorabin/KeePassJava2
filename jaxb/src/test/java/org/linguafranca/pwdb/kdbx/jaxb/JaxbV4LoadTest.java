package org.linguafranca.pwdb.kdbx.jaxb;

import org.junit.Test;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Visitor;
import org.linguafranca.pwdb.kdbx.KdbxCreds;

import java.io.InputStream;

/**
 * @author jo
 */
public class JaxbV4LoadTest {
    @Test
    public void loadKdbxV4() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("V4-AES-Argon2.kdbx");
        JaxbDatabase database = JaxbDatabase.load(new KdbxCreds("123".getBytes()), inputStream);
        database.visit(new Visitor.Print());
        // test what happens to dates in V4
        database.visit(new Visitor.Default(){
            @Override
            public void visit(Entry entry) {
                System.out.println(entry.getCreationTime());
            }
        });
    }

}
