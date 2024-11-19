package org.linguafranca.pwdb.kdbx.dom;

import org.junit.Test;
import org.linguafranca.pwdb.Visitor;
import org.linguafranca.pwdb.kdbx.KdbxCreds;

import java.io.InputStream;
import java.io.PrintStream;

import static org.linguafranca.test.util.TestUtil.getTestPrintStream;

/**
 * @author jo
 */
public class DomV4Test {

    static PrintStream printStream = getTestPrintStream();

    @Test
    public void loadKdbxV4() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("V4-AES-Argon2.kdbx");
        DomDatabaseWrapper database = DomDatabaseWrapper.load(new KdbxCreds("123".getBytes()), inputStream);
        database.visit(new Visitor.Print<DomDatabaseWrapper, DomGroupWrapper, DomEntryWrapper, DomIconWrapper>(printStream));
        // test what happens to dates in V4
        database.visit(new Visitor.Default<DomDatabaseWrapper, DomGroupWrapper, DomEntryWrapper, DomIconWrapper>(){

            @Override
            public void visit(DomEntryWrapper entry) {
                printStream.println(entry.getCreationTime());
            }
        });
    }

}
