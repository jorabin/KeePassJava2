package org.linguafranca.pwdb.kdbx.jaxb;

import org.junit.Test;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Visitor;
import org.linguafranca.pwdb.kdbx.KdbxCreds;

import java.io.InputStream;
import java.io.PrintStream;

import static org.linguafranca.util.TestUtil.getTestPrintStream;

/**
 * @author jo
 */
public class JaxbV4LoadTest {

    static PrintStream printStream = getTestPrintStream();

    @Test
    public void loadKdbxV4() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("V4-AES-Argon2.kdbx");
        JaxbDatabase database = JaxbDatabase.load(new KdbxCreds("123".getBytes()), inputStream);
        database.visit(new Visitor.Print(printStream));
        // test what happens to dates in V4
        database.visit(new Visitor.Default(){
            @Override
            public void visit(Entry entry) {
                printStream.println(entry.getCreationTime());
            }
        });
    }

}
